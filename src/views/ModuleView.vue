<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import ModuleIcon from '../components/ModuleIcon.vue'
import { modules } from '../data/modules'

const route = useRoute()
const currentModule = computed(() => modules.find((item) => item.slug === route.params.slug))
</script>

<template>
  <div v-if="currentModule" class="page module-page">
    <AppHeader />

    <main class="shell module-page__main">
      <RouterLink class="back-link" to="/">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M16 10H5m4-4-4 4 4 4" />
        </svg>
        Voltar aos módulos
      </RouterLink>

      <section class="module-intro" :class="`module-intro--${currentModule.color}`">
        <div class="module-intro__icon">
          <ModuleIcon :name="currentModule.icon" />
        </div>
        <span class="eyebrow"><i></i> Módulo {{ currentModule.number }}</span>
        <h1>{{ currentModule.title }}</h1>
        <p>{{ currentModule.description }}</p>

        <div class="module-intro__topics">
          <span v-for="topic in currentModule.topics" :key="topic">{{ topic }}</span>
        </div>
      </section>

      <section class="module-placeholder" aria-labelledby="placeholder-title">
        <div class="module-placeholder__art" aria-hidden="true">
          <div class="code-line code-line--long"></div>
          <div class="code-line code-line--short"></div>
          <div class="code-line code-line--medium"></div>
          <span>{ }</span>
        </div>
        <div>
          <span class="module-placeholder__badge">Próxima etapa</span>
          <h2 id="placeholder-title">Ambiente de operações</h2>
          <p>
            A entrada neste módulo já está pronta. Os formulários, simulações e resultados
            serão implementados na próxima fase do projeto.
          </p>
        </div>
      </section>
    </main>
  </div>
</template>
