import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.ImageIcon;

public class Furniture implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int x, y, width, height; 
    int imgwidth, imgheight;
    private transient Image image;
    boolean lock = false;
    private boolean selected;
    private static final int HANDLE_SIZE = 8;
    private static final Color HANDLE_COLOR = new Color(41, 128, 185);
    private static final Color HANDLE_BORDER_COLOR = new Color(52, 152, 219);
    private int rotation = 0;

    public Furniture(String name, int x, int y, ImageIcon imageIcon) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.image = imageIcon.getImage();
        this.imgwidth = imageIcon.getIconWidth();
        this.imgheight = imageIcon.getIconHeight();
        this.width = imgwidth;
        this.height = imgheight;
        this.selected = false;
    }

    public Furniture(String name, ImageIcon imageIcon) {
        this(name, 0, 0, imageIcon);
    }

    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImage() { return image; }
    public boolean isSelected() { return selected; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public void move(int deltaX, int deltaY) {
        if(lock) return;
        this.x += deltaX;
        this.y += deltaY;
    }

    public void resize(int newWidth, int newHeight) {
        if(lock) return;
        this.width = newWidth;
        this.height = newHeight;
    }

    public void rotateFurniture(int bydegrees) {
        this.rotation += bydegrees;
        this.rotation %= 360;

        // swap width and height if rotation is 90 or 270 degrees
        if (rotation == 90 || rotation == 270) {
            int temp = width;
            width = height;
            height = temp;
        }

    }

    public int getRotation() {
        return rotation;
    }

    public void draw(Graphics2D g2d) {
        // Draw selection rectangle if selected
        if (selected && !lock) {
            // Draw white background
            g2d.setColor(new Color(255, 255, 255, 128)); // Semi-transparent white
            g2d.fillRect(x, y, width, height);
            
            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, width, height);

            // Draw resize handles
            drawResizeHandles(g2d);
        } else {
            // Draw border for unselected state
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(x, y, width, height);
        }

        // Draw the furniture image
        int imgX = x + (width - imgwidth) / 2;
        int imgY = y + (height - imgheight) / 2;
        g2d.drawImage(image, imgX, imgY, imgwidth, imgheight, null);
    }

    private void drawResizeHandles(Graphics2D g2d) {
        // Enable anti-aliasing for smoother handle rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Define positions for all handles
        int[][] handlePositions = {
            {x, y},                    // TOP_LEFT
            {x + width/2, y},          // TOP
            {x + width, y},            // TOP_RIGHT
            {x + width, y + height/2}, // RIGHT
            {x + width, y + height},   // BOTTOM_RIGHT
            {x + width/2, y + height}, // BOTTOM
            {x, y + height},           // BOTTOM_LEFT
            {x, y + height/2}          // LEFT
        };

        // Draw each handle
        for (int[] pos : handlePositions) {
            Ellipse2D handle = new Ellipse2D.Double(
                pos[0] - HANDLE_SIZE/2, 
                pos[1] - HANDLE_SIZE/2,
                HANDLE_SIZE,
                HANDLE_SIZE
            );
            g2d.setColor(HANDLE_COLOR);
            g2d.fill(handle);
            g2d.setColor(HANDLE_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(handle);
        }
    }

    boolean containsHandle(int px, int py) {
        // Define positions for all handles
        int[][] handlePositions = {
                {x, y}, {x + width, y}, {x, y + height}, {x + width, y + height},
                {x + width / 2, y}, {x + width / 2, y + height},
                {x, y + height / 2}, {x + width, y + height / 2}
        };

        // Check each handle
        for (int[] pos : handlePositions) {
            // Create an expanded area around each handle
            Ellipse2D handle = new Ellipse2D.Double(
                    pos[0] - HANDLE_SIZE, // Expand by HANDLE_SIZE
                    pos[1] - HANDLE_SIZE,
                    HANDLE_SIZE * 2,      // Double the size
                    HANDLE_SIZE * 2
            );
            if (handle.contains(px, py)) {
                return true; // Point is inside an expanded handle
            }
        }
        return false; // Point is not inside any handle
    }

    public String getResizeDirection(int px, int py) {
        int handleSize = HANDLE_SIZE;
        boolean onLeft = Math.abs(px - x) <= handleSize;
        boolean onRight = Math.abs(px - (x + width)) <= handleSize;
        boolean onTop = Math.abs(py - y) <= handleSize;
        boolean onBottom = Math.abs(py - (y + height)) <= handleSize;

        if (onTop && onLeft) return "TOP_LEFT";
        if (onTop && onRight) return "TOP_RIGHT";
        if (onBottom && onLeft) return "BOTTOM_LEFT";
        if (onBottom && onRight) return "BOTTOM_RIGHT";
        if (onTop) return "TOP";
        if (onBottom) return "BOTTOM";
        if (onLeft) return "LEFT";
        if (onRight) return "RIGHT";
        return "";
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(new ImageIcon(image));
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        ImageIcon imageIcon = (ImageIcon) ois.readObject();
        image = imageIcon.getImage();
    }

    public void lock() {
        lock = true;
    }

    public void unlock() {
        lock = false;
    }
}