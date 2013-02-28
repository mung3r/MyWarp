package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Lister;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ListCommand(MyWarp plugin) {
        super("List");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.list"));
        setUsage("/warp list §8["
                + LanguageManager.getColorlessString("help.usage.owner")
                + "] §9["
                + LanguageManager.getColorlessString("help.usage.pageNumber")
                + "]");
        setArgumentRange(0, 2);
        setIdentifiers("list");
        setPermission("mywarp.warp.basic.list");
    }

    @Override
    public void execute(CommandSender sender, String identifier,
            String[] args) throws CommandException {
        String creator = null;
        int page = 0;

        // This command handles listing depending on how many parameters are
        // given, so the following code splits the incoming input
        // into different possibilities

        // No arguments: /warp list
        if (args.length == 0) {
            page = 1;
            // One argument: Either /warp list # or /warp list player
        } else if (args.length == 1) {
            if (isInteger(args[0])) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    // catch possible integer overflow
                    throw new CommandException(LanguageManager.getString("list.page.invalid"));
                }
            } else {
                if (args[0].equals("own")) {
                    if (!(sender instanceof Player)) {
                        throw new CommandException(LanguageManager
                                .getString("list.console"));
                    }
                    creator = sender.getName();
                } else {
                    creator = args[0];
                }
                page = 1;
            }
            // Two arguments: /warp list player #
        } else if (args.length == 2) {
            if (args[0].equals("own")) {
                if (!(sender instanceof Player)) {
                    throw new CommandException(LanguageManager
                            .getString("list.console"));
                }
                creator = sender.getName();
            } else {
                creator = args[0];
            }
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // catch possible integer overflow
                throw new CommandException(LanguageManager.getString("list.page.invalid"));
            }
        }
        
        Lister lister = new Lister(sender, creator, page,
                plugin.getWarpList());
        lister.listWarps();
    }

    /**
     * Extremely fast helper method to determine whether a string is an Integer
     * or not
     * 
     * @param str
     *            the string to check
     * @return true if the String is an Integer, false if not
     */
    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }
}
