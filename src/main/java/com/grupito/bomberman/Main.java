package com.grupito.bomberman;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;

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
        onKey(KeyCode.D,"Andar para Direita", () -> player.translateX(5));
        onKey(KeyCode.A, "Andar para Esquerda", () -> player.translateX(-5));
        onKey(KeyCode.W, "Andar para Cima",   () -> player.translateY(-5));
        onKey(KeyCode.S, "Andar para Baixo", () -> player.translateY(5));
        onKeyDown(KeyCode.SPACE, "Colocar bomba", () -> {
            int tileX = (int) (player.getCenter().getX() / Config.TILE);
            int tileY = (int) (player.getCenter().getY() / Config.TILE);
            spawn("bomb", tileX * Config.TILE, tileY * Config.TILE);
        });
    }

    protected void initPhysics(){
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BOMB, EntityType.PLAYER){
            protected void onCollisionBegin(Entity bomb, Entity player){
                player.removeFromWorld();
            }
        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}