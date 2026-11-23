<script setup>
import { computed, ref } from 'vue'

const alphabetText = ref('a,b')
const states = ref([
  { id: 'q0', x: 150, y: 150, initial: true, final: false },
  { id: 'q1', x: 390, y: 150, initial: false, final: true },
])
const transitions = ref([{ from: 'q0', symbol: 'a', to: 'q1' }])
const word = ref('a')
const result = ref(null)
const busy = ref(false)
const apiError = ref('')
const selectedFrom = ref('q0')
const selectedTo = ref('q1')
const transitionSymbol = ref('a')
const dragging = ref(null)
const connectMode = ref(false)
const connectOrigin = ref('')
const alphabet = computed(() => alphabetText.value.split(',').map((item) => item.trim()).filter(Boolean))
const initialState = computed(() => states.value.find((state) => state.initial)?.id || '—')
const finalStates = computed(() => states.value.filter((state) => state.final).map((state) => state.id))

function addState() {
  let index = states.value.length
  let id = `q${index}`
  while (states.value.some((state) => state.id === id)) id = `q${++index}`
  states.value.push({ id, x: 110 + (states.value.length % 3) * 180, y: 100 + Math.floor(states.value.length / 3) * 150, initial: states.value.length === 0, final: false })
  selectedFrom.value = id
  selectedTo.value = id
}
function removeState(id) {
  if (states.value.length <= 1) return
  states.value = states.value.filter((state) => state.id !== id)
  transitions.value = transitions.value.filter((item) => item.from !== id && item.to !== id)
  if (selectedFrom.value === id) selectedFrom.value = states.value[0].id
  if (selectedTo.value === id) selectedTo.value = states.value[0].id
}
function setInitial(id) {
  states.value.forEach((state) => { state.initial = state.id === id })
}
function stateClick(id) {
  if (!connectMode.value) return
  if (!connectOrigin.value) connectOrigin.value = id
  else {
    selectedFrom.value = connectOrigin.value
    selectedTo.value = id
    connectOrigin.value = ''
    connectMode.value = false
    document.getElementById('afd-symbol')?.focus()
  }
}
function addTransition() {
  if (!selectedFrom.value || !selectedTo.value || !transitionSymbol.value.trim()) return
  transitions.value.push({ from: selectedFrom.value, to: selectedTo.value, symbol: transitionSymbol.value.trim() })
  result.value = null
}
function pointFor(id) { return states.value.find((state) => state.id === id) }
function edgeGeometry(item) {
  const from = pointFor(item.from)
  const to = pointFor(item.to)
  if (!from || !to) return { path: '', label: { x: 0, y: 0 } }

  if (from.id === to.id) {
    const siblings = transitions.value.filter((edge) => edge.from === item.from && edge.to === item.to)
    const index = siblings.indexOf(item)
    const lift = 82 + index * 28
    return {
      path: `M ${from.x - 17} ${from.y - 27} C ${from.x - 70 - index * 12} ${from.y - lift}, ${from.x + 70 + index * 12} ${from.y - lift}, ${from.x + 17} ${from.y - 27}`,
      label: { x: from.x, y: from.y - lift - 5 },
    }
  }

  const dx = to.x - from.x
  const dy = to.y - from.y
  const length = Math.hypot(dx, dy) || 1
  const ux = dx / length
  const uy = dy / length
  const nx = -uy
  const ny = ux
  const siblings = transitions.value.filter((edge) =>
    (edge.from === item.from && edge.to === item.to) ||
    (edge.from === item.to && edge.to === item.from),
  )
  const index = siblings.indexOf(item)
  const offset = (index - (siblings.length - 1) / 2) * 42
  const startX = from.x + ux * 29
  const startY = from.y + uy * 29
  const endX = to.x - ux * 31
  const endY = to.y - uy * 31
  const controlX = (startX + endX) / 2 + nx * offset
  const controlY = (startY + endY) / 2 + ny * offset

  return {
    path: `M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`,
    label: {
      x: 0.25 * startX + 0.5 * controlX + 0.25 * endX,
      y: 0.25 * startY + 0.5 * controlY + 0.25 * endY - 10,
    },
  }
}
function edgePath(item) { return edgeGeometry(item).path }
function edgeLabel(item) { return edgeGeometry(item).label }
function canvasPoint(event) {
  const svg = event.currentTarget.ownerSVGElement || event.currentTarget
  const rect = svg.getBoundingClientRect()
  return { x: (event.clientX - rect.left) * 800 / rect.width, y: (event.clientY - rect.top) * 450 / rect.height }
}
function startDrag(event, state) {
  if (connectMode.value) return
  const point = canvasPoint(event)
  dragging.value = { id: state.id, dx: point.x - state.x, dy: point.y - state.y }
  event.currentTarget.setPointerCapture(event.pointerId)
}
function moveDrag(event) {
  if (!dragging.value) return
  const state = pointFor(dragging.value.id)
  if (!state) return
  const point = canvasPoint(event)
  state.x = Math.max(35, Math.min(760, point.x - dragging.value.dx))
  state.y = Math.max(45, Math.min(400, point.y - dragging.value.dy))
}
function stopDrag() { dragging.value = null }
async function simulate() {
  busy.value = true
  result.value = null
  apiError.value = ''
  const params = new URLSearchParams()
  params.set('alfabeto', alphabet.value.join(','))
  params.set('estados', states.value.map((s) => `${s.id}|${s.initial}|${s.final}`).join(';'))
  params.set('transicoes', transitions.value.map((t) => `${t.from}|${t.symbol}|${t.to}`).join(';'))
  params.set('palavra', word.value)
  try {
    const response = await fetch('http://localhost:8080/api/afd/simular', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }, body: params })
    const data = await response.json()
    if (!response.ok) throw new Error(data.erro || 'Não foi possível simular o autômato.')
    result.value = data
  } catch (error) {
    apiError.value = `${error.message}`// Verifique se o servidor Java está em execução na porta 8080.`
  } finally { busy.value = false }
}
function loadExample() {
  alphabetText.value = 'a,b'
  states.value = [
    { id: 'q0', x: 150, y: 150, initial: true, final: false },
    { id: 'q1', x: 390, y: 150, initial: false, final: true },
  ]
  transitions.value = [
    { from: 'q0', to: 'q0', symbol: 'b' }, { from: 'q0', to: 'q1', symbol: 'a' },
    { from: 'q1', to: 'q1', symbol: 'a' }, { from: 'q1', to: 'q0', symbol: 'b' },
  ]
  word.value = 'aab'
  result.value = null
}
</script>

<template>
  <section class="afd-workbench">
    <div class="afd-workbench__heading">
      <div><span class="afd-kicker">EDITOR DE AUTÔMATO</span><h2>Monte seu AFD</h2><p>Crie estados, conecte transições e teste uma palavra.</p></div>
      <button class="afd-button afd-button--quiet" @click="loadExample">Carregar exemplo</button>
    </div>

    <div class="afd-layout">
      <div class="afd-main-panel">
        <div class="afd-toolbar">
          <button class="afd-button afd-button--primary" @click="addState">＋ Novo estado</button>
          <button class="afd-button" :class="{ 'is-active': connectMode }" @click="connectMode = !connectMode; connectOrigin = ''">↗ {{ connectMode ? 'Selecione a origem' : 'Conectar estados' }}</button>
          <span class="afd-hint">{{ connectMode ? 'Clique no estado de origem e depois no destino.' : 'Arraste os estados para organizar o diagrama.' }}</span>
        </div>
        <div class="afd-canvas-wrap">
          <svg class="afd-canvas" viewBox="0 0 800 450" role="img" aria-label="Diagrama do autômato finito determinístico" @pointermove="moveDrag" @pointerup="stopDrag" @pointercancel="stopDrag">
            <defs><marker id="afd-arrow" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#8b7bea" /></marker></defs>
            <g v-for="(item, index) in transitions" :key="`${item.from}-${item.symbol}-${item.to}-${index}`">
              <path :d="edgePath(item)" class="afd-edge" marker-end="url(#afd-arrow)" />
              <text :x="edgeLabel(item).x" :y="edgeLabel(item).y" class="afd-edge-label">{{ item.symbol }}</text>
            </g>
            <g v-for="state in states" :key="state.id" class="afd-state" :class="{ 'is-final': state.final, 'is-origin': connectOrigin === state.id }" @pointerdown.stop="startDrag($event, state)" @click.stop="stateClick(state.id)">
              <circle v-if="state.initial" :cx="state.x - 47" :cy="state.y" r="3" fill="#6d4aff" />
              <path v-if="state.initial" :d="`M ${state.x - 72} ${state.y} L ${state.x - 31} ${state.y}`" class="afd-start-arrow" />
              <circle :cx="state.x" :cy="state.y" r="28" class="afd-state-circle" />
              <circle v-if="state.final" :cx="state.x" :cy="state.y" r="22" class="afd-state-inner" />
              <text :x="state.x" :y="state.y + 5" class="afd-state-name">{{ state.id }}</text>
            </g>
            <text v-if="!states.length" x="400" y="220" text-anchor="middle" class="afd-empty">Adicione um estado para começar</text>
          </svg>
          <div class="afd-canvas-legend"><span><i class="legend-initial"></i> Inicial</span><span><i class="legend-final"></i> Final</span><span><i class="legend-normal"></i> Estado</span></div>
        </div>
        <div class="afd-state-list"><div class="afd-subheading">Estados</div><div class="afd-state-chips"><div v-for="state in states" :key="state.id" class="afd-state-chip"><strong>{{ state.id }}</strong><span v-if="state.initial">Inicial</span><span v-if="state.final">Final</span><button aria-label="Remover estado" @click="removeState(state.id)">×</button></div></div></div>
      </div>

      <aside class="afd-side-panel">
        <section class="afd-config-section"><div class="afd-subheading">Configuração</div>
          <label class="afd-field"><span>Alfabeto (separe por vírgulas)</span><input v-model="alphabetText" placeholder="a,b" /></label>
          <div class="afd-summary"><span>Estado inicial</span><strong>{{ initialState }}</strong><span>Estados finais</span><strong>{{ finalStates.length ? finalStates.join(', ') : 'Nenhum' }}</strong></div>
          <label class="afd-field"><span>Definir estado inicial</span><select :value="initialState === '—' ? '' : initialState" @change="setInitial($event.target.value)"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <div class="afd-final-options"><label v-for="state in states" :key="state.id"><input v-model="state.final" type="checkbox" /> {{ state.id }} é final</label></div>
        </section>
        <section class="afd-config-section"><div class="afd-subheading">Nova transição</div>
          <label class="afd-field"><span>Origem</span><select v-model="selectedFrom"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <label class="afd-field"><span>Símbolo</span><input id="afd-symbol" v-model="transitionSymbol" placeholder="a" @keyup.enter="addTransition" /></label>
          <label class="afd-field"><span>Destino</span><select v-model="selectedTo"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <button class="afd-button afd-button--primary afd-full" :disabled="!states.length" @click="addTransition">Adicionar transição</button>
          <div class="afd-transition-list"><div v-for="(item, index) in transitions" :key="index" class="afd-transition-row"><code>{{ item.from }} —{{ item.symbol }}→ {{ item.to }}</code><button @click="transitions.splice(index, 1)">×</button></div><p v-if="!transitions.length" class="afd-muted">Nenhuma transição cadastrada.</p></div>
        </section>
        <section class="afd-config-section afd-simulate"><div class="afd-subheading">Testar palavra</div><label class="afd-field"><span>Palavra de entrada</span><input v-model="word" placeholder="Ex.: aab" @keyup.enter="simulate" /></label><button class="afd-button afd-button--primary afd-full" :disabled="busy" @click="simulate">{{ busy ? 'Simulando…' : '▶ Simular autômato' }}</button>
          <div v-if="result" class="afd-result" :class="result.aceita ? 'is-accepted' : 'is-rejected'"><strong>{{ result.aceita ? 'Palavra aceita!' : 'Palavra rejeitada' }}</strong><span>{{ result.mensagem }}</span></div>
          <p v-if="apiError" class="afd-error">{{ apiError }}</p>
        </section>
      </aside>
    </div>
    <p class="afd-footnote">A simulação é processada pelo backend Java. No AFD, cada estado deve ter no máximo uma transição para cada símbolo do alfabeto.</p>
  </section>
</template>

<style scoped>
.afd-workbench{margin-top:34px;color:#14213d}.afd-workbench__heading{display:flex;align-items:center;justify-content:space-between;gap:18px;margin-bottom:18px}.afd-kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#765ee8}.afd-workbench h2{font:700 25px Manrope,sans-serif;margin:5px 0}.afd-workbench__heading p{margin:0;color:#788198;font-size:13px}.afd-layout{display:grid;grid-template-columns:minmax(0,1.65fr) minmax(270px,.85fr);gap:16px;align-items:start}.afd-main-panel,.afd-side-panel{background:#fff;border:1px solid #e9eaf2;border-radius:17px;box-shadow:0 8px 28px #25315a0a;overflow:hidden}.afd-toolbar{display:flex;align-items:center;gap:9px;padding:13px 15px;border-bottom:1px solid #eff0f5;flex-wrap:wrap}.afd-button{border:1px solid #e3e4ed;background:#fff;color:#4d5670;border-radius:9px;padding:9px 12px;font-size:12px;font-weight:700;cursor:pointer}.afd-button:hover,.afd-button.is-active{border-color:#b7a9ff;background:#f5f2ff;color:#6045df}.afd-button--primary{background:#6d4aff;border-color:#6d4aff;color:#fff}.afd-button--primary:hover{background:#5637e6;color:#fff}.afd-button:disabled{opacity:.55;cursor:not-allowed}.afd-button--quiet{background:#f8f7ff;border-color:#eeeaff;color:#654de1}.afd-hint{font-size:11px;color:#9299aa}.afd-canvas-wrap{position:relative;background-color:#fcfcff;background-image:radial-gradient(#dfe1ed 1px,transparent 1px);background-size:19px 19px}.afd-canvas{display:block;width:100%;height:auto;min-height:300px;touch-action:none}.afd-edge{stroke:#9888ed;stroke-width:2;fill:none}.afd-edge-label{font-size:13px;font-weight:800;fill:#654de1;text-anchor:middle;paint-order:stroke;stroke:#fcfcff;stroke-width:5px;stroke-linejoin:round}.afd-state{cursor:grab}.afd-state:active{cursor:grabbing}.afd-state-circle{fill:#fff;stroke:#a99bf0;stroke-width:2.5;filter:drop-shadow(0 3px 5px #3c2d8117)}.afd-state-inner{fill:none;stroke:#a99bf0;stroke-width:1.7}.afd-state-name{font-size:13px;font-weight:800;text-anchor:middle;fill:#43377e;pointer-events:none}.afd-state.is-origin .afd-state-circle{stroke:#6d4aff;stroke-width:4}.afd-start-arrow{stroke:#6d4aff;stroke-width:2;fill:none}.afd-canvas-legend{position:absolute;bottom:10px;right:12px;display:flex;gap:12px;background:#ffffffdf;padding:7px 9px;border-radius:8px;font-size:10px;color:#788198}.afd-canvas-legend span{display:flex;align-items:center;gap:5px}.afd-canvas-legend i{width:8px;height:8px;border-radius:50%;display:inline-block}.legend-initial{background:#6d4aff}.legend-final{border:2px solid #a99bf0}.legend-normal{background:#d9d3fa}.afd-state-list{padding:14px 16px;border-top:1px solid #eff0f5}.afd-subheading{font-size:12px;font-weight:800;color:#303b58;margin-bottom:11px}.afd-state-chips{display:flex;flex-wrap:wrap;gap:7px}.afd-state-chip{display:flex;align-items:center;gap:7px;padding:6px 8px;border:1px solid #ececf3;border-radius:8px;font-size:10px;color:#7d8497}.afd-state-chip strong{color:#4d3d9d}.afd-state-chip button,.afd-transition-row button{border:0;background:none;color:#a0a5b2;font-size:17px;cursor:pointer;line-height:1}.afd-side-panel{padding:0 15px}.afd-config-section{padding:16px 0;border-bottom:1px solid #eff0f5}.afd-config-section:last-child{border-bottom:0}.afd-field{display:grid;gap:6px;margin:10px 0}.afd-field span{font-size:10px;font-weight:700;color:#7d8497}.afd-field input,.afd-field select{width:100%;height:36px;border:1px solid #e6e7ef;border-radius:8px;padding:0 10px;color:#34405c;background:#fff;font:inherit;font-size:12px;outline:none}.afd-field input:focus,.afd-field select:focus{border-color:#a99bf0;box-shadow:0 0 0 3px #6d4aff12}.afd-summary{display:grid;grid-template-columns:1fr auto;gap:7px;padding:10px;background:#f8f7ff;border-radius:9px;font-size:10px;color:#7e8498}.afd-summary strong{color:#5543b0;text-align:right}.afd-final-options{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px}.afd-final-options label{font-size:10px;color:#6e7589}.afd-final-options input{accent-color:#6d4aff}.afd-full{width:100%;margin-top:3px}.afd-transition-list{display:grid;gap:5px;margin-top:10px}.afd-transition-row{display:flex;justify-content:space-between;align-items:center;background:#fafaff;padding:7px 9px;border-radius:7px}.afd-transition-row code{font-size:10px;color:#5745b2}.afd-muted,.afd-footnote{font-size:11px;color:#9299aa}.afd-result{display:grid;gap:3px;margin-top:11px;padding:10px;border-radius:9px;font-size:11px}.afd-result span{opacity:.8}.is-accepted{background:#edf9f1;color:#187443}.is-rejected{background:#fff1f0;color:#b53b38}.afd-error{font-size:11px;color:#b53b38;line-height:1.5}.afd-footnote{margin:12px 2px}.afd-empty{fill:#a0a5b2;font-size:14px}@media(max-width:850px){.afd-layout{grid-template-columns:1fr}.afd-side-panel{display:grid;grid-template-columns:1fr 1fr;column-gap:18px}.afd-config-section:last-child{grid-column:1/-1}}@media(max-width:560px){.afd-workbench__heading{align-items:flex-start;flex-direction:column}.afd-side-panel{display:block}.afd-canvas{min-height:220px}.afd-hint{width:100%}}
</style>
