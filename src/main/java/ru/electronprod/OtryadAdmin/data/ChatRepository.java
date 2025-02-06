package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.electronprod.OtryadAdmin.models.Chat;
import ru.electronprod.OtryadAdmin.models.User;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
	Optional<Chat> findByChatId(Long chatId);

	Optional<Chat> findByOwner(User owner);
}
