/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.server;

import de.noamo.util.MD5;
import de.noamo.util.Protocol;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

public class DataBase {
    private static BasicDataSource basicDataSource;

    /**
     * Prüft, ob der Nutzer in der Datenbank vorhanden ist.
     *
     * @param username Username des Nutzers
     * @param password Passwort des Nutzers
     * @return true=Login erfolgreich,false=Login fehlerhaft
     * @throws SQLException       Problem mit der Datenbankverbindung
     * @throws NotActiveException Wenn der Account noch nicht aktiviert wurde
     */
    static boolean checkLogin(String username, String password) throws SQLException, NotActiveException {
        if (username == null || password == null || username.equals("") || password.equals("")) return false;
        try (Connection connection = basicDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM accounts WHERE username='" + username + "' AND password='" + MD5.encrypt(password) + "' AND active=1;");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return true;
            } else {
                try (PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT username FROM accounts WHERE username='" + username + "' AND password='" + MD5.encrypt(password) + "';");
                     ResultSet resultSet1 = preparedStatement1.executeQuery()) {
                    if (resultSet1.next()) throw new NotActiveException("The account " + username + " is not active!");
                    else return false;
                }
            }
        }
    }

    /**
     * Stellt eine Verbindung zu der Datenbank her und erstellt ggf. fehlende Tabellen in dieser.
     *
     * @param url Die vollständige URL der Datenbank (inkl. Passwort, Username, Driver, etc.)
     * @throws SQLException Falls keine Verbindung hergestellt werden kann oder beim Setup Probleme auftreten
     */
    static void connect(String url) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url)) {
            dataBaseSetup(connection);
        }

        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setValidationQuery("SELECT userid FROM accounts");
        basicDataSource.setMinIdle(3);
        basicDataSource.setMaxIdle(5);
        basicDataSource.setMaxOpenPreparedStatements(50);
    }

    /**
     * Erstellt die benötigen Tabellen, falls diese noch nicht existieren
     *
     * @param connection Die Verbindung zu der Datenbank
     */
    private static void dataBaseSetup(Connection connection) throws SQLException {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS accounts(userid INT NOT NULL AUTO_INCREMENT, " + // Eindeutige ID des Benutzers
                "admin BIT NOT NULL DEFAULT 0, " + // Ob der Nutzer Administrator ist
                "active BIT NOT NULL DEFAULT 0, " + // Gibt an, ob ein Account aktiviert wurde
                "created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " + // Erstellungszeitpunkt des Accounts
                "username VARCHAR(50) NOT NULL, " + // Der Benutzername (zum Anmelden)
                "password VARCHAR(50) NOT NULL, " + // Das Passwort (für die Anmeldung)
                "name VARCHAR(100) NOT NULL, " + // Der Name der Person
                "email VARCHAR(254) NOT NULL, " + // Eine Email-Adresse des Benutzers (für Infos über Probleme)
                "PRIMARY KEY (userid), " + // Eindeutige ID des Benutzers als Key
                "UNIQUE (username), UNIQUE (email));").executeUpdate();
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS software(softwareid INT NOT NULL AUTO_INCREMENT, " + // Einedeutige ID der Software
                "softwareName VARCHAR(50) NOT NULL, " +  // Name des Produktes
                "version FLOAT NOT NULL, " + // Aktuelle Version
                "downloadlink TEXT NOT NULL, " + // Link zu einem direkten Download
                "owner INT NOT NULL, " + // UserID des Besitzer-Accounts
                "iconLink TEXT NOT NULL, " + // Bild, dass im Launcher angezeigt wird
                "startFile VARCHAR(50), " + // Die Datei, mit der die Software startet (.class ist eine native App)
                "created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " + // Erstellungszeitpunkt der Software
                "PRIMARY KEY (softwareid), " + // ID als eindeutiger Schlüssel
                "UNIQUE (softwareName));").executeUpdate(); // Verhindern, dass der selbe Name mehrfach auftaucht
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS activationKeys(softwareid INT NOT NULL, " + // Zum Schlüssel zughörige Software
                "activationKey VARCHAR(30) NOT NULL, " + // Einzigartiger Aktivierungsschlüssel
                "UNIQUE(activationKey));").executeUpdate(); // Verhindern, dass es Aktivierungsschlüssel doppelt gibt
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS accountKeys(email VARCHAR(254) NOT NULL, " + // Zugehörige Email-Adresse
                "accountKey VARCHAR(30) NOT NULL, " + // Der Key, mit dem der Account aktiviert werden kann
                "created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP);").executeUpdate(); // Wann der Aktivierungskey erstellt wurde
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS ownedsoftware(softwareid INT NOT NULL, " + // Die ID der Software
                "userid INT NOT NULL, " + // Die UserID des Besitzers
                "activated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (softwareid,userid));").executeUpdate(); // Aktivierungszeitpunkt
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS serviceChat(messageid INT NOT NULL AUTO_INCREMENT, " + // Eindeutige ID für die Nachricht
                "time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " + // Zeitpunkt der Nachricht
                "userid INT NOT NULL, " + // Die ID Des Benutzers
                "fromService BIT NOT NULL, " + // Ob die Nachricht von einem Service Mitarbeiter stammt
                "content TEXT NOT NULL," + "PRIMARY KEY (messageid));").executeUpdate();
        try {
            connection.prepareStatement("INSERT INTO accounts (username, password, name, email, admin, active) VALUES ('admin', '" + MD5.encrypt("Initial") + "', 'Admin', 'info@noamo.de', 1,1);").executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {}
    }

    /**
     * Erstellt einen Account in der Datenbank (ist noch nicht aktiv) und senden
     * eine Bestätigunsmail an diese Person.
     *
     * @param username Der Benutzername der Person
     * @param password Ein Passwort mit min. 8 Zeichen
     * @param name Der vollsätige Name
     * @param email Die Email-Adresse des Nutzers (daran wird die Bestätigunsmail gesendet)
     * @return Direkt Antwort, die an den Client zurück gesendet werden kann
     * @throws SQLException Bei Datenbankverbindungsproblemen
     */
    static String register(String username, String password, String name, String email) throws SQLException {
        if (username == null || username.length() < 5) return Protocol.USERNAME_TO_SHORT;
        if (!username.matches("^[A-Za-z0-9._-]*$")) return Protocol.USERNAME_FORBIDDEN_CHARS;
        if (password == null || password.length() < 8) return Protocol.PASSWORD_TO_SHORT;
        if (name == null || name.length() < 3) return Protocol.NOT_FULL_NAME;
        if (email == null || !email.matches("^(.+)@(.+)$")) return Protocol.NOT_A_VALID_EMAIL_ADRESS;

        try (Connection connection = basicDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (username, password, name, email) VALUES ('" + username + "', '" + MD5.encrypt(password) + "', '" + name + "', '" + email + "');")) {
            try {
                preparedStatement.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                try (PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT username FROM accounts WHERE username='" + username + "';");
                     ResultSet resultSet = preparedStatement1.executeQuery()) {
                    if (resultSet.next()) return Protocol.USERNAME_ALREADY_IN_USE;
                    return Protocol.EMAIL_ALREADY_IN_USE;
                }
            }
            int key = new Random().nextInt(900000) + 100000;
            try (PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO accountKeys (email, accountKey) VALUES ('" + email + "', '" + key + "');")) {
                preparedStatement2.executeUpdate();
            }
            Mail.sendActivationMail(email, name, key);
            return Protocol.OK;
        }
    }

    public static class NotActiveException extends Exception {
        public NotActiveException(String s) {
            super(s);
        }
    }
}
