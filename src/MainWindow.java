import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;


public class MainWindow extends JFrame implements ActionListener {

    public menuBar menubar;
    public JButton addRoomButton, lockRoomsButton;
    public RoomDetailsDialog roomDialog;
    public JPanel leftPanel;
    public final FloorPlanPanel drawingPanel;
    public JTable roomTable;
    public RoomTableModel tableModel;
    public JScrollPane tableScrollPane;
    public boolean globalLock = false;
    //public FurnitureCatalogPanel furnitureCatalog;
    JLabel totalAreaLabel;
    JPanel infoPanel = new JPanel();
    JLabel addRoom = new JLabel("CLICK ON THE FLOOR PANEL TO POSITION THE ROOM");





    // Method to initialize the table
    private void initializeRoomTable() {
        tableModel = new RoomTableModel(drawingPanel.getRooms());
        roomTable = new JTable(tableModel);

        // Set up the button column
        roomTable.getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
        roomTable.getColumnModel().getColumn(1).setCellEditor(new ButtonEditor(roomTable, this));

        // Set column widths
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        // Set row height to accommodate buttons
        roomTable.setRowHeight(55);

        // Create scroll pane and add it to the left panel
        tableScrollPane = new JScrollPane(roomTable);
        tableScrollPane.setBounds(15, 220, 228, 720);
        leftPanel.add(tableScrollPane);
    }

    // methods to handle room operations
    public void addNewRoom( String roomName, String roomType, int width, int height,  ArrayList<Furniture> furnitureList) {
        Room newRoom = new Room(0, 0, width, height, roomName, roomType);
        newRoom.lock = globalLock;

        drawingPanel.setRoomToPlace(newRoom);

        updateTotalAreaLabel();
//        JLabel addRoom = new JLabel("Click on the floor plan to place the room.");
        addRoom.setForeground(Color.WHITE); // White text
        addRoom.setFont(infoPanel.getFont().deriveFont(Font.BOLD, 16f)); // Font styling
        addRoom.setHorizontalAlignment(SwingConstants.CENTER);
        //addRoom.setVisible(false);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(addRoom, BorderLayout.CENTER); // Add to the extreme right
        addRoom.setVisible(true);
    }

    public void deleteRoom(int index) {
        drawingPanel.getRooms().remove(index);
        updateRoomTable();
        updateTotalAreaLabel();
        drawingPanel.repaint();
    }

    public void editRoom(int index) {
        Room room = drawingPanel.getRooms().get(index);
        RoomEditDialog editDialog = new RoomEditDialog();
        if (editDialog.showDialog() == JOptionPane.OK_OPTION){}
        String newName = editDialog.roomName;
        String newType = editDialog.roomType;
        if (newName != null && !newName.trim().isEmpty()) {
            room.name = newName.trim();
            updateRoomTable();
            drawingPanel.repaint();
        }

        if(newType.equals("")){}
        else{
            room.type = newType;
            repaint();
        }
    }

    public void lockAllRooms() {
        lockRoomsButton.setText("Unlock Layout");
        globalLock = true;
        for (Room room : drawingPanel.getRooms()) {
            room.lockRoom();
        }
        repaint();
    }

    public void unlockAllRooms() {
        for (Room room : drawingPanel.getRooms()) {
            if (!room.getFixtureList().isEmpty()) {
                int option = JOptionPane.showConfirmDialog(this, "Unlocking the layout will delete all fixtures in the rooms. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) break;
                else return;
            }
        }
        for (Room room : drawingPanel.getRooms()) {
            room.unlockRoom();
        }
        globalLock = false;
        lockRoomsButton.setText("Lock Layout");
        repaint();
    }

    public void checkLock(){
        for(Room rooms : drawingPanel.getRooms()){
            if(rooms.lock){
                lockAllRooms();
                break;
            }
        }
    }

    //method to update the table
    public void updateRoomTable() {
        tableModel.updateData(drawingPanel.getRooms());
    }


    public MainWindow(){
        //Image Imports
        ImageIcon logo = new ImageIcon("src/Pngs/Logos/logo.png");
        ImageIcon logotxt = new ImageIcon("src/Pngs/Logos/logotxt.png");
        ImageIcon direction = new ImageIcon(new ImageIcon("src/Pngs/northup.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));

        // Add Room Button
        addRoomButton = new JButton("Add Room");
        addRoomButton.setBounds(25, 90, 200, 40);
        addRoomButton.addActionListener(this);
        addRoomButton.setFocusable(false);
        addRoomButton.setFont(new Font("Arial",Font.PLAIN ,25)); //Text Styling
        addRoomButton.setForeground(Color.white); // Text Colour
        addRoomButton.setBackground(Color.decode("#ed1a24"));//BackGround Colour
        addRoomButton.setBorder(BorderFactory.createEtchedBorder());//Button Border
        addRoomButton.setOpaque(true);
        addRoomButton.setHorizontalAlignment(SwingConstants.CENTER);
        addRoomButton.setVerticalAlignment(SwingConstants.CENTER);
        addRoomButton.addActionListener(e -> {
            roomDialog = new RoomDetailsDialog();
            if (roomDialog.showDialog() == JOptionPane.OK_OPTION) {
                addNewRoom(roomDialog.roomName, roomDialog.roomType,roomDialog.width, roomDialog.height,null);
            }
        });

        // Lock Room Button (Global Lock)
        lockRoomsButton = new JButton("Lock Layout");
        lockRoomsButton.setBounds(25, 150, 200, 40);
        lockRoomsButton.setFocusable(false);
        lockRoomsButton.setFont(new Font("Arial",Font.PLAIN ,25));
        lockRoomsButton.setForeground(Color.white);
        lockRoomsButton.setBackground(Color.decode("#ed1a24"));
        lockRoomsButton.setBorder(BorderFactory.createEtchedBorder());
        lockRoomsButton.setOpaque(true);
        lockRoomsButton.setHorizontalAlignment(SwingConstants.CENTER);
        lockRoomsButton.setVerticalAlignment(SwingConstants.CENTER);
        lockRoomsButton.addActionListener(e->{
            if(globalLock) unlockAllRooms();
            else lockAllRooms();
        });


        //Panels Code
        JLabel logolabel = new JLabel();
        logolabel.setBounds(0,10,250,70);
        logolabel.setIcon(logotxt);


        // Create and setup directions label
        JLabel directionsLabel = new JLabel(direction);
        directionsLabel.setBounds(0, 0, direction.getIconWidth(), direction.getIconHeight());
        JPanel directionsPanel = new JPanel(null);
        directionsPanel.setOpaque(false);
        directionsPanel.add(directionsLabel);
        directionsPanel.setBounds(0, 0, direction.getIconWidth(), direction.getIconHeight());

        leftPanel = new JPanel();
        leftPanel.setBackground(Color.black);
        leftPanel.setPreferredSize(new Dimension(250, this.getHeight())); // Fixed width, dynamic height
        leftPanel.setLayout(null); // Set to null layout for manual positioning

        drawingPanel = new FloorPlanPanel(this);
        drawingPanel.setBackground(new Color(211, 211, 211, 255)); //Grey color
        drawingPanel.setLayout(null); // For custom drawing and room placements
        drawingPanel.setBorder(BorderFactory.createEtchedBorder());//panel Border

        // Add directions panel to drawing panel & position the directions panel in the top right corner
        drawingPanel.add(directionsPanel);
        drawingPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int x = drawingPanel.getWidth() - direction.getIconWidth() - 10; // 10 pixels padding from right
                int y = 10; // 10 pixels padding from top
                directionsPanel.setBounds(x, y, direction.getIconWidth(), direction.getIconHeight());
            }
        });


        infoPanel.setBackground(Color.red);
        infoPanel.setPreferredSize(new Dimension(this.getWidth(), 20)); // Fixed height, dynamic width
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align text to the left
        totalAreaLabel = new JLabel("Total Area = " + drawingPanel.totalArea +" sq.ft.      ");
        totalAreaLabel.setForeground(Color.WHITE); // White text
        totalAreaLabel.setFont(infoPanel.getFont().deriveFont(Font.BOLD, 14f)); // Font styling
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(totalAreaLabel, BorderLayout.EAST); // Add to the extreme right


        //JFrame Code
        this.setTitle("PlanIt Floor Planner");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLayout(new BorderLayout());
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(Color.black);
        this.setIconImage(logo.getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //adding
        leftPanel.add(addRoomButton);
        leftPanel.add(lockRoomsButton);
        //Panels Code adding to Frame
        leftPanel.add(logolabel);
        this.add(leftPanel, BorderLayout.WEST);  // Left side for tools
        this.add(drawingPanel, BorderLayout.CENTER);  // Center for floor plan
        this.add(infoPanel, BorderLayout.SOUTH);  // Bottom for displaying information
        initializeRoomTable();


        //Menubar Code
        menubar = new menuBar();
        this.setJMenuBar(menubar);

        menubar.newfile.addActionListener(this);
        menubar.open.addActionListener(this);
        menubar.save.addActionListener(this);
        menubar.saveas.addActionListener(this);
        menubar.move.addActionListener(this);
        menubar.rename.addActionListener(this);
        menubar.print.addActionListener(this);
        menubar.exit.addActionListener(this);
        menubar.undo.addActionListener(this);
        menubar.redo.addActionListener(this);
        menubar.copy.addActionListener(this);
        menubar.paste.addActionListener(this);
        menubar.cut.addActionListener(this);
        menubar.selectall.addActionListener(this);

        //Exit Dialog
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = FileHandler.showExitConfirmationDialog(MainWindow.this);

                // Handle the user's choice
                switch (option) {
                    case JOptionPane.YES_OPTION:
                        System.exit(0);
                        break;
                    case JOptionPane.NO_OPTION:
                        FileHandler.save(drawingPanel.rooms,MainWindow.this);
                        System.exit(0);
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        break;
                    default:
                        break;
                }
                }
            }
        );


        //SetVisible Command for the window .ALWAYS KEEP THIS AS THE LAST LINE IN THE CONSTRUCTOR
        this.setVisible(true);
    }

    public void updateTotalAreaLabel() {
        int totalArea = 0;
        for (Room room : drawingPanel.getRooms()) {
            totalArea += (int) ((room.width * room.height)/(FloorPlanPanel.scale*FloorPlanPanel.scale*144));
        }

        totalAreaLabel.setText("Total Area = " + totalArea + "sq.ft.     ");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "New":
                new MainWindow();
                break;
            case "Open":
                FileHandler.loadFromFile();
                break;
            case "Save":
                FileHandler.save(drawingPanel.rooms,this);
                break;
            case "Save As...":
                FileHandler.saveAs(drawingPanel.rooms, this);
                break;
//            case "Move":
//                // Handle move action
//                break;
//            case "Rename":
//                // Handle rename action
//                break;
//            case "Print":
//                // Handle printing
//                break;
            case "Exit":
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Display the custom dialog
                int option = FileHandler.showExitConfirmationDialog(frame);

                // Handle the user's choice
                switch (option) {
                    case JOptionPane.YES_OPTION:
                        System.exit(0);
                        break;
                    case JOptionPane.NO_OPTION:
                        FileHandler.save(drawingPanel.rooms,this);
                        System.exit(0);
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        break;
                    default:
                        break;
                }
                break;
//            case "Undo":
//                // Handle undo
//                break;
//            case "Redo":
//                // Handle redo
//                break;
//            case "Copy":
//                // Handle copy
//                break;
//            case "Paste":
//                // Handle paste
//                break;
//            case "Cut":
//                // Handle cut
//                break;
//            case "Select All":
//                // Handle select all
//                break;
            case "":
            default:
                break;
        }



    }
    public static void main(String[] args) {
        MainWindow window = new MainWindow();
    }
}
