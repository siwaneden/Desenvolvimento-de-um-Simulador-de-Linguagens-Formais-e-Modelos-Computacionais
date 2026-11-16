package ap;

import modelo.Estado;

public class TransicaoPilha {

    private Estado origem;
    private String simboloEntrada;
    private String topoPilha;
    private String substituirPilha;
    private Estado destino;

    public TransicaoPilha(
            Estado origem,
            String simboloEntrada,
            String topoPilha,
            String substituirPilha,
            Estado destino) {

        this.origem = origem;
        this.simboloEntrada = simboloEntrada;
        this.topoPilha = topoPilha;
        this.substituirPilha = substituirPilha;
        this.destino = destino;
    }

    public Estado getOrigem() {
        return origem;
    }

    public String getSimboloEntrada() {
        return simboloEntrada;
    }

    public String getTopoPilha() {
        return topoPilha;
    }

    public String getSubstituirPilha() {
        return substituirPilha;
    }

    public Estado getDestino() {
        return destino;
    }

    @Override
    public String toString() {
        return origem.getNome()
                + " -- (" + simboloEntrada
                + ", " + topoPilha
                + " / " + substituirPilha
                + ") --> " + destino.getNome();
    }
}