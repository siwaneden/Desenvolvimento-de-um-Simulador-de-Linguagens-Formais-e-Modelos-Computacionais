package automatos.ap;

import automatos.Automato;
import automatos.Estado;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AutomatoPilha extends Automato {

    private static final String EPSILON = "ε";

    private List<TransicaoPilha> transicoesPilha;
    private String simboloInicialPilha;
    private List<String> historico;

    public AutomatoPilha() {
        super();
        transicoesPilha = new ArrayList<>();
        historico = new ArrayList<>();
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

    // Retorna os passos da última simulação, sem expor a lista interna.
    public List<String> getHistorico() {
        return new ArrayList<>(historico);
    }

    private String descreverPilha(Deque<String> pilha) {
        return pilha.isEmpty() ? "ε" : String.join("", pilha);
    }

    // Processa a palavra
    @Override
    public boolean processar(String palavra) {
        historico.clear();

        Estado atual = getEstadoInicial();

        if (atual == null) {
            historico.add("Simulação inválida: estado inicial não definido.");
            return false;
        }

        if (getEstadosFinais().isEmpty()) {
            historico.add("Simulação inválida: nenhum estado final definido.");
            return false;
        }

        if (simboloInicialPilha == null
                || simboloInicialPilha.isEmpty()) {

            historico.add("Simulação inválida: símbolo inicial da pilha não definido.");
            return false;
        }

        if (!validarPalavra(palavra)) {
            historico.add("Palavra rejeitada: contém símbolo fora do alfabeto.");
            return false;
        }

        // Cria a pilha
        Deque<String> pilha = new ArrayDeque<>();

        // Insere o simbolo inicial
        pilha.push(simboloInicialPilha);
        historico.add("Inicialização: estado " + atual.getNome()
                + ", entrada " + (palavra.isEmpty() ? "ε" : palavra)
                + ", pilha " + descreverPilha(pilha) + ".");

        int posicao = 0;
        int limite = 10000;
        int passos = 0;

        // Executa as transicoes
        while (passos < limite) {

            passos++;

            // Se toda a palavra foi lida e o estado e final
            if (posicao == palavra.length()
                    && atual.isFinal()) {

                historico.add("Aceita: entrada consumida no estado final "
                        + atual.getNome() + ", pilha " + descreverPilha(pilha) + ".");
                return true;
            }

            if (pilha.isEmpty()) {
                historico.add("Rejeita: a pilha ficou vazia antes da aceitação.");
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
                historico.add("Rejeita: nenhuma transição aplicável em "
                        + atual.getNome() + " com topo " + topo
                        + " e próxima entrada " + simboloAtual + ".");
                return false;
            }

            // Impede escolhas ambiguas nesta implementacao
            if (aplicaveis.size() > 1) {
                historico.add("Rejeita: há mais de uma transição aplicável; "
                        + "o simulador implementa AP determinístico.");
                return false;
            }

            TransicaoPilha transicao = aplicaveis.get(0);

            String entradaConsumida = transicao.getSimboloEntrada();
            String estadoOrigem = atual.getNome();
            String pilhaAntes = descreverPilha(pilha);

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

            historico.add("Passo " + passos + ": " + estadoOrigem
                    + " --(" + entradaConsumida + ", " + topo + "/"
                    + substituir + ")--> " + atual.getNome()
                    + "; pilha " + pilhaAntes + " → " + descreverPilha(pilha)
                    + "; restante " + (posicao >= palavra.length() ? "ε" : palavra.substring(posicao)) + ".");
        }

        historico.add("Rejeita: limite de 10.000 passos atingido.");
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