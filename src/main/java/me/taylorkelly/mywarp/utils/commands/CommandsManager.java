package me.taylorkelly.mywarp.utils.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandsManager {

    /**
     * Mapping of commands (including aliases) with a description. Root commands
     * are stored under a key of null, whereas child commands are cached under
     * their respective {@link Method}. The child map has the key of the command
     * name (one for each alias) with the method.
     */
    private Map<Method, Map<String, Method>> commands = new HashMap<Method, Map<String, Method>>();

    /**
     * Stores the instances under their respective {@link Method}.
     */
    private HashMap<Method, Object> instances = new HashMap<Method, Object>();

    /**
     * Stores the injector
     */
    private Injector injector;

    /**
     * Creates the CommandManager and initiates the injector.
     */
    public CommandsManager() {
        injector = new Injector();
    }

    /**
     * Checks if the command is usable by all possible command senders. If not,
     * it checks if the given sender may use the command.
     * 
     * @param command
     *            the command to check
     * @param sender
     *            the sender the check
     * @throws CommandException
     *             If the sender may not execute the command
     */
    private void checkSender(Method method, CommandSender sender) throws CommandException {

        Class<?>[] params = method.getParameterTypes();

        if (params[1] != CommandSender.class && !params[1].isAssignableFrom(sender.getClass())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("error.cmd.invalidSender", sender));
        }
    }

    /**
     * This one always tries to execute the root command identified by the given
     * topLevel String. If this is a nested command it calls the sub-commands
     * accordingly.
     * 
     * @param sender
     *            the sender of the command
     * @param fullArgs
     *            the full arguments given when executing the command
     * @throws CommandException
     */
    private void execute(CommandSender sender, String topCmdName, String[] fullArgs) throws CommandException {
        Method method = commands.get(null).get(topCmdName);

        // If we execute a nested command and the second argument is a valid
        // sub-command, we need to execute this sub-command. If not we
        // execute the root-command's method.
        if (method.isAnnotationPresent(NestedCommand.class) && fullArgs.length >= 2
                && hasSubComand(method, fullArgs[1])) {
            executeMethod(commands.get(method).get(fullArgs[1]), sender, 1, fullArgs);
        } else {
            executeMethod(method, sender, 0, fullArgs);
        }
    }

    /**
     * Executes the given method by calling
     * {@link #invokeMethod(Method, Object, Object[])}. Before doing this it
     * will convert the command arguments into {@link CommandContext} and check
     * if all conditions are met (command-permissions, sender, args-count...)
     * 
     * 
     * @param method
     *            the method of the command
     * @param sender
     *            the sender of the command
     * @param level
     *            usage level in the arguments, 0 for root-, 1 for sub-commands.
     * @param fullArgs
     *            all arguments given when executing the command
     * @throws CommandException
     *             When conditions defined for this command (flags,
     *             argument-count, permissions...) are not met.
     */
    private void executeMethod(Method method, CommandSender sender, int level, String[] fullArgs)
            throws CommandException {
        Command cmd = method.getAnnotation(Command.class);
        if (!hasPermission(cmd, sender)) {
            throw new CommandPermissionsException();
        }
        checkSender(method, sender);

        final Set<Character> valueFlags = new HashSet<Character>();
        char[] flags = cmd.flags().toCharArray();
        Set<Character> newFlags = new HashSet<Character>();
        for (int i = 0; i < flags.length; ++i) {
            if (flags.length > i + 1 && flags[i + 1] == ':') {
                valueFlags.add(flags[i]);
                ++i;
            }
            newFlags.add(flags[i]);
        }

        CommandContext context = new CommandContext(fullArgs, level, valueFlags, sender);

        // show the command-help if the only argument is a '?'
        if (context.argsLength() == 1 && context.getString(0).equals("?")) {
            sender.sendMessage(getUsage(fullArgs, 1, sender, cmd));
            return;
        }

        // test if the command has too few arguments
        if (context.argsLength() < cmd.min()) {
            throw new CommandUsageException(MyWarp.inst().getLocalizationManager()
                    .getString("error.cmd.toFewArgs", sender), getUsage(fullArgs, level, sender, cmd));
        }

        // test if the command has too many arguments
        if (cmd.max() != -1 && context.argsLength() > cmd.max()) {
            throw new CommandUsageException(MyWarp.inst().getLocalizationManager()
                    .getString("error.cmd.toManyArgs", sender), getUsage(fullArgs, level, sender, cmd));
        }

        // loop through all flags and catch unsupported ones
        for (char flag : context.getFlags()) {
            if (!newFlags.contains(flag)) {
                throw new CommandUsageException(MyWarp.inst().getLocalizationManager()
                        .getString("error.cmd.unknownFlag", sender)
                        + " " + flag, getUsage(fullArgs, level, sender, cmd));
            }
        }

        // if economy support is enabled we need to check if the sender can
        // afford using the command
        if (MyWarp.inst().getWarpSettings().economyEnabled && cmd.fee() != Fee.NONE) {
            double fee = MyWarp.inst().getPermissionsManager().getEconomyPrices(sender).getFee(cmd.fee());

            if (!MyWarp.inst().getEconomyLink().canAfford(sender, fee)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("error.economy.cannotAfford", sender, "%amount%",
                                Double.toString(fee)));
            }
        }
        Object instance = instances.get(method);

        // prepare arguments given to the method
        Object[] methodArgs = new Object[2];
        methodArgs[0] = context;
        methodArgs[1] = sender;

        invokeMethod(method, instance, methodArgs, sender);

        // if economy support is enabled whitdraw the sender - at this point the
        // command should have been executed without any errors
        // (see CommandException)
        if (MyWarp.inst().getWarpSettings().economyEnabled && cmd.fee() != Fee.NONE) {

            MyWarp.inst()
                    .getEconomyLink()
                    .withdrawSender(sender,
                            MyWarp.inst().getPermissionsManager().getEconomyPrices(sender).getFee(cmd.fee()));
        }
    }

    /**
     * Creates a {@link CharSequence} that contains all flags available for a
     * command (if any). The sequence will be enclosed in brackets or empty of
     * not flag is acceptable. Value flags will not be included.
     * 
     * @param cmd
     *            the command to check
     * @param sender
     *            the command-sender who will receive this informations
     * @return all acceptable arguments
     */
    public CharSequence getArguments(Command cmd, CommandSender sender) {
        final String flags = cmd.flags();

        final StringBuilder arguments = new StringBuilder();
        if (flags.length() > 0) {
            String flagString = flags.replaceAll(".:", "");
            if (flagString.length() > 0) {
                arguments.append("[-");
                for (int i = 0; i < flagString.length(); ++i) {
                    arguments.append(flagString.charAt(i));
                }
                arguments.append("] ");
            }
        }
        arguments.append(parseCmdUsage(cmd, sender));

        return arguments;
    }

    /**
     * Parses the usage of the given command by replacing key-words with the
     * translations received from the LocalizationManager.
     * 
     * @param cmd
     *            the command
     * @param sender
     *            the command-sender who will receive this informations
     * @return the translated command usage
     */
    public String parseCmdUsage(Command cmd, CommandSender sender) {
        String ret = StringUtils.replace(cmd.usage(), "player",
                MyWarp.inst().getLocalizationManager().getString("cmd.usage.player", sender));
        ret = StringUtils.replace(ret, "name",
                MyWarp.inst().getLocalizationManager().getString("cmd.usage.name", sender));
        ret = StringUtils.replace(ret, "group",
                MyWarp.inst().getLocalizationManager().getString("cmd.usage.group", sender));
        ret = StringUtils.replace(ret, "world",
                MyWarp.inst().getLocalizationManager().getString("cmd.usage.world", sender));
        ret = StringUtils.replace(ret, "creator",
                MyWarp.inst().getLocalizationManager().getString("cmd.usage.creator", sender));

        return ret;
    }

    /**
     * Returns a sorted set containing all commands usable (meaning he has the
     * permission to use this command). The set will be sorted by the first
     * alias of the command as this is the main alias.
     * 
     * @param sender
     *            the sender
     * @param rootIdentifier
     *            the root-level identifier of the commands
     * @return a list of usable commands
     */
    public Set<Command> getUsableCommands(CommandSender sender, String rootIdentifier) {
        TreeSet<Command> ret = new TreeSet<Command>(new Comparator<Command>() {
            @Override
            public int compare(Command cmd1, Command cmd2) {
                return cmd1.aliases()[0].compareTo(cmd2.aliases()[0]);
            }
        });
        Method method = commands.get(null).get(rootIdentifier);

        for (Entry<Method, Map<String, Method>> entry : commands.entrySet()) {
            if (entry.getKey() != method) {
                continue;
            }
            for (Method cmdMethod : entry.getValue().values()) {
                Command cmd = cmdMethod.getAnnotation(Command.class);
                if (hasPermission(cmd, sender) && !ret.contains(cmd)) {
                    ret.add(cmd);
                }
            }
        }
        return ret;
    }

    /**
     * Creates a colored multi-line string that reflects the usage of the given
     * command. The first line holds the full syntax using the root command
     * provided and all acceptable sub-command aliases together with acceptable
     * flags as returned by {@link #getArguments(Command)} and the additional
     * syntax defined by the command. The second line holds the description as
     * described by the command.
     * 
     * @param args
     *            the arguments that were used by the user
     * @param level
     *            the level of command to check - 0 for root-, 1 for
     *            sub-commands.
     * @param sender
     *            the command-sender who will receive this informations
     * @param cmd
     *            the command
     * @return a string containing the command's usage
     */
    public String getUsage(String[] args, int level, CommandSender sender, Command cmd) {
        final StringBuilder usage = new StringBuilder();
        usage.append(ChatColor.GOLD);
        usage.append('/');

        for (int i = 0; i <= level; ++i) {
            usage.append(args[i]);
            usage.append(' ');
        }
        usage.append(ChatColor.GRAY);

        usage.append(getArguments(cmd, sender));

        final String desc = MyWarp.inst().getLocalizationManager().getString(cmd.desc(), sender);
        if (desc.length() > 0) {
            usage.append("\n");
            usage.append(ChatColor.RESET);
            usage.append(desc);
        }
        return usage.toString();
    }

    /**
     * Bridge for bukkit's command-system. Will return false if the command name
     * is not one of the registered root-commands. If it is, it will call
     * {@link #execute(CommandSender, String, String[])} to execute the command
     * and return true.
     * 
     * @param sender
     *            the command sender
     * @param command
     *            the command as it was send
     * @param commandLabel
     *            the label of the command (equals the alias used on root-level,
     *            can configured by users)
     * @param args
     *            all arguments parsed to the command (this MUST NOT include the
     *            command's identifier on root-level!)
     * @return true if the command is managed by MyWarp, false if not
     */
    public boolean handleBukkitCommand(CommandSender sender, org.bukkit.command.Command command,
            String commandLabel, String[] args) {
        if (!hasRootCommand(command.getName())) {
            return false;
        }
        // add the used root alias to the args as we need it
        args = (String[]) ArrayUtils.add(args, 0, commandLabel);

        try {
            execute(sender, command.getName(), args);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED
                    + MyWarp.inst().getLocalizationManager().getString("error.noPermission", sender));
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(e.getUsage());
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    /**
     * Checks if the given sender has the permissions defined for the given
     * command.
     * 
     * @param command
     *            the command to check
     * @param sender
     *            the sender to check
     * @return true if the sender may use the command, false if not
     */
    private boolean hasPermission(Command command, CommandSender sender) {
        for (String perm : command.permissions()) {
            if (MyWarp.inst().getPermissionsManager().hasPermission(sender, perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if we have a root-command under the given identifier.
     * 
     * @param identifier
     *            the command's identifier
     * @return true if such a command is registered, false if not
     */
    public boolean hasRootCommand(String identifier) {
        return commands.get(null).containsKey(identifier);
    }

    public boolean hasSubComand(Method parent, String identifier) {
        return commands.get(parent).containsKey(identifier);
    }

    /**
     * Invokes the given method using the given instance and arguments.
     * Exceptions (excluding {@link CommandException}s) will be printed into the
     * console.
     * 
     * @param method
     *            the method
     * @param instance
     *            the corresponding instance
     * @param methodArgs
     *            the arguments used to invoke the method
     * @param sender
     *            the CommandSender that caused this invocation process
     * @throws CommandException
     */
    private void invokeMethod(Method method, Object instance, Object[] methodArgs, CommandSender sender)
            throws CommandException {
        try {
            method.invoke(instance, methodArgs);
        } catch (IllegalArgumentException e) {
            MyWarp.logger().log(Level.SEVERE, "Failed to execute command", e);
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("error.cmd.unknown", sender));
        } catch (IllegalAccessException e) {
            MyWarp.logger().log(Level.SEVERE, "Failed to execute command", e);
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("error.cmd.unknown", sender));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                throw (CommandException) e.getCause();
            } else {
                MyWarp.logger().log(Level.SEVERE, "Failed to execute command", e);
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("error.cmd.unknown", sender));
            }
        }
    }

    /**
     * Registers all methods annotated with the {@link Command}-Annotation in
     * the given class.
     * 
     * @param cls
     *            the class
     */
    public void register(Class<?> cls) {
        registerMethods(cls, null);
    }

    /**
     * Registers all methods annotated with the {@link Command}-Annotation in
     * the given class using the given method as parent. This means that the
     * registered method will be a sub-command of the command defined by the
     * parent-method. Root commands take null as parent.
     * 
     * This method will create instances for the given classes as needed.
     * 
     * @param cls
     *            the class
     * @param parent
     *            the method of the root command
     */
    public void registerMethods(Class<?> cls, Method parent) {
        Object obj = null;
        // creates an instance of the class containing our commands
        if (injector != null) {
            obj = injector.getInstance(cls);
        }
        registerMethods(cls, obj, parent);
    }

    /**
     * Registers all methods annotated with the {@link Command}-Annotation in
     * the given class. This method will do all the work storing all aliases and
     * instances into the corresponding maps.
     * 
     * @param cls
     *            the class
     * @param obj
     *            the object instance of the class
     * @param parent
     *            the method of the root command
     */
    private void registerMethods(Class<?> cls, Object obj, Method parent) {
        Map<String, Method> map;

        if (commands.containsKey(parent)) {
            map = commands.get(parent);
        } else {
            map = new HashMap<String, Method>();
            commands.put(parent, map);
        }

        for (Method method : cls.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            // cache the instance used to access the method
            instances.put(method, obj);

            Command cmd = method.getAnnotation(Command.class);

            // cache all aliases
            for (String alias : cmd.aliases()) {
                map.put(alias, method);
            }

            // Look for nested commands, they need to be cached too so we can
            // look them up quickly when processing commands.
            if (method.isAnnotationPresent(NestedCommand.class)) {
                NestedCommand nestedCmd = method.getAnnotation(NestedCommand.class);

                for (Class<?> nestedCls : nestedCmd.value()) {
                    registerMethods(nestedCls, method);
                }
            }

        }
    }
}
