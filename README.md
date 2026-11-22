# Bomberman PI

Jogo estilo Bomberman desenvolvido em **Java** com o framework **FXGL**, para a disciplina de PI (Programação Orientada a Objetos), aplicando o que foi aprendido em aula.

O jogador anda por um labirinto, coloca bombas para abrir caminho, derrota os monstros e avança de fase sem perder todas as vidas.

## Sumário

- [Tecnologias](#tecnologias)
- [Como rodar](#como-rodar)
- [Controles](#controles)
- [Regras do jogo](#regras-do-jogo)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Como o código funciona](#como-o-código-funciona)
- [Divisão do trabalho](#divisão-do-trabalho)
- [Fluxo de trabalho com Git](#fluxo-de-trabalho-com-git)
- [Pendências](#pendências)
- [Problemas comuns](#problemas-comuns)
- [Referências](#referências)

## Tecnologias

| Item | Versão |
|---|---|
| Java (JDK) | 25 (LTS) |
| Maven | 3.9 ou superior |
| FXGL | 21.1 |
| IDE sugerida | VS Code (com Extension Pack for Java) |

## Como rodar

1. Confira as versões instaladas:

   ```bash
   java -version
   mvn -version
   ```

   O `java -version` deve mostrar **25**. Com um JDK mais antigo a compilação falha com `release version 25 not supported`.

2. Clone o repositório e entre na pasta:

   ```bash
   git clone https://github.com/Julia-Muramoto3009/Projeto_Jogo_PI.git
   cd Projeto_Jogo_PI
   ```

3. Compile e rode:

   ```bash
   mvn clean javafx:run
   ```

Se abrir a janela do jogo, o ambiente está correto.

## Controles

| Tecla | Ação |
|---|---|
| `W` `A` `S` `D` | Mover o jogador |
| `Espaço` | Colocar bomba |
| `Esc` | Pausar |
| `K` | **Só para teste:** mata um monstro (será removida na versão final) |

## Regras do jogo

- O jogador começa com **3 vidas** e o jogo tem **3 fases**.
- A bomba explode depois de **2,5 segundos**, em formato de cruz, com alcance inicial de **2 tiles**.
- A chama **não atravessa paredes fixas** e **para ao atingir um tijolo**, destruindo-o.
- Tocar em um monstro ou ser atingido por uma explosão **tira uma vida**. O jogador volta ao ponto inicial e fica **invencível por 2 segundos**.
- Cada monstro derrotado vale **100 pontos**.
- A fase termina quando todos os monstros forem derrotados.
- Ao zerar as vidas aparece o **Game Over**, e ao vencer a última fase aparece a tela de **Vitória**. Nos dois casos o jogo volta ao menu principal.

Todos esses valores ficam em `Config.java` e podem ser ajustados em um lugar só.

## Estrutura de pastas

```
.
├── pom.xml                 Dependências (FXGL) e configuração do Maven
├── README.md               
├── .gitignore              Arquivos que não vão para o Git
└── src/main/
    ├── java/com/grupito/bomberman/
    │   ├── Main.java               Ponto de entrada (GameApplication), input, física e fluxo do jogo
    │   ├── BombermanFactory.java   Cria as entidades do jogo (@Spawns)
    │   ├── EntityType.java         Enum com os tipos de entidade
    │   ├── Config.java             Constantes (tamanho do tile, velocidades, tempos)
    │   ├── components/             Comportamento de cada entidade
    │   │   ├── Player.java         Jogador: movimento e bloqueio por obstáculos
    │   │   ├── Bomb.java           Bomba: pavio e detonação
    │   │   ├── Explosion.java      Explosão em cruz e seu alcance
    │   │   ├── Monster.java        Inimigos e sua IA
    │   │   ├── Upgrade.java        Power-ups
    │   │   └── Wall.java           Paredes e tijolos destrutíveis
    │   ├── level/
    │   │   └── LevelLoader.java    Lê o arquivo da fase e cria as entidades
    │   └── UI/
    │       └── Menu.java           Menu e telas (HUD, game over, vitória)
    └── resources/assets/           Recursos carregados pelo FXGL
        ├── levels/                 Mapas das fases
        ├── textures/               Imagens (sprites, ícones)
        ├── sounds/                 Efeitos sonoros curtos (.wav)
        ├── music/                  Trilhas musicais (.mp3)
```

> O FXGL procura os recursos em `src/main/resources/assets/<tipo>/`. Por isso os arquivos precisam estar nessas pastas, e o nome usado no código deve ser **idêntico** ao do arquivo (ex.: `texture("Bomberman_sprite.png")`).

### Pastas geradas automaticamente (não editar, não versionar)

- `target/`: resultado da compilação do Maven.
- `.vscode/`: configurações locais do editor.

## Como o código funciona

O FXGL chama os métodos da `Main` nesta ordem. Cada um tem uma função:

| Método | O que faz neste projeto |
|---|---|
| `initSettings` | Janela 960x832 (15x13 tiles de 64 px), título e menu principal |
| `initGameVars` | Cria as variáveis `score`, `lives`, `level` e `enemies` |
| `initGame` | Registra a `BombermanFactory` e cria o jogador e os inimigos |
| `initInput` | Liga as teclas às ações do jogador |
| `initPhysics` | Define o que acontece nas colisões (jogador × monstro, explosão × monstro, explosão × jogador) |
| `initUI` | Cria o HUD (pontos, vidas, fase e inimigos) |

Conceitos usados:

- **Entity**: qualquer objeto do mundo (jogador, bomba, monstro, parede).
- **EntityType**: o "rótulo" da entidade, usado nas colisões e nas buscas.
- **Component**: o comportamento grudado na entidade (`Player`, `Bomb`, `Monster`).
- **Factory**: a "receita" de cada entidade, chamada com `spawn("nome", x, y)`.
- **Variáveis do jogo**: valores globais (`geti("score")`, `inc("lives", -1)`). O HUD é ligado a elas e atualiza sozinho.

### Funções públicas da `Main` para o resto do grupo

| Função | Quem chama | Quando |
|---|---|---|
| `Main.onPlayerHit(player)` | Colisões | O jogador foi atingido: tira uma vida, reposiciona e dá invencibilidade |
| `Main.killMonster(monster)` | Colisões | O monstro foi atingido: soma pontos e verifica a vitória |
| `Main.setPlayerSpawn(x, y)` | `LevelLoader` | Ao carregar a fase, informa onde o jogador renasce |
| `Main.levelComplete()` | Porta de saída | O jogador chegou à saída: abre a próxima fase |
| `Main.gameOver()` | Regras do fluxo | Acabaram as vidas |

### Regras para criar novas entidades

1. Acrescente o tipo no **fim** do `EntityType` (para não bagunçar o código dos outros).
2. Crie o `@Spawns("nome")` na `BombermanFactory`, dentro do bloco da sua área.
3. Para a entidade participar de colisões, ela precisa de **hitbox** (`bbox` ou `viewWithBBox`) e de `.collidable()`.
4. Todas as posições devem estar **alinhadas à grade** (múltiplos de `Config.TILE`).

## Divisão do trabalho

| Integrante | Responsabilidade |
|---|---|
| Raquel | Jogador, bomba e explosão |
| Leticia | Mapa, `LevelLoader`, paredes, tijolos e power-ups |
| Julia | Inimigos, UI (menu, HUD, game over, vitória) e fluxo do jogo |

## Fluxo de trabalho com Git

1. Antes de começar: `git pull` na `main`.
2. Crie uma branch para a sua tarefa:

   ```bash
   git checkout -b feature/nome-da-tarefa
   ```

3. Faça commits pequenos, um por passo que funciona:

   ```bash
   git add .
   git commit -m "monstro anda pela grade"
   ```

4. Envie a branch e abra um Pull Request:

   ```bash
   git push -u origin feature/nome-da-tarefa
   ```

5. Peça a revisão de alguém do grupo antes de fazer o merge.

Dicas para evitar conflitos:

- A `Main.java` e a `BombermanFactory.java` são **compartilhadas**. Mexa só no bloco da sua área, marcado com o seu nome.
- Rode `git pull` com frequência e nunca faça commit de `target/`.

## Pendências

- [x] Jogador com movimento e bloqueio por obstáculos
- [x] Bomba com pavio e explosão em cruz
- [x] Monstros com movimento pela grade
- [x] HUD, menu principal, game over e vitória
- [x] Vidas, invencibilidade e troca de fase
- [ ] `LevelLoader` lendo o mapa do Tiled (`levels/`)
- [ ] Monstros nascendo a partir do mapa (remover os monstros de teste da `Main`)
- [ ] Alinhar a grade do mapa (`.tmj`) com a `Config` (15x13 tiles de 64 px)
- [ ] Porta de saída e tijolos destrutíveis
- [ ] Power-ups (`Upgrade`)
- [ ] Detonação em cadeia de bombas
- [ ] Sprites e animações (jogador, bomba, monstro)
- [ ] Música e efeitos sonoros (converter os arquivos para `.mp3` e `.wav`)
- [ ] Segundo tipo de inimigo (que persegue o jogador)
- [ ] Remover o código de teste (tecla `K`)

## Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `release version 25 not supported` | JDK antigo | Instalar JDK 25 (LTS) |
| `mvn` não é reconhecido | Maven fora do PATH | Instalar o Maven e reabrir o terminal |
| Erro de classe principal não encontrada | `mainClassName` do `pom.xml` diferente da `Main` | Usar `com.grupito.bomberman.Main` |
| Erro ao criar uma entidade com textura | Nome do arquivo diferente do usado em `texture("...")` | Conferir o nome (maiúsculas e extensão) em `assets/textures/` |
| Monstro atravessa o jogador | Falta hitbox ou `.collidable()` | Conferir a entidade na `BombermanFactory` |
| Muitos conflitos no `git pull` | Arquivos de `target/` versionados | `git rm -r --cached target` e conferir o `.gitignore` |

## Equipe


## Referências
 
### Áudio
 
Sons baixados do [Pixabay](https://pixabay.com/sound-effects/) (licença de uso gratuito):
 
- `brick_break_freesound.wav`: freesound
- `explosao_lumora.wav`: lumora
- `game_over_alphix.wav`: alphix
- `hit_chisper.wav`: chisper
- `place_freesound.wav`: freesound
- `powerup_freesound.wav`: freesound
- `win_freesound.wav`: freesound

Sons criados para o projeto, com o script `tools/gerar_audio.py` e auxílio do Claude (Anthropic):
- `monster_die.wav`

### Documentação
 
- [FXGL](https://github.com/AlmasB/FXGL)
- [JavaFX Media](https://openjfx.io/javadoc/23/javafx.media/javafx/scene/media/package-summary.html)
- [Licença do Pixabay](https://pixabay.com/service/license-summary/)
 

Projeto desenvolvido em grupo para a disciplina de PI.
