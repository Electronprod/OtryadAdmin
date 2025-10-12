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
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.ChatRepository;
import ru.electronprod.OtryadAdmin.data.UserRepository;
import ru.electronprod.OtryadAdmin.data.filesystem.TelegramLanguage;

/**
 * Provides methods to interact with telegram bot easily
 */
@Slf4j
@Service
public class BotService {
	@Autowired
	private TelegramLanguage lang;
	@Autowired
	private ChatRepository chatRep;
	@Autowired
	private UserRepository userRep;

	/**
	 * Sends text message with HTML markup support
	 * 
	 * @param chatId  - chat id to send message to
	 * @param message - message to send
	 */
	public void sendMessage(Long chatId, String message) {
		var msg = new SendMessage(chatId, message).parseMode(ParseMode.HTML);
		BotConfig.telegramBot.execute(msg);
	}

	/**
	 * Returns text message object prepared for sending using telegram with HTML
	 * markup support
	 * 
	 * @param chatId  - chat id to send message to
	 * @param message - message to send
	 */
	public SendMessage sendMessageObject(Long chatId, String message) {
		return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
	}

	/**
	 * Saves information required about chat and user to database
	 * 
	 * @param user - User to register
	 * @param chat - Chat to register
	 */
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

	/**
	 * Is this chatID saved to database?
	 * 
	 * @param chatID - chat id to check
	 * @return true/false (found/not found)
	 */
	public boolean isRegistered(Long chatID) {
		return chatRep.findByChatId(chatID).isPresent();
	}

	/**
	 * Sends message got by the key from
	 * {@link ru.electronprod.OtryadAdmin.data.filesystem.TelegramLanguage}
	 * 
	 * @param key  - message key
	 * @param chat - chat to send to
	 */
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
		sendNotification_marked_staff(user, eventName);
	}

	private void sendNotification_marked_staff(ru.electronprod.OtryadAdmin.models.User user, String eventName) {
		var chats = chatRep.findByOwnerIn(userRep.findAllByRole("ROLE_ADMIN"));
		chats.forEach(chat -> {
			try {
				if (chat.isSendMarkedNotification()) {
					var msg = new SendMessage(chat.getChatId(),
							lang.get("marked_staff").replace("%user%", user.getName()).replace("%event%", eventName))
							.parseMode(ParseMode.HTML).replyMarkup(getNotification_marked_button(chat));
					BotConfig.telegramBot.execute(msg);
				}
			} catch (Exception e) {
				log.warn("Mark message to staff error:" + e.getMessage());
			}
		});
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
