package cliente.GUI;

import cliente.controllers.AdminController;
import cliente.controllers.SessionController;
import cliente.controllers.VagaController;
import cliente.controllers.VehicleController;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** O que a janela principal oferece aos painéis: controllers e a execução de operações de rede. */
public interface Contexto {

    /** Roda a operação de rede fora da thread da tela e trata o resultado (na thread da tela). */
    void executar(Callable<String> operacao, Consumer<String> aoConcluir);

    /**
     * Igual a executar, mas só mexe na barra de status se a resposta for de erro. Serve para recarregar
     * uma lista depois de uma ação, sem apagar a mensagem da ação ("201 - Vaga criada com sucesso").
     */
    void atualizarLista(Callable<String> operacao, Consumer<String> aoConcluir);

    /** Mostra um erro de preenchimento (antes de enviar qualquer coisa ao servidor). */
    void erroLocal(String mensagem);

    /** Pergunta sim/não ao usuário. */
    boolean confirmar(String titulo, String pergunta);

    SessionController sessao();

    AdminController admin();

    VagaController vagas();

    VehicleController veiculos();
}