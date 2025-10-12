package ru.electronprod.OtryadAdmin.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.electronprod.OtryadAdmin.models.Chat;
import ru.electronprod.OtryadAdmin.models.User;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for
 * {@link ru.electronprod.OtryadAdmin.models.Chat}
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
	Optional<Chat> findByChatId(Long chatId);

	Optional<Chat> findByOwner(User owner);

	List<Chat> findByOwnerIn(List<User> owners);
}
