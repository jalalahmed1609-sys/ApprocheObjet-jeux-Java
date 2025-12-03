package fr.ubordeaux.ao.project07;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import fr.ubordeaux.ao.project07.engine.IWindowGame;

public class LevelMap {
    private Set<Point> walkable;
    private List<Point> currentTiles;
    private List<Point> spiderSpawns;
    private List<Point> answerPositions;
    private int floorTileId = 72;
    private int wallTileId = -1;
    private int borderTileId = -1;
    private int cornerTileId = -1;
    
    public LevelMap() {
        this.walkable = new HashSet<>();
        this.currentTiles = new ArrayList<>();
        this.spiderSpawns = new ArrayList<>();
        this.answerPositions = new ArrayList<>();
    }

    public void setFloorTileId(int id) {
        this.floorTileId = id;
    }

    public void setWallTileId(int id) {
        this.wallTileId = id;
    }

    public void setBorderTileId(int id) {
        this.borderTileId = id;
    }

    public void setCornerTileId(int id) {
        this.cornerTileId = id;
    }

    public Set<Point> getWalkable() {
        return walkable;
    }
    
    public List<Point> getSpiderSpawns() {
        return spiderSpawns;
    }

    public List<Point> getAnswerPositions() {
        return answerPositions;
    }

    public void loadLevel(int level, IWindowGame window) {
        // Clear old tiles
        for (Point p : currentTiles) {
            window.remove(floorTileId, p.x, p.y, 0);
            if (wallTileId != -1) {
                window.remove(wallTileId, p.x, p.y, 0);
            }
            if (borderTileId != -1) {
                window.remove(borderTileId, p.x, p.y, 0);
            }
            if (cornerTileId != -1) {
                window.remove(cornerTileId, p.x, p.y, 0);
            }
        }
        currentTiles.clear();
        walkable.clear();
        spiderSpawns.clear();
        answerPositions.clear();

        String[] layout = getLayout(level);
        int height = layout.length;
        int width = layout[0].length();

        // Draw borders
        if (borderTileId != -1 && cornerTileId != -1) {
            window.addBorder(borderTileId, cornerTileId, 0, 0, width, height);
            
            // Track border tiles for removal
            for (int x = -1; x <= width; x++) {
                for (int y = -1; y <= height; y++) {
                    if (x == -1 || x == width || y == -1 || y == height) {
                        currentTiles.add(new Point(x, y));
                    }
                }
            }
        }
        
        for (int y = 0; y < layout.length; y++) {
            String row = layout[y];
            for (int x = 0; x < row.length(); x++) {
                if (x >= row.length()) continue;
                char c = row.charAt(x);
                if (c == '#' || c == 'S' || c == 'A') {
                    Point p = new Point(x, y);
                    walkable.add(p);
                    window.add(floorTileId, x, y, 0);
                    currentTiles.add(p);
                    
                    if (c == 'S') {
                        spiderSpawns.add(p);
                    } else if (c == 'A') {
                        answerPositions.add(p);
                    }
                } else if (c == '.') {
                    if (wallTileId != -1) {
                        Point p = new Point(x, y);
                        window.add(wallTileId, x, y, 0);
                        currentTiles.add(p);
                    }
                }
            }
        }
    }

    private String[] getLayout(int level) {
        int mapIndex = (level - 1) % 8;
        switch (mapIndex) {
            case 0: // Level 1 - 3 Spiders
                        return new String[] {
                                "###############",
                                "#S..#.....#..A#",
                                "#.#.#.###.#.#.#",
                                "#.#...S...#.#.#",
                                "#.#.#####.#.#.#",
                                "#A#.....#...S.#",
                                "###############"
                        };
            case 1: // Level 2 - 4 Spiders
                        return new String[] {
                            "###############",
                            "#S..#...#...SA#",
                            "#.#.#.#.#.#.#.#",
                            "#...S.#.#.S...#",
                            "#.#.#.#.#.#.#.#",
                            "#SA...#...#..S#",
                            "###############"
                        };
            case 2: // Level 3 - 5 Spiders
                        return new String[] {
                            "###############",
                            "#S.#.#.#.#.S.A#",
                            "#.#.#.#.#.#.#.#",
                            "#...S.#.#.S...#",
                            "#.#.#.#.#.#.#.#",
                            "#A.#.#.#.#.#.S#",
                            "###############"
                        };
            case 3: // Level 4 - 6 Spiders
                        return new String[] {
                            "###############",
                            "#S..#.#.#..S..#",
                            "#.#.#.#.#.#.#.#",
                            "#..S.#.#.S..A.#",
                            "#.#.#.#.#.#.#.#",
                            "#A..#.#.#..S..#",
                            "###############"
                        };
            case 4: // Level 5 - 7 Spiders
                        return new String[] {
                            "###############",
                            "#S.#.#.#.#.S..#",
                            "#.#.#.#.#.#.#.#",
                            "#..S.#.#.S..A.#",
                            "#.#.#.#.#.#.#.#",
                            "#A.#.#.#.#.S..#",
                            "###############"
                        };
            case 5: // Level 6 - 8 Spiders
                        return new String[] {
                            "###############",
                            "#S.#.#.#.#.S..#",
                            "#.#.#.#.#.#.#.#",
                            "#..S.#.#.S..A.#",
                            "#.#.#.#.#.#.#.#",
                            "#A.#.#.#.#.S..#",
                            "###############"
                        };
            case 6: // Level 7 - 9 Spiders
                        return new String[] {
                            "###############",
                            "#S.#.#.#.#.S..#",
                            "#.#.#.#.#.#.#.#",
                            "#..S.#.#.S..A.#",
                            "#.#.#.#.#.#.#.#",
                            "#A.#.#.#.#.S..#",
                            "###############"
                        };
            case 7: // Level 8 - 10 Spiders
                        return new String[] {
                            "###############",
                            "#S.#.#.#.#.S..#",
                            "#.#.#.#.#.#.#.#",
                            "#..S.#.#.S..A.#",
                            "#.#.#.#.#.#.#.#",
                            "#A.#.#.#.#.S..#",
                            "###############"
                        };
            default:
                return new String[] {
                    "###############",
                    "###############",
                    "###############",
                    "###############",
                    "###############",
                    "###############",
                    "###############"
                };
        }
    }
}
