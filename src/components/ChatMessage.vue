<script setup lang="ts">
import { computed, ref } from 'vue'
import { renderMarkdown } from '@/utils/markdown'
import { Document, ArrowDown } from '@element-plus/icons-vue'
import ToolCallCard from './ToolCallCard.vue'
import IntentBadge from './IntentBadge.vue'
import type { ToolCall, ToolTrace } from '@/types'
import copyIcon from '@/assets/photo/复制.png'
import successIcon from '@/assets/photo/成功.png'
import likeIcon from '@/assets/photo/赞.png'
import likeActiveIcon from '@/assets/photo/赞2.png'
import dislikeIcon from '@/assets/photo/踩.png'
import dislikeActiveIcon from '@/assets/photo/踩2.png'
import regenerateIcon from '@/assets/photo/重新生成.png'
import thinkingIcon from '@/assets/photo/深度思考.png'

interface Message {
  id: string | number
  role: string
  content: string
  reasoning_content?: string
  files?: Array<{ url: string; name: string; type: string; size: number }>
  completion_tokens?: number
  speed?: number
  loading?: boolean
  meta?: {
    intent?: string
    route?: string
    tools?: ToolTrace[]
  }
  tool_calls?: ToolCall[]
  tool_results?: Array<{ name: string; content: string }>
}

const props = defineProps<{
  message: Message
  isLastAssistantMessage?: boolean
}>()

const emit = defineEmits<{
  (e: 'regenerate'): void
}>()

const isLiked = ref(false)
const isDisliked = ref(false)
const isCopied = ref(false)
const isReasoningExpanded = ref(true)

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(props.message.content)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2500)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

const handleLike = () => {
  if (isDisliked.value) isDisliked.value = false
  isLiked.value = !isLiked.value
}

const handleDislike = () => {
  if (isLiked.value) isLiked.value = false
  isDisliked.value = !isDisliked.value
}

const handleRegenerate = () => emit('regenerate')
const toggleReasoning = () => (isReasoningExpanded.value = !isReasoningExpanded.value)

const renderedContent = computed(() => renderMarkdown(props.message.content))
const renderedReasoning = computed(() => renderMarkdown(props.message.reasoning_content || ''))

const hasToolInfo = computed(() => {
  return (props.message.tool_calls && props.message.tool_calls.length > 0) ||
         (props.message.meta?.tools && props.message.meta.tools.length > 0)
})
</script>

<template>
  <div class="message-item" :class="{ 'is-mine': message.role === 'user' }">
    <div class="content">
      <div v-if="message.files && message.files.length > 0" class="files-container">
        <div v-for="file in message.files" :key="file.url" class="file-item">
          <div v-if="file.type === 'image'" class="image-preview">
            <img :src="file.url" :alt="file.name" />
          </div>
          <div v-else class="file-preview">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ (file.size / 1024).toFixed(1) }}KB</span>
          </div>
        </div>
      </div>

      <div v-if="message.loading && message.role === 'assistant'" class="thinking-text">
        <img src="@/assets/photo/加载中.png" alt="loading" class="loading-icon" />
        <span>Agent 正在执行中...</span>
      </div>

      <!-- 意图标签 -->
      <div v-if="message.meta?.intent && message.role === 'assistant'" class="intent-row">
        <IntentBadge :intent="message.meta.intent" />
      </div>

      <!-- 工具调用卡片 -->
      <div v-if="message.meta?.tools && message.meta.tools.length > 0" class="tool-calls-row">
        <ToolCallCard
          v-for="(tool, idx) in message.meta.tools"
          :key="idx"
          :tool-name="tool.name"
          :arguments_="{}"
          :result="tool.result"
          :status="tool.status === 'success' ? 'success' : 'running'"
        />
      </div>

      <div v-if="message.reasoning_content" class="reasoning-toggle" @click="toggleReasoning">
        <img :src="thinkingIcon" alt="thinking" />
        <span>推理过程</span>
        <el-icon class="toggle-icon" :class="{ 'is-expanded': isReasoningExpanded }">
          <ArrowDown />
        </el-icon>
      </div>

      <div
        v-if="message.reasoning_content && isReasoningExpanded"
        class="reasoning markdown-body"
        v-html="renderedReasoning"
      ></div>

      <div class="bubble markdown-body" v-html="renderedContent"></div>

      <div v-if="message.role === 'assistant' && message.loading === false" class="message-actions">
        <button
          v-if="isLastAssistantMessage"
          class="action-btn"
          @click="handleRegenerate"
          data-tooltip="重新生成"
        >
          <img :src="regenerateIcon" alt="regenerate" />
        </button>
        <button class="action-btn" @click="handleCopy" data-tooltip="复制">
          <img :src="isCopied ? successIcon : copyIcon" alt="copy" />
        </button>
        <button class="action-btn" @click="handleLike" data-tooltip="喜欢">
          <img :src="isLiked ? likeActiveIcon : likeIcon" alt="like" />
        </button>
        <button class="action-btn" @click="handleDislike" data-tooltip="不喜欢">
          <img :src="isDisliked ? dislikeActiveIcon : dislikeIcon" alt="dislike" />
        </button>
        <span v-if="message.completion_tokens" class="tokens-info">
          tokens: {{ message.completion_tokens }}, speed: {{ message.speed }} tokens/s
        </span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-item {
  display: flex;
  margin-bottom: 2rem;

  &.is-mine {
    justify-content: flex-end;

    .content .bubble.markdown-body {
      background-color: #eef4ff;
    }
  }

  .content {
    max-width: 100%;
    min-width: 0;
    width: fit-content;
    overflow: hidden;

    .reasoning-toggle {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      margin-bottom: 8px;
      cursor: pointer;
      width: fit-content;
      border-radius: 6px;
      background-color: #eef4ff;

      img { width: 14px; height: 14px; }
      span { font-size: 13px; color: #3f7af1; }
      .toggle-icon { font-size: 12px; color: #3f7af1; transition: transform 0.2s; &.is-expanded { transform: rotate(180deg); } }
    }

    .reasoning {
      margin-bottom: 8px;
      padding: 0 16px;
      background-color: #ffffff;
      border-left: 3px solid #dfe2e5;
      color: #8b8b8b;
      font-size: 14px;
      line-height: 1.6;
    }

    .bubble.markdown-body {
      display: block;
      width: 100%;
      padding: 0.75rem 1rem;
      background-color: #ffffff;
      border-radius: 1rem;
      font-size: 1rem;
      line-height: 1.5;
      word-break: break-word;
      overflow: hidden;
      border: 1px solid #e7edf6;
    }

    .message-actions {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.5rem;
      padding-left: 1rem;

      .action-btn {
        width: 1.5rem;
        height: 1.5rem;
        padding: 0;
        border: none;
        background: none;
        cursor: pointer;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;

        img { width: 1rem; height: 1rem; }
      }

      .tokens-info {
        display: flex;
        gap: 0.5rem;
        color: var(--text-color-secondary);
        font-size: 0.75rem;
        background-color: #f3f4f6;
        padding: 0.25rem 0.5rem;
        border-radius: 0.25rem;
      }
    }
  }

  .thinking-text {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0.75rem 1rem;
    color: #6b7280;
    font-size: 0.875rem;

    .loading-icon {
      width: 16px;
      height: 16px;
      animation: spin 1s linear infinite;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.files-container {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-preview {
  padding: 8px;
  background-color: #f4f4f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
