/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.launcher;

import de.noamo.util.BasicNetwork;
import de.noamo.util.Page;
import de.noamo.util.Protocol;
import de.noamo.util.Util;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Klasse für den HomeScreen des NOAMO Launchers
 *
 * @author Noah Moritz Hölterhoff
 * @version 05.05.2020
 * @since 05.05.2020
 */
public class HomeScreen extends Page {
    private final ArrayList<JButton> loadedCustomApps = new ArrayList<>();
    private final JPanel appdrawer = new JPanel();
    private JLabel l_welcome;

    /**
     * Erstellt einen neuen HomeScreen und lädt alle Apps vom Server
     */
    public HomeScreen() {
        super();
        setLayout(new BorderLayout(0, 0));
        setExternalApp(false);
        setMaximized(true);
        createMenue();
        createContent();
    }

    /**
     * Erstellt den Ihalt der Page
     */
    private void createContent() {
        // Wilkommensbotschaft oben im Panel
        l_welcome = new JLabel("<html>Wilkommen " + Values.name + "</html>");
        l_welcome.setFont(new Font("Futura", Font.BOLD, 25));
        l_welcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(l_welcome, BorderLayout.NORTH);

        // Panel, worin alle Apps eingefügt werden
        appdrawer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        add(appdrawer, BorderLayout.CENTER);

        // Standart-Apps hinzufügen
        JButton my_account = new JButton("Mein Konto");
        my_account.setIcon(Util.getImageIcon(HomeScreen.class, "/meinkonto.png", 100));
        my_account.addActionListener(e -> Values.currentVisualWrapper.transitionTo(new MyAccount(this)));
        setupAppTile(my_account);

        JButton service = new JButton("Kundenservice");
        service.setIcon(Util.getImageIcon(HomeScreen.class, "/service.png", 100));
        service.addActionListener(e -> Values.currentVisualWrapper.transitionTo(new SupportChatScreen(this)));
        setupAppTile(service);

        // Lädt die aktivierten Apps vom Server
        loadMyAppsFromServerIntoAppdrawer();
    }

    /**
     * Passt einen JButton so an, dass er als AppKachel funktioniert. Diese Kachel wird dann auch dem App-Drawer hinzugefügt
     *
     * @param appTile Der Button (benötigt bereits ein Icon, einen Text und einen Actionlistener)
     */
    private void setupAppTile(JButton appTile){
        appTile.setVerticalTextPosition(SwingConstants.BOTTOM);
        appTile.setHorizontalTextPosition(SwingConstants.CENTER);
        appTile.setFocusPainted(false);
        appTile.setMaximumSize(new Dimension(137,137));
        appTile.setMinimumSize(new Dimension(137,137));
        appTile.setPreferredSize(new Dimension(137,137));
        appdrawer.add(appTile);
    }

    /**
     * Erstellt das Menü des HomeScreens
     */
    private void createMenue() {
        JMenuItem[] menuItems = new JMenuItem[3];

        // Abmelden Menü
        JMenu abmelden = new JMenu("Abmelden");
        abmelden.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = JOptionPane.showConfirmDialog(Values.currentVisualWrapper, "Wollen Sie sich wirklich abmelden?", "Abmeldung", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (i == JOptionPane.YES_OPTION) {
                    Values.LOGIN_SAV_FILE.delete();
                    Values.currentVisualWrapper.transitionTo(new LoginScreen());
                }
            }
        });
        menuItems[0] = abmelden;

        // Apps aktualiseren Menü
        JMenu refresh = new JMenu("Aktualisieren");
        refresh.setToolTipText("Aktualisiert die Übersicht aller Apps, die Sie besitzen");
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshApps();
            }
        });
        menuItems[1] = refresh;

        // Hilfe Menü
        JMenu hilfe = new JMenu("Hilfe");
        JMenuItem webseite = new JMenuItem("Webseite");
        JMenuItem contact = new JMenuItem("Kontaktformular");
        webseite.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://noamo.de"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        });
        contact.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://noamo.de/contact.html"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        });

        hilfe.add(webseite);
        hilfe.add(contact);
        menuItems[2] = hilfe;

        // Anwenden
        setMenuItems(menuItems);
    }

    /**
     * Lädt eine App von einem Downloadlink herunter (App muss eine .zip Datei sein) und entpackt Sie in das
     * angegeben App-Stammverzeichnis. Falls dieses noch nicht existiert, wird es erstellt
     *
     * @param app_root Stammverzeichnis der App
     * @param downloadLink Downloadlink für die App
     * @throws IOException Falls beim Entpacken/Herunterladen/etc. ein Problem auftritt
     */
    private void downloadApp(File app_root, String downloadLink) throws IOException {
        app_root.mkdirs();
        File tempZipFile = File.createTempFile(UUID.randomUUID().toString(), ".zip");
        Util.download(downloadLink, tempZipFile);
        ZipFile zipFile = new ZipFile(tempZipFile);
        zipFile.extractAll(app_root.getAbsolutePath());
    }

    /**
     * Startet eine App
     *
     * @param appinfos Die Infos zu der App (In der Reihenfolge, die der Server ausgibt)
     * @param button Der Button im Appdrawer, der zu der App dazugehört
     */
    private void startApp(String[] appinfos, JButton button) {
        new Thread(() -> {
            // Speichert den Inhalt der Kachel zwischen
            Icon temp_icon = button.getIcon();
            String temp_name = button.getText();

            try {
                // Kachel ändern, um Start des Start-Prozesses zu signalisieren
                EventQueue.invokeLater(() -> {
                    button.setEnabled(false);
                    button.setText("Bitte warten");
                    button.setIcon(Util.getImageIcon(HomeScreen.class, "/loading.png", 100));
                });

                // Die wichtigsten Pfade in Files zwischenspeichern
                File app_root = new File(Values.INSTALL_ROOT.getAbsolutePath() + File.separator + appinfos[0] + File.separator);
                File version_file = new File(app_root.getAbsolutePath() + File.separator + "version.txt");

                // App herunterladen / Update machen
                if (!version_file.exists() || Double.parseDouble(Util.loadTxtFileIntoString(version_file)) < Double.parseDouble(appinfos[2])) {
                    EventQueue.invokeLater(() -> {
                        button.setText("L\u00E4dt herunter");
                        button.setIcon(Util.getImageIcon(HomeScreen.class, "/download.png", 100));
                    });
                    downloadApp(app_root, appinfos[5]);
                    Util.saveStringToFile(version_file, appinfos[2]);
                }

                // App starten
                if (appinfos[4].endsWith(".class")) { // Native App
                    try {
                        System.out.println("file:///" + app_root.getAbsolutePath().replaceAll(Matcher.quoteReplacement(File.separator), "/") + "/" + appinfos[4]);
                        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file:///" + app_root.getAbsolutePath() + "/")});
                        @SuppressWarnings("rawtypes") Class app = urlClassLoader.loadClass(appinfos[4].substring(0, appinfos[4].length() - 6));
                        Page p = (Page) app.newInstance();
                        p.setCloseListener(() -> Values.currentVisualWrapper.transitionTo(this));
                        p.setAppRoot(app_root);
                        Values.currentVisualWrapper.transitionTo(p);
                    } catch (Exception ex) { // Falls die Klasse nicht geladen werden konnte
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper,"Die App konnte nicht gestartet werden:"+System.lineSeparator()+ex.getMessage(),"Fehler",JOptionPane.ERROR_MESSAGE);
                    }
                } else Values.processes.add(new ProcessBuilder(app_root.getAbsolutePath() + File.separator + appinfos[4]).start()); // Normale App
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Die App konnte nicht heruntergeladen werden. Bitte überpüfen Sie Ihre Internetverbindung!" + System.lineSeparator() + "Sollte dies das Problem nicht beheben, wenden Sie sich bitte an den Hersteller dieser App." + System.lineSeparator() + "Dieser (und nicht NOAMO-Tech) kümmert sich um die Bereitsellung des Downloads", "Fehler", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Fehler bei der Installation der App. Wenn das Problem weiterhin auftritt, wenden Sie sich bitte an den Kundenservice" + System.lineSeparator() + "und an den Entwickler dieser App. In den meisten Fällen ist das Problem allerdings innerhalb wenigen Minuten automatisch behoben", "Fehler", JOptionPane.ERROR_MESSAGE);
            } finally {
                EventQueue.invokeLater(() -> {
                    button.setIcon(temp_icon);
                    button.setText(temp_name);
                    button.setEnabled(true);
                });
            }
        }).start();
    }

    private void loadMyAppsFromServerIntoAppdrawer() {
            try {
                String[] re = BasicNetwork.sendRequest(Values.HOST, Values.PORT, new String[]{Values.username, Values.password, Protocol.GET_SOFTWARE_LIST});
                //noinspection ConstantConditions
                switch (re[0]) {
                    case Protocol.OK:
                        for (int i = 1; i < re.length; i++) {
                            String[] temp_app_infos = re[i].split(Protocol.SPLIT);
                            JButton temp_app_button = new JButton(temp_app_infos[1]);
                            temp_app_button.addActionListener(e -> startApp(temp_app_infos, temp_app_button));
                            setupAppTile(temp_app_button);
                            loadedCustomApps.add(temp_app_button);

                            // LadeProzess des Icons
                            temp_app_button.setIcon(Util.getImageIcon(HomeScreen.class, "/blank_app.png", 100));
                            new Thread(() -> {
                                try {
                                    ImageIcon temp_icon = new ImageIcon(new URL(temp_app_infos[6]));
                                    ImageIcon scaled = new ImageIcon(temp_icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                                    temp_app_button.setIcon(scaled);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                        break;
                    case Protocol.LOGIN_FAILED:
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Die Passwort ist nicht mehr aktuell. Bitte schließen Sie das Programm" + System.lineSeparator() + "und melden Sie sich erneut an.", "Fehler", JOptionPane.ERROR_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, re[0], "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Es konnte keine Verbindung zum Server hergestellt werden", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
    }

    /**
     * Aktualisiert alle Apps im Appdrawer
     */
    private void refreshApps(){
        // Alle Custom Apps entfernen
        for (JButton button:loadedCustomApps){
            appdrawer.remove(button);
        }
        loadedCustomApps.clear();
        appdrawer.revalidate();
        appdrawer.repaint();

        // Apps neu laden
        loadMyAppsFromServerIntoAppdrawer();
    }

    @Override
    public void update() {
        l_welcome.setText("<html>Wilkommen " + Values.name + "</html>");
    }
}
