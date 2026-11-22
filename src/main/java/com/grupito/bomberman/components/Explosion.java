package com.grupito.bomberman.components;

import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.grupito.bomberman.Config;
import com.grupito.bomberman.EntityType;

import javafx.scene.image.Image;
import javafx.util.Duration;

public class Explosion extends Component {

    private static final String SPRITE_NAME = "Explosao_Sprite.png"; // Renomeie o arquivo sem acento em assets/textures
    private static final int SPRITE_FRAMES = 4; // Ajuste para a quantidade de quadros da spritesheet

    private final int range;
    private final List<Entity> spawnedTiles = new ArrayList<>();

    public Explosion() {
        this(Config.BOMB_RANGE);
    }

    public Explosion(int range) {
        this.range = Math.max(1, range);
    }

    @Override
    public void onAdded() {
        spawnCrossTiles();
    }

private void spawnCrossTiles() {
        // Normaliza a posição inicial para o início exato do tile (em múltiplos de Config.TILE)
        double startX = Math.round((entity.getX() - Config.MAP_X) / Config.TILE) * Config.TILE + Config.MAP_X;
        double startY = Math.round((entity.getY() - Config.HUD_TOP) / Config.TILE) * Config.TILE + Config.HUD_TOP;

        // Centro da explosão
        createExplosionTile(startX, startY);

        // 4 direções: direita, esquerda, baixo, cima
        int[][] directions = {
                { 1,  0},
                {-1,  0},
                { 0,  1},
                { 0, -1}
        };

        for (int[] dir : directions) {
            for (int step = 1; step <= range; step++) {
                double targetX = startX + dir[0] * step * Config.TILE;
                double targetY = startY + dir[1] * step * Config.TILE;

                // Impede sair dos limites do mapa
                if (targetX < Config.MAP_X
                        || targetX >= Config.MAP_X + Config.MAP_W
                        || targetY < Config.HUD_TOP
                        || targetY >= Config.HUD_TOP + Config.MAP_H) {
                    break;
                }

                // Parede fixa bloqueia a chama e não é destruída
                if (hasEntityAt(targetX, targetY, EntityType.WALL)) {
                    break;
                }

                createExplosionTile(targetX, targetY);

                // Tijolo destrutível é atingido pela chama, mas bloqueia a continuidade além dele
                if (hasEntityAt(targetX, targetY, EntityType.BRICK)) {
                    break;
                }
            }
        }
    }

    private void createExplosionTile(double x, double y) {
        Entity tile = FXGL.spawn("explosion", new SpawnData(x, y));

        AnimatedTexture animTexture = createExplosionAnimation();
        tile.getViewComponent().addChild(animTexture);

        spawnedTiles.add(tile);
    }

    private AnimatedTexture createExplosionAnimation() {
        Image sheet = FXGL.image(SPRITE_NAME);

        int frames = Math.max(1, SPRITE_FRAMES);
        int frameWidth = Math.max(1, (int) (sheet.getWidth() / frames));
        int frameHeight = Math.max(1, (int) sheet.getHeight());

        AnimationChannel channel = new AnimationChannel(
                sheet,
                frames,
                frameWidth,
                frameHeight,
                Duration.seconds(Config.EXPLOSION_DURATION),
                0,
                frames - 1
        );

        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.setFitWidth(Config.TILE);
        texture.setFitHeight(Config.TILE);
        texture.play();
        return texture;
    }

    private boolean hasEntityAt(double x, double y, EntityType type) {
        double centerX = x + Config.TILE / 2.0;
        double centerY = y + Config.TILE / 2.0;

        return FXGL.getGameWorld().getEntitiesByType(type).stream()
                .filter(e -> e.hasComponent(BoundingBoxComponent.class))
                .anyMatch(e -> {
                    BoundingBoxComponent bbox = e.getBoundingBoxComponent();
                    return centerX >= bbox.getMinXWorld()
                            && centerX <= bbox.getMaxXWorld()
                            && centerY >= bbox.getMinYWorld()
                            && centerY <= bbox.getMaxYWorld();
                });
    }

    @Override
    public void onRemoved() {
        spawnedTiles.clear();
    }
}