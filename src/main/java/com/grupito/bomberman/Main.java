package com.grupito.bomberman;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.grupito.bomberman.components.Player;

import javafx.scene.input.KeyCode;

public class Main extends GameApplication {
    private Entity player;

    // 1. Configurações da Janela
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(832);
        settings.setTitle("Bomberman");
        settings.setVersion("0.1");
    }

    // 2. Inicialização das Entidades do Jogo
    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BombermanFactory());
        
        player = spawn("player",0,0);
    }

    // 3. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.D, "Direita",  () -> player.getComponent(Player.class).moveRight());
        onKey(KeyCode.A, "Esquerda", () -> player.getComponent(Player.class).moveLeft());
        onKey(KeyCode.W, "Cima",     () -> player.getComponent(Player.class).moveUp());
        onKey(KeyCode.S, "Baixo",    () -> player.getComponent(Player.class).moveDown());
        onKeyDown(KeyCode.SPACE, "Colocar bomba", () -> {
            int tileX = (int) (player.getCenter().getX() / Config.TILE);
            int tileY = (int) (player.getCenter().getY() / Config.TILE);
            double x = tileX * Config.TILE;
            double y = tileY * Config.TILE;

            boolean jaTemBomba = getGameWorld().getEntitiesByType(EntityType.BOMB).stream()
                    .anyMatch(b -> b.getX() == x && b.getY() == y);

            if (!jaTemBomba) {
                spawn("bomb", x, y);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}