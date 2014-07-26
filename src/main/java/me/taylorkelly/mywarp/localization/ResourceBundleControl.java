package me.taylorkelly.mywarp.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import me.taylorkelly.mywarp.MyWarp;

import com.google.common.base.Preconditions;

/**
 * A custom implementation of {@link java.util.ResourceBundle.Control} for
 * Yaml-files only. ResourceBundle-files are expected to be found inside the
 * plugin's data folder. If the file does not exist (which is unlikely to
 * happen), it tries to load the file that is bundled with the plugin as
 * fallback.
 */
public class ResourceBundleControl extends ResourceBundle.Control {

    @Override
    public List<String> getFormats(String baseName) {
        Preconditions.checkNotNull(baseName);
        return Collections.unmodifiableList(Arrays.asList("yml", "yaml"));
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
            boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        Preconditions.checkNotNull(baseName);
        Preconditions.checkNotNull(locale);
        Preconditions.checkNotNull(format);
        Preconditions.checkNotNull(loader);

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, format);
        File file = new File(MyWarp.inst().getDataFolder(), resourceName);

        InputStream is = null;
        ResourceBundle bundle = null;

        try {
            if (file.isFile()) {
                // load the file from disk
                is = new FileInputStream(file);
            } else {
                // fallback: load the file out of the jar
                if (reload) {
                    URL url = loader.getResource("lang/" + resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            connection.setUseCaches(false);
                            is = connection.getInputStream();
                        }
                    }
                } else {
                    is = loader.getResourceAsStream("lang/" + resourceName);
                }
            }
            bundle = new YamlResourceBundle(is);

        } finally {
            if (is != null) {
                is.close();
            }
        }
        return bundle;
    }
}