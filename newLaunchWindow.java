import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class newLaunchWindow extends JFrame implements ActionListener {

    public menuBar menubar;
    public JButton addRoomButton;
    public AddRoomDialog roomDialog;
    public JPanel leftPanel;
    private final FloorPlanPanel drawingPanel;

    public newLaunchWindow(){
        //Image Imports
        ImageIcon logo = new ImageIcon("logo.png");
        ImageIcon logotxt = new ImageIcon("logotxt.png");
        ImageIcon direction = new ImageIcon("directions.png");

        // Buttons Code
        addRoomButton = new JButton("Add New Room");
        addRoomButton.setBounds(10, 90, 200, 40);
        addRoomButton.addActionListener(this);
        addRoomButton.setFocusable(false);
        addRoomButton.setFont(new Font("Comic Sans",Font.PLAIN ,25)); //Text Styling
        addRoomButton.setForeground(Color.white); // Text Colour
        addRoomButton.setBackground(Color.decode("#ed1a24"));//BackGround Colour
        addRoomButton.setBorder(BorderFactory.createEtchedBorder());//Button Border
        addRoomButton.setOpaque(true);
        addRoomButton.setHorizontalAlignment(SwingConstants.CENTER);
        addRoomButton.setVerticalAlignment(SwingConstants.CENTER);

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

        drawingPanel = new FloorPlanPanel();
        drawingPanel.setBackground(Color.white);
        drawingPanel.setLayout(null); // For custom drawing and room placements
        drawingPanel.setBorder(BorderFactory.createEtchedBorder());//panel Border

        // Add directions panel to drawing panel
        drawingPanel.add(directionsPanel);
        // Position the directions panel in the top right corner
        drawingPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int x = drawingPanel.getWidth() - direction.getIconWidth() - 10; // 10 pixels padding from right
                int y = 10; // 10 pixels padding from top
                directionsPanel.setBounds(x, y, direction.getIconWidth(), direction.getIconHeight());
            }
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.red);
        infoPanel.setPreferredSize(new Dimension(this.getWidth(), 20)); // Fixed height, dynamic width
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align text to the left

        //JFrame Code
        this.setTitle("Planit - 2D Floor Planner");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLayout(new BorderLayout());
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(Color.black);
        this.setIconImage(logo.getImage());

        //adding
        leftPanel.add(addRoomButton);
        //Panels Code adding to Frame
        leftPanel.add(logolabel);
        this.add(leftPanel, BorderLayout.WEST);  // Left side for tools
        this.add(drawingPanel, BorderLayout.CENTER);  // Center for floor plan
        this.add(infoPanel, BorderLayout.SOUTH);  // Bottom for displaying information

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

        //SetVisible Command for the window .ALWAYS KEEP THIS AS THE LAST LINE IN THE CONSTRUCTOR
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==addRoomButton){
            /*
            String widthStr = JOptionPane.showInputDialog(this, "Enter room width:");
            String heightStr = JOptionPane.showInputDialog(this, "Enter room height:");
            if (widthStr != null && heightStr != null) {
                try {
                    int width = Integer.parseInt(widthStr);
                    int height = Integer.parseInt(heightStr);
                    drawingPanel.addNewRoom(width, height);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
                }
            }
            */
            roomDialog = new AddRoomDialog();
            roomDialog.showDialog();
            int width = roomDialog.width;
            int height = roomDialog.height;
            drawingPanel.addNewRoom(width, height);
        }


        String command = e.getActionCommand();


        switch (command) {
            case "New":
                // Handle new file creation
                break;
            case "Open":
                // Handle opening a file
                break;
            case "Save":
                // Handle saving a file
                break;
            case "Save As...":
                // Handle "Save As..." functionality
                break;
            case "Move":
                // Handle move action
                break;
            case "Rename":
                // Handle rename action
                break;
            case "Print":
                // Handle printing
                break;
            case "Exit":
                System.exit(0);  // Exits the application
                break;
            case "Undo":
                // Handle undo
                break;
            case "Redo":
                // Handle redo
                break;
            case "Copy":
                // Handle copy
                break;
            case "Paste":
                // Handle paste
                break;
            case "Cut":
                // Handle cut
                break;
            case "Select All":
                // Handle select all
                break;
            default:
                break;
        }


    }
    public static void main(String[] args) {
        new newLaunchWindow();
    }
}
