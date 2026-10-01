package cliente.DTOs;

public class UpdateUsernameDataDTO {
    private String token;
    private String username;
    private String name;

    public UpdateUsernameDataDTO(String token, String username, String name) {
        this.token = token;
        this.username = username;
        this.name = name;
    }
}