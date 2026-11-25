<script setup>
import { computed, ref } from 'vue'
import { findDerivation } from '../utils/contextFreeGrammar.js'

const productions = ref('S → aSb | ε')
const startSymbol = ref('S')
const target = ref('aabb')
const result = ref(null)
const error = ref('')
const selectedStep = ref(0)

const derivation = computed(() => result.value?.derivation || [])
const currentStep = computed(() => derivation.value[selectedStep.value] || null)

function formatTree(node) {
  if (!node.children || !node.children.length) return node.symbol
  return `${node.symbol}(${node.children.map(formatTree).join(', ')})`
}

function restoreExample() {
  productions.value = 'S → aSb | ε'
  startSymbol.value = 'S'
  target.value = 'aabb'
  result.value = null
  error.value = ''
  selectedStep.value = 0
}

function derive() {
  error.value = ''
  result.value = null
  selectedStep.value = 0

  try {
    result.value = findDerivation(productions.value, startSymbol.value, target.value)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : 'Não foi possível analisar a gramática.'
  }
}
</script>

<template>
  <section class="grammar-workbench">
    <div class="heading">
      <div>
        <span class="kicker">GRAMÁTICAS LIVRES DE CONTEXTO</span>
        <h2>Construa uma gramática e derive palavras</h2>
        <p>Informe as produções e acompanhe uma derivação mais à esquerda até a palavra-alvo.</p>
      </div>
    </div>

    <div class="layout">
      <div class="panel controls">
        <label for="grammar-productions">Produções</label>
        <textarea
          id="grammar-productions"
          v-model="productions"
          rows="5"
          spellcheck="false"
          aria-describedby="grammar-format"
          placeholder="S → aSb | ε"
        />
        <p id="grammar-format" class="hint">
          Uma regra por linha; separe alternativas com |. Terminais são caracteres individuais e não terminais são letras maiúsculas de A a Z. Use ε para a cadeia vazia; a palavra-alvo não deve conter espaços.
        </p>

        <label for="grammar-start">Símbolo inicial</label>
        <input id="grammar-start" v-model="startSymbol" maxlength="1" autocomplete="off" />

        <label for="grammar-target">Palavra-alvo</label>
        <input
          id="grammar-target"
          v-model="target"
          autocomplete="off"
          placeholder="aabb ou ε"
          @keyup.enter="derive"
        />

        <div class="actions">
          <button class="button primary" @click="derive">Encontrar derivação</button>
          <button class="button secondary" @click="restoreExample">Restaurar exemplo</button>
        </div>
        <p v-if="error" class="error" role="alert">{{ error }}</p>
      </div>

      <div class="panel result-panel">
        <template v-if="result">
          <div class="status" :class="`status--${result.status}`" role="status">
            <strong v-if="result.status === 'generated'">PALAVRA GERADA</strong>
            <strong v-else-if="result.status === 'not-generated'">PALAVRA NÃO GERADA</strong>
            <strong v-else>BUSCA INTERROMPIDA</strong>
            <span v-if="result.status === 'generated'">Foi encontrada uma derivação completa para a palavra-alvo.</span>
            <span v-else-if="result.status === 'not-generated'">Nenhuma derivação para essa palavra foi encontrada com as produções informadas.</span>
            <span v-else>A busca atingiu um limite de segurança; não foi possível concluir se a palavra é gerada.</span>
          </div>

          <div v-if="currentStep" class="derivation">
            <div class="step-heading">
              <strong>Derivação passo a passo</strong>
              <span>Passo {{ selectedStep }} de {{ derivation.length - 1 }}</span>
            </div>

            <div class="sentential-form">
              <span class="form-label">{{ selectedStep === 0 ? 'Forma inicial' : 'Forma sentencial' }}</span>
              <code>{{ currentStep.form }}</code>
            </div>

            <p class="applied-rule">
              {{ selectedStep === 0 ? 'Símbolo inicial da gramática' : `Regra aplicada: ${currentStep.rule}` }}
            </p>

            <input
              v-model.number="selectedStep"
              class="step-range"
              type="range"
              min="0"
              :max="derivation.length - 1"
              aria-label="Navegar pelos passos da derivação"
            />
            <div class="step-buttons">
              <button :disabled="selectedStep === 0" @click="selectedStep--">← Anterior</button>
              <button :disabled="selectedStep >= derivation.length - 1" @click="selectedStep++">Próximo →</button>
            </div>
          </div>

          <div v-if="result.status === 'generated'" class="parse-tree">
            <strong>Árvore sintática</strong>
            <code>{{ formatTree(result.tree) }}</code>
          </div>
        </template>
        <p v-else class="empty">Defina a gramática e a palavra-alvo para visualizar uma derivação.</p>
      </div>
    </div>

    <p class="footnote">
      A busca usa derivação mais à esquerda e é limitada para manter a interface responsiva. Uma busca limitada não significa que a palavra não pertença à linguagem.
    </p>
  </section>
</template>

<style scoped>
.grammar-workbench{margin-top:34px;color:#14213d}.heading{margin-bottom:18px}.kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#d78134}.heading h2{margin:5px 0;font-size:25px}.heading p{margin:0;color:#788198;font-size:13px}.layout{display:grid;grid-template-columns:minmax(280px,.85fr) minmax(0,1.15fr);gap:16px}.panel{background:#fff;border:1px solid #e9eaf2;border-radius:17px;box-shadow:0 8px 28px #25315a0a;padding:18px}.controls{display:grid;align-content:start;gap:9px}.controls label{font-size:11px;font-weight:700;color:#7d8497}.controls textarea,.controls input{width:100%;border:1px solid #e6e7ef;border-radius:8px;padding:10px;background:#fff;color:#14213d;font:inherit;font-size:13px}.controls textarea{resize:vertical;min-height:110px;font-family:ui-monospace,SFMono-Regular,Consolas,monospace}.controls input{height:39px}.hint,.empty,.footnote{font-size:11px;color:#9299aa;line-height:1.55}.hint{margin:0 0 5px}.actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:5px}.button{border:0;border-radius:9px;padding:10px 13px;font-weight:800;cursor:pointer}.primary{background:#dc8738;color:#fff}.secondary{background:#fff4e9;color:#9b571c}.result-panel{min-height:280px;display:grid;align-content:start;gap:16px}.status{display:grid;gap:5px;padding:12px;border-radius:10px;font-size:12px}.status--generated{background:#edf9f1;color:#187443}.status--not-generated{background:#fff1f0;color:#b53b38}.status--limit{background:#fff7e8;color:#8a5a12}.derivation{display:grid;gap:15px}.step-heading{display:flex;justify-content:space-between;gap:10px;color:#657087;font-size:12px}.sentential-form{display:grid;gap:9px;padding:18px;border:1px solid #f0dfcf;border-radius:12px;background:#fffaf5}.form-label{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.08em;color:#9b754f}.sentential-form code{font-size:24px;font-weight:700;overflow-wrap:anywhere;color:#733f16}.applied-rule{margin:0;color:#788198;font-size:12px}.step-range{width:100%;accent-color:#dc8738}.step-buttons{display:flex;justify-content:center;gap:8px}.step-buttons button{border:1px solid #dfe3ea;background:#fff;border-radius:8px;padding:8px 12px;cursor:pointer}.step-buttons button:disabled{opacity:.4;cursor:default}.parse-tree{display:grid;gap:8px;padding:13px;border:1px solid #f0dfcf;border-radius:10px;background:#fffaf5}.parse-tree strong{font-size:11px;color:#9b754f}.parse-tree code{color:#733f16;overflow-wrap:anywhere;line-height:1.6}.error{margin:0;color:#b53b38;font-size:12px}.empty{align-self:center;text-align:center;margin:0}.footnote{margin:12px 0 0}@media(max-width:760px){.layout{grid-template-columns:1fr}}
</style>
