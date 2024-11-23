import java.awt.*;
import javax.swing.*;

public class FurnitureCatalogPanel extends JPanel {
    private Furniture selectedFurniture;

    public FurnitureCatalogPanel() {
        setLayout(new GridLayout(0, 1));

        // Adding furniture items
        addFurnitureToCatalog("Sofa", "Pngs/Furniture/sofa.png");
        addFurnitureToCatalog("Table", "Pngs/Furniture/table.png");
        addFurnitureToCatalog("Chair", "Pngs/Furniture/armchair.png");
        addFurnitureToCatalog("Bed", "Pngs/Furniture/bed.png");
        addFurnitureToCatalog("Dining Set", "Pngs/Furniture/Dining Set.png");

        addFurnitureToCatalog("Commode","Pngs/Furniture/commode.png");
        addFurnitureToCatalog("Washbasin","Pngs/Furniture/washbasin.png");
        addFurnitureToCatalog("Shower","Pngs/Furniture/shower.png");
        addFurnitureToCatalog("Kitchen Sink","Pngs/Furniture/sink.png");
        addFurnitureToCatalog("Stove","Pngs/Furniture/stove.png");

    }

    private void addFurnitureToCatalog(String name, String imagePath) {
        ImageIcon image = new ImageIcon(new ImageIcon(getClass().getResource(imagePath)).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JButton furnitureButton = new JButton(name, image);
        furnitureButton.addActionListener(e -> selectedFurniture = new Furniture(name, image));
        add(furnitureButton);
    }

    public Furniture showFurnitureCatalog() {
        int option = JOptionPane.showConfirmDialog(this, this, "Furniture Catalog", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            return selectedFurniture;
        } else {
            return null;
        }
    }
}