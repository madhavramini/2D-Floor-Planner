import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class FloorPlanPanel extends JPanel implements MouseListener, MouseMotionListener {
    ArrayList<Room> rooms = new ArrayList<>();
    Room selectedRoom = null;
    Point mouseOffset;
    Point initialPoint;
    int snapDistance = 10;
    boolean resizing = false;
    String resizeDirection = "";
    private ArrayList<Line2D> projectionLines = new ArrayList<>();

    FloorPlanPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (Room room : rooms) {
            room.draw(g2d);
        }

        // Draw projection lines
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        for (Line2D line : projectionLines) {
            g2d.draw(line);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (Room room : rooms) {
            if (room.containsHandle(e.getX(), e.getY())) {
                resizing = true;
                selectedRoom = room;
                initialPoint = e.getPoint();
                resizeDirection = getResizeDirection(room, e.getX(), e.getY());
                break;
            } else if (room.contains(e.getX(), e.getY())) {
                selectedRoom = room;
                room.selected = true;
                mouseOffset = new Point(e.getX() - room.x, e.getY() - room.y);
                break;
            }
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
    public void mouseReleased(MouseEvent e) {
        if (selectedRoom != null) {
            selectedRoom.selected = false;
            selectedRoom = null;
        }
        resizing = false;
        projectionLines.clear();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedRoom != null) {
            if (resizing) {
                handleResizing(e);
            } else {
                handleMoving(e);
            }
        }
        repaint();
    }

    private void handleResizing(MouseEvent e) {
        int dx = e.getX() - initialPoint.x;
        int dy = e.getY() - initialPoint.y;
        int newX = selectedRoom.x;
        int newY = selectedRoom.y;
        int newWidth = selectedRoom.width;
        int newHeight = selectedRoom.height;

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

        // Clear previous projection lines
        projectionLines.clear();

        // Apply snapping and generate projection lines
        SnapResult snapX = snapToNearestRoom(newX, newY, newWidth, newHeight, true);
        SnapResult snapY = snapToNearestRoom(newY, newX, newHeight, newWidth, false);

        newX = snapX.position;
        newY = snapY.position;
        newWidth = snapX.size;
        newHeight = snapY.size;

        // Add projection lines for both axes
        addProjectionLines(snapX, snapY);

        // Ensure minimum size and within panel boundaries
        newWidth = Math.max(20, newWidth);
        newHeight = Math.max(20, newHeight);
        newX = Math.max(0, Math.min(newX, getWidth() - newWidth));
        newY = Math.max(0, Math.min(newY, getHeight() - newHeight));

        // Check for overlaps before resizing
        if (isRoomPlacementValid(selectedRoom, newWidth, newHeight, newX, newY)) {
            selectedRoom.resize(newWidth, newHeight);
            selectedRoom.moveTo(newX, newY);
        }

        initialPoint = e.getPoint();
    }

    private void handleMoving(MouseEvent e) {
        int newX = e.getX() - mouseOffset.x;
        int newY = e.getY() - mouseOffset.y;

        // Clear previous projection lines
        projectionLines.clear();

        // Apply snapping and generate projection lines
        SnapResult snapX = snapToNearestRoom(newX, newY, selectedRoom.width, selectedRoom.height, true);
        SnapResult snapY = snapToNearestRoom(newY, newX, selectedRoom.height, selectedRoom.width, false);

        newX = snapX.position;
        newY = snapY.position;

        // Add projection lines for both axes
        addProjectionLines(snapX, snapY);

        // Ensure the room stays within panel boundaries
        newX = Math.max(0, Math.min(newX, getWidth() - selectedRoom.width));
        newY = Math.max(0, Math.min(newY, getHeight() - selectedRoom.height));

        // Check for overlaps before moving
        if (isRoomPlacementValid(selectedRoom, selectedRoom.width, selectedRoom.height, newX, newY)) {
            selectedRoom.moveTo(newX, newY);
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

    public void addNewRoom(int width, int height) {
        int x = 50;
        int y = 50;

        boolean validPosition = false;
        while (!validPosition) {
            validPosition = true;
            for (Room room : rooms) {
                if (room.getBounds().intersects(new Rectangle(x, y, width, height))) {
                    validPosition = false;
                    x += 10;
                    y += 10;
                    break;
                }
            }
            // Check if the new room is within panel boundaries
            if (x + width > getWidth() || y + height > getHeight()) {
                x = 50;
                y = 50;
                validPosition = false;
            }
        }

        rooms.add(new Room(x, y, width, height));
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}