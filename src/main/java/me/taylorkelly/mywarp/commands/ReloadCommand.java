package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.command.CommandSender;

public class ReloadCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ReloadCommand(MyWarp plugin) {
        super("Reload");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.reload"));
        setUsage("/warp reload");
        setArgumentRange(0, 0);
        setIdentifiers("reload");
        setPermission("mywarp.admin.reload");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        plugin.reloadConfig();
        WarpSettings.initialize(plugin);
        LanguageManager.initialize(plugin);
        executor.sendMessage(LanguageManager.getString("reload.config"));
        return true;
    }

}
