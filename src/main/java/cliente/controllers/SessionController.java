package cliente.controllers;

import cliente.DTOs.*;
import cliente.managers.NetworkManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Versão do UserController sem Scanner/System.out, para ser usada pela interface gráfica.
 * Cada método recebe os dados já prontos, envia a requisição e devolve a resposta (JSON em texto).
 * Quem chama decide como mostrar o resultado.
 */
public class SessionController {

    private final Supplier<NetworkManager> conexao;
    private final Consumer<String> log;
    private final Gson gson = new Gson();

    private volatile String tokenSessao;
    private volatile String usernameLogado;

    /**
     * @param conexao fornece o NetworkManager com o IP/porta atuais da tela
     * @param log     recebe as linhas de log (mensagem enviada e resposta)
     */
    public SessionController(Supplier<NetworkManager> conexao, Consumer<String> log) {
        this.conexao = conexao;
        this.log = log;
    }

    public boolean estaLogado() {
        return tokenSessao != null;
    }

    public String getUsernameLogado() {
        return usernameLogado;
    }

    // [A] Cadastro
    public String cadastrar(String name, String username, String password) {
        return enviar(new RequestDTO("register", new CadastroDataDTO(name, username, password)));
    }

    // [C] Login: guarda o token se o servidor responder 200
    public String login(String username, String password) {
        String resposta = enviar(new RequestDTO("login", new LoginDataDTO(username, password)));
        if (statusCode(resposta) == 200) {
            JsonObject dados = dados(resposta);
            if (dados != null && dados.has("token") && !dados.get("token").isJsonNull()) {
                this.tokenSessao = dados.get("token").getAsString();
                this.usernameLogado = username;
            }
        }
        return resposta;
    }

    // [B] Consulta dos próprios dados
    public String consultar() {
        exigirLogin();
        return enviar(new RequestDTO("getuser", new GetUserDataDTO(tokenSessao, usernameLogado)));
    }

    // [E] Atualizar nome
    public String atualizarNome(String novoNome) {
        exigirLogin();
        return enviar(new RequestDTO("updateusername",
                new UpdateUsernameDataDTO(tokenSessao, usernameLogado, novoNome)));
    }

    // [E] Atualizar senha
    public String atualizarSenha(String senhaAntiga, String novaSenha) {
        exigirLogin();
        return enviar(new RequestDTO("updateuserpassword",
                new UpdatePasswordDataDTO(tokenSessao, usernameLogado, senhaAntiga, novaSenha)));
    }

    // [D] Logout: a sessão local é limpa mesmo que o servidor responda erro
    public String logout() {
        exigirLogin();
        String resposta = enviar(new RequestDTO("logout", new TokenDataDTO(tokenSessao)));
        limparSessao();
        return resposta;
    }

    // [F] Apagar conta: limpa a sessão se o servidor responder 200
    public String apagarConta() {
        exigirLogin();
        String resposta = enviar(new RequestDTO("deleteuser", new DeleteUserDataDTO(tokenSessao, usernameLogado)));
        if (statusCode(resposta) == 200) {
            limparSessao();
        }
        return resposta;
    }

    private String enviar(RequestDTO requisicao) {
        log.accept("[CLIENTE] Enviado: " + gson.toJson(requisicao));
        String resposta = conexao.get().enviarRequisicao(requisicao);
        log.accept("Servidor respondeu: " + resposta);
        return resposta;
    }

    private void exigirLogin() {
        if (tokenSessao == null) {
            throw new IllegalStateException("Precisa de fazer login primeiro!");
        }
    }

    private void limparSessao() {
        this.tokenSessao = null;
        this.usernameLogado = null;
    }

    // ---------- leitura da resposta (parse do JSON, sem depender de espaços) ----------

    /** Retorna o statusCode da resposta, ou -1 se a resposta não for um JSON válido. */
    public static int statusCode(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("statusCode") || !obj.get("statusCode").isJsonPrimitive()) {
            return -1;
        }
        try {
            return obj.get("statusCode").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Retorna o campo message, ou "" se não existir. */
    public static String mensagem(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("message") || !obj.get("message").isJsonPrimitive()) {
            return "";
        }
        return obj.get("message").getAsString();
    }

    /** Retorna o objeto "data", ou null se ausente/nulo. */
    public static JsonObject dados(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("data") || !obj.get("data").isJsonObject()) {
            return null;
        }
        return obj.getAsJsonObject("data");
    }

    private static JsonObject objeto(String texto) {
        try {
            JsonElement el = JsonParser.parseString(texto);
            return el.isJsonObject() ? el.getAsJsonObject() : null;
        } catch (Exception e) {
            return null;
        }
    }
}