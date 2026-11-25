import assert from 'node:assert/strict'
import test from 'node:test'
import { findDerivation } from '../src/utils/contextFreeGrammar.js'

test('finds a leftmost derivation for a^n b^n', () => {
  const result = findDerivation('S → aSb | ε', 'S', 'aabb')

  assert.equal(result.status, 'generated')
  assert.deepEqual(result.derivation.map((step) => step.form), ['S', 'aSb', 'aaSbb', 'aabb'])
  assert.equal(result.tree.symbol, 'S')
  assert.deepEqual(result.tree.children.map((node) => node.symbol), ['a', 'S', 'b'])
})

test('recognizes the empty word through epsilon production', () => {
  const result = findDerivation('S -> ε', 'S', '')

  assert.equal(result.status, 'generated')
  assert.deepEqual(result.derivation.map((step) => step.form), ['S', 'ε'])
  assert.deepEqual(result.tree.children.map((node) => node.symbol), ['ε'])
})

test('reports words that cannot be derived', () => {
  const result = findDerivation('S -> aSb | ε', 'S', 'aab')

  assert.equal(result.status, 'not-generated')
  assert.deepEqual(result.derivation, [])
})

test('rejects references to nonterminals without productions', () => {
  assert.throws(
    () => findDerivation('S -> aA', 'S', 'a'),
    /Não há produção definida para o não terminal A/,
  )
})
