<script setup>
import AppHeader from '../components/AppHeader.vue'
import ModuleCard from '../components/ModuleCard.vue'
import { modules } from '../data/modules'
</script>

<template>
  <div class="page page--home">
    <AppHeader />

    <main>
      <section class="hero shell" aria-labelledby="hero-title">
        <div class="hero__copy">
          <span class="eyebrow"><i></i> Simulador educacional</span>
          <h1 id="hero-title">
            Explore os fundamentos da
            <em>computação teórica.</em>
          </h1>
          <p>
            Um ambiente visual para estudar, construir e simular linguagens formais e
            modelos computacionais.
          </p>
        </div>

        <div class="hero__visual" aria-hidden="true">
          <div class="orbit orbit--outer">
            <span class="orbit__node orbit__node--a">q<sub>0</sub></span>
            <span class="orbit__node orbit__node--b">q<sub>1</sub></span>
            <span class="orbit__node orbit__node--c">q<sub>f</sub></span>
          </div>
          <div class="orbit orbit--inner"></div>
          <div class="hero__sigma">Σ</div>
          <span class="hero__symbol hero__symbol--one">a, b</span>
          <span class="hero__symbol hero__symbol--two">δ</span>
        </div>
      </section>

      <section class="modules-section shell" aria-labelledby="modules-title">
        <div class="section-heading">
          <div>
            <span class="eyebrow"><i></i> Área de estudos</span>
            <h2 id="modules-title">Escolha um módulo</h2>
          </div>
          <p>Selecione um tema para acessar o ambiente de operações.</p>
        </div>

        <div class="modules-grid">
          <ModuleCard v-for="item in modules" :key="item.slug" :module="item" />
        </div>
      </section>
    </main>

    <footer class="site-footer">
      <div class="shell site-footer__inner">
        <p>FormaLab <span>•</span> Aspectos Teóricos da Computação</p>
        <p>Projeto acadêmico</p>
      </div>
    </footer>
  </div>
</template>
