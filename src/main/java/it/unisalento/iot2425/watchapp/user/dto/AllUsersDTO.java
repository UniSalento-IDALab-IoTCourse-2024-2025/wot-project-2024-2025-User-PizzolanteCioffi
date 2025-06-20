package it.unisalento.iot2425.watchapp.user.dto;

import java.util.List;

public class AllUsersDTO {
    private List<PatientDTO> patients;
    private List<AssistantDTO> assistants;

    public List<PatientDTO> getPatients() {
        return patients;
    }

    public void setPatients(List<PatientDTO> patients) {
        this.patients = patients;
    }

    public List<AssistantDTO> getAssistants() {
        return assistants;
    }

    public void setAssistants(List<AssistantDTO> assistants) {
        this.assistants = assistants;
    }
}
