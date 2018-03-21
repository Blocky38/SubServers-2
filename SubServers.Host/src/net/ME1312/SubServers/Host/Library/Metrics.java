package net.ME1312.SubServers.Host.Library;

import net.ME1312.SubServers.Host.ExHost;
import net.ME1312.SubServers.Host.Library.Config.YAMLConfig;
import net.ME1312.SubServers.Host.Library.Config.YAMLSection;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class Metrics {

    static {
        // You can use the property to disable the check in your test environment
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
            final String defaultPackage = new String(
                    new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'n', 'g', 'e', 'e', 'c', 'o', 'r', 'd'});
            final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            // We want to make sure nobody just copy & pastes the example and use the wrong package names
            if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }

    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;

    // The url to which the data is sent
    private static final String URL = "https://bStats.org/submitData/other";

    // The host
    private final ExHost host;

    // Is bStats enabled on this server?
    private boolean enabled;

    // The uuid of the server
    private String serverUUID;

    // Should failed requests be logged?
    private boolean logFailedRequests = false;

    // A list with all known metrics class objects including this one
    private static final List<Object> knownMetricsInstances = new ArrayList<>();

    public Metrics(ExHost host) {
        this.host = host;

        try {
            loadConfig();
        } catch (IOException e) {
            // Failed to load configuration
            host.log.warn.println("Failed to load bStats config!", e);
            return;
        }

        // We are not allowed to send data about this server :(
        if (!enabled) {
            return;
        }

        Class<?> usedMetricsClass = getFirstBStatsClass();
        if (usedMetricsClass == null) {
            // Failed to get first metrics class
            return;
        }
        if (usedMetricsClass == getClass()) {
            // We are the first! :)
            linkMetrics(this);
            startSubmitting();
        } else {
            // We aren't the first so we link to the first metrics class
            try {
                usedMetricsClass.getMethod("linkMetrics", Object.class).invoke(null,this);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                if (logFailedRequests) {
                    host.log.warn.println("Failed to link to first metrics class " + usedMetricsClass.getName() + "!", e);
                }
            }
        }
    }

    /**
     * Links an other metrics class with this class.
     * This method is called using Reflection.
     *
     * @param metrics An object of the metrics class to link.
     */
    public static void linkMetrics(Object metrics) {
        knownMetricsInstances.add(metrics);
    }

    /**
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    public JSONObject getPluginData() {
        JSONObject data = new JSONObject();

        String pluginName = "SubServers Host";
        String pluginVersion = host.version.toString();

        data.put("pluginName", pluginName);
        data.put("pluginVersion", pluginVersion);

        JSONArray customCharts = new JSONArray();
        data.put("customCharts", customCharts);

        return data;
    }

    private void startSubmitting() {
        new Timer().schedule( new TimerTask() {
            @Override
            public void run() {
                // The data collection is async, as well as sending the data
                // Bungeecord does not have a main thread, everything is async
                submitData();
            }
        }, TimeUnit.MINUTES.toMillis(2), TimeUnit.MINUTES.toMillis(30));
        // Submit the data every 30 minutes, first time after 2 minutes to give other plugins enough time to start
        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
        // WARNING: Just don't do it!
    }

    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JSONObject getServerData() {
        // OS/Java specific data
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        JSONObject data = new JSONObject();

        data.put("serverUUID", serverUUID);

        data.put("javaVersion", javaVersion);
        data.put("osName", osName);
        data.put("osArch", osArch);
        data.put("osVersion", osVersion);
        data.put("coreCount", coreCount);

        return data;
    }

    /**
     * Collects the data and sends it afterwards.
     */
    private void submitData() {
        final JSONObject data = getServerData();

        final JSONArray pluginData = new JSONArray();
        // Search for all other bStats Metrics classes to get their plugin data
        for (Object metrics : knownMetricsInstances) {
            try {
                Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
                if (plugin instanceof JSONObject) {
                    pluginData.put(plugin);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) { }
        }

        data.put("plugins", pluginData);

        try {
            // Send the data
            sendData(data);
        } catch (Exception e) {
            // Something went wrong! :(
            if (logFailedRequests) {
                host.log.warn.println("Could not submit plugin stats!", e);
            }
        }
    }

    /**
     * Loads the bStats configuration.
     *
     * @throws IOException If something did not work :(
     */
    private void loadConfig() throws IOException {
        Path configPath = new File(host.dir, "plugins").toPath().resolve("bStats");
        configPath.toFile().mkdirs();
        File configFile = new File(configPath.toFile(), "config.yml");
        if (!configFile.exists()) {
            writeFile(configFile,
                    "#bStats collects some data for plugin authors like how many servers are using their plugins.",
                    "#To honor their work, you should not disable it.",
                    "#This has nearly no effect on the server performance!",
                    "#Check out https://bStats.org/ to learn more :)",
                    "enabled: true",
                    "serverUuid: \"" + UUID.randomUUID().toString() + "\"",
                    "logFailedRequests: false");
        }

        YAMLSection configuration = new YAMLConfig(configFile).get();

        // Load configuration
        enabled = configuration.getBoolean("enabled", true);
        serverUUID = configuration.getString("serverUuid");
        logFailedRequests = configuration.getBoolean("logFailedRequests", false);
    }

    /**
     * Gets the first bStat Metrics class.
     *
     * @return The first bStats metrics class.
     */
    private Class<?> getFirstBStatsClass() {
        Path configPath = new File(host.dir, "plugins").toPath().resolve("bStats");
        configPath.toFile().mkdirs();
        File tempFile = new File(configPath.toFile(), "temp.txt");

        try {
            String className = readFile(tempFile);
            if (className != null) {
                try {
                    // Let's check if a class with the given name exists.
                    return Class.forName(className);
                } catch (ClassNotFoundException ignored) { }
            }
            writeFile(tempFile, getClass().getName());
            return getClass();
        } catch (IOException e) {
            if (logFailedRequests) {
                host.log.warn.println("Failed to get first bStats class!", e);
            }
            return null;
        }
    }

    /**
     * Reads the first line of the file.
     *
     * @param file The file to read. Cannot be null.
     * @return The first line of the file or <code>null</code> if the file does not exist or is empty.
     * @throws IOException If something did not work :(
     */
    private String readFile(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader =  new BufferedReader(fileReader);
        ) {
            return bufferedReader.readLine();
        }
    }

    /**
     * Writes a String to a file. It also adds a note for the user,
     *
     * @param file The file to write to. Cannot be null.
     * @param lines The lines to write.
     * @throws IOException If something did not work :(
     */
    private void writeFile(File file, String... lines) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
        ) {
            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Sends the data to the bStats server.
     *
     * @param data The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(JSONObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

        // Compress the data to save bandwidth
        byte[] compressedData = compress(data.toString());

        // Add headers
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

        // Send data
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(compressedData);
        outputStream.flush();
        outputStream.close();

        connection.getInputStream().close(); // We don't care about the response - Just send our data :)
    }

    /**
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return outputStream.toByteArray();
    }
}