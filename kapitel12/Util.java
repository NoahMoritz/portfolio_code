package de.noamo.sql;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

/**
 * @author Noah Moritz Hölterhoff
 */
public class Util {
    public static JTable resultSetToJTable(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        Vector columns = new Vector(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(md.getColumnName(i));
        }
        Vector data = new Vector();
        Vector row;
        while (rs.next()) {
            row = new Vector(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getString(i));
            }
            data.add(row);
        }
        JTable returnValue = new JTable(data, columns);
        returnValue.setVisible(true);
        returnValue.setEnabled(false);
        return returnValue;
    }
}
