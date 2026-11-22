package com.grupito.bomberman.components;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.tpf;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;


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
    private static final double IDLE_DELAY = 0.1;
    
    private int lives = Config.PLAYER_LIVES;
    private int maxBombs = 1;         // Limite inicial de bombas simultâneas
    private int activeBombs = 0;      // Quantas bombas o jogador colocou e ainda estão ativas no mapa
    private int explosionRange = Config.BOMB_RANGE; // Alcance atual da explosão

    // Getters e Setters para os Upgrades
    public int getLives() { return lives; }
    public void addLife() { if (lives < 5) lives++; }

    public int getMaxBombs() { return maxBombs; }
    public void increaseMaxBombs() { maxBombs++; }

    public int getActiveBombs() { return activeBombs; }
    public void incrementActiveBombs() { activeBombs++; }
    public void decrementActiveBombs() { activeBombs = Math.max(0, activeBombs - 1); }

    public int getExplosionRange() { return explosionRange; }
    public void increaseExplosionRange() { explosionRange++; }

    private final AnimatedTexture texture;
    private final AnimationChannel animIdle, animWalk_W, animWalk_A, animWalk_S, animWalk_D;
    private double timeWithoutMovement;

    public Player() {
        animIdle = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 4, 4);
        animWalk_W = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 0, 2);
        animWalk_A = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 6, 8);
        animWalk_S = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 3, 5);
        animWalk_D = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 9, 11);

        texture = new AnimatedTexture(animIdle);
    }

    public void moveRight() { move(SPEED * tpf(), 0, animWalk_D); }
    public void moveLeft()  { move(-SPEED * tpf(), 0, animWalk_A); }
    public void moveUp()    { move(0, -SPEED * tpf(), animWalk_W); }
    public void moveDown()  { move(0, SPEED * tpf(), animWalk_S); }

    private void playAnimation(AnimationChannel channel) {
        if (texture.getAnimationChannel() != channel) {
            texture.loopAnimationChannel(channel);
        }
    }

    //Limitação do movimento do personagem
    private void move(double dx, double dy, AnimationChannel animation) {
        timeWithoutMovement = 0;
        boolean moved = false;

        if (dx != 0 && canMoveTo(entity.getX() + dx, entity.getY())) {
            entity.translateX(dx);
            moved = true;
        }
        if (dy != 0 && canMoveTo(entity.getX(), entity.getY() + dy)) {
            entity.translateY(dy);
            moved = true;
        }

        playAnimation(moved ? animation : animIdle);
    }

    @Override
    public void onUpdate(double tpf) {
        timeWithoutMovement += tpf;
        if (timeWithoutMovement >= IDLE_DELAY) {
            playAnimation(animIdle);
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

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
    }

}