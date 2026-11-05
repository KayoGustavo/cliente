package cliente.controllers;

import cliente.DTOs.CadastroDataDTO;
import cliente.DTOs.DeleteUserDataDTO;
import cliente.DTOs.Dados;
import cliente.DTOs.GetUserDataDTO;
import cliente.DTOs.LoginDataDTO;
import cliente.DTOs.RequestDTO;
import cliente.DTOs.TokenDataDTO;
import cliente.DTOs.UpdatePasswordDataDTO;
import cliente.DTOs.UpdateUsernameDataDTO;
import cliente.managers.NetworkManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class SessionController {

    public static final String SESSAO_INVALIDA = "Sessão expirada ou encerrada.";

    private final Supplier<NetworkManager> conexao;
    private final Consumer<String> log;
    private final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    private volatile String tokenSessao;
    private volatile String usernameLogado;
    private volatile String nomeLogado;
    private volatile String roleLogado;

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

    public String getNomeLogado() {
        return nomeLogado;
    }

    /** "admin", "user", ou null enquanto o perfil ainda não foi consultado. */
    public String getRole() {
        return roleLogado;
    }

    public boolean ehAdmin() {
        return "admin".equals(roleLogado);
    }

    // [A] Cadastro (usuário comum se cadastrando: sem role e sem token)
    public String cadastrar(String name, String username, String password) {
        return enviar("register", new CadastroDataDTO(name, username, password));
    }

    // [C] Login: guarda o token se o servidor responder 200 e já consulta o perfil (role) da conta
    public String login(String username, String password) {
        String resposta = enviar("login", new LoginDataDTO(username, password));
        if (statusCode(resposta) == 200) {
            JsonObject dados = dados(resposta);
            if (dados != null && dados.has("token") && !dados.get("token").isJsonNull()) {
                this.tokenSessao = dados.get("token").getAsString();
                this.usernameLogado = username;
                this.nomeLogado = null;
                this.roleLogado = null;
                carregarPerfil();
            }
        }
        return resposta;
    }

    /** getuser da própria conta: descobre o nome e o perfil (admin ou user). */
    private void carregarPerfil() {
        guardarPerfil(enviar("getuser", new GetUserDataDTO(tokenSessao, usernameLogado)));
    }

    private void guardarPerfil(String resposta) {
        if (statusCode(resposta) == 200) {
            JsonObject d = dados(resposta);
            if (d != null) {
                this.nomeLogado = texto(d, "name");
                this.roleLogado = texto(d, "role");
            }
        }
    }

    // [B] Consulta dos próprios dados
    public String consultar() {
        exigirLogin();
        String resposta = enviar("getuser", new GetUserDataDTO(tokenSessao, usernameLogado));
        guardarPerfil(resposta);
        return resposta;
    }

    // [E] Atualizar nome
    public String atualizarNome(String novoNome) {
        exigirLogin();
        String resposta = enviar("updateusername", new UpdateUsernameDataDTO(tokenSessao, usernameLogado, novoNome));
        if (statusCode(resposta) == 200) {
            this.nomeLogado = novoNome;
        }
        return resposta;
    }

    // [E] Atualizar senha
    public String atualizarSenha(String senhaAntiga, String novaSenha) {
        exigirLogin();
        return enviar("updateuserpassword",
                new UpdatePasswordDataDTO(tokenSessao, usernameLogado, senhaAntiga, novaSenha));
    }

    // [D] Logout: a sessão local é limpa mesmo que o servidor responda erro
    public String logout() {
        exigirLogin();
        String resposta = enviar("logout", new TokenDataDTO(tokenSessao));
        limparSessao();
        return resposta;
    }

    // [F] Apagar a própria conta: limpa a sessão se o servidor responder 200
    public String apagarConta() {
        exigirLogin();
        String resposta = enviar("deleteuser", new DeleteUserDataDTO(tokenSessao, usernameLogado));
        if (statusCode(resposta) == 200) {
            limparSessao();
        }
        return resposta;
    }

    /**
     * Envia uma operação que exige login: coloca o token da sessão no início do "data" e
     * junta os campos informados. Usado pelos controllers de admin, vagas e veículos.
     */
    public String enviarComToken(String method, Dados dados) {
        exigirLogin();
        JsonObject corpo = new JsonObject();
        corpo.addProperty("token", tokenSessao);
        for (Map.Entry<String, JsonElement> campo : dados.json().entrySet()) {
            corpo.add(campo.getKey(), campo.getValue());
        }
        return enviar(method, corpo);
    }

    private String enviar(String method, Object data) {
        RequestDTO requisicao = new RequestDTO(method, data);
        log.accept("[CLIENTE] Enviado: " + gson.toJson(requisicao));
        boolean comSessao = tokenSessao != null;

        String resposta = conexao.get().enviarRequisicao(requisicao);
        log.accept("Servidor respondeu: " + resposta);

        // Só a resposta de sessão inválida encerra a sessão local. Um 401 de "sem permissão"
        // (ex.: admin em operação de user) mantém a sessão válida.
        if (comSessao && statusCode(resposta) == 401 && SESSAO_INVALIDA.equals(mensagem(resposta))) {
            limparSessao();
        }
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
        this.nomeLogado = null;
        this.roleLogado = null;
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

    /** Resposta com statusCode 2XX. */
    public static boolean sucesso(String resposta) {
        int c = statusCode(resposta);
        return c >= 200 && c < 300;
    }

    /** Retorna o campo message, ou "" se não existir. */
    public static String mensagem(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("message") || !obj.get("message").isJsonPrimitive()) {
            return "";
        }
        return obj.get("message").getAsString();
    }

    /** Retorna o objeto "data", ou null se ausente/nulo/de outro tipo. */
    public static JsonObject dados(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("data") || !obj.get("data").isJsonObject()) {
            return null;
        }
        return obj.getAsJsonObject("data");
    }

    /** Retorna a lista em "data", ou null se ausente/nula/de outro tipo. */
    public static JsonArray lista(String resposta) {
        JsonObject obj = objeto(resposta);
        if (obj == null || !obj.has("data") || !obj.get("data").isJsonArray()) {
            return null;
        }
        return obj.getAsJsonArray("data");
    }

    private static String texto(JsonObject obj, String chave) {
        return (obj.has(chave) && obj.get(chave).isJsonPrimitive()) ? obj.get(chave).getAsString() : null;
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