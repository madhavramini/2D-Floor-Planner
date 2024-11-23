import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

class RoomTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Room Name", "Actions"};
    private ArrayList<Room> rooms;

    
    public RoomTableModel(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }
    
    @Override
    public int getRowCount() {
        return rooms.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Room room = rooms.get(rowIndex);
        if (columnIndex == 0) {
            return "<html>" + room.name + "<br><font color='red'>" + room.type + "</font></html>" ;
        }
        return ""; // For the buttons column
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 1; // Only allow editing in the buttons column
    }
    
    public void updateData(ArrayList<Room> newRooms) {
        this.rooms = newRooms;
        fireTableDataChanged();
    }
}

class ButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton deleteBtn, editBtn;
    
    public ButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        ImageIcon deleteIcon = new ImageIcon(getClass().getResource("Pngs/TableIcons/deleteicon.png"));
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
        ImageIcon editIcon = new ImageIcon(getClass().getResource("Pngs/TableIcons/editicon.png"));
        editIcon = new ImageIcon(editIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));

        deleteBtn = new JButton(deleteIcon);
        editBtn = new JButton(editIcon);
        
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        
        add(editBtn);
        add(deleteBtn);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        return this;
    }
}

class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JButton deleteBtn, editBtn;
    private final JTable table;
    private final MainWindow window;
    
    public ButtonEditor(JTable table, MainWindow window) {
        this.table = table;
        this.window = window;
        
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        ImageIcon deleteIcon = new ImageIcon(getClass().getResource("Pngs/TableIcons/deleteicon.png"));
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
        ImageIcon editIcon = new ImageIcon(getClass().getResource("Pngs/TableIcons/editicon.png"));
        editIcon = new ImageIcon(editIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));

        deleteBtn = new JButton(deleteIcon);
        editBtn = new JButton(editIcon);
        
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        
        deleteBtn.addActionListener(e -> deleteRoom());
        editBtn.addActionListener(e -> editRoom());
        
        panel.add(editBtn);
        panel.add(deleteBtn);
    }
    
    private void deleteRoom() {
        int row = table.getSelectedRow();
        if (row != -1) {
            window.deleteRoom(row);
        }
        fireEditingStopped();
    }

    private void editRoom() {
        int row = table.getSelectedRow();
        if (row != -1) {
            window.editRoom(row);
        }
        fireEditingStopped();
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }
    
    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
