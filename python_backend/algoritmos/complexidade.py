"""Catálogo e análise do módulo 6 — Complexidade Computacional."""

CATALOGO = {
    "validar_cadeia": {
        "nome": "Validar e segmentar cadeia",
        "descricao": "Verifica se a cadeia pode ser formada pelos símbolos do alfabeto.",
        "melhor": "Ω(n)",
        "medio": "Θ(n·k·l + n²)",
        "pior": "O(n·k·l + n²)",
        "espaco": "O(n²)",
        "estimador": lambda n: n * n,
        "analise": {
            "funcionamento": "O algoritmo usa programação dinâmica. Cada posição alcançável da cadeia testa os símbolos do alfabeto e registra uma segmentação válida. Isso permite trabalhar também com símbolos que possuem mais de um caractere.",
            "justificativa": "A cadeia possui n posições. Em cada uma delas podem ser testados k símbolos, com até l caracteres. A implementação também copia listas parciais da segmentação, acrescentando um termo quadrático no tempo.",
            "memoria": "As posições podem armazenar cópias de segmentações parciais. Por isso, a implementação atual pode utilizar O(n²) de memória.",
            "melhoria": "Guardar somente o predecessor de cada posição reduziria o espaço para O(n). Para alfabetos de símbolos unitários, uma busca em conjunto também simplificaria a validação.",
        },
    },
    "gerar_fechamento": {
        "nome": "Gerar Σ* ou Σ+ até tamanho n",
        "descricao": "Enumera todas as cadeias possíveis até o limite informado.",
        "melhor": "Ω(1)",
        "medio": "Θ(|Σ|ⁿ)",
        "pior": "O(|Σ|ⁿ)",
        "espaco": "O(|Σ|ⁿ)",
        "estimador": lambda n: 2 ** n,
        "analise": {
            "funcionamento": "O algoritmo combina os símbolos do alfabeto em todos os comprimentos entre zero e n. Σ* inclui ε, enquanto Σ+ começa nas cadeias de tamanho um.",
            "justificativa": "Para um alfabeto com k símbolos existem kⁱ cadeias de tamanho i. A soma 1 + k + k² + ... + kⁿ é dominada pelo último termo e cresce exponencialmente.",
            "memoria": "Todas as cadeias geradas são mantidas para envio à interface, exigindo espaço proporcional à quantidade e ao comprimento dos resultados.",
            "melhoria": "Gerar os valores sob demanda ou usar paginação evita materializar todo o fechamento. O projeto também limita o resultado a 5.000 cadeias.",
        },
    },
    "turing_copia": {
        "nome": "MT para reconhecer w#w",
        "descricao": "Marca um símbolo de w por rodada e procura seu correspondente.",
        "melhor": "Ω(n)",
        "medio": "Θ(n²)",
        "pior": "O(n²)",
        "espaco": "O(n)",
        "estimador": lambda n: n * n,
        "analise": {
            "funcionamento": "A máquina marca um símbolo antes de #, atravessa a fita para comparar o correspondente depois de # e retorna ao início. O processo continua até os dois lados estarem marcados.",
            "justificativa": "São realizadas até n rodadas e cada rodada pode percorrer n posições da fita. Por isso, entradas aceitas e rejeições tardias apresentam crescimento quadrático.",
            "memoria": "A fita usa O(n). Na versão visual, o histórico guarda uma fotografia de tamanho O(n) para cada passo e pode chegar a O(n³) em entradas quadráticas.",
            "melhoria": "Sem armazenar todas as fotografias, a memória volta a O(n). A interface também pode paginar ou limitar o histórico exibido.",
        },
    },
    "turing_soma": {
        "nome": "MT para soma unária",
        "descricao": "Percorre a fita, remove o separador e apaga o zero excedente.",
        "melhor": "Ω(n)",
        "medio": "Θ(n)",
        "pior": "O(n)",
        "espaco": "O(n)",
        "estimador": lambda n: n + 2,
        "analise": {
            "funcionamento": "A máquina localiza o separador 1, troca-o por 0, avança até o final da fita e apaga um zero excedente. Assim, 000100 se transforma em 00000.",
            "justificativa": "A cabeça percorre a entrada um número constante de vezes. Logo, a quantidade de transições cresce linearmente com n.",
            "memoria": "A fita ocupa O(n). O histórico visual completo ocupa O(n²), pois cada um dos O(n) passos salva uma fotografia de tamanho O(n).",
            "melhoria": "Manter apenas a configuração atual ou registrar somente as alterações da fita reduz o custo adicional do histórico.",
        },
    },
    "afd": {
        "nome": "Simulação de AFD",
        "descricao": "Executa uma transição para cada símbolo da entrada.",
        "melhor": "Ω(n)",
        "medio": "Θ(n·t)",
        "pior": "O(n·t)",
        "espaco": "O(1)",
        "estimador": lambda n: n,
        "analise": {
            "funcionamento": "O AFD valida os símbolos da palavra e executa exatamente uma transição para cada símbolo lido.",
            "justificativa": "A validação percorre n símbolos. Como as transições estão em uma lista, localizar uma regra pode examinar até t transições a cada passo, resultando em O(n·t).",
            "memoria": "A simulação mantém apenas o estado atual e algumas variáveis auxiliares, usando espaço constante, sem considerar a estrutura do autômato já carregada.",
            "melhoria": "Indexar as transições por par (estado, símbolo) em um mapa reduz a simulação para Θ(n) em média.",
        },
    },
    "ap": {
        "nome": "Simulação de AP determinístico",
        "descricao": "Processa a entrada e atualiza a pilha a cada transição.",
        "melhor": "Ω(1)",
        "medio": "Θ(n·t)",
        "pior": "O(n·t)",
        "espaco": "O(n)",
        "estimador": lambda n: n,
        "analise": {
            "funcionamento": "O AP determinístico lê a entrada, consulta uma transição compatível com o estado e o topo e então desempilha ou empilha símbolos.",
            "justificativa": "Cada passo pode percorrer a lista de t transições. Para uma execução proporcional à entrada, o custo é O(n·t), limitado no projeto a 10.000 passos.",
            "memoria": "A pilha pode crescer proporcionalmente à entrada, consumindo O(n). O histórico passo a passo também aumenta o espaço utilizado.",
            "melhoria": "Um índice por estado, entrada e topo evita a busca linear e aproxima o processamento de Θ(n), desde que o tamanho das substituições seja limitado.",
        },
    },
}


def listar_algoritmos() -> list[dict]:
    return [{"id": identificador, **{k: v for k, v in dados.items() if k != "estimador"}} for identificador, dados in CATALOGO.items()]


def analisar_complexidade(algoritmo: str, tamanho_entrada: int) -> dict:
    if algoritmo not in CATALOGO:
        raise ValueError("Algoritmo desconhecido.")
    if not isinstance(tamanho_entrada, int) or tamanho_entrada < 0 or tamanho_entrada > 10_000:
        raise ValueError("O tamanho da entrada deve ser um inteiro entre 0 e 10.000.")

    dados = CATALOGO[algoritmo]
    return {
        "id": algoritmo,
        **{chave: valor for chave, valor in dados.items() if chave != "estimador"},
        "n": tamanho_entrada,
        "operacoes_estimadas": dados["estimador"](tamanho_entrada),
        "observacao": "A estimativa ilustra a taxa de crescimento; não representa tempo em segundos.",
    }

