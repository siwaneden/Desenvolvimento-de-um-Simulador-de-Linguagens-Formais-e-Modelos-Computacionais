<script setup>
import { computed, ref } from 'vue'

const type = ref('copia')
const input = ref('010#010')
const result = ref(null)
const error = ref('')
const busy = ref(false)
const selectedStep = ref(0)
const currentConfiguration = computed(() => result.value?.historico?.[selectedStep.value])

function changeType() {
  input.value = type.value === 'copia' ? '010#010' : '000100'
  result.value = null
  error.value = ''
}
async function simulate() {
  busy.value = true
  error.value = ''
  result.value = null
  try {
    const response = await fetch('http://localhost:8000/api/turing/simular', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tipo: type.value, entrada: input.value }) })
    const data = await response.json()
    if (!response.ok) throw new Error(data.erro || 'Não foi possível simular a máquina.')
    result.value = data
    selectedStep.value = data.historico.length - 1
  } catch (reason) { error.value = reason.message } finally { busy.value = false }
}
</script>

<template>
  <section class="turing-workbench">
    <div class="heading"><div><span class="kicker">MÁQUINA DE TURING</span><h2>Simule a fita passo a passo</h2><p>Escolha uma das duas opções exigidas pelo enunciado.</p></div></div>
    <div class="layout">
      <aside class="panel controls">
        <label><span>Máquina</span><select v-model="type" @change="changeType"><option value="copia">Opção A — reconhecer w#w</option><option value="soma">Opção B — soma unária</option></select></label>
        <label><span>Entrada</span><input v-model="input" @keyup.enter="simulate" /></label>
        <p class="hint">{{ type === 'copia' ? 'Exemplo: 010#010' : 'Exemplo: 000100 representa 3 + 2' }}</p>
        <button class="button" :disabled="busy" @click="simulate">{{ busy ? 'Simulando…' : '▶ Executar máquina' }}</button>
        <p v-if="error" class="error">{{ error }}</p>
        <div v-if="result" class="summary" :class="result.aceita ? 'accepted' : 'rejected'"><strong>{{ result.aceita ? 'ACEITA' : 'REJEITA' }}</strong><span>{{ result.mensagem }}</span><span v-if="type === 'soma'">Resultado: {{ result.saida }}</span></div>
      </aside>
      <div class="panel simulation">
        <template v-if="currentConfiguration">
          <div class="simulation-head"><strong>{{ currentConfiguration.estado }}</strong><span>Passo {{ selectedStep }} de {{ result.passos }}</span></div>
          <div class="tape"><div v-for="(symbol, index) in currentConfiguration.fita" :key="index" class="cell" :class="{ active: index === currentConfiguration.cabeca }"><i v-if="index === currentConfiguration.cabeca">▼</i>{{ symbol }}</div></div>
          <input v-model.number="selectedStep" class="step-range" type="range" min="0" :max="result.historico.length - 1" />
          <p class="action">{{ currentConfiguration.acao }}</p>
          <div class="step-buttons"><button :disabled="selectedStep === 0" @click="selectedStep--">← Anterior</button><button :disabled="selectedStep >= result.historico.length - 1" @click="selectedStep++">Próximo →</button></div>
        </template>
        <p v-else class="empty">A fita e o cabeçote aparecerão aqui.</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.turing-workbench{margin-top:34px;color:#14213d}.heading{margin-bottom:18px}.kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#27a8ad}.heading h2{margin:5px 0;font-size:25px}.heading p{margin:0;color:#788198;font-size:13px}.layout{display:grid;grid-template-columns:300px minmax(0,1fr);gap:16px}.panel{background:#fff;border:1px solid #e9eaf2;border-radius:17px;box-shadow:0 8px 28px #25315a0a;padding:18px}.controls{display:grid;align-content:start;gap:13px}.controls label{display:grid;gap:6px}.controls label span{font-size:11px;font-weight:700;color:#7d8497}.controls input,.controls select{height:39px;border:1px solid #e6e7ef;border-radius:8px;padding:0 10px;background:#fff}.hint,.empty,.action{font-size:11px;color:#9299aa;margin:0}.button{border:0;border-radius:9px;padding:11px;background:#159da4;color:#fff;font-weight:800;cursor:pointer}.summary{display:grid;gap:5px;padding:12px;border-radius:9px;font-size:11px}.accepted{background:#edf9f1;color:#187443}.rejected{background:#fff1f0;color:#b53b38}.error{font-size:11px;color:#b53b38}.simulation{min-height:260px;display:grid;align-content:center;gap:18px;overflow:hidden}.simulation-head{display:flex;justify-content:space-between;color:#657087;font-size:12px}.tape{display:flex;justify-content:center;overflow-x:auto;padding:28px 0 8px}.cell{position:relative;min-width:45px;height:45px;border:1px solid #cfd5e2;display:grid;place-items:center;font:700 16px monospace;margin-left:-1px}.cell.active{background:#eafafa;border-color:#159da4;color:#087b81;z-index:1}.cell i{position:absolute;top:-25px;font-style:normal;color:#159da4}.step-range{width:100%;accent-color:#159da4}.step-buttons{display:flex;justify-content:center;gap:8px}.step-buttons button{border:1px solid #dfe3ea;background:#fff;border-radius:8px;padding:8px 12px;cursor:pointer}.step-buttons button:disabled{opacity:.4}@media(max-width:760px){.layout{grid-template-columns:1fr}}
</style>

