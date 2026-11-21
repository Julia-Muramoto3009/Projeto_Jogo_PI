package com.grupito.bomberman.components;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * Comportamento da bomba: pulsa enquanto o pavio queima, depois explode em cruz.
 *
 * A entidade da bomba é criada na BombermanFactory (@Spawns("bomb")).
 * A chama é outra entidade (@Spawns("explosion")) que some sozinha depois de um tempo.
 */
public class Bomb extends Component {

    // Direções da cruz: cima, baixo, esquerda, direita (deslocamento em tiles)
    private static final int[][] DIRECTIONS = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0}
    };

    private static final String[] TIPS    = {"up", "down", "left", "right"};
    private static final String[] MIDDLES = {"v",  "v",    "h",    "h"};


    private static final double PULSE_SECONDS = 0.6;   // duração de um ciclo da animação

    private final int range;                                 // alcance em tiles
    private double timeLeft = Config.BOMB_FUSE_SECONDS;      // tempo restante do pavio
    private boolean exploded = false;

    private final AnimatedTexture texture;
    private final AnimationChannel pulse;

    public Bomb(int range) {
        this.range = range;

        pulse = new AnimationChannel(FXGL.image("Bomba_sprite.png"), 2, 64, 64,
                Duration.seconds(PULSE_SECONDS), 0, 2);
        texture = new AnimatedTexture(pulse);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(pulse);
    }

    /** Roda a cada quadro. tpf = tempo (em segundos) desde o quadro anterior. */
    @Override
    public void onUpdate(double tpf) {
        timeLeft -= tpf;

        if (timeLeft <= 0 && !exploded) {
            explode();
        }
    }

    private void explode() {
        exploded = true;

        // Posição da bomba convertida de pixels para tile
        int tileX = (int) (entity.getX() / Config.TILE);
        int tileY = (int) (entity.getY() / Config.TILE);

        // A bomba some
        entity.removeFromWorld();

        // Chama central
        spawnFlame(tileX, tileY, "center");

        // Propagação em cruz
        for (int d = 0; d < DIRECTIONS.length; d++) {
            int[] dir = DIRECTIONS[d];

            int length = 0;
            for (int i = 1; i <= range; i++) {
                int tx = tileX + dir[0] * i;
                int ty = tileY + dir[1] * i;

                if (hasEntityOfType(tx, ty, EntityType.WALL)) break;   // parede: não entra
                length = i;
                if (hasEntityOfType(tx, ty, EntityType.BRICK)) break;  // tijolo: entra, mas não passa
            }

            for (int i = 1; i <= length; i++) {
                spawnFlame(tileX + dir[0] * i, tileY + dir[1] * i, i == length ? TIPS[d] : MIDDLES[d]);
            }
        }

        // TODO (extras, depois que o básico funcionar):
        //  - detonação em cadeia: chama que atinge outra bomba a faz explodir
        //  - devolver a bomba ao jogador (limite de bombas simultâneas)
        //  - som e animação da explosão
    }

    private void spawnFlame(int tileX, int tileY, String part) {
        spawn("explosion", new SpawnData(tileX * Config.TILE, tileY * Config.TILE).put("part", part));
    }
    
    /**
     * Verifica se existe uma entidade do tipo informado dentro do tile.
     * Obs.: se a Leticia disponibilizar um grid no LevelLoader, esta consulta
     * pode ser trocada por uma leitura da matriz.
     */
    private boolean hasEntityOfType(int tileX, int tileY, EntityType type) {
        double size = Config.TILE;

        // Margem de 2px para não capturar entidades dos tiles vizinhos
        Rectangle2D area = new Rectangle2D(
                tileX * size + 2, tileY * size + 2, size - 4, size - 4);

        return getGameWorld().getEntitiesInRange(area)
                .stream()
                .anyMatch(e -> e.isType(type));
    }
}
