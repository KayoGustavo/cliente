package cliente.DTOs;

public class DeleteUserDataDTO {
    private String token;
    private String username;

    public DeleteUserDataDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }
}