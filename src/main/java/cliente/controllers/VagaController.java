package cliente.controllers;

import cliente.DTOs.Dados;

/** Operações de vagas. Campos opcionais em branco não são enviados (o servidor preserva o valor). */
public class VagaController {

    private final SessionController sessao;

    public VagaController(SessionController sessao) {
        this.sessao = sessao;
    }

    /** [d] createvaga (admin). */
    public String criar(String spotName, int floor, String type) {
        return sessao.enviarComToken("createvaga", new Dados()
                .com("spotName", spotName)
                .com("floor", floor)
                .com("type", type));
    }

    /** [e] getvagas (user e admin): todas as vagas, sem filtro. */
    public String listar() {
        return sessao.enviarComToken("getvagas", new Dados());
    }

    /** [f] updatevaga (admin): nome, andar e/ou status. Null ou vazio = não alterar. */
    public String atualizarComoAdmin(String spotName, String novoNome, Integer novoAndar, String novoStatus) {
        Dados d = new Dados().com("spotName", spotName);
        if (preenchido(novoNome)) d.com("newSpotName", novoNome);
        if (novoAndar != null) d.com("floor", novoAndar);
        if (preenchido(novoStatus)) d.com("status", novoStatus);
        return sessao.enviarComToken("updatevaga", d);
    }

    /** updatevaga (user): estaciona um veículo seu na vaga. */
    public String estacionar(String spotName, String licensePlate) {
        return sessao.enviarComToken("updatevaga", new Dados()
                .com("spotName", spotName)
                .com("status", "ocupada")
                .com("licensePlate", licensePlate));
    }

    /** updatevaga (user): libera a vaga que ele ocupou (status disponivel e licensePlate null). */
    public String liberar(String spotName) {
        return sessao.enviarComToken("updatevaga", new Dados()
                .com("spotName", spotName)
                .com("status", "disponivel")
                .comNulo("licensePlate"));
    }

    /** [g] deletevaga (admin). */
    public String remover(String spotName) {
        return sessao.enviarComToken("deletevaga", new Dados().com("spotName", spotName));
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }
}