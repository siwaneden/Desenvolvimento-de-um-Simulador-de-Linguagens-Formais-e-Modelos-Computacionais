package automatos;

import java.util.ArrayList;
import java.util.List;

public abstract class Automato {

    protected List<String> alfabeto;
    protected List<Estado> estados;
    protected List<Transicao> transicoes;

    public Automato() {
        this.alfabeto = new ArrayList<>();
        this.estados = new ArrayList<>();
        this.transicoes = new ArrayList<>();
    }

    // Adicionar simbolo ao alfabeto
    public void adicionarSimbolo(String simbolo) {
        if (!alfabeto.contains(simbolo)) {
            alfabeto.add(simbolo);
        }
    }

    // Adicionar estado
    public void adicionarEstado(Estado estado) {
        estados.add(estado);
    }

    // Adicionar transicao
    public void adicionarTransicao(Transicao transicao) {
        transicoes.add(transicao);
    }

    // Buscar estado pelo nome
    public Estado buscarEstado(String nome) {
        for (Estado estado : estados) {
            if (estado.getNome().equals(nome)) {
                return estado;
            }
        }

        return null;
    }

    // Obter estado inicial
    public Estado getEstadoInicial() {
        for (Estado estado : estados) {
            if (estado.isInicial()) {
                return estado;
            }
        }

        return null;
    }

    // Obter estados finais
    public List<Estado> getEstadosFinais() {
        List<Estado> finais = new ArrayList<>();

        for (Estado estado : estados) {
            if (estado.isFinal()) {
                finais.add(estado);
            }
        }

        return finais;
    }

    // Exibir estrutura do automato
    public void exibirAutomato() {

        System.out.println("\n===== AUTOMATO =====");

        System.out.println("Alfabeto: " + alfabeto);

        System.out.println("\nEstados:");

        for (Estado estado : estados) {
            System.out.println(
                estado.getNome()
                + (estado.isInicial() ? " [Inicial]" : "")
                + (estado.isFinal() ? " [Final]" : "")
            );
        }

        System.out.println("\nTransicoes:");

        for (Transicao transicao : transicoes) {
            System.out.println(transicao);
        }
    }

    // Getters
    public List<String> getAlfabeto() {
        return alfabeto;
    }

    public List<Estado> getEstados() {
        return estados;
    }

    public List<Transicao> getTransicoes() {
        return transicoes;
    }

    // Metodo que cada automato implementara
    public abstract boolean processar(String palavra);
}