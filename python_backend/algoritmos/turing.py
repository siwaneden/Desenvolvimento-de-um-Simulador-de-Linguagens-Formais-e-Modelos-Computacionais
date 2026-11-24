"""Máquinas de Turing do módulo 5, com histórico de configurações."""

BRANCO = "□"
LIMITE_PASSOS = 10_000


def _instantaneo(fita: dict[int, str], cabeca: int, estado: str, acao: str) -> dict:
    usados = [indice for indice, simbolo in fita.items() if simbolo != BRANCO]
    inicio = min(usados + [cabeca]) - 1
    fim = max(usados + [cabeca]) + 1
    return {
        "estado": estado,
        "cabeca": cabeca - inicio,
        "fita": "".join(fita.get(indice, BRANCO) for indice in range(inicio, fim + 1)),
        "acao": acao,
    }


def _executar(entrada: str, transicoes: dict, estado_inicial: str, estados_finais: set[str]) -> dict:
    fita = {indice: simbolo for indice, simbolo in enumerate(entrada)}
    cabeca = 0
    estado = estado_inicial
    historico = [_instantaneo(fita, cabeca, estado, "Configuração inicial")]

    for numero in range(1, LIMITE_PASSOS + 1):
        if estado in estados_finais:
            break
        lido = fita.get(cabeca, BRANCO)
        regra = transicoes.get((estado, lido))
        if regra is None:
            estado = "q_rejeita"
            historico.append(_instantaneo(fita, cabeca, estado, f"Sem transição para {lido}"))
            break

        proximo, escrito, movimento = regra
        fita[cabeca] = escrito
        cabeca += 1 if movimento == "D" else -1 if movimento == "E" else 0
        estado = proximo
        historico.append(
            _instantaneo(fita, cabeca, estado, f"Passo {numero}: lê {lido}, escreve {escrito}, move {movimento}")
        )
    else:
        estado = "q_rejeita"
        historico.append(_instantaneo(fita, cabeca, estado, "Limite de passos atingido"))

    usados = [indice for indice, simbolo in fita.items() if simbolo != BRANCO]
    saida = "" if not usados else "".join(fita.get(i, BRANCO) for i in range(min(usados), max(usados) + 1))
    return {
        "aceita": estado == "q_aceita",
        "estado_final": estado,
        "saida": saida,
        "passos": len(historico) - 1,
        "historico": historico,
    }


def reconhecer_copia(entrada: str) -> dict:
    """Reconhece L={w#w | w pertence a {0,1}*}."""
    transicoes = {}

    def adicionar(estado, lidos, proximo, escritos=None, movimento="D"):
        for lido in lidos:
            escrito = lido if escritos is None else escritos
            transicoes[(estado, lido)] = (proximo, escrito, movimento)

    adicionar("q_inicio", "XY", "q_inicio")
    adicionar("q_inicio", "0", "q_acha_hash_0", "X")
    adicionar("q_inicio", "1", "q_acha_hash_1", "Y")
    adicionar("q_inicio", "#", "q_verifica", "#")

    adicionar("q_acha_hash_0", "01XY", "q_acha_hash_0")
    adicionar("q_acha_hash_0", "#", "q_compara_0")
    adicionar("q_acha_hash_1", "01XY", "q_acha_hash_1")
    adicionar("q_acha_hash_1", "#", "q_compara_1")

    adicionar("q_compara_0", "XY", "q_compara_0")
    adicionar("q_compara_0", "0", "q_retorna", "X", "E")
    adicionar("q_compara_1", "XY", "q_compara_1")
    adicionar("q_compara_1", "1", "q_retorna", "Y", "E")

    adicionar("q_retorna", "01XY#", "q_retorna", movimento="E")
    adicionar("q_retorna", BRANCO, "q_inicio", BRANCO, "D")
    adicionar("q_verifica", "XY", "q_verifica")
    adicionar("q_verifica", BRANCO, "q_aceita", BRANCO, "P")

    resultado = _executar(entrada, transicoes, "q_inicio", {"q_aceita", "q_rejeita"})
    resultado["mensagem"] = "A entrada pertence a L = {w#w}." if resultado["aceita"] else "A entrada não pertence a L = {w#w}."
    return resultado


def somar_unarios(entrada: str) -> dict:
    """Soma 0^m 1 0^n, deixando 0^(m+n) na fita."""
    transicoes = {
        ("q_busca_separador", "0"): ("q_busca_separador", "0", "D"),
        ("q_busca_separador", "1"): ("q_busca_fim", "0", "D"),
        ("q_busca_fim", "0"): ("q_busca_fim", "0", "D"),
        ("q_busca_fim", BRANCO): ("q_apaga_excedente", BRANCO, "E"),
        ("q_apaga_excedente", "0"): ("q_aceita", BRANCO, "P"),
    }
    resultado = _executar(entrada, transicoes, "q_busca_separador", {"q_aceita", "q_rejeita"})
    resultado["mensagem"] = "Soma unária concluída." if resultado["aceita"] else "Use o formato 0^m10^n, com m,n ≥ 1."
    return resultado


def simular_maquina(tipo: str, entrada: str) -> dict:
    entrada = str(entrada).strip()
    if tipo == "copia":
        if not entrada or any(simbolo not in "01#" for simbolo in entrada) or entrada.count("#") != 1:
            raise ValueError("Para w#w, use 0, 1 e exatamente um separador #.")
        return reconhecer_copia(entrada)
    if tipo == "soma":
        if not entrada or any(simbolo not in "01" for simbolo in entrada) or entrada.count("1") != 1:
            raise ValueError("Para a soma, use 0^m10^n, por exemplo 000100.")
        esquerda, direita = entrada.split("1")
        if not esquerda or not direita:
            raise ValueError("Os dois números unários devem ter pelo menos um zero.")
        return somar_unarios(entrada)
    raise ValueError("Tipo de máquina inválido. Use 'copia' ou 'soma'.")

