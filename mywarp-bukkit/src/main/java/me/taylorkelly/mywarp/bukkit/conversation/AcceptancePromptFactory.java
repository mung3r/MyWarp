/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.bukkit.BukkitPlayer;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.bukkit.util.BukkitMessageInterpreter;
import me.taylorkelly.mywarp.command.printer.InfoPrinter;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationService;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Creates and handles conversations with players who are asked to accept a certain Warp.
 */
public class AcceptancePromptFactory {

  private static final int TIMEOUT = 30;

  private static final DynamicMessages msg = new DynamicMessages(MyWarpPlugin.CONVERSATIONS_RESOURCE_BUNDLE_NAME);

  private final AuthorizationService authorizationService;
  private final ConversationFactory factory;
  private final MyWarpPlugin plugin;

  /**
   * Creates an instance.
   *
   * @param plugin               the plugin instance
   * @param authorizationService the AuthorizationService
   */
  public AcceptancePromptFactory(MyWarpPlugin plugin, AuthorizationService authorizationService) {
    this.plugin = plugin;
    this.authorizationService = authorizationService;
    this.factory =
        new ConversationFactory(plugin).withModality(true).withTimeout(TIMEOUT).withFirstPrompt(new QuestionPrompt());
  }

  /**
   * Creates a acceptance-prompt for the given player to accept the given Warp.
   *
   * @param warp      the Warp
   * @param initiator the Actor who initiated the action
   */
  public void create(BukkitPlayer forWhom, Warp warp, Actor initiator) {
    Map<Object, Object> initialSessionData = new HashMap<Object, Object>();
    initialSessionData.put(Locale.class, forWhom.getLocale());
    initialSessionData.put(Warp.class, warp);
    initialSessionData.put(String.class, initiator.getName());

    factory.withInitialSessionData(initialSessionData).buildConversation(forWhom.getLoadedPlayer()).begin();
  }

  private class QuestionPrompt extends StringPrompt {

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
      LocaleManager.setLocale((Locale) context.getSessionData(Locale.class));
      if (input.equalsIgnoreCase(getInfoSequence())) {
        return new InfoPrompt();
      }
      if (input.equalsIgnoreCase(getAcceptanceSequence())) {
        return new AcceptPrompt();
      }
      if (input.equalsIgnoreCase(getDeclineSequence())) {
        return new DeclinePrompt();
      }

      return new QuestionPrompt();
    }

    @Override
    public String getPromptText(ConversationContext context) {
      Warp warp = (Warp) context.getSessionData(Warp.class);
      String initiatorName = (String) context.getSessionData(String.class);

      LocaleManager.setLocale((Locale) context.getSessionData(Locale.class));

      Message
          message =
          Message.builder().append(
              msg.getString("warp-acceptance-conversation.want-to-accept", initiatorName, warp.getName(),
                            getAcceptanceSequence(), getDeclineSequence(), getInfoSequence(), TIMEOUT)).build();
      return BukkitMessageInterpreter.interpret(message);
    }

    /**
     * Gets the translated acceptance sequence.
     *
     * @return the acceptance sequence
     */
    private String getAcceptanceSequence() {
      return msg.getString("warp-acceptance-conversation.acceptance-sequence");
    }

    /**
     * Gets the translated decline sequence.
     *
     * @return the decline sequence
     */
    private String getDeclineSequence() {
      return msg.getString("warp-acceptance-conversation.decline-sequence");
    }

    /**
     * Gets the translated info sequence.
     *
     * @return the info sequence
     */
    private String getInfoSequence() {
      return msg.getString("warp-acceptance-conversation.info-sequence");
    }
  }

  /**
   * Displays information about the warp and calls {@link QuestionPrompt} afterwards.
   */
  private class InfoPrompt extends MessagePrompt {

    @Override
    protected Prompt getNextPrompt(ConversationContext context) {
      return new QuestionPrompt();
    }

    @Override
    public String getPromptText(ConversationContext context) {
      Warp warp = (Warp) context.getSessionData(Warp.class);

      LocaleManager.setLocale((Locale) context.getSessionData(Locale.class));
      return BukkitMessageInterpreter.interpret(new InfoPrinter(warp, authorizationService)
                                                    .getText(plugin.getAdapter().adapt((Player) context.getForWhom())));
    }
  }

  /**
   * Handles the acceptance of the ownership change and ends the conversation.
   */
  private class AcceptPrompt extends MessagePrompt {

    @Override
    public String getPromptText(ConversationContext context) {
      Warp warp = (Warp) context.getSessionData(Warp.class);
      warp.setCreator(plugin.getAdapter().adapt((Player) context.getForWhom()).getProfile());

      LocaleManager.setLocale((Locale) context.getSessionData(Locale.class));
      Message
          message =
          Message.builder().append(msg.getString("warp-acceptance-conversation.accepted-successful", warp.getName()))
              .build();
      return BukkitMessageInterpreter.interpret(message);
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
      LocaleManager.setLocale((Locale) context.getSessionData(Locale.class));
      Message
          message =
          Message.builder().append(msg.getString("warp-acceptance-conversation.declined-successful")).build();
      return BukkitMessageInterpreter.interpret(message);
    }

    @Override
    protected Prompt getNextPrompt(ConversationContext context) {
      return Prompt.END_OF_CONVERSATION;
    }

  }
}
