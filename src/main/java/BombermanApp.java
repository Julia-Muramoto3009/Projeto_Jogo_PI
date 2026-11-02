import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Importação estática recomendada para facilidades da DSL do FXGL
import static com.almasb.fxgl.dsl.FXGL.*;

public class BombermanApp extends GameApplication {

    private Entity player;

    // 1. Configurações da Janela
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Bomberman FXGL - Inicial");
        settings.setVersion("1.0");
    }

    // 2. Inicialização das Entidades do Jogo
    @Override
    protected void initGame() {
        // Criando o bloco inicial que representa o Jogador
        player = entityBuilder()
                .at(100, 100) // Posição inicial (x, y)
                .view(new Rectangle(40, 40, Color.BLUE)) // Representação gráfica
                .buildAndAttach(); // Finaliza e adiciona à cena
    }

    // 3. Mapeamento de Entradas de Teclado (Controles)
    @Override
    protected void initInput() {
        onKey(KeyCode.RIGHT, () -> player.translateX(5));
        onKey(KeyCode.LEFT,  () -> player.translateX(-5));
        onKey(KeyCode.UP,    () -> player.translateY(-5));
        onKey(KeyCode.DOWN,  () -> player.translateY(5));
    }

    // Método main para disparar a aplicação JavaFX / FXGL
    public static void main(String[] args) {
        launch(args);
    }
}