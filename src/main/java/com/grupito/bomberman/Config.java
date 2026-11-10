package com.grupito.bomberman;

public class Config {
    // Bombas
    public static final double BOMB_FUSE_SECONDS = 2.0;      // tempo até a bomba explodir
    public static final double EXPLOSION_DURATION = 0.5;     // tempo que a chama fica na tela
    public static final int BOMB_RANGE = 2;                  // alcance inicial (em tiles)

    // Mundo 
    public static final int TILE = 64;
    public static final int COLS = 15;
    public static final int ROWS = 13;
    
    // Inimigos
    public static final double ENEMIES_SPEED = 60;
    public static final int  SCORE_MONSTER = 100;
    
    // Fluxo
    public static final int TOTAL_LEVELS = 3;

    // Player
    public static final int PLAYER_LIVES = 3;
    public static final double INVINCIBLE_SECONDS = 2;
}
