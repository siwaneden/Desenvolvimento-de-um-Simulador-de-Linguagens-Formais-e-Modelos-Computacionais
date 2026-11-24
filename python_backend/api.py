"""API HTTP, separada dos algoritmos, para os módulos Python.

Execute na raiz do projeto com: python -m python_backend.api
"""

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

from .algoritmos.complexidade import analisar_complexidade, listar_algoritmos
from .algoritmos.linguagens import analisar_linguagem
from .algoritmos.turing import simular_maquina


HOST = "127.0.0.1"
PORTA = 8000


class APIHandler(BaseHTTPRequestHandler):
    def _responder(self, status: int, conteudo: dict | list) -> None:
        corpo = json.dumps(conteudo, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(corpo)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()
        self.wfile.write(corpo)

    def do_OPTIONS(self) -> None:
        self._responder(204, {})

    def do_GET(self) -> None:
        caminho = urlparse(self.path).path
        if caminho == "/api/saude":
            self._responder(200, {"status": "ok", "servico": "módulos Python"})
        elif caminho == "/api/complexidade/algoritmos":
            self._responder(200, listar_algoritmos())
        else:
            self._responder(404, {"erro": "Rota não encontrada."})

    def do_POST(self) -> None:
        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
            dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
            caminho = urlparse(self.path).path

            if caminho == "/api/linguagens/analisar":
                resultado = analisar_linguagem(
                    dados.get("alfabeto", []),
                    dados.get("cadeia", ""),
                    dados.get("linguagem", []),
                    dados.get("concatenar_com", ""),
                    dados.get("tamanho_fechamento", 3),
                )
            elif caminho == "/api/turing/simular":
                resultado = simular_maquina(dados.get("tipo", ""), dados.get("entrada", ""))
            elif caminho == "/api/complexidade/analisar":
                resultado = analisar_complexidade(dados.get("algoritmo", ""), dados.get("n", 0))
            else:
                self._responder(404, {"erro": "Rota não encontrada."})
                return
            self._responder(200, resultado)
        except (ValueError, TypeError, json.JSONDecodeError) as erro:
            self._responder(400, {"erro": str(erro)})
        except Exception:
            self._responder(500, {"erro": "Erro interno ao processar a solicitação."})

    def log_message(self, formato: str, *args) -> None:
        print(f"[API Python] {self.address_string()} - {formato % args}")


def executar() -> None:
    servidor = ThreadingHTTPServer((HOST, PORTA), APIHandler)
    print(f"API Python iniciada em http://{HOST}:{PORTA}")
    try:
        servidor.serve_forever()
    except KeyboardInterrupt:
        print("\nAPI encerrada.")
    finally:
        servidor.server_close()


if __name__ == "__main__":
    executar()

