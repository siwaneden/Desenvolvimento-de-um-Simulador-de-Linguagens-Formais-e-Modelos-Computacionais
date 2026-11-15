<script setup>
defineProps({
  name: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <svg
    v-if="name === 'language'"
    viewBox="0 0 32 32"
    fill="none"
    aria-hidden="true"
  >
    <path d="M8 6.5H5.5v19H8M24 6.5h2.5v19H24" />
    <path d="m12 21 4-10 4 10m-6.7-3.2h5.4" />
  </svg>
  <svg v-else-if="name === 'nodes'" viewBox="0 0 32 32" fill="none" aria-hidden="true">
    <circle cx="8" cy="16" r="4" />
    <circle cx="24" cy="9" r="4" />
    <circle cx="24" cy="23" r="4" />
    <path d="m12 15 8-4m-8 6 8 4" />
  </svg>
  <svg v-else-if="name === 'grammar'" viewBox="0 0 32 32" fill="none" aria-hidden="true">
    <path d="M5 8h8M5 16h6M5 24h8M17 8h10M17 24h10" />
    <path d="m18 13 4 3-4 3m4-3h5" />
  </svg>
  <svg v-else-if="name === 'stack'" viewBox="0 0 32 32" fill="none" aria-hidden="true">
    <path d="m16 4 11 5-11 5L5 9l11-5Z" />
    <path d="m5 15 11 5 11-5M5 21l11 5 11-5" />
  </svg>
  <svg v-else-if="name === 'tape'" viewBox="0 0 32 32" fill="none" aria-hidden="true">
    <rect x="4" y="9" width="24" height="14" rx="3" />
    <path d="M12 9v14m8-14v14M7 16h2m6 0h2m6 0h2" />
  </svg>
  <svg v-else viewBox="0 0 32 32" fill="none" aria-hidden="true">
    <path d="M6 26V14m7 12V7m7 19v-8m7 8V4" />
    <path d="M4 26h25" />
  </svg>
</template>

<style scoped>
svg {
  width: 30px;
  height: 30px;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
