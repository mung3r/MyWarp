/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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
package me.taylorkelly.mywarp.utils.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;

/**
 * The context of a commands that provides convenient methods to parse the
 * underlying argument arrays into primitives.
 */
public class CommandContext {
    private final String[] command;
    private final List<String> parsedArgs;
    private final List<Integer> originalArgIndices;
    private final String[] originalArgs;
    private final Set<Character> booleanFlags = new HashSet<Character>();
    private final Map<Character, String> valueFlags = new HashMap<Character, String>();

    /**
     * Matches flags.
     */
    private static final Pattern FLAG_PATTERN = Pattern.compile("^-[a-zA-Z\\?]+$");

    /**
     * Initializes this context.
     * 
     * @param args
     *            An array with arguments. Empty strings outside quotes will be
     *            removed.
     * @param sender
     *            the command-sender who initiated the command
     * @throws CommandException
     *             This is thrown if flag fails for some reason.
     */
    public CommandContext(String args, CommandSender sender) throws CommandException {
        this(args.split(" "), 0, null, sender);
    }

    /**
     * Initializes this context.
     * 
     * @param args
     *            An array with arguments. Empty strings outside quotes will be
     *            removed.
     * @param sender
     *            the command-sender who initiated the command
     * @throws CommandException
     *             This is thrown if flag fails for some reason.
     */
    public CommandContext(String[] args, CommandSender sender) throws CommandException {
        this(args, 0, null, sender);
    }

    /**
     * Initializes this context.
     * 
     * @param args
     *            An array with arguments. Empty strings outside quotes will be
     *            removed.
     * @param valueFlags
     *            A set containing all value flags. Pass <code>null</code> to
     *            disable value flag parsing.
     * @param sender
     *            the command-sender who initiated the command
     * @throws CommandException
     *             This is thrown if flag fails for some reason.
     */
    public CommandContext(String args, Set<Character> valueFlags, CommandSender sender)
            throws CommandException {
        this(args.split(" "), 0, valueFlags, sender);
    }

    /**
     * Initializes this context.
     * 
     * @param args
     *            An array with arguments. Empty strings outside quotes will be
     *            removed.
     * @param valueFlags
     *            A set containing all value flags. Pass <code>null</code> to
     *            disable value flag parsing.
     * @param level
     *            An Integer representing where on what command level we are
     *            working - 0 for root, 1 for sub-commands.
     * @param sender
     *            the command-sender who initiated the command
     * @throws CommandException
     *             This is thrown if flag fails for some reason.
     */
    public CommandContext(String[] args, int level, @Nullable Set<Character> valueFlags, CommandSender sender)
            throws CommandException {
        if (valueFlags == null) {
            valueFlags = Collections.emptySet();
        }

        originalArgs = args;
        // make sure root- and sub-commands are parsed
        command = Arrays.copyOfRange(args, 0, ++level);

        // Eliminate empty args and combine multiword args first
        List<Integer> argIndexList = new ArrayList<Integer>(args.length);
        List<String> argList = new ArrayList<String>(args.length);
        for (int i = level; i < args.length; ++i) {
            String arg = args[i];
            if (arg.length() == 0) {
                continue;
            }

            argIndexList.add(i);

            if (arg.charAt(0) == '\'' || arg.charAt(0) == '"') {
                final StringBuilder build = new StringBuilder();
                final char quotedChar = arg.charAt(0);

                int endIndex;
                for (endIndex = i; endIndex < args.length; ++endIndex) {
                    final String arg2 = args[endIndex];
                    if (arg2.charAt(arg2.length() - 1) == quotedChar && arg2.length() > 1) {
                        if (endIndex != i) {
                            build.append(' ');
                        }
                        build.append(arg2.substring(endIndex == i ? 1 : 0, arg2.length() - 1));
                        break;
                    } else if (endIndex == i) {
                        build.append(arg2.substring(1));
                    } else {
                        build.append(' ').append(arg2);
                    }
                }

                if (endIndex < args.length) {
                    arg = build.toString();
                    i = endIndex;
                }

                // In case there is an empty quoted string
                if (arg.length() == 0) {
                    continue;
                }
                // else raise exception about hanging quotes?
            }
            argList.add(arg);
        }

        // Then flags

        this.originalArgIndices = new ArrayList<Integer>(argIndexList.size());
        this.parsedArgs = new ArrayList<String>(argList.size());

        for (int nextArg = 0; nextArg < argList.size();) {
            // Fetch argument
            String arg = argList.get(nextArg++);

            // Not a flag?
            if (arg.charAt(0) != '-' || arg.length() == 1 || !FLAG_PATTERN.matcher(arg).matches()) {
                originalArgIndices.add(argIndexList.get(nextArg - 1));
                parsedArgs.add(arg);
                continue;
            }

            // Handle flag parsing terminator --
            if (arg.equals("--")) {
                while (nextArg < argList.size()) {
                    originalArgIndices.add(argIndexList.get(nextArg));
                    parsedArgs.add(argList.get(nextArg++));
                }
                break;
            }

            // Go through the flag characters
            for (int i = 1; i < arg.length(); ++i) {
                char flagName = arg.charAt(i);

                if (valueFlags.contains(flagName)) {
                    if (this.valueFlags.containsKey(flagName)) {
                        throw new CommandException(MyWarp.inst().getLocalizationManager()
                                .getString("commands.library.flag-already-set", sender, flagName));
                    }

                    if (nextArg >= argList.size()) {
                        throw new CommandException(MyWarp.inst().getLocalizationManager()
                                .getString("commands.library.flag-no-value", sender, flagName));
                    }

                    // If it is a value flag, read another argument and add it
                    this.valueFlags.put(flagName, argList.get(nextArg++));
                } else {
                    booleanFlags.add(flagName);
                }
            }
        }
    }

    /**
     * Gets the command.
     * 
     * @return the command
     */
    public String[] getCommand() {
        return command;
    }

    /**
     * Gets the command as readable string.
     * 
     * @return the command
     */
    public String getCommandString() {
        return Joiner.on(' ').join(command);
    }

    /**
     * Returns whether the command is equal to the given command.
     * 
     * @param command
     *            the command
     * @return true if both are equal
     */
    public boolean matches(String command) {
        return getCommandString().equalsIgnoreCase(command);
    }

    /**
     * Gets the string at the given index.
     * 
     * @param index
     *            the index
     * @return the string or <code>null</code> if there is none
     */
    @Nullable
    public String getString(int index) {
        return parsedArgs.get(index);
    }

    /**
     * Gets the string at the given index. If there is none, the given default
     * is returned.
     * 
     * @param index
     *            the index
     * @param def
     *            the default
     * @return the string or the default
     */
    public String getString(int index, String def) {
        return index < parsedArgs.size() ? parsedArgs.get(index) : def;
    }

    /**
     * Joins all arguments, starting at the given initial index.
     * 
     * @param initialIndex
     *            the initial index
     * @return the joined arguments
     */
    public String getJoinedStrings(int initialIndex) {
        initialIndex = originalArgIndices.get(initialIndex);
        StringBuilder buffer = new StringBuilder(originalArgs[initialIndex]);
        for (int i = initialIndex + 1; i < originalArgs.length; ++i) {
            buffer.append(" ").append(originalArgs[i]);
        }
        return buffer.toString();
    }

    /**
     * Gets the integer at the given index.
     * 
     * @param index
     *            the index
     * @return the string or <code>null</code> if there is none
     * @throws NumberFormatException
     *             the the value at the given index is not a number
     */
    @Nullable
    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(parsedArgs.get(index));
    }

    /**
     * Gets the integer at the given index. If there is none, the given default
     * is returned.
     * 
     * @param index
     *            the index
     * @param def
     *            the default
     * @return the integer or the default
     * @throws NumberFormatException
     *             the the value at the given index is not a number
     */
    public int getInteger(int index, int def) throws NumberFormatException {
        return index < parsedArgs.size() ? Integer.parseInt(parsedArgs.get(index)) : def;
    }

    /**
     * Gets the double at the given index.
     * 
     * @param index
     *            the index
     * @return the string or <code>null</code> if there is none
     * @throws NumberFormatException
     *             the the value at the given index is not a number
     */
    @Nullable
    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(parsedArgs.get(index));
    }

    /**
     * Gets the double at the given index. If there is none, the given default
     * is returned.
     * 
     * @param index
     *            the index
     * @param def
     *            the default
     * @return the integer or the default
     * @throws NumberFormatException
     *             the the value at the given index is not a number
     */
    public double getDouble(int index, double def) throws NumberFormatException {
        return index < parsedArgs.size() ? Double.parseDouble(parsedArgs.get(index)) : def;
    }

    /**
     * Gets a slice of the original, unparsed arguments that where given to the
     * command, starting from the given index.
     * 
     * @param index
     *            the index
     * @return a slice of the command's arguments
     */
    public String[] getSlice(int index) {
        String[] slice = new String[originalArgs.length - index];
        System.arraycopy(originalArgs, index, slice, 0, originalArgs.length - index);
        return slice;
    }

    /**
     * Gets a slice of the original, unparsed arguments that where given to the
     * command, starting from the given index with the given padding.
     * 
     * @param index
     *            the index
     * @param padding
     *            the padding
     * @return a slice of the command's arguments
     */
    public String[] getPaddedSlice(int index, int padding) {
        String[] slice = new String[originalArgs.length - index + padding];
        System.arraycopy(originalArgs, index, slice, padding, originalArgs.length - index);
        return slice;
    }

    /**
     * Gets a slice of the parsed arguments that where given to the command,
     * starting from the given index.
     * 
     * @param index
     *            the index
     * @return a slice of the parsed command's arguments
     */
    public String[] getParsedSlice(int index) {
        String[] slice = new String[parsedArgs.size() - index];
        System.arraycopy(parsedArgs.toArray(new String[parsedArgs.size()]), index, slice, 0,
                parsedArgs.size() - index);
        return slice;
    }

    /**
     * Gets a slice of the parsed arguments that where given to the command,
     * starting from the given index with the given padding.
     * 
     * @param index
     *            the index
     * @param padding
     *            the padding
     * @return a slice of the parsed command's arguments
     */
    public String[] getParsedPaddedSlice(int index, int padding) {
        String[] slice = new String[parsedArgs.size() - index + padding];
        System.arraycopy(parsedArgs.toArray(new String[parsedArgs.size()]), index, slice, padding,
                parsedArgs.size() - index);
        return slice;
    }

    /**
     * Returns whether the command has the given flag.
     * 
     * @param ch
     *            the flag's character
     * @return true if the command has the flag
     */
    public boolean hasFlag(char ch) {
        return booleanFlags.contains(ch) || valueFlags.containsKey(ch);
    }

    /**
     * Gets the non-value flags of the command.
     * 
     * @return the non-value flags of the command
     */
    public Set<Character> getFlags() {
        return booleanFlags;
    }

    /**
     * Gets the value flags of the command.
     * 
     * @return the value flags of the command
     */
    public Map<Character, String> getValueFlags() {
        return valueFlags;
    }

    /**
     * Gets the string-value of the given flag.
     * 
     * @param ch
     *            the flag's character
     * @return the string or <code>null</code> if there is none
     */
    @Nullable
    public String getFlag(char ch) {
        return valueFlags.get(ch);
    }

    /**
     * Gets the string-value of the given flag. If the flag is not set, the
     * given default is returned.
     * 
     * @param ch
     *            the flag's character
     * @param def
     *            the default
     * @return the string or the default
     */
    public String getFlag(char ch, String def) {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * Gets the integer-value of the given flag.
     * 
     * @param ch
     *            the flag's character
     * @return the string or <code>null</code> if there is none
     * @throws NumberFormatException
     *             if the value of the given flag is not a number
     */
    @Nullable
    public int getFlagInteger(char ch) throws NumberFormatException {
        return Integer.parseInt(valueFlags.get(ch));
    }

    /**
     * Gets the integer-value of the given flag. If the flag is not set, the
     * given default is returned.
     * 
     * @param ch
     *            the flag's character
     * @param def
     *            the default
     * @return the integer or the default
     * @throws NumberFormatException
     *             if the value of the given flag is not a number
     */
    public int getFlagInteger(char ch, int def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Integer.parseInt(value);
    }

    /**
     * Gets the double-value of the given flag.
     * 
     * @param ch
     *            the flag's character
     * @return the string or <code>null</code> if there is none
     * @throws NumberFormatException
     *             if the value of the given flag is not a number
     */
    @Nullable
    public double getFlagDouble(char ch) throws NumberFormatException {
        return Double.parseDouble(valueFlags.get(ch));
    }

    /**
     * Gets the double-value of the given flag. If the flag is not set, the
     * given default is returned.
     * 
     * @param ch
     *            the flag's character
     * @param def
     *            the default
     * @return the double or the default
     * @throws NumberFormatException
     *             if the value of the given flag is not a number
     */
    public double getFlagDouble(char ch, double def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Double.parseDouble(value);
    }

    /**
     * Gets the number of arguments that where given to the command.
     * 
     * @return the number of arguments
     */
    public int argsLength() {
        return parsedArgs.size();
    }
}
