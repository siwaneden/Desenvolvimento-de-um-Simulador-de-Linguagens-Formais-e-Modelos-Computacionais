const NONTERMINAL = /^[A-Z]$/
const MAX_SEARCH_STATES = 12000
const MAX_SEARCH_DEPTH = 80
const MAX_FORM_LENGTH = 80

function parseGrammar(productionsText, startSymbol) {
  const productions = new Map()
  const lines = productionsText.split(/\r?\n/).map((line) => line.trim()).filter(Boolean)

  if (!lines.length) {
    throw new Error('Informe pelo menos uma produção.')
  }

  for (const [index, line] of lines.entries()) {
    const match = line.match(/^([A-Z])\s*(?:->|→)\s*(.+)$/)
    if (!match) {
      throw new Error(`Produção inválida na linha ${index + 1}. Use, por exemplo, S → aSb | ε.`)
    }

    const left = match[1]
    const alternatives = match[2].split('|').map((item) => item.trim())
    const current = productions.get(left) || []

    for (const alternative of alternatives) {
      const normalized = alternative.replace(/\s/gu, '')
      if (!normalized) {
        throw new Error(`A produção de ${left} contém uma alternativa vazia. Escreva ε para representar a cadeia vazia.`)
      }
      if (normalized === 'ε' || normalized === 'λ') {
        current.push([])
        continue
      }
      if (/[ελ]/u.test(normalized)) {
        throw new Error('Use ε ou λ sozinho para representar a cadeia vazia.')
      }
      current.push(Array.from(normalized))
    }

    productions.set(left, current)
  }

  if (!NONTERMINAL.test(startSymbol)) {
    throw new Error('O símbolo inicial deve ser uma letra maiúscula de A a Z.')
  }
  if (!productions.has(startSymbol)) {
    throw new Error(`Não há produção definida para o símbolo inicial ${startSymbol}.`)
  }

  for (const alternatives of productions.values()) {
    for (const alternative of alternatives) {
      for (const symbol of alternative) {
        if (NONTERMINAL.test(symbol) && !productions.has(symbol)) {
          throw new Error(`Não há produção definida para o não terminal ${symbol}.`)
        }
      }
    }
  }

  return productions
}

function isTerminal(symbol, productions) {
  return !productions.has(symbol)
}

function terminalsFitTarget(form, target, productions) {
  const terminals = form.filter((symbol) => isTerminal(symbol, productions))
  if (terminals.length > target.length) return false

  let targetIndex = 0
  for (const terminal of terminals) {
    targetIndex = target.indexOf(terminal, targetIndex)
    if (targetIndex === -1) return false
    targetIndex++
  }
  return true
}

function buildDerivation(nodes, foundIndex, productions) {
  const path = []
  let index = foundIndex

  while (index !== -1) {
    const node = nodes[index]
    path.push(node)
    index = node.parent
  }

  path.reverse()
  const root = { symbol: path[0].form[0], children: null }
  const frontier = [root]
  const derivation = path.map((node) => ({
    form: node.form.join('') || 'ε',
    rule: node.rule,
  }))

  for (const node of path.slice(1)) {
    const position = frontier.findIndex((item) => productions.has(item.symbol))
    const children = node.right.length
      ? node.right.map((symbol) => ({ symbol, children: null }))
      : [{ symbol: 'ε', children: null }]
    frontier[position].children = children
    frontier.splice(position, 1, ...children)
  }

  return { derivation, tree: root }
}

export function findDerivation(productionsText, startSymbolInput, targetInput) {
  const startSymbol = startSymbolInput.trim()
  const productions = parseGrammar(productionsText, startSymbol)
  const targetText = targetInput.trim()

  if (/\s/u.test(targetText)) {
    throw new Error('A palavra-alvo não deve conter espaços.')
  }
  if (/[ελ]/u.test(targetText) && targetText !== 'ε' && targetText !== 'λ') {
    throw new Error('Use ε ou λ sozinho para representar a cadeia vazia.')
  }

  const target = Array.from(targetText === 'ε' || targetText === 'λ' ? '' : targetText)
  const nodes = [{ form: [startSymbol], parent: -1, rule: '', depth: 0 }]
  const visited = new Set([startSymbol])
  let limited = false

  for (let cursor = 0; cursor < nodes.length; cursor++) {
    const node = nodes[cursor]
    const nonterminalIndex = node.form.findIndex((symbol) => productions.has(symbol))

    if (nonterminalIndex === -1) {
      if (node.form.length === target.length && node.form.every((symbol, index) => symbol === target[index])) {
        return { status: 'generated', ...buildDerivation(nodes, cursor, productions) }
      }
      continue
    }

    if (node.depth >= MAX_SEARCH_DEPTH) {
      limited = true
      continue
    }

    const nonterminal = node.form[nonterminalIndex]
    const alternatives = productions.get(nonterminal)

    for (const alternative of alternatives) {
      const form = [
        ...node.form.slice(0, nonterminalIndex),
        ...alternative,
        ...node.form.slice(nonterminalIndex + 1),
      ]

      if (form.length > MAX_FORM_LENGTH) {
        limited = true
        continue
      }
      if (!terminalsFitTarget(form, target, productions)) continue

      const key = form.join('')
      if (visited.has(key)) continue
      if (nodes.length >= MAX_SEARCH_STATES) {
        limited = true
        break
      }

      visited.add(key)
      nodes.push({
        form,
        parent: cursor,
        rule: `${nonterminal} → ${alternative.join('') || 'ε'}`,
        right: alternative,
        depth: node.depth + 1,
      })
    }

    if (nodes.length >= MAX_SEARCH_STATES) {
      limited = true
      break
    }
  }

  return { status: limited ? 'limit' : 'not-generated', derivation: [] }
}
