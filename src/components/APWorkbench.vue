<script setup>
import { computed, ref } from 'vue'

const alphabetText = ref('a,b')
const initialStack = ref('Z')
const states = ref([
  { id: 'q0', x: 125, y: 155, initial: true, final: false },
  { id: 'q1', x: 390, y: 155, initial: false, final: false },
  { id: 'q2', x: 650, y: 155, initial: false, final: true },
])
const transitions = ref([
  { from: 'q0', input: 'a', top: 'Z', replacement: 'AZ', to: 'q0' },
  { from: 'q0', input: 'a', top: 'A', replacement: 'AA', to: 'q0' },
  { from: 'q0', input: 'b', top: 'A', replacement: 'ε', to: 'q1' },
  { from: 'q1', input: 'b', top: 'A', replacement: 'ε', to: 'q1' },
  { from: 'q1', input: 'ε', top: 'Z', replacement: 'ε', to: 'q2' },
])
const word = ref('aaabbb')
const result = ref(null)
const busy = ref(false)
const apiError = ref('')
const selectedFrom = ref('q0')
const selectedTo = ref('q0')
const inputSymbol = ref('a')
const stackTop = ref('Z')
const stackReplacement = ref('AZ')
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
  states.value.push({ id, x: 100 + (states.value.length % 3) * 230, y: 100 + Math.floor(states.value.length / 3) * 145, initial: states.value.length === 0, final: false })
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
function setInitial(id) { states.value.forEach((state) => { state.initial = state.id === id }) }
function stateClick(id) {
  if (!connectMode.value) return
  if (!connectOrigin.value) connectOrigin.value = id
  else {
    selectedFrom.value = connectOrigin.value
    selectedTo.value = id
    connectOrigin.value = ''
    connectMode.value = false
    document.getElementById('ap-input-symbol')?.focus()
  }
}
function addTransition() {
  if (!selectedFrom.value || !selectedTo.value || !inputSymbol.value.trim() || !stackTop.value.trim() || !stackReplacement.value.trim()) return
  transitions.value.push({ from: selectedFrom.value, input: inputSymbol.value.trim(), top: stackTop.value.trim(), replacement: stackReplacement.value.trim(), to: selectedTo.value })
  result.value = null
}
function pointFor(id) { return states.value.find((state) => state.id === id) }
function edgeGeometry(item) {
  const from = pointFor(item.from)
  const to = pointFor(item.to)
  if (!from || !to) return { path: '', label: { x: 0, y: 0 } }
  const siblings = transitions.value.filter((edge) => edge.from === item.from && edge.to === item.to)
  const index = siblings.indexOf(item)
  if (from.id === to.id) {
    const lift = 82 + index * 27
    return {
      path: `M ${from.x - 17} ${from.y - 27} C ${from.x - 70 - index * 12} ${from.y - lift}, ${from.x + 70 + index * 12} ${from.y - lift}, ${from.x + 17} ${from.y - 27}`,
      label: { x: from.x, y: from.y - lift - 7 },
    }
  }
  const dx = to.x - from.x
  const dy = to.y - from.y
  const length = Math.hypot(dx, dy) || 1
  const ux = dx / length
  const uy = dy / length
  const nx = -uy
  const ny = ux
  const offset = (index - (siblings.length - 1) / 2) * 35
  const startX = from.x + ux * 29
  const startY = from.y + uy * 29
  const endX = to.x - ux * 31
  const endY = to.y - uy * 31
  const controlX = (startX + endX) / 2 + nx * offset
  const controlY = (startY + endY) / 2 + ny * offset
  return {
    path: `M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`,
    label: { x: 0.25 * startX + 0.5 * controlX + 0.25 * endX, y: 0.25 * startY + 0.5 * controlY + 0.25 * endY - 13 },
  }
}
function edgeLabel(item) { return `${item.input}, ${item.top}/${item.replacement}` }
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
  params.set('pilhaInicial', initialStack.value)
  params.set('estados', states.value.map((state) => `${state.id}|${state.initial}|${state.final}`).join(';'))
  params.set('transicoes', transitions.value.map((item) => `${item.from}|${item.input}|${item.top}|${item.replacement}|${item.to}`).join(';'))
  params.set('palavra', word.value)
  try {
    const response = await fetch('http://localhost:8080/api/ap/simular', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
      body: params,
    })
    const data = await response.json()
    if (!response.ok) throw new Error(data.erro || 'Não foi possível simular o Autômato com Pilha.')
    result.value = data
  } catch (error) {
    apiError.value = error.message
  } finally {
    busy.value = false
  }
}
function loadExample() {
  alphabetText.value = 'a,b'
  initialStack.value = 'Z'
  states.value = [
    { id: 'q0', x: 125, y: 155, initial: true, final: false },
    { id: 'q1', x: 390, y: 155, initial: false, final: false },
    { id: 'q2', x: 650, y: 155, initial: false, final: true },
  ]
  transitions.value = [
    { from: 'q0', input: 'a', top: 'Z', replacement: 'AZ', to: 'q0' },
    { from: 'q0', input: 'a', top: 'A', replacement: 'AA', to: 'q0' },
    { from: 'q0', input: 'b', top: 'A', replacement: 'ε', to: 'q1' },
    { from: 'q1', input: 'b', top: 'A', replacement: 'ε', to: 'q1' },
    { from: 'q1', input: 'ε', top: 'Z', replacement: 'ε', to: 'q2' },
  ]
  selectedFrom.value = 'q0'
  selectedTo.value = 'q0'
  inputSymbol.value = 'a'
  stackTop.value = 'Z'
  stackReplacement.value = 'AZ'
  word.value = 'aaabbb'
  result.value = null
  apiError.value = ''
}
</script>

<template>
  <section class="ap-workbench">
    <div class="ap-heading">
      <div><span class="ap-kicker">EDITOR DE AUTÔMATO</span><h2>Monte seu Autômato com Pilha</h2><p>Modele estados, regras de empilhamento e teste uma palavra.</p></div>
      <button class="ap-button ap-button--quiet" @click="loadExample">Carregar exemplo aⁿbⁿ</button>
    </div>

    <div class="ap-layout">
      <div class="ap-main-panel">
        <div class="ap-toolbar">
          <button class="ap-button ap-button--primary" @click="addState">＋ Novo estado</button>
          <button class="ap-button" :class="{ 'is-active': connectMode }" @click="connectMode = !connectMode; connectOrigin = ''">↗ {{ connectMode ? 'Selecione a origem' : 'Conectar estados' }}</button>
          <span class="ap-hint">{{ connectMode ? 'Clique na origem e depois no destino.' : 'Arraste os estados para organizar o diagrama.' }}</span>
        </div>
        <div class="ap-canvas-wrap">
          <svg class="ap-canvas" viewBox="0 0 800 450" role="img" aria-label="Diagrama do Autômato com Pilha" @pointermove="moveDrag" @pointerup="stopDrag" @pointercancel="stopDrag">
            <defs><marker id="ap-arrow" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#8b7bea" /></marker></defs>
            <g v-for="(item, index) in transitions" :key="`${item.from}-${item.input}-${item.top}-${item.replacement}-${item.to}-${index}`">
              <path :d="edgeGeometry(item).path" class="ap-edge" marker-end="url(#ap-arrow)" />
              <text :x="edgeGeometry(item).label.x" :y="edgeGeometry(item).label.y" class="ap-edge-label">{{ edgeLabel(item) }}</text>
            </g>
            <g v-for="state in states" :key="state.id" class="ap-state" :class="{ 'is-origin': connectOrigin === state.id }" @pointerdown.stop="startDrag($event, state)" @click.stop="stateClick(state.id)">
              <path v-if="state.initial" :d="`M ${state.x - 72} ${state.y} L ${state.x - 31} ${state.y}`" class="ap-start-arrow" />
              <circle :cx="state.x" :cy="state.y" r="28" class="ap-state-circle" />
              <circle v-if="state.final" :cx="state.x" :cy="state.y" r="22" class="ap-state-inner" />
              <text :x="state.x" :y="state.y + 5" class="ap-state-name">{{ state.id }}</text>
            </g>
          </svg>
          <div class="ap-legend"><span><i class="ap-initial"></i> Inicial</span><span><i class="ap-final"></i> Final</span><span><i class="ap-normal"></i> Estado</span></div>
        </div>
        <div class="ap-state-list"><div class="ap-subheading">Estados</div><div class="ap-state-chips"><div v-for="state in states" :key="state.id" class="ap-state-chip"><strong>{{ state.id }}</strong><span v-if="state.initial">Inicial</span><span v-if="state.final">Final</span><button aria-label="Remover estado" @click="removeState(state.id)">×</button></div></div></div>
      </div>

      <aside class="ap-side-panel">
        <section class="ap-section"><div class="ap-subheading">Configuração</div>
          <label class="ap-field"><span>Alfabeto de entrada</span><input v-model="alphabetText" placeholder="a,b" /></label>
          <label class="ap-field"><span>Símbolo inicial da pilha</span><input v-model="initialStack" maxlength="1" placeholder="Z" /></label>
          <div class="ap-summary"><span>Estado inicial</span><strong>{{ initialState }}</strong><span>Estados finais</span><strong>{{ finalStates.length ? finalStates.join(', ') : 'Nenhum' }}</strong></div>
          <label class="ap-field"><span>Definir estado inicial</span><select :value="initialState === '—' ? '' : initialState" @change="setInitial($event.target.value)"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <div class="ap-final-options"><label v-for="state in states" :key="state.id"><input v-model="state.final" type="checkbox" /> {{ state.id }} é final</label></div>
        </section>
        <section class="ap-section"><div class="ap-subheading">Nova transição</div>
          <label class="ap-field"><span>Origem</span><select v-model="selectedFrom"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <label class="ap-field"><span>Entrada (símbolo ou ε)</span><input id="ap-input-symbol" v-model="inputSymbol" maxlength="1" placeholder="a ou ε" /></label>
          <label class="ap-field"><span>Topo da pilha</span><input v-model="stackTop" maxlength="1" placeholder="Z" /></label>
          <label class="ap-field"><span>Substituir topo por</span><input v-model="stackReplacement" placeholder="AZ ou ε" /></label>
          <label class="ap-field"><span>Destino</span><select v-model="selectedTo"><option v-for="state in states" :key="state.id" :value="state.id">{{ state.id }}</option></select></label>
          <button class="ap-button ap-button--primary ap-full" :disabled="!states.length" @click="addTransition">Adicionar regra</button>
          <div class="ap-transition-list"><div v-for="(item, index) in transitions" :key="index" class="ap-transition-row"><code>{{ item.from }} —({{ item.input }}, {{ item.top }}/{{ item.replacement }})→ {{ item.to }}</code><button @click="transitions.splice(index, 1); result = null">×</button></div><p v-if="!transitions.length" class="ap-muted">Nenhuma regra cadastrada.</p></div>
        </section>
        <section class="ap-section ap-simulate"><div class="ap-subheading">Testar palavra</div>
          <label class="ap-field"><span>Palavra de entrada</span><input v-model="word" placeholder="Ex.: aaabbb" @keyup.enter="simulate" /></label>
          <button class="ap-button ap-button--primary ap-full" :disabled="busy" @click="simulate">{{ busy ? 'Simulando…' : '▶ Simular autômato' }}</button>
          <div v-if="result" class="ap-result" :class="result.aceita ? 'is-accepted' : 'is-rejected'"><strong>{{ result.aceita ? 'Palavra aceita!' : 'Palavra rejeitada' }}</strong><span>{{ result.mensagem }}</span></div>
          <p v-if="apiError" class="ap-error">{{ apiError }}</p>
        </section>
      </aside>
    </div>

    <section v-if="result?.passos?.length" class="ap-trace"><div class="ap-subheading">Execução passo a passo</div><ol><li v-for="(step, index) in result.passos" :key="index">{{ step }}</li></ol></section>
    <p class="ap-footnote">Este módulo implementa AP determinístico com aceitação por estado final. ε representa leitura vazia ou ausência de inserção na pilha. Na substituição, o primeiro caractere informado passa a ser o novo topo.</p>
  </section>
</template>

<style scoped>
.ap-workbench{margin-top:34px;color:#14213d}.ap-heading{display:flex;align-items:center;justify-content:space-between;gap:18px;margin-bottom:18px}.ap-kicker{font-size:10px;font-weight:800;letter-spacing:.13em;color:#765ee8}.ap-workbench h2{font:700 25px Manrope,sans-serif;margin:5px 0}.ap-heading p{margin:0;color:#788198;font-size:13px}.ap-layout{display:grid;grid-template-columns:minmax(0,1.65fr) minmax(280px,.85fr);gap:16px;align-items:start}.ap-main-panel,.ap-side-panel,.ap-trace{background:#fff;border:1px solid #e9eaf2;border-radius:17px;box-shadow:0 8px 28px #25315a0a;overflow:hidden}.ap-toolbar{display:flex;align-items:center;gap:9px;padding:13px 15px;border-bottom:1px solid #eff0f5;flex-wrap:wrap}.ap-button{border:1px solid #e3e4ed;background:#fff;color:#4d5670;border-radius:9px;padding:9px 12px;font-size:12px;font-weight:700;cursor:pointer}.ap-button:hover,.ap-button.is-active{border-color:#b7a9ff;background:#f5f2ff;color:#6045df}.ap-button--primary{background:#6d4aff;border-color:#6d4aff;color:#fff}.ap-button--primary:hover{background:#5637e6;color:#fff}.ap-button:disabled{opacity:.55;cursor:not-allowed}.ap-button--quiet{background:#f8f7ff;border-color:#eeeaff;color:#654de1}.ap-hint{font-size:11px;color:#9299aa}.ap-canvas-wrap{position:relative;background-color:#fcfcff;background-image:radial-gradient(#dfe1ed 1px,transparent 1px);background-size:19px 19px}.ap-canvas{display:block;width:100%;height:auto;min-height:300px;touch-action:none}.ap-edge{stroke:#9888ed;stroke-width:2;fill:none}.ap-edge-label{font-size:11px;font-weight:800;fill:#654de1;text-anchor:middle;paint-order:stroke;stroke:#fcfcff;stroke-width:5px;stroke-linejoin:round}.ap-state{cursor:grab}.ap-state:active{cursor:grabbing}.ap-state-circle{fill:#fff;stroke:#a99bf0;stroke-width:2.5;filter:drop-shadow(0 3px 5px #3c2d8117)}.ap-state-inner{fill:none;stroke:#a99bf0;stroke-width:1.7}.ap-state-name{font-size:13px;font-weight:800;text-anchor:middle;fill:#43377e;pointer-events:none}.ap-state.is-origin .ap-state-circle{stroke:#6d4aff;stroke-width:4}.ap-start-arrow{stroke:#6d4aff;stroke-width:2;fill:none}.ap-legend{position:absolute;bottom:10px;right:12px;display:flex;gap:12px;background:#ffffffdf;padding:7px 9px;border-radius:8px;font-size:10px;color:#788198}.ap-legend span{display:flex;align-items:center;gap:5px}.ap-legend i{width:8px;height:8px;border-radius:50%;display:inline-block}.ap-initial{background:#6d4aff}.ap-final{border:2px solid #a99bf0}.ap-normal{background:#d9d3fa}.ap-state-list{padding:14px 16px;border-top:1px solid #eff0f5}.ap-subheading{font-size:12px;font-weight:800;color:#303b58;margin-bottom:11px}.ap-state-chips{display:flex;flex-wrap:wrap;gap:7px}.ap-state-chip{display:flex;align-items:center;gap:7px;padding:6px 8px;border:1px solid #ececf3;border-radius:8px;font-size:10px;color:#7d8497}.ap-state-chip strong{color:#4d3d9d}.ap-state-chip button,.ap-transition-row button{border:0;background:none;color:#a0a5b2;font-size:17px;cursor:pointer;line-height:1}.ap-side-panel{padding:0 15px}.ap-section{padding:16px 0;border-bottom:1px solid #eff0f5}.ap-section:last-child{border-bottom:0}.ap-field{display:grid;gap:6px;margin:10px 0}.ap-field span{font-size:10px;font-weight:700;color:#7d8497}.ap-field input,.ap-field select{width:100%;height:36px;border:1px solid #e6e7ef;border-radius:8px;padding:0 10px;color:#34405c;background:#fff;font:inherit;font-size:12px;outline:none}.ap-field input:focus,.ap-field select:focus{border-color:#a99bf0;box-shadow:0 0 0 3px #6d4aff12}.ap-summary{display:grid;grid-template-columns:1fr auto;gap:7px;padding:10px;background:#f8f7ff;border-radius:9px;font-size:10px;color:#7e8498}.ap-summary strong{color:#5543b0;text-align:right}.ap-final-options{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px}.ap-final-options label{font-size:10px;color:#6e7589}.ap-final-options input{accent-color:#6d4aff}.ap-full{width:100%;margin-top:3px}.ap-transition-list{display:grid;gap:5px;margin-top:10px}.ap-transition-row{display:flex;justify-content:space-between;align-items:center;gap:6px;background:#fafaff;padding:7px 9px;border-radius:7px}.ap-transition-row code{font-size:10px;color:#5745b2;overflow-wrap:anywhere}.ap-muted,.ap-footnote{font-size:11px;color:#9299aa}.ap-result{display:grid;gap:3px;margin-top:11px;padding:10px;border-radius:9px;font-size:11px}.ap-result span{opacity:.8}.is-accepted{background:#edf9f1;color:#187443}.is-rejected{background:#fff1f0;color:#b53b38}.ap-error{font-size:11px;color:#b53b38;line-height:1.5}.ap-trace{margin-top:16px;padding:16px 18px}.ap-trace ol{display:grid;gap:8px;margin:0;padding-left:22px}.ap-trace li{font-size:12px;line-height:1.5;color:#68728a}.ap-footnote{margin:12px 2px;line-height:1.6}@media(max-width:850px){.ap-layout{grid-template-columns:1fr}.ap-side-panel{display:grid;grid-template-columns:1fr 1fr;column-gap:18px}.ap-section:last-child{grid-column:1/-1}}@media(max-width:560px){.ap-heading{align-items:flex-start;flex-direction:column}.ap-side-panel{display:block}.ap-canvas{min-height:220px}.ap-hint{width:100%}}
</style>
