"""Operações do módulo 1 — Linguagens Formais."""

from itertools import product


LIMITE_GERACAO = 5_000


def _normalizar_alfabeto(alfabeto: list[str]) -> list[str]:
    if not isinstance(alfabeto, list):
        raise ValueError("O alfabeto deve ser uma lista de símbolos.")

    simbolos: list[str] = []
    for item in alfabeto:
        simbolo = str(item).strip()
        if not simbolo:
            raise ValueError("O alfabeto não pode conter símbolos vazios.")
        if simbolo not in simbolos:
            simbolos.append(simbolo)

    if not simbolos:
        raise ValueError("Informe pelo menos um símbolo no alfabeto.")
    return simbolos


def _segmentar(cadeia: str, alfabeto: list[str]) -> list[str] | None:
    """Encontra uma segmentação válida, inclusive para símbolos com vários caracteres."""
    possibilidades: list[list[str] | None] = [None] * (len(cadeia) + 1)
    possibilidades[0] = []

    for posicao in range(len(cadeia) + 1):
        if possibilidades[posicao] is None:
            continue
        for simbolo in alfabeto:
            if cadeia.startswith(simbolo, posicao):
                fim = posicao + len(simbolo)
                if possibilidades[fim] is None:
                    possibilidades[fim] = possibilidades[posicao] + [simbolo]
    return possibilidades[-1]


def gerar_fechamento(alfabeto: list[str], tamanho_maximo: int, positivo: bool = False) -> list[str]:
    if not isinstance(tamanho_maximo, int) or tamanho_maximo < 0 or tamanho_maximo > 8:
        raise ValueError("O tamanho máximo deve ser um inteiro entre 0 e 8.")

    quantidade = sum(len(alfabeto) ** tamanho for tamanho in range(tamanho_maximo + 1))
    if quantidade > LIMITE_GERACAO:
        raise ValueError(f"A geração ultrapassaria o limite de {LIMITE_GERACAO} cadeias.")

    inicio = 1 if positivo else 0
    resultado: list[str] = []
    for tamanho in range(inicio, tamanho_maximo + 1):
        if tamanho == 0:
            resultado.append("ε")
        else:
            resultado.extend("".join(itens) for itens in product(alfabeto, repeat=tamanho))
    return resultado


def analisar_linguagem(
    alfabeto: list[str],
    cadeia: str,
    linguagem: list[str] | None = None,
    concatenar_com: str = "",
    tamanho_fechamento: int = 3,
) -> dict:
    simbolos = _normalizar_alfabeto(alfabeto)
    cadeia = str(cadeia)
    concatenar_com = str(concatenar_com)
    segmentacao = _segmentar(cadeia, simbolos)
    segmentacao_concatenacao = _segmentar(concatenar_com, simbolos)

    linguagem_normalizada = list(dict.fromkeys(str(item) for item in (linguagem or [])))
    return {
        "alfabeto": simbolos,
        "cadeia": cadeia,
        "cadeia_valida": segmentacao is not None,
        "segmentacao": segmentacao or [],
        "tamanho": len(segmentacao) if segmentacao is not None else len(cadeia),
        "linguagem": linguagem_normalizada,
        "pertence_linguagem": cadeia in linguagem_normalizada,
        "concatenacao": cadeia + concatenar_com,
        "concatenacao_valida": segmentacao is not None and segmentacao_concatenacao is not None,
        "sigma_estrela": gerar_fechamento(simbolos, tamanho_fechamento),
        "sigma_mais": gerar_fechamento(simbolos, tamanho_fechamento, positivo=True),
    }

