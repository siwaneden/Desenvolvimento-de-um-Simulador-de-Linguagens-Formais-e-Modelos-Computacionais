import ap.AutomatoPilha;
import ap.TransicaoPilha;
import modelo.Estado;

public class TesteAP {

    public static void main(String[] args) {

        AutomatoPilha ap = new AutomatoPilha();

        // Criando estados
        Estado q0 = new Estado("q0", true, false);
        Estado q1 = new Estado("q1", false, false);
        Estado q2 = new Estado("q2", false, true);

        // Adicionando estados
        ap.adicionarEstado(q0);
        ap.adicionarEstado(q1);
        ap.adicionarEstado(q2);

        // Definindo alfabeto
        ap.adicionarSimbolo("a");
        ap.adicionarSimbolo("b");

        // Simbolo inicial da pilha
        ap.setSimboloInicialPilha("Z");

        // Transicoes para empilhar os A
        ap.adicionarTransicaoPilha(
            new TransicaoPilha(q0, "a", "Z", "AZ", q0)
        );

        ap.adicionarTransicaoPilha(
            new TransicaoPilha(q0, "a", "A", "AA", q0)
        );

        // Transicoes para desempilhar os A
        ap.adicionarTransicaoPilha(
            new TransicaoPilha(q0, "b", "A", "ε", q1)
        );

        ap.adicionarTransicaoPilha(
            new TransicaoPilha(q1, "b", "A", "ε", q1)
        );

        // Quando restar apenas o simbolo Z,
        // remove Z e vai para o estado final
        ap.adicionarTransicaoPilha(
            new TransicaoPilha(q1, "ε", "Z", "ε", q2)
        );

        // Exibindo automato
        ap.exibirAutomato();

        // Testando palavras
        String[] palavras = {
            "ab",
            "aabb",
            "aaabbb",
            "aab",
            "abab",
            "abb"
        };

        System.out.println("\n===== RESULTADOS =====");

        for (String palavra : palavras) {

            boolean resultado = ap.processar(palavra);

            System.out.println(
                palavra + " -> "
                + (resultado ? "ACEITA" : "REJEITADA")
            );
        }
    }
}