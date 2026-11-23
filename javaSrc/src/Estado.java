package automatos;

public class Estado {

    private String nome;
    private boolean inicial;
    private boolean isEstadoFinal;

    public Estado(String nome) {
        this.nome = nome;
        this.inicial = false;
        this.isEstadoFinal = false;
    }

    public Estado(String nome, boolean inicial, boolean isEstadoFinal) {
        this.nome = nome;
        this.inicial = inicial;
        this.isEstadoFinal = isEstadoFinal;
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
        return isEstadoFinal;
    }

    public void setFinal(boolean isEstadoFinal) {
        this.isEstadoFinal = isEstadoFinal;
    }

    @Override
    public String toString() {
        return nome;
    }
}
