package com.grupito.bomberman;

import java.util.Map;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
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
import com.grupito.bomberman.UI.Menu;
import com.grupito.bomberman.components.Monster;
import com.grupito.bomberman.components.Player;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
public class Main extends GameApplication {
    private Entity player;

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
        settings.setWidth(Config.WINDOW_W);
        settings.setHeight(Config.WINDOW_H);

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

        player = spawn("player", 0, 0);

        var vp = getGameScene().getViewport();
        vp.setX(-Config.MAP_X);
        vp.setY(-Config.HUD_TOP);
        getGameScene().setBackgroundColor(Config.COLOR_BORDER);   // cor da borda (janela toda)

        // TEMP: chão do mapa. Apagar quando o LevelLoader desenhar o Piso2.png
        Rectangle chao = new Rectangle(Config.MAP_W, Config.MAP_H, Config.COLOR_FLOOR);
        chao.setStroke(Config.COLOR_FRAME);
        chao.setStrokeWidth(4);
        entityBuilder().at(0, 0).view(chao).zIndex(-100).buildAndAttach();

        invincible = false;
        ending = false;

        // TEMP: monstros de teste enquanto o LevelLoader não existe.
        // Quando a Leticia terminar, o LevelLoader cria os monstros com
        // spawn("monster", x, y)
        // e estas linhas são apagadas.
        int t = Config.TILE;
        int n = Math.min(2 + geti("level"), 4);
        int[][] cells = { { 13, 11 }, { 13, 1 }, { 1, 11 }, { 7, 5 } };
        for (int i = 0; i < n; i++) {
            spawn("monster", cells[i][0] * t, cells[i][1] * t);
        }
    }

    // 4. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.D, "Direita", () -> player.getComponent(Player.class).moveRight());
        onKey(KeyCode.A, "Esquerda", () -> player.getComponent(Player.class).moveLeft());
        onKey(KeyCode.W, "Cima", () -> player.getComponent(Player.class).moveUp());
        onKey(KeyCode.S, "Baixo", () -> player.getComponent(Player.class).moveDown());
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

        // TEMP: K mata um monstro, para testar pontos e vitória sem precisar da
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
        onCollision(EntityType.PLAYER, EntityType.ENEMY, (p, monster) -> Main.onPlayerHit(p));

        // Explosão atingindo o jogador
        onCollisionBegin(EntityType.EXPLOSION, EntityType.PLAYER, (explosion, p) -> onPlayerHit(p));

        // Explosão atingindo o monstro
        onCollisionBegin(EntityType.EXPLOSION, EntityType.ENEMY, (explosion, monster) -> killMonster(monster));
    }

    // 6. Interface (Julia)
    @Override
    protected void initUI() {
        Menu.initHud();
    }

    // =====================================================================
    // Regras do fluxo
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
        if (geti("lives") <= 0) {
            gameOver();
            return;
        }

        // volta ao ponto inicial e fica invencível por alguns segundos
        hitPlayer.setPosition(playerSpawn);
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
        Menu.showGameOver(geti("score"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}