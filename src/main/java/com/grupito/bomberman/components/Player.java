package com.grupito.bomberman.components;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.tpf;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;
import com.almasb.*;
import javafx.geometry.Rectangle2D;


/**
 * Comportamento do jogador: movimento com bloqueio por obstáculos.
 *
 * Regras:
 *  - Parede e tijolo bloqueiam sempre.
 *  - Bomba bloqueia, EXCETO a bomba em que o jogador já está dentro
 *    (ele precisa conseguir sair do tile onde a colocou).
 *  - O jogador não sai do mapa.
 */
public class Player extends Component {

    private static final double SPEED = 200;   // pixels por segundo
    private static final double MARGIN = 8;    // folga da hitbox

    public void moveRight() { move(SPEED * tpf(), 0); }
    public void moveLeft()  { move(-SPEED * tpf(), 0); }
    public void moveUp()    { move(0, -SPEED * tpf()); }
    public void moveDown()  { move(0, SPEED * tpf()); }


    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk_W, animWalk_A, animWalk_S, animWalk_D;
    // Animação
        public AnimationComponent() {
        animIdle = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 2, 2);
        animWalk_W = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 3, 4);
        animWalk_A = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 5, 7);
        animWalk_S = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 2, 4);
        animWalk_D = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 8, 10);

        texture = new AnimatedTexture(animIdle);
    }




    /**
     Limitação do movimento do personagem
     */
    private void move(double dx, double dy) {
        if (dx != 0 && canMoveTo(entity.getX() + dx, entity.getY())) {
            entity.translateX(dx);
        }
        if (dy != 0 && canMoveTo(entity.getX(), entity.getY() + dy)) {
            entity.translateY(dy);
        }
    }

    /** Retângulo ocupado pelo jogador se ele estivesse em (x, y). */
    private Rectangle2D hitbox(double x, double y) {
        return new Rectangle2D(
                x + MARGIN, y + MARGIN,
                entity.getWidth() - 2 * MARGIN, entity.getHeight() - 2 * MARGIN);
    }

    private boolean canMoveTo(double newX, double newY) {
        // Limites do mapa
        if (newX < 0 || newY < 0
                || newX + entity.getWidth() > Config.COLS * Config.TILE
                || newY + entity.getHeight() > Config.ROWS * Config.TILE) {
            return false;
        }

        Rectangle2D next = hitbox(newX, newY);
        Rectangle2D now = hitbox(entity.getX(), entity.getY());

        for (Entity e : getGameWorld().getEntitiesByType(EntityType.WALL, EntityType.BRICK, EntityType.BOMB)) {
            Rectangle2D box = new Rectangle2D(e.getX(), e.getY(), e.getWidth(), e.getHeight());

            if (!next.intersects(box)) {
                continue; // não encosta neste obstáculo
            }

            // Bomba em que o jogador já está dentro: pode sair dela
            if (e.isType(EntityType.BOMB) && now.intersects(box)) {
                continue;
            }

            return false; // obstáculo no caminho
        }
        return true;
    }
}