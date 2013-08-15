package me.taylorkelly.mywarp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import me.taylorkelly.mywarp.utils.UnicodeBOMInputStream;
import me.taylorkelly.mywarp.utils.UnicodeBOMInputStream.BOM;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

/**
 * This class provides several static that are needed to manage languages. It
 * must be initialized via the {{@link #initialize(MyWarp)} first!
 * 
 */
public class LanguageManager {

    /**
     * The language map with all used messages stored under their individual
     * key.
     */
    private HashMap<String, String> languageMap = new HashMap<String, String>();

    public LanguageManager() {
        createLanguageFile("en_US");
        createLanguageFile("de_DE");
        try {
            loadLanguage(MyWarp.inst().getWarpSettings().locale);
            MyWarp.logger().info("Using localization: " + MyWarp.inst().getWarpSettings().locale);
        } catch (IOException e) {
            MyWarp.logger().severe(
                    "Could not load file: ." + File.separator + MyWarp.inst().getDataFolder().getName()
                            + File.separator + MyWarp.inst().getWarpSettings().locale
                            + ".txt, defaulting to en_US");
            try {
                loadLanguage("en_US");
            } catch (IOException e1) {
                MyWarp.logger().severe(
                        "Could not load file: ." + File.separator + MyWarp.inst().getDataFolder().getName()
                                + File.separator + MyWarp.inst().getWarpSettings().locale + ".txt");
            }
        }
    }

    /**
     * Creates the language file under the given name by copying it out of the
     * jar. If the file already exists, it will call
     * {@link #checkLanguageFile(String)} to make sure the language file is up
     * to date.
     * 
     * This method will print errors if the language-file does not exist in the
     * jar!
     * 
     * @param name
     *            the name of the language file
     */
    private void createLanguageFile(String name) {
        
        
        
        File actual = new File(MyWarp.inst().getDataFolder(), name + ".txt");

        InputStream input = MyWarp.inst().getResource("lang/" + name + ".txt");
        if (!actual.exists()) {
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(MyWarp.inst().getDataFolder() + File.separator + name
                            + ".txt");
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    MyWarp.logger().info("Default language file written: " + name + ".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            checkLanguageFile(name);
        }
    }

    /**
     * Calls {{@link checkLanguageFile(String, String)} while the given name is
     * both, the file to check and the original.
     * 
     * @param name
     *            the name of the file that should be checked (equals the
     *            filename without ending)
     */
    private void checkLanguageFile(String name) {
        checkLanguageFile(name, name);
    }

    /**
     * Will determine if every keys that exist in the original file are also
     * present in the given file that is bundled with the plugin. Missing keys
     * will be added together with their corresponding message afterwards.
     * 
     * @param name
     *            the name of the file that should be checked (equals the
     *            filename without ending)
     * @param original
     *            the name of the original thats bundled with the MyWarp.inst()
     *            (equals the filename without ending)
     */
    private void checkLanguageFile(String name, String original) {
        HashMap<String, String> map = new HashMap<String, String>();

        // Read the original file provided in the plugin's jar file
        Scanner scan = null;
        try {
            // forcing UTF-8 BOM default since we provide the file
            scan = new Scanner(new UnicodeBOMInputStream(MyWarp.inst().getResource(
                    "lang/" + original + ".txt"), BOM.UTF_8).skipBOM(), "UTF-8");
        } catch (IOException e) {
            scan = new Scanner(MyWarp.inst().getResource("lang/" + original + ".txt"), "UTF-8");
        }

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith("#")) {
                continue;
            }
            if (line.split(":", 2).length != 2) {
                MyWarp.logger().severe(
                        "Error reading default language file " + original + ".txt, line " + line
                                + " - Please inform the developer.");
                continue;
            }

            map.put(line.split(":", 2)[0], line.split(":", 2)[1]);
        }
        if (scan != null) {
            scan.close();
        }

        // Read the given file and determine if it contains all keys
        File f = new File(MyWarp.inst().getDataFolder() + File.separator + name + ".txt");
        if (f.exists()) {
            UnicodeBOMInputStream uis = null;
            BufferedReader br = null;

            try {
                uis = new UnicodeBOMInputStream(new FileInputStream(f));
                br = new BufferedReader(new InputStreamReader(uis.skipBOM(), "UTF-8"));

                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#") || line.split(":", 2).length != 2) {
                        continue;
                    }
                    String key = line.split(":", 2)[0];
                    if (map.containsKey(key)) {
                        map.remove(key);
                    }
                }
            } catch (FileNotFoundException e) {
                MyWarp.logger().severe("Could not find file: " + f.getPath() + File.separator + f.getName());
            } catch (IOException e) {
                MyWarp.logger().severe(
                        "Failed to read file: " + f.getPath() + File.separator + f.getName() + ": " + e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
                if (uis != null) {
                    try {
                        uis.close();
                    } catch (IOException e) {
                    }
                }
            }

            // Write missing keys into the file
            if (!map.isEmpty()) {
                BufferedWriter output = null;
                try {
                    output = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        output.newLine();
                        output.write(entry.getKey() + ":" + entry.getValue());
                    }
                    MyWarp.logger().info("Updated language file: " + name + ".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Loads the language-file of the given locale into the language map. If the
     * language-file is not one of the default ones it is checked via
     * {@link #checkLanguageFile(String, String)} first.
     * 
     * @param locale
     *            the locale that should be loaded (equals the file name without
     *            ending)
     * @throws IOException
     *             if any problems encounter during reading.
     */
    private void loadLanguage(String locale) throws IOException {
        UnicodeBOMInputStream uis = null;
        BufferedReader br = null;
        try {
            if (!locale.equals("en_US") || !locale.equals("de_DE")) {
                checkLanguageFile(locale, "en_US");
            }
            File f = new File(MyWarp.inst().getDataFolder(), locale + ".txt");

            uis = new UnicodeBOMInputStream(new FileInputStream(f));
            br = new BufferedReader(new InputStreamReader(uis.skipBOM(), "UTF-8"));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.split(":", 2).length != 2) {
                    continue;
                }
                languageMap.put(line.split(":", 2)[0], line.split(":", 2)[1]);
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (uis != null) {
                uis.close();
            }
        }
    }

    /**
     * Returns the string find under the the given key in the language map with
     * replaced new-line and color codes. Will return the key if no
     * corresponding string could be found
     * 
     * @param key
     *            the key of the string
     * @return the corresponding string out of the language map
     */
    public String getString(String key) {
        return languageMap.get(key) != null ? ChatColor.translateAlternateColorCodes('§',
                StringUtils.replace(languageMap.get(key), "%n", "\n")) : key;
    }

    /**
     * Returns {@link #getString(String)} without colors
     * 
     * @param key
     *            the key of the string
     * @return the corresponding string out of the language map without colors
     */
    public String getColorlessString(String key) {
        return ChatColor.stripColor(getString(key));
    }

    /**
     * Returns {@link #getString(String)} and replaces all searchStrings with
     * the given replacements. Form is searchString, replacement, searchString2,
     * replacement2, etc. Will throw an exception if replacement's length is not
     * even.
     * 
     * @param key
     *            the key of the string
     * @param replacements
     *            the replacements - must be even
     * @return the corresponding string out of the language map with replaced
     *         values
     */
    public String getEffectiveString(String key, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("The given arguments length must be equal");
        }
        String trans = getString(key);

        for (int i = 0; i < replacements.length; i = i + 2) {
            trans = StringUtils.replace(trans, replacements[i], replacements[i + 1]);
        }
        return trans;
    }
}
