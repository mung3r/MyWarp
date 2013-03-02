package me.taylorkelly.mywarp.commands;

import java.util.ArrayList;
import java.util.List;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand extends BasicCommand implements Command {
    private static final int CMDS_PER_PAGE = 8;
    private MyWarp plugin;

    public HelpCommand(MyWarp plugin) {
        super("Help");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.help"));
        setUsage("["
                + LanguageManager.getColorlessString("help.usage.pageNumber")
                + "]");
        setArgumentRange(0, 1);
        setIdentifiers("help");
        setPermission("mywarp.warp.basic.help");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args) {
        int page = 0;
        if (args.length != 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
            }
        }

        List<Command> sortCommands = plugin.getCommandHandler().getCommands();
        List<Command> commands = new ArrayList<Command>();

        // Build list of permitted commands
        for (Command command : sortCommands) {
            if (command.isShownOnHelpMenu()) {
                if (plugin.getCommandHandler().hasPermission(sender,
                        command.getPermission())) {
                    commands.add(command);
                }
            }
        }

        int numPages = commands.size() / CMDS_PER_PAGE;
        if (commands.size() % CMDS_PER_PAGE != 0) {
            numPages++;
        }

        if (page >= numPages || page < 0) {
            page = 0;
        }
        sender.sendMessage(ChatColor.YELLOW + "----- " + ChatColor.WHITE
                + "MyWarp " + LanguageManager.getColorlessString("help.help")
                + " (" + (page + 1) + "/" + numPages + ")" + ChatColor.YELLOW
                + " -----");
        sender.sendMessage(ChatColor.GRAY
                + LanguageManager.getString("help.more"));

        int start = page * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (end > commands.size()) {
            end = commands.size();
        }
        for (int c = start; c < end; c++) {
            Command cmd = commands.get(c);
            sender.sendMessage(ChatColor.GOLD + "/warp "
                    + StringUtils.join(cmd.getIdentifiers(), '|') + " "
                    + ChatColor.GRAY + cmd.getUsage());
        }
    }
}
