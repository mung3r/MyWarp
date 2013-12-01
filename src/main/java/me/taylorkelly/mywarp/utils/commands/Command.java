package me.taylorkelly.mywarp.utils.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.economy.WarpFees;

import org.bukkit.command.CommandSender;

/**
 * This annotation indicates a command. Methods should be marked with this
 * annotation to tell {@link CommandsManager} that the method is a command.
 * While the method name can be anything, it is absolutely important that the
 * method takes a {@link CommandSender} as first and a {@link CommandContext} as
 * second argument"
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * A list of aliases for the command. The first alias is the most important
     * - it is the main name of the command. (The method name is never used for
     * anything).
     * 
     * @return Aliases for a command
     */
    String[] aliases();

    /**
     * A description for the command. The given string is parsed on to the
     * {@Link LocalizationManager} that will try to load the
     * corresponding string from the language file.
     * 
     * @return A description for the command.
     */
    String desc();

    /**
     * The fee that points to the amount the sender is charged when using the
     * command. It is parsed via the {@link WarpFees} container. This has only
     * an effect if economy support is enabled.
     * 
     * @return The fee type used for this command
     */
    Fee fee() default Fee.NONE;

    /**
     * Flags allow special processing for flags such as -h in the command,
     * allowing users to easily turn on a flag. This is a string with each
     * character being a flag. Use A-Z and a-z as possible flags. Appending a
     * flag with a : makes the flag character before a value flag, meaning that
     * if it is given it must have a value
     * 
     * @return Flags matching a-zA-Z
     */
    String flags() default "";

    /**
     * The maximum number of arguments. Use -1 for an unlimited number of
     * arguments.
     * 
     * @return the maximum number of arguments
     */
    int max() default -1;

    /**
     * The minimum number of arguments. This should be 0 or above.
     * 
     * @return the minimum number of arguments
     */
    int min() default 0;

    /**
     * A list of permissions, only one has to be meet to get access to the
     * command.
     * 
     * @return permissions needed to access this command
     */
    String[] permissions();

    /**
     * Usage instruction. Example text for usage could be
     * <code>[-h harps] [name] [message]</code>.
     * 
     * @return Usage instructions for a command
     */
    String usage();

}
