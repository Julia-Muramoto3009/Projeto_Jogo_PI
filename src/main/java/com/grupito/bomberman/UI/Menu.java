package com.grupito.bomberman.UI;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getip;
import com.grupito.bomberman.Config;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Menu e telas: menu principal, HUD, game over e vitória.
 * Os métodos estáticos são chamados pela Main.
 */
public class Menu extends FXGLMenu {

    // ================= Menu principal =================

    public Menu() {
        super(MenuType.MAIN_MENU);

        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.rgb(20, 20, 45));

        Text title = new Text("BOMBERMAN");
        title.setFill(Color.ORANGE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 64));

        Button play = new Button("JOGAR");
        play.setPrefWidth(200);
        play.setOnAction(e -> fireNewGame());

        Button exit = new Button("SAIR");
        exit.setPrefWidth(200);
        exit.setOnAction(e -> fireExit());

        VBox box = new VBox(24, title, play, exit);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(getAppWidth());
        box.setTranslateY(200);

        getContentRoot().getChildren().addAll(bg, box);
    }

    // ================= HUD =================

    /** Textos ligados (bind) às variáveis do jogo: atualizam sozinhos. */
        public static void initHud() {
        int x0 = Config.MAP_X;                       // alinha com a borda do mapa
        int baixoY = Config.HUD_TOP + Config.MAP_H;  // onde começa a faixa de baixo

        // ----- faixa de cima: pontos, vidas, fase, inimigos -----
        Text score = label();
        score.textProperty().bind(getip("score").asString("PONTOS: %d"));
        Text lives = label();
        lives.textProperty().bind(getip("lives").asString("VIDAS: %d"));
        Text level = label();
        level.textProperty().bind(getip("level").asString("FASE: %d"));
        Text enemies = label();
        enemies.textProperty().bind(getip("enemies").asString("INIMIGOS: %d"));

        HBox topo = new HBox(48, score, lives, level, enemies);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.setPrefHeight(Config.HUD_TOP);
        addUINode(topo, x0, 0);

        // ----- faixa de baixo: upgrades obtidos -----
        Text titulo = label();
        titulo.setText("UPGRADES:");
    }

    private static Text label() {
        Text t = new Text();
        t.setFill(Color.WHITE);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        return t;
    }

    // ================= Game over, vitória e fim de fase =================

    public static void showGameOver(int score) {
        getDialogService().showMessageBox(
                "GAME OVER\nPontuação: " + score,
                () -> getGameController().gotoMainMenu());
    }

    public static void showVictory(int score) {
        getDialogService().showMessageBox(
                "VOCÊ VENCEU!\nPontuação final: " + score,
                () -> getGameController().gotoMainMenu());
    }

    /**
     * onContinue roda quando o jogador clica OK (a Main usa para abrir a próxima
     * fase).
     */
    public static void showLevelComplete(int level, Runnable onContinue) {
        getDialogService().showMessageBox("Fase " + level + " concluída!", onContinue);
    }
}