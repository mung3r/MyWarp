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
 * The central manager for all localizations uses through the plugin
 */
public class LocalizationManager implements Reloadable {

    /**
     * Stored used Locales under their string-reference used by Minecraft
     * internally.
     */
    private final Map<String, Locale> localeCache = new HashMap<String, Locale>();

    /**
     * The resourceBundleControl-object for our ResourceBundles
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
                }
            }
            ConfigUtils.getYamlConfig(new File(MyWarp.inst().getDataFolder(), bundleName), bundledConfig,
                    true);
        }
    }

    /**
     * Gets {@link #getString(String, CommandSender)} without colors
     * 
     * @param key
     *            the key of the string
     * @param sender
     *            the command-sender who receives this message
     * @return the corresponding string out of the language map without colors
     */
    public String getColorlessString(String key, CommandSender sender) {
        return getColorlessString(key, getCommandSenderLocale(sender));
    }

    /**
     * Returns {@link #getString(String)} without colors
     * 
     * @param key
     *            the key of the string
     * @return the corresponding string out of the language map without colors
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
        Locale locale = MyWarp.inst().getWarpSettings().localizationDefLocale;
        if (MyWarp.inst().getWarpSettings().localizationPerPlayer && sender instanceof Player) {
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
     * @see #getCommandSenderLocale(CommandSender)
     * 
     * @param key
     *            the key of the string
     * @param sender
     *            the command-sender who receives this message
     * @param replacements
     *            the replacements
     * @return the corresponding string out of effective resource bundle
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
     * @param replacements
     *            the replacements
     * @return the corresponding string out of effective resource bundle
     */
    public String getString(String key, Locale locale, Object... replacements) {
        ResourceBundle resource = getResourceBundle(locale);
        String value = ChatColor.translateAlternateColorCodes('§', resource.getString(key));
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
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private String getLanguage(Player p) throws IllegalArgumentException, IllegalAccessException,
            SecurityException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
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
