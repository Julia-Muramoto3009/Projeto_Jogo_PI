# Bomberman (FXGL)

Clone do jogo Bomberman feito em Java com [FXGL](https://github.com/AlmasB/FXGL) e Maven.
Projeto com objetivo de **aprendizado**, desenvolvido em grupo por 3 pessoas.

## Equipe

| Responsável | Área |
|---|---|
| Raquel | Jogador e bombas |
| Leticia | Mundo e itens |
| Julia | Inimigos, UI e fluxo do jogo |

## Como rodar

Pré-requisitos: JDK 17.0.20  e Maven 3.10.0 (rode `java -version` e `mvn -version` no terminal para verificar).

```bash
mvn clean javafx:run
```

Se abrir a janela do jogo, o ambiente está correto.

## Estrutura de pastas

```
.
├── pom.xml                         Dependências (FXGL) e configuração do Maven
├── README.md                       Este arquivo
├── .gitignore                      Arquivos que não vão para o Git
└── src/main/
    ├── java/com/grupito/bomberman/
    │   ├── Main.java               Ponto de entrada (GameApplication)
    │   ├── BombermanFactory.java   Cria as entidades do jogo (@Spawns)
    │   ├── EntityType.java         Enum com os tipos de entidade
    │   ├── Config.java             Constantes (tamanho do tile, velocidades, tempos)
    │   ├── components/             Comportamento de cada entidade
    │   │   ├── Player.java         Jogador: movimento e ações
    │   │   ├── Bomb.java           Bomba: pavio e detonação
    │   │   ├── Explosion.java      Explosão em cruz e seu alcance
    │   │   ├── Monster.java        Inimigos e sua IA
    │   │   ├── Upgrade.java        Power-ups
    │   │   └── Wall.java           Paredes e tijolos destrutíveis
    │   ├── level/
    │   │   └── LevelLoader.java    Lê o arquivo da fase e cria as entidades
    │   └── ui/
    │       └── Menu.java           Menu e telas (HUD, game over, vitória)
    └── resources/assets/           Recursos carregados pelo FXGL
        ├── levels/                 Mapas das fases
        ├── textures/               Imagens (sprites, ícones)
        ├── sounds/                 Efeitos sonoros curtos (.wav)
        ├── music/                  Trilhas musicais (.mp3)
        └── ui/                     Imagens e recursos de interface
```

> O FXGL procura os recursos em `src/main/resources/assets/<tipo>/`.
> Por isso os arquivos precisam estar nessas pastas.

### Pastas geradas automaticamente (não editar, não versionar)

- `target/`: resultado da compilação do Maven.
- `logs/`: logs gerados pelo FXGL a cada execução.
- `system/`: arquivos internos do FXGL.

## Arquivos principais

| Arquivo | Para que serve |
|---|---|
| `Main.java` | Configura a janela, registra input, colisões e inicia o jogo. Quem precisar registrar algo aqui deve chamar um método da sua área, sem escrever a lógica dentro do `Main`. |
| `BombermanFactory.java` | Fábrica das entidades. Cada `@Spawns("nome")` descreve como criar um tipo de entidade (jogador, bomba, parede, monstro...). |
| `EntityType.java` | Lista os tipos de entidade (PLAYER, BOMB, EXPLOSION, WALL, BRICK, ENEMY, POWERUP...). Usado nas colisões e buscas. |
| `Config.java` | Valores fixos do jogo, como tamanho do tile, velocidade, tempo do pavio e alcance da bomba. |
| `LevelLoader.java` | Carrega a fase a partir de um arquivo em `assets/levels/` e faz o `spawn` de cada elemento. |
| `Menu.java` | Menu principal e demais telas. |

## Mapas das fases

Cada fase será um arquivo de texto em `assets/levels/` (`fase1.txt`, `fase2.txt`, ...).
Criar uma fase nova é criar um arquivo, sem escrever código novo.

Legenda sugerida:

| Símbolo | Significado |
|---|---|
| `#` | Parede fixa |
| `*` | Tijolo destrutível |
| `.` | Espaço vazio |
| `P` | Posição inicial do jogador |
| `E` | Inimigo |

## Divisão de tarefas

### Raquel (jogador e bombas)

- Arquivos: `Player.java`, `Bomb.java`, `Explosion.java`
- Input do teclado e movimento em grid
- Colocar bomba e propagar a explosão em cruz
- Colisão: jogador × parede/bomba
- Assets: `Bomber1.png`, `Bomb.png`

### Leticia (mundo e itens)

- Arquivos: `Wall.java`, `Upgrade.java`, `LevelLoader.java`, mapas em `assets/levels/`
- Carregamento das fases e grid do mapa
- Tijolos que quebram e power-ups que aparecem ao quebrá-los
- Porta de saída
- Colisões: explosão × tijolo, jogador × power-up
- Assets: `HardWall.png`, `SoftWall.png`, `PowerFireUp.png`

### Julia (inimigos, UI e fluxo)

- Arquivos: `Monster.java`, `Menu.java` e novos arquivos em `ui/` (HUD, game over, vitória)
- IA dos inimigos
- Menu, HUD, vidas, pontuação, game over e vitória
- Áudio (música e efeitos)
- Colisões: jogador × inimigo, explosão × inimigo
- Assets: `bomberman.png`, `icon.png`, sons e músicas

### Dependências entre as áreas

- Raquel deve entregar cedo uma entidade `EXPLOSION` simples (mesmo sem animação), pois B (tijolos) e C (inimigos) reagem a ela.
- Julia precisa do grid criado por pela Leticia para saber onde há paredes. Enquanto isso, testar o monstro em um mapa vazio.

## Arquivos compartilhados (cuidado com conflitos)

Estes arquivos são editados pelas três pessoas:

- **`BombermanFactory.java`**: adicionar os `@Spawns` em um bloco próprio, com comentário de seção (ex.: `// ===== Raquel: jogador e bomba =====`). Commits pequenos e frequentes.
- **`EntityType.java`**: apenas acrescentar valores no fim do enum e avisar o grupo.
- **`Main.java`**: apenas adicionar chamadas a métodos da própria área.
- **`Config.java`**: apenas acrescentar constantes, sem alterar as que outras pessoas usam.

## Fluxo de Git

1. A branch `main` fica sempre funcionando (compila e abre o jogo).
2. Cada funcionalidade em uma branch própria: `feature/bomba`, `feature/level-loader`, `feature/monstro`...
3. Ao terminar, abrir um Pull Request para a `main`.
4. Outra pessoa do grupo revisa antes do merge. Assim todas conhecem o código inteiro.
5. Antes de começar algo novo: `git pull` na `main`.

## Fases do projeto

1. **Fundação (em grupo):** projeto rodando, janela abrindo, jogador andando, mapa fixo.
2. **Mecânicas básicas (em paralelo):** bomba e explosão (A), mapa e tijolos (B), um monstro e HUD (C).
3. **Integração:** conectar as colisões e o menu.
4. **Polimento:** power-ups variados, várias fases, sons, animações e IA melhor.

## Pendências conhecidas

- Converter `music.ogg` para `.mp3`: o JavaFX Media não toca `.ogg`.
- Substituir `BombAnimation.mp4` por um efeito `.wav` (em `sounds/`) e por uma spritesheet em PNG (em `textures/`).
