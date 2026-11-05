package cliente.GUI;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/** Login. Ao entrar, a janela principal troca as abas conforme o perfil (admin ou user). */
public class LoginPanel extends JPanel {

    private final JTextField username = new JTextField(20);
    private final JPasswordField senha = new JPasswordField(20);

    public LoginPanel(Contexto ctx) {
        super(new BorderLayout());

        JButton entrar = new JButton("Entrar");
        Runnable acao = () -> {
            String u = username.getText();
            String s = new String(senha.getPassword());
            ctx.executar(() -> ctx.sessao().login(u, s), resp -> senha.setText(""));
        };
        entrar.addActionListener(e -> acao.run());
        senha.addActionListener(e -> acao.run()); // Enter no campo de senha

        JPanel form = Ui.formulario("Acesso",
                new String[]{"Username:", "Senha:"},
                new JComponent[]{username, senha}, entrar);
        add(Ui.embrulhar(form), BorderLayout.CENTER);
    }
}