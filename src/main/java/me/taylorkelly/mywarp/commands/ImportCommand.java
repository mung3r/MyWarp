package me.taylorkelly.mywarp.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;

import org.bukkit.command.CommandSender;

public class ImportCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ImportCommand(MyWarp plugin) {
        super("import");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.import"));
        setUsage("/warp import §9<SQLite|MySQL>");
        setArgumentRange(1, 1);
        setIdentifiers("import");
        setPermission("mywarp.admin.import");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        boolean importMySQL;

        if (args[0].equalsIgnoreCase("mysql")) {
            importMySQL = true;
        } else if (args[0].equalsIgnoreCase("sqlite")) {
            importMySQL = false;
        } else {
            return false;
        }

        try {
            int counter = 0;
            ConnectionManager importConnection = new ConnectionManager(
                    importMySQL, false, true);
            HashMap<String, Warp> importedWarps = importConnection.getMap();

            for (Entry<String, Warp> importedWarpEntry : importedWarps
                    .entrySet()) {
                String name = importedWarpEntry.getKey();
                Warp importedWarp = importedWarpEntry.getValue();

                if (plugin.getWarpList().warpExists(name)) {
                    executor.sendMessage(LanguageManager.getString(
                            "error.import.exists").replaceAll("%warp%", name));
                } else {
                    plugin.getWarpList().addWarp(name, importedWarp);
                    counter++;
                }
            }
            executor.sendMessage(counter + " warps were imported sucessfully.");
            return true;
        } catch (DataConnectionException ex) {
            executor.sendMessage(LanguageManager
                    .getString("error.import.noConnection") + ex.getMessage());
            return true;
        }
    }
}
