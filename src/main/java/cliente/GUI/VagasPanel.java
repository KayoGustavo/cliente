package cliente.GUI;


import cliente.controllers.SessionController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Vagas. Admin: criar, atualizar (nome, andar, status) e excluir.
 * User: estacionar um veículo seu e liberar a vaga que ocupou. Os dois veem a lista completa.
 */
public class VagasPanel extends JPanel implements Atualizavel {

    private static final String MANTER = "(manter)";
    private static final String[] COLUNAS = {"Andar", "Vaga", "Tipo", "Status", "Placa"};
    private static final String[] CHAVES = {"floor", "spotName", "type", "status", "licensePlate"};
    private static final int COL_VAGA = 1;

    private final Contexto ctx;
    private final DefaultTableModel modelo = Ui.modelo(COLUNAS);
    private final JTable tabela = Ui.tabela(modelo);

    // admin: criar
    private final JTextField criarNome = new JTextField(8);
    private final JTextField criarAndar = new JTextField(8);
    private final JComboBox<String> criarTipo = new JComboBox<>(new String[]{"eletrico", "combustao"});
    // admin: atualizar
    private final JTextField atualizarVaga = new JTextField(8);
    private final JTextField atualizarNome = new JTextField(8);
    private final JTextField atualizarAndar = new JTextField(8);
    private final JComboBox<String> atualizarStatus =
            new JComboBox<>(new String[]{MANTER, "disponivel", "ocupada", "fora_de_servico"});
    // admin: excluir
    private final JTextField excluirVaga = new JTextField(8);
    // user: estacionar e liberar
    private final JTextField estacionarVaga = new JTextField(8);
    private final JTextField estacionarPlaca = new JTextField(8);
    private final JTextField liberarVaga = new JTextField(8);

    public VagasPanel(Contexto ctx, boolean admin) {
        super(new BorderLayout(0, 8));
        this.ctx = ctx;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // lista de vagas
        JButton atualizarLista = new JButton("Atualizar lista");
        atualizarLista.addActionListener(e -> listar());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.add(atualizarLista);

        JPanel lista = new JPanel(new BorderLayout(0, 4));
        lista.setBorder(BorderFactory.createTitledBorder("Vagas do estacionamento"));
        lista.add(barra, BorderLayout.NORTH);
        lista.add(Ui.rolagem(tabela, 140), BorderLayout.CENTER);
        add(lista, BorderLayout.CENTER);

        // clicar numa linha preenche o nome da vaga nos formulários
        tabela.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            String vaga = Ui.selecionado(tabela, COL_VAGA);
            if (vaga == null) return;
            atualizarVaga.setText(vaga);
            excluirVaga.setText(vaga);
            estacionarVaga.setText(vaga);
            liberarVaga.setText(vaga);
        });

        add(admin ? formulariosAdmin() : formulariosUser(), BorderLayout.SOUTH);
    }

    @Override
    public void aoExibir() {
        listar();
    }

    // ================= formulários =================

    private JPanel formulariosAdmin() {
        JButton criar = new JButton("Criar vaga");
        criar.addActionListener(e -> criar());
        JButton atualizar = new JButton("Atualizar vaga");
        atualizar.addActionListener(e -> atualizar());
        JButton excluir = new JButton("Excluir vaga");
        excluir.addActionListener(e -> excluir());

        JPanel grade = new JPanel(new GridLayout(1, 3, 8, 8));
        grade.add(Ui.formulario("Criar vaga",
                new String[]{"Nome:", "Andar:", "Tipo:"},
                new JComponent[]{criarNome, criarAndar, criarTipo}, criar));
        grade.add(Ui.formulario("Atualizar vaga",
                new String[]{"Vaga:", "Novo nome:", "Novo andar:", "Status:"},
                new JComponent[]{atualizarVaga, atualizarNome, atualizarAndar, atualizarStatus}, atualizar));
        grade.add(Ui.formulario("Excluir vaga",
                new String[]{"Vaga:"}, new JComponent[]{excluirVaga}, excluir));
        return grade;
    }

    private JPanel formulariosUser() {
        JButton estacionar = new JButton("Estacionar");
        estacionar.addActionListener(e -> estacionar());
        JButton liberar = new JButton("Liberar vaga");
        liberar.addActionListener(e -> liberar());

        JPanel grade = new JPanel(new GridLayout(1, 2, 8, 8));
        grade.add(Ui.formulario("Estacionar um veículo meu",
                new String[]{"Vaga:", "Placa:"},
                new JComponent[]{estacionarVaga, estacionarPlaca}, estacionar));
        grade.add(Ui.formulario("Liberar a vaga que ocupei",
                new String[]{"Vaga:"}, new JComponent[]{liberarVaga}, liberar));
        return grade;
    }

    // ================= ações =================

    /** Botão "Atualizar lista" e abertura da aba: a barra de status mostra a resposta. */
    private void listar() {
        ctx.executar(ctx.vagas()::listar, this::preencherTabela);
    }

    /** Depois de uma ação: recarrega a lista sem apagar a mensagem da ação na barra de status. */
    private void recarregar() {
        ctx.atualizarLista(ctx.vagas()::listar, this::preencherTabela);
    }

    private void preencherTabela(String resp) {
        if (SessionController.sucesso(resp)) {
            Ui.preencher(modelo, SessionController.lista(resp), CHAVES);
        }
    }

    private void criar() {
        Integer andar = Ui.inteiro(criarAndar.getText());
        if (andar == null) {
            ctx.erroLocal("O andar deve ser um número inteiro.");
            return;
        }
        String nome = criarNome.getText();
        String tipo = (String) criarTipo.getSelectedItem();
        ctx.executar(() -> ctx.vagas().criar(nome, andar, tipo), resp -> {
            if (SessionController.sucesso(resp)) {
                criarNome.setText("");
                criarAndar.setText("");
                recarregar();
            }
        });
    }

    private void atualizar() {
        String vaga = atualizarVaga.getText();
        String novoNome = atualizarNome.getText();
        Integer novoAndar = null;
        if (!atualizarAndar.getText().isBlank()) {
            novoAndar = Ui.inteiro(atualizarAndar.getText());
            if (novoAndar == null) {
                ctx.erroLocal("O andar deve ser um número inteiro.");
                return;
            }
        }
        String status = MANTER.equals(atualizarStatus.getSelectedItem())
                ? null : (String) atualizarStatus.getSelectedItem();
        Integer andarFinal = novoAndar;
        ctx.executar(() -> ctx.vagas().atualizarComoAdmin(vaga, novoNome, andarFinal, status), resp -> {
            if (SessionController.sucesso(resp)) {
                atualizarNome.setText("");
                atualizarAndar.setText("");
                atualizarStatus.setSelectedIndex(0);
                recarregar();
            }
        });
    }

    private void excluir() {
        String vaga = excluirVaga.getText();
        if (!ctx.confirmar("Excluir vaga", "Excluir a vaga \"" + vaga + "\"?")) {
            return;
        }
        ctx.executar(() -> ctx.vagas().remover(vaga), resp -> {
            if (SessionController.sucesso(resp)) {
                excluirVaga.setText("");
                recarregar();
            }
        });
    }

    private void estacionar() {
        String vaga = estacionarVaga.getText();
        String placa = estacionarPlaca.getText();
        ctx.executar(() -> ctx.vagas().estacionar(vaga, placa), resp -> {
            if (SessionController.sucesso(resp)) {
                estacionarPlaca.setText("");
                recarregar();
            }
        });
    }

    private void liberar() {
        String vaga = liberarVaga.getText();
        ctx.executar(() -> ctx.vagas().liberar(vaga), resp -> {
            if (SessionController.sucesso(resp)) recarregar();
        });
    }
}