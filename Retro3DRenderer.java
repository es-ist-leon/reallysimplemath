package rendering;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Retro3DRenderer - 8-bit Isometric 3D Rendering Engine
 * Erstellt einen coolen Retro-Look mit Pixelart und isometrischer Perspektive
 */
public class Retro3DRenderer {
    
    // 8-bit Farbpalette (NES-inspiriert)
    public static final Color[] PALETTE = {
        new Color(0, 0, 0),         // 0: Schwarz
        new Color(28, 28, 28),      // 1: Dunkelgrau
        new Color(85, 85, 85),      // 2: Grau
        new Color(170, 170, 170),   // 3: Hellgrau
        new Color(255, 255, 255),   // 4: Weiß
        new Color(0, 87, 132),      // 5: Dunkelblau
        new Color(0, 128, 255),     // 6: Blau
        new Color(85, 170, 255),    // 7: Hellblau
        new Color(0, 127, 14),      // 8: Dunkelgrün
        new Color(0, 200, 50),      // 9: Grün
        new Color(85, 255, 127),    // 10: Hellgrün
        new Color(132, 0, 0),       // 11: Dunkelrot
        new Color(230, 50, 50),     // 12: Rot
        new Color(255, 127, 127),   // 13: Hellrot
        new Color(127, 51, 0),      // 14: Braun
        new Color(255, 127, 0),     // 15: Orange
        new Color(255, 200, 50),    // 16: Gelb
        new Color(100, 0, 127),     // 17: Lila
        new Color(170, 85, 200),    // 18: Violett
        new Color(255, 170, 200),   // 19: Pink
        new Color(0, 100, 100),     // 20: Cyan dunkel
        new Color(0, 200, 200),     // 21: Cyan
    };
    
    private int pixelSize = 3; // Größe eines "Pixels" für Retro-Look
    private List<Voxel> voxels;
    private double rotationY = 0;
    private int width, height;
    
    // Isometrische Projektionswinkel
    private static final double ISO_ANGLE = Math.toRadians(30);
    
    public Retro3DRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.voxels = new ArrayList<>();
    }
    
    public void clear() {
        voxels.clear();
    }
    
    public void setRotation(double angle) {
        this.rotationY = angle;
    }
    
    /**
     * Fügt einen 3D-Block (Voxel) hinzu
     */
    public void addVoxel(double x, double y, double z, int size, int colorIndex) {
        voxels.add(new Voxel(x, y, z, size, PALETTE[colorIndex % PALETTE.length]));
    }
    
    public void addVoxel(double x, double y, double z, int size, Color color) {
        voxels.add(new Voxel(x, y, z, size, color));
    }
    
    /**
     * Rendert die 3D-Szene mit 8-bit Ästhetik
     */
    public void render(Graphics2D g2d) {
        // Pixelated Hintergrund
        renderBackground(g2d);
        
        // Sortiere Voxels für korrekte Tiefendarstellung (Painter's Algorithm)
        voxels.sort(Comparator.comparingDouble(v -> -v.z - v.y + v.x));
        
        // Render alle Voxels
        for (Voxel voxel : voxels) {
            renderVoxel(g2d, voxel);
        }
    }
    
    private void renderBackground(Graphics2D g2d) {
        // Gradient-Hintergrund im Retro-Stil
        for (int y = 0; y < height; y += pixelSize * 2) {
            int colorIndex = (y * 3 / height);
            Color bgColor = blendColors(new Color(20, 20, 40), new Color(40, 60, 100), (double) y / height);
            g2d.setColor(quantizeColor(bgColor));
            g2d.fillRect(0, y, width, pixelSize * 2);
        }
        
        // Grid-Linien für Retro-Feeling
        g2d.setColor(new Color(50, 50, 80, 100));
        for (int x = 0; x < width; x += 20) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += 20) {
            g2d.drawLine(0, y, width, y);
        }
    }
    
    /**
     * Rendert einen einzelnen Voxel (3D-Block) isometrisch
     */
    private void renderVoxel(Graphics2D g2d, Voxel voxel) {
        // Rotation anwenden
        double rotatedX = voxel.x * Math.cos(rotationY) - voxel.z * Math.sin(rotationY);
        double rotatedZ = voxel.x * Math.sin(rotationY) + voxel.z * Math.cos(rotationY);
        
        // Isometrische Projektion
        int screenX = (int) (width / 2 + (rotatedX - rotatedZ) * Math.cos(ISO_ANGLE) * 1.5);
        int screenY = (int) (height / 2 + (rotatedX + rotatedZ) * Math.sin(ISO_ANGLE) - voxel.y * 1.2);
        
        int size = voxel.size;
        
        // Berechne die Eckpunkte des isometrischen Würfels
        int[][] topFace = getIsometricTopFace(screenX, screenY, size);
        int[][] leftFace = getIsometricLeftFace(screenX, screenY, size);
        int[][] rightFace = getIsometricRightFace(screenX, screenY, size);
        
        // Render die drei sichtbaren Seiten mit Schattierung
        // Rechte Seite (dunkelste)
        g2d.setColor(darkenColor(voxel.color, 0.5));
        g2d.fillPolygon(rightFace[0], rightFace[1], 4);
        drawPixelatedOutline(g2d, rightFace[0], rightFace[1], 4);
        
        // Linke Seite (mittel)
        g2d.setColor(darkenColor(voxel.color, 0.7));
        g2d.fillPolygon(leftFace[0], leftFace[1], 4);
        drawPixelatedOutline(g2d, leftFace[0], leftFace[1], 4);
        
        // Oberseite (hellste)
        g2d.setColor(voxel.color);
        g2d.fillPolygon(topFace[0], topFace[1], 4);
        drawPixelatedOutline(g2d, topFace[0], topFace[1], 4);
        
        // Highlight auf der Oberseite
        if (voxel.highlight) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillPolygon(topFace[0], topFace[1], 4);
        }
    }
    
    private int[][] getIsometricTopFace(int x, int y, int size) {
        int halfSize = size / 2;
        int quarterSize = size / 4;
        
        int[] xPoints = {
            x,
            x + halfSize,
            x,
            x - halfSize
        };
        int[] yPoints = {
            y - quarterSize,
            y,
            y + quarterSize,
            y
        };
        return new int[][] {xPoints, yPoints};
    }
    
    private int[][] getIsometricLeftFace(int x, int y, int size) {
        int halfSize = size / 2;
        int quarterSize = size / 4;
        
        int[] xPoints = {
            x - halfSize,
            x,
            x,
            x - halfSize
        };
        int[] yPoints = {
            y,
            y + quarterSize,
            y + quarterSize + halfSize,
            y + halfSize
        };
        return new int[][] {xPoints, yPoints};
    }
    
    private int[][] getIsometricRightFace(int x, int y, int size) {
        int halfSize = size / 2;
        int quarterSize = size / 4;
        
        int[] xPoints = {
            x,
            x + halfSize,
            x + halfSize,
            x
        };
        int[] yPoints = {
            y + quarterSize,
            y,
            y + halfSize,
            y + quarterSize + halfSize
        };
        return new int[][] {xPoints, yPoints};
    }
    
    private void drawPixelatedOutline(Graphics2D g2d, int[] xPoints, int[] yPoints, int nPoints) {
        g2d.setColor(PALETTE[0]); // Schwarz
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, nPoints);
    }
    
    /**
     * Quantisiert eine Farbe auf die 8-bit Palette
     */
    public static Color quantizeColor(Color color) {
        Color closest = PALETTE[0];
        double minDistance = Double.MAX_VALUE;
        
        for (Color paletteColor : PALETTE) {
            double distance = colorDistance(color, paletteColor);
            if (distance < minDistance) {
                minDistance = distance;
                closest = paletteColor;
            }
        }
        return closest;
    }
    
    private static double colorDistance(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
    
    public static Color darkenColor(Color color, double factor) {
        return new Color(
            (int) (color.getRed() * factor),
            (int) (color.getGreen() * factor),
            (int) (color.getBlue() * factor)
        );
    }
    
    public static Color blendColors(Color c1, Color c2, double ratio) {
        return new Color(
            (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio),
            (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio),
            (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio)
        );
    }
    
    /**
     * Innere Klasse für 3D-Voxel
     */
    public static class Voxel {
        double x, y, z;
        int size;
        Color color;
        boolean highlight = false;
        double animationOffset = 0;
        
        public Voxel(double x, double y, double z, int size, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.color = color;
        }
        
        public void setHighlight(boolean highlight) {
            this.highlight = highlight;
        }
    }
}
