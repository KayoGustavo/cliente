package cliente.DTOs;

public class AtualizarDataDTO {
    private String token;
    private String name;
    private String password;

    public AtualizarDataDTO(String token, String name, String password) {
        this.token = token;
        this.name = name;
        this.password = password;
    }
}