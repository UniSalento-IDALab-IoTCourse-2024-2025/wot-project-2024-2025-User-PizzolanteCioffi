package it.unisalento.iot2425.watchapp.user.restcontrollers;

import it.unisalento.iot2425.watchapp.user.domain.Assistant;
import it.unisalento.iot2425.watchapp.user.domain.Patient;
import it.unisalento.iot2425.watchapp.user.dto.*;
import it.unisalento.iot2425.watchapp.user.exceptions.UserNotFoundException;
import it.unisalento.iot2425.watchapp.user.repositories.AssistantRepository;
import it.unisalento.iot2425.watchapp.user.repositories.PatientRepository;
import it.unisalento.iot2425.watchapp.user.security.JwtUtilities;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static it.unisalento.iot2425.watchapp.user.configuration.SecurityConfig.passwordEncoder;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    AssistantRepository assistantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AllUsersDTO getAll() {
        AllUsersDTO allUsers = new AllUsersDTO();

        List<PatientDTO> patientList = patientRepository.findAll().stream()
                .map(patient -> {
                    PatientDTO dto = new PatientDTO();
                    dto.setId(patient.getId());
                    dto.setName(patient.getName());
                    dto.setSurname(patient.getSurname());
                    dto.setEmail(patient.getEmail());
                    dto.setRole(Patient.getRole());
                    dto.setPassword(patient.getPassword());
                    dto.setAddress(patient.getAddress());
                    dto.setPhoneNumber(patient.getPhoneNumber());
                    dto.setAge(patient.getAge());
                    dto.setAccessToken(patient.getAccessToken());
                    dto.setRefreshToken(patient.getRefreshToken());
                    dto.setAssistantId(patient.getAssistantId());
                    dto.setFcmToken(patient.getFcmToken());
                    dto.setBehaviour(patient.getBehaviour());
                    return dto;
                }).collect(Collectors.toList());

        List<AssistantDTO> assistantList = assistantRepository.findAll().stream()
                .map(assistant -> {
                    AssistantDTO dto = new AssistantDTO();
                    dto.setId(assistant.getId());
                    dto.setName(assistant.getName());
                    dto.setSurname(assistant.getSurname());
                    dto.setEmail(assistant.getEmail());
                    dto.setRole(Assistant.getRole());
                    dto.setPassword(assistant.getPassword());
                    dto.setPhoneNumber(assistant.getPhoneNumber());
                    dto.setAccessToken(assistant.getAccessToken());
                    dto.setRefreshToken(assistant.getRefreshToken());
                    dto.setPatientId(assistant.getPatientId());
                    dto.setFcmToken(assistant.getFcmToken());
                    return dto;
                }).collect(Collectors.toList());

        allUsers.setPatients(patientList);
        allUsers.setAssistants(assistantList);

        return allUsers;
    }



    @RequestMapping(value= "/refreshToken", method = RequestMethod.GET
    )
    public ResponseEntity<?> home(HttpServletRequest request) throws IOException {

        final String authHeader = request.getHeader("Authorization");

        String token = authHeader.substring(7);

        String id=jwtUtilities.extractUserId(token);

        String role=jwtUtilities.extractUserRole(token);

        //prendo il refresh token attuale per poterlo usare per richiedere il nuovo access token

        String uri ="http://localhost:8080/api/users/" + role.toLowerCase() + "/" + id;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> httpData = response.getBody();
        String refreshToken = (String) httpData.get("refreshToken");

        System.out.println("Refresh Token: " + refreshToken);


        //effettuo chiamata a fitbit per ottenere il nuovo access token

        String tokenEndPoint = "https://api.fitbit.com/oauth2/token";
        restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("client_id", "23QJNJ");
        requestData.add("grant_type", "refresh_token");
        requestData.add("refresh_token", refreshToken);


        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("23QJNJ", "3d5aec9ce1ff80ab2f37321836c90d11");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestData, headers);

        response = restTemplate.exchange(tokenEndPoint, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token") || !tokenResponse.containsKey("refresh_token")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore durante il recupero del token da Fitbit.");
        }
        String newAccessToken = (String) tokenResponse.get("access_token");
        String neWRefreshToken = (String) tokenResponse.get("refresh_token");

        //aggiorniamo i token del paziente e dell'assisente associato o viceversa

        uri ="http://localhost:8080/api/users/updateTokens/"  + id;

        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> updatedTokens = new HashMap<>();
        updatedTokens.put("accessToken", newAccessToken);
        updatedTokens.put("refreshToken", neWRefreshToken);
        HttpEntity<Map<String, String>> entityPut = new HttpEntity<>(updatedTokens, headers);

        restTemplate.exchange(uri, HttpMethod.PUT, entityPut, Void.class);

        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @RequestMapping(value = "/patient/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PatientDTO findPatientById(@PathVariable String id) throws UserNotFoundException {

        Optional<Patient> patient = patientRepository.findById(id);

        if (patient.isEmpty()) {
            throw new UserNotFoundException();
        }

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(patient.get().getId());
        patientDTO.setName(patient.get().getName());
        patientDTO.setSurname(patient.get().getSurname());
        patientDTO.setEmail(patient.get().getEmail());
        patientDTO.setRole(Patient.getRole());
        patientDTO.setPassword(patient.get().getPassword());
        patientDTO.setAddress(patient.get().getAddress());
        patientDTO.setPhoneNumber(patient.get().getPhoneNumber());
        patientDTO.setAge(patient.get().getAge());
        patientDTO.setAccessToken(patient.get().getAccessToken());
        patientDTO.setRefreshToken(patient.get().getRefreshToken());
        patientDTO.setAssistantId(patient.get().getAssistantId());

        return patientDTO;
    }

    @RequestMapping(value = "/assistant/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AssistantDTO findAssistantById(@PathVariable String id) throws UserNotFoundException {

        Optional<Assistant> assistant = assistantRepository.findById(id);

        if (assistant.isEmpty()) {
            throw new UserNotFoundException();
        }

        AssistantDTO assistantDTO = new AssistantDTO();
        assistantDTO.setId(assistant.get().getId());
        assistantDTO.setName(assistant.get().getName());
        assistantDTO.setSurname(assistant.get().getSurname());
        assistantDTO.setEmail(assistant.get().getEmail());
        assistantDTO.setRole(Assistant.getRole());
        assistantDTO.setPassword(assistant.get().getPassword());
        assistantDTO.setPhoneNumber(assistant.get().getPhoneNumber());
        assistantDTO.setAccessToken(assistant.get().getAccessToken());
        assistantDTO.setRefreshToken(assistant.get().getRefreshToken());
        assistantDTO.setPatientId(assistant.get().getPatientId());

        return assistantDTO;
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );
        String jwt="";
        if(loginDTO.getRole().equals("Assistant")){
            Optional<Assistant> assistant = assistantRepository.findByEmail(authentication.getName());
            if (assistant.isEmpty()) {
                throw new UserNotFoundException();
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", assistant.get().getId());
            claims.put("role", Assistant.getRole());
            //generiamo il token che conterrà email, ruolo e id
            jwt = jwtUtilities.generateToken(assistant.get().getEmail(), claims);

            String uri ="http://localhost:8080/api/users/refreshToken";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            uri="http://localhost:8080/api/users/updateFcmToken/" + assistant.get().getId();
            headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> fcmToken = new HashMap<>();
            fcmToken.put("fcmToken", loginDTO.getFcmToken());
            HttpEntity<Map<String, String>> entityPut = new HttpEntity<>(fcmToken, headers);

            restTemplate.exchange(uri, HttpMethod.PUT, entityPut, Void.class);


        } else if(loginDTO.getRole().equals("Patient")){
            Optional<Patient> patient = patientRepository.findByEmail(authentication.getName());
            if (patient.isEmpty()) {
                throw new UserNotFoundException();
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", patient.get().getId());
            claims.put("role", Patient.getRole());
            //generiamo il token che conterrà email, ruolo e id
            jwt = jwtUtilities.generateToken(patient.get().getEmail(), claims);

            String uri ="http://localhost:8080/api/users/refreshToken";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            uri="http://localhost:8080/api/users/updateFcmToken/" + patient.get().getId();
            headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> fcmToken = new HashMap<>();
            fcmToken.put("fcmToken", loginDTO.getFcmToken());
            System.out.println("fcmToken:" + loginDTO.getFcmToken());
            HttpEntity<Map<String, String>> entityPut = new HttpEntity<>(fcmToken, headers);

            restTemplate.exchange(uri, HttpMethod.PUT, entityPut, Void.class);
        }
        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));
    }

    //solo l'assistente può eliminare il paziente, per ragioni di sicurezza. se si elimina un paziente, è necessario
    //che venga eliminato anche l'assistente.
    @PreAuthorize("hasRole('Assistant')")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) throws UserNotFoundException {


        Optional<Assistant> assistant = assistantRepository.findById(id);

        String patientId=assistant.get().getPatientId();
        try {
            assistantRepository.deleteById(id);
            patientRepository.deleteById(patientId);
        }
        catch(Exception e) {
            System.out.println("Errore durante l'eliminazione dell'utente " + e.getMessage());
        }



    }

    @RequestMapping(value = "/updateFcmToken/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateFcmToken(@PathVariable String id, @RequestBody Map<String, String> userDTOMap, HttpServletRequest request) {


        final String authHeader = request.getHeader("Authorization");

        String token = authHeader.substring(7);

        String role=jwtUtilities.extractUserRole(token);

        if(role.equals("Patient")){
            Optional<Patient> existingPatientOptional = patientRepository.findById(id);

            if (!existingPatientOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Patient existingPatient = existingPatientOptional.get();

            existingPatient.setFcmToken(userDTOMap.get("fcmToken"));

            //salva utente aggiornato nel database
            Patient updatedPatient = patientRepository.save(existingPatient);

            //converte l'entità in DTO
            PatientDTO updatedPatientDTO = new PatientDTO();

            updatedPatientDTO.setFcmToken(updatedPatient.getFcmToken());

            return ResponseEntity.status(HttpStatus.OK).body(updatedPatientDTO);
        }
        else if(role.equals("Assistant")){
            Optional<Assistant> existingAssistantOptional = assistantRepository.findById(id);

            if (!existingAssistantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Assistant existingAssistant = existingAssistantOptional.get();

            existingAssistant.setFcmToken(userDTOMap.get("fcmToken"));



            //salva utente aggiornato nel database
            Assistant updatedAssistant = assistantRepository.save(existingAssistant);

            //converte l'entità in DTO
            AssistantDTO updatedAssistantDTO = new AssistantDTO();

            updatedAssistantDTO.setFcmToken(updatedAssistant.getFcmToken());

            return ResponseEntity.status(HttpStatus.CREATED).body(updatedAssistantDTO);
        }
        else{
            throw new UserNotFoundException();
        }

    }

    @RequestMapping(value = "/update/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, String> userDTOMap, HttpServletRequest request) {


        final String authHeader = request.getHeader("Authorization");

        String token = authHeader.substring(7);

        String role=jwtUtilities.extractUserRole(token);

        if(role.equals("Patient")){
            Optional<Patient> existingPatientOptional = patientRepository.findById(id);

            if (!existingPatientOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Patient existingPatient = existingPatientOptional.get();

            //aggiorniamo i campi dell'utente
            existingPatient.setName(userDTOMap.get("name"));
            existingPatient.setSurname(userDTOMap.get("surname"));
            existingPatient.setEmail(userDTOMap.get("email"));
            if (userDTOMap.get("password") != null && !userDTOMap.get("password").isEmpty()) {
                existingPatient.setPassword(passwordEncoder().encode(userDTOMap.get("password")));
            }
            existingPatient.setAddress(userDTOMap.get("address"));
            existingPatient.setPhoneNumber(userDTOMap.get("phoneNumber"));
            existingPatient.setAge(Integer.parseInt(userDTOMap.get("age")));

            //salva utente aggiornato nel database
            Patient updatedPatient = patientRepository.save(existingPatient);

            //converte l'entità in DTO
            PatientDTO updatedPatientDTO = new PatientDTO();

            updatedPatientDTO.setId(updatedPatient.getId());
            updatedPatientDTO.setName(updatedPatient.getName());
            updatedPatientDTO.setSurname(updatedPatient.getSurname());
            updatedPatientDTO.setEmail(updatedPatient.getEmail());
            updatedPatientDTO.setPassword(null);
            updatedPatientDTO.setAddress(updatedPatient.getAddress());
            updatedPatientDTO.setPhoneNumber(updatedPatient.getPhoneNumber());
            updatedPatientDTO.setAge(updatedPatient.getAge());
            updatedPatientDTO.setRole(updatedPatient.getRole());
            updatedPatientDTO.setAccessToken(updatedPatient.getAccessToken());
            updatedPatientDTO.setRefreshToken(updatedPatient.getRefreshToken());
            updatedPatientDTO.setAssistantId(updatedPatient.getAssistantId());
            updatedPatientDTO.setFcmToken(updatedPatient.getFcmToken());

            return ResponseEntity.status(HttpStatus.OK).body(updatedPatientDTO);
        }
        else if(role.equals("Assistant")){
            Optional<Assistant> existingAssistantOptional = assistantRepository.findById(id);

            if (!existingAssistantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Assistant existingAssistant = existingAssistantOptional.get();

            //aggiorniamo i campi dell'utente
            existingAssistant.setName(userDTOMap.get("name"));
            existingAssistant.setSurname(userDTOMap.get("surname"));
            existingAssistant.setEmail(userDTOMap.get("email"));
            if (userDTOMap.get("password") != null && !userDTOMap.get("password").isEmpty()) {
                existingAssistant.setPassword(passwordEncoder().encode(userDTOMap.get("password")));
            }
            existingAssistant.setPhoneNumber(userDTOMap.get("phoneNumber"));


            //salva utente aggiornato nel database
            Assistant updatedAssistant = assistantRepository.save(existingAssistant);

            //converte l'entità in DTO
            AssistantDTO updatedAssistantDTO = new AssistantDTO();

            updatedAssistantDTO.setId(updatedAssistant.getId());
            updatedAssistantDTO.setName(updatedAssistant.getName());
            updatedAssistantDTO.setSurname(updatedAssistant.getSurname());
            updatedAssistantDTO.setEmail(updatedAssistant.getEmail());
            updatedAssistantDTO.setPassword(null);
            updatedAssistantDTO.setPhoneNumber(updatedAssistant.getPhoneNumber());
            updatedAssistantDTO.setRole(updatedAssistant.getRole());
            updatedAssistantDTO.setAccessToken(updatedAssistant.getAccessToken());
            updatedAssistantDTO.setRefreshToken(updatedAssistant.getRefreshToken());
            updatedAssistantDTO.setPatientId(updatedAssistant.getPatientId());
            updatedAssistantDTO.setFcmToken(updatedAssistant.getFcmToken());

            return ResponseEntity.status(HttpStatus.CREATED).body(updatedAssistantDTO);
        }
        else{
            throw new UserNotFoundException();
        }

    }

    @RequestMapping(value = "/updateTokens/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateTokens(@PathVariable String id, @RequestBody Map<String, String> updatedTokens, HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        String token = authHeader.substring(7);

        String role=jwtUtilities.extractUserRole(token);

        if(role.equals("Assistant")){
            //cerco l'assisente per id, e modifico i sui token e quelli del paziente associato
            Optional<Assistant> existingAssistantOptional = assistantRepository.findById(id);

            if (!existingAssistantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Assistant existingAssistant = existingAssistantOptional.get();
            existingAssistant.setAccessToken(updatedTokens.get("accessToken"));
            existingAssistant.setRefreshToken(updatedTokens.get("refreshToken"));
            assistantRepository.save(existingAssistant);

            String patiendId= existingAssistant.getPatientId();

            Optional<Patient> existingPatientOptional = patientRepository.findById(patiendId);
            if (!existingPatientOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Patient existingPatient = existingPatientOptional.get();
            existingPatient.setAccessToken(updatedTokens.get("accessToken"));
            existingPatient.setRefreshToken(updatedTokens.get("refreshToken"));
            patientRepository.save(existingPatient);


        }
        else if(role.equals("Patient")){
            //cerco il paziente per id, e modifico i sui token e quelli dell'assistente associato
            Optional<Patient> existingPatientOptional = patientRepository.findById(id);
            if (!existingPatientOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Patient existingPatient = existingPatientOptional.get();
            existingPatient.setAccessToken(updatedTokens.get("accessToken"));
            existingPatient.setRefreshToken(updatedTokens.get("refreshToken"));
            patientRepository.save(existingPatient);

            String assistantId = existingPatient.getAssistantId();
            Optional<Assistant> existingAssistantOptional = assistantRepository.findById(assistantId);

            if (!existingAssistantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Assistant existingAssistant = existingAssistantOptional.get();
            existingAssistant.setAccessToken(updatedTokens.get("accessToken"));
            existingAssistant.setRefreshToken(updatedTokens.get("refreshToken"));
            assistantRepository.save(existingAssistant);
        }

        return ResponseEntity.status(HttpStatus.OK).body("Tokens updated successfully");

    }

    @RequestMapping(value = "/updateBehaviour/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateBehaviour (@PathVariable String id, @RequestBody Map <String, Integer> behaviourMap){

        Optional<Patient> existingPatientOptional = patientRepository.findById(id);

        Patient existingPatient = existingPatientOptional.get();

        existingPatient.setBehaviour(behaviourMap.get("behaviour"));

        //salva utente aggiornato nel database
        Patient updatedPatient = patientRepository.save(existingPatient);

        PatientDTO updatedPatientDTO = new PatientDTO();

        updatedPatientDTO.setId(updatedPatient.getId());
        updatedPatientDTO.setName(updatedPatient.getName());
        updatedPatientDTO.setSurname(updatedPatient.getSurname());
        updatedPatientDTO.setEmail(updatedPatient.getEmail());
        updatedPatientDTO.setPassword(null);
        updatedPatientDTO.setAddress(updatedPatient.getAddress());
        updatedPatientDTO.setPhoneNumber(updatedPatient.getPhoneNumber());
        updatedPatientDTO.setAge(updatedPatient.getAge());
        updatedPatientDTO.setRole(updatedPatient.getRole());
        updatedPatientDTO.setAccessToken(updatedPatient.getAccessToken());
        updatedPatientDTO.setBehaviour(updatedPatient.getBehaviour());
        updatedPatientDTO.setRefreshToken(updatedPatient.getRefreshToken());
        updatedPatientDTO.setAssistantId(updatedPatient.getAssistantId());

        return ResponseEntity.ok(updatedPatientDTO);
    }

    //restituisce il behaviour del paziente, sia nel caso del paziente che nel caso dell'assistente
    @RequestMapping(value = "/behaviour",
            method = RequestMethod.GET,
           produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getBehaviour ( HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        String token = authHeader.substring(7);

        String role = jwtUtilities.extractUserRole(token);
        String id = jwtUtilities.extractUserId(token);
        System.out.println("ruolo: " + role);
        System.out.println("id:" + id);


        if (role.equals("Patient")) {
            Optional<Patient> patient = patientRepository.findById(id);
            Map<String, Integer> behaviourMap = new HashMap<>();
            if (patient.isPresent()) {
                behaviourMap.put("behaviour", patient.get().getBehaviour());
                return ResponseEntity.ok(behaviourMap);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
            }
        } else if (role.equals("Assistant")) {
            Optional<Assistant> assistant = assistantRepository.findById(id);
            String patientId = assistant.get().getPatientId();
            Optional<Patient> patient = patientRepository.findById(patientId);
            Map<String, Integer> behaviourMap = new HashMap<>();
            if (patient.isPresent()) {
                behaviourMap.put("behaviour", patient.get().getBehaviour());
                return ResponseEntity.ok(behaviourMap);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
            }

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

    }


}
