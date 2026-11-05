package cliente.GUI;


import cliente.controllers.SessionController;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/** Antes do login: o usuário comum se cadastra (register sem role e sem token). */
public class CadastroPanel extends JPanel {

    private final JTextField nome = new JTextField(20);
    private final JTextField username = new JTextField(20);
    private final JPasswordField senha = new JPasswordField(20);

    public CadastroPanel(Contexto ctx) {
        super(new BorderLayout());

        JButton cadastrar = new JButton("Cadastrar");
        cadastrar.addActionListener(e -> {
            String n = nome.getText();
            String u = username.getText();
            String s = new String(senha.getPassword());
            ctx.executar(() -> ctx.sessao().cadastrar(n, u, s), resp -> {
                if (SessionController.sucesso(resp)) {
                    nome.setText("");
                    username.setText("");
                    senha.setText("");
                }
            });
        });

        JPanel form = Ui.formulario("Novo usuário",
                new String[]{"Nome:", "Username:", "Senha:"},
                new JComponent[]{nome, username, senha}, cadastrar);
        add(Ui.embrulhar(form), BorderLayout.CENTER);
    }
}