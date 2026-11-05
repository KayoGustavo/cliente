package cliente.DTOs;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Monta o "data" de uma requisição campo a campo, na ordem em que foram adicionados.
 * Diferente dos DTOs fixos, permite deixar um campo de fora (preserva o valor no servidor)
 * ou mandá-lo explicitamente como null (ex.: "licensePlate": null desvincula a vaga).
 */
public class Dados {

    private final JsonObject json = new JsonObject();

    public Dados com(String chave, String valor) {
        json.addProperty(chave, valor);
        return this;
    }

    public Dados com(String chave, int valor) {
        json.addProperty(chave, valor);
        return this;
    }

    /** Campo presente com o valor null. */
    public Dados comNulo(String chave) {
        json.add(chave, JsonNull.INSTANCE);
        return this;
    }

    public JsonObject json() {
        return json;
    }
}