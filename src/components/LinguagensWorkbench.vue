<script setup>
import { computed, ref } from 'vue'

const alphabetText = ref('a,b')
const languageText = ref('abba,ab,a')
const word = ref('abba')
const concatenate = ref('ba')
const maxLength = ref(3)
const result = ref(null)
const error = ref('')
const busy = ref(false)
const alphabet = computed(() => alphabetText.value.split(',').map((item) => item.trim()).filter(Boolean))
const language = computed(() => languageText.value.split(',').map((item) => item.trim()).filter(Boolean))

async function analyze() {
  busy.value = true
  error.value = ''
  result.value = null
  try {
    const response = await fetch('http://localhost:8000/api/linguagens/analisar', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ alfabeto: alphabet.value, linguagem: language.value, cadeia: word.value, concatenar_com: concatenate.value, tamanho_fechamento: Number(maxLength.value) }),
    })
    const data = await response.json()
    if (!response.ok) throw new Error(data.erro || 'Não foi possível analisar a linguagem.')
    result.value = data
  } catch (reason) { error.value = reason.message } finally { busy.value = false }
}
</script>

<template>
  <section class="python-workbench">
    <div class="workbench-heading"><div><span class="kicker">LINGUAGENS FORMAIS</span><h2>Explore alfabetos e cadeias</h2><p>Defina Σ e L, valide uma cadeia e gere fechamentos finitos.</p></div><button class="button primary" :disabled="busy" @click="analyze">{{ busy ? 'Analisando…' : 'Analisar' }}</button></div>
    <div class="workbench-grid">
      <div class="panel form-panel">
        <label><span>Alfabeto Σ (separado por vírgulas)</span><input v-model="alphabetText" placeholder="a,b" /></label>
        <label><span>Linguagem L (cadeias separadas por vírgulas)</span><input v-model="languageText" placeholder="abba,ab,a" /></label>
        <label><span>Cadeia de entrada</span><input v-model="word" placeholder="abba" @keyup.enter="analyze" /></label>
        <label><span>Concatenar com</span><input v-model="concatenate" placeholder="ba" /></label>
        <label><span>Gerar Σ* e Σ+ até o tamanho</span><input v-model.number="maxLength" type="number" min="0" max="8" /></label>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
      <div class="panel result-panel">
        <div v-if="result" class="results">
          <div class="status" :class="result.cadeia_valida ? 'accepted' : 'rejected'"><strong>{{ result.cadeia_valida ? 'Cadeia válida para Σ' : 'Cadeia inválida para Σ' }}</strong><span>Tamanho: {{ result.tamanho }}</span></div>
          <div class="metric"><span>Pertence a L?</span><strong>{{ result.pertence_linguagem ? 'Sim' : 'Não' }}</strong></div>
          <div class="metric"><span>Concatenação</span><code>{{ result.concatenacao || 'ε' }}</code></div>
          <div><h3>Σ*</h3><div class="chips"><code v-for="item in result.sigma_estrela" :key="`s-${item}`">{{ item }}</code></div></div>
          <div><h3>Σ+</h3><div class="chips"><code v-for="item in result.sigma_mais" :key="`p-${item}`">{{ item }}</code></div></div>
        </div>
        <p v-else class="empty">Preencha os dados e clique em Analisar.</p>
      </div>
    </div>
    <p class="footnote">Σ* inclui a cadeia vazia ε; Σ+ contém apenas cadeias com pelo menos um símbolo. A geração é limitada para manter a interface responsiva.</p>
  </section>
</template>

<style scoped>
.python-workbench{margin-top:34px;color:#14213d}.workbench-heading{display:flex;justify-content:space-between;align-items:center;gap:18px;margin-bottom:18px}.kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#765ee8}.workbench-heading h2{margin:5px 0;font-size:25px}.workbench-heading p{margin:0;color:#788198;font-size:13px}.workbench-grid{display:grid;grid-template-columns:minmax(280px,.8fr) minmax(0,1.2fr);gap:16px}.panel{background:#fff;border:1px solid #e9eaf2;border-radius:17px;box-shadow:0 8px 28px #25315a0a;padding:18px}.form-panel{display:grid;gap:12px}.form-panel label{display:grid;gap:6px}.form-panel span,.metric span{font-size:11px;color:#7d8497;font-weight:700}.form-panel input{height:39px;border:1px solid #e6e7ef;border-radius:8px;padding:0 11px;font:inherit;font-size:13px}.button{border:1px solid #6d4aff;border-radius:9px;padding:10px 16px;font-weight:700;cursor:pointer}.primary{background:#6d4aff;color:#fff}.button:disabled{opacity:.55}.results{display:grid;gap:15px}.status{display:flex;justify-content:space-between;padding:12px;border-radius:10px;font-size:12px}.accepted{background:#edf9f1;color:#187443}.rejected{background:#fff1f0;color:#b53b38}.metric{display:flex;justify-content:space-between;border-bottom:1px solid #eff0f5;padding-bottom:10px}.result-panel h3{font-size:12px;margin:0 0 8px}.chips{display:flex;gap:6px;flex-wrap:wrap;max-height:130px;overflow:auto}.chips code{background:#f5f2ff;color:#6045df;padding:5px 7px;border-radius:6px;font-size:11px}.empty,.footnote{color:#9299aa;font-size:11px}.error{color:#b53b38;font-size:12px}@media(max-width:760px){.workbench-grid{grid-template-columns:1fr}.workbench-heading{align-items:flex-start;flex-direction:column}}
</style>

