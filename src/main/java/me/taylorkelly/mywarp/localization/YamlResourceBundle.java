/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.localization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import me.taylorkelly.mywarp.utils.ConfigUtils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A ResourceBundle implementation for YAML files.
 */
public class YamlResourceBundle extends ResourceBundle {

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
