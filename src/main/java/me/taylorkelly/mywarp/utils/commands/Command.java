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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.taylorkelly.mywarp.economy.FeeBundle;

/**
 * This annotation indicates a command. Methods should be marked with this
 * annotation to tell {@link CommandsManager} that the method is a command.
 * While the method name can be anything, it is absolutely important that the
 * method takes a {@link org.bukkit.command.CommandSender} as first and a {@link CommandContext} as
 * second argument"
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * A list of aliases for the command. The first alias is the most important
     * - it is the main name of the command. (The method name is never used for
     * anything).
     */
    String[] aliases();

    /**
     * A description for the command. The given string is parsed on to the
     * {@Link LocalizationManager} that will try to load the
     * corresponding string from the language file.
     */
    String desc();

    /**
     * The fee that points to the amount the sender is charged when using the
     * command. It is parsed via the {@link FeeBundle} container. This has only
     * an effect if economy support is enabled.
     */
    FeeBundle.Fee fee() default FeeBundle.Fee.NONE;

    /**
     * Flags allow special processing for flags such as -h in the command,
     * allowing users to easily turn on a flag. This is a string with each
     * character being a flag. Use A-Z and a-z as possible flags. Appending a
     * flag with a : makes the flag character before a value flag, meaning that
     * if it is given it must have a value
     */
    String flags() default "";

    /**
     * The maximum number of arguments. Use -1 for an unlimited number of
     * arguments.
     */
    int max() default -1;

    /**
     * The minimum number of arguments. This should be 0 or above.
     */
    int min() default 0;

    /**
     * A list of permissions, only one has to be meet to get access to the
     * command.
     */
    String[] permissions();

    /**
     * Usage instruction. Example text for usage could be
     * <code>[-h harps] [name] [message]</code>.
     */
    String usage();

}
