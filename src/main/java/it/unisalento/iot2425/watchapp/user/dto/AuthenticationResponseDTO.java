package it.unisalento.iot2425.watchapp.user.dto;

public class AuthenticationResponseDTO {
    private String jwt;

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public AuthenticationResponseDTO(String jwt) {
        this.jwt = jwt;
    }
}
