package fr.ubordeaux.ao.project07;

import fr.ubordeaux.ao.project07.engine.IWindowGame;
import javax.swing.JLabel;
import java.awt.Point;
import java.awt.Color;

import java.util.List;

public class MathPuzzle {
    private int level;
    private int correctValue;
    private int wrongValue;
    private Point correctPos;
    private Point wrongPos;
    private int baseImageId;
    private int currentA;
    private int currentB;

    private boolean numbersVisible = false;

    public MathPuzzle(int baseImageId) {
        this.level = 1;
        this.baseImageId = baseImageId;
        this.correctValue = -1;
        this.wrongValue = -1;
        this.numbersVisible = false;
    }

    public void prepareNextLevel(IWindowGame window, List<Point> answerPositions) {
        // Remove old numbers if they exist
        hideNumbers(window);

        // Generate new numbers
        this.currentA = (int)(Math.random() * 5); // 0-4
        this.currentB = (int)(Math.random() * 5); // 0-4
        int res = currentA + currentB;
        
        int wrong;
        do {
            wrong = (int)(Math.random() * 10); // 0-9
        } while (wrong == res);

        this.correctValue = res;
        this.wrongValue = wrong;

        // Set positions from the list
        if (answerPositions != null && answerPositions.size() >= 2) {
            Point p1 = answerPositions.get(0);
            Point p2 = answerPositions.get(1);
            
            if (Math.random() > 0.5) {
                this.correctPos = p1;
                this.wrongPos = p2;
            } else {
                this.correctPos = p2;
                this.wrongPos = p1;
            }
        } else {
            // Fallback if no positions defined
            this.correctPos = new Point(14, 0);
            this.wrongPos = new Point(14, 6);
        }
        
        // Do NOT add to window yet
        this.numbersVisible = false;
    }

    public void showNumbers(IWindowGame window) {
        if (!numbersVisible && correctValue != -1) {
            window.add(baseImageId + correctValue, correctPos.x, correctPos.y, 0);
            window.add(baseImageId + wrongValue, wrongPos.x, wrongPos.y, 0);
            numbersVisible = true;
        }
    }

    public void hideNumbers(IWindowGame window) {
        if (correctValue != -1) {
            window.remove(baseImageId + correctValue, correctPos.x, correctPos.y, 0);
            window.remove(baseImageId + wrongValue, wrongPos.x, wrongPos.y, 0);
        }
        numbersVisible = false;
    }

    public boolean areNumbersVisible() {
        return numbersVisible;
    }

    public int getCurrentA() {
        return currentA;
    }

    public int getCurrentB() {
        return currentB;
    }

    public void incrementLevel() {
        this.level++;
    }

    public Point getCorrectPos() {
        return correctPos;
    }

    public Point getWrongPos() {
        return wrongPos;
    }
    
    public int getLevel() {
        return level;
    }

    public int getCorrectValue() {
        return correctValue;
    }
}
