package cliente.DTOs;

public class LoginDataDTO {
    private String username;
    private String password;

    public LoginDataDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}