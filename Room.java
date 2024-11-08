import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Represents a room in a floor plan.
 * This class handles the drawing, selection, and manipulation of rooms.
 */
public class Room {
    // Room position and dimensions
    int x, y, width, height;

    // Selection state of the room
    boolean selected = false;

    // Constants for handle appearance and drawing
    private static final int HANDLE_SIZE = 10;
    private static final Color HANDLE_COLOR = new Color(41, 128, 185);
    private static final Color HANDLE_BORDER_COLOR = new Color(52, 152, 219);
    private static final int LINE_THICKNESS = 2;

    /**
     * Constructs a new Room with the given position and dimensions.
     *
     * @param x      The x-coordinate of the top-left corner
     * @param y      The y-coordinate of the top-left corner
     * @param width  The width of the room
     * @param height The height of the room
     */
    Room(int x, int y, int width, int height) {
        this.x = x;          // Set the x-coordinate of the room
        this.y = y;          // Set the y-coordinate of the room
        this.width = width;  // Set the width of the room
        this.height = height;// Set the height of the room
    }

    /**
     * Draws the room on the given Graphics2D context.
     * This includes the room boundary, resize handles, and dimensions.
     *
     * @param g2d The Graphics2D context to draw on
     */
    void draw(Graphics2D g2d) {
        // Store the original stroke to restore it later
        Stroke originalStroke = g2d.getStroke();
        // Set a new stroke with the defined line thickness
        g2d.setStroke(new BasicStroke(LINE_THICKNESS));

        // Draw room boundary
        g2d.setColor(selected ? Color.RED : Color.BLACK);  // Set color based on selection state
        g2d.drawRect(x, y, width, height);  // Draw the room rectangle

        // Draw resize handles
        drawResizeHandles(g2d);

        // Draw dimensions
        drawDimensions(g2d);

        // Restore the original stroke
        g2d.setStroke(originalStroke);
    }

    /**
     * Draws resize handles at the corners and midpoints of the room.
     *
     * @param g2d The Graphics2D context to draw on
     */
    private void drawResizeHandles(Graphics2D g2d) {
        // Enable anti-aliasing for smoother handle rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Define positions for all handles
        int[][] handlePositions = {
                {x, y}, {x + width, y}, {x, y + height}, {x + width, y + height},
                {x + width / 2, y}, {x + width / 2, y + height},
                {x, y + height / 2}, {x + width, y + height / 2}
        };

        // Draw each handle
        for (int[] pos : handlePositions) {
            // Create an ellipse shape for the handle
            Ellipse2D handle = new Ellipse2D.Double(pos[0] - HANDLE_SIZE / 2, pos[1] - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
            g2d.setColor(HANDLE_COLOR);  // Set the fill color for the handle
            g2d.fill(handle);  // Fill the handle
            g2d.setColor(HANDLE_BORDER_COLOR);  // Set the border color for the handle
            g2d.setStroke(new BasicStroke(2));  // Set the stroke for the handle border
            g2d.draw(handle);  // Draw the handle border
        }
    }

    /**
     * Draws the dimensions of the room.
     *
     * @param g2d The Graphics2D context to draw on
     */
    private void drawDimensions(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);  // Set color for dimension text
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));  // Set font for dimension text

        // Draw width dimensions at top and bottom
        g2d.drawString(width + " px", x + width / 2 - 20, y + 20);
        g2d.drawString(width + " px", x + width / 2 - 20, y + height - 10);

        // Draw height dimensions at left and right
        g2d.drawString(height + " px", x + 10, y + height / 2);
        g2d.drawString(height + " px", x + width - 40, y + height / 2);

        // Draw room size in the center
        g2d.drawString(width + " x " + height, x + width / 2 - 30, y + height / 2);
    }

    /**
     * Checks if a point is inside the room.
     *
     * @param px The x-coordinate of the point
     * @param py The y-coordinate of the point
     * @return true if the point is inside the room, false otherwise
     */
    boolean contains(int px, int py) {
        // Check if the point is within the room's boundaries
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    /**
     * Checks if a point is inside any of the room's resize handles.
     *
     * @param px The x-coordinate of the point
     * @param py The y-coordinate of the point
     * @return true if the point is inside a handle, false otherwise
     */
    boolean containsHandle(int px, int py) {
        // Define positions for all handles
        int[][] handlePositions = {
                {x, y}, {x + width, y}, {x, y + height}, {x + width, y + height},
                {x + width / 2, y}, {x + width / 2, y + height},
                {x, y + height / 2}, {x + width, y + height / 2}
        };

        // Check each handle
        for (int[] pos : handlePositions) {
            // Create an ellipse shape for the handle and check if it contains the point
            if (new Ellipse2D.Double(pos[0] - HANDLE_SIZE / 2, pos[1] - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE).contains(px, py)) {
                return true;  // Point is inside a handle
            }
        }
        return false;  // Point is not inside any handle
    }

    /**
     * Resizes the room to the given dimensions, ensuring a minimum size.
     *
     * @param newWidth  The new width of the room
     * @param newHeight The new height of the room
     */
    void resize(int newWidth, int newHeight) {
        this.width = Math.max(20, newWidth);   // Set new width, ensuring it's at least 20
        this.height = Math.max(20, newHeight); // Set new height, ensuring it's at least 20
    }

    /**
     * Moves the room to a new position.
     *
     * @param newX The new x-coordinate for the top-left corner
     * @param newY The new y-coordinate for the top-left corner
     */
    void moveTo(int newX, int newY) {
        this.x = newX;  // Set the new x-coordinate
        this.y = newY;  // Set the new y-coordinate
    }

    /**
     * Returns the bounding rectangle of the room.
     *
     * @return A Rectangle object representing the room's bounds
     */
    Rectangle getBounds() {
        return new Rectangle(x, y, width, height);  // Create and return a new Rectangle object with the room's dimensions
    }
}