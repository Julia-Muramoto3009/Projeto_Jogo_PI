# Bomberman PI

Jogo estilo Bomberman em **Java** com o framework **FXGL**, desenvolvido para a disciplina de
Desenvolvimento de Sistemas Orientados a Objetos (PUC-Campinas).

O jogador anda por um labirinto, coloca bombas para abrir caminho, derrota todos os monstros e
avança pelas 3 fases sem perder todas as vidas.

## Como rodar

Requisitos:

| Item | Versão |
|---|---|
| Java (JDK) | **25** |
| Maven | 3.9 ou superior |

O FXGL (21.1) e o JavaFX são baixados pelo Maven na primeira execução (precisa de internet).

```bash
java -version          # deve mostrar 25
mvn -version

cd Projeto_Jogo_PI     # pasta onde está o pom.xml
mvn clean javafx:run
```

Quando a janela do jogo abrir, clique em **JOGAR**.

## Controles

| Tecla | Ação |
|---|---|
| `W` `A` `S` `D` | Mover o jogador |
| `Espaço` | Colocar bomba |
| `Esc` | Pausar |
| `K` | Só para teste: mata um monstro |

## Regras do jogo

- O jogador começa com **3 vidas**. O jogo tem **3 fases**.
- A bomba explode depois de **2,5 segundos**, em cruz. O alcance inicial é de **1 tile**.
- A chama **não atravessa paredes fixas** e **para ao atingir um tijolo**, que é destruído.
- Tocar em um monstro ou ser atingido por uma explosão **tira uma vida**. Depois do dano o jogador
  fica **invencível por 2 segundos** (aparece transparente).
- Cada monstro derrotado vale **100 pontos**. A fase termina quando todos os monstros morrem.
- Ao destruir um tijolo há **40% de chance** de aparecer um upgrade:

  | Upgrade | Efeito |
  |---|---|
  | Coração | +1 vida |
  | Fogo | +1 de alcance da explosão |
  | Bomba | +1 bomba simultânea |

- Sem vidas aparece o **Game Over**. Ao vencer a fase 3 aparece a tela de **Vitória**. Nos dois
  casos o jogo volta ao menu principal.

Os valores (tempos, vidas, chance de upgrade etc.) ficam em `Config.java`.

## Estrutura do projeto

```
.
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/grupito/bomberman/
    │   ├── Main.java               Janela, controles, colisões, fluxo do jogo e música
    │   ├── BombermanFactory.java   Cria cada entidade (@Spawns)
    │   ├── EntityType.java         Tipos de entidade (usados nas colisões e buscas)
    │   ├── Config.java             Constantes do jogo
    │   ├── components/
    │   │   ├── Player.java         Movimento, animação e upgrades do jogador
    │   │   ├── Bomb.java           Pavio e explosão em cruz
    │   │   └── Monster.java        Movimento pela grade e animações do inimigo
    │   ├── fases/
    │   │   └── LevelLoader.java    Carrega o mapa da fase e o chão
    │   └── UI/
    │       └── Menu.java           Menu principal, HUD, game over e vitória
    └── resources/assets/
        ├── levels/                 Mapa1.tmx, Mapa2.tmx, Mapa3.tmx (editados no Tiled)
        ├── textures/               Sprites e imagens
        ├── sounds/                 Efeitos sonoros (.wav)
        └── music/                  Música de fundo (.mp3)
```

O FXGL procura os recursos em `assets/<tipo>/`. O nome usado no código deve ser **idêntico** ao do
arquivo (por exemplo `texture("Bomberman_sprite.png")`).

## Como o código funciona

O FXGL chama os métodos da `Main` nesta ordem:

| Método | O que faz |
|---|---|
| `initSettings` | Janela de 960x832 (15x13 tiles de 64 px), título e menu principal |
| `initGameVars` | Cria as variáveis `level`, `score`, `lives` e `enemies` |
| `initGame` | Registra a factory, carrega a fase, cria o jogador e inicia a música |
| `initInput` | Liga as teclas às ações do jogador |
| `initPhysics` | Define o que acontece nas colisões |
| `initUI` | Cria o HUD (pontos, vidas, fase e inimigos) |

- **Entity**: qualquer objeto do mundo (jogador, bomba, monstro, parede).
- **Component**: o comportamento acoplado à entidade (`Player`, `Bomb`, `Monster`).
- **Factory**: a "receita" de cada entidade, chamada por `spawn("nome", ...)`.
- **Variáveis do jogo**: valores globais (`geti("score")`, `inc("lives", -1)`) ligados ao HUD.

### Mapas (Tiled)

Cada fase é um arquivo `.tmx` em `assets/levels/`, com **só camadas de objetos** de 64x64 px, alinhados
à grade. O campo `type` de cada objeto é o nome do `@Spawns` da factory:

| `type` | Objeto |
|---|---|
| `wall` | Parede fixa (indestrutível) |
| `brick` | Tijolo (destrutível) |
| `monster` | Posição inicial de um monstro |

O chão e a posição inicial do jogador (coluna 1, linha 1) são definidos no `LevelLoader`.
Para criar uma fase nova, salve um dos mapas como `MapaN.tmx` e ajuste `Config.TOTAL_LEVELS`.

## Divisão do trabalho

| Integrante | Responsabilidade |
|---|---|
| Raquel | Jogador, bomba, explosão, upgrades, animação da bomba, sons e música |
| Leticia | Mapas no Tiled, `LevelLoader`, paredes, tijolos e sprites |
| Julia | Monstros, UI (menu, HUD, game over, vitória) e fluxo do jogo |

## Problemas comuns

| Sintoma | Solução |
|---|---|
| `release version 25 not supported` | Instalar o JDK 25 |
| `mvn` não é reconhecido | Instalar o Maven e reabrir o terminal |
| Erro de textura ou som não encontrado | Conferir o nome do arquivo (maiúsculas e extensão) em `assets/` |
| Aviso `TiledMap generated from 1.12.2. Supported version: 1.9.0` | Pode ser ignorado, os mapas carregam normalmente |

## Referências

- [FXGL](https://github.com/AlmasB/FXGL) e [JavaFX Media](https://openjfx.io/javadoc/23/javafx.media/javafx/scene/media/package-summary.html)
- Efeitos sonoros do [Pixabay](https://pixabay.com/sound-effects/) ([licença](https://pixabay.com/service/license-summary/)):
  `brick_break_freesound.wav`, `explosao_lumora.wav`, `game_over_alphix.wav`, `hit_chisper.wav`,
  `place_freesound.wav`, `powerup_freesound.wav`, `win_freesound.wav`
- `monster_die.wav`: gerado para o projeto com auxílio do Claude (Anthropic)
