/*
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
package me.taylorkelly.mywarp.bukkit.conversation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.BukkitAdapter;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

/**
 * Creates and handles conversations with players who are asked to accept a
 * certain Warp.
 */
public class WarpAcceptancePromptFactory {

    private static final int TIMEOUT = 30;

    private static final DynamicMessages MESSAGES = new DynamicMessages(
            UsageCommands.CONVERSATIONS_RESOURCE_BUNDLE_NAME);

    private final ConversationFactory factory;
    private final BukkitAdapter adapter;

    /**
     * Creates an instance.
     * 
     * @param plugin
     *            the plugin instance
     * @param adapter
     *            the adapter
     */
    public WarpAcceptancePromptFactory(MyWarpPlugin plugin, BukkitAdapter adapter) {
        this.factory = new ConversationFactory(plugin).withModality(true).withTimeout(TIMEOUT)
                .withFirstPrompt(new AskPlayerPrompt());
        this.adapter = adapter;
    }

    /**
     * Creates a acceptance-prompt for the given player to accept the given
     * Warp.
     * 
     * @param initiator
     *            the Actor who initiated the action
     * @param localPlayer
     *            the LocalPlayer
     * @param warp
     *            the Warp
     */
    public void create(Actor initiator, LocalPlayer localPlayer, Warp warp) {
        Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
        initialSessionData.put(Locale.class, localPlayer.getLocale());
        initialSessionData.put(Warp.class, warp);
        initialSessionData.put(String.class, initiator.getName());

        factory.withInitialSessionData(initialSessionData).buildConversation(adapter.adapt(localPlayer))
                .begin();
    }

    /**
     * Asks the conversion partner if he wants to accept and calls the next
     * prompt based on the response.
     */
    private class AskPlayerPrompt extends BooleanPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData(Warp.class);
            String initiatorName = (String) context.getSessionData(String.class);
            // XXX Add a way to get informations about the warp via InfoPrinter.

            Locale locale = (Locale) context.getSessionData(Locale.class);
            return ChatColor.AQUA
                    + MESSAGES.getString("warp-acceptance-conversation.want-to-accept", locale,
                            initiatorName, warp.getName(), "yes", "no", TIMEOUT);
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            return input ? new AcceptPrompt() : new DeclinePrompt();
        }

    }

    /**
     * Handles the acceptance of the ownership change and ends the conversation.
     */
    private class AcceptPrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData(Warp.class);
            warp.setCreator(adapter.adapt((Player) context.getForWhom()).getProfile());

            Locale locale = (Locale) context.getSessionData(Locale.class);
            return ChatColor.AQUA
                    + MESSAGES.getString("warp-acceptance-conversation.accepted-successful", locale,
                            warp.getName());
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }

    /**
     * Handles the decline of the ownership transfer and ends the conversation.
     */
    private class DeclinePrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Locale locale = (Locale) context.getSessionData(Locale.class);
            return ChatColor.AQUA
                    + MESSAGES.getString("warp-acceptance-conversation.declined-successful", locale);
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }
}
