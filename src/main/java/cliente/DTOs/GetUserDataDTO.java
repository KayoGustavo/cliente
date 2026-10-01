package cliente.DTOs;

public class GetUserDataDTO {
    private String token;
    private String username;

    public GetUserDataDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }
}