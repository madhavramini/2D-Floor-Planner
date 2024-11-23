import java.awt.*;
import javax.swing.*;

public class RoomEditDialog {
    JPanel panel;
    String roomType;
    String roomName;
    String[] roomTypes = {"" ,"Bedroom", "Drawing/Dining Room", "Kitchen", "Bathroom"};
    JComboBox<String> typeComboBox;
    JTextField nameField;

    public RoomEditDialog() {
        // Create a panel to hold the input fields
        panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Room Type:"));
        typeComboBox = new JComboBox<>(roomTypes);
        panel.add(typeComboBox);
        panel.add(new JLabel("Room Name:"));
        nameField = new JTextField(20);
        panel.add(nameField);
    }

    public int showDialog() {
        // Show the dialog and get the input values
        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            roomType = (String) typeComboBox.getSelectedItem();
            roomName = nameField.getText();
        }
        return result;
    }
}