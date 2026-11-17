package com.grupito.bomberman.components;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class AnimationComponent extends Component {

    private int speed = 0;

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;

    public AnimationComponent() {
        animIdle = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 2, 2);
        animWalk_W = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 3, 4);
        animWalk_A = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 5, 7);
        animWalk_S = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 2, 4);
        animWalk_D = new AnimationChannel(FXGL.image("Bomberman_sprite.png"), 4, 64, 64, Duration.seconds(1), 8, 10);

        texture = new AnimatedTexture(animIdle);
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translateX(speed * tpf);

        if (speed != 0) {

            if (texture.getAnimationChannel() == animIdle) {
                texture.loopAnimationChannel(animWalk);
            }

            speed = (int) (speed * 0.9);

            if (FXGLMath.abs(speed) < 1) {
                speed = 0;
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    public void moveRight() {
        speed = 150;

        getEntity().setScaleX(1);
    }

    public void moveLeft() {
        speed = -150;

        getEntity().setScaleX(-1);
    }
}