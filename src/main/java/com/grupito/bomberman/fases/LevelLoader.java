package com.grupito.bomberman.fases;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.Main;

/** 
 * Lê o mapa da fase (assents/levels/MapaN.tmj) e cria as entidades:
 * chão, blocos fixos, blocos destruíveis e o jogador.
 * chamada pela Main.initGame(): {@code player = LevelLoader.load(geti("level"));}
 */
public class LevelLoader {
    private static final String FOLDER = "/assets/levels/";
    private LevelLoader() {   // Construtor privado para evitar instanciação
    }

    /** Carregar a fase e devolver o jogador já posicionado no ponto inicial. */
    public static Entity load(int level) {
        MapData map = readMap(level);
        map.warnings().forEach(w -> System.out.println("LevelLoader: " + w));

        drawFloor(map);
        int t = Config.TILE;
        for (int[] c: map.walls()) {
            spawn("wall", c[0] * t, c[1] * t);
        }
        for (int[] c: map.bricks()) {
            spawn("brick", c[0] * t, c[1] * t);
        }
        double x = Config.PLAYER_START_COL * t;
        double y = Config.PLAYER_START_ROW * t;
        Main.setPlayerSpawn(x, y);
        return spawn("player", x, y);
    }

    private static MapData readMap(int level) {
        String file = "Mapa" + level + ".tmj";
        InputStream in = LevelLoader.class.getResourceAsStream(FOLDER + file);
        if (in == null) {
            // Enquanto só existe o Mapa1, as outras fases repraproveitam o mesmo mapa
            System.err.println("[LevelLoader]" + file + " não encontrado, usando Mapa1.tmj");
            in = LevelLoader.class.getResourceAsStream(FOLDER + "Mapa1.tmj");
        }
        if (in == null) {
            throw new IllegalStateException("Mapa não encontrado em scr/main/resources" + FOLDER);
        }
        try {
            return new MapData(new String(in.readAllBytes(), StandardCharsets.UTF_8), Config.TILE);
        }
        catch (IOException e) {
            throw new IllegalStateException("Erro ao ler o mapa: " + file, e);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ignored) {
            }
        }
    }
    
    /** TEMP: chão. Trocar pela textura Piso3.png quando os srites entrarem. */
    private static void drawFloor(MapData map) {
        int t = Config.TILE;
        for (int row = 0; row < map.rows(); row++) {
            for (int col = 0; col < map.cols(); col++) {
                entityBuilder()
                    .at(col * t, row * t)
                    .view(FXGL.texture("Piso3.png")) // Chão
                    .zIndex(-100)
                    .buildAndAttach();
                }
            }
    }
}
