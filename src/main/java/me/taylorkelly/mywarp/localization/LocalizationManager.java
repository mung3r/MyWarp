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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.Reloadable;
import me.taylorkelly.mywarp.utils.ConfigUtils;

import org.apache.commons.lang.LocaleUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * A manager for localization files.
 */
public class LocalizationManager implements Reloadable {

    /**
     * Stored used Locales under their string-reference used by Minecraft
     * internally.
     */
    private final Map<String, Locale> localeCache = new HashMap<String, Locale>();

    /**
     * The resourceBundleControl-object for our ResourceBundles.
     */
    private final ResourceBundleControl resourceBundleControl = new ResourceBundleControl();

    /**
     * Creates this language-manager instance. This will create all
     * language-files provided by the plugin and update them, if needed.
     * 
     * @throws LocalizationException
     *             if one of the bundled localization files is not readable
     */
    public LocalizationManager() throws LocalizationException {
        // create all language files that we provide
        for (String bundleName : Arrays.asList("mywarp_lang.yml", "mywarp_lang_en.yml", "mywarp_lang_de.yml")) {
            InputStream bundled = MyWarp.inst().getResource("lang/" + bundleName);
            FileConfiguration bundledConfig;
            try {
                bundledConfig = ConfigUtils.getYamlConfig(bundled, true);
            } catch (IOException e1) {
                throw new LocalizationException("Failed to read bundled localization-file: " + bundleName, e1);
            } catch (InvalidConfigurationException e1) {
                throw new LocalizationException("The bundled localization-file " + bundleName
                        + "is not a valid configuration-file.", e1);
            } finally {
                try {
                    bundled.close();
                } catch (IOException e) {
                    //we cannot do anything, so this can be ignored for now.
                }
            }
            // ...and update them!
            ConfigUtils.getYamlConfig(new File(MyWarp.inst().getDataFolder(), bundleName), bundledConfig,
                    true);
        }
    }

    /**
     * Gets the string that is associated with the given key in the resource
     * bundle that is used for the locale used by the the given command-sender
     * and removes all color- and formatting codes.
     * 
     * @see #getString(String, Locale, Object...)
     * 
     * @param key
     *            the key of the string
     * @param sender
     *            the command-sender who receives this message
     * @return the corresponding string out of the effective resource bundle
     */
    public String getColorlessString(String key, CommandSender sender) {
        return getColorlessString(key, getCommandSenderLocale(sender));
    }

    /**
     * Gets the string that is associated with the given key in the resource
     * bundle that is used for the given locale and removes all color- and
     * formatting codes.
     * 
     * @see #getString(String, Locale, Object...)
     * 
     * @param key
     *            the key of the string
     * @param locale
     *            the locale
     * @return the corresponding string out of the effective resource bundle
     */
    public String getColorlessString(String key, Locale locale) {
        return ChatColor.stripColor(getString(key, locale));
    }

    /**
     * Gets the locale that is in effect for the given command sender. If the
     * sender is a Player, the method will call {@link #getLanguage(Player)} to
     * determine his locale. If not, the default locale set in the configuration
     * will be used.
     * 
     * Should never return null or throw Exceptions, but return the default
     * locale instead.
     * 
     * @param sender
     *            the command sender
     * @return the locale
     */
    private Locale getCommandSenderLocale(CommandSender sender) {
        Locale locale = MyWarp.inst().getSettings().getLocalizationDefaultLocale();
        if (MyWarp.inst().getSettings().isLocalizationPerPlayer() && sender instanceof Player) {
            try {
                String mcLocale = getLanguage((Player) sender);
                if (localeCache.containsKey(mcLocale)) {
                    locale = localeCache.get(mcLocale);
                } else {
                    locale = LocaleUtils.toLocale(mcLocale);
                    localeCache.put(mcLocale, locale);
                }
            } catch (Exception e) {
                MyWarp.logger().warning(
                        "Failed to get locale from " + sender.getName() + ": " + e.getMessage()
                                + ", defaulting to " + locale.toString());
            }
        }
        return locale;
    }

    /**
     * Gets the string that is associated with the given key in the resource
     * bundle that is used for the locale used by the the given command-sender.
     * 
     * @see #getString(String, Locale, Object...)
     * 
     * @param key
     *            the key of the string
     * @param sender
     *            the command-sender who receives this message
     * @param replacements
     *            the replacements
     * @return the corresponding string out of the effective resource bundle
     */
    public String getString(String key, CommandSender sender, Object... replacements) {
        return getString(key, getCommandSenderLocale(sender), replacements);
    }

    /**
     * Gets the string that is associated with the given key in the resource
     * bundle that is used for the given locale. If replacements are given, the
     * string will be parsed via a {@link MessageFormat} to match all
     * replacements with its placeholder.
     * 
     * This method will also replace any color code it finds with the
     * corresponding {@link ChatColor}.
     * 
     * @param key
     *            the key of the string
     * @param locale
     *            the locale
     * @param replacements
     *            the replacements
     * @return the corresponding string out of the effective resource bundle
     */
    public String getString(String key, Locale locale, Object... replacements) {
        ResourceBundle resource = getResourceBundle(locale);
        String value;
        try {
            value = ChatColor.translateAlternateColorCodes('&', resource.getString(key));
        } catch (MissingResourceException e) {
            MyWarp.logger().warning(
                    "The resource for '" + key + "' is missing in all resource bundles applicable for "
                            + locale.toString() + ".");
            return key;
        }
        // do not create a new MessageFormat if it won't be needed
        if (replacements.length == 0) {
            return value;
        }
        MessageFormat msgFormat = new MessageFormat(value);
        msgFormat.setLocale(resource.getLocale());
        return msgFormat.format(replacements);
    }

    /**
     * Attempts to get the locale used by the given player (client-side). This
     * method relies on reflection to load the minecraft-player-object through
     * the craftbukkit-implementation and access its <code>locale</code> field.
     * 
     * May break on Minecraft or CraftBukkit updates.
     * 
     * @param p
     *            the player
     * @return the used locale as string
     * @throws IllegalAccessException
     *             if the underlying reflection fails
     * @throws NoSuchFieldException
     *             if the underlying reflection fails
     * @throws InvocationTargetException
     *             if the underlying reflection fails
     * @throws NoSuchMethodException
     *             if the underlying reflection fails
     */
    private String getLanguage(Player p) throws IllegalAccessException, NoSuchFieldException,
            InvocationTargetException, NoSuchMethodException {
        Object minecraftHandle = p.getClass().getMethod("getHandle").invoke(p);
        Field localeField = minecraftHandle.getClass().getDeclaredField("locale");
        localeField.setAccessible(true);
        return (String) localeField.get(minecraftHandle);
    }

    /**
     * Gets the resource-bundle that is used for the given locale.
     * 
     * @param locale
     *            the locale
     * @return the corresponding resource-bundle
     */
    private ResourceBundle getResourceBundle(Locale locale) {
        return YamlResourceBundle.getBundle("mywarp_lang", locale, resourceBundleControl);
    }

    @Override
    public void reload() {
        ResourceBundle.clearCache();
    }
}
