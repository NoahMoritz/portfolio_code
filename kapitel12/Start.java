package de.noamo.sql;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Noah Moritz Hölterhoff
 */
public class Start {

    public static void main(String[] args) {
        new ConnectToDBGUI();
    }

    private static class ConnectToDBGUI extends JFrame {
        public ConnectToDBGUI() {
            setTitle("Verbindung");
            setIconImage(Toolkit.getDefaultToolkit().getImage(Start.class.getResource("/com/sun/java/swing/plaf/motif/icons/DesktopIcon.gif")));
            setResizable(false); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setBounds(100, 100, 240, 245);
            setLocationRelativeTo(null);
            JPanel contentPane = new JPanel();
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(null);

            JLabel lblVerbindungZurDtanebank = new JLabel("Verbindung herstellen");
            lblVerbindungZurDtanebank.setBounds(5, 5, 225, 31);
            lblVerbindungZurDtanebank.setHorizontalAlignment(SwingConstants.CENTER);
            lblVerbindungZurDtanebank.setFont(new Font("Tahoma", Font.PLAIN, 20));
            contentPane.add(lblVerbindungZurDtanebank);

            JTextField host = new JTextField();
            host.setBounds(5, 55, 156, 20);
            contentPane.add(host);
            host.setColumns(10);

            JLabel lblHostname = new JLabel("Hostname:");
            lblHostname.setBounds(5, 40, 81, 14);
            contentPane.add(lblHostname);

            JTextField port = new JTextField();
            port.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent evt) {
                    char vchar = evt.getKeyChar();
                    if (!(Character.isDigit(vchar)) || (vchar == KeyEvent.VK_BACK_SPACE) || (vchar == KeyEvent.VK_DELETE))
                        evt.consume();
                }
            });
            port.setText("3306");
            port.setBounds(170, 55, 59, 20);
            contentPane.add(port);

            JLabel lblPort = new JLabel("Port:");
            lblPort.setBounds(170, 40, 46, 14);
            contentPane.add(lblPort);

            JTextField username = new JTextField();
            username.setBounds(5, 125, 225, 20);
            contentPane.add(username);
            username.setColumns(10);

            JLabel lblBenutzername = new JLabel("Benutzername:");
            lblBenutzername.setBounds(5, 110, 111, 14);
            contentPane.add(lblBenutzername);

            JPasswordField password = new JPasswordField();
            password.setBounds(5, 160, 225, 20);
            contentPane.add(password);

            JLabel lblPasswort = new JLabel("Passwort:");
            lblPasswort.setBounds(5, 145, 81, 14);
            contentPane.add(lblPasswort);

            JButton connect = new JButton("Verbinden"); connect.setBounds(5, 185, 225, 20); contentPane.add(connect);

            JTextField dataBase = new JTextField();
            dataBase.setBounds(5, 90, 225, 20);
            contentPane.add(dataBase);
            dataBase.setColumns(10);

            JLabel lblDatenbank = new JLabel("Datenbank:");
            lblDatenbank.setBounds(5, 75, 156, 16);
            contentPane.add(lblDatenbank);

            connect.addActionListener(evt -> {
                try {
                    new GUI(new DataBase(host.getText(), Integer.parseInt(port.getText()), username.getText(), password.getText(), dataBase.getText()));
                    dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            setVisible(true);
        }
    }
}
