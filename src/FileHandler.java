import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileHandler {
    private static final String FILE_EXTENSION = "plnt";
    public static String filePath;
    public static ArrayList<Room> savedRooms;
    
    /**
     * Saves the rooms ArrayList to a file selected by the user
     * @param rooms The ArrayList of rooms to save
     * @return true if save was successful, false otherwise
     */

    public static boolean saving(ArrayList<Room> rooms, String filePath){
        savedRooms = rooms;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // Write the number of rooms
            oos.writeInt(rooms.size());

            // Write each room's data
            for (Room room : rooms) {
                // Write room properties
                oos.writeObject(room);

                // Write furniture list if it exists
                if (room.furnitureList != null || !room.furnitureList.isEmpty()) {
                    oos.writeInt(room.furnitureList.size());
                    for (Furniture furniture : room.furnitureList) {
                        oos.writeObject(furniture);
                    }
                } else {
                    oos.writeInt(0);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void saveAs(ArrayList<Room> rooms, MainWindow mainWindow) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PlanIt Files (*." + FILE_EXTENSION + ")", FILE_EXTENSION));

        if (fileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + FILE_EXTENSION)) {
                filePath += "." + FILE_EXTENSION;
            }
            boolean result = saving(rooms,filePath);
            if (result) {
                JOptionPane.showMessageDialog(mainWindow, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainWindow, "An error occurred while saving the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void save(ArrayList<Room> rooms, MainWindow mainWindow){
        if(filePath != null){
            saving(rooms,filePath);
        }
        else {
            saveAs(rooms,mainWindow);
        }
    }

    public static int showExitConfirmationDialog(JFrame parentFrame) {
        // Custom message
        String message = "Floor Plan is NOT Saved.\nDo you STILL want to Quit?";

        // Options
        Object[] options = {"Yes", "Save and Exit", "No"};

        // Show the dialog with custom options and return the user's choice
        return JOptionPane.showOptionDialog(
                parentFrame,
                message,
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[2]
        );
    }

    public static void loading(JFileChooser fileChooser){
        java.io.File f = fileChooser.getSelectedFile();
        filePath = f.getAbsolutePath();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()))) {
            int roomCount = ois.readInt();
            ArrayList<Room> loadedRooms = new ArrayList<>();

            for (int i = 0; i < roomCount; i++) {
                Room room = (Room) ois.readObject();
                
                int furnitureCount = ois.readInt();
                if (furnitureCount > 0) {
                    room.furnitureList = new ArrayList<>();
                    for (int j = 0; j < furnitureCount; j++) {
                        Furniture furniture = (Furniture) ois.readObject();
                        room.furnitureList.add(furniture);
                    }
                }

                loadedRooms.add(room);
            }
            MainWindow mainWindow = new MainWindow();
            if (loadedRooms != null) {
                mainWindow.drawingPanel.rooms = loadedRooms;
                mainWindow.updateRoomTable();
                mainWindow.drawingPanel.repaint();
                mainWindow.checkLock();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads rooms from a file selected by the user
     */
    public static void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PlanIt Files (*." + FILE_EXTENSION + ")", FILE_EXTENSION));
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            loading(fileChooser);
        }
    }

    public static void loadFromFile(StartOnLaunchPage window){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PlanIt Files (*." + FILE_EXTENSION + ")", FILE_EXTENSION));
        if (fileChooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            loading(fileChooser);
        }
    }


}

