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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * Beinhaltet alle Pages für die Anmeldung (Login, Registrierung, Validierung)
 *
 * @author Noah Moritz Hölterhoff
 * @version 03.05.2020
 * @since 22.03.2020
 */
public class LoginScreen extends Page {
    private final JLabel l_ueberschrift = new JLabel();
    private final JButton login = new JButton("Anmelden");
    private final JPasswordField password = new JPasswordField();
    private final JButton register = new JButton("Registrieren");
    private final JTextField t_username = new JTextField();

    /**
     * Der ANmeldetscreen
     */
    public LoginScreen() {
        // PanelEinstellungen
        super();
        setMaximumSize(new Dimension(438, 229));
        setPreferredSize(new Dimension(438, 229));
        setExternalApp(false);
        setLayout(null);

        // Listener (Vorbereitung)
        KeyListener enter_listener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) login();
            }
        };

        // Überschrift - Label
        l_ueberschrift.setBounds(0, 0, 438, 30);
        l_ueberschrift.setText("Anmelden");
        l_ueberschrift.setHorizontalTextPosition(SwingConstants.RIGHT);
        l_ueberschrift.setHorizontalAlignment(SwingConstants.CENTER);
        l_ueberschrift.setFont(new Font("Dialog", Font.BOLD, 22));
        add(l_ueberschrift);

        // Infolabel
        JLabel info = new JLabel("Klicken Sie auf 'Registrieren', wenn Sie einen neuen Account erstellen m\u00F6chten");
        info.setHorizontalAlignment(SwingConstants.CENTER);
        info.setVerticalAlignment(SwingConstants.CENTER);
        info.setFont(new Font("Dialog",Font.PLAIN,11));
        info.setBounds(0,30,438,20);
        add(info);

        // Benutzername - Input
        t_username.setBounds(164, 56, 261, 33);
        t_username.addKeyListener(enter_listener);
        add(t_username);
        // Benutzername - Label
        JLabel l_username = new JLabel("Benutzername");
        l_username.setBounds(8, 56, 149, 33);
        add(l_username);

        // Passwort - Input
        password.setBounds(164, 104, 261, 33);
        password.addKeyListener(enter_listener);
        add(password);
        // Passwort - Label
        JLabel l_password = new JLabel("Passwort");
        l_password.setBounds(8, 104, 149, 33);
        add(l_password);

        // Login - Button
        login.setBounds(224, 152, 201, 33);
        login.setMargin(new Insets(2, 2, 2, 2));
        login.addActionListener(evt -> login());
        login.addKeyListener(enter_listener);
        add(login);

        // Register - Button
        register.setBounds(8, 152, 201, 33);
        register.setMargin(new Insets(2, 2, 2, 2));
        register.addActionListener(evt -> Values.currentVisualWrapper.transitionTo(new RegisterScreen(this)));
        add(register);
    }

    /**
     * Meldete den Benutzer mit den eingegeben Daten im System an.
     * Wenn die Anmeldung erfoglreich war, wird {@link } geöffnet
     */
    public void login() {
        String usernameString = t_username.getText(), passwordString = new String(password.getPassword());

        t_username.setEditable(false);
        password.setEditable(false);
        login.setEnabled(false);
        register.setEnabled(false);
        l_ueberschrift.setText("Bitte warten...");

        new Thread(() -> {
            if (VisualWrapper.login(usernameString, passwordString)) {
                Values.currentVisualWrapper.transitionTo(new HomeScreen());
            } else {
                t_username.setEditable(true);
                password.setEditable(true);
                login.setEnabled(true);
                register.setEnabled(true);
                l_ueberschrift.setText("Bei NOAMO anmelden");
            }
        }).start();
    }

    /**
     * Ermöglicht das Registrieren eines Accounts bei NOAMO Tech
     */
    public static class RegisterScreen extends Page {
        private final JTextField email;
        private final JLabel l_titel;
        private final JTextField name;
        private final JPasswordField password;
        private final JPasswordField passwordR;
        private final JButton register, iHaveAKey;
        private final JTextField username;

        public RegisterScreen(Page last) {
            super();
            setLast(last);

            // Fenstereinstellungen
            setMinimumSize(new Dimension(450, 320));
            setPreferredSize(new Dimension(450, 320));
            setExternalApp(false);
            setLayout(null);

            // Listener (Vorbereitung)
            KeyListener enter_listener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) register();
                }
            };

            name = new JTextField();
            name.setToolTipText("Bitte geben Sie Ihren vollst\u00E4ndigen Namen (Vor- und Nachname) ein");
            name.setBounds(201, 56, 239, 33);
            name.addKeyListener(enter_listener);
            add(name);
            name.setColumns(10);

            JLabel l_name = new JLabel("Vollst\u00E4ndiger Name");
            l_name.setBounds(10, 56, 181, 33);
            add(l_name);

            email = new JTextField();
            email.setToolTipText("Ihre Email-Adresse (Sie bekommen gleich einen Best\u00E4tigunscode zugeschickt)");
            email.setColumns(10);
            email.addKeyListener(enter_listener);
            email.setBounds(201, 100, 239, 33);
            add(email);

            JLabel l_email = new JLabel("Email-Adresse");
            l_email.setBounds(10, 100, 181, 33);
            add(l_email);

            JLabel l_password = new JLabel("Passwort");
            l_password.setBounds(10, 188, 181, 33);
            add(l_password);

            password = new JPasswordField();
            password.setToolTipText("Verwenden Sie bitte ein sicheres Passwort mit min. 8 Zeichen");
            password.setBounds(201, 188, 239, 33);
            password.addKeyListener(enter_listener);
            add(password);

            JLabel l_passwordR = new JLabel("Passwort wiederholen");
            l_passwordR.setBounds(10, 232, 181, 33);
            add(l_passwordR);

            passwordR = new JPasswordField();
            passwordR.addKeyListener(enter_listener);
            passwordR.setToolTipText("Wiederholen Sie Ihr Passwort");
            passwordR.setBounds(201, 232, 239, 33);
            add(passwordR);

            username = new JTextField();
            username.setToolTipText("Ein Benutzername (A-Z, a-z, 0-9, .-_)");
            username.addKeyListener(enter_listener);
            username.setBounds(201, 144, 239, 33);
            add(username);

            JLabel lblBenutzername_1 = new JLabel("Benutzername");
            lblBenutzername_1.setBounds(10, 144, 181, 33);
            add(lblBenutzername_1);

            register = new JButton("Registrieren");
            register.addActionListener(e -> register());
            register.setToolTipText("Registrierung abschlie\u00DFen und Konto erstellen");
            register.setBounds(305, 276, 135, 33);
            add(register);

            iHaveAKey = new JButton("Ich habe einen Best\u00E4tigunscode");
            iHaveAKey.setToolTipText("Falls Sie bereits einen Best\u00E4tigungscode bekommen haben, k\u00F6nnen Sie ihn hier eingeben");
            iHaveAKey.setBounds(10, 276, 285, 33);
            iHaveAKey.addActionListener(e -> Values.currentVisualWrapper.transitionTo(new ValidationScreen(getLast(), null, null, null)));
            add(iHaveAKey);

            l_titel = new JLabel("Registrieren");
            l_titel.setHorizontalAlignment(SwingConstants.CENTER);
            l_titel.setFont(new Font("Dialog", Font.BOLD, 22));
            l_titel.setBounds(10, 11, 430, 30);
            add(l_titel);
        }

        @SuppressWarnings("ConstantConditions")
        private void register() {
            if (name.getText().length() < 3)
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Bitte geben Sie Ihren vollstädnigen Namen (Vor- und Nachname) ein ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (!email.getText().matches("^(.+)@(.+)$"))
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Bitte geben Sie eine gültige Email-Adresse ein ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (username.getText().length() < 5 || !username.getText().matches("^[A-Za-z0-9._-]*$"))
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, "Verwenden Sie bitte nur gültige Zeichen in Ihrem Benutzernamen (A-Z, a-z, 0-9, -_.) ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (new String(password.getPassword()).length() < 8)
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Verwenden Sie bitte ein Passwort mit mindestens 8 Zeichen ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (new String(password.getPassword()).length() < 8)
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Verwenden Sie bitte ein Passwort mit mindestens 8 Zeichen ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (!new String(password.getPassword()).equals(new String(passwordR.getPassword())))
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Die Passwörter stimmen nicht überein ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else {
                setUIActiavtion(false);
                new Thread(() -> {
                    try {
                        String[] re = BasicNetwork.sendRequest(Values.HOST, Values.PORT, new String[]{"REGISTER", username.getText(), new String(password.getPassword()), name.getText(), email.getText()});
                        switch (re[0]) {
                            case Protocol.OK:
                                Values.currentVisualWrapper.transitionTo(new ValidationScreen(getLast(), email.getText(), new String(password.getPassword()), username.getText()));
                                break;
                            case Protocol.USERNAME_ALREADY_IN_USE:
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Ihr gewünschter Benutzername wird bereits verwendet ", "Fehler", JOptionPane.ERROR_MESSAGE);
                                break;
                            case Protocol.EMAIL_ALREADY_IN_USE:
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Es existiert bereits ein Konto mit dieser EMail-Adresse ", "Fehler", JOptionPane.ERROR_MESSAGE);
                                break;
                            default:
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, re[0], " Fehler ", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Keine Verbindung zum Server! ", "Fehler", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Interner Fehler ", "Fehler", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setUIActiavtion(true);
                    }
                }).start();
            }
        }

        private void setUIActiavtion(boolean b) {
            l_titel.setText((b ? "Registrieren" : "Bitte warten..."));
            name.setEditable(b);
            username.setEditable(b);
            email.setEditable(b);
            password.setEditable(b);
            passwordR.setEditable(b);
            register.setEnabled(b);
            iHaveAKey.setEnabled(b);
        }
    }

    /**
     * Ist für die Darstellung eines Screens für die Eingabe eines Aktivierungscodes verantwortlich
     */
    public static class ValidationScreen extends Page {
        private final JTextField code;
        private JTextField email;
        private final JButton finishValidation;
        private final boolean full;
        private final JLabel l_titel;
        private JPasswordField password;
        private final String pre_email;
        private final String pre_password;
        private final String pre_username;


        public ValidationScreen(Page loginScreen, String pre_email, String pre_password, String pre_username) {
            super();
            setLast(loginScreen);
            full = pre_email == null;
            this.pre_email = pre_email;
            this.pre_password = pre_password;
            this.pre_username = pre_username;

            // Fenstereinstellungen
            setMinimumSize(new Dimension(450, (full ? 230 : 140)));
            setPreferredSize(new Dimension(450, (full ? 230 : 140)));
            setExternalApp(false);
            setLayout(null);

            l_titel = new JLabel("Account best\u00E4tigen");
            l_titel.setFont(new Font("Dialog", Font.BOLD, 22));
            l_titel.setHorizontalAlignment(SwingConstants.CENTER);
            l_titel.setBounds(10, 0, 430, 30);
            add(l_titel);

            // Infolabel
            JLabel info = new JLabel("Sie haben diesen Code an Ihre Email-Adresse geschickt bekommen");
            info.setHorizontalAlignment(SwingConstants.CENTER);
            info.setVerticalAlignment(SwingConstants.CENTER);
            info.setFont(new Font("Dialog",Font.PLAIN,11));
            info.setBounds(0,30,438,20);
            add(info);

            if (full) {
                JLabel l_username = new JLabel("Email-Adresse");
                l_username.setBounds(10, 55, 196, 33);
                add(l_username);

                email = new JTextField();
                email.setBounds(216, 55, 224, 33);
                add(email);
                email.setColumns(10);

                JLabel l_password = new JLabel("Passwort");
                l_password.setBounds(10, 99, 196, 33);
                add(l_password);

                password = new JPasswordField();
                password.setColumns(10);
                password.setBounds(216, 99, 224, 33);
                add(password);
            }

            JLabel l_code = new JLabel("Best\u00E4tigungscode");
            l_code.setBounds(10, (full ? 143 : 55), 196, 33);
            add(l_code);

            code = new JTextField();
            code.setColumns(10);
            code.setBounds(216, (full ? 143 : 55), 224, 33);
            add(code);

            finishValidation = new JButton("Best\u00E4tigung abschlie\u00DFen");
            finishValidation.setBounds(10, (full ? 187 : 99), 430, 33);
            finishValidation.addActionListener(e -> validateAccount());
            add(finishValidation);
        }

        private void setUIActivation(boolean b) {
            l_titel.setText((b ? "Account bestätigen" : "Bitte warten..."));
            if (full) {
                email.setEditable(b);
                password.setEditable(b);
            }
            code.setEditable(b);
            finishValidation.setEnabled(b);
        }

        @SuppressWarnings("ConstantConditions")
        private void validateAccount() {
            if (!code.getText().matches("^[0-9]{6}$"))
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Der eingebene Code ist falsch (Tipp: Der Aktivierungscode besteht aus 6 Ziffern) ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (full && !email.getText().matches("^(.+)@(.+)$"))
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Bitte geben Sie die E-Mail-Adresse Ihres Kontos ein ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else if (full && new String(password.getPassword()).length() < 6)
                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Falsches Passwort ", "Fehler", JOptionPane.ERROR_MESSAGE);
            else {
                setUIActivation(false);
                new Thread(() -> {
                    try {
                        String[] re = BasicNetwork.sendRequest(Values.HOST, Values.PORT, new String[]{Protocol.VALIDATE, (full ? email.getText() : pre_email), (full ? new String(password.getPassword()) : pre_password), code.getText()});
                        switch (re[0]) {
                            case Protocol.VALIDATION_CODE_WRONG: // Der Code ist falsch
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Der Aktivierungscode ist falsch ", "Fehler", JOptionPane.ERROR_MESSAGE);
                                break;
                            case Protocol.LOGIN_FAILED: // Anmeldedaten waren fehlerhaft
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Die Anmeldeinformationen sind falsch ", "Fehler", JOptionPane.ERROR_MESSAGE);
                                break;
                            case Protocol.ALREADY_VALIDATED: // Account wurde bereits bestätigt
                                if (full) Values.currentVisualWrapper.transitionTo(getLast());
                                else {
                                    VisualWrapper.login(pre_username, pre_password);
                                    Values.currentVisualWrapper.transitionTo(new HomeScreen());
                                }
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Ihr Account wurde bereits bestätigt." + System.lineSeparator() + "Sie werden nun angemeldet ", "Bereits bestätigt", JOptionPane.INFORMATION_MESSAGE);
                            case Protocol.OK:  // Erfolg
                                if (full) Values.currentVisualWrapper.transitionTo(getLast());
                                else {
                                    VisualWrapper.login(pre_username, pre_password);
                                    Values.currentVisualWrapper.transitionTo(new HomeScreen());
                                }
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Ihr Account wurde erfolgreich aktiviert ", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                                break;
                            default:
                                JOptionPane.showMessageDialog(Values.currentVisualWrapper, re[0], " Fehler ", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Keine Verbindung zum Server ", "Fehler", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Values.currentVisualWrapper, " Interner Fehler ", "Fehler", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setUIActivation(true);
                    }
                }).start();
            }
        }
    }
}
