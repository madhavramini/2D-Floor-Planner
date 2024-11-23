import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.*;

public class FloorPlanPanel extends JPanel implements MouseListener, MouseMotionListener {
    private int mouseX, mouseY;
    public ArrayList<Room> rooms = new ArrayList<>();
    private Room selectedRoom = null;
    private Furniture selectedFurniture = null;
    private Point mouseOffset;
    private Point initialPoint;
    private final int snapDistance = 10;
    private boolean resizing = false;
    private String resizeDirection = "";
    private ArrayList<Line2D> projectionLines = new ArrayList<>();
    private JPopupMenu roomMenu, furnitureMenu;
    private MainWindow window;
    private Room roomToPlace = null;
    public static double scale = 2;
    public int totalArea;

    public FloorPlanPanel(MainWindow window) {
        addMouseListener(this);
        addMouseMotionListener(this);
        this.window = window;
        setLayout(null); // Use absolute positioning
    }

    private void initializeRoomMenu() {
        roomMenu = new JPopupMenu();
        
        // Delete
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            if (selectedRoom != null) {
                rooms.remove(selectedRoom);
                selectedRoom = null;
                repaint();
                window.updateRoomTable();
                updateTotalArea();
                window.updateTotalAreaLabel();
            } else if(selectedFurniture != null){
                selectedRoom.furnitureList.remove(selectedFurniture);
            }
        });

        // Duplicate
        JMenuItem duplicateItem = new JMenuItem("Duplicate");
        duplicateItem.addActionListener(e -> {
            if (selectedRoom != null) {
                ArrayList<Furniture> furnitureListCopy = new ArrayList<>();
                for (Furniture furniture : selectedRoom.getFurnitureList()) {
                    Furniture newFurniture = new Furniture(furniture.getName(), furniture.getX() - selectedRoom.x, furniture.getY() - selectedRoom.y, new ImageIcon(furniture.getImage()));
                    newFurniture.setWidth(furniture.getWidth());
                    newFurniture.setHeight(furniture.getHeight());
                    furnitureListCopy.add(newFurniture);
                }
                window.addNewRoom(selectedRoom.name,selectedRoom.type, selectedRoom.width, selectedRoom.height, furnitureListCopy);
                selectedRoom = null;
                repaint();
                window.updateRoomTable();
            }
        });

        // Add Furniture
        JMenuItem addFurniture = new JMenuItem("Add Furniture");
        addFurniture.addActionListener(e -> {
            if (selectedRoom != null) {
                Furniture selectedFurniture = new FurnitureCatalogPanel().showFurnitureCatalog();
                if (selectedFurniture != null) {
                    addFurnitureToRoom(selectedRoom, selectedFurniture);
                }
                else {
                    JOptionPane.showMessageDialog(null, "No furniture item was selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add Door
        JMenuItem addDoor = new JMenuItem("Add Door");
        addDoor.addActionListener(e -> {
            if(selectedRoom != null){
                addDoor(selectedRoom);
            }
        });

        // Add Window
        JMenuItem addWindow = new JMenuItem("Add Window");
        addWindow.addActionListener(e -> {
            if(selectedRoom != null){
                addWindow(selectedRoom);
            }
        });

        JMenuItem clearFixtures = new JMenuItem("Clear all Fixtures");
        clearFixtures.addActionListener(e -> {
            if (selectedRoom != null) {
                selectedRoom.clearFixtures();
                repaint();
            }
        });

        // Rotate options
        JMenu rotateMenu = new JMenu("Rotate");

        JMenuItem rotate90 = new JMenuItem("Rotate Right");
        rotate90.addActionListener(e -> {
            if (selectedRoom != null) {
                rotateRoom(selectedRoom, 90);
                repaint();
            }
        });

        JMenuItem rotate270 = new JMenuItem("Rotate 90° Counter-clockwise");
        rotate270.addActionListener(e -> {
            if (selectedRoom != null) {
                rotateRoom(selectedRoom, 270);
                repaint();
            }
        });

        rotateMenu.add(rotate90);
        rotateMenu.add(rotate270);
        roomMenu.add(rotateMenu);
        roomMenu.add(addFurniture);
        roomMenu.add(duplicateItem);
        roomMenu.add(addDoor);
        roomMenu.add(addWindow);
        roomMenu.add(clearFixtures);
        roomMenu.add(deleteItem);
        if(window.globalLock){
            addFurniture.setEnabled(false);
            addDoor.setEnabled(true);
            addWindow.setEnabled(true);
            clearFixtures.setEnabled(true);
            deleteItem.setEnabled(false);
            duplicateItem.setEnabled(false);
            rotateMenu.setEnabled(false);
        } else {
            addFurniture.setEnabled(true);
            addDoor.setEnabled(false);
            addWindow.setEnabled(false);
            clearFixtures.setEnabled(true);
            deleteItem.setEnabled(true);
            duplicateItem.setEnabled(true);
            rotateMenu.setEnabled(true);
        }
    }

    private void initializeFurnitureMenu(){
        furnitureMenu = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            if (selectedFurniture != null) {
                selectedRoom.furnitureList.remove(selectedFurniture);
                selectedFurniture = null;
                repaint();
            }
        });

        JMenuItem rotate90 = new JMenuItem("Rotate Right");
        rotate90.addActionListener(e -> {
            if (selectedFurniture != null) {
                selectedFurniture.rotateFurniture(90);
                repaint();
            }
        });

        JMenuItem rotate270 = new JMenuItem("Rotate Left");
        rotate270.addActionListener(e -> {
            if (selectedFurniture != null) {
                selectedFurniture.rotateFurniture(270);
                repaint();
            }
        });

        furnitureMenu.add(rotate90);
        furnitureMenu.add(rotate270);
        furnitureMenu.add(deleteItem);
        if(window.globalLock){
            deleteItem.setEnabled(false);
            rotate90.setEnabled(false);
            rotate270.setEnabled(false);
        } else {
            deleteItem.setEnabled(true);
            rotate90.setEnabled(true);
            rotate270.setEnabled(true);
        }
    }

    private void rotateRoom(Room room, int degrees) {
        if (room.lock) {
            JOptionPane.showMessageDialog(null, "Room must be unlocked to rotate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int originalWidth = room.width;
        int originalHeight = room.height;

        // Swap width and height if rotating by 90 or 270 degrees
        if (degrees == 90 || degrees == 270) {
            room.width = originalHeight;
            room.height = originalWidth;
        }

        // Keep the room centered during the rotation
        int centerX = room.x + originalWidth / 2;
        int centerY = room.y + originalHeight / 2;
        room.x = centerX - room.width / 2;
        room.y = centerY - room.height / 2;

        // check overlap and revert if any
        if (!resolveOverlap(room)) {
            room.width = originalWidth;
            room.height = originalHeight;
            room.x = centerX - room.width / 2;
            room.y = centerY - room.height / 2;
            JOptionPane.showMessageDialog(null, "Room cannot be rotated due to overlap.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure room stays within bounds
        constrainRoomToBounds(room);

        // Rotate the furniture within the room
        rotateFurniture(room, degrees);
    }

    private void rotateFurniture(Room room, int degrees) {
        double angle = Math.toRadians(degrees);
        int roomCenterX = room.x + room.width / 2;
        int roomCenterY = room.y + room.height / 2;

        for (Furniture furniture : room.getFurnitureList()) {
            // Calculate furniture's center relative to the room's center
            int furnitureCenterX = furniture.getX() + furniture.getWidth() / 2;
            int furnitureCenterY = furniture.getY() + furniture.getHeight() / 2;

            // Rotate furniture's position
            int offsetX = furnitureCenterX - roomCenterX;
            int offsetY = furnitureCenterY - roomCenterY;

            int rotatedX = (int) (roomCenterX + offsetX * Math.cos(angle) - offsetY * Math.sin(angle));
            int rotatedY = (int) (roomCenterY + offsetX * Math.sin(angle) + offsetY * Math.cos(angle));

            // Update furniture position and swap dimensions for 90°/270° rotations
            furniture.setX(rotatedX - furniture.getWidth() / 2);
            furniture.setY(rotatedY - furniture.getHeight() / 2);
            if (degrees == 90 || degrees == 270) {
                int temp = furniture.getWidth();
                furniture.setWidth(furniture.getHeight());
                furniture.setHeight(temp);
            }

            // Ensure furniture stays within room bounds
            int minX = room.x;
            int minY = room.y;
            int maxX = room.x + room.width - furniture.getWidth();
            int maxY = room.y + room.height - furniture.getHeight();

            furniture.setX(Math.max(minX, Math.min(furniture.getX(), maxX)));
            furniture.setY(Math.max(minY, Math.min(furniture.getY(), maxY)));
        }
    }

    private boolean resolveOverlap(Room room) {
        int maxAttempts = 50; // Prevent infinite loops
        int attempts = 0;
        int pushDistance = 10; // Initial push distance in pixels

        while (attempts < maxAttempts) {
            boolean hasOverlap = false;

            for (Room otherRoom : rooms) {
                if (otherRoom == room) continue;

                Rectangle roomBounds = new Rectangle(room.x, room.y, room.width, room.height);
                Rectangle otherBounds = new Rectangle(otherRoom.x, otherRoom.y, otherRoom.width, otherRoom.height);

                if (roomBounds.intersects(otherBounds)) {
                    hasOverlap = true;

                    // Calculate overlap areas in different directions
                    int leftPush = (otherRoom.x + otherRoom.width) - room.x;
                    int rightPush = (room.x + room.width) - otherRoom.x;
                    int upPush = (otherRoom.y + otherRoom.height) - room.y;
                    int downPush = (room.y + room.height) - otherRoom.y;

                    // Find the smallest push needed
                    int minPush = Math.min(Math.min(leftPush, rightPush), Math.min(upPush, downPush));

                    // Apply the push in the appropriate direction
                    if (minPush == leftPush) {
                        room.x += pushDistance;
                    } else if (minPush == rightPush) {
                        room.x -= pushDistance;
                    } else if (minPush == upPush) {
                        room.y += pushDistance;
                    } else if (minPush == downPush) {
                        room.y -= pushDistance;
                    }

                    // Ensure room stays within panel bounds
                    constrainRoomToBounds(room);
                }
            }

            if (!hasOverlap) {
                return true; // Successfully resolved all overlaps
            }

            attempts++;
            // Increase push distance slightly with each attempt
            if (attempts % 5 == 0) {
                pushDistance += 5;
            }
        }

        return false; // Could not resolve overlaps within max attempts
    }

    private void constrainRoomToBounds(Room room) {
        room.x = Math.max(0, Math.min(room.x, getWidth() - room.width));
        room.y = Math.max(0, Math.min(room.y, getHeight() - room.height));
    }

    // methods to handle fixtures:
    private int[] showAddFixtureDialog() {
        if (selectedRoom == null) {
            return null;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel wallLabel = new JLabel("Wall:");
        String[] walls = {"Top", "Right", "Bottom", "Left"};
        JComboBox<String> wallComboBox = new JComboBox<>(walls);

        // Sliders for position and size
        JLabel startPointLabel = new JLabel("Start Point:");
        JLabel endPointLabel = new JLabel("End Point:");

        // Update slider limits based on selected wall
        wallComboBox.addActionListener(e -> {
            String selectedWall = (String) wallComboBox.getSelectedItem();
            int wallLimit = getWallLimit(selectedWall, selectedRoom);

            JSlider startPointSlider = new JSlider(0, wallLimit, 0);
            JSlider endPointSlider = new JSlider(0, wallLimit, wallLimit);

            startPointSlider.setMajorTickSpacing(wallLimit / 4);
            startPointSlider.setPaintTicks(true);
            Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
            for (int i = 0; i <= 4; i ++) {
                int feet = (int)(((double) (wallLimit * i) /4)/(scale*12)); // Convert inches to feet
                int inches = ((int)(((double) (wallLimit * i) /4)/scale)) % 12; // Remaining inches
                if (feet > 0) {
                    labelTable.put(i*wallLimit/4, new JLabel(feet + "'" + inches + "''"));
                } else {
                    labelTable.put(i*wallLimit/4, new JLabel(inches + "''"));
                }
            }
            startPointSlider.setLabelTable(labelTable);
            startPointSlider.setPaintLabels(true);

            endPointSlider.setMajorTickSpacing(wallLimit / 4);
            endPointSlider.setPaintTicks(true);
            endPointSlider.setLabelTable(labelTable);
            endPointSlider.setPaintLabels(true);




            // Ensure end point is always greater than start point
            startPointSlider.addChangeListener(sl -> {
                if (endPointSlider.getValue() <= startPointSlider.getValue()) {
                    endPointSlider.setValue(startPointSlider.getValue() + 1);
                }
            });

            endPointSlider.addChangeListener(sl -> {
                if (startPointSlider.getValue() >= endPointSlider.getValue()) {
                    startPointSlider.setValue(endPointSlider.getValue() - 1);
                }
            });

            panel.removeAll(); // Clear existing components
            panel.add(wallLabel);
            panel.add(wallComboBox);
            panel.add(startPointLabel);
            panel.add(startPointSlider);
            panel.add(endPointLabel);
            panel.add(endPointSlider);


            panel.revalidate();
            panel.repaint();
        });

        // Trigger initial setup
        wallComboBox.setSelectedIndex(0);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Fixture", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            JSlider startPointSlider = (JSlider) panel.getComponents()[3];
            JSlider endPointSlider = (JSlider) panel.getComponents()[5];

            String selectedWall = (String) wallComboBox.getSelectedItem();
            int position = startPointSlider.getValue();
            int size = endPointSlider.getValue() - startPointSlider.getValue();

            return new int[]{wallsToPosition(selectedWall), position, size};
        }

        return null;
    }

    private int getWallLimit(String wall, Room room) {
        return switch (wall) {
            case "Top", "Bottom" -> room.width;
            case "Right", "Left" -> room.height;
            default -> -1;
        };
    }

    private int wallsToPosition(String wall) {
        return switch (wall) {
            case "Top" -> 0;
            case "Right" -> 1;
            case "Bottom" -> 2;
            case "Left" -> 3;
            default -> -1;
        };
    }

    private int getWallPosition(int wall, int position, Room room) {
        switch (wall) {
            case 0: // Top wall
                return position;
            case 1: // Right wall
                return room.width + position;
            case 2: // Bottom wall
                return room.width + room.height + position;
            case 3: // Left wall
                return 2 * room.width + room.height + position;
            default:
                return -1;
        }
    }

    public void addDoor(Room selectedRoom){
        if(!selectedRoom.lock) {
            JOptionPane.showMessageDialog(null, "Room must be locked to add a door.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] result = showAddFixtureDialog();
        if (result != null) {
            int wall = result[0];
            int position = result[1];
            int wallPosition = getWallPosition(wall, position, selectedRoom);
            int size = result[2];
            if(selectedRoom.type.equals("Bedroom") || selectedRoom.type.equals("Bathroom")){
                if(isFixtureFacingOut(selectedRoom, wall, position , size)) {
                    JOptionPane.showMessageDialog(null, "Doors cannot face outwards for bedrooms and bathrooms", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            selectedRoom.addDoor(wallPosition,size);
            repaint();
        }
    }

    public void addWindow(Room selectedRoom){
        if(!selectedRoom.lock) {
            JOptionPane.showMessageDialog(null, "Room must be locked to add a window.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] result = showAddFixtureDialog();
        if (result != null) {
            int wall = result[0];
            int position = result[1];
            int wallPosition = getWallPosition(wall, position, selectedRoom);
            int size = result[2];
            if(!isFixtureFacingOut(selectedRoom, wall, position , size)){
                JOptionPane.showMessageDialog(null, "Window must face outwards.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedRoom.addWindow(wallPosition,size);
            repaint();
        }
    }

    public boolean isFixtureFacingOut(Room room, int wallPosition, int position , int size) {
        // find the point just in front of window outside the room
        int x = room.x;
        int y = room.y;
        int x1,y1;
        switch (wallPosition) {
            case 0: //Top
                x += position;
                y -= 1;
                x1 = x+size;
                y1 = y;
                break;
            case 1: //Right
                x += room.width + 1;
                y += position;
                x1 = x;
                y1 = y + size;
                break;
            case 2: //Bottom
                x += position;
                y += room.height + 1;
                x1 = x - size;
                y1 = y;
                break;
            case 3: //Left
                x -= 1;
                y += position;
                x1 = x;
                y1 = y - size;
                break;
            default:
                x1 = x;
                y1 = y;
        }



        // check if the point is inside another room
        if(findRoomContainingPoint(x, y) != null || findRoomContainingPoint(x1,y1) != null) return false;

        return true;
    }

    // add furniture to room
    public void addFurnitureToRoom(Room room, Furniture furniture) {
        room.addFurniture(furniture);
        repaint();
    }

    private void clearAllSelections() {
        // Clear room selections
        for (Room room : rooms) {
            room.selected = false;
            // Clear furniture selections within each room
            for (Furniture furniture : room.getFurnitureList()) {
                furniture.setSelected(false);
            }
        }
        selectedRoom = null;
        selectedFurniture = null;
    }

    private Room findRoomContainingPoint(int x, int y) {
        // Check rooms in reverse order (top-most first)
        for (int i = rooms.size() - 1; i >= 0; i--) {
            Room room = rooms.get(i);
            if(room.containsHandle(x,y)){
                return room;
            }
            if (room.contains(x, y)) {
                return room;
            }
        }
        return null;
    }

    private Furniture findFurnitureAtPoint(int x, int y) {
        for (Room room : rooms) {
            for (Furniture furniture : room.getFurnitureList()) {
                if(furniture.containsHandle(x,y)){
                    return furniture;
                }
                if (x >= furniture.getX() && x <= furniture.getX() + furniture.getWidth() &&
                    y >= furniture.getY() && y <= furniture.getY() + furniture.getHeight()) {
                    return furniture;
                }
            }
        }
        return null;
    }

    private Room findRoomContainingFurniture(Furniture furniture) {
        for (Room room : rooms) {
            if (room.getFurnitureList().contains(furniture)) {
                return room;
            }
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        if (roomToPlace != null) {
            roomToPlace.x = e.getX();
            roomToPlace.y = e.getY();

            if (isOverlapping(roomToPlace)) {
                JOptionPane.showMessageDialog(this, "Room overlaps with an existing room!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                rooms.add(roomToPlace);
                roomToPlace = null; // Placement done
                repaint();
                window.updateRoomTable();
                updateTotalArea();
                window.updateTotalAreaLabel();
                window.addRoom.setVisible(false);
            }
        }
        else {
            if (e.isPopupTrigger()) {
                handleRoomMenu(e);
                return;
            }

            // First, try to find furniture at click point
            Furniture clickedFurniture = findFurnitureAtPoint(mouseX, mouseY);
            Room clickedRoom = findRoomContainingPoint(mouseX, mouseY);

            // Clear all current selections first
            clearAllSelections();

            if (clickedFurniture != null) {
                // Handle furniture selection
                selectedFurniture = clickedFurniture;
                clickedFurniture.setSelected(true);

                // Check if clicking on a resize handle
                if (clickedFurniture.containsHandle(mouseX, mouseY)) {
                    resizing = true;
                    initialPoint = e.getPoint();
                    resizeDirection = clickedFurniture.getResizeDirection(mouseX, mouseY);
                } else {
                    // Normal selection for moving
                    mouseOffset = new Point(mouseX - clickedFurniture.getX(),
                            mouseY - clickedFurniture.getY());
                }

                // Also select the containing room
                Room containingRoom = findRoomContainingFurniture(clickedFurniture);
                if (containingRoom != null) {
                    selectedRoom = containingRoom;
                    containingRoom.selected = true;
                }
            } else if (clickedRoom != null) {
                // Handle room selection and resize handles
                if (clickedRoom.containsHandle(mouseX, mouseY) && !clickedRoom.lock) {
                    resizing = true;
                    selectedRoom = clickedRoom;
                    clickedRoom.selected = true;
                    initialPoint = e.getPoint();
                    resizeDirection = getResizeDirection(clickedRoom, mouseX, mouseY);
                } else if (!clickedRoom.lock) {
                    selectedRoom = clickedRoom;
                    clickedRoom.selected = true;
                    mouseOffset = new Point(mouseX - clickedRoom.x, mouseY - clickedRoom.y);
                }
            }

            repaint();

        }
    }

    public void setRoomToPlace(Room room) {
        this.roomToPlace = room;
        repaint();
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            handleRoomMenu(e);
        } else {
            resizing = false;
            projectionLines.clear();
        }
        repaint();
    }

    private void handleRoomMenu(MouseEvent e) {
        Furniture clickedFurniture = findFurnitureAtPoint(e.getX(), e.getY());
        if (clickedFurniture != null) {
            handleFurnitureMenu(selectedFurniture, e);
            return;
        }
        Room clickedRoom = findRoomContainingPoint(e.getX(), e.getY());
        if (clickedRoom != null) {
            clearAllSelections();
            selectedRoom = clickedRoom;
            clickedRoom.selected = true;
            repaint();
            initializeRoomMenu();
            roomMenu.show(this, e.getX(), e.getY());
        }
    }

    private void handleFurnitureMenu(Furniture clickedFurniture, MouseEvent e) {
        clickedFurniture.setSelected(true);
        initializeFurnitureMenu();
        furnitureMenu.show(this, e.getX(), e.getY());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (Room room : rooms) {
            room.drawRooms(g2d);
        }

        for (Room room : rooms){
            room.drawFixtures(g2d);
        }

        // Draw projection lines
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        for (Line2D line : projectionLines) {
            g2d.draw(line);
        }
    }


    private String getResizeDirection(Room room, int mouseX, int mouseY) {
        int handleSize = 8; // Same as HANDLE_SIZE in Room class
        boolean onLeft = Math.abs(mouseX - room.x) <= handleSize;
        boolean onRight = Math.abs(mouseX - (room.x + room.width)) <= handleSize;
        boolean onTop = Math.abs(mouseY - room.y) <= handleSize;
        boolean onBottom = Math.abs(mouseY - (room.y + room.height)) <= handleSize;

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

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isPopupTrigger()) {
            handleRoomMenu(e);
            return;
        }
        if (selectedRoom != null || selectedFurniture != null) {
            if (resizing) {
                handleResizing(e);
            } else {
                handleMoving(e);
            }
        }
        repaint();
    }

    private void handleResizing(MouseEvent e) {
        if (window.globalLock) return;
        if (selectedFurniture != null) {
            handleFurnitureResizing(e);
        } else if (selectedRoom != null) {
            handleRoomResizing(e);
        }
        repaint();
    }

    private void handleFurnitureResizing(MouseEvent e) {
        if (selectedFurniture.lock) return;

        int dx = e.getX() - initialPoint.x;
        int dy = e.getY() - initialPoint.y;

        // Find the containing room
        Room containingRoom = null;
        for (Room room : rooms) {
            if (room.getFurnitureList().contains(selectedFurniture)) {
                containingRoom = room;
                break;
            }
        }

        if (containingRoom == null) return;

        // Calculate new dimensions based on resize direction
        int newX = selectedFurniture.getX();
        int newY = selectedFurniture.getY();
        int newWidth = selectedFurniture.getWidth();
        int newHeight = selectedFurniture.getHeight();

        switch (resizeDirection) {
            case "TOP_LEFT":
                newX += dx;
                newY += dy;
                newWidth -= dx;
                newHeight -= dy;
                break;
            case "TOP":
                newY += dy;
                newHeight -= dy;
                break;
            case "TOP_RIGHT":
                newY += dy;
                newWidth += dx;
                newHeight -= dy;
                break;
            case "RIGHT":
                newWidth += dx;
                break;
            case "BOTTOM_RIGHT":
                newWidth += dx;
                newHeight += dy;
                break;
            case "BOTTOM":
                newHeight += dy;
                break;
            case "BOTTOM_LEFT":
                newX += dx;
                newWidth -= dx;
                newHeight += dy;
                break;
            case "LEFT":
                newX += dx;
                newWidth -= dx;
                break;
        }

        // Ensure minimum size
        int minSize = Math.max(selectedFurniture.imgwidth, selectedFurniture.imgheight);
        if (newWidth < minSize) {
            if (newX != selectedFurniture.getX()) {
                newX = selectedFurniture.getX() + selectedFurniture.getWidth() - minSize;
            }
            newWidth = minSize;
        }
        if (newHeight < minSize) {
            if (newY != selectedFurniture.getY()) {
                newY = selectedFurniture.getY() + selectedFurniture.getHeight() - minSize;
            }
            newHeight = minSize;
        }

        // Ensure furniture stays within room boundaries
        newX = Math.max(containingRoom.x, Math.min(newX, containingRoom.x + containingRoom.width - newWidth));
        newY = Math.max(containingRoom.y, Math.min(newY, containingRoom.y + containingRoom.height - newHeight));
        newWidth = Math.min(newWidth, containingRoom.x + containingRoom.width - newX);
        newHeight = Math.min(newHeight, containingRoom.y + containingRoom.height - newY);

        // Update furniture dimensions
        selectedFurniture.setX(newX);
        selectedFurniture.setY(newY);
        selectedFurniture.resize(newWidth, newHeight);

        // Update initial point for next drag
        initialPoint = e.getPoint();
    }

    private void handleRoomResizing(MouseEvent e) {
        int dx = e.getX() - initialPoint.x;
        int dy = e.getY() - initialPoint.y;
        int newX = selectedRoom.x;
        int newY = selectedRoom.y;
        int newWidth = selectedRoom.width;
        int newHeight = selectedRoom.height;

        // Original room resizing logic...
        switch (resizeDirection) {
            case "TOP":
                newY += dy;
                newHeight -= dy;
                break;
            case "BOTTOM":
                newHeight += dy;
                break;
            case "LEFT":
                newX += dx;
                newWidth -= dx;
                break;
            case "RIGHT":
                newWidth += dx;
                break;
            case "TOP_LEFT":
                newX += dx;
                newY += dy;
                newWidth -= dx;
                newHeight -= dy;
                break;
            case "TOP_RIGHT":
                newY += dy;
                newWidth += dx;
                newHeight -= dy;
                break;
            case "BOTTOM_LEFT":
                newX += dx;
                newWidth -= dx;
                newHeight += dy;
                break;
            case "BOTTOM_RIGHT":
                newWidth += dx;
                newHeight += dy;
                break;
        }

        // Apply snapping and boundary checks as before
        projectionLines.clear();
        SnapResult snapX = snapToNearestRoom(newX, newY, newWidth, newHeight, true);
        SnapResult snapY = snapToNearestRoom(newY, newX, newHeight, newWidth, false);

        newX = snapX.position;
        newY = snapY.position;
        newWidth = snapX.size;
        newHeight = snapY.size;

        addProjectionLines(snapX, snapY);

        // Ensure minimum size and within panel boundaries
        newWidth = Math.max(20, newWidth);
        newHeight = Math.max(20, newHeight);
        newX = Math.max(0, Math.min(newX, getWidth() - newWidth));
        newY = Math.max(0, Math.min(newY, getHeight() - newHeight));

        // Check if the new room dimensions would cause any furniture to go out of bounds
        boolean furnitureInBounds = true;
        for (Furniture furniture : selectedRoom.getFurnitureList()) {
            if (!isFurnitureInBounds(furniture, newX, newY, newWidth, newHeight)) {
                furnitureInBounds = false;
                break;
            }
        }

        // Only resize if room placement is valid and all furniture stays in bounds
        if (isRoomPlacementValid(selectedRoom, newWidth, newHeight, newX, newY) && furnitureInBounds) {
            selectedRoom.resize(newWidth, newHeight);
            selectedRoom.move(newX, newY);
            updateTotalArea();
            window.updateTotalAreaLabel();

            // Adjust furniture positions if necessary
            adjustFurniturePositions(selectedRoom);
        }

        initialPoint = e.getPoint();
    }

    private void handleMoving(MouseEvent e) {
        if (window.globalLock) return;
        if (selectedFurniture != null) {
            handleFurnitureMoving(e);
        } else if (selectedRoom != null) {
            handleRoomMoving(e);
        }
        repaint();
    }

    private void handleFurnitureMoving(MouseEvent e) {
        if(selectedFurniture.lock) return;
        int newX = e.getX() - mouseOffset.x;
        int newY = e.getY() - mouseOffset.y;

        // Find the containing room
        Room containingRoom = null;
        for (Room room : rooms) {
            if (room.getFurnitureList().contains(selectedFurniture)) {
                containingRoom = room;
                break;
            }
        }

        if (containingRoom != null) {
            // Ensure furniture stays within room boundaries
            newX = Math.max(containingRoom.x,
                          Math.min(newX,
                                 containingRoom.x + containingRoom.width - selectedFurniture.getWidth()));
            newY = Math.max(containingRoom.y,
                          Math.min(newY,
                                 containingRoom.y + containingRoom.height - selectedFurniture.getHeight()));

            selectedFurniture.setX(newX);
            selectedFurniture.setY(newY);
        }
    }

    private void handleRoomMoving(MouseEvent e) {
        int newX = e.getX() - mouseOffset.x;
        int newY = e.getY() - mouseOffset.y;

        projectionLines.clear();

        SnapResult snapX = snapToNearestRoom(newX, newY, selectedRoom.width, selectedRoom.height, true);
        SnapResult snapY = snapToNearestRoom(newY, newX, selectedRoom.height, selectedRoom.width, false);

        newX = snapX.position;
        newY = snapY.position;

        addProjectionLines(snapX, snapY);

        newX = Math.max(0, Math.min(newX, getWidth() - selectedRoom.width));
        newY = Math.max(0, Math.min(newY, getHeight() - selectedRoom.height));

        if (isRoomPlacementValid(selectedRoom, selectedRoom.width, selectedRoom.height, newX, newY)) {
            // Calculate the movement delta
            int deltaX = newX - selectedRoom.x;
            int deltaY = newY - selectedRoom.y;

            // Move the room
            selectedRoom.move(newX, newY);
        }
    }

    private boolean isFurnitureInBounds(Furniture furniture, int roomX, int roomY, int roomWidth, int roomHeight) {
        return furniture.getX() >= roomX &&
               furniture.getY() >= roomY &&
               furniture.getX() + furniture.getWidth() <= roomX + roomWidth &&
               furniture.getY() + furniture.getHeight() <= roomY + roomHeight;
    }

    private void adjustFurniturePositions(Room room) {
        for (Furniture furniture : room.getFurnitureList()) {
            // Ensure furniture stays within room boundaries
            int newX = Math.max(room.x, Math.min(furniture.getX(),
                              room.x + room.width - furniture.getWidth()));
            int newY = Math.max(room.y, Math.min(furniture.getY(),
                              room.y + room.height - furniture.getHeight()));

            furniture.setX(newX);
            furniture.setY(newY);
        }
    }

    private void addProjectionLines(SnapResult snapX, SnapResult snapY) {
        // Add vertical projection lines
        if (snapX.snapped) {
            projectionLines.add(new Line2D.Double(snapX.snapLine, 0, snapX.snapLine, getHeight()));
            // Add opposite axis lines for X
            if (snapX.nearestRoom != null) {
                projectionLines.add(new Line2D.Double(0, snapX.nearestRoom.y, getWidth(), snapX.nearestRoom.y));
                projectionLines.add(new Line2D.Double(0, snapX.nearestRoom.y + snapX.nearestRoom.height, getWidth(), snapX.nearestRoom.y + snapX.nearestRoom.height));
            }
        }

        // Add horizontal projection lines
        if (snapY.snapped) {
            projectionLines.add(new Line2D.Double(0, snapY.snapLine, getWidth(), snapY.snapLine));
            // Add opposite axis lines for Y
            if (snapY.nearestRoom != null) {
                projectionLines.add(new Line2D.Double(snapY.nearestRoom.x, 0, snapY.nearestRoom.x, getHeight()));
                projectionLines.add(new Line2D.Double(snapY.nearestRoom.x + snapY.nearestRoom.width, 0, snapY.nearestRoom.x + snapY.nearestRoom.width, getHeight()));
            }
        }
    }

    private class SnapResult {
        int position;
        int size;
        boolean snapped;
        int snapLine;
        Room nearestRoom;

        SnapResult(int position, int size, boolean snapped, int snapLine, Room nearestRoom) {
            this.position = position;
            this.size = size;
            this.snapped = snapped;
            this.snapLine = snapLine;
            this.nearestRoom = nearestRoom;
        }
    }

    private SnapResult snapToNearestRoom(int position, int otherCoordinate, int size, int otherSize, boolean isHorizontal) {
        int closestDistance = Integer.MAX_VALUE;
        int snappedPosition = position;
        int snappedSize = size;
        boolean snapped = false;
        int snapLine = 0;
        Room nearestRoom = null;

        for (Room room : rooms) {
            if (room == selectedRoom) continue;

            int[] snapPoints = isHorizontal
                    ? new int[]{room.x, room.x + room.width}
                    : new int[]{room.y, room.y + room.height};

            for (int point : snapPoints) {
                int[] distances = {
                        Math.abs(position - point),
                        Math.abs(position + size - point)
                };

                for (int i = 0; i < distances.length; i++) {
                    if (distances[i] <= snapDistance && distances[i] < closestDistance) {
                        boolean aligned = isHorizontal
                                ? (otherCoordinate < room.y + room.height && otherCoordinate + otherSize > room.y)
                                : (otherCoordinate < room.x + room.width && otherCoordinate + otherSize > room.x);

                        if (aligned) {
                            closestDistance = distances[i];
                            if (i == 0) {
                                snappedPosition = point;
                                snappedSize = size;
                            } else {
                                snappedPosition = point - size;
                                snappedSize = size;
                            }
                            snapped = true;
                            snapLine = point;
                            nearestRoom = room;
                        }
                    }
                }
            }
        }

        return new SnapResult(snappedPosition, snappedSize, snapped, snapLine, nearestRoom);
    }

    private boolean isRoomPlacementValid(Room roomToCheck, int newWidth, int newHeight, int newX, int newY) {
        // Check if the room is within panel boundaries
        if (newX < 0 || newY < 0 || newX + newWidth > getWidth() || newY + newHeight > getHeight()) {
            return false;
        }

        Rectangle newRoomBounds = new Rectangle(newX, newY, newWidth, newHeight);
        for (Room room : rooms) {
            if (room != roomToCheck && room.getBounds().intersects(newRoomBounds)) {
                return false; // Overlap detected
            }
        }
        return true; // No overlap and within boundaries
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
//        for (Room room : rooms){
//            if (!room.lock) {
//                if (room.contains(e.getX(), e.getY())) {
//                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
//            } else if (room.containsHandle(e.getX(), e.getY())) {
//                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
//            }  else{
//                 this.setCursor(Cursor.getDefaultCursor());
//                }
//            }
//        }
    }

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    private boolean isOverlapping(Room room) {
        Rectangle newBounds = new Rectangle(room.x, room.y, room.width, room.height);
        for (Room existingRoom : rooms) {
            Rectangle existingBounds = new Rectangle(existingRoom.x, existingRoom.y, existingRoom.width, existingRoom.height);
            if (newBounds.intersects(existingBounds)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void updateTotalArea() {
        totalArea = 0;
        for (Room room : rooms) {
            totalArea += (int) ((room.width * room.height)/(scale*scale*144));

        }
    }


}