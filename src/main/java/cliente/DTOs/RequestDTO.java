package cliente.DTOs;

public class RequestDTO {
    private String method;
    private Object data; // Pode receber qualquer DTO (Login, Cadastro, Token, etc.)

    public RequestDTO(String method, Object data) {
        this.method = method;
        this.data = data;
    }

    public String getMethod() {
        return method;
    }

    public Object getData() {
        return data;
    }
}