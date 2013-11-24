package me.taylorkelly.mywarp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Bundles various utilities used when working with yaml configuration files
 */
public class ConfigUtils {

    /**
     * Gets the FileConfiguration from the given InputStream.
     * 
     * @param stream
     *            the stream
     * @param forceEncoding
     *            if set, the stream will be read using <code>UTF-8</code>,
     *            instead of the default char-set
     * @return the FileConfiguration created from the stream
     * @throws IOException
     *             if the given stream is unreadable
     * @throws InvalidConfigurationException
     *             if the input stream is not a proper YamlConfiguration
     */
    public static FileConfiguration getYamlConfig(InputStream stream, boolean forceEncoding)
            throws IOException, InvalidConfigurationException {
        YamlConfiguration yamlConfig = new YamlConfiguration();

        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;

        try {
            reader = forceEncoding ? new InputStreamReader(stream, "UTF-8") : new InputStreamReader(stream);
            bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append(System.getProperty("line.separator"));
            }
            yamlConfig.loadFromString(builder.toString());
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return yamlConfig;
    }

    /**
     * Gets the FileConfiguration that belongs to the given file. If the file
     * does not exist, it will be created. In both cases, missing values are
     * added from the provided default-configuration.
     * 
     * If the configuration-file is unreadable, this method will return the
     * provided default-configuration.
     * 
     * @param defaultConfig
     *            a FileConfiguration that contains all default values
     * @param forceEncoding
     *            if set, the stream will be read using <code>UTF-8</code>,
     *            instead of the default char-set
     * @return the FileConfiguraion of the given file
     */
    public static FileConfiguration getYamlConfig(File configFile, FileConfiguration defaultConfig,
            boolean forceEncoding) {
        FileConfiguration config = defaultConfig;

        try {
            // create the configuration path if it does not exist
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            // create the configuration file if it does not exist
            if (!configFile.exists()) {
                configFile.createNewFile();
                MyWarp.logger().info("Default " + configFile.getName() + " created successfully!");
            }

            // load the configuration-file
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(configFile);
                config = getYamlConfig(stream, forceEncoding);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            // copy defaults for missing values
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
            config.save(configFile);
        } catch (IOException e) {
            MyWarp.logger().log(Level.SEVERE,
                    "Failed to create default " + configFile.getName() + " , using build-in defaults: ", e);
        } catch (InvalidConfigurationException e) {
            MyWarp.logger().log(
                    Level.SEVERE,
                    "Failed to read " + configFile.getName()
                            + " (not a FileConfiguration), using build-in defaults: ", e);
        }
        return config;
    }

    /**
     * Converts the given string into a Locale.
     * 
     * @param str
     *            the string
     * @return the corresponding locale
     */
    public static Locale stringToLocale(String str) {
        StringTokenizer tempStringTokenizer = new StringTokenizer(str, ",");
        String l = "";
        String c = "";
        if (tempStringTokenizer.hasMoreTokens()) {
            l = (String) tempStringTokenizer.nextElement();
        }
        if (tempStringTokenizer.hasMoreTokens()) {
            c = (String) tempStringTokenizer.nextElement();
        }
        return new Locale(l, c);
    }

}
