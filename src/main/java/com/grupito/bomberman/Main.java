package com.grupito.bomberman;

import java.util.Map;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.geti;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.onCollision;
import static com.almasb.fxgl.dsl.FXGL.onCollisionBegin;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.runOnce;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.grupito.bomberman.Config.PowerUpType;
import com.grupito.bomberman.UI.Menu;
import com.grupito.bomberman.components.Monster;
import com.grupito.bomberman.components.Player;
import com.grupito.bomberman.fases.LevelLoader;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

public class Main extends GameApplication {
    private Entity player;

    // Música de fundo (static: sobrevive ao startNewGame() de cada fase)
    private static Music musica = null;

    // 0. Fluxo do Jogo (Julia)
    // Valores que sobrevivem ao startNewGame() (que recria as variáveis do jogo)
    private static boolean advancing = false;
    private static int level = 1;
    private static int carriedScore = 0;
    private static int carriedLives = Config.PLAYER_LIVES;

    private static boolean invincible = false;
    private static boolean ending = false;
    private static Point2D playerSpawn = new Point2D(0, 0); // mesmo ponto do spawn("player", 0, 0)

    // 1. Configurações da Janela
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(832);
        settings.setTitle("Bomberman");
        settings.setVersion("0.1");

        // Menu principal e menu de pausa
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new Menu();
            }
        });
    }

    // 2. Variáveis do jogo (Julia)
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        if (!advancing) { // jogo novo (menu ou depois de game over)
            level = 1;
            carriedScore = 0;
            carriedLives = Config.PLAYER_LIVES;
        }
        advancing = false;

        vars.put("level", level);
        vars.put("score", carriedScore);
        vars.put("lives", carriedLives);
        vars.put("enemies", 0); // cada Monster soma 1 ao ser criado
    }

    // 3. Inicialização das Entidades do Jogo
    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BombermanFactory());

        player = LevelLoader.load(geti("level"));

        invincible = false;
        ending = false;

        // TEMP (Julia): monstros de teste enquanto o LevelLoader não existe.
        // Quando a Leticia terminar, o LevelLoader cria os monstros com
        // spawn("monster", x, y)
        // e estas linhas são apagadas.
        int t = Config.TILE;
        int n = Math.min(2 + geti("level"), 4);
        int[][] cells = { { 13, 11 }, { 13, 1 }, { 1, 11 }, { 7, 5 } };
        for (int i = 0; i < n; i++) {
            spawn("monster", cells[i][0] * t, cells[i][1] * t);
        }

        iniciarMusica();
    }

    // 4. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.D, "Direita", () -> player.getComponent(Player.class).moveRight());
        onKey(KeyCode.A, "Esquerda", () -> player.getComponent(Player.class).moveLeft());
        onKey(KeyCode.W, "Cima", () -> player.getComponent(Player.class).moveUp());
        onKey(KeyCode.S, "Baixo", () -> player.getComponent(Player.class).moveDown());
        onKeyDown(KeyCode.SPACE, "Colocar bomba", () -> {
            if (player == null || !player.isActive()) return;
            
            Player playerComp = player.getComponent(Player.class);

            // Verifica se o jogador atingiu o limite de bombas ativas
            if (playerComp.getActiveBombs() >= playerComp.getMaxBombs()) {
                return; 
            }

            int tileX = (int) (player.getCenter().getX() / Config.TILE);
            int tileY = (int) (player.getCenter().getY() / Config.TILE);
            double x = tileX * Config.TILE;
            double y = tileY * Config.TILE;

            boolean jaTemBomba = getGameWorld().getEntitiesByType(EntityType.BOMB).stream()
                    .anyMatch(b -> b.getX() == x && b.getY() == y);

            if (!jaTemBomba) {
                playerComp.incrementActiveBombs(); // Soma 1 nas bombas ativas do player
                FXGL.play("place_freesound.wav");
                
                // Spawna a bomba passando corretamente o range e a referência do player
                spawn("bomb", new SpawnData(x, y).put("range", playerComp.getExplosionRange()).put("player", player));
            }
        });

        // TEMP (Julia): K mata um monstro, para testar pontos e vitória sem precisar da
        // bomba
        onKeyDown(KeyCode.K, "Matar monstro (teste)", () -> {
            for (Entity e : getGameWorld().getEntitiesByType(EntityType.ENEMY)) {
                if (!e.getComponent(Monster.class).isDead()) {
                    killMonster(e);
                    break;
                }
            }
        });
    }

    // 5. Colisões (Julia)
    @Override
    protected void initPhysics() {
        // Encostando no monstro: se não estiver invencível, perde vida
        onCollision(EntityType.PLAYER, EntityType.ENEMY, (p, monster) -> {monster.getComponent(Monster.class).playAttackAnimation();onPlayerHit(p);});

        // Explosão atingindo o jogador
        onCollisionBegin(EntityType.EXPLOSION, EntityType.PLAYER, (explosion, p) -> onPlayerHit(p));

        // Explosão atingindo o tijolo (BRICK): Destroi e tem chance de dropar power-up
        onCollisionBegin(EntityType.EXPLOSION, EntityType.BRICK, (explosion, brick) -> {
            
        // Define a probabilidade de drop (Exemplo: 40% de chance de nascer upgrade)
        double chanceDeDrop = 0.4; // 0.4 = 40% | Mude para 0.3 se quiser 30%, etc.
            
            if (FXGL.random() < chanceDeDrop) {
                // Sorteia aleatoriamente qual tipo de power-up vai nascer
                PowerUpType[] types = PowerUpType.values();
                PowerUpType randomType = types[FXGL.random(0, types.length - 1)];
                
                // Spawna o power-up na posição do tijolo
                spawn("powerup", new SpawnData(brick.getPosition().getX(), brick.getPosition().getY())
                        .put("type", randomType));
            }

            // Remove o tijolo do mapa de qualquer forma
            brick.removeFromWorld();
            FXGL.play("brick_break_freesound.wav");
        });

        // Coleta de Power-Up pelo Jogador
        onCollision(EntityType.PLAYER, EntityType.POWERUP, (playerEntity, powerUpEntity) -> {
            Player playerComp = playerEntity.getComponent(Player.class);
            PowerUpType type = powerUpEntity.getObject("powerUpType");

            if (type != null) {
                switch (type) {
                    case HEART -> {
                        playerComp.addLife();
                        // INCREMENTA A VIDA GLOBAL NO FXGL PARA ATUALIZAR O HUD
                        inc("lives", 1);
                        FXGL.getNotificationService().pushNotification("Vida Extra Coletada! ❤️");
                    }
                    case BOMB -> {
                        playerComp.increaseMaxBombs();
                        FXGL.getNotificationService().pushNotification("Bomba Simultânea Extra! 💣");
                    }
                    case FIRE -> {
                        playerComp.increaseExplosionRange();
                        FXGL.getNotificationService().pushNotification("Alcance da Explosão Aumentado! 🔥");
                    }
                }
            }

            FXGL.play("powerup_freesound.wav");
            powerUpEntity.removeFromWorld(); // Remove o power-up do mapa após ser pego
        });
    }

    // 6. Interface (Julia)
    @Override
    protected void initUI() {
        Menu.initHud();
    }

    // =====================================================================
    // Regras do fluxo (Julia)
    // Leticia: ao carregar a fase, chamar Main.setPlayerSpawn(x, y);
    // com porta de saída, chamar Main.levelComplete() ao jogador entrar nela.
    // =====================================================================

    public static void setPlayerSpawn(double x, double y) {
        playerSpawn = new Point2D(x, y);
    }

    public static void onPlayerHit(Entity hitPlayer) {
        if (invincible || ending)
            return;

        inc("lives", -1);
        FXGL.play("hit_chisper.wav");
        if (geti("lives") <= 0) {
            gameOver();
            return;
        }

        // NÃO move mais para o spawn. O jogador continua no mesmo lugar,
        // mas fica invencível e piscando (com opacidade reduzida) por alguns segundos.
        invincible = true;
        hitPlayer.getViewComponent().setOpacity(0.4);

        runOnce(() -> {
            invincible = false;
            hitPlayer.getViewComponent().setOpacity(1.0);
        }, Duration.seconds(Config.INVINCIBLE_SECONDS));
    }

    public static void killMonster(Entity monsterEntity) {
        Monster m = monsterEntity.getComponent(Monster.class);
        if (m.isDead())
            return; // várias chamas podem atingir o mesmo monstro

        m.kill();
        FXGL.play("monster_die.wav");
        inc("score", Config.SCORE_MONSTER);
        inc("enemies", -1);

        if (geti("enemies") <= 0) {
            levelComplete();
        }
    }

    public static void levelComplete() {
        if (ending)
            return;
        ending = true;

        if (geti("level") >= Config.TOTAL_LEVELS) {
            pararMusica();
            FXGL.play("win_freesound.wav");
            Menu.showVictory(geti("score"));
            return;
        }

        int next = geti("level") + 1;
        int score = geti("score");
        int lives = geti("lives");

        Menu.showLevelComplete(geti("level"), () -> {
            level = next;
            carriedScore = score;
            carriedLives = lives;
            advancing = true;
            getGameController().startNewGame();
        });
    }

    public static void gameOver() {
        if (ending)
            return;
        ending = true;
        pararMusica();
        FXGL.play("game_over_alhpix.wav");
        Menu.showGameOver(geti("score"));
    }

    // =====================================================================
    // Áudio (Raquel)
    // =====================================================================

    public static void iniciarMusica() {
        if (musica == null) {
            musica = FXGL.getAssetLoader().loadMusic("musica.mp3");
            FXGL.getAudioPlayer().loopMusic(musica);
        }
    }

    public static void pararMusica() {
        if (musica != null) {
            FXGL.getAudioPlayer().stopMusic(musica);
            musica = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}