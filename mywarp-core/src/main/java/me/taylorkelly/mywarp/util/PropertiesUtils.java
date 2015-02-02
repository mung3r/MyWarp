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

package me.taylorkelly.mywarp.util;

import com.google.common.base.Charsets;

import org.apache.commons.lang.text.StrBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides utility methods for working with {@link Properties}.
 */
public final class PropertiesUtils {

  private static final Logger log = Logger.getLogger(PropertiesUtils.class.getName());

  /**
   * Block initialization of this class.
   */
  private PropertiesUtils() {
  }

  /**
   * Copies key-value-pairs found in the given Properties but not in the given File into it. The
   * file is expected to exist and contain a structure readable as Properties. <p> Instead of using
   * the sytem's default encoding, this method will use <b>UTF-8</b> to write the file. A comment
   * about the encoding is added to the file if changes need to be saved. </p>
   *
   * @param file     the file that potentially has missing values
   * @param defaults the Properties that contains all values
   * @throws IOException if reading or writing the file fails for some reason
   */
  public static void copyMissing(File file, Properties defaults) throws IOException {
    copyMissing(file, defaults, Charsets.UTF_8);
  }

  /**
   * Copies key-value-pairs found in the given Properties but not in the given File into it. The
   * file is not created by this method but is expected to exist and contain a structure readable as
   * Properties. <p> Instead of using the sytem's default encoding, this method will use given one
   * to write the file. A comment about the encoding is added to the file if changes need to be
   * saved. </p>
   *
   * @param file     the file that potentially has missing values
   * @param defaults the Properties that contains all values
   * @param charset  the charset that should be used to read and write the file
   * @throws IOException if reading or writing the file fails for some reason
   */
  public static void copyMissing(File file, Properties defaults, Charset charset)
      throws IOException {
    Properties loaded = new Properties();

    InputStream inputStream;
    Reader reader = null;
    try {
      inputStream = new FileInputStream(file);
      reader = new InputStreamReader(inputStream, charset);
      loaded.load(reader);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          log.log(Level.FINER, "Failed to close Reader for '" + file.getAbsolutePath() + "'.",
                  e); // NON-NLS
        }
      }
    }

    boolean needsStorage = false;
    for (Entry<Object, Object> entry : defaults.entrySet()) {
      if (loaded.containsKey(entry.getKey().toString())) {
        continue;
      }
      loaded.setProperty(entry.getKey().toString(), entry.getValue().toString());
      needsStorage = true;
    }

    if (needsStorage) {
      OutputStream outputStream = null;
      Writer writer = null;

      try {
        outputStream = new FileOutputStream(file);
        writer = new OutputStreamWriter(outputStream, charset);
        loaded.store(writer, getHeader(charset));
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            log.log(Level.FINER, "Failed to close Writer to '" + file.getAbsolutePath() + "'.",
                    e); // NON-NLS
          }
        }
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            log.log(Level.FINER,
                    "Failed to close OutputStream to '" + file.getAbsolutePath() // NON-NLS
                    + "'.", e);
          }
        }
      }
    }

  }

  /**
   * Gets the header for the given Charset. The header includes informations about the Charset and
   * on how to edit the file.
   *
   * @param charset the Charset
   * @return the header
   */
  private static String getHeader(Charset charset) {
    StrBuilder ret = new StrBuilder();
    ret.append("This file is encoded in '"); // NON-NLS
    ret.append(charset.name());
    ret.append("', you MUST NOT change this encoding or special characters will brake."); // NON-NLS
    ret.appendNewLine();
    ret.append(
        "Use an appropriate editor when editing this file and make sure you know what you are doing!"); // NON-NLS
    return ret.toString();
  }
}
