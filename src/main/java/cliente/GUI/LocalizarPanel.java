package cliente.GUI;


import cliente.controllers.SessionController;
import com.google.gson.JsonObject;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Busca por placa. Localizar (findvehicle) mostra o andar, a vaga e o status; Consultar (getvehicle)
 * mostra os dados do veículo. User só encontra veículos seus; admin encontra e exclui qualquer um.
 */
public class LocalizarPanel extends JPanel {

    private final JTextField placa = new JTextField(12);

    private final JLabel locAndar = new JLabel("-");
    private final JLabel locVaga = new JLabel("-");
    private final JLabel locStatus = new JLabel("-");

    private final JLabel veiPlaca = new JLabel("-");
    private final JLabel veiMarca = new JLabel("-");
    private final JLabel veiModelo = new JLabel("-");
    private final JLabel veiTipo = new JLabel("-");

    public LocalizarPanel(Contexto ctx, boolean admin) {
        super(new BorderLayout());

        JButton localizar = new JButton("Localizar veículo");
        localizar.addActionListener(e -> {
            String p = placa.getText();
            ctx.executar(() -> ctx.veiculos().localizar(p), resp -> {
                JsonObject d = SessionController.dados(resp);
                if (SessionController.sucesso(resp) && d != null) {
                    locAndar.setText(Ui.texto(d, "floor"));
                    locVaga.setText(Ui.texto(d, "spotName"));
                    locStatus.setText(Ui.texto(d, "status"));
                } else {
                    limparLocalizacao();
                }
            });
        });

        JButton consultar = new JButton("Consultar veículo");
        consultar.addActionListener(e -> {
            String p = placa.getText();
            ctx.executar(() -> ctx.veiculos().consultar(p), resp -> {
                JsonObject d = SessionController.dados(resp);
                if (SessionController.sucesso(resp) && d != null) {
                    veiPlaca.setText(Ui.texto(d, "licensePlate"));
                    veiMarca.setText(Ui.texto(d, "brand"));
                    veiModelo.setText(Ui.texto(d, "model"));
                    veiTipo.setText(Ui.texto(d, "type"));
                } else {
                    limparVeiculo();
                }
            });
        });

        JPanel busca;
        if (admin) {
            JButton excluir = new JButton("Excluir veículo");
            excluir.addActionListener(e -> {
                String p = placa.getText();
                if (!ctx.confirmar("Excluir veículo", "Excluir o veículo \"" + p + "\"?")) {
                    return;
                }
                ctx.executar(() -> ctx.veiculos().remover(p), resp -> {
                    if (SessionController.sucesso(resp)) {
                        limparLocalizacao();
                        limparVeiculo();
                    }
                });
            });
            busca = Ui.formulario("Placa do veículo",
                    new String[]{"Placa:"}, new JComponent[]{placa}, localizar, consultar, excluir);
        } else {
            busca = Ui.formulario("Placa do veículo",
                    new String[]{"Placa:"}, new JComponent[]{placa}, localizar, consultar);
        }

        JPanel localizacao = Ui.formulario("Onde está estacionado",
                new String[]{"Andar:", "Vaga:", "Status:"},
                new JComponent[]{locAndar, locVaga, locStatus});
        JPanel veiculo = Ui.formulario("Dados do veículo",
                new String[]{"Placa:", "Marca:", "Modelo:", "Tipo:"},
                new JComponent[]{veiPlaca, veiMarca, veiModelo, veiTipo});

        JPanel resultados = new JPanel(new GridLayout(1, 2, 8, 8));
        resultados.add(localizacao);
        resultados.add(veiculo);

        JPanel conteudo = new JPanel(new BorderLayout(0, 8));
        conteudo.add(busca, BorderLayout.NORTH);
        conteudo.add(resultados, BorderLayout.CENTER);
        add(Ui.embrulhar(conteudo), BorderLayout.CENTER);
    }

    private void limparLocalizacao() {
        locAndar.setText("-");
        locVaga.setText("-");
        locStatus.setText("-");
    }

    private void limparVeiculo() {
        veiPlaca.setText("-");
        veiMarca.setText("-");
        veiModelo.setText("-");
        veiTipo.setText("-");
    }
}