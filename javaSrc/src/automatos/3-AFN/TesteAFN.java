import automatos.afn.AFN;
import automatos.Estado;
import automatos.Transicao;

public class TesteAFN {

    public static void main(String[] args) {

        AFN afn = new AFN();

        // Criando estados
        Estado q0 = new Estado("q0", true, false);
        Estado q1 = new Estado("q1", false, false);
        Estado q2 = new Estado("q2", false, true);

        // Adicionando estados
        afn.adicionarEstado(q0);
        afn.adicionarEstado(q1);
        afn.adicionarEstado(q2);

        // Alfabeto
        afn.adicionarSimbolo("a");
        afn.adicionarSimbolo("b");

        // Transicoes
        afn.adicionarTransicao(new Transicao(q0, "a", q0));
        afn.adicionarTransicao(new Transicao(q0, "b", q0));

        afn.adicionarTransicao(new Transicao(q0, "a", q1));

        afn.adicionarTransicao(new Transicao(q1, "b", q2));

        afn.adicionarTransicao(new Transicao(q2, "a", q2));
        afn.adicionarTransicao(new Transicao(q2, "b", q2));

        // Exibindo automato
        afn.exibirAutomato();

        // Testando palavras
        String[] palavras = {
            "ab",
            "aab",
            "baba",
            "aaa",
            "bbb"
        };

        for (String palavra : palavras) {

            System.out.println(
                palavra + " -> " +
                (afn.processar(palavra) ? "ACEITA" : "REJEITADA")
            );
        }
    }
}