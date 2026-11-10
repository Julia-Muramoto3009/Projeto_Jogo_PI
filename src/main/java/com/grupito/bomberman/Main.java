package com.grupito.bomberman;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class Main extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(832);
        settings.setTitle("Bomberman");
        settings.setVersion("0.1");
    }

    public static void main(String[] args) {
        launch(args);
    }
}