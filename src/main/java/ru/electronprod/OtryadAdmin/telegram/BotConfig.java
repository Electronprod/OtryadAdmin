package ru.electronprod.OtryadAdmin.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;

/**
 * Telegram bot configuration. Required to obtain the telegramBot object
 */
@Configuration
public class BotConfig implements TelegramBotGlobalPropertiesConfiguration {
	@Getter
	@Value("${telegram.bot.token}")
	private String token;
	/**
	 * Telegram bot object
	 */
	public static TelegramBot telegramBot;

	@Override
	public void configure(TelegramBotGlobalProperties.Builder builder) {
		builder.processBot(token, bot -> {
			BotConfig.telegramBot = bot;
		});
	}
}
