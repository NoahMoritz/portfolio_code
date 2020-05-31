package de.noamo.sql;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Noah Moritz Hölterhoff
 */
public class GUI extends JFrame {
    private final static String TITLE = "MySQL - Manager"; // Der Titel des Fensters
    private final static int STANDARD_HEIGHT = 500, STANDARD_WIDTH = 700; // Die Standartgröße
    private final static Font STANDART_FONT = new Font("Consolas", Font.PLAIN, 12);
    private final static Icon treeLeaf = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/menu.gif")));
    private final static Icon treeRoot = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/hardDrive.gif")));

    private JSplitPane dataSet; // Der Bereich mit allen Tabbeln und dere Anzeigen
    private JTree dataBaseTree = new JTree(); // Übersicht aller Tabellen
    private JTable currentTabel = new JTable(); // Inhalt der ausgewählten Tabelle
    private DataBase dataBase; // Die Datenbank-Verbindung
    private String displayedTabel; // Der Name der Tabelle, die aktuell angezeigt wird (zum aktualisieren)

    public GUI(DataBase pDataBase) throws SQLException {
        super(TITLE); // JFrame erstellen und Titel einfügen
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/com/sun/java/swing/plaf/motif/icons/DesktopIcon.gif")));
        this.dataBase = pDataBase; // Datenbank Objekt ins Attribut kopieren
        this.setSize(STANDARD_WIDTH, STANDARD_HEIGHT); // Größe anpassen
        this.setLocationRelativeTo(null); // JFrame in die Mitte setzten
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Bei auf X drücken schließen
        this.getContentPane().setLayout(new BorderLayout(5, 5)); // Border-Layout einstellen
        this.getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 5 Pixel Rand
        this.createComponents(); // Mit Inhalt füllen
        this.setVisible(true); // JFrame sichtbar machen
    }

    private void createComponents() throws SQLException {
        JLabel title = new JLabel("MySQL - Manager", SwingConstants.CENTER); // Überschrift
        title.setFont(title.getFont().deriveFont(25F)); // Schriftgröße ändern
        this.getContentPane().add(title, BorderLayout.NORTH); // Überschirft zum Fenster hinzufügen

        dataSet = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataBaseTree, new JScrollPane(currentTabel)); // JTree und Tabelle zusammenfügen
        dataSet.setDividerLocation(150); // Größe der Tabellen-Übersicht festlegen
        dataSet.setBorder(BorderFactory.createEmptyBorder());
        this.add(dataSet, BorderLayout.CENTER); // JTree und Tabelle hinzufügen

        updatedataBaseTree(); // JTree mit der Datenbank synchronisieren
        createInputComponents(); // Input Bereich erstellen
    }

    private void createInputComponents() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder());

        // --- SQL QUERY ---
        JPanel queryPanel = new JPanel(null);
        queryPanel.setPreferredSize(new Dimension(300, 220));
        queryPanel.setMinimumSize(new Dimension(300, 220));
        queryPanel.setMaximumSize(new Dimension(300, 220));
        queryPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JLabel queryTitle = new JLabel("Abfrage senden", JLabel.CENTER);
        queryTitle.setFont(STANDART_FONT.deriveFont(16F).deriveFont(Font.BOLD));
        queryPanel.add(queryTitle);
        JTextArea queryTextArea = new JTextArea();
        queryTextArea.setFont(STANDART_FONT);
        JScrollPane queryScrollTextArea = new JScrollPane(queryTextArea);
        queryPanel.add(queryScrollTextArea);
        JButton queryButton = new JButton("Senden");
        queryButton.addActionListener(e -> {
            try {
                ResultSet rs = dataBase.executeQuery(queryTextArea.getText());
                JFrame output = new JFrame("Output");
                output.getContentPane().add(new JScrollPane(Util.resultSetToJTable(rs)));
                output.setVisible(true);
                output.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                output.pack();
                output.setMinimumSize(new Dimension(300, 200));
                output.setLocationRelativeTo(null);
            } catch (SQLException e1) {
                e1.printStackTrace();
                try {
                    dataBase.executeUpdate(queryTextArea.getText());
                    updatedataBaseTree();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
            refreshTabel();
        });
        queryPanel.add(queryButton);
        queryTitle.setBounds(5, 5, 290, 20);
        queryScrollTextArea.setBounds(5, 30, 290, 150);
        queryButton.setBounds(5, 185, 290, 30);
        inputPanel.add(queryPanel);

        inputPanel.add(new JPanel());
        this.add(inputPanel, BorderLayout.EAST);
    }

    private void updatedataBaseTree() throws SQLException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(dataBase.getDataBase());
        ArrayList<String> all = new ArrayList<>();
        ResultSet rs = dataBase.executeQuery("SHOW TABLES");
        while (rs.next()) {
            String s = rs.getString(1);
            DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(s);
            root.add(dmtn);
            all.add(s);
        }
        dataBaseTree = new JTree(root);
        dataBaseTree.addTreeSelectionListener(e -> {
            String node = e.getNewLeadSelectionPath().getLastPathComponent().toString();
            for (String s : all) {
                if (s.equals(node)) openTable(node); // JTabel zu der Tabel aufrufen
            }
        });
        int dividerLoc = dataSet.getDividerLocation();
        dataSet.setLeftComponent(new JScrollPane(dataBaseTree));
        dataSet.setDividerLocation(dividerLoc);

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) dataBaseTree.getCellRenderer();
        renderer.setLeafIcon(treeLeaf);
        renderer.setClosedIcon(treeRoot);
        renderer.setOpenIcon(treeRoot);
    }

    private void refreshTabel() {
        if (displayedTabel != null) openTable(displayedTabel);
    }

    private void openTable(String table) {
        try {
            currentTabel = Util.resultSetToJTable(dataBase.executeQuery("SELECT * FROM " + table));
            int dividerLoc = dataSet.getDividerLocation();
            dataSet.setRightComponent(new JScrollPane(currentTabel));
            dataSet.setDividerLocation(dividerLoc);
            displayedTabel = table;
        } catch (Exception ex) {
            System.out.printf("Tabelle konnte nicht geöffnet werden!");
        }
    }
}
