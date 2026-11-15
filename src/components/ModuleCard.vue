<script setup>
import ModuleIcon from './ModuleIcon.vue'

defineProps({
  module: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <article class="module-card" :class="`module-card--${module.color}`">
    <div class="module-card__topline">
      <span class="module-card__number">Módulo {{ module.number }}</span>
      <span class="module-card__icon">
        <ModuleIcon :name="module.icon" />
      </span>
    </div>

    <div class="module-card__content">
      <h2>{{ module.title }}</h2>
      <p>{{ module.description }}</p>
    </div>

    <div class="module-card__topics" aria-label="Tópicos do módulo">
      <span v-for="topic in module.topics" :key="topic">{{ topic }}</span>
    </div>

    <RouterLink
      class="module-card__action"
      :to="{ name: 'module', params: { slug: module.slug } }"
      :aria-label="`Entrar no módulo ${module.title}`"
    >
      Entrar no módulo
      <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path d="M4 10h11m-4-4 4 4-4 4" />
      </svg>
    </RouterLink>
  </article>
</template>
