package fr.ubordeaux.ao.project07;
import fr.ubordeaux.ao.project07.engine.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;



import fr.ubordeaux.ao.project07.engine.IWindowGame;

import fr.ubordeaux.ao.project07.engine.WindowGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public class MathSpiderGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IWindowGame window = new WindowGame();

            // Chargement des images personnalisées pour les ponts
            int bridgeOK = 1000;
            int bridgeBroken = 1001;
            int floorId = 1002;
            int borderId = 1003;
            int cornerId = 1004;
            int numberBaseId = 2000; // IDs 2000-2009 pour les chiffres

            try {
                // Chargement du sol (dirt_E)
                BufferedImage imgFloor = ImageIO.read(MathSpiderGame.class.getResource("/Dungeon-Pack/png/dirt_E.png"));
                if (imgFloor != null) {
                    ((WindowGame)window).loadImagesSheet("/Dungeon-Pack/png/dirt_E.png", imgFloor.getWidth(), imgFloor.getHeight(), 1f, 0, 0, floorId);
                }

                // Chargement des bordures (dirtTiles_E)
                BufferedImage imgBorder = ImageIO.read(MathSpiderGame.class.getResource("/Dungeon-Pack/png/dirtTiles_E.png"));
                if (imgBorder != null) {
                    ((WindowGame)window).loadImagesSheet("/Dungeon-Pack/png/dirtTiles_E.png", imgBorder.getWidth(), imgBorder.getHeight(), 1f, 0, 0, borderId);
                }

                // Chargement des angles (barrel_E)
                BufferedImage imgCorner = ImageIO.read(MathSpiderGame.class.getResource("/Dungeon-Pack/png/barrel_E.png"));
                if (imgCorner != null) {
                    ((WindowGame)window).loadImagesSheet("/Dungeon-Pack/png/barrel_E.png", imgCorner.getWidth(), imgCorner.getHeight(), 1f, 0, 0, cornerId);
                }

                // Chargement du pont cassé
                BufferedImage imgBroken = ImageIO.read(MathSpiderGame.class.getResource("/images/pont/planksBroken_E.png"));
                if (imgBroken != null) {
                    ((WindowGame)window).loadImagesSheet("/images/pont/planksBroken_E.png", imgBroken.getWidth(), imgBroken.getHeight(), 1f, 0, 75, bridgeBroken);
                }

                // Chargement des images de numéros (0-9)
                for (int i = 0; i <= 9; i++) {
                    String path = "/images/numero/" + i + ".png";
                    BufferedImage imgNum = ImageIO.read(MathSpiderGame.class.getResource(path));
                    if (imgNum != null) {
                        // On charge chaque chiffre comme une "feuille" d'une seule image
                        // On ajuste l'échelle si besoin (ici 0.25f pour réduire la taille) et on centre (offset 0,0)
                        ((WindowGame)window).loadImagesSheet(path, imgNum.getWidth(), imgNum.getHeight(), 0.25f, 0, 0, numberBaseId + i);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement des images.");
            }

            // Fond et zone de jeu
            ((WindowGame)window).setBackground(java.awt.Color.decode("#375463"));
            
            // Définition des positions autorisées (mask)
            final Set<Point> walkable = new HashSet<>();
            
            LevelMap levelMap = new LevelMap();
            levelMap.setWallTileId(floorId); // Utiliser dirt_E pour les murs/vide
            levelMap.setBorderTileId(borderId);
            levelMap.setCornerTileId(cornerId);
            levelMap.loadLevel(1, window);
            walkable.addAll(levelMap.getWalkable());

            // --- MATH GAME SETUP ---
            MathPuzzle mathPuzzle = new MathPuzzle(numberBaseId);
            
            // On lance le premier niveau après la création de l'interface (plus bas)
            // --- END MATH GAME SETUP ---

            // Création du joueur
            Crusader crusader = new Crusader();
            crusader.setScale(2.5f);
            // Ajustement de l'offset selon la demande : Hauteur 0,8 sur longeur 0,15
            // crusader.setOffset(0.15f, 0.8f);
            // position de départ
            final int[] playerPos = {0, 0}; // Commence à 0,0 comme demandé pour le respawn
            crusader.setPosition(playerPos[0], playerPos[1], 0);
            window.add(crusader);

            JFrame frame = (JFrame) window;
            
            // Récupérer le panneau de jeu (BoardGame) qui est actuellement le ContentPane
            java.awt.Container gamePanel = frame.getContentPane();
            
            // Créer un nouveau panneau principal pour organiser l'interface
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(java.awt.Color.decode("#232f3e"));
            
            // --- HEADER UI ---
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(java.awt.Color.decode("#232f3e"));
            
            // Left: Question
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new javax.swing.BoxLayout(questionPanel, javax.swing.BoxLayout.Y_AXIS));
            questionPanel.setBackground(java.awt.Color.decode("#232f3e"));
            
            JLabel lblQuestionTitle = new JLabel("Question 1 :");
            lblQuestionTitle.setForeground(java.awt.Color.WHITE);
            lblQuestionTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            
            JLabel lblEquation = new JLabel("A + B = C");
            lblEquation.setForeground(java.awt.Color.WHITE);
            lblEquation.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
            
            questionPanel.add(lblQuestionTitle);
            questionPanel.add(javax.swing.Box.createVerticalStrut(10));
            questionPanel.add(lblEquation);
            questionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // Right: Instructions
            JPanel instructionsPanel = new JPanel();
            instructionsPanel.setLayout(new javax.swing.BoxLayout(instructionsPanel, javax.swing.BoxLayout.Y_AXIS));
            instructionsPanel.setBackground(java.awt.Color.decode("#232f3e"));
            
            JLabel lblInstrTitle = new JLabel("Instructions :");
            lblInstrTitle.setForeground(java.awt.Color.WHITE);
            
            JLabel lblInstr1 = new JLabel("• utiliser Button ( Espace ) pour attaquer les Spiders");
            lblInstr1.setForeground(java.awt.Color.WHITE);
            
            JLabel lblInstr2 = new JLabel("• Choisir la bonne réponse pour passé à un autre niveau");
            lblInstr2.setForeground(java.awt.Color.WHITE);
            
            JLabel lblInstr3 = new JLabel("• si besoin ! Button *** S *** pour Sauter ");
            lblInstr3.setForeground(java.awt.Color.WHITE);

            instructionsPanel.add(lblInstrTitle);
            instructionsPanel.add(lblInstr1);
            instructionsPanel.add(lblInstr2);
            instructionsPanel.add(lblInstr3);
            instructionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // Center: Feedback
            JLabel lblFeedback = new JLabel("", javax.swing.SwingConstants.CENTER);
            lblFeedback.setForeground(java.awt.Color.GREEN);
            lblFeedback.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            
            headerPanel.add(questionPanel, BorderLayout.WEST);
            headerPanel.add(lblFeedback, BorderLayout.CENTER);
            headerPanel.add(instructionsPanel, BorderLayout.EAST);
            
            // Ajouter le header au Nord
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            
            // Ajouter le jeu au Centre
            mainPanel.add(gamePanel, BorderLayout.CENTER);

            JPanel ctrl = new JPanel(new FlowLayout());
            JLabel status = new JLabel("Position: (" + playerPos[0] + "," + playerPos[1] + ")");
            
            // Ajouter le status au Sud
            ctrl.add(status);
            mainPanel.add(ctrl, BorderLayout.SOUTH);
            
            // Définir le nouveau ContentPane
            frame.setContentPane(mainPanel);
            frame.revalidate();
            
            // Initialisation du niveau 1
            mathPuzzle.prepareNextLevel(window, levelMap.getAnswerPositions());
            lblQuestionTitle.setText("Question " + mathPuzzle.getLevel() + " :");
            lblEquation.setText(mathPuzzle.getCurrentA() + " + " + mathPuzzle.getCurrentB() + " = ?");
            
            // --- SPIDERS SETUP ---
            List<Spider> spiders = new ArrayList<>();
            Map<Spider, Integer> spiderHealth = new HashMap<>();
            Map<Spider, int[]> spiderPos = new HashMap<>();
            
            // Variables pour la gestion de l'attaque et de la mort du joueur
            final int[] attackCounter = {0};
            final long[] lastDefenseTime = {0};

            // Positions des araignées (sur l'île de droite)
            // int[][] spawnPoints = {{10, 2}, {12, 4}, {14, 1}};

            for (Point pos : levelMap.getSpiderSpawns()) {
                Spider s = new Spider();
                s.setScale(2.5f); // Taille par défaut
                s.setPosition(pos.x, pos.y, 0);
                window.add(s);
                spiders.add(s);
                spiderHealth.put(s, 5); // 5 taps sur Espace pour tuer
                spiderPos.put(s, new int[]{pos.x, pos.y});
            }

            // Timer (déplacement toutes les 1s)
            Timer aiTimer = new Timer(1000, e -> {
                if (crusader.getCurrentMode() == Crusader.Mode.DEATH) return;

                boolean isUnderAttack = false;

                for (Spider s : spiders) {
                    if (s.getCurrentMode() == Spider.Mode.DEATH) continue;

                    int[] sPos = spiderPos.get(s);
                    int targetX = playerPos[0];
                    int targetY = playerPos[1];

                    // Calcul de la distance
                    double dist = Math.sqrt(Math.pow(targetX - sPos[0], 2) + Math.pow(targetY - sPos[1], 2));

                    if (dist <= 1.5) {
                        // Attaque si proche
                        s.setMode(Spider.Mode.ATTACK);
                        isUnderAttack = true;
                    } else {
                        // Déplacement vers le joueur
                        int dx = Integer.compare(targetX, sPos[0]);
                        int dy = Integer.compare(targetY, sPos[1]);

                        int nextX = sPos[0];
                        int nextY = sPos[1];

                        // Essayer de bouger en X
                        boolean occupiedX = false;
                        // Vérifier collision avec le joueur
                        if (sPos[0] + dx == playerPos[0] && sPos[1] == playerPos[1]) {
                            occupiedX = true;
                        }
                        // Vérifier collision avec les autres araignées
                        if (!occupiedX) {
                            for (Spider other : spiders) {
                                if (other != s && other.getCurrentMode() != Spider.Mode.DEATH) {
                                    int[] otherPos = spiderPos.get(other);
                                    if (otherPos[0] == sPos[0] + dx && otherPos[1] == sPos[1]) {
                                        occupiedX = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (dx != 0 && walkable.contains(new Point(sPos[0] + dx, sPos[1])) && !occupiedX) {
                            nextX += dx;
                            s.setDirection(dx > 0 ? Direction.EAST : Direction.WEST);
                        } 
                        // Sinon essayer en Y
                        else {
                            boolean occupiedY = false;
                            // Vérifier collision avec le joueur
                            if (sPos[0] == playerPos[0] && sPos[1] + dy == playerPos[1]) {
                                occupiedY = true;
                            }
                            // Vérifier collision avec les autres araignées
                            if (!occupiedY) {
                                for (Spider other : spiders) {
                                    if (other != s && other.getCurrentMode() != Spider.Mode.DEATH) {
                                        int[] otherPos = spiderPos.get(other);
                                        if (otherPos[0] == sPos[0] && otherPos[1] == sPos[1] + dy) {
                                            occupiedY = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (dy != 0 && walkable.contains(new Point(sPos[0], sPos[1] + dy)) && !occupiedY) {
                                nextY += dy;
                                s.setDirection(dy > 0 ? Direction.SOUTH : Direction.NORTH);
                            }
                        }

                        // Mise à jour si mouvement possible
                        if (nextX != sPos[0] || nextY != sPos[1]) {
                            s.setMode(Spider.Mode.WALK);
                            sPos[0] = nextX;
                            sPos[1] = nextY;
                            s.setPosition(nextX, nextY, 0);
                        } else {
                            s.setMode(Spider.Mode.IDLE);
                        }
                    }
                }

                // Gestion de la mort du joueur si attaqué sans défense
                if (isUnderAttack) {
                    // Si le joueur n'a pas attaqué (défendu) depuis 1.5s
                    if (System.currentTimeMillis() - lastDefenseTime[0] > 1500) {
                        attackCounter[0]++;
                        System.out.println("Joueur attaqué ! Compteur : " + attackCounter[0] + "/5");
                    } else {
                        attackCounter[0] = 0; // Reset si défense active
                    }
                } else {
                    attackCounter[0] = 0; // Reset si plus attaqué
                }

                if (attackCounter[0] >= 5) {
                    window.playSound("abstract/perish");
                    crusader.setMode(Crusader.Mode.DEATH);
                    status.setText("Mort par les araignées ! Respawn...");
                    
                    // Trigger de fin d'animation pour respawn
                    crusader.setEndAnimationTrigger(c -> {
                        playerPos[0] = 0;
                        playerPos[1] = 0;
                        c.setPosition(0, 0, 0);
                        if (c instanceof Crusader) {
                            ((Crusader) c).setMode(Crusader.Mode.IDLE);
                        }
                        status.setText("Position: (" + playerPos[0] + "," + playerPos[1] + ")");
                        c.setEndAnimationTrigger(null);
                        attackCounter[0] = 0; // Reset compteur
                        return true;
                    });
                }
            });
            aiTimer.start();
            // --- END SPIDERS SETUP ---
            
            // Logique de déplacement
            Runnable moveLogic = new Runnable() {
                @Override
                public void run() {
                    crusader.setPosition(playerPos[0], playerPos[1], 0);
                    status.setText("Position: (" + playerPos[0] + "," + playerPos[1] + ")");

                    // Vérification Math Game
                    // positions[0] contient la position de la réponse CORRECTE
                    if (mathPuzzle.areNumbersVisible()) {
                        if (playerPos[0] == mathPuzzle.getCorrectPos().x && playerPos[1] == mathPuzzle.getCorrectPos().y) {
                            status.setText("BRAVO ! ... aprés 10 secondes vous allez au niveau suivant");
                            
                            // Show result
                            lblEquation.setText(mathPuzzle.getCurrentA() + " + " + mathPuzzle.getCurrentB() + " = " + mathPuzzle.getCorrectValue());
                            lblFeedback.setText("Bravo ! Bonne réponse ! ");
                            
                            // Delay for 2 seconds
                            Timer nextLevelTimer = new Timer(1000, evt -> {
                                mathPuzzle.incrementLevel();
                                
                                // Update Map
                                levelMap.loadLevel(mathPuzzle.getLevel(), window);
                                walkable.clear();
                                walkable.addAll(levelMap.getWalkable());

                                // Respawn et niveau suivant
                                playerPos[0] = 0;
                                playerPos[1] = 0;
                                crusader.setPosition(0, 0, 0);
                                mathPuzzle.prepareNextLevel(window, levelMap.getAnswerPositions());
                                
                                // Update labels
                                lblQuestionTitle.setText("Question " + mathPuzzle.getLevel() + " :");
                                lblEquation.setText(mathPuzzle.getCurrentA() + " + " + mathPuzzle.getCurrentB() + " = ?");
                                lblFeedback.setText("");
                                
                                // Respawn des araignées pour le nouveau niveau
                                // Remove old spiders
                                for (Spider s : spiders) {
                                    window.remove(s);
                                }
                                spiders.clear();
                                spiderHealth.clear();
                                spiderPos.clear();

                                for (Point pos : levelMap.getSpiderSpawns()) {
                                    Spider s = new Spider();
                                    s.setScale(2.5f);
                                    s.setPosition(pos.x, pos.y, 0);
                                    window.add(s);
                                    spiders.add(s);
                                    spiderHealth.put(s, 5);
                                    spiderPos.put(s, new int[]{pos.x, pos.y});
                                }
                            });
                            nextLevelTimer.setRepeats(false);
                            nextLevelTimer.start();
                            
                        } else if (playerPos[0] == mathPuzzle.getWrongPos().x && playerPos[1] == mathPuzzle.getWrongPos().y) {
                            // positions[1] contient la position de la réponse FAUSSE
                            status.setText("Mauvaise réponse ! Ooops...");
                            crusader.setMode(Crusader.Mode.DEATH);
                            
                            crusader.setEndAnimationTrigger(c -> {
                                playerPos[0] = 0;
                                playerPos[1] = 0;
                                c.setPosition(0, 0, 0);
                                if (c instanceof Crusader) {
                                    ((Crusader) c).setMode(Crusader.Mode.IDLE);
                                }
                                status.setText("Position: (" + playerPos[0] + "," + playerPos[1] + ")");
                                c.setEndAnimationTrigger(null);
                                
                                // Respawn des araignées
                                // Remove old spiders
                                for (Spider s : spiders) {
                                    window.remove(s);
                                }
                                spiders.clear();
                                spiderHealth.clear();
                                spiderPos.clear();

                                for (Point pos : levelMap.getSpiderSpawns()) {
                                    Spider s = new Spider();
                                    s.setScale(2.5f);
                                    s.setPosition(pos.x, pos.y, 0);
                                    window.add(s);
                                    spiders.add(s);
                                    spiderHealth.put(s, 5);
                                    spiderPos.put(s, new int[]{pos.x, pos.y});
                                }
                                
                                // Hide numbers on respawn
                                mathPuzzle.hideNumbers(window);
                                
                                return true;
                            });
                        }
                    }
                }
            };

            // Gestionnaire de mouvement
            ActionListener moveAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Ne pas bouger si mort
                    if (crusader.getCurrentMode() == Crusader.Mode.DEATH) return;

                    if (crusader.getCurrentMode() != Crusader.Mode.WALK) {
                        crusader.setMode(Crusader.Mode.WALK);
                    }

                    String cmd = e.getActionCommand();
                    int dx = 0, dy = 0;
                    Direction dir = Direction.SOUTH;

                    if ("N".equals(cmd)) { dy = -1; dir = Direction.NORTH; }
                    else if ("S".equals(cmd)) { dy = 1; dir = Direction.SOUTH; }
                    else if ("W".equals(cmd)) { dx = -1; dir = Direction.WEST; }
                    else if ("E".equals(cmd)) { dx = 1; dir = Direction.EAST; }

                    // Turn before move logic
                    if (crusader.getDirection() != dir) {
                        crusader.setDirection(dir);
                        return;
                    }

                    int newX = playerPos[0] + dx;
                    int newY = playerPos[1] + dy;

                    // Vérification des limites
                    // -2,-3 à 16,7 (élargi pour inclure y=6)
                    if (newX >= -2 && newX <= 16 && newY >= -3 && newY <= 7) {
                        // Vérifier le "mask" : n'autoriser que les tuiles définies dans walkable
                        if (walkable.contains(new Point(newX, newY))) {
                            
                            // 1. Vérifier collision avec les araignées vivantes
                            boolean collision = false;
                            boolean spidersAlive = false;
                            for (Spider s : spiders) {
                                if (s.getCurrentMode() != Spider.Mode.DEATH) {
                                    spidersAlive = true;
                                    int[] sPos = spiderPos.get(s);
                                    // System.out.println("Spider at " + sPos[0] + "," + sPos[1] + " Player trying " + newX + "," + newY);
                                    if (sPos[0] == newX && sPos[1] == newY) {
                                        collision = true;
                                        break;
                                    }
                                }
                            }
                            if (collision) {
                                status.setText("Impossible de traverser une araignée !");
                                return;
                            }

                            // 2. Vérifier si on marche sur une réponse (positions[0] ou positions[1])
                            // Seulement si les nombres sont visibles
                            if (mathPuzzle.areNumbersVisible()) {
                                boolean isAnswerTile = (newX == mathPuzzle.getCorrectPos().x && newY == mathPuzzle.getCorrectPos().y) || 
                                                       (newX == mathPuzzle.getWrongPos().x && newY == mathPuzzle.getWrongPos().y);
                                
                                if (isAnswerTile) {
                                    // On peut marcher dessus pour valider
                                }
                            }

                            playerPos[0] = newX;
                            playerPos[1] = newY;
                            crusader.setDirection(dir);
                            moveLogic.run();
                        }
                    }
                }
            };

            // Clavier
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (crusader.getCurrentMode() == Crusader.Mode.DEATH) return;
                    
                    int code = e.getKeyCode();

                    // Attaque du joueur
                    if (code == KeyEvent.VK_SPACE) {
                        crusader.setMode(Crusader.Mode.ATTACK);
                        lastDefenseTime[0] = System.currentTimeMillis(); // Enregistrer le temps de défense
                        // Vérifier les dégâts aux araignées
                        for (Spider s : spiders) {
                            if (s.getCurrentMode() == Spider.Mode.DEATH) continue;
                            int[] sPos = spiderPos.get(s);
                            double dist = Math.sqrt(Math.pow(playerPos[0] - sPos[0], 2) + Math.pow(playerPos[1] - sPos[1], 2));
                            
                            if (dist <= 1.5) {
                                int hp = spiderHealth.get(s) - 1;
                                spiderHealth.put(s, hp);
                                System.out.println("Spider hit! HP: " + hp);
                                if (hp <= 0) {
                                    s.setMode(Spider.Mode.DEATH);
                                    
                                    // Check if all spiders are dead
                                    boolean allDead = true;
                                    for (Spider check : spiders) {
                                        if (check.getCurrentMode() != Spider.Mode.DEATH) {
                                            allDead = false;
                                            break;
                                        }
                                    }
                                    
                                    if (allDead) {
                                        mathPuzzle.showNumbers(window);
                                        status.setText("Voie libre ! Choisissez la bonne réponse.");
                                    }
                                }
                            }
                        }
                        return;
                    }

                    // Jump with S
                    if (code == KeyEvent.VK_S) {
                        if (crusader.getCurrentMode() == Crusader.Mode.JUMP || 
                            crusader.getCurrentMode() == Crusader.Mode.ATTACK ||
                            crusader.getCurrentMode() == Crusader.Mode.DEATH) return;

                        Direction dir = crusader.getDirection();
                        int dx = 0, dy = 0;
                        if (dir == Direction.NORTH) dy = -2;
                        else if (dir == Direction.SOUTH) dy = 2;
                        else if (dir == Direction.WEST) dx = -2;
                        else if (dir == Direction.EAST) dx = 2;

                        int targetX = playerPos[0] + dx;
                        int targetY = playerPos[1] + dy;

                        // Check bounds and walkable
                        if (targetX >= -2 && targetX <= 16 && targetY >= -3 && targetY <= 7) {
                             if (walkable.contains(new Point(targetX, targetY))) {
                                 
                                 // Check spiders at target
                                 boolean spiderAtTarget = false;
                                 for (Spider s : spiders) {
                                     if (s.getCurrentMode() != Spider.Mode.DEATH) {
                                         int[] sPos = spiderPos.get(s);
                                         if (sPos[0] == targetX && sPos[1] == targetY) {
                                             spiderAtTarget = true;
                                             break;
                                         }
                                     }
                                 }
                                 
                                 if (!spiderAtTarget) {
                                     crusader.setMode(Crusader.Mode.JUMP);
                                     
                                     // Move after animation
                                     crusader.setEndAnimationTrigger(c -> {
                                         playerPos[0] = targetX;
                                         playerPos[1] = targetY;
                                         c.setPosition(targetX, targetY, 0);
                                         if (c instanceof Crusader) {
                                             ((Crusader) c).setMode(Crusader.Mode.IDLE);
                                         }
                                         status.setText("Position: (" + playerPos[0] + "," + playerPos[1] + ")");
                                         c.setEndAnimationTrigger(null);
                                         moveLogic.run(); // Trigger logic (win/die check)
                                         return true;
                                     });
                                 }
                             }
                        }
                        return;
                    }

                    String cmd = null;
                    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) cmd = "N";
                    else if (code == KeyEvent.VK_DOWN) cmd = "S";
                    else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) cmd = "W";
                    else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) cmd = "E";

                    if (cmd != null) {
                        // Simuler l'action
                        moveAction.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, cmd));
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    int code = e.getKeyCode();
                    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W ||
                        code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S ||
                        code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A ||
                        code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                        
                        if (crusader.getCurrentMode() != Crusader.Mode.DEATH && crusader.getCurrentMode() != Crusader.Mode.ATTACK) {
                             crusader.setMode(Crusader.Mode.IDLE);
                        }
                    }
                }
            });
            
            // Focusable pour le clavier
            frame.setFocusable(true);
            frame.requestFocusInWindow();

            frame.revalidate();
            frame.repaint();
        });
    }
}
