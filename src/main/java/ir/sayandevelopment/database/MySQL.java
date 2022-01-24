package ir.sayandevelopment.database;

import java.sql.DriverManager;

public class MySQL extends SQL {

    private final String host, database, username, password;
    private final int port;

    public MySQL(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void openConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password));
    }
}
