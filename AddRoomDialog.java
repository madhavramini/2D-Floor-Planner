import java.awt.*;
import javax.swing.*;

public class AddRoomDialog {
    JPanel panel;
    int width;
    int height;
    String roomType;
    String roomName;
    JTextField widthField;
    JTextField heightField;
    String[] roomTypes = {"Bedroom", "Living Room", "Kitchen", "Bathroom"};
    JComboBox<String> typeComboBox;
    JTextField nameField;

    public AddRoomDialog() {
        // Create a panel to hold the input fields
        panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Room Width:"));
        widthField = new JTextField(10);
        panel.add(widthField);
        panel.add(new JLabel("Room Height:"));
        heightField = new JTextField(10);
        panel.add(heightField);
        panel.add(new JLabel("Room Type:"));
        typeComboBox = new JComboBox<>(roomTypes);
        panel.add(typeComboBox);
        panel.add(new JLabel("Room Name:"));
        nameField = new JTextField(20);
        panel.add(nameField);
    }

    public void showDialog() {
        // Show the dialog and get the input values
        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String widthStr = widthField.getText();
            String heightStr = heightField.getText();
            roomType = (String) typeComboBox.getSelectedItem();
            roomName = nameField.getText();

            // Process the input values (e.g., validate, send to drawingPanel)
            try {
                width = Integer.parseInt(widthStr);
                height = Integer.parseInt(heightStr);
                // ... other processing, like sending to drawingPanel
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers for width and height.");
            }
        }
    }
}