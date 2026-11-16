package afn;

import modelo.Automato;
import modelo.Estado;
import modelo.Transicao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AFN extends Automato {

    private static final String EPSILON = "ε";

    public AFN() {
        super();
    }

    // Busca todos os destinos de uma transicao
    private Set<Estado> buscarDestinos(
            Estado atual, String simbolo) {

        Set<Estado> destinos = new HashSet<>();

        for (Transicao transicao : transicoes) {

            if (transicao.getOrigem().equals(atual)
                    && transicao.getSimbolo().equals(simbolo)) {

                destinos.add(transicao.getDestino());
            }
        }

        return destinos;
    }

    // Calcula o fechamento epsilon
    private Set<Estado> fechamentoEpsilon(Set<Estado> estadosAtuais) {

        Set<Estado> fechamento = new HashSet<>(estadosAtuais);

        List<Estado> fila = new ArrayList<>(estadosAtuais);

        while (!fila.isEmpty()) {

            Estado atual = fila.remove(0);

            Set<Estado> destinos = buscarDestinos(atual, EPSILON);

            for (Estado destino : destinos) {

                if (fechamento.add(destino)) {
                    fila.add(destino);
                }
            }
        }

        return fechamento;
    }

    // Processa a palavra no AFN
    @Override
    public boolean processar(String palavra) {

        Estado inicial = getEstadoInicial();

        if (inicial == null) {
            System.out.println("Erro: estado inicial nao definido!");
            return false;
        }

        if (getEstadosFinais().isEmpty()) {
            System.out.println("Erro: nenhum estado final definido!");
            return false;
        }

        // Verifica se todos os simbolos pertencem ao alfabeto
        for (int i = 0; i < palavra.length(); i++) {

            String simbolo = String.valueOf(palavra.charAt(i));

            if (!alfabeto.contains(simbolo)) {
                return false;
            }
        }

        // Comeca pelo estado inicial
        Set<Estado> estadosAtuais = new HashSet<>();
        estadosAtuais.add(inicial);

        estadosAtuais = fechamentoEpsilon(estadosAtuais);

        // Percorre a palavra
        for (int i = 0; i < palavra.length(); i++) {

            String simbolo = String.valueOf(palavra.charAt(i));

            Set<Estado> proximosEstados = new HashSet<>();

            // Analisa todos os estados atuais
            for (Estado estado : estadosAtuais) {

                proximosEstados.addAll(
                    buscarDestinos(estado, simbolo)
                );
            }

            // Aplica fechamento epsilon novamente
            estadosAtuais = fechamentoEpsilon(proximosEstados);

            // Se nao houver caminhos, rejeita
            if (estadosAtuais.isEmpty()) {
                return false;
            }
        }

        // Verifica se algum caminho terminou em estado final
        estadosAtuais = fechamentoEpsilon(estadosAtuais);

        for (Estado estado : estadosAtuais) {

            if (estado.isFinal()) {
                return true;
            }
        }

        return false;
    }
}