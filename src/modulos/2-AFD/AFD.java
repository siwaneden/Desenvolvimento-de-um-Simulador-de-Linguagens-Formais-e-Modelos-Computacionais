package afd;

import modelo.Automato;
import modelo.Estado;
import modelo.Transicao;

public class AFD extends Automato {

    public AFD() {
        super();
    }

    // Verifica se a palavra pertence ao alfabeto
    private boolean validarAlfabeto(String palavra) {

        for (int i = 0; i < palavra.length(); i++) {

            String simbolo = String.valueOf(palavra.charAt(i));

            if (!alfabeto.contains(simbolo)) {
                return false;
            }
        }

        return true;
    }

    // Busca a transicao correspondente
    private Estado buscarTransicao(Estado atual, String simbolo) {

        for (Transicao transicao : transicoes) {

            if (transicao.getOrigem().equals(atual)
                    && transicao.getSimbolo().equals(simbolo)) {

                return transicao.getDestino();
            }
        }

        return null;
    }

    // Processa a palavra no AFD
    @Override
    public boolean processar(String palavra) {

        Estado atual = getEstadoInicial();

        // Verifica se existe estado inicial
        if (atual == null) {
            System.out.println("Erro: estado inicial nao definido!");
            return false;
        }

        // Verifica se existem estados finais
        if (getEstadosFinais().isEmpty()) {
            System.out.println("Erro: nenhum estado final definido!");
            return false;
        }

        // Verifica se a palavra pertence ao alfabeto
        if (!validarAlfabeto(palavra)) {
            return false;
        }

        // Percorre cada simbolo da palavra
        for (int i = 0; i < palavra.length(); i++) {

            String simbolo = String.valueOf(palavra.charAt(i));

            Estado proximo = buscarTransicao(atual, simbolo);

            // Se nao existir transicao, rejeita
            if (proximo == null) {
                return false;
            }

            atual = proximo;
        }

        // Verifica se terminou em um estado final
        return atual.isFinal();
    }
}