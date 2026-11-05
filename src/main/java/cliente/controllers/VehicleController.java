package cliente.controllers;

import cliente.DTOs.Dados;

/** Operações de veículos e de localização. O dono do veículo é definido pelo servidor, pelo token. */
public class VehicleController {

    private final SessionController sessao;

    public VehicleController(SessionController sessao) {
        this.sessao = sessao;
    }

    /** createvehicle (user). Modelo em branco vai como null. */
    public String criar(String brand, String model, String type, String licensePlate) {
        Dados d = new Dados().com("brand", brand);
        if (preenchido(model)) {
            d.com("model", model);
        } else {
            d.comNulo("model");
        }
        d.com("type", type).com("licensePlate", licensePlate);
        return sessao.enviarComToken("createvehicle", d);
    }

    /** getvehicles (user): só os veículos de quem está logado. */
    public String listar() {
        return sessao.enviarComToken("getvehicles", new Dados());
    }

    /** getvehicle (user: só os seus; admin: qualquer um). */
    public String consultar(String licensePlate) {
        return sessao.enviarComToken("getvehicle", new Dados().com("licensePlate", licensePlate));
    }

    /**
     * updatevehicle (user). Em branco = não alterar. limparModelo manda "model": null.
     * A placa identifica o veículo e não muda.
     */
    public String atualizar(String licensePlate, String brand, String model, boolean limparModelo, String type) {
        Dados d = new Dados().com("licensePlate", licensePlate);
        if (preenchido(brand)) d.com("brand", brand);
        if (limparModelo) {
            d.comNulo("model");
        } else if (preenchido(model)) {
            d.com("model", model);
        }
        if (preenchido(type)) d.com("type", type);
        return sessao.enviarComToken("updatevehicle", d);
    }

    /** deletevehicle (user: só os seus; admin: qualquer um). */
    public String remover(String licensePlate) {
        return sessao.enviarComToken("deletevehicle", new Dados().com("licensePlate", licensePlate));
    }

    /** [h] findvehicle (user: só os seus; admin: qualquer um): andar, vaga e status. */
    public String localizar(String licensePlate) {
        return sessao.enviarComToken("findvehicle", new Dados().com("licensePlate", licensePlate));
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }
}