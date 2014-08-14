package me.taylorkelly.mywarp.commands;

import java.util.HashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

/**
 * Handles warp acceptance conversations..
 */
public class WarpAcceptanceConversation {

    private static final int TIMEOUT = 30;

    /**
     * The custom conversation factory
     */
    private static final ConversationFactory conversationFactory = new ConversationFactory(MyWarp.inst())
            .withModality(true).withTimeout(TIMEOUT).withFirstPrompt(new AskPlayerPrompt());

    /**
     * Initiates a conversation with the given player asking him to accept the
     * given warp that the given command-sender wants to transfer to him.
     * 
     * @param player
     *            the player
     * @param warp
     *            the warp
     * @param sender
     *            the command-sender
     */
    public static void initiate(Player player, Warp warp, CommandSender sender) {
        Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
        initialSessionData.put(Warp.class, warp);
        initialSessionData.put(String.class, sender.getName());

        conversationFactory.withInitialSessionData(initialSessionData)
                .buildConversation(player).begin();
    }

    /**
     * A prompt that asks the conversion partner if he wants to accept and calls
     * the next prompt based on the palyer's response.
     */
    private static class AskPlayerPrompt extends BooleanPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData(Warp.class);
            String senderName = (String) context.getSessionData(String.class);
            //TODO Add a way to get informations about the warp.
            return MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("warp-acceptance-conversation.want-to-accept", (Player) context.getForWhom(),
                            senderName, warp.getName(), "yes", "no", TIMEOUT);
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            return input ? new AcceptPrompt() : new DeclinePrompt();
        }

    }

    /**
     * A prompt that handles the acceptance of the ownership change and ends the
     * conversation afterwards.
     */
    private static class AcceptPrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Warp warp = (Warp) context.getSessionData(Warp.class);
            Player givee = (Player) context.getForWhom();
            warp.setCreatorId(givee.getUniqueId());

            return MyWarp.inst().getLocalizationManager()
                    .getString("warp-acceptance-conversation.accepted-successful", givee, warp.getName());
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }

    /**
     * A prompt that handles the decline of the ownership transfer and ends the
     * conversation afterwards.
     */
    private static class DeclinePrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("warp-acceptance-conversation.declined-successful",
                            (Player) context.getForWhom());
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }
}
