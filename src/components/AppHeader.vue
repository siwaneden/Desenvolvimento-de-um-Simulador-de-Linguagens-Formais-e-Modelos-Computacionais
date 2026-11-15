<script setup>
import { RouterLink } from 'vue-router'
import BrandMark from './BrandMark.vue'
</script>

<template>
  <header class="app-header">
    <div class="shell app-header__inner">
      <RouterLink class="brand" to="/" aria-label="FormaLab — início">
        <BrandMark />
        <span class="brand__copy">
          <strong>Forma<span>Lab</span></strong>
          <small>Teoria da Computação</small>
        </span>
      </RouterLink>

      <div class="header-status" aria-label="Status do projeto">
        <span class="header-status__dot"></span>
        Ambiente de estudo
      </div>
    </div>
  </header>
</template>
