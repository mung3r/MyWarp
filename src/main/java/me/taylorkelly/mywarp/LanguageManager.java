package me.taylorkelly.mywarp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.ChatColor;

import me.taylorkelly.mywarp.utils.WarpLogger;

public class LanguageManager {

    private static MyWarp plugin;

    private static HashMap<String, String> languageMap = new HashMap<String, String>();

    public static void initialize(MyWarp plugin) {
        LanguageManager.plugin = plugin;

        createLanguageFile("en_US");
        try {
            loadLanguage(WarpSettings.locale);
            WarpLogger.info("Using localization: " + WarpSettings.locale);
        } catch (IOException e) {
            WarpLogger.severe("Could not load file: ." + File.separator
                    + plugin.getDataFolder().getName() + File.separator
                    + WarpSettings.locale + ".txt, defaulting to en_US");
            try {
                loadLanguage("en_US");
            } catch (IOException e1) {
                WarpLogger.severe("Could not load file: ." + File.separator
                        + plugin.getDataFolder().getName() + File.separator
                        + WarpSettings.locale + ".txt");
            }
        }
    }

    private static void createLanguageFile(String name) {
        File actual = new File(plugin.getDataFolder(), name + ".txt");

        InputStream input = plugin.getResource("lang/" + name + ".txt");
        if (!actual.exists()) {
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(plugin.getDataFolder()
                            + File.separator + name + ".txt");
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    WarpLogger.info("Default language file written: " + name + ".txt");
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

    private static void checkLanguageFile(String name) {
        checkLanguageFile(name, name);
    }

    private static void checkLanguageFile(String name, String original) {
        Scanner scan = new Scanner(plugin.getResource("lang/" + original + ".txt"),
                "UTF-8");
        HashMap<String, String> map = new HashMap<String, String>();

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            map.put(line.split(":", 2)[0], line.split(":", 2)[1]);
        }

        File f = new File(plugin.getDataFolder() + File.separator + name + ".txt");
        if (f.exists()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(f), "UTF-8"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.split(":", 2).length != 2) continue;
                    String key = line.split(":", 2)[0];
                    if (map.containsKey(key))
                        map.remove(key);
                }
                br.close();
            } catch (Exception e) {
                WarpLogger.severe("Could not find file: "
                        + plugin.getDataFolder().getName() + File.separator + name
                        + ".txt");
            }

            if (!map.isEmpty()) {
                BufferedWriter output = null;
                try {
                    output = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(f, true), "UTF-8"));
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        output.newLine();
                        output.write(entry.getKey() + ":" + entry.getValue());
                    }
                    WarpLogger.info("Updated language file: " + name + ".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private static void loadLanguage(String locale) throws IOException {
        if (!locale.equals("en_US")) {
            checkLanguageFile(locale, "en_US");
        }
        File f = new File(plugin.getDataFolder(), locale + ".txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.split(":", 2).length != 2) continue;
            languageMap.put(line.split(":", 2)[0], line.split(":", 2)[1]);
        }
        br.close();
    }

    public static String getString(String s) {
        return languageMap.get(s) != null ? ChatColor.translateAlternateColorCodes('§',
                languageMap.get(s)).replaceAll("%n", "\n") : s;
    }

    public static String getColorlessString(String s) {
        return ChatColor.stripColor(getString(s));
    }
}
