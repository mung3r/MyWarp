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
package me.taylorkelly.mywarp.commands;

import java.util.HashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

/**
 * Handles welcome message changes initiated by players.
 */
public class WelcomeMessageConversation {

    private static final String ESCAPE_SEQUENCE = "#cancel";
    private static final String REMOVE_SEQUENCE = "#none";
    private static final int TIMEOUT = 30;

    /**
     * Block initialization of this class.
     */
    private WelcomeMessageConversation() {
    }

    /**
     * The custom conversation factory.
     */
    private static final ConversationFactory CONVERSATION_FACTORY = new ConversationFactory(MyWarp.inst())
            .withModality(true).withTimeout(TIMEOUT).withEscapeSequence(ESCAPE_SEQUENCE)
            .withFirstPrompt(new MessageInputPrompt());

    /**
     * Initiates a conversation with the given player asking him to enter the
     * new welcome message for the given warp.
     * 
     * @param player
     *            the player
     * @param warp
     *            the warp
     */
    public static void initiate(Player player, Warp warp) {
        Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
        initialSessionData.put(Warp.class, warp);

        CONVERSATION_FACTORY.withInitialSessionData(initialSessionData).buildConversation(player).begin();
    }

    /**
     * A prompt that asks for the new welcome message and validates the given
     * input accordingly.
     */
    private static class MessageInputPrompt extends StringPrompt {

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
            return MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("welcome-message-conversation.enter-message", (Player) context.getForWhom(),
                            warp.getName(), REMOVE_SEQUENCE, ESCAPE_SEQUENCE, TIMEOUT);
        }
    }

    /**
     * A prompt that changes the welcome message and ends the conversation
     * afterwards.
     */
    private static class ChangePrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData(Warp.class);
            String message = (String) context.getSessionData(String.class);
            warp.setWelcomeMessage(message);

            if (message.isEmpty()) {
                return MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("welcome-message-conversation.removed-successful",
                                (Player) context.getForWhom(), warp.getName());
            }
            return MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("welcome-message-conversation.changed-successful",
                            (Player) context.getForWhom(), warp.getName(), message);
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }
}
