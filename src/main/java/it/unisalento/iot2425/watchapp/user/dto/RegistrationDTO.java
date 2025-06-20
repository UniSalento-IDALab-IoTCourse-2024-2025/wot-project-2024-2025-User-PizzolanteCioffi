package it.unisalento.iot2425.watchapp.user.dto;

public class RegistrationDTO {
    //patient
    private String patientId;
    private String patientName;
    private String patientSurname;
    private String patientEmail;
    private String patientPassword;
    private String patientPhoneNumber;
    private Integer patientAge;
    private String patientAddress;
    private String refreshToken;
    private String accessToken;


    //assistant
    private String assistantId;
    private String assistantName;
    private String assistantSurname;
    private String assistantEmail;
    private String assistantPassword;
    private String assistantPhoneNumber;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPassword() {
        return patientPassword;
    }

    public void setPatientPassword(String patientPassword) {
        this.patientPassword = patientPassword;
    }

    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }

    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantDd) {
        this.assistantId = assistantDd;
    }

    public String getAssistantName() {
        return assistantName;
    }

    public void setAssistantName(String assistantName) {
        this.assistantName = assistantName;
    }

    public String getAssistantSurname() {
        return assistantSurname;
    }

    public void setAssistantSurname(String assistantSurname) {
        this.assistantSurname = assistantSurname;
    }

    public String getAssistantEmail() {
        return assistantEmail;
    }

    public void setAssistantEmail(String assistantEmail) {
        this.assistantEmail = assistantEmail;
    }

    public String getAssistantPassword() {
        return assistantPassword;
    }

    public void setAssistantPassword(String assistantPassword) {
        this.assistantPassword = assistantPassword;
    }

    public String getAssistantPhoneNumber() {
        return assistantPhoneNumber;
    }

    public void setAssistantPhoneNumber(String assistantPhoneNumber) {
        this.assistantPhoneNumber = assistantPhoneNumber;
    }
}
