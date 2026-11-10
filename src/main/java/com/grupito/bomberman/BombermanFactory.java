package com.grupito.bomberman;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.grupito.bomberman.components.Bomb;
import com.grupito.bomberman.components.Player;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class BombermanFactory  implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(texture("bomberman.png"))
                .with(new Player())
                .build();
    }

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        // O alcance pode ser passado no spawn; se não for, usa o padrão
        int range = data.hasKey("range") ? data.get("range") : Config.BOMB_RANGE;

        return entityBuilder(data)
                .type(EntityType.BOMB)
                .viewWithBBox(new Rectangle(Config.TILE, Config.TILE, Color.DARKRED)) // placeholder
                .with(new Bomb(range))
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

}