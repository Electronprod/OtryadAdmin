package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.BaseRequest;

import lombok.extern.slf4j.Slf4j;
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
}
