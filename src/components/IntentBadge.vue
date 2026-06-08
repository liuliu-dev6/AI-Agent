<script setup lang="ts">
import { computed } from 'vue'
import type { IntentType } from '@/types'
import { INTENT_LABELS, INTENT_COLORS } from '@/types'

const props = defineProps<{
  intent: string
  confidence?: number
}>()

const intentType = computed<IntentType>(() => {
  const valid = ['chat', 'knowledge_search', 'tool_call', 'code_analysis', 'workflow', 'sql_query']
  return valid.includes(props.intent) ? (props.intent as IntentType) : 'chat'
})

const label = computed(() => INTENT_LABELS[intentType.value] || props.intent)
const color = computed(() => INTENT_COLORS[intentType.value] || '#3f7af1')

const confidencePercent = computed(() => {
  if (props.confidence == null) return null
  return Math.round(props.confidence * 100)
})
</script>

<template>
  <span class="intent-badge" :style="{ background: color + '18', color, borderColor: color + '30' }">
    <span class="dot" :style="{ background: color }"></span>
    {{ label }}
    <span v-if="confidencePercent != null" class="confidence">{{ confidencePercent }}%</span>
  </span>
</template>

<style lang="scss" scoped>
.intent-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.confidence {
  opacity: 0.7;
  font-size: 11px;
}
</style>
