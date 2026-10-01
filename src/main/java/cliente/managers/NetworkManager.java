package cliente.managers;

import cliente.DTOs.RequestDTO;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager {
    private String host;
    private int port;
    private Gson gson;

    public NetworkManager(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new Gson();
    }

    // Envia a requisição abrindo uma nova conexão TCP conforme os requisitos não-funcionais
    public String enviarRequisicao(RequestDTO requestDTO) {
        // Converte o objeto de requisição (método + dados) em uma String JSON
        String jsonRequest = gson.toJson(requestDTO);

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envia o JSON para o servidor
            out.println(jsonRequest);
            System.out.println("[CLIENTE] Enviado: " + jsonRequest);

            // Lê a resposta enviada pelo servidor
            String resposta = in.readLine();
            return resposta;

        } catch (Exception e) {
            System.err.println("[ERRO DE REDE] Não foi possível comunicar com o servidor: " + e.getMessage());
            return "{\"statusCode\":500, \"message\":\"Erro de conexão com o servidor\", \"data\":null}";
        }
    }
}