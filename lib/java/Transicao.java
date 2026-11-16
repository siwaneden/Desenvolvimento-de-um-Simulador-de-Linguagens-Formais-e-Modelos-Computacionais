package modelo;

public class Transicao {

    private Estado origem;
    private String simbolo;
    private Estado destino;

    public Transicao(Estado origem, String simbolo, Estado destino) {
        this.origem = origem;
        this.simbolo = simbolo;
        this.destino = destino;
    }

    public Estado getOrigem() {
        return origem;
    }

    public void setOrigem(Estado origem) {
        this.origem = origem;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public Estado getDestino() {
        return destino;
    }

    public void setDestino(Estado destino) {
        this.destino = destino;
    }

    @Override
    public String toString() {
        return origem.getNome() + " --" + simbolo + "--> " + destino.getNome();
    }
}