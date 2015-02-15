/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp.util.i18n;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides translated messages using Java ResourceBundles. <p> When created, instances will uses the default resource
 * bundle lookup as described in the {@link java.util.ResourceBundle} documentation. This process can be customized by
 * registering a custom {@link java.util.ResourceBundle.Control} via {@link #setControl(ResourceBundle.Control)}. </p>
 */
public class DynamicMessages {

  private static final Logger log = Logger.getLogger(DynamicMessages.class.getName());

  private static ResourceBundle.Control control = new ResourceBundle.Control() {
  };

  private final String baseName;

  /**
   * Creates an instance. Strings will be looked up from a ResourceBundle of the given name.
   *
   * @param baseName the name of the ResourceBundle
   */
  public DynamicMessages(String baseName) {
    this.baseName = baseName;
  }

  /**
   * Clears the cache used by all DynamicMessages.
   */
  public static void clearCache() {
    ResourceBundle.clearCache();
  }

  /**
   * Sets the {@link java.util.ResourceBundle.Control} instance used by all DynamicMessages instances.
   *
   * @param control the ResourceBundle.Control
   */
  public static synchronized void setControl(ResourceBundle.Control control) {
    DynamicMessages.control = control;
  }

  /**
   * Gets a translated string. <p> If no translation exits, {@code $ key} will be returned. </p>
   *
   * @param key the key
   * @return the translated string
   */
  public String getString(String key) {
    return getString(key, LocaleManager.getLocale());
  }

  /**
   * Gets a translated string. <p> If no translation exits, {@code $ key} will be returned. </p>
   *
   * @param key    the key
   * @param locale the Locale
   * @return the translated string
   */
  public String getString(String key, Locale locale) {
    ResourceBundle bundle = getBundle(locale);
    try {
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      log.log(Level.WARNING, "Failed to find message.", e); // NON-NLS
    }
    return "${" + key + "}";
  }

  /**
   * Gets a translated and formatted string. <p> If no translation exits, {@code $ key}:args} will be returned. </p>
   *
   * @param key  the key
   * @param args the arguments
   * @return the translated string
   */
  public String getString(String key, Object... args) {
    return getString(key, LocaleManager.getLocale(), args);
  }

  /**
   * Gets a translated and formatted string. <p> If no translation exits, {@code $ key}:args} will be returned. </p>
   *
   * @param key    the key
   * @param locale the Locale
   * @param args   the arguments
   * @return a translated string
   */
  public String getString(String key, Locale locale, Object... args) {
    ResourceBundle bundle = getBundle(locale);
    try {
      MessageFormat format = new MessageFormat(bundle.getString(key), locale);
      return format.format(args);
    } catch (MissingResourceException e) {
      log.log(Level.WARNING, "Failed to find message.", e); // NON-NLS
    }
    return "${" + key + "}:" + Arrays.toString(args);
  }

  /**
   * Gets the ResourceBundle applicable for the given key.
   *
   * @param locale the Locale
   * @return the applicable ResourceBundle
   */
  private ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle(baseName, locale, control);
  }
}
