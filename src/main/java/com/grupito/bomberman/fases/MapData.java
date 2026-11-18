package com.grupito.bomberman.fases;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê um mapa do Tiled (.tmj) e guarda as células (coluna, linha)
 * de paredes fixas e tijolos quebráveis.
 *
 * Convenção do mapa (ordem dos tilesets no Tiled):
 *   gid 1 = HardWall (parede fixa)
 *   gid 2 = SoftWall1 (tijolo quebrável)
 *   gid 3 = Piso3 (chão)
 */
public final class MapData {

    public static final int GID_WALL = 1;
    public static final int GID_BRICK = 2;

    private static final Pattern TILE_LAYER = Pattern.compile(
            "\"data\"\\s*:\\s*\\[([^\\]]*)\\]\\s*,\\s*\"height\"\\s*:\\s*(\\d+).*?\"width\"\\s*:\\s*(\\d+)",
            Pattern.DOTALL);
    private static final Pattern TILE_OBJECT = Pattern.compile("\\{[^{}]*\"gid\"\\s*:\\s*\\d+[^{}]*\\}");

    private final int cols;
    private final int rows;
    private final List<int[]> walls = new ArrayList<>();   // {col, row}
    private final List<int[]> bricks = new ArrayList<>();  // {col, row}
    private final List<String> warnings = new ArrayList<>();

    public MapData(String tmjText, int tileSize) {
        int c = 0, r = 0;
        boolean[][] wall = null;
        boolean[][] brick = null;

        Matcher layer = TILE_LAYER.matcher(tmjText);
        while (layer.find()) {
            int w = Integer.parseInt(layer.group(3));
            int h = Integer.parseInt(layer.group(2));
            if (wall == null) {
                c = w;
                r = h;
                wall = new boolean[r][c];
                brick = new boolean[r][c];
            }
            String[] values = layer.group(1).trim().split("\\s*,\\s*");
            for (int i = 0; i < values.length && i < w * h; i++) {
                if (values[i].isEmpty()) continue;
                int gid = Integer.parseInt(values[i]);
                int row = i / w;
                int col = i % w;

                if (gid == GID_WALL) {
                    wall[row][col] = true;
                } else if (gid == GID_BRICK) {
                    brick[row][col] = true;
                }
            }
        }
        if (wall == null) {
            throw new IllegalArgumentException("Nenhuma camada de tiles encontrada no mapa");
        }
        this.cols = c;
        this.rows = r;

        Set<Long> seen = new LinkedHashSet<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (wall[row][col]) {
                    walls.add(new int[] { col, row });
                } else if (brick[row][col]) {
                    seen.add((long) row * cols + col);
                    bricks.add(new int[] { col, row });
                }
            }
        }

        // Mantém compatibilidade caso algum mapa antigo use camada de objetos
        Matcher obj = TILE_OBJECT.matcher(tmjText);
        while (obj.find()) {
            String o = obj.group();
            if ((int) number(o, "gid") != GID_BRICK) {
                continue;
            }
            int col = (int) Math.round(number(o, "x") / tileSize);
            int row = (int) Math.round((number(o, "y") - number(o, "height")) / tileSize);

            if (col < 0 || row < 0 || col >= cols || row >= rows) {
                warnings.add("Tijolo fora do mapa ignorado: coluna " + col + ", linha " + row);
            } else if (wall[row][col]) {
                warnings.add("Tijolo sobre parede fixa ignorado: coluna " + col + ", linha " + row);
            } else if (!seen.add((long) row * cols + col)) {
                warnings.add("Tijolo duplicado ignorado: coluna " + col + ", linha " + row);
            } else {
                bricks.add(new int[] { col, row });
            }
        }
    }

    private static double number(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?[0-9.]+(?:[eE][-+]?[0-9]+)?)").matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Campo \"" + key + "\" não encontrado em: " + json);
        }
        return Double.parseDouble(m.group(1));
    }

    public int cols() { return cols; }
    public int rows() { return rows; }
    public List<int[]> walls() { return walls; }
    public List<int[]> bricks() { return bricks; }
    public List<String> warnings() { return warnings; }
}