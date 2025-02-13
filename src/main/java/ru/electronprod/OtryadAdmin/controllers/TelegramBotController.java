package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import com.github.kshashov.telegram.api.bind.annotation.request.CallbackQueryRequest;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.ChatRepository;
import ru.electronprod.OtryadAdmin.data.filesystem.TelegramLanguage;
import ru.electronprod.OtryadAdmin.telegram.BotConfig;
import ru.electronprod.OtryadAdmin.telegram.BotService;

@Slf4j
@BotController
public class TelegramBotController implements TelegramMvcController {
	@Autowired
	private TelegramLanguage lang;
	@Autowired
	private BotService botServ;
	@Autowired
	private BotConfig botCfg;
	@Autowired
	private ChatRepository chatRep;

	@Override
	public String getToken() {
		return botCfg.getToken();
	}

	@BotRequest(type = { MessageType.ANY })
	public BaseRequest<?, ?> empty_request(Chat chat) {
		if (botServ.isRegistered(chat.id()))
			return botServ.sendMessageObject(chat.id(), lang.get("unknown_command"));
		return botServ.sendMessageObject(chat.id(), lang.get("please_register"));
	}

	@MessageRequest("/start")
	public BaseRequest<?, ?> start(User user, Chat chat) {
		botServ.registerChat(user, chat);
		return botServ.sendMessageObject(chat.id(), lang.get("greeting"));
	}

	@MessageRequest("/test_lang {key:[\\S]+}")
	public BaseRequest<?, ?> test_lang(@BotPathVariable("key") String key, Chat chat) {
		return botServ.sendMessageObject(chat.id(), lang.get(key));
	}

	@MessageRequest("/mark_notifications")
	public BaseRequest<?, ?> switchMarkNotifications_command(User user, Chat chat) {
		if (!botServ.isRegistered(chat.id()))
			botServ.sendMessageObject(chat.id(), lang.get("please_register"));
		ru.electronprod.OtryadAdmin.models.Chat chat1 = chatRep.findByChatId(chat.id()).orElseThrow();
		// Updating the setting
		chat1.setSendMarkedNotification(!chat1.isSendMarkedNotification());
		chatRep.save(chat1);
		return botServ.sendMessageObject(chat.id(), lang.get("switch_off_mark_notifications_changed_status")
				.replace("%val%", String.valueOf(chat1.isSendMarkedNotification())));
	}

	@CallbackQueryRequest("switch_mark_notifications")
	public Object switchMarkNotifications(TelegramRequest request, Update update) {
		// Getting variables
		Integer messageId = update.callbackQuery().message().messageId();
		Long chatID = request.getChat().id();
		ru.electronprod.OtryadAdmin.models.Chat chat = chatRep.findByChatId(chatID).orElseThrow();
		// Updating the setting
		chat.setSendMarkedNotification(!chat.isSendMarkedNotification());
		chatRep.save(chat);
		EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup(chatID, messageId)
				.replyMarkup(botServ.getNotification_marked_button(chat));
		if (!chat.isSendMarkedNotification()) {
			var msg = new SendMessage(chatID, lang.get("switch_off_mark_notifications_help")).parseMode(ParseMode.HTML)
					.replyMarkup(botServ.getNotification_marked_button(chat));
			BotConfig.telegramBot.execute(msg);
		}
		return editMarkup;
	}

}
