<script setup>
import { onMounted, ref } from 'vue'

const algorithms = ref([])
const algorithm = ref('turing_copia')
const n = ref(10)
const result = ref(null)
const error = ref('')
const busy = ref(false)

async function loadAlgorithms() {
  try {
    const response = await fetch('http://localhost:8000/api/complexidade/algoritmos')
    if (!response.ok) throw new Error('Não foi possível carregar os algoritmos.')
    algorithms.value = await response.json()
  } catch (reason) { error.value = reason.message }
}
async function analyze() {
  busy.value = true
  error.value = ''
  try {
    const response = await fetch('http://localhost:8000/api/complexidade/analisar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ algoritmo: algorithm.value, n: Number(n.value) }) })
    const data = await response.json()
    if (!response.ok) throw new Error(data.erro || 'Não foi possível analisar a complexidade.')
    result.value = data
  } catch (reason) { error.value = reason.message } finally { busy.value = false }
}
onMounted(loadAlgorithms)
</script>

<template>
  <section class="complexity-workbench">
    <div class="heading"><span class="kicker">ANÁLISE ASSINTÓTICA</span><h2>Compare o custo dos algoritmos</h2><p>Consulte melhor, médio e pior caso e estime o crescimento para uma entrada n.</p></div>
    <div class="panel controls"><label><span>Algoritmo</span><select v-model="algorithm"><option v-for="item in algorithms" :key="item.id" :value="item.id">{{ item.nome }}</option></select></label><label><span>Tamanho da entrada (n)</span><input v-model.number="n" type="number" min="0" max="10000" /></label><button :disabled="busy || !algorithms.length" @click="analyze">{{ busy ? 'Calculando…' : 'Analisar complexidade' }}</button></div>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="result" class="analysis">
      <div class="panel description"><h3>{{ result.nome }}</h3><p>{{ result.descricao }}</p><div><span>n = {{ result.n }}</span><strong>≈ {{ result.operacoes_estimadas.toLocaleString('pt-BR') }} operações</strong></div></div>
      <div class="case best"><span>Melhor caso</span><strong>{{ result.melhor }}</strong></div><div class="case average"><span>Caso médio</span><strong>{{ result.medio }}</strong></div><div class="case worst"><span>Pior caso</span><strong>{{ result.pior }}</strong></div><div class="case space"><span>Espaço</span><strong>{{ result.espaco }}</strong></div>
      <section v-if="result.analise" class="panel written-analysis">
        <div class="written-heading"><span>ANÁLISE ESCRITA</span><h3>Como essa complexidade foi determinada</h3></div>
        <article><h4>Funcionamento do algoritmo</h4><p>{{ result.analise.funcionamento }}</p></article>
        <article><h4>Justificativa assintótica</h4><p>{{ result.analise.justificativa }}</p></article>
        <article><h4>Uso de memória</h4><p>{{ result.analise.memoria }}</p></article>
        <article><h4>Possível melhoria</h4><p>{{ result.analise.melhoria }}</p></article>
      </section>
      <p class="note">{{ result.observacao }}</p>
    </div>
  </section>
</template>

<style scoped>
.complexity-workbench{margin-top:34px;color:#14213d}.heading{margin-bottom:18px}.kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#2f9d61}.heading h2{margin:5px 0;font-size:25px}.heading p{margin:0;color:#788198;font-size:13px}.panel,.case{background:#fff;border:1px solid #e9eaf2;border-radius:15px;box-shadow:0 8px 28px #25315a0a}.controls{display:grid;grid-template-columns:minmax(240px,1fr) 190px auto;gap:12px;align-items:end;padding:17px}.controls label{display:grid;gap:6px}.controls span{font-size:11px;font-weight:700;color:#7d8497}.controls input,.controls select{height:39px;border:1px solid #e2e5ec;border-radius:8px;padding:0 10px;background:#fff}.controls button{height:39px;border:0;border-radius:8px;padding:0 16px;background:#248c55;color:#fff;font-weight:800;cursor:pointer}.analysis{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-top:16px}.description{grid-column:1/-1;padding:17px}.description h3{margin:0 0 5px}.description p{color:#788198;font-size:12px}.description div{display:flex;justify-content:space-between;background:#f2faf5;padding:10px;border-radius:8px;color:#237047;font-size:12px}.case{padding:16px;display:grid;gap:8px}.case span{font-size:11px;color:#7d8497}.case strong{font-size:20px}.best{border-top:3px solid #49b779}.average{border-top:3px solid #e9b949}.worst{border-top:3px solid #df6a62}.space{border-top:3px solid #6d8ddd}.written-analysis{grid-column:1/-1;padding:20px;display:grid;grid-template-columns:1fr 1fr;gap:18px}.written-heading{grid-column:1/-1;border-bottom:1px solid #edf0f2;padding-bottom:12px}.written-heading span{font-size:9px;font-weight:800;letter-spacing:.14em;color:#2f9d61}.written-heading h3{font-size:17px;margin:4px 0 0}.written-analysis article{background:#fafcfb;border-left:3px solid #79bd94;border-radius:0 8px 8px 0;padding:13px 14px}.written-analysis h4{font-size:12px;margin:0 0 7px;color:#315d45}.written-analysis p{font-size:12px;line-height:1.65;color:#667287;margin:0}.note{grid-column:1/-1;font-size:11px;color:#9299aa}.error{color:#b53b38;font-size:12px}@media(max-width:760px){.controls{grid-template-columns:1fr}.analysis{grid-template-columns:1fr 1fr}.written-analysis{grid-template-columns:1fr}}@media(max-width:480px){.analysis{grid-template-columns:1fr}}
</style>
