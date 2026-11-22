package com.grupito.bomberman;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.grupito.bomberman.Config.PowerUpType;
import com.grupito.bomberman.components.Bomb;
import com.grupito.bomberman.components.Monster;
import com.grupito.bomberman.components.Player;

import javafx.geometry.Point2D;
import javafx.util.Duration;


public class BombermanFactory  implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .bbox(new HitBox(new Point2D(12, 13), BoundingShape.box(40, 50)))
                .with(new CollidableComponent(true))
                .with(new Player())
                .build();
    }

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        // Pega o range enviado pelo player, ou usa o padrão do Config se não houver
        int range = data.hasKey("range") ? (int) data.get("range") : Config.BOMB_RANGE;

        javafx.scene.image.Image sheet = FXGL.image("Bomba_sprite.png");
        int frameWidth = (int) (sheet.getWidth() / 2);
        int frameHeight = (int) (sheet.getHeight() / 2);

        com.almasb.fxgl.texture.AnimationChannel animChannel = new com.almasb.fxgl.texture.AnimationChannel(
                sheet, 2, frameWidth, frameHeight, Duration.seconds(0.5), 0, 2
        );

        com.almasb.fxgl.texture.AnimatedTexture animTexture = new com.almasb.fxgl.texture.AnimatedTexture(animChannel);
        animTexture.setFitWidth(Config.TILE);
        animTexture.setFitHeight(Config.TILE);
        animTexture.loop();

        // Guarda também a referência do player dentro da entidade da bomba para decrementar depois
        Entity player = data.get("player");

        return entityBuilder(data)
                .type(EntityType.BOMB)
                .bbox(new HitBox(BoundingShape.box(Config.TILE, Config.TILE)))
                .view(animTexture)
                .with(new CollidableComponent(true), new Bomb(range))
                .with("player", player)
                .build();
    }
    
    @Spawns("explosion")
    public Entity newExplosion(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.EXPLOSION)
                .bbox(new HitBox(new Point2D(2, 2), BoundingShape.box(Config.TILE - 4, Config.TILE - 4)))
                //.view(new Rectangle(Config.TILE, Config.TILE, Color.ORANGE))
                //.viewWithBBox(new Rectangle(Config.TILE-10, Config.TILE-10, Color.ORANGE)) // placeholder
                .with(new CollidableComponent(true))
                .with(new ExpireCleanComponent(Duration.seconds(Config.EXPLOSION_DURATION)))
                .build();
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .viewWithBBox(FXGL.texture("HardWall.png"))
                .collidable()
                .build();
    }

    @Spawns("brick")
    public Entity newBrick(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.BRICK)
                .viewWithBBox(FXGL.texture("SoftWall.png")) // Muro frágil destrutível
                .collidable()
                .zIndex(1)
                .build();
    }

    @Spawns("floor") 
    public Entity newFloor(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.FLOOR)
                .viewWithBBox(FXGL.texture("Piso3.png")) // Chão
                .build();
    }

    @Spawns("powerup")
    public Entity newPowerUp(SpawnData data) {
        PowerUpType type = data.get("type");
        if (type == null) type = PowerUpType.HEART;

        // Escolhe o nome do arquivo de imagem de acordo com o tipo de upgrade
        String textureName = switch (type) {
            case HEART -> "UpgradeHeart.png";       // Nome da imagem do coração
            case BOMB -> "UpgradeBombPower.png";     // Nome da imagem de bomba extra
            case FIRE -> "UpgradeFireUp.png";     // Nome da imagem de fogo/alcance
            default -> "PowerUp.png";         // Fallback caso venha algo genérico
        };

        var texture = FXGL.texture(textureName);
        texture.setFitWidth(Config.TILE);
        texture.setFitHeight(Config.TILE);

        return FXGL.entityBuilder(data)
                .type(EntityType.POWERUP)
                .viewWithBBox(texture)
                .collidable()
                .with("powerUpType", type)
                .build();
    }

    @Spawns("monster")
    public Entity newMonster(SpawnData data) {
        int t = Config.TILE;

        // Hitbox menor que a célula
        HitBox hitBox = new HitBox(new Point2D(12, 12), BoundingShape.box(t - 24, t - 24));

        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .bbox(hitBox)
                .collidable()
                .zIndex(10)
                .with(new Monster(Config.ENEMIES_SPEED))
                .build();
    }
}