<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'

const { t } = useI18n()
</script>

<template>
  <section>
    <h2>{{ t('notFound.title') }}</h2>
    <p>{{ t('notFound.body') }}</p>
    <p><RouterLink to="/today">{{ t('notFound.home') }}</RouterLink></p>
  </section>
</template>
