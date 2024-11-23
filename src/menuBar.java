import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public class menuBar extends JMenuBar {

    private static final Color LOGO_RED = new Color(237, 28, 36);
    private static final Color BACKGROUND_BLACK = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE; // Changed to white
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 30);

    public JMenuItem newfile, open, save, saveas, move, rename, print, exit;
    public JMenuItem undo, redo, copy, paste, cut, selectall;

    public menuBar() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        JMenu file = createMenu("File");
        JMenu edit = createMenu("Edit");
        JMenu insert = createMenu("Insert");
        JMenu help = createMenu("Help");

        newfile = createMenuItem("New", KeyEvent.VK_N);
        open = createMenuItem("Open", KeyEvent.VK_O);
        save = createMenuItem("Save", KeyEvent.VK_S);
        saveas = createMenuItem("Save As...", KeyEvent.VK_S, InputEvent.SHIFT_MASK);
        move = createMenuItem("Move", KeyEvent.VK_M);
        rename = createMenuItem("Rename", KeyEvent.VK_R);
        print = createMenuItem("Print", KeyEvent.VK_P);
        exit = createMenuItem("Exit", KeyEvent.VK_ESCAPE, 0);

        undo = createMenuItem("Undo", KeyEvent.VK_Z);
        redo = createMenuItem("Redo", KeyEvent.VK_Y);
        copy = createMenuItem("Copy", KeyEvent.VK_C);
        paste = createMenuItem("Paste", KeyEvent.VK_V);
        cut = createMenuItem("Cut", KeyEvent.VK_X);
        selectall = createMenuItem("Select All", KeyEvent.VK_A);

        addItemsToMenu(file, newfile, open, save, saveas, move, rename, print, exit);
        addItemsToMenu(edit, undo, redo, copy, paste, cut, selectall);

        add(file);
        add(edit);
        add(insert);
        add(help);
    }

    private JMenu createMenu(String title) {
        JMenu menu = new JMenu(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, LOGO_RED, 0, getHeight(), BACKGROUND_BLACK));
                g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
            }
        };
        menu.setForeground(TEXT_COLOR);
        menu.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        menu.setFont(menu.getFont().deriveFont(Font.BOLD, 14f));

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menu.setOpaque(true);
                menu.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menu.setOpaque(false);
                menu.setBackground(null);
            }
        });

        return menu;
    }

    private JMenuItem createMenuItem(String title, int keyEvent) {
        return createMenuItem(title, keyEvent, InputEvent.CTRL_MASK);
    }

    private JMenuItem createMenuItem(String title, int keyEvent, int modifiers) {
        JMenuItem item = new JMenuItem(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BACKGROUND_BLACK, 0, getHeight(), LOGO_RED));
                g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
            }
        };
        item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifiers));
        item.setForeground(TEXT_COLOR);
        item.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        item.setFont(item.getFont().deriveFont(12f));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setOpaque(true);
                item.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setOpaque(false);
                item.setBackground(null);
            }
        });

        return item;
    }

    private void addItemsToMenu(JMenu menu, JMenuItem... items) {
        for (JMenuItem item : items) {
            menu.add(item);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, LOGO_RED, 0, getHeight(), BACKGROUND_BLACK);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Add a subtle glossy effect
        GradientPaint highlight = new GradientPaint(0, 0, new Color(255, 255, 255, 50), 0, getHeight() / 2, new Color(255, 255, 255, 0));
        g2.setPaint(highlight);
        g2.fillRect(0, 0, getWidth(), getHeight() / 2);

        g2.dispose();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorderPainted(false);
    }
}