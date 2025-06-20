package it.unisalento.iot2425.watchapp.user.repositories;

import it.unisalento.iot2425.watchapp.user.domain.Assistant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AssistantRepository extends MongoRepository<Assistant, String> {
    Optional<Assistant> findByEmail(String name);
}
