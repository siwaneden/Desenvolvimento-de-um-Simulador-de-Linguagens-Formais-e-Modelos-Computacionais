# Simulador de Linguagens Formais e Modelos Computacionais

Interface Vue com backend Java para os autômatos e backend Python para os módulos 1, 5 e 6.

## Executar os módulos Python

Na raiz do projeto, abra um terminal e execute:

```powershell
python -m python_backend.api
```

A API ficará disponível em `http://127.0.0.1:8000`. Ela usa somente a biblioteca padrão do Python; não é necessário instalar FastAPI ou outras dependências.

Em outro terminal, execute a interface:

```powershell
npm install
npm run dev
```

Os módulos implementados em Python são:

- Módulo 1 — Linguagens Formais: validação, pertinência a L, tamanho, concatenação, Σ* e Σ+;
- Módulo 5 — Máquina de Turing: reconhecimento de `w#w` e soma unária;
- Módulo 6 — análise de melhor caso, caso médio, pior caso e espaço.

## Rotas da API Python

| Método | Rota | Finalidade |
|---|---|---|
| `GET` | `/api/saude` | Verificar se a API está ativa |
| `POST` | `/api/linguagens/analisar` | Executar operações de linguagens formais |
| `POST` | `/api/turing/simular` | Simular uma das Máquinas de Turing |
| `GET` | `/api/complexidade/algoritmos` | Listar algoritmos analisados |
| `POST` | `/api/complexidade/analisar` | Obter a análise assintótica |

## Testes

```powershell
python -m unittest discover -s python_backend/tests -v
```

A fundamentação escrita de funcionamento, correção e custos assintóticos está em [`docs/ANALISE_DOS_ALGORITMOS.md`](docs/ANALISE_DOS_ALGORITMOS.md).

## Backend Java

Para os módulos AFD, AFN e AP, execute `javaSrc/buildRun.bat` em um ambiente com JDK configurado.
