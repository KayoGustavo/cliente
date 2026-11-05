package cliente.GUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Pequenos utilitários de layout e de leitura de campos usados por todos os painéis. */
public final class Ui {

    private Ui() {
    }

    /** Caixa com título, rótulos e campos em duas colunas, e uma linha de botões embaixo. */
    public static JPanel formulario(String titulo, String[] rotulos, JComponent[] campos, JButton... botoes) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < rotulos.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            p.add(new JLabel(rotulos[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(campos[i], c);
        }

        if (botoes.length > 0) {
            JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            for (JButton b : botoes) {
                linha.add(b);
                linha.add(Box.createHorizontalStrut(8));
            }
            c.gridx = 0;
            c.gridy = rotulos.length;
            c.gridwidth = 2;
            c.weightx = 1;
            c.fill = GridBagConstraints.NONE;
            p.add(linha, c);
        }
        return p;
    }

    /** Margem em volta do conteúdo, que fica colado no topo. */
    public static JPanel embrulhar(JPanel conteudo) {
        JPanel externo = new JPanel(new BorderLayout());
        externo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        externo.add(conteudo, BorderLayout.NORTH);
        return externo;
    }

    /** Texto do campo, ou "" se ausente ou null. */
    public static String texto(JsonObject obj, String chave) {
        if (obj == null || !obj.has(chave) || obj.get(chave).isJsonNull() || !obj.get(chave).isJsonPrimitive()) {
            return "";
        }
        return obj.get(chave).getAsString();
    }

    /** O número inteiro digitado, ou null se não for um inteiro. */
    public static Integer inteiro(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- tabelas ----------

    /** Modelo de tabela só para exibição (células não editáveis). */
    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    public static JTable tabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    public static JScrollPane rolagem(JTable tabela, int altura) {
        JScrollPane s = new JScrollPane(tabela);
        s.setPreferredSize(new Dimension(400, altura));
        return s;
    }

    /** Troca as linhas da tabela pelos itens da lista, lendo as chaves dadas (null vira vazio). */
    public static void preencher(DefaultTableModel modelo, JsonArray itens, String... chaves) {
        modelo.setRowCount(0);
        if (itens == null) return;
        for (JsonElement el : itens) {
            if (!el.isJsonObject()) continue;
            Object[] linha = new Object[chaves.length];
            for (int i = 0; i < chaves.length; i++) {
                linha[i] = texto(el.getAsJsonObject(), chaves[i]);
            }
            modelo.addRow(linha);
        }
    }

    /** Valor de uma coluna da linha selecionada, ou null se nada está selecionado. */
    public static String selecionado(JTable tabela, int coluna) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return null;
        Object v = tabela.getValueAt(linha, coluna);
        return v == null ? null : v.toString();
    }
}