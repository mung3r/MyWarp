package me.taylorkelly.mywarp.data;

import java.util.HashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

/**
 * Handles welcome message changes initiated by players. Basically a static
 * wrapper for bukkit's conversation API.
 */
public class WelcomeMessageHandler {

    private static final String ESCAPE_SEQUENCE = "#cancel";
    private static final String REMOVE_SEQUENCE = "#none";

    /**
     * The custom conversation factory
     */
    private static ConversationFactory conversationFactory = new ConversationFactory(MyWarp.inst())
            .withModality(true).withTimeout(30).withEscapeSequence(ESCAPE_SEQUENCE)
            .withFirstPrompt(new WelcomeMessagePrompt());

    /**
     * Initiates a change of the welcome message for the given warp through a
     * conversation with the given player.
     * 
     * @param player
     *            the player
     * @param warp
     *            the warp
     */
    public static void initiateWelcomeMessageChange(Player player, Warp warp) {
        Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
        initialSessionData.put("warp", warp);

        WelcomeMessageHandler.conversationFactory.withInitialSessionData(initialSessionData)
                .buildConversation(player).begin();
    }

    /**
     * A prompt that asks for the new welcome message and validates the given
     * one accordingly. Will call {@link WelcomeMessageChange} to perform the
     * change.
     */
    private static class WelcomeMessagePrompt extends StringPrompt {

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(REMOVE_SEQUENCE)) {
                input = "";
            }
            context.setSessionData("message", input);
            return new WelcomeMessageChange();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData("warp");
            return MyWarp.inst().getLocalizationManager()
                    .getString("commands.welcome.enter", (Player) context.getForWhom(), warp.getName());
        }
    }

    /**
     * A prompt that changes the welcome message to the given one and ends the
     * conversation afterwards.
     */
    private static class WelcomeMessageChange extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData("warp");
            String message = (String) context.getSessionData("message");
            warp.setWelcomeMessage(message);

            if (message.isEmpty()) {
                return MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("commands.welcome.removed-successful", (Player) context.getForWhom(),
                                warp.getName());
            } else {
                return MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("commands.welcome.changed-successful", (Player) context.getForWhom(),
                                warp.getName(), message);
            }
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }
}
