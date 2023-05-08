package me.statuxia.srapic.database;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ConnectionPool {
    private static final String JDBC = "jdbc:mysql://%1$s/%2$s";

    private static final int TIMEOUT_SECONDS = 5;
    private static final int VALIDATION_DELAY_SECONDS = 10;
    private static final int DEFAULT_POOL_SIZE = 32;
    private static volatile ConnectionPool instanceReference = null;
    private static String dbURL;
    private static String dbUser;
    private static String dbPassword;
    private final Cell[] cells;
    private final Semaphore semaphore;
    private final ScheduledExecutorService maintainer;

    private ConnectionPool(int poolSize) {
        cells = Stream.generate(Cell::new).limit(poolSize).toArray(Cell[]::new);
        semaphore = new Semaphore(poolSize, true);
        maintainer = Executors.newScheduledThreadPool(8);

        DriverManager.setLoginTimeout(TIMEOUT_SECONDS);
        for (var cell : cells)
            maintainer.scheduleWithFixedDelay(() -> {

                if (cell.isUsed.get() || !semaphore.tryAcquire())
                    return;

                if (cell.isUsed.compareAndSet(false, true)) {
                    try {
                        if (cell.connection == null || !cell.connection.isValid(TIMEOUT_SECONDS))
                            cell.connection = createConnection(); /*A leak? If a connection is not valid, then is it also closed?*/
                    } catch (Exception ignored) {
                    }
                    cell.isUsed.set(false);
                }

                semaphore.release();

            }, 0, VALIDATION_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbURL, dbUser, dbPassword);
    }

    public static void initialize(String url, String user, String password, int poolSize) {
        dbURL = url;
        dbUser = user;
        dbPassword = password;
        instanceReference = new ConnectionPool(poolSize);
    }

    public static void initialize(JSONObject mysqlJSON, int poolSize) {
        dbURL = String.format(JDBC, mysqlJSON.getString("host"), mysqlJSON.getString("database"));
        dbUser = mysqlJSON.getString("user");
        dbPassword = mysqlJSON.getString("password");
        instanceReference = new ConnectionPool(poolSize);
    }

    public static void shutdown() {
        ScheduledExecutorService maintainer = instanceReference.maintainer;
        if (!maintainer.isShutdown())
            instanceReference.maintainer.shutdown();
    }

    public static ConnectionPool.Agent acquireConnection() throws InterruptedException, SQLException, RuntimeException {
        final var instance = instanceReference;

        if (!instance.semaphore.tryAcquire(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            throw new SQLTimeoutException();

        for (var cell : instance.cells)
            if (cell.isUsed.compareAndSet(false, true)) {
                if (cell.connection == null || cell.connection.isClosed())
                    cell.connection = createConnection();
                return new ConnectionPool.Agent(cell, instance.semaphore);
            }

        /* Will never happen, but just in case */
        throw new RuntimeException();
    }

    public void initialize(String url, String user, String password) {
        initialize(url, user, password, DEFAULT_POOL_SIZE);
    }

    public void initialize(JSONObject mysqlJSON) {
        String url = String.format(JDBC, mysqlJSON.getString("host"), mysqlJSON.getString("database"));
        String user = mysqlJSON.getString("user");
        String password = mysqlJSON.getString("password");
        initialize(url, user, password, DEFAULT_POOL_SIZE);
    }

    private static class Cell {
        AtomicBoolean isUsed = new AtomicBoolean(false);
        volatile Connection connection = null;
    }

    public static class Agent implements AutoCloseable {
        private final Cell cell;
        private final Semaphore semaphore;
        private boolean isClosed = false;

        public Agent(Cell cell, Semaphore semaphore) {
            this.cell = cell;
            this.semaphore = semaphore;
        }

        public Connection getConnection() {
            return cell.connection;
        }

        @Override
        public void close() {
            if (isClosed) return;
            isClosed = true;

            cell.isUsed.set(false);
            semaphore.release();
        }
    }
}