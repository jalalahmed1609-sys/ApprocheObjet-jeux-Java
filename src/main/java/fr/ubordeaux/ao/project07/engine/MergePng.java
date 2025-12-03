package fr.ubordeaux.ao.project07.engine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MergePng {

    public static void main(String[] args) {

        // Arguments
        String folder = args.length >= 1 ? args[0] : "png";
        int targetWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 256;
        int targetHeight = args.length >= 3 ? Integer.parseInt(args[2]) : 512;
        int imagesPerRow = args.length >= 4 ? Integer.parseInt(args[3]) : 10;
        int linesPerAtlas = args.length >= 5 ? Integer.parseInt(args[4]) : 10;
        int offset = args.length >= 6 ? Integer.parseInt(args[5]) : 600;

        File dir = new File(folder);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Dossier '" + folder + "' introuvable !");
            System.exit(1);
        }

        // Lister et trier les fichiers PNG
        List<File> files = new ArrayList<>();
        for (File f : dir.listFiles((_, name) -> name.toLowerCase().endsWith(".png"))) {
            files.add(f);
        }
        files.sort(Comparator.comparing(File::getName));

        if (files.isEmpty()) {
            System.err.println("Aucun fichier PNG trouvé dans " + folder);
            return;
        }

        int maxImagesPerAtlas = imagesPerRow * linesPerAtlas;
        int atlasCount = (int) Math.ceil(files.size() / (double) maxImagesPerAtlas);

        System.out.println("Total fichiers : " + files.size());
        System.out.println("Nombre d'atlas générés : " + atlasCount);
        System.out.println("Cellule : " + targetWidth + "x" + targetHeight);
        System.out.println("Images par ligne : " + imagesPerRow + ", lignes par atlas : " + linesPerAtlas);

        for (int atlasIndex = 0; atlasIndex < atlasCount; atlasIndex++) {
            int start = atlasIndex * maxImagesPerAtlas;
            int end = Math.min(start + maxImagesPerAtlas, files.size());

            int canvasWidth = imagesPerRow * targetWidth;
            int canvasHeight = linesPerAtlas * targetHeight;

            BufferedImage atlas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = atlas.createGraphics();

            // Fond transparent
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, canvasWidth, canvasHeight);
            g2d.setComposite(AlphaComposite.SrcOver);

            int x = 0, y = 0;

            for (int i = start; i < end; i++) {
                File f = files.get(i);
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img == null)
                        continue;

                    // Calcul du scale pour rentrer dans la cellule
                    double scale = Math.min(
                            (double) targetWidth / img.getWidth(),
                            (double) targetHeight / img.getHeight());

                    int newWidth = (int) (img.getWidth() * scale);
                    int newHeight = (int) (img.getHeight() * scale);

                    // Centrer dans la cellule puis appliquer offset
                    int dstX = x + (targetWidth - newWidth) / 2;
                    int dstY = y + (targetHeight - newHeight) / 2;

                    g2d.drawImage(img, dstX, dstY, newWidth, newHeight, null);

                    x += targetWidth;
                    if (x >= imagesPerRow * targetWidth) {
                        x = 0;
                        y += targetHeight;
                    }

                } catch (IOException e) {
                    System.err.println("Erreur lecture " + f.getName() + " : " + e.getMessage());
                }
            }

            g2d.dispose();

            // Enregistrer le résultat
            try {
                File outFile = new File(String.format("all_%03d.png", offset + 100 * atlasIndex));
                ImageIO.write(atlas, "png", outFile);
                System.out.println("✅ Image générée : " + outFile.getName() +
                        " (" + canvasWidth + "x" + canvasHeight + ")");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("✔️  Fusion terminée !");
    }
}
