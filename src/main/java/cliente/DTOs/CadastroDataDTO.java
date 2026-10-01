package cliente.DTOs;

public class CadastroDataDTO {
    private String name;
    private String username;
    private String password;

    public CadastroDataDTO(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
}