package modelo;

public class Estado {

    private String nome;
    private boolean inicial;
    private boolean finalEstado;

    public Estado(String nome) {
        this.nome = nome;
        this.inicial = false;
        this.finalEstado = false;
    }

    public Estado(String nome, boolean inicial, boolean finalEstado) {
        this.nome = nome;
        this.inicial = inicial;
        this.finalEstado = finalEstado;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isInicial() {
        return inicial;
    }

    public void setInicial(boolean inicial) {
        this.inicial = inicial;
    }

    public boolean isFinal() {
        return finalEstado;
    }

    public void setFinal(boolean finalEstado) {
        this.finalEstado = finalEstado;
    }

    @Override
    public String toString() {
        return nome;
    }
}
