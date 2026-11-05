package cliente.GUI;

import cliente.controllers.SessionController;
import com.google.gson.JsonObject;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Admin: cadastra usuários (com perfil) e consulta, renomeia e apaga outras contas.
 * Os dados da conta consultada ficam só aqui: nada disto altera o token, o username
 * ou o perfil da sessão do admin.
 */
public class UsuariosAdminPanel extends JPanel {

    // cadastrar usuário
    private final JTextField cadNome = new JTextField(16);
    private final JTextField cadUser = new JTextField(16);
    private final JPasswordField cadSenha = new JPasswordField(16);
    private final JComboBox<String> cadPerfil = new JComboBox<>(new String[]{"user", "admin"});

    // conta administrada
    private final JTextField alvoUser = new JTextField(16);
    private final JLabel alvoNome = new JLabel("-");
    private final JLabel alvoUsername = new JLabel("-");
    private final JLabel alvoPerfil = new JLabel("-");
    private final JTextField novoNome = new JTextField(16);

    public UsuariosAdminPanel(Contexto ctx) {
        super(new BorderLayout());

        JButton cadastrar = new JButton("Cadastrar usuário");
        cadastrar.addActionListener(e -> {
            String n = cadNome.getText();
            String u = cadUser.getText();
            String s = new String(cadSenha.getPassword());
            String perfil = (String) cadPerfil.getSelectedItem();
            ctx.executar(() -> ctx.admin().cadastrar(n, u, s, perfil), resp -> {
                if (SessionController.sucesso(resp)) {
                    cadNome.setText("");
                    cadUser.setText("");
                    cadSenha.setText("");
                }
            });
        });

        JButton consultar = new JButton("Consultar");
        consultar.addActionListener(e -> {
            String alvo = alvoUser.getText();
            ctx.executar(() -> ctx.admin().consultar(alvo), resp -> {
                JsonObject d = SessionController.dados(resp);
                if (SessionController.sucesso(resp) && d != null) {
                    alvoNome.setText(Ui.texto(d, "name"));
                    alvoUsername.setText(Ui.texto(d, "username"));
                    alvoPerfil.setText(Ui.texto(d, "role"));
                } else {
                    limparConsulta();
                }
            });
        });

        JButton atualizarNome = new JButton("Atualizar nome");
        atualizarNome.addActionListener(e -> {
            String alvo = alvoUser.getText();
            String novo = novoNome.getText();
            ctx.executar(() -> ctx.admin().atualizarNome(alvo, novo), resp -> {
                if (SessionController.sucesso(resp)) {
                    novoNome.setText("");
                    alvoNome.setText(novo); // conta administrada, não a sessão do admin
                }
            });
        });

        JButton apagar = new JButton("Apagar usuário");
        apagar.addActionListener(e -> {
            String alvo = alvoUser.getText();
            if (!ctx.confirmar("Apagar usuário", "Apagar a conta \"" + alvo + "\"?")) {
                return;
            }
            ctx.executar(() -> ctx.admin().apagar(alvo), resp -> {
                if (SessionController.sucesso(resp)) limparConsulta();
            });
        });

        JPanel cadastro = Ui.formulario("Cadastrar usuário",
                new String[]{"Nome:", "Username:", "Senha:", "Perfil:"},
                new JComponent[]{cadNome, cadUser, cadSenha, cadPerfil}, cadastrar);

        JPanel conta = Ui.formulario("Conta de outro usuário",
                new String[]{"Username:", "Nome:", "Username (resp.):", "Perfil:", "Novo nome:"},
                new JComponent[]{alvoUser, alvoNome, alvoUsername, alvoPerfil, novoNome},
                consultar, atualizarNome, apagar);

        JPanel grade = new JPanel(new GridLayout(1, 2, 8, 8));
        grade.add(cadastro);
        grade.add(conta);
        add(Ui.embrulhar(grade), BorderLayout.CENTER);
    }

    private void limparConsulta() {
        alvoNome.setText("-");
        alvoUsername.setText("-");
        alvoPerfil.setText("-");
    }
}