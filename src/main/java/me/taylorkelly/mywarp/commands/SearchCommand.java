package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Searcher;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SearchCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public SearchCommand(MyWarp plugin) {
        super("Search");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.search"));
        setUsage("/warp search §9<"
                + LanguageManager.getColorlessString("help.usage.query") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("search");
        setPermission("mywarp.warp.basic.search");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        Searcher searcher = new Searcher(plugin.getWarpList());
        searcher.addExecutor(executor, player);
        searcher.setQuery(StringUtils.join(args, ' '));
        searcher.search();
        return true;
    }
}
