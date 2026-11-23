import automatos.afd.AFD;
import automatos.Estado;
import automatos.Transicao;

public class TesteAFD {

    public static void initTeste() {

        AFD afd = new AFD();

        // Criando estados
        Estado q0 = new Estado("q0", true, false);
        Estado q1 = new Estado("q1", false, false);
        Estado q2 = new Estado("q2", false, true);

        // Adicionando estados
        afd.adicionarEstado(q0);
        afd.adicionarEstado(q1);
        afd.adicionarEstado(q2);

        // Definindo alfabeto
        afd.adicionarSimbolo("a");
        afd.adicionarSimbolo("b");

        // Definindo transicoes
        afd.adicionarTransicao(new Transicao(q0, "a", q1));
        afd.adicionarTransicao(new Transicao(q0, "b", q0));

        afd.adicionarTransicao(new Transicao(q1, "a", q1));
        afd.adicionarTransicao(new Transicao(q1, "b", q2));

        afd.adicionarTransicao(new Transicao(q2, "a", q1));
        afd.adicionarTransicao(new Transicao(q2, "b", q0));

        // Exibindo automato
        afd.exibirAutomato();

        // Testando palavras
        String palavra = "aab";

        if (afd.processar(palavra)) {
            System.out.println("\nPALAVRA ACEITA!");
        } else {
            System.out.println("\nPALAVRA REJEITADA!");
        }
    }
}