package it.unisalento.iot2425.watchapp.user.dto;

public class RegistrationResultDTO {
    //patient
    private String patientId;
    private String patientName;
    private String patientSurname;
    private String patientEmail;
    private String patientRole;
    private String patientPassword;
    private String patientPhoneNumber;
    private Integer patientAge;
    private String patientAddress;
    private String patientRefreshToken;
    private String patientAccessToken;
    private String patientAssistantId;
    private Integer behaviour;


    //assistant
    private String assistantId;
    private String assistantName;
    private String assistantSurname;
    private String assistantEmail;
    private String assistantRole;
    private String assistantPassword;
    private String assistantPhoneNumber;
    private String assistantRefreshToken;
    private String assistantAccessToken;
    private String assistantPatientId;


    public Integer getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(Integer behaviour) {
        this.behaviour = behaviour;
    }

    public String getPatientAssistantId() {
        return patientAssistantId;
    }

    public void setPatientAssistantId(String patientAssistantId) {
        this.patientAssistantId = patientAssistantId;
    }

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

    public String getPatientRole() {
        return patientRole;
    }

    public void setPatientRole(String patientRole) {
        this.patientRole = patientRole;
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

    public String getPatientRefreshToken() {
        return patientRefreshToken;
    }

    public void setPatientRefreshToken(String patientRefreshToken) {
        this.patientRefreshToken = patientRefreshToken;
    }

    public String getPatientAccessToken() {
        return patientAccessToken;
    }

    public void setPatientAccessToken(String patientAccessToken) {
        this.patientAccessToken = patientAccessToken;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
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

    public String getAssistantRole() {
        return assistantRole;
    }

    public void setAssistantRole(String assistantRole) {
        this.assistantRole = assistantRole;
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

    public String getAssistantRefreshToken() {
        return assistantRefreshToken;
    }

    public void setAssistantRefreshToken(String assistantRefreshToken) {
        this.assistantRefreshToken = assistantRefreshToken;
    }

    public String getAssistantAccessToken() {
        return assistantAccessToken;
    }

    public void setAssistantAccessToken(String assistantAccessToken) {
        this.assistantAccessToken = assistantAccessToken;
    }

    public String getAssistantPatientId() {
        return assistantPatientId;
    }

    public void setAssistantPatientId(String assistantPatientId) {
        this.assistantPatientId = assistantPatientId;
    }
}
