package cliente.GUI;


import cliente.controllers.SessionController;
import com.google.gson.JsonObject;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** A própria conta (user ou admin): consultar, atualizar nome e senha, apagar. */
public class ContaPanel extends JPanel {

    private final JLabel contaNome = new JLabel("-");
    private final JLabel contaUser = new JLabel("-");
    private final JLabel contaPerfil = new JLabel("-");
    private final JTextField novoNome = new JTextField(20);
    private final JPasswordField senhaAntiga = new JPasswordField(20);
    private final JPasswordField senhaNova = new JPasswordField(20);

    public ContaPanel(Contexto ctx) {
        super(new BorderLayout());

        JButton consultar = new JButton("Consultar meus dados");
        consultar.addActionListener(e -> ctx.executar(ctx.sessao()::consultar, resp -> {
            JsonObject d = SessionController.dados(resp);
            if (SessionController.sucesso(resp) && d != null) {
                contaNome.setText(Ui.texto(d, "name"));
                contaUser.setText(Ui.texto(d, "username"));
                contaPerfil.setText(Ui.texto(d, "role"));
            }
        }));

        JButton atualizarNome = new JButton("Atualizar nome");
        atualizarNome.addActionListener(e -> {
            String novo = novoNome.getText();
            ctx.executar(() -> ctx.sessao().atualizarNome(novo), resp -> {
                if (SessionController.sucesso(resp)) novoNome.setText("");
            });
        });

        JButton atualizarSenha = new JButton("Atualizar senha");
        atualizarSenha.addActionListener(e -> {
            String antiga = new String(senhaAntiga.getPassword());
            String nova = new String(senhaNova.getPassword());
            ctx.executar(() -> ctx.sessao().atualizarSenha(antiga, nova), resp -> {
                if (SessionController.sucesso(resp)) {
                    senhaAntiga.setText("");
                    senhaNova.setText("");
                }
            });
        });

        JButton apagar = new JButton("Apagar minha conta");
        apagar.addActionListener(e -> {
            if (!ctx.confirmar("Apagar conta",
                    "Tem certeza que deseja apagar a conta \"" + ctx.sessao().getUsernameLogado() + "\"?")) {
                return;
            }
            ctx.executar(ctx.sessao()::apagarConta, resp -> {
            });
        });

        JPanel grade = new JPanel(new GridLayout(2, 2, 8, 8));
        grade.add(Ui.formulario("Meus dados",
                new String[]{"Nome:", "Username:", "Perfil:"},
                new JComponent[]{contaNome, contaUser, contaPerfil}, consultar));
        grade.add(Ui.formulario("Atualizar nome",
                new String[]{"Novo nome:"}, new JComponent[]{novoNome}, atualizarNome));
        grade.add(Ui.formulario("Atualizar senha",
                new String[]{"Senha antiga:", "Nova senha:"},
                new JComponent[]{senhaAntiga, senhaNova}, atualizarSenha));
        grade.add(Ui.formulario("Excluir conta", new String[]{}, new JComponent[]{}, apagar));
        add(Ui.embrulhar(grade), BorderLayout.CENTER);
    }
}