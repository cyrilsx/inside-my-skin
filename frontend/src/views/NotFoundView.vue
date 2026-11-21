<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section>
    <h2>Page not found</h2>
    <p>The page you are looking for does not exist.</p>
    <p><RouterLink to="/today">Go to Today</RouterLink></p>
  </section>
</template>
