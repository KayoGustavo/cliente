package cliente.controllers;

import cliente.DTOs.Dados;

/** Operações do administrador sobre contas de outros usuários. O token vem da sessão. */
public class AdminController {

    private final SessionController sessao;

    public AdminController(SessionController sessao) {
        this.sessao = sessao;
    }

    /** register com role e token: só o servidor aceita se o token for de um admin. */
    public String cadastrar(String name, String username, String password, String role) {
        return sessao.enviarComToken("register", new Dados()
                .com("name", name)
                .com("username", username)
                .com("password", password)
                .com("role", role));
    }

    /** [a] getuser de outro usuário. */
    public String consultar(String username) {
        return sessao.enviarComToken("getuser", new Dados().com("username", username));
    }

    /** [b] updateusername de outro usuário (só o name muda, o username é o identificador). */
    public String atualizarNome(String username, String name) {
        return sessao.enviarComToken("updateusername", new Dados()
                .com("username", username)
                .com("name", name));
    }

    /** [c] deleteuser de outro usuário. */
    public String apagar(String username) {
        return sessao.enviarComToken("deleteuser", new Dados().com("username", username));
    }
}