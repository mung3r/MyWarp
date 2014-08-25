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
package me.taylorkelly.mywarp.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;

import com.google.common.collect.ImmutableMap;

/**
 * Provides several static methods that allow basic formating of strings send to
 * players. Colors are supported, bold formatting not!
 * 
 * Additionally it should be noted that the basic character widths this class
 * relies on where extracted from old Minecraft source code. The algorithm and
 * supported characters in Minecraft have changed, but these changes are not
 * covered!
 */
public final class FormattingUtils {

    /**
     * The horizontal width of a chat line in vanilla Minecraft.
     */
    private static final int CHAT_WIDTH = 318; // 325

    /**
     * The character used to indicate formatting-codes.
     */
    private static final char FORMATTING_CHAR = '§';

    /**
     * A map that stores supported characters and their width in the Minecraft
     * chat. This map is a conversion of code originally found in older
     * Minecraft versions. It does not include most unicode characters that are
     * supported today!
     */
    private static final Map<Character, Integer> CHAR_WIDTHS = ImmutableMap.<Character, Integer>builder()
            .put(' ', 4).put('!', 2).put('"', 5).put('#', 6).put('$', 6).put('%', 6).put('&', 6).put('\'', 3)
            .put('(', 5).put(')', 5).put('*', 5).put('+', 6).put(',', 2).put('-', 6).put('.', 2).put('/', 6)
            .put('0', 6).put('1', 6).put('2', 6).put('3', 6).put('4', 6).put('5', 6).put('6', 6).put('7', 6)
            .put('8', 6).put('9', 6).put(':', 2).put(';', 2).put('<', 5).put('=', 6).put('>', 5).put('?', 6)
            .put('@', 7).put('A', 6).put('B', 6).put('C', 6).put('D', 6).put('E', 6).put('F', 6).put('G', 6)
            .put('H', 6).put('I', 4).put('J', 6).put('K', 6).put('L', 6).put('M', 6).put('N', 6).put('O', 6)
            .put('P', 6).put('Q', 6).put('R', 6).put('S', 6).put('T', 6).put('U', 6).put('V', 6).put('W', 6)
            .put('X', 6).put('Y', 6).put('Z', 6).put('[', 4).put('\\', 6).put(']', 4).put('^', 6).put('_', 6)
            .put('a', 6).put('b', 6).put('c', 6).put('d', 6).put('e', 6).put('f', 5).put('g', 6).put('h', 6)
            .put('i', 2).put('j', 6).put('k', 5).put('l', 3).put('m', 6).put('n', 6).put('o', 6).put('p', 6)
            .put('q', 6).put('r', 6).put('s', 6).put('t', 4).put('u', 6).put('v', 6).put('w', 6).put('x', 6)
            .put('y', 6).put('z', 6).put('{', 5).put('|', 2).put('}', 5).put('~', 7).put('⌂', 6).put('Ç', 6)
            .put('ü', 6).put('é', 6).put('â', 6).put('ä', 6).put('à', 6).put('å', 6).put('ç', 6).put('ê', 6)
            .put('ë', 6).put('è', 6).put('ï', 4).put('î', 6).put('ì', 3).put('Ä', 6).put('Å', 6).put('É', 6)
            .put('æ', 6).put('Æ', 6).put('ô', 6).put('ö', 6).put('ò', 6).put('û', 6).put('ù', 6).put('ÿ', 6)
            .put('Ö', 6).put('Ü', 6).put('ø', 6).put('£', 6).put('Ø', 6).put('×', 4).put('ƒ', 6).put('á', 6)
            .put('í', 3).put('ó', 6).put('ú', 6).put('ñ', 6).put('Ñ', 6).put('ª', 6).put('º', 6).put('¿', 6)
            .put('®', 7).put('¬', 6).put('½', 6).put('¼', 6).put('¡', 2).put('«', 6).put('»', 6)
            .put(FORMATTING_CHAR, 0).build();

    /**
     * Block initialization of this class.
     */
    private FormattingUtils() {
    }

    /**
     * Gets the width of the given string. Formating codes are ignored.
     * 
     * @param str
     *            the string to check
     * @return the width of the string in pixels
     */
    public static int getWidth(String str) {
        int i = 0;
        if (str != null) {
            str = ChatColor.stripColor(str);
            for (int j = 0; j < str.length(); j++) {
                i += getWidth(str.charAt(j));
            }
        }
        return i;
    }

    /**
     * Gets the width of the given character. Will return 0 if the character is
     * not displayed or not covered by the underling data.
     * 
     * @param c
     *            the character to check
     * @return the width of the character
     */
    public static int getWidth(char c) {
        return CHAR_WIDTHS.get(c);
    }

    /**
     * Calls {@link #padRight(String, char)} using a space as padding character.
     * 
     * @param str
     *            the string
     * @return the padded string
     */
    public static String paddingRight(String str) {
        return padRight(str, ' ');
    }

    /**
     * Calls {@link #padRight(String, char, int)} using the chat width as
     * paddedWidth.
     * 
     * @param str
     *            the string
     * @param pad
     *            the padding char
     * @return the padded string
     */
    public static String padRight(String str, char pad) {
        return padRight(str, pad, CHAT_WIDTH);
    }

    /**
     * Pads the given string with the given character on the right until the
     * string has the given width.
     * 
     * @param str
     *            the string
     * @param pad
     *            the padding char
     * @param paddedWidth
     *            the width of the padded string
     * @return the padded string
     */
    public static String padRight(String str, char pad, int paddedWidth) {
        paddedWidth -= getWidth(str);
        return StringUtils.rightPad(str, paddedWidth / getWidth(pad), pad);
    }

    /**
     * Calls {@link #paddingLeft(String, char)} using a space as padding char.
     * 
     * @param str
     *            the string
     * @return the padded string
     */
    public static String paddingLeft(String str) {
        return paddingLeft(str, ' ');
    }

    /**
     * Calls {@link #padLeft(String, char, int)} using the chat width as
     * paddedWidth.
     * 
     * @param str
     *            the string
     * @param pad
     *            the padding char
     * @return the padded string
     */
    public static String paddingLeft(String str, char pad) {
        return padLeft(str, pad, CHAT_WIDTH);
    }

    /**
     * Pads the given string with the given character on the left until the
     * string has the given width.
     * 
     * @param str
     *            the string
     * @param pad
     *            the padding character
     * @param paddedWidth
     *            the width of the padded string
     * @return the padded string
     */
    public static String padLeft(String str, char pad, int paddedWidth) {
        paddedWidth -= getWidth(str);
        return StringUtils.leftPad(str, paddedWidth / getWidth(pad), pad);
    }

    /**
     * Calls {@link #center(String, char)} using a space as padding character.
     * 
     * @param str
     *            the string
     * @return the centered string
     */
    public static String center(String str) {
        return center(str, ' ');
    }

    /**
     * Calls {@link #center(String, char, int)} using the chat width as
     * paddedWidth.
     * 
     * @param str
     *            the string
     * @param pad
     *            the padding char
     * @return the centered string
     */
    public static String center(String str, char pad) {
        return center(str, pad, CHAT_WIDTH);
    }

    /**
     * Centralizes the given string relative to the given width by padding it
     * with the given character.
     * 
     * @param str
     *            the string to centralize
     * @param pad
     *            the padding char
     * @param paddedWidth
     *            the width of the padded string
     * @return the centered string
     */
    public static String center(String str, char pad, int paddedWidth) {
        paddedWidth -= getWidth(str);
        String padding = StringUtils.repeat(Character.toString(pad), paddedWidth / getWidth(pad) / 2);
        return padding + str + padding;
    }

    /**
     * Calls {@link #trim(String, int)} using the chat width as trimmedWidth.
     * 
     * @param str
     *            the string to trim
     * @return the trimmed string
     */
    public static String trim(String str) {
        return trim(str, CHAT_WIDTH);
    }

    /**
     * Trims the given String until it has the given width. Formating codes are
     * recognized and fully removed.
     * 
     * @param str
     *            the string to trim
     * @param trimmedWidth
     *            the width of the trimmed string
     * @return the trimmed string
     */
    public static String trim(String str, int trimmedWidth) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {

            // also cut off color codes
            if (chars[i - 1] == FORMATTING_CHAR) {
                --i;
            }
            String check = String.valueOf(chars, 0, i);
            if (getWidth(check) <= trimmedWidth) {
                return check;
            }
        }
        return "";
    }

    /**
     * Calls {@link #twoColumnAlign(String, String, char)} using a space as
     * padding char.
     * 
     * @param leftColumn
     *            the contents of the left column
     * @param rightColumn
     *            the contents of the right column
     * @return the formatted string
     */
    public static String twoColumnAlign(String leftColumn, String rightColumn) {
        return twoColumnAlign(leftColumn, rightColumn, ' ');
    }

    /**
     * Calls {@link #twoColumnAlign(String, String, char, int)} using the chat
     * width as totalWidth.
     * 
     * @param leftColumn
     *            the contents of the left column
     * @param rightColumn
     *            the contents of the right column
     * @param pad
     *            the padding character
     * @return the formatted string
     */
    public static String twoColumnAlign(String leftColumn, String rightColumn, char pad) {
        return twoColumnAlign(leftColumn, rightColumn, pad, CHAT_WIDTH);
    }

    /**
     * Creates a two-column-layout from the given strings, using the given
     * character as padding in between with the given total width. The left
     * column will be aligned on the left, the right column on the right of the
     * given width.
     * 
     * @param leftColumn
     *            the contents of the left column
     * @param rightColumn
     *            the contents of the right column
     * @param pad
     *            the padding character
     * @param totalWidth
     *            the horizontal width that should be covered by the layout
     * @return the formatted string
     */
    public static String twoColumnAlign(String leftColumn, String rightColumn, char pad, int totalWidth) {
        int leftWidth = getWidth(leftColumn);
        int rightWidth = getWidth(rightColumn);
        totalWidth -= (leftWidth + rightWidth);

        if (totalWidth > 0) {
            return leftColumn + StringUtils.repeat(Character.toString(pad), totalWidth / getWidth(pad))
                    + rightColumn;
        }
        // If both columns together are larger than the totalWidth, the larger
        // one is trimmed until it fits.
        int seperatorWidth = getWidth(pad);
        totalWidth -= seperatorWidth;
        if (leftWidth > rightWidth) {
            leftColumn = trim(leftColumn, leftWidth + totalWidth);
        } else {
            rightColumn = trim(rightColumn, rightWidth + totalWidth);
        }
        return leftColumn + pad + rightColumn;
    }

    /**
     * Calls {@link #toList(char, String...)} using a '-' as list character.
     * 
     * @param entries
     *            the list's entries
     * @return a string with all entries
     */
    public static String toList(String... entries) {
        return toList('-', entries);
    }

    /**
     * Calls {@link #toList(char, int, String...)} using the chat width as
     * totalWidth.
     * 
     * @param listChar
     *            the character that will be displayed as bullet point before
     *            each entry
     * @param entries
     *            the list's entries
     * @return a string with all entries
     */
    public static String toList(char listChar, String... entries) {
        return toList(listChar, CHAT_WIDTH, entries);
    }

    /**
     * Creates a not numbered list from the given strings. Each string
     * represents an independent entry on the list. Strings that are longer than
     * the given width will be split above several lines.
     * 
     * @param listChar
     *            the character that will be displayed as bullet point before
     *            each entry
     * @param maxWidth
     *            the maximal width of each entry
     * @param entries
     *            the list's entries
     * @return the list
     */
    public static String toList(char listChar, int maxWidth, String... entries) {
        StrBuilder fullLines = new StrBuilder();
        for (String entry : entries) {
            if (!fullLines.isEmpty()) {
                fullLines.appendNewLine();
                // reset colors from the previous entry
                fullLines.append(ChatColor.RESET);
            }

            StrBuilder line = new StrBuilder();
            String[] words = entry.split(" ");
            line.append(listChar);
            line.append(' ');

            for (String word : words) {
                // if the word itself is longer than the max. length, add chars
                // as long as possible
                if (getWidth(word) > maxWidth) {
                    for (char c : word.toCharArray()) {
                        if (getWidth(line.toString()) + getWidth(c) > maxWidth) {
                            fullLines.appendln(line.toString());
                            line.clear();
                            line.appendPadding(3, ' ');
                        }
                        line.append(c);
                    }
                } else {
                    // if the world plus the needed blank is longer than the
                    // max. length, make a new line
                    if (getWidth(line.toString()) + getWidth(word) + getWidth(' ') > maxWidth) {
                        fullLines.appendln(line.toString());
                        line.clear();
                        line.appendPadding(3, ' ');
                    }
                    if (!line.isEmpty() && line.charAt(line.length() - 1) != ' ') {
                        line.append(' ');
                    }
                    line.append(word);
                }
            }
            if (!line.isEmpty()) {
                fullLines.append(line);
            }
        }
        return fullLines.toString();
    }
}
