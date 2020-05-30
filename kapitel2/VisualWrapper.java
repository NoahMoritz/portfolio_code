/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.launcher;

import com.github.weisj.darklaf.DarkLaf;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import de.noamo.util.BasicNetwork;
import de.noamo.util.Page;
import de.noamo.util.Protocol;
import de.noamo.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * @author Noah Mortiz Hölterhoff
 * @version 12.05.2020
 * @since 10.03.2020
 */
public class VisualWrapper extends JFrame {
    private static final Dimension MIN_DIMENSION = new Dimension(800, 580);
    private static final Dimension PREFERRED_DIMENSION = new Dimension(1039, 569);
    private Page currentPage;
    private HomeScreen homeScreen=null;

    /**
     * Erstellt den übergeordnetes JFrame für alle Fenster in dem Programm
     */
    public VisualWrapper() {
        super();

        Values.currentVisualWrapper = this;
        setMinimumSize(MIN_DIMENSION);
        setSize(PREFERRED_DIMENSION);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("NOAMO Launcher");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Process p : Values.processes){
                    p.destroyForcibly();
                }
            }
        });

        try {
            setIconImage(ImageIO.read(VisualWrapper.class.getResourceAsStream("/logo.png")));
        } catch (Exception ignored) {
        }

        JLabel loading = new JLabel("Wird geladen");
        loading.setHorizontalAlignment(SwingConstants.CENTER);
        loading.setFont(new Font("Arial", Font.BOLD, 20));
        add(loading);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static void checkArgs(String[] args){
        for (String s : args){
            try {
                if (s.toLowerCase().startsWith("laf")) {
                    if (s.endsWith("0")) loadDarklefTheme();
                    else if (s.endsWith("1")) loadSystemTheme();
                }
                if (s.toLowerCase().startsWith("host")) {
                    Values.HOST = s.substring(4);
                }
            } catch (Exception ex){
              JOptionPane.showMessageDialog(null,"Parameter '"+s+"' konnte nicht gelesen werden.","Fehler",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JMenuBar creatJMenuBarFromCurrentPage() {
        JMenuItem[] menuItems = currentPage.getMenuItems();
        JMenuBar bar = new JMenuBar();

        if(currentPage.isExternalApp()&&homeScreen!=null){
            JMenu close = new JMenu("App schlie\u00DFen");
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    transitionTo(homeScreen);
                }
            });
            bar.add(close);
        }

        if (currentPage.getLast() != null) {
            JMenu back = new JMenu("Zur\u00fcck");
            back.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    transitionTo(currentPage.getLast());
                }
            });
            bar.add(back);
        }

        if (menuItems != null) {
            for (JMenuItem menuItem : menuItems) {
                bar.add(menuItem);
            }
        }

        return bar;
    }

    private static void loadDarklefTheme(){
        try {
            LafManager.setTheme(new DarculaTheme());
            UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            loadSystemTheme();
        }
    }

    private static void loadFont() {
        try {
            // Datei laden
            InputStream inputStream = VisualWrapper.class.getResourceAsStream("/Futura.ttf");

            // Schriftart im System registrieren
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, inputStream));

            // Schriftart in Variable laden
            FontUIResource font = new FontUIResource("Futura", Font.BOLD, 14);

            // Alle UI Elemente mit Schriftarten
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);

                // Alle UI Elemente setzten
                if (value instanceof FontUIResource) UIManager.put(key, font);
            }
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    private static boolean loadLogin() {
        try {
            if(!Values.LOGIN_SAV_FILE.exists()) return false;
            String temp = Util.loadTxtFileIntoString(Values.LOGIN_SAV_FILE);
            String[] temp_split = temp.split(System.lineSeparator());
            return login(temp_split[0], temp_split[1]);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void loadSystemTheme(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean login(String username, String password) {
        if(username==null||password==null||username.equals("")||password.equals("")) return false;
        try {
            String[] re = BasicNetwork.sendRequest(Values.HOST, Values.PORT, new String[]{username, password, Protocol.ACCOUNT_INFOS});
            if (re[0].equals(Protocol.LOGIN_FAILED)) {
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Anmeldedaten falsch ", "Anmeldedaten", JOptionPane.ERROR_MESSAGE);
                if (Values.LOGIN_SAV_FILE.exists()) Values.LOGIN_SAV_FILE.delete();
                return false;
            }
            if (re[0].equals(Protocol.ERROR) || re[0].equals(Protocol.SERVER_ERROR)) {
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Server-Fehler! Bitte probieren Sie es später noch einmal ", "Fehler", JOptionPane.ERROR_MESSAGE);
                if (Values.LOGIN_SAV_FILE.exists()) Values.LOGIN_SAV_FILE.delete();
                return false;
            }
            if (re[0].equals(Protocol.NOT_VALIDATED)){
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Ihr Account ist aktuell nicht aktiv. Gehen Sie bitte wie folgt vor:" + System.lineSeparator()+
                        " 1. Falls Sie Ihren Account noch nicht mit dem Code in der Email aktiviert haben," +System.lineSeparator()+
                        "     gehen Sie auf Registrieren->Ich habe einen Bestätigungscode und tun Sie es dort "+System.lineSeparator()+
                        " 2. Falls Ihr Account vorher aktiv war und diese Meldung nun auftaucht, wurde er"+System.lineSeparator()+
                        "     automatisch aufgrunde von Sicherheitsbedenken deaktiviert. Wenden Sie sich in"+System.lineSeparator()+
                        "     diesem Fall bitte an den Support. Diesen erreichen Sie unter info@noamo.de", "Account inaktiv", JOptionPane.INFORMATION_MESSAGE);
                if (Values.LOGIN_SAV_FILE.exists()) Values.LOGIN_SAV_FILE.delete();
                return false;
            }
            Values.username = username;
            Values.password = password;
            Values.name = re[0];
            Values.email = re[1];
            Values.created = re[3];
            Values.admin = Boolean.parseBoolean(re[2]);
            Util.saveStringToFile(Values.LOGIN_SAV_FILE, username + System.lineSeparator() + password);
            return true;
        } catch (IOException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Es konnte keine Verbindung zu dem Server hergestellt werden.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Interner Fehler! Bitte probieren Sie es später noch einmal ", "Fehler", JOptionPane.ERROR_MESSAGE);
            Values.LOGIN_SAV_FILE.delete();
            return false;
        }
    }

    public static void main(String[] args) {
        loadDarklefTheme();
        loadFont();
        checkArgs(args);
        VisualWrapper visualWrapper = new VisualWrapper();

        // Passende GUI starten (+prüfen, ob eine valide Anmeldung in login.sav vorliegt)
        if (!loadLogin()) visualWrapper.transitionTo(new LoginScreen());
        else visualWrapper.transitionTo(new HomeScreen());
    }

    public void transitionTo(Page newPage) {
        if (newPage instanceof HomeScreen) homeScreen = (HomeScreen)newPage;
        newPage.setContext(this);
        if (currentPage != null) currentPage.kill();
        currentPage = newPage;
        getContentPane().removeAll();
        getContentPane().setLayout((newPage.isMaximized() ? new BorderLayout() : new GridBagLayout()));
        getContentPane().repaint();
        getContentPane().add(newPage);
        if(getJMenuBar()!=null)getJMenuBar().removeAll();
        revalidate();
        setJMenuBar(creatJMenuBarFromCurrentPage());
        newPage.update();
        revalidate();
    }
}
