package it.unisalento.iot2425.watchapp.user.repositories;

import it.unisalento.iot2425.watchapp.user.domain.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient, String>{

    Optional<Patient> findByEmail(String email);

}
