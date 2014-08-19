package me.taylorkelly.mywarp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Charsets;

/**
 * Bundles various utilities used when working with yaml configuration files
 */
public class ConfigUtils {

    /**
     * Block initialization of this class.
     */
    private ConfigUtils() {
    }

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
        FancyYamlConfiguration yamlConfig = new FancyYamlConfiguration();

        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;

        try {
            reader = forceEncoding ? new InputStreamReader(stream, Charsets.UTF_8) : new InputStreamReader(
                    stream);
            bufferedReader = new BufferedReader(reader);
            StrBuilder builder = new StrBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                builder.appendln(line);
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
     * @param configFile
     *            the file that holds the configuration
     * @param defaultConfig
     *            a FileConfiguration that contains all default values
     * @param forceEncoding
     *            if set, the stream will be read using <code>UTF-8</code>,
     *            instead of the default char-set
     * @return the FileConfiguration of the given file
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

            // save the file
            saveYamlConfig(configFile, config, forceEncoding);
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
     * Saves the given FileConfiguration back to disk. If the file does not
     * exist, it will be created.
     * 
     * @param configFile
     *            the file that holds the configuration
     * @param config
     *            the FileConfiguration that should be saved
     * @param forceEncoding
     *            if set, the stream will be written using <code>UTF-8</code>,
     *            instead of the default char-set
     * @return true if the configuration could be saved
     */
    public static boolean saveYamlConfig(File configFile, FileConfiguration config, boolean forceEncoding) {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        FileOutputStream stream = null;
        OutputStreamWriter writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            stream = new FileOutputStream(configFile);
            writer = forceEncoding ? new OutputStreamWriter(stream, "UTF-8") : new OutputStreamWriter(stream);
            bufferedWriter = new BufferedWriter(writer);

            writer.write(config.saveToString());
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }
}
