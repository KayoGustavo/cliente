package cliente.DTOs;


public class UpdatePasswordDataDTO {
    private String token;
    private String username;
    private String oldPassword;
    private String newPassword;

    public UpdatePasswordDataDTO(String token, String username, String oldPassword, String newPassword) {
        this.token = token;
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}