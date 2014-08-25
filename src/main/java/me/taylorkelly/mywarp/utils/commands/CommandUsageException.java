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

/**
 * Indicates that the commands fails due to wrong usage (e.g. flags,
 * argument-length...).
 */
public class CommandUsageException extends CommandException {
    private static final long serialVersionUID = -6761418114414516542L;

    private final String usage;

    /**
     * Constructs this exception with the given message and the given correct
     * usage.
     * 
     * @param message
     *            the message
     * @param correctUsage
     *            the correct usage
     */
    public CommandUsageException(String message, String correctUsage) {
        super(message);
        this.usage = correctUsage;
    }

    /**
     * Gets the correct usage.
     * 
     * @return the correct usage
     */
    public String getCorrectUsage() {
        return usage;
    }
}
