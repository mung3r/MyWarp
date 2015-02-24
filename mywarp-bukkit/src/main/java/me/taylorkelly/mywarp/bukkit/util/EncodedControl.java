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

package me.taylorkelly.mywarp.bukkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Resolves {@link java.util.PropertyResourceBundle}s from the classpath similar to the default {@link
 * java.util.ResourceBundle.Control}, but requires an explicit encoding to parse the files.
 *
 * @deprecated To be removed from future versions.
 */
@Deprecated
public class EncodedControl extends ResourceBundle.Control {

  private final Charset encoding;

  /**
   * Initializes this instance. ResourceBundles will be parsed with the given encoding.
   *
   * @param encoding the encoding
   */
  public EncodedControl(Charset encoding) {
    this.encoding = encoding;
  }

  @Override
  public List<String> getFormats(String baseName) {
    return ResourceBundle.Control.FORMAT_PROPERTIES;
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {
    //this implementation closely mirrors Java's but initializes the ResourceBundle with a Reader and encoding
    String bundleName = toBundleName(baseName, locale);
    ResourceBundle bundle = null;
    if (format.equals("java.properties")) {
      final String resourceName = toResourceName(bundleName, "properties");
      final ClassLoader classLoader = loader;
      final boolean reloadFlag = reload;
      InputStream stream = null;
      try {
        stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
          public InputStream run() throws IOException {
            InputStream is = null;
            if (reloadFlag) {
              URL url = classLoader.getResource(resourceName);
              if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                  // Disable caches to get fresh data for
                  // reloading.
                  connection.setUseCaches(false);
                  is = connection.getInputStream();
                }
              }
            } else {
              is = classLoader.getResourceAsStream(resourceName);
            }
            return is;
          }
        });
      } catch (PrivilegedActionException e) {
        throw (IOException) e.getException();
      }
      if (stream != null) {
        Reader reader = new InputStreamReader(stream, encoding);
        try {

          bundle = new PropertyResourceBundle(reader);
        } finally {
          reader.close();
        }
      }
    } else {
      throw new IllegalArgumentException("unknown format: " + format);
    }
    return bundle;
  }
}
