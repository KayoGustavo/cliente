package cliente.managers;

import cliente.DTOs.RequestDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetworkManager {

    private static final int TEMPO_CONEXAO_MS = 5_000;
    private static final int TEMPO_RESPOSTA_MS = 15_000;
    private static final String ERRO_DE_CONEXAO =
            "{\"statusCode\":500, \"message\":\"Erro de conexão com o servidor\", \"data\":null}";

    private final String host;
    private final int port;
    // serializeNulls: o protocolo usa campos com valor null (ex.: "licensePlate": null)
    private final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    public NetworkManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Envia a requisição abrindo uma nova conexão TCP conforme os requisitos não-funcionais
    public String enviarRequisicao(RequestDTO requestDTO) {
        // Converte o objeto de requisição (método + dados) em uma String JSON
        String jsonRequest = gson.toJson(requestDTO);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TEMPO_CONEXAO_MS);
            socket.setSoTimeout(TEMPO_RESPOSTA_MS);

            // UTF-8 nos dois sentidos: nomes e mensagens têm acentos
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Envia o JSON para o servidor
            out.println(jsonRequest);

            // Lê a resposta enviada pelo servidor
            String resposta = in.readLine();
            return (resposta != null) ? resposta : ERRO_DE_CONEXAO;

        } catch (Exception e) {
            System.err.println("[ERRO DE REDE] Não foi possível comunicar com o servidor: " + e.getMessage());
            return ERRO_DE_CONEXAO;
        }
        // o try-with-resources fecha o socket: conexão encerrada pelo cliente
    }
}