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

import com.google.common.base.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Searches for {@link PropertyResourceBundle}s in a folder, before trying to resolve them from the
 * classpath. ResourceBundles are loaded using <b>UTF-8</b> file-encoding.
 */
public class FolderSourcedControl extends ResourceBundle.Control {

  private static final Charset CHARSET = Charsets.UTF_8;

  private final File bundleFolder;

  /**
   * Initializes this instance. Resource bundles will be resolved from the given folder before
   * falling back to the class-path.
   *
   * @param bundleFolder the folder
   */
  public FolderSourcedControl(File bundleFolder) {
    this.bundleFolder = bundleFolder;
  }

  @Override
  public List<String> getFormats(String baseName) {
    if (baseName == null) {
      throw new NullPointerException();
    }
    return Arrays.asList("properties"); // NON-NLS
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                                  boolean reload) throws IOException {

    String bundleName = toBundleName(baseName, locale);
    ResourceBundle bundle = null;

    final String resourceName = toResourceName(bundleName, "properties"); // NON-NLS
    InputStream stream = null;
    final File bundleFile = new File(bundleFolder, resourceName);

    if (bundleFile.isFile()) {
      stream = new FileInputStream(bundleFile);
    } else if (reload) {
      URL url = loader.getResource(resourceName);
      if (url != null) {
        URLConnection connection = url.openConnection();
        if (connection != null) {
          // Disable caches to get fresh data for
          // reloading.
          connection.setUseCaches(false);
          stream = connection.getInputStream();
        }

      } else {
        stream = loader.getResourceAsStream(resourceName);
      }
    }
    if (stream != null) {
      Reader reader = new InputStreamReader(stream, CHARSET);
      try {

        bundle = new PropertyResourceBundle(reader);
      } finally {
        // this also closes the stream
        reader.close();
      }
    }

    return bundle;
  }
}
