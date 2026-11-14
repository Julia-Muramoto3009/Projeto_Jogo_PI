package com.grupito.bomberman.components;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.inc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;

import javafx.geometry.Point2D;

/**
 * Inimigo que anda de célula em célula.
 * Ao chegar no centro de uma célula, escolhe a próxima direção:
 * segue reto na maioria das vezes, vira de vez em quando e só volta se estiver
 * num beco.
 */
public class Monster extends Component {

    private static final Point2D[] DIRS = {
            new Point2D(1, 0), new Point2D(-1, 0), new Point2D(0, 1), new Point2D(0, -1)
    };

    private final double speed;
    private final Random rnd = new Random();

    private Point2D dir = Point2D.ZERO;
    private Point2D target = null; // canto superior esquerdo da célula de destino
    private boolean dead = false;

    public Monster(double speed) {
        this.speed = speed;
    }

    @Override
    public void onAdded() {
        // encaixa na grade, caso tenha sido criado fora do alinhamento
        double t = Config.TILE;
        entity.setPosition(Math.round(entity.getX() / t) * t, Math.round(entity.getY() / t) * t);
        inc("enemies", 1);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead)
            return;

        if (target == null) {
            dir = chooseDirection();
            if (dir.equals(Point2D.ZERO))
                return; // cercado: espera
            target = entity.getPosition().add(dir.multiply(Config.TILE));
        }

        double step = speed * tpf;
        double dist = entity.getPosition().distance(target);

        if (step >= dist) {
            entity.setPosition(target);
            target = null;
        } else {
            entity.translate(dir.multiply(step));
        }
    }

    private Point2D chooseDirection() {
        List<Point2D> free = new ArrayList<>();
        for (Point2D d : DIRS) {
            if (!isBlocked(entity.getPosition().add(d.multiply(Config.TILE)))) {
                free.add(d);
            }
        }
        if (free.isEmpty())
            return Point2D.ZERO;

        Point2D back = dir.multiply(-1);
        List<Point2D> forward = new ArrayList<>(free);
        forward.remove(back);

        if (forward.isEmpty())
            return back; // beco sem saída
        if (forward.contains(dir) && rnd.nextDouble() < 0.75)
            return dir; // segue reto
        return forward.get(rnd.nextInt(forward.size()));
    }

    /**
     * Uma célula está bloqueada se estiver fora do mapa ou tiver parede, tijolo ou
     * bomba.
     */
    private boolean isBlocked(Point2D cell) {
        double t = Config.TILE;
        if (cell.getX() < 0 || cell.getY() < 0
                || cell.getX() + t > Config.COLS * t
                || cell.getY() + t > Config.ROWS * t) {
            return true;
        }
        for (Entity e : getGameWorld().getEntitiesByType(EntityType.WALL, EntityType.BRICK, EntityType.BOMB)) {
            if (e.getPosition().distance(cell) < t / 2.0) {
                return true;
            }
        }
        return false;
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * Marca como morto e remove do mundo. Pontos e vitória são tratados no
     * GameFlow.
     */
    public void kill() {
        dead = true;
        entity.removeFromWorld();
    }
}