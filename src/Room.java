import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Represents a room in a floor plan.
 * This class handles the drawing, selection, and manipulation of rooms.
 */
public class Room implements Serializable {
    int x, y, width, height;
    boolean selected = false;
    String name;
    String type;
    ArrayList<Furniture> furnitureList = new ArrayList<>();
    private static final int HANDLE_SIZE = 10;
    private static final Color HANDLE_COLOR = new Color(41, 128, 185);
    private static final Color HANDLE_BORDER_COLOR = new Color(52, 152, 219);
    private static final int LINE_THICKNESS = 4;
    boolean lock = false;
    private ArrayList<Fixtures> fixtureList = new ArrayList<>();
    //private ArrayList<Integer> doorPositions = new ArrayList<>();
    //private ArrayList<Integer> windowPositions = new ArrayList<>();
    public static final int DOOR_SIZE = 30; // Size of the door opening
    public static final int WINDOW_SIZE = 30; // Size of the window opening

    Room(int x, int y, int width, int height, String name,String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.type = type;
    }

    private void roomColor (String type,Graphics2D g2d){
        switch(type) {
            case "Drawing/Dining Room":
                final Color LIVING_ROOM_COLOR = new Color(251, 203, 121);
                g2d.setColor(LIVING_ROOM_COLOR);
                break;
            case "Bedroom":
                final Color BEDROOM_COLOR = new Color(173, 255, 180);
                g2d.setColor(BEDROOM_COLOR);
                break;
            case "Kitchen":
                final Color KITCHEN_COLOR = new Color(255, 137, 137, 255);
                g2d.setColor(KITCHEN_COLOR);
                break;
            case "Bathroom":
                final Color BATHROOM_COLOR = new Color(155, 234, 251);
                g2d.setColor(BATHROOM_COLOR);
                break;
        }
    }

    void drawRooms(Graphics2D g2d) {
        // Store the original stroke to restore it later
        Stroke originalStroke = g2d.getStroke();
        // Set a new stroke with the defined line thickness
        g2d.setStroke(new BasicStroke(LINE_THICKNESS));

        g2d.setColor(selected ? Color.RED:Color.BLACK);  // Set color based on selection state
        g2d.drawRect(x, y, width, height);  // Draw the room rectangle

        // Fill room
        roomColor(type,g2d);
        g2d.fillRect(x+LINE_THICKNESS/2,y+LINE_THICKNESS/2,width-LINE_THICKNESS,height-LINE_THICKNESS);

        // Draw resize handles
        if(selected){drawResizeHandles(g2d);}


        // Restore the original stroke
        g2d.setStroke(originalStroke);

        drawDimensions(g2d);

        drawFurniture(g2d);
        // Draw dimensions

    }

    private void drawResizeHandles(Graphics2D g2d) {
        if(!lock) {
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
    }

    private void drawDimensions(Graphics2D g2d) {
        double measure = FloorPlanPanel.scale;
        g2d.setColor(Color.BLUE);  // Set color for dimension text
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));  // Set font for dimension text
        FontMetrics fm = g2d.getFontMetrics();
        String nameDisplay;
        String dimDisplay = (int)(width/(measure*12)) + "'" + ((int)(width/measure))%12 + "'' x " + (int)(height/(measure*12)) + "'" + ((int)(height/measure))%12 + "''";
        String widthDisplay = (int)(width/(measure*12)) + "'" + ((int)(width/measure))%12 + "''";
        String heightDisplay = (int)(height/(measure*12)) + "'" + ((int)(height/measure))%12 + "''";
        int widthWidth = fm.stringWidth(widthDisplay);
        int heightWidth = fm.stringWidth(heightDisplay);
        int dimWidth = fm.stringWidth(dimDisplay);

        if(lock){
            nameDisplay = name + "(Locked)";
        }
        else{
            nameDisplay = name;
        }
        int nameWidth = fm.stringWidth(nameDisplay);
        // Draw width dimensions at top and bottom
        g2d.drawString(widthDisplay, x + width / 2 - widthWidth/2, y + 20);
        g2d.drawString(widthDisplay, x + width / 2 - widthWidth/2, y + height - 10);

        // Draw height dimensions at left and right
        g2d.drawString(heightDisplay, x + 10, y + height / 2);
        g2d.drawString(heightDisplay, x + width - 10 - heightWidth, y + height / 2);

         //Draw room name and size in the center
        int nameX = x + width/2 - nameWidth/2;
        int nameY = y + height/2 - 10;
        int dimX = x + width/2 - dimWidth/2;
        int dimY = y + height/2 + 10;

        g2d.drawString(nameDisplay,nameX,nameY);
        g2d.drawString(dimDisplay, dimX, dimY);
    }

    boolean contains(int px, int py) {
        // Check if the point is within the room's boundaries
        return px >= x && px <= x + width && py >= y && py <= y + height;
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
            // Create an ellipse shape for the handle and check if it contains the point
            if (new Ellipse2D.Double(pos[0] - (double) HANDLE_SIZE / 2, pos[1] - (double) HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE).contains(px, py)) {
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
    //has lock
    void resize(int newWidth, int newHeight) {
        if (!lock) {
            this.width = Math.max(20, newWidth);   // Set new width, ensuring it's at least 20
            this.height = Math.max(20, newHeight); // Set new height, ensuring it's at least 20
        }
    }

    /**
     * Moves the room to a new position.
     *
     * @param newX The new x-coordinate for the top-left corner
     * @param newY The new y-coordinate for the top-left corner
     */
    //has lock
    public void move(int newX, int newY) {
        int deltaX = newX - x;
        int deltaY = newY - y;
        if (!lock) {
            this.x += deltaX;
            this.y += deltaY;
            for (Furniture furniture : furnitureList) {
                furniture.move(deltaX, deltaY);
            }
        }
    }

    /**
     * Returns the bounding rectangle of the room.
     *
     * @return A Rectangle object representing the room's bounds
     */
    Rectangle getBounds() {
        return new Rectangle(x, y, width, height);  // Create and return a new Rectangle object with the room's dimensions
    }
    
    // Methods to manage furniture
    public void addFurniture(Furniture furniture) {
        furnitureList.add(furniture);
        if (furniture.getX() < x || furniture.getX() + furniture.getWidth() > x + width || furniture.getY() < y || furniture.getY() + furniture.getHeight() > y + height) {
            furniture.setX(x);
            furniture.setY(y);
        }
    }

    public void removeFurniture(Furniture furniture) {
        if (furnitureList.contains(furniture)) {
            furnitureList.remove(furniture);
        }
        else {
            JOptionPane.showMessageDialog(null, "Furniture not found in the list.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ArrayList<Furniture> getFurnitureList() {
        return furnitureList;
    }

    public void drawFurniture(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (Furniture furniture : furnitureList) {
            furniture.draw(g2d);
        }
    }


    //methods to manage doors and windows
    public void addDoor(int position,int size) {
        if (isValidOpening(position)) {
            //doorPositions.add(position);
            fixtureList.add(new Fixtures(position,"door",size));
        } else {
            JOptionPane.showMessageDialog(null, "Invalid door placement.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void addWindow(int position, int size) {
        if (isValidOpening(position)) {
            //windowPositions.add(position);
            fixtureList.add(new Fixtures(position,"window",size));
        } else {
            JOptionPane.showMessageDialog(null, "Invalid window placement.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /*
    public void removeDoor(int position) {
        if (doorPositions.contains(position)) {
            doorPositions.remove(Integer.valueOf(position));
        } else {
            JOptionPane.showMessageDialog(null, "Door not found in the list.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void removeWindow(int position) {
        if (windowPositions.contains(position)) {
            windowPositions.remove(Integer.valueOf(position));
        } else {
            JOptionPane.showMessageDialog(null, "Window not found in the list.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    */
//    public ArrayList<Integer> getDoorPositions() {
//        return doorPositions;
//    }
//
//    public ArrayList<Integer> getWindowPositions() {
//        return windowPositions;
//    }


    public ArrayList<Fixtures> getFixtureList() {
        return fixtureList;
    }

    public void drawFixtures(Graphics2D g2d) {

        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        for (Fixtures fixtures : fixtureList) {
            if (Objects.equals(fixtures.type, "door")) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(LINE_THICKNESS));
            } else if (Objects.equals(fixtures.type, "window")) {
//                g2d.setColor(Color.WHITE);
//                g2d.setStroke(new BasicStroke(LINE_THICKNESS));
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9, 5}, 0));
            }
            if (fixtures.position < width) {
                // Top wall
                g2d.drawLine(x + fixtures.position, y, x + fixtures.position + fixtures.size, y);
            } else if (fixtures.position < width + height) {
                // Right wall
                g2d.drawLine(x + width, y + fixtures.position - width, x + width, y + fixtures.position - width + fixtures.size);
            } else if (fixtures.position < 2 * width + height) {
                // Bottom wall
                g2d.drawLine(x + fixtures.position - width - height, y + height, x + fixtures.position - width - height + fixtures.size, y + height);
            } else {
                // Left wall
                g2d.drawLine(x, y + fixtures.position - 2 * width - height, x, y + fixtures.position - 2 * width - height + fixtures.size);
            }
        }

        // Restore original settings
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
}

    
    private boolean isValidOpening(int position) {
        for (Fixtures existingFixture : fixtureList) {
            if (Math.abs(existingFixture.position - position) < existingFixture.size) {
                return false;
            }
        }
        return true;
    }
    
    /*
    private void drawWallSegment(Graphics2D g2d, int startX, int startY, int width, int height, ArrayList<Integer> doorPositions, boolean isHorizontal) {
        int endX = startX + width;
        int endY = startY + height;
    
        for (int position : doorPositions) {
            if (isHorizontal && position >= startX && position <= endX) {
                g2d.drawLine(startX, startY, position, startY);
                startX = position + DOOR_SIZE;
            } else if (!isHorizontal && position >= startY && position <= endY) {
                g2d.drawLine(startX, startY, startX, position);
                startY = position + DOOR_SIZE;
            }
        }
    
        g2d.drawLine(startX, startY, endX, endY);
    }
    */

    public void clearFixtures() {
        fixtureList.clear();
    }

    public void lockRoom(){
        lock = true;
        for (Furniture furniture : furnitureList) {
            furniture.lock();
        }

    }

    public void unlockRoom(){
        lock = false;
        clearFixtures();
        for (Furniture furniture : furnitureList) {
            furniture.unlock();
        }
    }

    public void toggleRoomLock(){
        if (lock){
            unlockRoom();
        }
        else{
            lockRoom();
        }
    }

    public boolean doesNotHaveFixtures(){
  //      return doorPositions.isEmpty() && windowPositions.isEmpty();
        return fixtureList.isEmpty();
    }
}