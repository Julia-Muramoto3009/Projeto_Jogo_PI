package com.grupito.bomberman.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.runOnce;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Monster extends Component {

    private static final Point2D[] DIRS = {
            new Point2D(1, 0), new Point2D(-1, 0), new Point2D(0, 1), new Point2D(0, -1)
    };

    private final double speed;
    private final Random rnd = new Random();

    private Point2D dir = Point2D.ZERO;
    private Point2D target = null;
    private boolean dead = false;

    private AnimatedTexture texture;
    private AnimationChannel animDown, animUp, animLeft, animRight;
    private AnimationChannel animHit, animDead;
    private AnimationChannel currentChannel;

    public Monster(double speed) {
        this.speed = speed;
    }

    @Override
    public void onAdded() {
        double t = Config.TILE;
        entity.setPosition(Math.round(entity.getX() / t) * t, Math.round(entity.getY() / t) * t);
        FXGL.inc("enemies", 1);

        Image sheet = FXGL.image("Monstro1Sprite.png");
        int fw = (int) (sheet.getWidth() / 4);
        int fh = (int) (sheet.getHeight() / 4);

        // Canais de animação baseados nos quadros válidos
        animDown  = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.5), 1, 3);
        animLeft  = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.4), 4, 5);
        animRight = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.4), 7, 8);
        animUp    = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.5), 10, 13);

        animHit   = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.25), 0, 0);
        animDead  = new AnimationChannel(sheet, 4, fw, fh, Duration.seconds(0.5), 14, 14);

        currentChannel = animDown;

        // Limpa qualquer nó visual anterior que possa ter vindo de fábrica por engano
        entity.getViewComponent().clearChildren();

        // Cria a textura e define o tamanho exato do tile
        texture = new AnimatedTexture(currentChannel);
        texture.setFitWidth(t);
        texture.setFitHeight(t);
        texture.loop();

        // Adiciona a textura limpa à entidade
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead)
            return;

        if (target == null) {
            dir = chooseDirection();
            
            // SE ESTIVER PRESO: zera tudo e mantém o frame atual estático
            if (dir.equals(Point2D.ZERO)) {
                target = null;
                if (texture != null && currentChannel != null) {
                    // Mantém o monstro estático no primeiro frame do canal atual sem bugar
                    texture.playAnimationChannel(currentChannel);
                    // Opcional: se quiser congelar na hora, você pode apenas garantir que o destino seja nulo
                }
                return;
            }
            
            target = entity.getPosition().add(dir.multiply(Config.TILE));
            updateAnimationDirection();
        }

        // Movimento normal até o destino
        double step = speed * tpf;
        double dist = entity.getPosition().distance(target);

        if (step >= dist) {
            entity.setPosition(target);
            target = null; // Chegou ao bloco, vai reavaliar no próximo frame se pode continuar
        } else {
            entity.translate(dir.multiply(step));
        }
    }

    private void updateAnimationDirection() {
        if (dead) return;

        AnimationChannel nextChannel = animDown;

        if (dir.getY() < 0) {
            nextChannel = animUp;
        } else if (dir.getY() > 0) {
            nextChannel = animDown;
        } else if (dir.getX() < 0) {
            nextChannel = animLeft;
        } else if (dir.getX() > 0) {
            nextChannel = animRight;
        }

        // Só altera se realmente mudou de direção
        if (currentChannel != nextChannel) {
            currentChannel = nextChannel;
            texture.loopAnimationChannel(currentChannel);
        }
    }

    private boolean isBlocked(Point2D cell) {
        double t = Config.TILE;
        
        // Limites do mapa
        if (cell.getX() < 0 || cell.getY() < 0
                || cell.getX() + t > Config.COLS * t
                || cell.getY() + t > Config.ROWS * t) {
            return true;
        }
        
        // Verifica se existe alguma parede, bloco destrutível ou bomba na célula de destino
        for (Entity e : FXGL.getGameWorld().getEntitiesByType(EntityType.WALL, EntityType.BRICK, EntityType.BOMB)) {
            if (e.getPosition().distance(cell) < t / 4.0) {
                return true;
            }
        }
        
        return false;
    }

    private Point2D chooseDirection() {
        List<Point2D> free = new ArrayList<>();
        
        for (Point2D d : DIRS) {
            Point2D nextCell = entity.getPosition().add(d.multiply(Config.TILE));
            if (!isBlocked(nextCell)) {
                free.add(d);
            }
        }
        
        // Se a lista estiver vazia, retorna estritamente ZERO
        if (free.isEmpty()) {
            return Point2D.ZERO;
        }

        Point2D back = dir.multiply(-1);
        List<Point2D> forward = new ArrayList<>(free);
        forward.remove(back);

        if (forward.isEmpty())
            return back;
            
        if (forward.contains(dir) && rnd.nextDouble() < 0.75)
            return dir;
            
        return forward.get(rnd.nextInt(forward.size()));
    }

    public boolean isDead() {
        return dead;
    }

    public void playAttackAnimation() {
        if (dead) return;
        texture.playAnimationChannel(animHit);
        runOnce(() -> {
            if (!dead) {
                updateAnimationDirection();
            }
        }, Duration.seconds(0.4));
    }

    public void kill() {
        if (dead) return;
        dead = true;

        currentChannel = animHit;
        texture.playAnimationChannel(animHit);

        runOnce(() -> {
            currentChannel = animDead;
            texture.playAnimationChannel(animDead);
        }, Duration.seconds(0.25));

        runOnce(() -> {
            entity.removeFromWorld();
        }, Duration.seconds(0.85));
    }
}