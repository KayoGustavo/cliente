package cliente;

import cliente.controllers.UserController;
import cliente.managers.NetworkManager;

import java.util.Scanner;

public class ClientApplication {
    public static void main(String[] args) {
        // Configura o IP e a porta do servidor (ajuste se necessário, ex: "localhost", 8080)
        Scanner scanner = new Scanner(System.in);
        System.out.print("IP do servidor: ");
        String host = scanner.nextLine();
        System.out.print("Porta: ");
        int port = Integer.parseInt(scanner.nextLine());

        NetworkManager networkManager = new NetworkManager(host, port);
        UserController userController = new UserController(networkManager);


        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n========================================");
            System.out.println("   CLIENTE DISTRIBUÍDO - MENU PRINCIPAL");
            System.out.println("========================================");
            System.out.println("1. Cadastrar Usuário (Register) [A]");
            System.out.println("2. Fazer Login [C]");
            System.out.println("3. Consultar Meus Dados (Get User) [B]");
            System.out.println("4. Atualizar Nome de Usuário (Update Username) [E]");
            System.out.println("5. Atualizar Senha (Update Password)");
            System.out.println("6. Logout [D]");
            System.out.println("7. Apagar Conta (Delete User) [F]");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1:
                        userController.cadastrarUsuario(scanner);
                        break;
                    case 2:
                        userController.fazerLogin(scanner);
                        break;
                    case 3:
                        userController.lerMeusDados(scanner);
                        break;
                    case 4:
                        userController.atualizarNome(scanner);
                        break;
                    case 5:
                        userController.atualizarSenha(scanner);
                        break;
                    case 6:
                        userController.fazerLogout();
                        break;
                    case 7:
                        userController.apagarConta(scanner);
                        break;
                    case 0:
                        System.out.println("A encerrar aplicação...");
                        break;
                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            }
        }
        scanner.close();
    }
}
