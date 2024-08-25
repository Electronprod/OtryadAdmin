package ru.electronprod.OtryadAdmin.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.LanguageService;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.User;

import java.util.Optional;

@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
	private final TelegramClient telegramClient;
	@Autowired
	private DBService db;
	@Autowired
	private LanguageService lang_config;
	@Autowired
	private OptionService optionServ;

	public TelegramBot() {
		telegramClient = new OkHttpTelegramClient(getBotToken());
	}

	@Override
	public String getBotToken() {
		return "7461225875:AAHL7JDkX5a0YXypIq12ahtby7Oswa_lew0";
	}

	@Override
	public LongPollingUpdateConsumer getUpdatesConsumer() {
		return this;
	}

	@AfterBotRegistration
	public void afterRegistration(BotSession botSession) {
		log.info("Registered bot running state is: " + botSession.isRunning());

	}

	@Override
	public void consume(Update update) {
		try {
			if (update.hasMessage() && update.getMessage().hasText()) {
				// Data
				String username = update.getMessage().getFrom().getUserName();
				long chat_id = update.getMessage().getChatId();
				String message_text = update.getMessage().getText();
				// Auth
				Optional<User> userOptional = db.getUserService().findByTelegram(username);
				if (!userOptional.isPresent()) {
					SendMessage message = SendMessage.builder().chatId(chat_id)
							.text(lang_config.getLanguage().get("tg_unautorized")).build();
					telegramClient.execute(message);
					return;
				}
				User user = userOptional.get();
				// Squadcommander commands
				if (user.getRole().equalsIgnoreCase("ROLE_SQUADCOMMANDER")) {
					if (message_text.equalsIgnoreCase("/mark")) {
						SendMessage message = SendMessage.builder().chatId(chat_id)
								.text(lang_config.getLanguage().get("tg_squadcommander_mark")).parseMode("html")
								.build();
						telegramClient.execute(message);
						return;
					}
					if (message_text.equalsIgnoreCase("/events")) {
						String events = "";
						for (String event : optionServ.getEvent_types().values()) {
							events = events + "\n" + event;
						}
						SendMessage message = SendMessage.builder().chatId(chat_id)
								.text(lang_config.getLanguage().get("tg_squadcommander_events") + events)
								.parseMode("html").build();
						telegramClient.execute(message);
						return;
					}
					if (message_text.equalsIgnoreCase("/reasons")) {
						String reasons = "";
						for (String reason : optionServ.getReasons_for_absences().values()) {
							reasons = reasons + "\n" + reason;
						}
						SendMessage message = SendMessage.builder().chatId(chat_id)
								.text(lang_config.getLanguage().get("tg_squadcommander_reasons") + reasons)
								.parseMode("html").build();
						telegramClient.execute(message);
						return;
					}
				}
				SendMessage message = SendMessage.builder().chatId(chat_id)
						.text("Error: there are no commands for your role.").parseMode("HTML").build();
				telegramClient.execute(message);
			} else if (update.hasCallbackQuery()) {
				// Data
				String call_data = update.getCallbackQuery().getData();
				long message_id = update.getCallbackQuery().getMessage().getMessageId();
				long chat_id = update.getCallbackQuery().getMessage().getChatId();
				//

			}
		} catch (TelegramApiException e) {
			log.error("TelegramAPI exception: " + e.getMessage());
		}
	}
}
