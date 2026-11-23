package servidor;

import automatos.Estado;
import automatos.Transicao;
import automatos.afd.AFD;
import automatos.afn.AFN;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    public static void init() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/usuarios", exchange -> {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            sendJson(exchange, 200, "[{\"nome\":\"João\"}]");
        });

        server.createContext("/api/afd/simular", exchange -> handleSimulation(exchange, false));
        server.createContext("/api/afn/simular", exchange -> handleSimulation(exchange, true));
        server.start();
        System.out.println("Servidor iniciado em http://localhost:8080");
    }

    private static void handleSimulation(HttpExchange exchange, boolean nondeterministic) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"erro\":\"Método não permitido.\"}");
            return;
        }

        try {
            Map<String, String> params = readForm(exchange);
            String alphabet = required(params, "alfabeto");
            String statesText = required(params, "estados");
            String transitionsText = params.getOrDefault("transicoes", "");
            String word = params.getOrDefault("palavra", "");

            AFD afd = nondeterministic ? null : new AFD();
            AFN afn = nondeterministic ? new AFN() : null;
            for (String symbol : alphabet.split(",")) {
                String clean = symbol.trim();
                if (!clean.isEmpty()) {
                    if (nondeterministic) afn.adicionarSimbolo(clean);
                    else afd.adicionarSimbolo(clean);
                }
            }

            Map<String, Estado> byName = new HashMap<>();
            for (String entry : statesText.split(";")) {
                if (entry.trim().isEmpty()) continue;
                String[] parts = entry.split("\\|", -1);
                if (parts.length != 3) throw new IllegalArgumentException("Formato inválido para um estado.");
                String name = parts[0].trim();
                if (name.isEmpty() || byName.containsKey(name)) throw new IllegalArgumentException("Nome de estado vazio ou duplicado: " + name);
                Estado state = new Estado(name, Boolean.parseBoolean(parts[1]), Boolean.parseBoolean(parts[2]));
                byName.put(name, state);
                if (nondeterministic) afn.adicionarEstado(state);
                else afd.adicionarEstado(state);
            }
            if (byName.isEmpty()) throw new IllegalArgumentException("Adicione pelo menos um estado.");

            for (String entry : transitionsText.split(";")) {
                if (entry.trim().isEmpty()) continue;
                String[] parts = entry.split("\\|", -1);
                if (parts.length != 3) throw new IllegalArgumentException("Formato inválido para uma transição.");
                Estado from = byName.get(parts[0].trim());
                Estado to = byName.get(parts[2].trim());
                String symbol = parts[1].trim();
                if (from == null || to == null) throw new IllegalArgumentException("Uma transição referencia um estado inexistente.");
                if (symbol.isEmpty()) throw new IllegalArgumentException("Informe o símbolo da transição.");
                if (nondeterministic) {
                    if (!"ε".equals(symbol) && !afn.getAlfabeto().contains(symbol)) throw new IllegalArgumentException("O símbolo '" + symbol + "' não pertence ao alfabeto.");
                    afn.adicionarTransicao(new Transicao(from, symbol, to));
                } else {
                    if (!afd.getAlfabeto().contains(symbol)) throw new IllegalArgumentException("O símbolo '" + symbol + "' não pertence ao alfabeto.");
                    for (Transicao existing : afd.getTransicoes()) {
                        if (existing.getOrigem().equals(from) && existing.getSimbolo().equals(symbol)) {
                            throw new IllegalArgumentException("O AFD não pode ter duas transições para o mesmo símbolo a partir de um estado.");
                        }
                    }
                    afd.adicionarTransicao(new Transicao(from, symbol, to));
                }
            }

            boolean accepted = nondeterministic ? afn.processar(word) : afd.processar(word);
            String type = nondeterministic ? "AFN" : "AFD";
            String message = accepted
                ? "A palavra foi aceita pelo " + type + "."
                : "A palavra foi rejeitada pelo " + type + ".";
            sendJson(exchange, 200, "{\"aceita\":" + accepted + ",\"mensagem\":\"" + escapeJson(message) + "\"}");
        } catch (IllegalArgumentException ex) {
            sendJson(exchange, 400, "{\"erro\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 500, "{\"erro\":\"Falha interna ao simular o autômato.\"}");
        }
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        exchange.getRequestBody().transferTo(buffer);
        String body = buffer.toString(StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }

    private static String required(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Campo obrigatório ausente: " + key + ".");
        return value;
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
