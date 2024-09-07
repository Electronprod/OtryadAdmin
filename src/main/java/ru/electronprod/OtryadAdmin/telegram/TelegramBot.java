package ru.electronprod.OtryadAdmin.telegram;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.filesystem.FileOptions;
import ru.electronprod.OtryadAdmin.data.filesystem.LanguageService;
import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.helpers.StatsFormHelper;
import ru.electronprod.OtryadAdmin.services.SearchService;
import ru.electronprod.OtryadAdmin.services.StatsHelperService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.lang.Math.toIntExact;

import java.io.File;

@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer, InitializingBean {
	private final TelegramClient telegramClient;
	private File config = new File("telegram_config.txt");
	@Autowired
	private DBService db;
	@Autowired
	private LanguageService lang_config;
	@Autowired
	private OptionService optionServ;
	@Autowired
	private SearchService search;
	@Autowired
	private StatsHelperService statsServ;

	public TelegramBot() {
		telegramClient = new OkHttpTelegramClient(getBotToken());
	}

	@Override
	public void afterPropertiesSet() {
		if (FileOptions.loadFile(config) || FileOptions.getFileLines(config.getPath()).isEmpty()) {
			FileOptions.writeFile("your_token_here", config);
		}
	}

	@Override
	public String getBotToken() {
		return FileOptions.getFileLine(config);
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
	@Transactional
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
					sendMessage("tg_unautorized", chat_id);
					return;
				}
				User user = userOptional.get();
				// Squadcommander commands
				if (user.getRole().equalsIgnoreCase("ROLE_SQUADCOMMANDER")) {
					if (message_text.toLowerCase().contains("report")) {
						Squad squad = db.getUserService().findById(user.getId()).get().getSquad();
						String data = recognizeData(message_text, chat_id, db.getHumanService().findBySquad(squad));
						if (data.isBlank()) {
							return;
						}
						SendMessage message = SendMessage.builder().chatId(chat_id).text(data).parseMode("HTML")
								.replyMarkup(InlineKeyboardMarkup.builder()
										.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
												.text(lang_config.getLanguage().get("tg_squadcommander_confirm_btn"))
												.callbackData("squadcommander_mark_confirm").build()))
										.build())
								.build();
						telegramClient.execute(message);
						return;
					}
					SendMessage message = SendMessage.builder().chatId(chat_id)
							.text(lang_config.getLanguage().get("tg_squadcommander_mark")).parseMode("HTML")
							.replyMarkup(InlineKeyboardMarkup.builder().keyboard(getSquadCommanderButtons()).build())
							.build();

					telegramClient.execute(message);
					return;
				}
				SendMessage message = SendMessage.builder().chatId(chat_id)
						.text("Error: there are no commands for your role.").parseMode("HTML").build();
				telegramClient.execute(message);
			} else if (update.hasCallbackQuery()) {
				// Data
				String call_data = update.getCallbackQuery().getData();
				long message_id = update.getCallbackQuery().getMessage().getMessageId();
				long chat_id = update.getCallbackQuery().getMessage().getChatId();
				// Auth
				Optional<User> userOptional = db.getUserService()
						.findByTelegram(update.getCallbackQuery().getFrom().getUserName());
				if (!userOptional.isPresent()) {
					sendMessage("tg_unautorized", chat_id);
					return;
				}
				switch (call_data) {
				case "squadcommander_events":
					String events = "";
					for (String event : optionServ.getEvent_types().values()) {
						events = events + "\n" + event;
					}
					EditMessageText message = EditMessageText.builder().messageId(toIntExact(message_id))
							.chatId(chat_id).text(lang_config.getLanguage().get("tg_squadcommander_events") + events)
							.parseMode("html")
							.replyMarkup(InlineKeyboardMarkup.builder()
									.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
											.text(lang_config.getLanguage().get("tg_squadcommander_back_btn"))
											.callbackData("squadcommander").build()))
									.build())
							.build();
					telegramClient.execute(message);
					return;
				case "squadcommander_reasons":
					String reasons = "";
					for (String reason : optionServ.getReasons_for_absences().values()) {
						reasons = reasons + "\n" + reason;
					}
					EditMessageText message1 = EditMessageText.builder().messageId(toIntExact(message_id))
							.chatId(chat_id).text(lang_config.getLanguage().get("tg_squadcommander_reasons") + reasons)
							.parseMode("html")
							.replyMarkup(InlineKeyboardMarkup.builder()
									.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
											.text(lang_config.getLanguage().get("tg_squadcommander_back_btn"))
											.callbackData("squadcommander").build()))
									.build())
							.build();
					telegramClient.execute(message1);
					return;
				case "squadcommander":
					EditMessageText message2 = EditMessageText.builder().chatId(chat_id)
							.text(lang_config.getLanguage().get("tg_squadcommander_mark")).parseMode("HTML")
							.messageId(toIntExact(message_id))
							.replyMarkup(InlineKeyboardMarkup.builder().keyboard(getSquadCommanderButtons()).build())
							.build();
					telegramClient.execute(message2);
					return;

				case "squadcommander_mark_confirm":
					EditMessageText message3 = EditMessageText.builder().chatId(chat_id).parseMode("HTML")
							.messageId(toIntExact(message_id))
							.text(lang_config.getLanguage().get("tg_squadcommander_mark_saved"))
							.replyMarkup(InlineKeyboardMarkup.builder()
									.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
											.text(lang_config.getLanguage().get("tg_squadcommander_back_btn"))
											.callbackData("squadcommander").build()))
									.build())
							.build();
					boolean result = saveData(update.getCallbackQuery().getMessage().toString(), chat_id,
							userOptional.get());
					if (!result) {
						return;
					}

					telegramClient.execute(message3);
					return;
				}
			}
		} catch (TelegramApiException e) {
			log.error("TelegramAPI exception: " + e.getMessage());
		}
	}

	private List<InlineKeyboardRow> getSquadCommanderButtons() {
		List<InlineKeyboardRow> arr = new ArrayList<InlineKeyboardRow>();
		arr.add(new InlineKeyboardRow(
				InlineKeyboardButton.builder().text(lang_config.getLanguage().get("tg_squadcommander_events_btn"))
						.callbackData("squadcommander_events").build()));
		arr.add(new InlineKeyboardRow(
				InlineKeyboardButton.builder().text(lang_config.getLanguage().get("tg_squadcommander_reasons_btn"))
						.callbackData("squadcommander_reasons").build()));
		return arr;
	}

	@Transactional
	private boolean saveData(String message, long chat_id, User user) throws TelegramApiException {
		try {
			message = message.split("text=")[1].split(", entities")[0];
			message = message.toLowerCase().replace(lang_config.getLanguage().get("tg_squadcommander_mark_check"), "")
					.replaceFirst("\n", "");
			Map<Integer, String> details = new HashMap<Integer, String>(); // human ID + Reason
			log.debug("I save the message:\n" + message);
			String[] lines = message.split("\n");
			if (!lines[0].contains("!")) {
				throw new Exception(lang_config.getLanguage().get("tg_squadcommander_error_event"));
			}
			String event = OptionService.getKeyByValue(optionServ.getEvent_types(),
					search.findMostSimilarEvent(lines[0].replaceFirst("!", "")));
			if (optionServ.getEvent_types().containsKey(event) == false) {
				log.warn("Found unknown event: " + event + " Original: " + lines[0]);
				throw new Exception(lang_config.getLanguage().get("tg_squadcommander_error_event"));
			}
			// Recognizing people
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i];
				if (!line.contains(":")) {
					throw new Exception(
							lang_config.getLanguage().get("tg_squadcommander_error_line").replaceAll("!line!", line));
				}
				String[] spl = line.split(":");
				String reason = OptionService.getKeyByValue(optionServ.getReasons_for_absences(),
						search.findMostSimilarReason(spl[1]));
				details.put(Integer.parseInt(spl[0].split(";")[0]), reason);
			}
			StatsFormHelper helper = new StatsFormHelper();
			helper.setDetails(details);
			statsServ.squad_mark(helper, event, user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			sendMessage(chat_id, e.getMessage());
			return false;
		}
	}

	@Transactional
	private String recognizeData(String original_message, long chat_id, List<Human> people)
			throws TelegramApiException {
		try {
			original_message = original_message.toLowerCase().replace("report", "").replaceFirst("\n", "");
			log.debug("I recognize the message:\n" + original_message);
			String[] lines = original_message.split("\n");
			// Recognizing event
			if (!lines[0].contains("!")) {
				throw new Exception(lang_config.getLanguage().get("tg_squadcommander_error_event"));
			}
			String event = search.findMostSimilarEvent(lines[0].replaceFirst("!", ""));
			if (optionServ.getEvent_types().containsValue(event) == false) {
				log.warn("Found unknown event: " + event + " Original: " + lines[0]);
				throw new Exception(lang_config.getLanguage().get("tg_squadcommander_error_event"));
			}
			String result_message = lang_config.getLanguage().get("tg_squadcommander_mark_check") + "!" + event;
			// Recognizing people
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i];
				if (!line.contains(":")) {
					throw new Exception(
							lang_config.getLanguage().get("tg_squadcommander_error_line").replaceAll("!line!", line));
				}
				String[] spl = line.split(":");
				Human human = SearchService.findMostSimilarHuman(spl[0], people);
				String reason = search.findMostSimilarReason(spl[1]);
				if (human == null || reason.isBlank()) {
					throw new Exception(lang_config.getLanguage().get("tg_squadcommander_error_notfound") + line);
				}
				result_message = result_message + "\n" + human.getId() + "; " + human.getLastname() + " "
						+ human.getName() + " : " + reason;
			}
			return result_message;
		} catch (Exception e) {
			sendMessage(chat_id, e.getMessage());
			return "";
		}
	}

	private void sendMessage(String langid, long chat_id) throws TelegramApiException {
		SendMessage message = SendMessage.builder().chatId(chat_id).text(lang_config.getLanguage().get(langid))
				.parseMode("HTML").build();
		telegramClient.execute(message);
	}

	private void sendMessage(long chat_id, String l) throws TelegramApiException {
		SendMessage message = SendMessage.builder().chatId(chat_id).text(l).parseMode("HTML").build();
		telegramClient.execute(message);
	}
}
