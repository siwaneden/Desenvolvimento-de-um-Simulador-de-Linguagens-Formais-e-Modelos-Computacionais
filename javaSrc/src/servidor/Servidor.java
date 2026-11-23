package servidor;

import automatos.Estado;
import automatos.Transicao;
import automatos.afd.AFD;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servidor {
    public static void init() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/usuarios", exchange -> {
            if (handleOptions(exchange)) return;
            send(exchange, 200, "[{\"nome\":\"João\"}]");
        });
        server.createContext("/api/afd/simular", Servidor::simularAFD);
        server.start();
        System.out.println("Servidor iniciado em http://localhost:8080");
    }

    private static void simularAFD(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"erro\":\"Use o método POST.\"}");
            return;
        }
        try {
            Map<String, String> form = parseForm(readBody(exchange.getRequestBody()));
            AFD afd = new AFD();
            String alphabet = form.getOrDefault("alfabeto", "");
            for (String symbol : alphabet.split(",")) {
                String value = symbol.trim();
                if (!value.isEmpty()) {
                    if (value.length() != 1) throw new IllegalArgumentException("Cada símbolo do alfabeto deve ter exatamente um caractere.");
                    afd.adicionarSimbolo(value);
                }
            }

            List<String> stateNames = new ArrayList<>();
            String rawStates = form.getOrDefault("estados", "");
            for (String row : rawStates.split(";")) {
                if (row.trim().isEmpty()) continue;
                String[] parts = row.split("\\|", -1);
                if (parts.length != 3) throw new IllegalArgumentException("Formato inválido para estado.");
                String name = parts[0].trim();
                if (name.isEmpty() || stateNames.contains(name)) throw new IllegalArgumentException("Nome de estado vazio ou duplicado.");
                Estado state = new Estado(name, Boolean.parseBoolean(parts[1]), Boolean.parseBoolean(parts[2]));
                afd.adicionarEstado(state);
                stateNames.add(name);
            }
            if (stateNames.isEmpty()) throw new IllegalArgumentException("Adicione pelo menos um estado.");
            long initialCount = stateNames.stream().filter(name -> {
                for (Estado state : afd.getEstados()) if (state.getNome().equals(name)) return state.isInicial();
                return false;
            }).count();
            if (initialCount != 1) throw new IllegalArgumentException("O AFD precisa ter exatamente um estado inicial.");
            if (afd.getEstadosFinais().isEmpty()) throw new IllegalArgumentException("Defina pelo menos um estado final.");

            String rawTransitions = form.getOrDefault("transicoes", "");
            List<String> usedPairs = new ArrayList<>();
            for (String row : rawTransitions.split(";")) {
                if (row.trim().isEmpty()) continue;
                String[] parts = row.split("\\|", -1);
                if (parts.length != 3) throw new IllegalArgumentException("Formato inválido para transição.");
                Estado from = afd.buscarEstado(parts[0].trim());
                Estado to = afd.buscarEstado(parts[2].trim());
                String symbol = parts[1].trim();
                if (symbol.length() != 1) throw new IllegalArgumentException("Cada transição deve consumir exatamente um caractere.");
                if (from == null || to == null) throw new IllegalArgumentException("A transição referencia um estado inexistente.");
                if (!afd.getAlfabeto().contains(symbol)) throw new IllegalArgumentException("O símbolo '" + symbol + "' não pertence ao alfabeto.");
                String pair = from.getNome() + "\u0000" + symbol;
                if (usedPairs.contains(pair)) throw new IllegalArgumentException("Transição duplicada: " + from.getNome() + " com símbolo " + symbol + ". Um AFD não pode ter duas saídas para o mesmo símbolo.");
                usedPairs.add(pair);
                afd.adicionarTransicao(new Transicao(from, symbol, to));
            }
            String word = form.getOrDefault("palavra", "");
            boolean accepted = afd.processar(word);
            String message = accepted ? "A execução terminou em um estado final." : "A execução não terminou em um estado final ou não encontrou uma transição válida.";
            send(exchange, 200, "{\"aceita\":" + accepted + ",\"mensagem\":\"" + escapeJson(message) + "\"}");
        } catch (IllegalArgumentException error) {
            send(exchange, 400, "{\"erro\":\"" + escapeJson(error.getMessage()) + "\"}");
        } catch (Exception error) {
            send(exchange, 500, "{\"erro\":\"Erro interno ao simular o AFD.\"}");
            error.printStackTrace();
        }
    }

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        addCors(exchange.getResponseHeaders());
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static String readBody(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static Map<String, String> parseForm(String body) throws Exception {
        Map<String, String> values = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], "UTF-8") : "";
            values.put(key, value);
        }
        return values;
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        addCors(exchange.getResponseHeaders());
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static void addCors(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }
}
