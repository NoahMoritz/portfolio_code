package de.noamo.sql;

import java.sql.*;

/**
 * @author Noah Moritz Hölterhoff
 */
public class DataBase {
    private final Connection connection;
    private final String dataBase;

    public DataBase(String server, int port, String username, String password, String dataBase) throws Exception {
        this.dataBase = dataBase; Class.forName("com.mysql.jdbc.Driver");
        String db_URL = "jdbc:mysql://" + server + ":" + port + "/" + dataBase + "?ServerCertificate=false&" + "user=" + username + "&password=" + password + "&serverTimezone=UTC";
        connection = DriverManager.getConnection(db_URL);
    }

    protected String getDataBase() {
        return dataBase;
    }

    /**
     * Führt einen Befehl in der MySQL Datenbank aus.
     *
     * @param pStatement Der Befehl in Form eines Strings
     * @throws SQLException Falls keine Verbindung besteht oder sonstige Fehler auftreten
     */
    public void executeUpdate(String pStatement) throws SQLException {
        PreparedStatement posted = connection.prepareStatement(pStatement);
        posted.executeUpdate();
    }

    /**
     * Führt einen Such-Befehl in der MySQL Datenbank aus.
     *
     * @param pStatement Der Befehl in Form eines Strings
     * @return Ein ResultSet auf dem der passende gesuchte Datensatz ermittelt werden kann
     * @throws SQLException Falls keine Verbindung besteht oder sonstige Fehler auftreten
     */
    public ResultSet executeQuery(String pStatement) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(pStatement);
        return statement.executeQuery();
    }
}
