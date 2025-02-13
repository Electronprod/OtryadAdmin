package ru.electronprod.OtryadAdmin.telegram;

import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import ru.electronprod.OtryadAdmin.data.ChatRepository;
import ru.electronprod.OtryadAdmin.data.filesystem.TelegramLanguage;

@Service
public class BotService {
	@Autowired
	private TelegramLanguage lang;
	@Autowired
	private ChatRepository chatRep;

	public void sendMessage(Long chatId, String message) {
		var msg = new SendMessage(chatId, message).parseMode(ParseMode.HTML);
		BotConfig.telegramBot.execute(msg);
	}

	public SendMessage sendMessageObject(Long chatId, String message) {
		return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
	}

	@Transactional
	public void registerChat(User user, Chat chat) {
		if (chatRep.findByChatId(chat.id()).isEmpty()) {
			var chatObj = new ru.electronprod.OtryadAdmin.models.Chat();
			chatObj.setChatId(chat.id());
			chatObj.setUsername(user.username());
			chatObj.setFirstname(user.firstName());
			chatObj.setLastname(user.lastName());
			chatObj.setRegtime(new Timestamp(System.currentTimeMillis()));
			chatObj.setOwner(null);
			chatRep.save(chatObj);
		}
	}

	public boolean isRegistered(Long chatID) {
		return chatRep.findByChatId(chatID).isPresent();
	}

	@Async
	public void sendPreparedMessage(String key, ru.electronprod.OtryadAdmin.models.Chat chat) {
		sendMessage(chat.getChatId(), lang.get(key));
	}

	@Async
	public void sendSignedMessage(String content, String author, ru.electronprod.OtryadAdmin.models.Chat chat) {
		sendMessage(chat.getChatId(), lang.get("send").replace("%content%", content).replace("%author%", author));
	}

	@Async
	public void sendSignedReminder(String title, String content, String author,
			ru.electronprod.OtryadAdmin.models.Chat chat) {
		sendMessage(chat.getChatId(), lang.get("reminder_authored").replace("%eventname%", title)
				.replace("%content%", content).replace("%author%", author));
		chat.setRemaindsCounter(chat.getRemaindsCounter() + 1);
		chatRep.save(chat);
	}

	@Async
	public void sendNotification_marked(ru.electronprod.OtryadAdmin.models.User user, String eventName) {
		Optional<ru.electronprod.OtryadAdmin.models.Chat> chat = chatRep.findByOwner(user);
		if (chat.isPresent()) {
			if (chat.get().isSendMarkedNotification()) {
				var msg = new SendMessage(chat.orElseThrow().getChatId(),
						lang.get("marked").replace("%event%", eventName)).parseMode(ParseMode.HTML)
						.replyMarkup(getNotification_marked_button(chat.orElseThrow()));
				BotConfig.telegramBot.execute(msg);
			}
		}
	}

	public InlineKeyboardMarkup getNotification_marked_button(ru.electronprod.OtryadAdmin.models.Chat chat) {
		InlineKeyboardMarkup newKeyboard;
		if (chat.isSendMarkedNotification()) {
			newKeyboard = new InlineKeyboardMarkup(
					new InlineKeyboardButton[] { new InlineKeyboardButton(lang.get("switch_off_mark_notifications"))
							.callbackData("switch_mark_notifications") });
		} else {
			newKeyboard = new InlineKeyboardMarkup(
					new InlineKeyboardButton[] { new InlineKeyboardButton(lang.get("switch_on_mark_notifications"))
							.callbackData("switch_mark_notifications") });
		}
		return newKeyboard;
	}
}
