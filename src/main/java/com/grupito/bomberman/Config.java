package com.grupito.bomberman;

import javafx.scene.paint.Color;

public class Config {
    // Bombas
    public static final double BOMB_FUSE_SECONDS = 2.5;      // tempo até a bomba explodir
    public static final double EXPLOSION_DURATION = 0.5;     // tempo que a chama fica na tela
    public static final int BOMB_RANGE = 2;                  // alcance inicial (em tiles)

    // Mundo 
    public static final int TILE = 64;
    public static final int COLS = 15;
    public static final int ROWS = 13;
    
    // ===== janela e HUD =====
    public static final int WINDOW_W = 1040;
    public static final int WINDOW_H = 960;

    public static final int MAP_W = COLS * TILE;                    // 960
    public static final int MAP_H = ROWS * TILE;                    // 832

    public static final int MAP_X = (WINDOW_W - MAP_W) / 2;         // 40: margem de cada lado
    public static final int HUD_TOP = 64;                           // faixa de cima
    public static final int HUD_BOTTOM = WINDOW_H - HUD_TOP - MAP_H; // 64: faixa de baixo
    
       // ===== Cores do HUD =====
    public static final Color COLOR_BORDER = Color.rgb(20, 20, 45);   // margens e faixas do HUD
    public static final Color COLOR_FLOOR  = Color.rgb(70, 130, 70);  // chão do mapa
    public static final Color COLOR_FRAME  = Color.ORANGE;            // linha ao redor do mapa

    // Inimigos
    public static final double ENEMIES_SPEED = 60;
    public static final int  SCORE_MONSTER = 100;
    
    // Fluxo
    public static final int TOTAL_LEVELS = 3;

    // Player
    public static final int PLAYER_LIVES = 3;
    public static final double INVINCIBLE_SECONDS = 2;
    public static final int PLAYER_START_COL = 1;
    public static final int PLAYER_START_ROW = 1;
}
