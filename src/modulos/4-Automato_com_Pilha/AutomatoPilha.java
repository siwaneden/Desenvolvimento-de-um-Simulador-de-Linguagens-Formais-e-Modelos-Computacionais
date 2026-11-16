package ap;

import modelo.Automato;
import modelo.Estado;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AutomatoPilha extends Automato {

    private static final String EPSILON = "ε";

    private List<TransicaoPilha> transicoesPilha;
    private String simboloInicialPilha;

    public AutomatoPilha() {
        super();
        transicoesPilha = new ArrayList<>();
    }

    // Define o simbolo inicial da pilha
    public void setSimboloInicialPilha(String simbolo) {
        this.simboloInicialPilha = simbolo;
    }

    // Adiciona uma transicao
    public void adicionarTransicaoPilha(
            TransicaoPilha transicao) {

        transicoesPilha.add(transicao);
    }

    // Exibe as transicoes do AP
    public void exibirTransicoesPilha() {

        System.out.println("\n===== TRANSICOES DO AP =====");

        for (TransicaoPilha transicao : transicoesPilha) {
            System.out.println(transicao);
        }
    }

    // Verifica se a palavra pertence ao alfabeto
    private boolean validarPalavra(String palavra) {

        for (int i = 0; i < palavra.length(); i++) {

            String simbolo =
                    String.valueOf(palavra.charAt(i));

            if (!alfabeto.contains(simbolo)) {
                return false;
            }
        }

        return true;
    }

    // Processa a palavra
    @Override
    public boolean processar(String palavra) {

        Estado atual = getEstadoInicial();

        if (atual == null) {
            System.out.println("Estado inicial nao definido!");
            return false;
        }

        if (getEstadosFinais().isEmpty()) {
            System.out.println("Nenhum estado final definido!");
            return false;
        }

        if (simboloInicialPilha == null
                || simboloInicialPilha.isEmpty()) {

            System.out.println("Simbolo inicial da pilha nao definido!");
            return false;
        }

        if (!validarPalavra(palavra)) {
            return false;
        }

        // Cria a pilha
        Deque<String> pilha = new ArrayDeque<>();

        // Insere o simbolo inicial
        pilha.push(simboloInicialPilha);

        int posicao = 0;
        int limite = 10000;
        int passos = 0;

        // Executa as transicoes
        while (passos < limite) {

            passos++;

            // Se toda a palavra foi lida e o estado e final
            if (posicao == palavra.length()
                    && atual.isFinal()) {

                return true;
            }

            if (pilha.isEmpty()) {
                return false;
            }

            String topo = pilha.peek();

            String simboloAtual = EPSILON;

            if (posicao < palavra.length()) {

                simboloAtual = String.valueOf(
                        palavra.charAt(posicao)
                );
            }

            List<TransicaoPilha> aplicaveis =
                    new ArrayList<>();

            // Procura as transicoes possiveis
            for (TransicaoPilha transicao : transicoesPilha) {

                boolean mesmoEstado =
                        transicao.getOrigem().equals(atual);

                boolean mesmoTopo =
                        transicao.getTopoPilha().equals(topo);

                boolean entradaValida =
                        transicao.getSimboloEntrada().equals(EPSILON)
                        || transicao.getSimboloEntrada()
                                .equals(simboloAtual);

                if (mesmoEstado && mesmoTopo && entradaValida) {

                    aplicaveis.add(transicao);
                }
            }

            // Nenhuma transicao encontrada
            if (aplicaveis.isEmpty()) {
                return false;
            }

            // Impede escolhas ambiguas nesta implementacao
            if (aplicaveis.size() > 1) {
                System.out.println(
                    "Erro: AP deterministico possui transicoes ambiguas!"
                );
                return false;
            }

            TransicaoPilha transicao = aplicaveis.get(0);

            // Remove o topo da pilha
            pilha.pop();

            // Insere os novos simbolos na pilha
            String substituir = transicao.getSubstituirPilha();

            if (!substituir.equals(EPSILON)) {

                // O primeiro caractere representa o novo topo
                for (int i = substituir.length() - 1; i >= 0; i--) {

                    pilha.push(
                        String.valueOf(substituir.charAt(i))
                    );
                }
            }

            // Atualiza o estado
            atual = transicao.getDestino();

            // Avanca na palavra se nao for epsilon
            if (!transicao.getSimboloEntrada().equals(EPSILON)) {
                posicao++;
            }
        }

        System.out.println("Limite de processamento atingido!");
        return false;
    }

    // Exibe o automato
    @Override
    public void exibirAutomato() {

        super.exibirAutomato();

        System.out.println(
            "\nSimbolo inicial da pilha: "
            + simboloInicialPilha
        );

        exibirTransicoesPilha();
    }
}