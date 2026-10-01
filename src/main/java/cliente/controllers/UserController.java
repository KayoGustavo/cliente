package cliente.controllers;

import cliente.DTOs.*;
import cliente.managers.NetworkManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class UserController {
    private NetworkManager networkManager;
    private String tokenSessao;
    private String usernameLogado;
    private Gson gson;

    public UserController(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.tokenSessao = null;
        this.usernameLogado = null;
        this.gson = new Gson();
    }

    // [A] Cadastro de Utilizador
    public void cadastrarUsuario(Scanner scanner) {
        System.out.println("\n--- CADASTRO DE UTILIZADOR ---");
        System.out.print("Nome: ");
        String name = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Senha: ");
        String password = scanner.nextLine();

        CadastroDataDTO dados = new CadastroDataDTO(name, username, password);
        RequestDTO requisicao = new RequestDTO("register", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);
    }

    // [C] Login
    public void fazerLogin(Scanner scanner) {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Senha: ");
        String password = scanner.nextLine();

        LoginDataDTO dados = new LoginDataDTO(username, password);
        RequestDTO requisicao = new RequestDTO("login", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);

        if (resposta != null && resposta.contains("\"statusCode\":200")) {
            try {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                if (jsonResposta.has("data") && !jsonResposta.get("data").isJsonNull()) {
                    JsonObject dataObj = jsonResposta.getAsJsonObject("data");
                    if (dataObj.has("token")) {
                        this.tokenSessao = dataObj.get("token").getAsString();
                        this.usernameLogado = username;
                        System.out.println("Login efetuado com sucesso! Token guardado na sessão.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar o token da resposta: " + e.getMessage());
            }
        }
    }

    // [B] Consulta de Dados do Utilizador (Get User)
    public void lerMeusDados(Scanner scanner) {
        System.out.println("\n--- CONSULTAR MEUS DADOS ---");
        if (tokenSessao == null) {
            System.out.println("Erro: Precisa de fazer login primeiro!");
            return;
        }

        GetUserDataDTO dados = new GetUserDataDTO(this.tokenSessao, this.usernameLogado);
        RequestDTO requisicao = new RequestDTO("getuser", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);
    }

    // [E] Atualização de Nome de Utilizador (updateUsername)
    public void atualizarNome(Scanner scanner) {
        System.out.println("\n--- ATUALIZAR NOME DE UTILIZADOR ---");
        if (tokenSessao == null) {
            System.out.println("Erro: Precisa de fazer login primeiro!");
            return;
        }

        System.out.print("Novo Nome: ");
        String novoNome = scanner.nextLine();

        UpdateUsernameDataDTO dados = new UpdateUsernameDataDTO(this.tokenSessao, this.usernameLogado, novoNome);
        RequestDTO requisicao = new RequestDTO("updateusername", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);
    }

    // Atualização de Senha (updateUserPassword)
    public void atualizarSenha(Scanner scanner) {
        System.out.println("\n--- ATUALIZAR SENHA ---");
        if (tokenSessao == null) {
            System.out.println("Erro: Precisa de fazer login primeiro!");
            return;
        }

        System.out.print("Senha Antiga: ");
        String senhaAntiga = scanner.nextLine();
        System.out.print("Nova Senha: ");
        String novaSenha = scanner.nextLine();

        UpdatePasswordDataDTO dados = new UpdatePasswordDataDTO(this.tokenSessao, this.usernameLogado, senhaAntiga, novaSenha);
        RequestDTO requisicao = new RequestDTO("updateuserpassword", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);
    }

    // [D] Logout
    public void fazerLogout() {
        System.out.println("\n--- LOGOUT ---");
        if (tokenSessao == null) {
            System.out.println("Aviso: Não existe nenhuma sessão ativa.");
            return;
        }

        TokenDataDTO dados = new TokenDataDTO(this.tokenSessao);
        RequestDTO requisicao = new RequestDTO("logout", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);

        this.tokenSessao = null;
        this.usernameLogado = null;
        System.out.println("Sessão limpa no cliente.");
    }

    // [F] Exclusão de Cadastro (Delete User)
    public void apagarConta(Scanner scanner) {
        System.out.println("\n--- APAGAR CONTA ---");
        if (tokenSessao == null) {
            System.out.println("Erro: Precisa de fazer login primeiro!");
            return;
        }

        DeleteUserDataDTO dados = new DeleteUserDataDTO(this.tokenSessao, this.usernameLogado);
        RequestDTO requisicao = new RequestDTO("deleteuser", dados);

        String resposta = networkManager.enviarRequisicao(requisicao);
        System.out.println("Servidor respondeu: " + resposta);

        if (resposta != null && resposta.contains("\"statusCode\":200")) {
            this.tokenSessao = null;
            this.usernameLogado = null;
            System.out.println("Conta eliminada e sessão encerrada com sucesso.");
        }
    }
}