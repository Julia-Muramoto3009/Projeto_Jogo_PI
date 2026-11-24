package com.grupito.bomberman.fases;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.setLevelFromMap;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.almasb.fxgl.dsl.FXGL.texture;
import com.almasb.fxgl.entity.Entity;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.Main;

/**
 * Carrega a fase a partir de assets/levels/MapaN.tmx.
 * O FXGL lê o .tmx e, para cada objeto do Tiled, chama o @Spawns da
 * BombermanFactory com o mesmo nome do "type" do objeto (wall, brick, monster).
 * Chamada pela Main.initGame(): player = LevelLoader.load(geti("level"));
 */
public class LevelLoader {

    private LevelLoader() {
    }

    /** Carrega a fase e devolve o jogador já posicionado no ponto inicial. */
    public static Entity load(int level) {
        setLevelFromMap("Mapa" + level + ".tmx");

        // O chão vem depois: setLevelFromMap limpa o mundo antes de criar as entidades
        drawFloor();

        double x = Config.PLAYER_START_COL * Config.TILE;
        double y = Config.PLAYER_START_ROW * Config.TILE;
        Main.setPlayerSpawn(x, y);
        return spawn("player", x, y);
    }

    private static void drawFloor() {
        for (int row = 0; row < Config.ROWS; row++) {
            for (int col = 0; col < Config.COLS; col++) {
                entityBuilder()
                        .at(col * Config.TILE, row * Config.TILE)
                        .view(texture("Piso3.png"))
                        .zIndex(-100)
                        .buildAndAttach();
            }
        }
    }
}