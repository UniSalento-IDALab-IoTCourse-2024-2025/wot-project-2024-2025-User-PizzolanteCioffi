package it.unisalento.iot2425.watchapp.user.restcontrollers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.iot2425.watchapp.user.domain.Assistant;
import it.unisalento.iot2425.watchapp.user.domain.Patient;
import it.unisalento.iot2425.watchapp.user.dto.RegistrationDTO;
import it.unisalento.iot2425.watchapp.user.dto.RegistrationResultDTO;
import it.unisalento.iot2425.watchapp.user.messaging.EmailTemplate;
import it.unisalento.iot2425.watchapp.user.messaging.MqttPublisherService;
import it.unisalento.iot2425.watchapp.user.repositories.AssistantRepository;
import it.unisalento.iot2425.watchapp.user.repositories.PatientRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static it.unisalento.iot2425.watchapp.user.configuration.SecurityConfig.passwordEncoder;

@RestController
@RequestMapping("/api")
public class UserRegistrationRestController {

    public String url ="https://1dkrfof8di.execute-api.us-east-1.amazonaws.com/dev";
    public String accessToken="";
    public String refreshToken="";

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    AssistantRepository assistantRepository;

    @Autowired
    MqttPublisherService publisherService;


    //richiesta senza necessità di inserire i campi di registration
    @RequestMapping(value="/registration1",
            method= RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> redirectPatient() throws JsonProcessingException {

    String redirectUrl= UriComponentsBuilder.fromHttpUrl("http://www.fitbit.com/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", "23QJNJ")
            .queryParam("redirect_uri", url + "/api/temporary")

            .queryParam("scope", "heartrate sleep respiratory_rate oxygen_saturation temperature weight nutrition profile activity location")
            .encode(StandardCharsets.UTF_8)
            .build().toUriString();

    return ResponseEntity.ok(Map.of("fitbitAuthUrl", redirectUrl));

    }

    //indirizzo di callback. qui devo trovare l'access token e il refresh token
    @RequestMapping(value= "/temporary", method = RequestMethod.GET
    )
    public ResponseEntity<?> home(@RequestParam("code") String code, HttpServletResponse resp) throws IOException {

        String tokenEndPoint = "https://api.fitbit.com/oauth2/token";
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("client_id", "23QJNJ");
        requestData.add("grant_type", "authorization_code");
        requestData.add("redirect_uri", url + "/api/temporary");
        requestData.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("23QJNJ", "3d5aec9ce1ff80ab2f37321836c90d11");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestData, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenEndPoint, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token") || !tokenResponse.containsKey("refresh_token")) {
           // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore durante il recupero del token da Fitbit.");
            //return "Errore durante il recupero del token da Fitbit.";
        }
         accessToken = (String) tokenResponse.get("access_token");
         refreshToken = (String) tokenResponse.get("refresh_token");

         String url1="https://1dkrfof8di.execute-api.us-east-1.amazonaws.com/dev/home.html";
        ResponseEntity <String> response1 = restTemplate.exchange(url1 , HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(response1.getBody());

    }


    @RequestMapping(value= "/registration2", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> savePatient(@RequestBody RegistrationDTO registrationDTO) throws IOException {

        Optional<Patient> existingPatient = patientRepository.findByEmail(registrationDTO.getPatientEmail());
        Optional<Assistant> existingAssistant = assistantRepository.findByEmail(registrationDTO.getAssistantEmail());

        if(existingPatient.isPresent() || existingAssistant.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("email già associata ad un altro utente");
        }

        Patient patient= new Patient();
        registrationDTO.setAccessToken(accessToken);
        registrationDTO.setRefreshToken(refreshToken);

        patient.setName(registrationDTO.getPatientName());
        patient.setSurname(registrationDTO.getPatientSurname());
        patient.setEmail(registrationDTO.getPatientEmail());
        patient.setPassword(passwordEncoder().encode(registrationDTO.getPatientPassword()));
        patient.setAddress(registrationDTO.getPatientAddress());
        patient.setAge(registrationDTO.getPatientAge());
        patient.setPhoneNumber(registrationDTO.getPatientPhoneNumber());
        patient.setAccessToken(registrationDTO.getAccessToken());
        patient.setRefreshToken(registrationDTO.getRefreshToken());
        patient.setBehaviour(3);

        patient= patientRepository.save(patient);

        Assistant assistant = new Assistant();
        assistant.setName(registrationDTO.getAssistantName());
        assistant.setSurname(registrationDTO.getAssistantSurname());
        assistant.setEmail(registrationDTO.getAssistantEmail());
        assistant.setPassword(passwordEncoder().encode(registrationDTO.getAssistantPassword()));
        assistant.setPhoneNumber(registrationDTO.getAssistantPhoneNumber());
        assistant.setAccessToken(registrationDTO.getAccessToken());
        assistant.setRefreshToken(registrationDTO.getRefreshToken());
        assistant.setPatientId(patient.getId());

        assistant = assistantRepository.save(assistant);

        patient.setAssistantId(assistant.getId());
        patient= patientRepository.save(patient);


        RegistrationResultDTO resultDTO= new RegistrationResultDTO();

        resultDTO.setPatientId(patient.getId());
        resultDTO.setPatientRole(Patient.getRole());
        resultDTO.setPatientName(patient.getName());
        resultDTO.setPatientSurname(patient.getSurname());
        resultDTO.setPatientEmail(patient.getEmail());
        resultDTO.setPatientPhoneNumber(patient.getPhoneNumber());
        resultDTO.setPatientAge(patient.getAge());
        resultDTO.setPatientAddress(patient.getAddress());
        resultDTO.setPatientPassword(patient.getPassword());
        resultDTO.setPatientAccessToken(patient.getAccessToken());
        resultDTO.setPatientRefreshToken(patient.getRefreshToken());
        resultDTO.setPatientAssistantId(patient.getAssistantId());
        resultDTO.setBehaviour(patient.getBehaviour());

        resultDTO.setAssistantId(assistant.getId());
        resultDTO.setAssistantRole(Assistant.getRole());
        resultDTO.setAssistantName(assistant.getName());
        resultDTO.setAssistantSurname(assistant.getSurname());
        resultDTO.setAssistantEmail(assistant.getEmail());
        resultDTO.setAssistantPhoneNumber(assistant.getPhoneNumber());
        resultDTO.setAssistantPassword(assistant.getPassword());
        resultDTO.setAssistantAccessToken(assistant.getAccessToken());
        resultDTO.setAssistantRefreshToken(assistant.getRefreshToken());
        resultDTO.setAssistantPatientId(assistant.getPatientId());


        //patient
        Map<String, Object> message = new HashMap<>();
        //String path="src/main/resources/static/email.html";
        InputStream templateStream= getClass().getClassLoader().getResourceAsStream("static/email.html");
        String emailHtml = EmailTemplate.loadEmailTemplate(templateStream, patient.getName(), patient.getEmail());
        message.put("message", emailHtml);
        message.put("subject", "Welcome to WatchApp! 🎉");
        message.put("receiverEmail", patient.getEmail());

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] payload = objectMapper.writeValueAsBytes(message);

        publisherService.publish("registration", payload, 1);

        //assistant
         message = new HashMap<>();
         //path="src/main/resources/static/email.html";
         templateStream= getClass().getClassLoader().getResourceAsStream("static/email.html");
        emailHtml = EmailTemplate.loadEmailTemplate(templateStream, assistant.getName(), assistant.getEmail());
        message.put("message", emailHtml);
        message.put("subject", "Welcome to WatchApp! 🎉");
        message.put("receiverEmail", assistant.getEmail());

        objectMapper = new ObjectMapper();
        payload = objectMapper.writeValueAsBytes(message);

        publisherService.publish("registration", payload, 1);

        return new ResponseEntity<>(resultDTO, HttpStatus.OK);

    }


}

