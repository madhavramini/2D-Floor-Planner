import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class StartOnLaunchPage extends JFrame implements ActionListener {

    JButton newWindowButton;
    JButton openWindowButton;

    public StartOnLaunchPage(){

        ImageIcon logo = new ImageIcon("src/Pngs/Logos/logo.png");
        ImageIcon logoImage = new ImageIcon("src/Pngs/Logos/biglogo.png");


        newWindowButton = new JButton();
        newWindowButton.setBounds(25,125,170,50);
        newWindowButton.setText("New Project");
        newWindowButton.setFocusable(false);
        newWindowButton.addActionListener(this);
        newWindowButton.setFont(new Font("Comic Sans",Font.PLAIN ,25)); //Text Styling
        newWindowButton.setForeground(Color.WHITE);//Text Colour
        newWindowButton.setBackground(Color.RED);//BackGround Colour
        newWindowButton.setBorder(BorderFactory.createEtchedBorder());//Button Border
        newWindowButton.setOpaque(true);

        openWindowButton = new JButton();
        openWindowButton.setBounds(25,225,170,50);
        openWindowButton.setText("Open Project");
        openWindowButton.setFocusable(false);
        openWindowButton.addActionListener(this);
        openWindowButton.setFont(new Font("Comic Sans",Font.PLAIN,25));
        openWindowButton.setForeground(Color.WHITE);
        openWindowButton.setBackground(Color.RED);
        openWindowButton.setBorder(BorderFactory.createEtchedBorder());
        openWindowButton.setOpaque(true);







        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(logoImage);
        logoLabel.setVerticalAlignment(JLabel.CENTER);
        logoLabel.setHorizontalAlignment(JLabel.CENTER);




        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(Color.black);
        buttonsPanel.setBounds(0,0,250,550);
        buttonsPanel.setLayout(null);


        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Color.black);
        logoPanel.setBounds(250,0,550,550);
        logoPanel.setLayout(new BorderLayout());
        logoPanel.add(logoLabel, BorderLayout.WEST);






        this.setTitle("Planit - 2D Floor Planner");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,550);
        this.setLayout(null);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(Color.black);
        logoPanel.add(logoLabel);
        buttonsPanel.add(newWindowButton);
        buttonsPanel.add(openWindowButton);
        this.add(logoPanel);
        this.add(buttonsPanel);
        this.setIconImage(logo.getImage());
        this.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==newWindowButton){
            new MainWindow();
            this.dispose();
        }
        if(e.getSource()==openWindowButton){
            FileHandler.loadFromFile(this);
            this.dispose();
        }
    }

    public static void main(String[] args) {
        new StartOnLaunchPage();
    }


}
