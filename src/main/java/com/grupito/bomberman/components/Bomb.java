package com.grupito.bomberman.components;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.texture.Texture;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;
import com.grupito.bomberman.Main;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * Comportamento da bomba: pulsa enquanto o pavio queima, depois explode em cruz.
 *
 * A entidade da bomba é criada na BombermanFactory (@Spawns("bomb")).
 * A chama é outra entidade (@Spawns("explosion")) que some sozinha depois de um tempo.
 */
public class Bomb extends Component {

    private static final String EXPLOSION_SPRITE = "Explosao_Sprite.png";

    // Direções: Cima, Baixo, Esquerda, Direita
    private static final int[][] DIRECTIONS = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0}
    };

    // Índices da spritesheet 3x3 (0 a 6)
    private static final int SPRITE_TIP_LEFT  = 0;
    private static final int SPRITE_TIP_UP    = 1;
    private static final int SPRITE_CENTER    = 2;
    private static final int SPRITE_TIP_DOWN  = 3;
    private static final int SPRITE_TIP_RIGHT = 4;
    private static final int SPRITE_MID_VERT  = 5;
    private static final int SPRITE_MID_HORIZ = 6;

    private final int range;
    private double timeLeft = Config.BOMB_FUSE_SECONDS;
    private boolean exploded = false;

    private final AnimatedTexture texture;
    private final AnimationChannel pulse;

    public Bomb(int range) {
        this.range = Math.max(1, range);
    }

    @Override
    public void onAdded() {
        snapEntityToGrid();
    }

    @Override
    public void onUpdate(double tpf) {
        timeLeft -= tpf;

        if (timeLeft <= 0 && !exploded) {
            explode();
        }
    }

private void snapEntityToGrid() {
        int col = getCurrentCol();
        int row = getCurrentRow();

        if (isInsideGrid(col, row)) {
            // Posiciona diretamente na grade sem margens extras
            entity.setPosition(col * Config.TILE, row * Config.TILE);
        }
    }

    private int getCurrentCol() {
        double w = entity.getWidth() > 0 ? entity.getWidth() : Config.TILE;
        double centerX = entity.getX() + w / 2.0;
        // Removeu o - Config.MAP_X para alinhar com o sistema de coordenadas atual
        return (int) Math.floor(centerX / Config.TILE);
    }

    private int getCurrentRow() {
        double h = entity.getHeight() > 0 ? entity.getHeight() : Config.TILE;
        double centerY = entity.getY() + h / 2.0;
        // Removeu o - Config.HUD_TOP
        return (int) Math.floor(centerY / Config.TILE);
    }

    private void explode() {
        exploded = true;

        // Decrementa a quantidade de bombas ativas do jogador que a soltou (se ele ainda existir)
        Entity playerEntity = entity.getObject("player");
        if (playerEntity != null && playerEntity.isActive()) {
            playerEntity.getComponent(Player.class).decrementActiveBombs();
        }

        int startCol = getCurrentCol();
        int startRow = getCurrentRow();

        entity.removeFromWorld();

        if (!isInsideGrid(startCol, startRow)) {
            return;
        }

        // Centro da cruz no tile exato da bomba
        spawnFlame(startCol, startRow, SPRITE_CENTER);
        checkAndKillMonsterAt(startCol, startRow); // <-- Mata o monstro no centro se houver

        // 4 braços da cruz: só param fora da grade ou em bloco fixo (WALL)
        for (int dirIndex = 0; dirIndex < DIRECTIONS.length; dirIndex++) {
            int[] dir = DIRECTIONS[dirIndex];

            for (int step = 1; step <= range; step++) {
                int targetCol = startCol + dir[0] * step;
                int targetRow = startRow + dir[1] * step;

                if (!isInsideGrid(targetCol, targetRow)) {
                    break;
                }

                // Única exceção: bloco fixo (WALL) bloqueia a chama
                if (hasEntityOfType(targetCol, targetRow, EntityType.WALL)) {
                    break;
                }

                int nextCol = targetCol + dir[0];
                int nextRow = targetRow + dir[1];
                boolean isTip = (step == range)
                        || !isInsideGrid(nextCol, nextRow)
                        || hasEntityOfType(nextCol, nextRow, EntityType.WALL);

                int spriteIndex = getFlameSpriteIndex(dirIndex, isTip);
                spawnFlame(targetCol, targetRow, spriteIndex);
                
                // Mata o monstro instantaneamente neste tile da explosão
                checkAndKillMonsterAt(targetCol, targetRow);

                // Tijolo destrutível é atingido pela chama, mas bloqueia a continuidade além dele
                if (hasEntityOfType(targetCol, targetRow, EntityType.BRICK)) {
                    break;
                }
            }
        }
    }

<<<<<<< HEAD
    private void spawnFlame(int tileX, int tileY, String part) {
        spawn("explosion", new SpawnData(tileX * Config.TILE, tileY * Config.TILE).put("part", part));
    }
    
    /**
     * Verifica se existe uma entidade do tipo informado dentro do tile.
     * Obs.: se a Leticia disponibilizar um grid no LevelLoader, esta consulta
     * pode ser trocada por uma leitura da matriz.
     */
    private boolean hasEntityOfType(int tileX, int tileY, EntityType type) {
=======
private void checkAndKillMonsterAt(int col, int row) {
>>>>>>> main
        double size = Config.TILE;

        Rectangle2D area = new Rectangle2D(
                col * size + 2,
                row * size + 2,
                size - 4,
                size - 4
        );

        getGameWorld().getEntitiesInRange(area)
                .stream()
                .filter(e -> e.isType(EntityType.ENEMY) && e.hasComponent(Monster.class))
                .forEach(monsterEntity -> {
                    Monster m = monsterEntity.getComponent(Monster.class);
                    if (!m.isDead()) {
                        Main.killMonster(monsterEntity);
                    }
                });
    }

    private boolean isInsideGrid(int col, int row) {
        return col >= 0 && col < Config.COLS && row >= 0 && row < Config.ROWS;
    }

    private int getFlameSpriteIndex(int dirIndex, boolean isTip) {
        return switch (dirIndex) {
            case 0 -> isTip ? SPRITE_TIP_UP    : SPRITE_MID_VERT;  // Cima
            case 1 -> isTip ? SPRITE_TIP_DOWN  : SPRITE_MID_VERT;  // Baixo
            case 2 -> isTip ? SPRITE_TIP_LEFT  : SPRITE_MID_HORIZ; // Esquerda
            case 3 -> isTip ? SPRITE_TIP_RIGHT : SPRITE_MID_HORIZ; // Direita
            default -> SPRITE_CENTER;
        };
    }

private void spawnFlame(int col, int row, int spriteIndex) {
        double worldX = col * Config.TILE;
        double worldY = row * Config.TILE;

        Entity flame = spawn("explosion", worldX, worldY);
        Texture piece = getExplosionPiece(spriteIndex);
        piece.setTranslateX(0);
        piece.setTranslateY(0);
        flame.getViewComponent().addChild(piece);
    }

    private Texture getExplosionPiece(int index) {
        Image sheet = FXGL.image(EXPLOSION_SPRITE);
        double tileW = sheet.getWidth() / 3.0;
        double tileH = sheet.getHeight() / 3.0;

        int col = index % 3;
        int row = index / 3;

        // Apara levemente as bordas de cada célula para aproximar os segmentos da cruz
        double padX = tileW * 0.04;
        double padY = tileH * 0.04;

        double cropX = Math.max(0, col * tileW + padX);
        double cropY = Math.max(0, row * tileH + padY);
        double cropW = Math.max(1, tileW - padX * 2);
        double cropH = Math.max(1, tileH - padY * 2);

        Rectangle2D viewport = new Rectangle2D(cropX, cropY, cropW, cropH);
        Texture tex = new Texture(sheet).subTexture(viewport);
        tex.setFitWidth(Config.TILE);
        tex.setFitHeight(Config.TILE);

        return tex;
    }

private boolean hasEntityOfType(int col, int row, EntityType type) {
        double size = Config.TILE;
        double tileCenterX = col * size + size / 2.0;
        double tileCenterY = row * size + size / 2.0;

        Rectangle2D area = new Rectangle2D(
                col * size + 4,
                row * size + 4,
                size - 8,
                size - 8
        );

        return getGameWorld().getEntitiesInRange(area)
                .stream()
                .anyMatch(e -> {
                    if (!e.isType(type) || !e.hasComponent(BoundingBoxComponent.class)) {
                        return false;
                    }
                    BoundingBoxComponent bbox = e.getBoundingBoxComponent();
                    return bbox.getMinXWorld() <= tileCenterX
                            && bbox.getMaxXWorld() >= tileCenterX
                            && bbox.getMinYWorld() <= tileCenterY
                            && bbox.getMaxYWorld() >= tileCenterY;
                });
    }
}