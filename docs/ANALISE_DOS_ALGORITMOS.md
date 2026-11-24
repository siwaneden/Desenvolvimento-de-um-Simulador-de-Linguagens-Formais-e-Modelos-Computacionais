# Análise dos algoritmos implementados em Python

## 1. Objetivo e critérios da análise

Os algoritmos Python foram separados da API HTTP para que a lógica possa ser testada e analisada independentemente da interface. A análise considera tempo de execução e memória utilizada, empregando as seguintes notações:

- **O(f(n))** representa um limite superior de crescimento, normalmente associado ao pior caso;
- **Ω(f(n))** representa um limite inferior, normalmente associado ao melhor caso;
- **Θ(f(n))** representa um limite assintótico justo, quando o crescimento superior e inferior possui a mesma ordem.

As variáveis usadas são:

- `n`: tamanho da cadeia ou da entrada da Máquina de Turing;
- `k`: quantidade de símbolos do alfabeto;
- `l`: comprimento do maior símbolo do alfabeto;
- `m`: tamanho máximo escolhido para gerar Σ* e Σ+;
- `p`: quantidade de passos executados pela Máquina de Turing.

## 2. Operações sobre linguagens formais

O módulo de linguagens formais normaliza o alfabeto, verifica se uma cadeia pode ser formada pelos símbolos informados, calcula seu tamanho, testa sua presença em uma linguagem finita, realiza concatenação e gera subconjuntos finitos de Σ* e Σ+.

### 2.1 Normalização do alfabeto

Cada símbolo é convertido para texto, tem seus espaços externos removidos e é inserido apenas se ainda não estiver presente. A lista final mantém a ordem informada pelo usuário.

Como a verificação de duplicidade é feita em uma lista, cada nova consulta pode percorrer até `k` elementos. Assim:

- melhor caso: Ω(k), quando as verificações encontram os símbolos rapidamente;
- caso médio: Θ(k²);
- pior caso: O(k²);
- espaço adicional: O(k).

Uma possível otimização seria usar um conjunto auxiliar para detectar símbolos repetidos em tempo médio constante, reduzindo a normalização para Θ(k).

### 2.2 Validação e segmentação da cadeia

A cadeia pode usar símbolos com mais de um caractere. Por isso, o algoritmo não verifica apenas caractere por caractere. Ele utiliza programação dinâmica: para cada posição alcançável da cadeia, testa quais símbolos do alfabeto começam naquela posição e registra uma segmentação válida.

Esse procedimento evita testar repetidamente todas as combinações possíveis. Considerando as comparações de prefixos e as cópias das listas que guardam a segmentação:

- melhor caso: Ω(n), pois a cadeia precisa ser recebida e examinada;
- caso médio: Θ(n·k·l + n²);
- pior caso: O(n·k·l + n²);
- espaço: O(n²) na implementação atual, porque diferentes posições podem armazenar cópias parciais da segmentação.

Quando todos os símbolos possuem apenas um caractere, a parte de comparação se aproxima de Θ(n·k). O espaço poderia ser reduzido para O(n) armazenando somente o predecessor de cada posição, em vez de copiar a lista completa de símbolos.

### 2.3 Pertinência a uma linguagem finita

A linguagem `L` é representada por uma lista de cadeias. A operação verifica se a cadeia de entrada aparece nessa lista.

- melhor caso: Ω(1), quando a cadeia está na primeira posição;
- caso médio: Θ(|L|);
- pior caso: O(|L|);
- espaço adicional: O(1).

O custo de comparar as próprias cadeias também depende de seus comprimentos. Um conjunto (`set`) poderia tornar a busca média constante.

### 2.4 Concatenação

A concatenação cria uma nova cadeia contendo a primeira entrada seguida da segunda.

- tempo: Θ(n + r), sendo `r` o tamanho da segunda cadeia;
- espaço: Θ(n + r), pois uma nova cadeia é produzida.

Depois da concatenação, as duas entradas também são validadas em relação ao alfabeto.

### 2.5 Geração de Σ* e Σ+

Como Σ* e Σ+ são conjuntos infinitos para alfabetos não vazios, o programa gera apenas as cadeias até o tamanho `m` definido pelo usuário. Para um alfabeto com `k` símbolos, a quantidade gerada em Σ* é:

`1 + k + k² + ... + kᵐ`

Para `k > 1`, esse total cresce na ordem de Θ(kᵐ). Portanto:

- melhor caso: Ω(1), quando `m = 0`;
- caso médio: Θ(kᵐ · m);
- pior caso: O(kᵐ · m);
- espaço: Θ(kᵐ · m).

O fator `m` aparece porque cada resultado pode conter até `m` símbolos. A implementação limita a geração a 5.000 cadeias, evitando consumo excessivo de memória e travamento da interface.

## 3. Máquina de Turing para reconhecer w#w

A Máquina de Turing reconhece cadeias em que o trecho depois de `#` é idêntico ao trecho anterior. Os símbolos `0` e `1` já comparados são marcados como `X` e `Y`.

O processamento segue estas etapas:

1. localiza o primeiro símbolo ainda não marcado antes de `#`;
2. marca esse símbolo como `X` ou `Y`;
3. atravessa a fita e procura o próximo símbolo não marcado depois de `#`;
4. rejeita se os símbolos forem diferentes;
5. marca o símbolo correspondente e retorna ao início da fita;
6. aceita quando os dois lados foram completamente marcados.

Para uma entrada válida, cada símbolo do primeiro trecho provoca uma ida até o segundo trecho e um retorno ao início. São realizadas Θ(n) rodadas, e cada rodada pode percorrer Θ(n) posições. Consequentemente:

- melhor caso de uma entrada sintaticamente válida: Ω(n), quando uma incompatibilidade é encontrada cedo;
- caso médio: Θ(n²);
- pior caso: O(n²);
- espaço de trabalho da fita: O(n).

A simulação guarda uma fotografia textual da fita em cada passo para permitir a visualização na interface. Como podem existir O(n²) passos e cada fotografia ocupa O(n), o histórico completo pode chegar a O(n³) de memória. Esse custo pertence à visualização passo a passo; a Máquina de Turing conceitual usa apenas O(n) de fita.

Pelo mesmo motivo, montar todas as fotografias pode elevar o tempo da implementação visual para O(n³). Sem a produção do histórico, o processamento da máquina permanece O(n²).

## 4. Máquina de Turing para soma unária

Na soma unária, uma entrada como `000100` representa `3 + 2`. O símbolo `1` separa os dois números, enquanto a quantidade de zeros representa seus valores.

O algoritmo:

1. percorre os zeros do primeiro número;
2. substitui o separador `1` por `0`;
3. avança até o fim da entrada;
4. remove um zero excedente;
5. encerra com `00000`, que representa o resultado 5.

A fita é percorrida um número constante de vezes:

- melhor caso: Ω(n), pois a entrada precisa ser validada e percorrida;
- caso médio: Θ(n);
- pior caso: O(n);
- espaço de trabalho da fita: O(n).

Assim como no reconhecedor de `w#w`, a versão visual salva uma configuração completa da fita por passo. Por isso, o histórico da interface usa O(n²) de memória, embora a máquina conceitual utilize O(n).

A criação dessas fotografias também faz a implementação visual consumir Θ(n²) de tempo. Sem o histórico, a soma permanece Θ(n).

## 5. Serviço de análise de complexidade

O serviço de complexidade mantém um catálogo com nome, descrição, melhor caso, caso médio, pior caso e uso de espaço dos algoritmos. A consulta é feita por identificador em um dicionário Python.

- consulta de um algoritmo: Θ(1) em média e O(a) no pior caso teórico de colisões;
- cálculo da estimativa numérica: Θ(1);
- espaço adicional por consulta: Θ(1);
- listagem de todos os algoritmos: Θ(a), sendo `a` a quantidade cadastrada.

As estimativas numéricas servem para demonstrar a taxa de crescimento. Elas não representam segundos nem substituem medições reais, pois o tempo observado também depende do computador, da linguagem, da implementação e dos dados de entrada.

## 6. Resumo das complexidades

| Algoritmo | Melhor caso | Caso médio | Pior caso | Espaço principal |
|---|---:|---:|---:|---:|
| Normalização do alfabeto | Ω(k) | Θ(k²) | O(k²) | O(k) |
| Segmentação da cadeia | Ω(n) | Θ(n·k·l + n²) | O(n·k·l + n²) | O(n²) |
| Busca em linguagem finita | Ω(1) | Θ(|L|) | O(|L|) | O(1) |
| Concatenação | Ω(n + r) | Θ(n + r) | O(n + r) | Θ(n + r) |
| Geração limitada de Σ* e Σ+ | Ω(1) | Θ(kᵐ·m) | O(kᵐ·m) | Θ(kᵐ·m) |
| MT para reconhecer w#w | Ω(n) | Θ(n²) | O(n²) | O(n) sem histórico |
| MT para soma unária | Ω(n) | Θ(n) | O(n) | O(n) sem histórico |
| Consulta no catálogo de complexidade | Ω(1) | Θ(1) | O(a) teórico | O(1) |

## 7. Conclusão

As operações simples de validação, concatenação e soma unária apresentam crescimento linear ou próximo do linear. A geração de Σ* e Σ+ possui crescimento exponencial, pois precisa materializar todas as combinações possíveis. Já a Máquina de Turing para `w#w` apresenta comportamento quadrático porque compara os símbolos por meio de várias travessias da fita. O armazenamento do histórico aumenta o consumo de memória, mas é necessário para cumprir o requisito didático de mostrar a execução passo a passo.
