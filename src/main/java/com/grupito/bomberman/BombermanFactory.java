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
import com.grupito.bomberman.components.Bomb;
import com.grupito.bomberman.components.Monster;
import com.grupito.bomberman.components.Player;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class BombermanFactory  implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .bbox(new HitBox(BoundingShape.box(48,64))) // hitbox menor que a célula
                //.viewWithBBox(texture("bomberman.png"))
                .with(new CollidableComponent(true))
                .with(new Player())
                .build();
    }

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        int range = data.hasKey("range") ? ((Number) data.get("range")).intValue() : Config.BOMB_RANGE;
        return entityBuilder(data)
                .type(EntityType.BOMB)
                .viewWithBBox(new Rectangle(Config.TILE, Config.TILE, Color.DARKRED)) // placeholder
                .with(new CollidableComponent(true), new Bomb(range))
                .build();
    }
    
    @Spawns("explosion")
    public Entity newExplosion(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.EXPLOSION)
                .viewWithBBox(new Rectangle(Config.TILE, Config.TILE, Color.ORANGE)) // placeholder
                .with(new CollidableComponent(true))
                .with(new ExpireCleanComponent(Duration.seconds(Config.EXPLOSION_DURATION)))
                .build();
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .viewWithBBox(FXGL.texture("HardWall.png")) // Parede indestrutível
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
        return FXGL.entityBuilder(data)
                .type(EntityType.POWERUP)
                .viewWithBBox(FXGL.texture("PowerUp.png"))
                .collidable()
                .build();
    }

    @Spawns("monster")
    public Entity newMonster(SpawnData data) {
        int t = Config.TILE;
        // Hitbox menor que a célula: o jogador só morre ao encostar de verdade
        HitBox hitBox = new HitBox(new Point2D(12, 12), BoundingShape.box(t - 24, t - 24));

        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .view(new Circle(t / 2.0, t / 2.0, t / 2.0 - 8, Color.CRIMSON))
                .bbox(hitBox)
                .collidable()
                .zIndex(10)
                .with(new Monster(Config.ENEMIES_SPEED))
                .build();
    }
}