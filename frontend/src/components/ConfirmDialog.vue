<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  title: string
  message: string
  confirmLabel?: string
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const dialog = ref<HTMLDialogElement | null>(null)

function open(): void {
  dialog.value?.showModal()
}

function onConfirm(): void {
  dialog.value?.close()
  emit('confirm')
}

function onCancel(): void {
  dialog.value?.close()
  emit('cancel')
}

defineExpose({ open })
</script>

<template>
  <dialog ref="dialog" :aria-label="props.title" @cancel="onCancel">
    <h2>{{ props.title }}</h2>
    <p>{{ props.message }}</p>
    <div class="actions">
      <button class="danger" @click="onConfirm">{{ props.confirmLabel ?? 'Confirm' }}</button>
      <button @click="onCancel">Cancel</button>
    </div>
  </dialog>
</template>

<style scoped>
dialog {
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px 20px;
  background: var(--bg);
  color: var(--fg);
  max-width: 28rem;
}
dialog::backdrop {
  background: rgba(0, 0, 0, 0.45);
}
.actions { display: flex; gap: 8px; justify-content: flex-end; }
</style>
