import java.awt.*;
import javax.swing.*;

public class RoomDetailsDialog {
    JPanel panel;
    int width;  // total width in inches
    int height; // total height in inches
    String roomType;
    String roomName;
    JTextField widthFeetField;
    JTextField widthInchesField;
    JTextField heightFeetField;
    JTextField heightInchesField;
    String[] roomTypes = {"Bedroom", "Drawing/Dining Room", "Kitchen", "Bathroom"};
    JComboBox<String> typeComboBox;
    JTextField nameField;

    public RoomDetailsDialog() {
        // Create a panel to hold the input fields
        panel = new JPanel(new GridLayout(4, 2, 5, 5));

        // Width fields
        panel.add(new JLabel("Room Width:"));
        JPanel widthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        widthFeetField = new JTextField(3);
        widthInchesField = new JTextField(3);
        widthPanel.add(widthFeetField);
        widthPanel.add(new JLabel("ft"));
        widthInchesField.setText("0");
        widthPanel.add(widthInchesField);
        widthPanel.add(new JLabel("in"));
        panel.add(widthPanel);

        // Height fields
        panel.add(new JLabel("Room Height:"));
        JPanel heightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        heightFeetField = new JTextField(3);
        heightInchesField = new JTextField(3);
        heightPanel.add(heightFeetField);
        heightPanel.add(new JLabel("ft"));
        heightInchesField.setText("0");
        heightPanel.add(heightInchesField);
        heightPanel.add(new JLabel("in"));
        panel.add(heightPanel);

        // Room type
        panel.add(new JLabel("Room Type:"));
        typeComboBox = new JComboBox<>(roomTypes);
        panel.add(typeComboBox);

        // Room name
        panel.add(new JLabel("Room Name:"));
        nameField = new JTextField(20);
        nameField.setText("New Room");
        panel.add(nameField);
    }

    public int showDialog() {
        // Show the dialog and get the input values
        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Parse feet and inches for width
                int widthFeet = parseField(widthFeetField.getText(), "width feet");
                int widthInches = parseInches(widthInchesField.getText(), "width inches");

                // Parse feet and inches for height
                int heightFeet = parseField(heightFeetField.getText(), "height feet");
                int heightInches = parseInches(heightInchesField.getText(), "height inches");

                // Convert to total inches
                width = (int) (((widthFeet * 12) + widthInches)*FloorPlanPanel.scale);
                height = (int) (((heightFeet * 12) + heightInches)*FloorPlanPanel.scale);

                roomType = (String) typeComboBox.getSelectedItem();
                roomName = nameField.getText();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
                return JOptionPane.CANCEL_OPTION;
            }
        }
        return result;
    }

    private int parseField(String value, String fieldName) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Please enter a valid number for " + fieldName);
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number < 0) {
                throw new NumberFormatException(fieldName + " cannot be negative");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Please enter a valid number for " + fieldName);
        }
    }

    private int parseInches(String value, String fieldName) throws NumberFormatException {
        int inches = parseField(value, fieldName);
        if (inches >= 12) {
            throw new NumberFormatException("Inches must be less than 12");
        }
        return inches;
    }
}