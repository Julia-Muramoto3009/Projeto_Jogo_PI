package com.grupito.bomberman;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.spawn;

import com.almasb.fxgl.entity.Entity;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;



import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.grupito.bomberman.components.Bomb;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class BombermanFactory extends GameApplication {

    private Entity player;

    // 1. Configurações da Janela
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Bomberman FXGL - Inicial");
        settings.setVersion("1.0");
    }

    // 2. Inicialização das Entidades do Jogo
    @Override
    protected void initGame() {
        // Criando o bloco inicial que representa o Jogador
        player = entityBuilder()
                .at(100, 100) // Posição inicial (x, y)
                .view(new Rectangle(40, 40, Color.BLUE)) // Representação gráfica
                .buildAndAttach(); // Finaliza e adiciona à cena
    }

    // 3. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> player.translateX(5));
        onKey(KeyCode.A,  () -> player.translateX(-5));
        onKey(KeyCode.W,    () -> player.translateY(-5));
        onKey(KeyCode.S,  () -> player.translateY(5));

        onKeyDown(KeyCode.SPACE, "Colocar bomba", () -> {
        // Tile onde o jogador está (usa o centro dele)
        int tileX = (int) (player.getCenter().getX() / Config.TILE_SIZE);
        int tileY = (int) (player.getCenter().getY() / Config.TILE_SIZE);

        // A bomba é criada alinhada ao grid
        spawn("bomb", tileX * Config.TILE_SIZE, tileY * Config.TILE_SIZE);
    });
    }


    // ===== Raquel: bomba e explosão =====

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        // O alcance pode ser passado no spawn; se não for, usa o padrão
        int range = data.hasKey("range") ? data.get("range") : Config.BOMB_RANGE;

        return entityBuilder(data)
                .type(EntityType.BOMB)
                .viewWithBBox(new Rectangle(Config.TILE_SIZE, Config.TILE_SIZE, Color.DARKRED)) // placeholder
                .with(new Bomb(range))
                .build();
    }

    @Spawns("explosion")
    public Entity newExplosion(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.EXPLOSION)
                .viewWithBBox(new Rectangle(Config.TILE_SIZE, Config.TILE_SIZE, Color.ORANGE)) // placeholder
                .with(new CollidableComponent(true))
                .with(new ExpireCleanComponent(Duration.seconds(Config.EXPLOSION_DURATION)))
                .build();
    }

    

    // Método main para disparar a aplicação JavaFX / FXGL
    public static void main(String[] args) {
        launch(args);
    }
}