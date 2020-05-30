/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

import de.noamo.util.BasicNetwork;
import de.noamo.util.Page;
import de.noamo.util.Protocol;
import de.noamo.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * App für Mitarbeiter des Kundenservices.
 *
 * @author Noah Moritz Hölterhoff
 * @version 20.05.2020
 * @since 15.05.2020
 */
public class SupportApp extends Page {
    private final static Color ACCENT_COLOR = new Color(51, 51, 51);
    private final static ImageIcon BLANK_APP = Util.getImageIcon(SupportApp.class, "/blank_app.png", 20);
    private final static Color DARK_ACCENT_COLOR = new Color(41, 41, 41);
    private final static String HOST = "admin.noamo.de";
    private final static SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private final static File LOGIN_SAV_FILE = new File(System.getProperty("user.home") + File.separator + "noamo" + File.separator + "config" + File.separator + "login.sav");
    private final static SimpleDateFormat ONLINE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private final static int PORT = 49636;
    private final static HashMap<Integer, ImageIcon> allIcons = new HashMap<>();
    private Chat activeChat;
    private final ArrayList<Chat> activeChats = new ArrayList<>();
    private JScrollPane active_Software_Panel = null;
    private final ArrayList<Chat> allChats = new ArrayList<>();
    private static String[] allSoftwareProducts;
    private boolean appClosed = false;
    private JPanel chatBackgroundWrapper;
    private JPanel chatList;
    private JScrollPane chatScroll;
    private final Thread keepActiveThread;
    private JTextField messageTextField;
    private JButton softwareConfigButton;
    private String username, password;

    public SupportApp() {
        // Basic Setup - Fenstereinstellungen
        setMaximized(true);
        setExternalApp(true);
        setLayout(new BorderLayout());

        // Vorab Infos laden
        loadUsernameAndPassword();
        setupSoftwareAndIconList();

        // UI erstellen
        createLeftView(); // Linkes Panel
        createCenterView(); // Mittleres Panel

        keepActiveThread = new Thread(() -> {
            while (!appClosed) {
                try {
                    updateChatList();
                    //noinspection BusyWait
                    Thread.sleep(5000);
                } catch (Exception ignored) {}
            }
        });
        keepActiveThread.start();
    }

    private void addToChat(boolean external, String name, String online_Time, String text, JPanel context) {
        String date = online_Time;
        try {
            Date temp_date = ONLINE_DATE_FORMAT.parse(online_Time);
            date = LOCAL_DATE_FORMAT.format(temp_date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JLabel chatText = new JLabel("<html><b>" + name + "</b> (" + date + "):<br>" + text + "</html>");

        JPanel chatBubbel = new JPanel();
        chatBubbel.add(chatText);
        chatBubbel.setBorder(new LineBorder(ACCENT_COLOR, 5, true));
        chatBubbel.setBackground(ACCENT_COLOR);
        chatBubbel.setMaximumSize(new Dimension(500, (int) chatText.getPreferredSize().getHeight()));

        JPanel chatBubbelWrapper = new JPanel(new BorderLayout());
        chatBubbelWrapper.setBorder(new EmptyBorder(5, 0, 5, 0));
        chatBubbelWrapper.add(chatBubbel, (external ? BorderLayout.WEST : BorderLayout.EAST));
        context.add(chatBubbelWrapper);
        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Erstellt das mittler Panel der App.<br>
     * Das mittlere Panel enthält den Chat mit dem Nutzer.
     */
    private void createCenterView() {
        // Das Panel, dass alle Komponenten im Center-Bereich beinhaltet
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        add(centerPanel, BorderLayout.CENTER);

        // Der Titel der Software
        JLabel titel = new JLabel("Support App f\u00FCr Mitarbeiter");
        titel.setFont(new Font("Dialog", Font.BOLD, 22));
        titel.setBorder(new EmptyBorder(10, 10, 0, 10));
        titel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(titel, BorderLayout.NORTH);

        // Panel für die alle Komponenten im Bereich "Text eingeben", "Senden" etc.
        JPanel messageContainer = new JPanel(new BorderLayout(0, 0));
        messageContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        centerPanel.add(messageContainer, BorderLayout.SOUTH);

        // Text-Field für die Eingabe von Nachrichten in den aktuellen Chat
        messageTextField = new JTextField();
        messageTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) send();
            }
        });
        messageContainer.add(messageTextField, BorderLayout.CENTER);
        messageTextField.setColumns(10);

        // Senden Button
        JButton send = new JButton("Senden");
        send.addActionListener(e -> send());
        messageContainer.add(send, BorderLayout.EAST);

        // Wrapper, der das Scrollen im Chat ermöglicht
        chatScroll = new JScrollPane();
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.setBorder(new EmptyBorder(10, 10, 0, 10));
        centerPanel.add(chatScroll, BorderLayout.CENTER);

        // Hintergrund des Chats (Funktion: Verhindern, dass MessageBoxen gestreckt werden). Message Boxen werden
        // für jeden Chat auf ein neues JPanel gezeichnet. Dies hier ist nur ein Wrapper
        chatBackgroundWrapper = new JPanel(new BorderLayout());
        chatScroll.setViewportView(chatBackgroundWrapper);
    }

    /**
     * Erstellt das linke Panel der App.<br>
     * Das linke Panel enthält die List mit allen Kundenchats und Buttons für verschiedene Ansichten
     * im rechente Panel
     */
    private void createLeftView() {
        // Scroll-Pane, damit innerhalb der Chatliste gescrollt werden kann
        JScrollPane scrollForListofChats = new JScrollPane();
        scrollForListofChats.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForListofChats.setBorder(new MatteBorder(0, 0, 0, 5, ACCENT_COLOR));
        add(scrollForListofChats, BorderLayout.WEST);

        // Panel innerhalb des Scroll-Panes (Funktion: verhindern, dass bei wenig Chats, die Boxen gestreckt werden)
        JPanel leftAntiResizePanel = new JPanel(new BorderLayout());
        scrollForListofChats.setViewportView(leftAntiResizePanel);

        // Panel, dass die Liste an Chats beinhaltet
        chatList = new JPanel(new GridLayout(0, 1, 0, 5));
        leftAntiResizePanel.add(chatList, BorderLayout.NORTH);

        // Wrapper Panel für die Optionen (Funktion: beinhaltet die angezeigte Border)
        JPanel optionWrapper = new JPanel(new BorderLayout());
        optionWrapper.setBorder(new MatteBorder(5, 0, 0, 0, ACCENT_COLOR));
        leftAntiResizePanel.add(optionWrapper, BorderLayout.SOUTH);

        // Panel auf dem die Button liegen (beihaltet Abstandspolicy für die Button)
        JPanel options = new JPanel(new GridLayout(0, 1, 5, 5));
        options.setBorder(new EmptyBorder(5, 5, 5, 5));
        optionWrapper.add(options, BorderLayout.CENTER);

        // Button für die Verwaltung des Software, die die Kunden besitzer
        softwareConfigButton = new JButton("Softwareverwaltung");
        softwareConfigButton.addActionListener(e -> {
            if (active_Software_Panel != null) {
                remove(active_Software_Panel);
                active_Software_Panel = null;
                revalidate();
                repaint();
            } else updateSoftwareScreen();
        });
        options.add(softwareConfigButton);

        // Button für das Ändern der Email-Adresse des Nutzers
        JButton changeEmail = new JButton("Email \u00E4ndern");
        options.add(changeEmail);
    }

    private Chat findChat(String userid) {
        int int_id = Integer.parseInt(userid);
        for (Chat c : allChats) {
            if (c.userid == int_id) return c;
        }
        return null;
    }

    @Override
    public void kill() {
        appClosed = true;
        keepActiveThread.interrupt();
    }

    private boolean loadIconFromIconFolder(String id, File iconFolder) {
        File iconMetaFile = new File(iconFolder.getAbsolutePath() + File.separator + id + ".meta");
        File iconFile = new File(iconFolder.getAbsolutePath() + File.separator + id + ".jpeg");

        // Prüfen, ob die Datei existiert
        if (!iconFile.exists() || !iconMetaFile.exists()) return false;

        try {
            // Bild auf Aktualität prüfen
            String metaContent = Util.loadTxtFileIntoString(iconMetaFile);
            if (System.currentTimeMillis() - Long.parseLong(metaContent) > 259200000) { // Falls das Bild zu alt ist
                iconFile.delete();
                iconMetaFile.delete();
                return false;
            }

            // Bild laden
            allIcons.put(Integer.parseInt(id), new ImageIcon(ImageIO.read(iconFile)));
            return true;
        } catch (Exception ex) {return false;}
    }

    private void loadUsernameAndPassword() {
        try {
            String temp = Util.loadTxtFileIntoString(LOGIN_SAV_FILE);
            String[] temp_split = temp.split(System.lineSeparator());
            username = temp_split[0];
            password = temp_split[1];
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(getContext(), "Benutzername und Passwort konnten nicht geladen werden:" + System.lineSeparator() + e.getMessage());
        }
    }

    private void send() {
        new Thread(() -> {
            if (messageTextField.getText() == null || messageTextField.getText().equals("") || activeChat == null)
                return;
            String[] re = simplifiedRequest(new String[]{Protocol.TEXT_FROM_SERVICE, "" + activeChat.userid, messageTextField.getText()});
            if (re == null) return;
            messageTextField.setText("");
            keepActiveThread.interrupt();
        }).start();
    }

    /**
     * Aktualisiert in einem neuen Thread Icon- und Softwareliste
     */
    private void setupSoftwareAndIconList() {
        new Thread(() -> {
            allSoftwareProducts = simplifiedRequest(new String[]{Protocol.GET_SOFTWARE_OVERVIEW}); // Alle Software-Produkte abfragen
            if (allSoftwareProducts == null) return; // Falls ein Fehler auftritt

            // Icon-Ordner erstellen
            while (getAppRoot() == null) {} // Warte darauf, dass App-Root gesetzt wird
            File imagesFolder = new File(getAppRoot().getAbsolutePath() + File.separator + "icons" + File.separator);
            imagesFolder.mkdirs();

            // ArrayList für Icons erstellen, die nicht geladen werden
            ArrayList<String[]> failedToLoadIcons = new ArrayList<>();

            // Icons laden


            for (int i = 1; i < allSoftwareProducts.length; i++) { // Für alle Software-Produkte die Icons laden
                String[] split = allSoftwareProducts[i].split(Protocol.SPLIT);
                if (!loadIconFromIconFolder(split[0], imagesFolder)) failedToLoadIcons.add(split);
            }

            // Icons herunterladen, die nicht geladen werden konnten
            for (String[] s : failedToLoadIcons) {
                System.out.println(Arrays.toString(s));
                try {
                    // Bild herunterladen und speichern
                    File imageSave = new File(imagesFolder.getAbsolutePath() + File.separator + s[0] + ".jpeg");
                    ImageIcon downloadedIcon = new ImageIcon(new ImageIcon(new URL(s[2])).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
                    ImageIO.write(imageIconToRenderedImage(downloadedIcon), "JPEG", imageSave);

                    // Zeitpunkt der Erstellung notieren
                    File metaSave = new File(imagesFolder.getAbsolutePath() + File.separator + s[0] + ".meta");
                    Util.saveStringToFile(metaSave, String.valueOf(System.currentTimeMillis()));

                    // Bild In HashMap ablegen
                    allIcons.put(Integer.parseInt(s[0]), downloadedIcon);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private RenderedImage imageIconToRenderedImage(ImageIcon imageIcon){
        Image image = imageIcon.getImage();
        RenderedImage rendered = null;
        if (image instanceof RenderedImage) {
            rendered = (RenderedImage) image;
        } else {
            BufferedImage buffered = new BufferedImage(
                    imageIcon.getIconWidth(),
                    imageIcon.getIconHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g = buffered.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            rendered = buffered;
        }
        return rendered;
    }

    private String[] simplifiedRequest(String[] content) {
        String[] request = new String[content.length + 2];
        request[0] = username;
        request[1] = password;
        System.arraycopy(content, 0, request, 2, content.length);
        try {
            String[] re = BasicNetwork.sendRequest(HOST, PORT, request);
            //noinspection ConstantConditions
            switch (re[0]) {
                case Protocol.OK:
                    return re;
                case Protocol.LOGIN_FAILED:
                    JOptionPane.showMessageDialog(getContext(), "Die Anmeldedaten sind falsch. Bitte starten Sie den Launcher neu und wiederholen Sie die Anmeldung", "Fehler", JOptionPane.ERROR_MESSAGE);
                    requestClose();
                    return null;
                case Protocol.NO_ADMIN:
                    JOptionPane.showMessageDialog(getContext(), "Sie müssen Adminrechte haben um diese Aktion auszuführen", "Fehler", JOptionPane.ERROR_MESSAGE);
                    requestClose();
                    return null;
                default:
                    JOptionPane.showMessageDialog(getContext(), re[0], "Fehler", JOptionPane.ERROR_MESSAGE);
                    return null;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getContext(), "Es konnte keine Verbindung zum Server hergstellt werden", "Fehler", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void updateChatList() {
        updateMessages();
        for (Chat chat : allChats) {
            if (activeChats.contains(chat)) continue;
            chatList.add(chat.chatBox);
            activeChats.add(chat);
        }
        revalidate();
        repaint();
        if (activeChat != null) activeChat.updateChat();
    }

    private void updateMessages() {
        String[] re = simplifiedRequest(new String[]{Protocol.GET_ALL_SUPPORT_CHATS});
        if (re == null) return;

        // Alle Nachrichten durchlaufen
        messageLoop:
        for (int i = 1; i < re.length; i++) {
            String[] split = re[i].split(Protocol.SPLIT, 8);

            // Chat finden
            Chat chat = findChat(split[1]);
            if (chat == null) {
                chat = new Chat(split[1], split[4], split[6], split[5]);
                allChats.add(chat);
            }

            // Nachricht einfügen
            int messageid = Integer.parseInt(split[0]);
            for (Message m : chat.messages) {
                if (m.messageid == messageid) continue messageLoop;
            }
            chat.messages.add(new Message(split[2], split[7], messageid, split[3]));
        }
    }

    private void updateSoftwareScreen() {
        new Thread(() -> {
            try {
                // Panelöffnung prüfen und "Laden" anzeigen
                if (activeChat == null) return;
                if (active_Software_Panel != null) {
                    remove(active_Software_Panel);
                    active_Software_Panel = null;
                    revalidate();
                    repaint();
                }
                SwingUtilities.invokeLater(() -> {
                    softwareConfigButton.setText("Bitte warten...");
                    softwareConfigButton.setEnabled(false);
                });

                // Infos vom Server laden
                String[] reuser = simplifiedRequest(new String[]{Protocol.GET_SOFTWARE_LIST, String.valueOf(activeChat.userid)});
                if (reuser == null || allSoftwareProducts == null) return;

                // UI erstellen
                active_Software_Panel = new JScrollPane();
                active_Software_Panel.setBorder(new MatteBorder(0, 5, 0, 0, ACCENT_COLOR));
                active_Software_Panel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                add(active_Software_Panel, BorderLayout.EAST);

                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBorder(new EmptyBorder(5, 0, 5, 0));
                active_Software_Panel.setViewportView(wrapper);

                JPanel innerPanel = new JPanel(new GridLayout(0, 1));
                wrapper.add(innerPanel, BorderLayout.NORTH);

                // Software des Nutzer in in ArrayList laden
                ArrayList<Integer> ownedSoftware = new ArrayList<>();
                for (int i = 1; i < reuser.length; i++) {
                    String[] split = reuser[i].split(Protocol.SPLIT, 2);
                    ownedSoftware.add(Integer.parseInt(split[0]));
                }

                // Alle Software-Produkte laden
                for (int i = 1; i < allSoftwareProducts.length; i++) {
                    String[] split = allSoftwareProducts[i].split(Protocol.SPLIT, 3); // Nachricht teilen
                    boolean owned = ownedSoftware.contains(Integer.parseInt(split[0])); // Ownership festellen

                    // UI für einzeldens Programm erstellen
                    JPanel app = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    app.setBorder(new EmptyBorder(0, 0, 0, 10));
                    JCheckBox checkBox = new JCheckBox(split[1].length() > 13 ? split[1].substring(0, 12) + "..." : split[1]);
                    checkBox.setSelected(owned);
                    checkBox.addItemListener(e -> new Thread(() -> { // Auf Änderung reagieren
                        checkBox.setEnabled(false);
                        String[] re = simplifiedRequest(new String[]{Protocol.SET_OWNERSHIP, String.valueOf(activeChat.userid), split[0], String.valueOf(checkBox.isSelected())});

                        ItemListener temp_listener = checkBox.getItemListeners()[0];
                        checkBox.removeItemListener(temp_listener);
                        if (re == null) checkBox.setSelected(!checkBox.isSelected());
                        checkBox.addItemListener(temp_listener);

                        checkBox.setEnabled(true);
                    }).start());

                    // Icon der App laden
                    JLabel icon = new JLabel();
                    ImageIcon imageIcon = allIcons.get(Integer.parseInt(split[0]));
                    if (imageIcon == null) icon.setIcon(BLANK_APP);
                    else icon.setIcon(imageIcon);

                    // UI einfügen
                    app.add(icon);
                    app.add(checkBox);
                    innerPanel.add(app);
                }
                revalidate();
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(getContext(), "Die Infos können nicht geladen werden:" + System.lineSeparator() + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    softwareConfigButton.setText("Softwareverwaltung");
                    softwareConfigButton.setEnabled(true);
                });
            }
        }).start();
    }

    private class Chat {
        private final JPanel chatBox;
        private final JPanel detailView = new JPanel(new GridLayout(0, 1));
        private final ArrayList<Message> messages = new ArrayList<>();
        private final String name;
        private final ArrayList<Message> paintedMessages = new ArrayList<>();
        private final int userid;

        public Chat(String userid, String name, String email, String username) {
            this.userid = Integer.parseInt(userid);
            this.name = name;

            chatBox = new JPanel(new GridLayout(0, 1, 0, 0));
            chatBox.add(new JLabel("  " + name));
            chatBox.add(new JLabel("  " + userid + "-" + username));
            chatBox.add(new JLabel("  " + email));
            chatBox.setMaximumSize(new Dimension(120, (int) chatBox.getPreferredSize().getHeight()));
            chatBox.setBorder(new EmptyBorder(2, 0, 2, 20));
            chatBox.setBackground(ACCENT_COLOR);
            chatBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    chatBox.setBackground(DARK_ACCENT_COLOR);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    chatBox.setBackground(ACCENT_COLOR);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    activeChat = Chat.this;
                    if (active_Software_Panel != null) updateSoftwareScreen();
                    updateChat();
                    chatBackgroundWrapper.removeAll();
                    chatBackgroundWrapper.revalidate();
                    chatBackgroundWrapper.add(detailView, BorderLayout.SOUTH);
                    chatBackgroundWrapper.revalidate();
                    chatBackgroundWrapper.repaint();

                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = chatScroll.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });
                }
            });
        }

        private void updateChat() {
            for (Message m : messages) {
                if (paintedMessages.contains(m)) continue;
                addToChat(m.external, m.external ? name : "Kundenservice", m.timestamp, m.content, detailView);
                paintedMessages.add(m);
            }
        }
    }

    private static class Message {
        private final boolean external;
        private final int messageid;
        private final String timestamp, content;

        public Message(String timestamp, String content, int messageid, String external) {
            this.content = content;
            this.timestamp = timestamp;
            this.messageid = messageid;
            this.external = Boolean.parseBoolean(external);
        }
    }
}
