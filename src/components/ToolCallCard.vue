<script setup lang="ts">
import { ref, computed } from 'vue'
import { Setting, Refresh } from '@element-plus/icons-vue'

const props = defineProps<{
  toolName: string
  arguments_: string | Record<string, unknown>
  result?: string
  status?: 'running' | 'success' | 'error'
}>()

const isExpanded = ref(true)

const parsedArgs = computed(() => {
  if (typeof props.arguments_ === 'string') {
    try {
      return JSON.parse(props.arguments_)
    } catch {
      return { raw: props.arguments_ }
    }
  }
  return props.arguments_
})

const statusColor = computed(() => {
  switch (props.status) {
    case 'running': return '#f59e0b'
    case 'success': return '#10b981'
    case 'error': return '#ef4444'
    default: return '#10b981'
  }
})
</script>

<template>
  <div class="tool-call-card">
    <div class="tool-header" @click="isExpanded = !isExpanded">
      <div class="tool-left">
        <el-icon class="tool-icon"><Setting /></el-icon>
        <span class="tool-name">{{ toolName }}</span>
        <span class="tool-status-dot" :style="{ background: statusColor }"></span>
        <span class="tool-status-text">{{ status === 'running' ? '执行中...' : '已完成' }}</span>
      </div>
      <el-icon class="expand-icon" :class="{ expanded: isExpanded }">
        <component :is="isExpanded ? 'ArrowUp' : 'ArrowDown'" />
      </el-icon>
    </div>

    <div v-show="isExpanded" class="tool-body">
      <div class="tool-section">
        <div class="section-label">参数</div>
        <pre class="tool-json">{{ JSON.stringify(parsedArgs, null, 2) }}</pre>
      </div>
      <div v-if="result" class="tool-section">
        <div class="section-label">结果</div>
        <pre class="tool-result">{{ result }}</pre>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.tool-call-card {
  margin: 8px 0;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
  overflow: hidden;
  font-size: 13px;
}

.tool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: #f1f5f9;
  }
}

.tool-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-icon {
  color: #6366f1;
  font-size: 15px;
}

.tool-name {
  font-weight: 600;
  color: #334155;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.tool-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.tool-status-text {
  color: #64748b;
  font-size: 12px;
}

.expand-icon {
  color: #94a3b8;
  transition: transform 0.2s;

  &.expanded {
    transform: rotate(180deg);
  }
}

.tool-body {
  padding: 0 14px 12px;
}

.tool-section {
  margin-top: 10px;
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.tool-json,
.tool-result {
  margin: 0;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 12px;
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
}

.tool-result {
  border-left: 3px solid #10b981;
}
</style>
