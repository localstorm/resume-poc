package co.kuznetsov;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class HFDoorKnockRunnable implements Runnable {
    private final AtomicReference<ResumeOutcome> outcomeRef;
    private final String endpoint;
    private final int port;
    private final String username;
    private final String password;
    private final long maxWaitMillis;

    public HFDoorKnockRunnable(AtomicReference<ResumeOutcome> outcomeRef, String endpoint, int port, String username, String password, long maxWaitMillis) {
        this.outcomeRef = outcomeRef;
        this.endpoint = endpoint;
        this.port = port;
        this.username = username;
        this.password = password;
        this.maxWaitMillis = maxWaitMillis;
    }

    @Override
    public void run() {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("connectTimeout", "500");

        long start = System.currentTimeMillis();
        boolean drop = false;

        while ((System.currentTimeMillis() - start) < maxWaitMillis) {
            try (var conn = DriverManager.getConnection("jdbc:mysql://" + endpoint + ":" + port, properties)) {
                conn.createStatement().execute("SELECT 1");
            } catch (SQLException e) {
                drop = true;
                continue;
            }
            var durationMillis = System.currentTimeMillis() - start;
            outcomeRef.set(new ResumeOutcome(drop, false, durationMillis, false));
            return;
        }
        var durationMillis = System.currentTimeMillis() - start;
        outcomeRef.set(new ResumeOutcome(true, true, durationMillis, false));
    }
}
