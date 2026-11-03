package com.grupito.bomberman;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.texture.AnimatedTexture;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import com.almasb.fxgl.entity.Entity;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BombermanFactory extends GameApplication {

    private Entity player;
    private Entity bloco;

    // 1. Configurações da Janela
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(832);
        settings.setHeight(960);
        settings.setTitle("Bomberman");
        settings.setVersion("0.1.0");
    }

    // 2. Inicialização das Entidades do Jogo
    @Override
    protected void initGame() {
        // Criando o bloco inicial que representa o Jogador
        player = entityBuilder()
                .at(100, 100) // Posição inicial (x, y)
                .view("icon.png") // Representação gráfica
                .buildAndAttach(); // Finaliza e adiciona à cena

        bloco = entityBuilder()
                .at(0, 0) // Posição inicial (x, y)
                .view("HardWall64.png") // Representação gráfica
                .buildAndAttach(); // Finaliza e adiciona à cena
    }

    // 3. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> player.translateX(5));
        onKey(KeyCode.A,  () -> player.translateX(-5));
        onKey(KeyCode.W,    () -> player.translateY(-5));
        onKey(KeyCode.S,  () -> player.translateY(5));
    }

    

    // Método main para disparar a aplicação JavaFX / FXGL
    public static void main(String[] args) {
        launch(args);
    }
}