package it.unisalento.iot2425.watchapp.user.service;


import it.unisalento.iot2425.watchapp.user.domain.Assistant;
import it.unisalento.iot2425.watchapp.user.domain.Patient;
import it.unisalento.iot2425.watchapp.user.repositories.AssistantRepository;
import it.unisalento.iot2425.watchapp.user.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    AssistantRepository assistantRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Patient> patient = patientRepository.findByEmail(email);


        if(patient.isPresent()) {
            return org.springframework.security.core.userdetails.User.withUsername(patient.get().getEmail())
                    .password(patient.get().getPassword())
                    .roles(patient.get().getRole())
                    .build();

        }

        Optional<Assistant> assistant = assistantRepository.findByEmail(email);
        if(assistant.isPresent()) {
            return org.springframework.security.core.userdetails.User.withUsername(assistant.get().getEmail())
                    .password(assistant.get().getPassword())
                    .roles(assistant.get().getRole())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
