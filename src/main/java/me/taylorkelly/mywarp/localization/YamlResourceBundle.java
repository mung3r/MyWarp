package me.taylorkelly.mywarp.localization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import me.taylorkelly.mywarp.utils.ConfigUtils;

/**
 * A ResourceBundle implementation for YAML files
 */
public class YamlResourceBundle extends ResourceBundle {

    /**
     * The map that contains the key/value pairs
     */
    private FileConfiguration lookup;

    /**
     * Creates this instance from the given stream. Will return an empty map if
     * the stream could not be read or is not a valid yaml configuration.
     * 
     * @param stream
     *            the yaml-formatted stream
     */
    public YamlResourceBundle(InputStream stream) {
        try {
            lookup = ConfigUtils.getYamlConfig(stream, true);
        } catch (IOException e) {
            lookup = null;
        } catch (InvalidConfigurationException e) {
            lookup = null;
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(lookup.getKeys(true));
    }

    @Override
    protected Object handleGetObject(String key) {
        return lookup != null ? lookup.get(key) : null;
    }
}
