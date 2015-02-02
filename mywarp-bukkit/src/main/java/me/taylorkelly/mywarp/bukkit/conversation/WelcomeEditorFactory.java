/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.conversation;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.BukkitAdapter;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Creates and handles conversations with players who want to change a Warp's welcome-message.
 */
public class WelcomeEditorFactory {

  private static final String ESCAPE_SEQUENCE = "#cancel"; // NON-NLS
  private static final String REMOVE_SEQUENCE = "#none"; // NON-NLS
  private static final int TIMEOUT = 30;

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.CONVERSATIONS_RESOURCE_BUNDLE_NAME);

  private final ConversationFactory factory;
  private final BukkitAdapter adapter;

  /**
   * Creates an instance.
   *
   * @param plugin  the plugin instance
   * @param adapter the adapter
   */
  public WelcomeEditorFactory(MyWarpPlugin plugin, BukkitAdapter adapter) {
    this.factory =
        new ConversationFactory(plugin).withModality(true).withTimeout(TIMEOUT).withEscapeSequence(ESCAPE_SEQUENCE)
            .withFirstPrompt(new MessageInputPrompt());
    this.adapter = adapter;
  }

  /**
   * Creates an welcome-editor for the given player to change the welcome-message of the given Warp.
   *
   * @param player the LocalPlayer
   * @param warp   the Warp
   */
  public void create(LocalPlayer player, Warp warp) {
    Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
    initialSessionData.put(Locale.class, player.getLocale());
    initialSessionData.put(Warp.class, warp);

    factory.withInitialSessionData(initialSessionData).buildConversation(adapter.adapt(player)).begin();
  }

  /**
   * Asks for a new welcome message and validates the input accordingly.
   */
  private class MessageInputPrompt extends StringPrompt {

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
      input = input.trim();
      if (input.equalsIgnoreCase(REMOVE_SEQUENCE)) {
        input = "";
      }
      context.setSessionData(String.class, input);
      return new ChangePrompt();
    }

    @Override
    public String getPromptText(ConversationContext context) {
      Warp warp = (Warp) context.getSessionData(Warp.class);
      Locale locale = (Locale) context.getSessionData(Locale.class);
      return ChatColor.AQUA + MESSAGES
          .getString("welcome-message-conversation.enter-message", locale, warp.getName(), REMOVE_SEQUENCE,
                     ESCAPE_SEQUENCE, TIMEOUT);
    }
  }

  /**
   * Changes the welcome message and ends the conversation afterwards.
   */
  private class ChangePrompt extends MessagePrompt {

    @Override
    public String getPromptText(ConversationContext context) {
      Warp warp = (Warp) context.getSessionData(Warp.class);
      String message = (String) context.getSessionData(String.class);
      warp.setWelcomeMessage(message);

      Locale locale = (Locale) context.getSessionData(Locale.class);

      if (message.isEmpty()) {
        return MESSAGES.getString("welcome-message-conversation.removed-successful", locale, warp.getName());
      }
      return new StrBuilder().append(ChatColor.AQUA)
          .append(MESSAGES.getString("welcome-message-conversation.changed-successful", locale, warp.getName()))
          .appendNewLine().append(ChatColor.ITALIC).append(message).toString();
    }

    @Override
    protected Prompt getNextPrompt(ConversationContext context) {
      return Prompt.END_OF_CONVERSATION;
    }

  }
}
