package ru.electronprod.OtryadAdmin.telegram;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
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

	public void sendRegisteredGreeting(ru.electronprod.OtryadAdmin.models.Chat chat) {
		CompletableFuture.runAsync(() -> {
			sendMessage(chat.getChatId(), lang.get("registered"));
		});
	}

	public void sendParting(ru.electronprod.OtryadAdmin.models.Chat chat) {
		CompletableFuture.runAsync(() -> {
			sendMessage(chat.getChatId(), lang.get("parting"));
		});
	}

	public void sendMessage(String content, String author, ru.electronprod.OtryadAdmin.models.Chat chat) {
		CompletableFuture.runAsync(() -> {
			sendMessage(chat.getChatId(), lang.get("send").replace("%content%", content).replace("%author%", author));
		});
	}

	public void sendMarkedNotification(ru.electronprod.OtryadAdmin.models.User user, String eventName) {
		CompletableFuture.runAsync(() -> {
			Optional<ru.electronprod.OtryadAdmin.models.Chat> chat = chatRep.findByOwner(user);
			if (chat.isPresent())
				sendMessage(chat.orElseThrow().getChatId(), lang.get("marked").replace("%event%", eventName));
		});
	}

	public void sendRemainderAutodetect(String eventName, ru.electronprod.OtryadAdmin.models.Chat chat) {
		CompletableFuture.runAsync(() -> {
			sendMessage(chat.getChatId(), lang.get("remainder_autodetect").replace("%eventname%", eventName));
		});
	}

	public void sendRemainderFrom(String title, String content, String author,
			ru.electronprod.OtryadAdmin.models.Chat chat) {
		CompletableFuture.runAsync(() -> {
			sendMessage(chat.getChatId(), lang.get("remainder_authored").replace("%eventname%", title)
					.replace("%content%", content).replace("%author%", author));
		});
	}
}
