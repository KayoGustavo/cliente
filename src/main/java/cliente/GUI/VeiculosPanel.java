package cliente.GUI;

import cliente.controllers.SessionController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** User: cadastra, lista, altera e exclui os próprios veículos. */
public class VeiculosPanel extends JPanel implements Atualizavel {

    private static final String MANTER = "(manter)";
    private static final String[] COLUNAS = {"Placa", "Marca", "Modelo", "Tipo"};
    private static final String[] CHAVES = {"licensePlate", "brand", "model", "type"};
    private static final int COL_PLACA = 0;

    private final Contexto ctx;
    private final DefaultTableModel modelo = Ui.modelo(COLUNAS);
    private final JTable tabela = Ui.tabela(modelo);

    // cadastrar
    private final JTextField cadPlaca = new JTextField(8);
    private final JTextField cadMarca = new JTextField(8);
    private final JTextField cadModelo = new JTextField(8);
    private final JComboBox<String> cadTipo = new JComboBox<>(new String[]{"combustao", "eletrico"});
    // alterar
    private final JTextField altPlaca = new JTextField(8);
    private final JTextField altMarca = new JTextField(8);
    private final JTextField altModelo = new JTextField(8);
    private final JCheckBox altLimparModelo = new JCheckBox("limpar modelo (null)");
    private final JComboBox<String> altTipo = new JComboBox<>(new String[]{MANTER, "combustao", "eletrico"});
    // excluir
    private final JTextField excPlaca = new JTextField(8);

    public VeiculosPanel(Contexto ctx) {
        super(new BorderLayout(0, 8));
        this.ctx = ctx;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton atualizarLista = new JButton("Atualizar lista");
        atualizarLista.addActionListener(e -> listar());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.add(atualizarLista);

        JPanel lista = new JPanel(new BorderLayout(0, 4));
        lista.setBorder(BorderFactory.createTitledBorder("Meus veículos"));
        lista.add(barra, BorderLayout.NORTH);
        lista.add(Ui.rolagem(tabela, 140), BorderLayout.CENTER);
        add(lista, BorderLayout.CENTER);

        // clicar numa linha preenche a placa dos formulários de alterar e excluir
        tabela.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            String placa = Ui.selecionado(tabela, COL_PLACA);
            if (placa == null) return;
            altPlaca.setText(placa);
            excPlaca.setText(placa);
        });

        JButton cadastrar = new JButton("Cadastrar");
        cadastrar.addActionListener(e -> cadastrar());
        JButton alterar = new JButton("Alterar");
        alterar.addActionListener(e -> alterar());
        JButton excluir = new JButton("Excluir");
        excluir.addActionListener(e -> excluir());

        JPanel grade = new JPanel(new GridLayout(1, 3, 8, 8));
        grade.add(Ui.formulario("Cadastrar veículo",
                new String[]{"Placa:", "Marca:", "Modelo:", "Tipo:"},
                new JComponent[]{cadPlaca, cadMarca, cadModelo, cadTipo}, cadastrar));
        grade.add(Ui.formulario("Alterar veículo",
                new String[]{"Placa:", "Marca:", "Modelo:", "", "Tipo:"},
                new JComponent[]{altPlaca, altMarca, altModelo, altLimparModelo, altTipo}, alterar));
        grade.add(Ui.formulario("Excluir veículo",
                new String[]{"Placa:"}, new JComponent[]{excPlaca}, excluir));
        add(grade, BorderLayout.SOUTH);
    }

    @Override
    public void aoExibir() {
        listar();
    }

    /** Botão "Atualizar lista" e abertura da aba: a barra de status mostra a resposta. */
    private void listar() {
        ctx.executar(ctx.veiculos()::listar, this::preencherTabela);
    }

    /** Depois de uma ação: recarrega a lista sem apagar a mensagem da ação na barra de status. */
    private void recarregar() {
        ctx.atualizarLista(ctx.veiculos()::listar, this::preencherTabela);
    }

    private void preencherTabela(String resp) {
        if (SessionController.sucesso(resp)) {
            Ui.preencher(modelo, SessionController.lista(resp), CHAVES);
        }
    }

    private void cadastrar() {
        String marca = cadMarca.getText();
        String modelo = cadModelo.getText();
        String tipo = (String) cadTipo.getSelectedItem();
        String placa = cadPlaca.getText();
        ctx.executar(() -> ctx.veiculos().criar(marca, modelo, tipo, placa), resp -> {
            if (SessionController.sucesso(resp)) {
                cadPlaca.setText("");
                cadMarca.setText("");
                cadModelo.setText("");
                recarregar();
            }
        });
    }

    private void alterar() {
        String placa = altPlaca.getText();
        String marca = altMarca.getText();
        String modelo = altModelo.getText();
        boolean limpar = altLimparModelo.isSelected();
        String tipo = MANTER.equals(altTipo.getSelectedItem()) ? null : (String) altTipo.getSelectedItem();
        ctx.executar(() -> ctx.veiculos().atualizar(placa, marca, modelo, limpar, tipo), resp -> {
            if (SessionController.sucesso(resp)) {
                altMarca.setText("");
                altModelo.setText("");
                altLimparModelo.setSelected(false);
                altTipo.setSelectedIndex(0);
                recarregar();
            }
        });
    }

    private void excluir() {
        String placa = excPlaca.getText();
        if (!ctx.confirmar("Excluir veículo", "Excluir o veículo \"" + placa + "\"?")) {
            return;
        }
        ctx.executar(() -> ctx.veiculos().remover(placa), resp -> {
            if (SessionController.sucesso(resp)) {
                excPlaca.setText("");
                recarregar();
            }
        });
    }
}