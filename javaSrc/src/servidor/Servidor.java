package servidor;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Servidor {
    public static void init() throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/api/usuarios", exchange -> {
            String json = "[{\"nome\":\"João\"}]";

            exchange.getResponseHeaders()
                    .set("Content-Type", "application/json");

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", "*");

            byte[] response = json.getBytes("UTF-8");

            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
    }
}