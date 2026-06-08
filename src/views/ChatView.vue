<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Setting } from '@element-plus/icons-vue'
import ChatInput from '@/components/ChatInput.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import SettingsPanel from '@/components/SettingsPanel.vue'
import IntentBadge from '@/components/IntentBadge.vue'
import { useChatStore } from '@/stores/chat'
import { useSettingStore } from '@/stores/setting'
import { messageHandler } from '@/utils/messageHandler'
import { getAgentCatalog, getOverview, runAgentWorkspace, createConversation, deleteConversation } from '@/services/api'
import { Agent, SendMessageRequest, ChatHistoryItem } from '@/types'

const router = useRouter()
const chatStore = useChatStore()
const settingStore = useSettingStore()
const messagesContainer = ref<HTMLElement | null>(null)
const settingDrawer = ref<any>(null)

const agents = ref<Agent[]>([])
const activeAgent = ref<Agent | null>(null)
const isLoading = computed(() => chatStore.isLoading)
const runTrace = ref<Array<{ label: string; intent?: string; route?: string; status?: string }>>([])
const runMetrics = ref({ promptTokens: 0, completionTokens: 0, latencyMs: 0 })

const currentMessages = computed(() => chatStore.currentMessages)
const currentTitle = computed(() => chatStore.currentConversation?.title || '新会话')

watch(currentMessages, () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}, { deep: true })

onMounted(async () => {
  if (chatStore.conversations.length === 0) chatStore.createConversation()
  const [agentList, overview] = await Promise.all([getAgentCatalog(), getOverview()])
  agents.value = agentList
  activeAgent.value = agentList.find(a => a.id === 'general-agent') || agentList[0]
})

const handleSend = async (messageContent: SendMessageRequest) => {
  if (!activeAgent.value) return
  try {
    chatStore.addMessage(messageHandler.formatMessage('user', messageContent.text, '', messageContent.files))
    chatStore.addMessage(messageHandler.formatMessage('assistant', '', ''))
    chatStore.setIsLoading(true)
    const lastMsg = chatStore.getLastMessage()
    if (lastMsg) lastMsg.loading = true

    const history: ChatHistoryItem[] = chatStore.currentMessages
      .slice(0, -2)
      .map(({ role, content, reasoning_content, files }) => ({
        role, content, reasoning_content, files,
      }))

    const response = await runAgentWorkspace({
      conversationId: chatStore.currentConversationId,
      agentId: activeAgent.value.id,
      message: messageContent,
      history,
    })

    runTrace.value = [
      { label: '意图', intent: response.intent, route: response.route, status: 'matched' },
    ]
    if (response.tools?.length) {
      runTrace.value.push({ label: '工具调用', status: 'done' })
    }
    runMetrics.value = response.metrics || { promptTokens: 0, completionTokens: 0, latencyMs: 0 }

    chatStore.updateLastMessage(
      response.assistantMessage,
      response.reasoning,
      response.metrics?.completionTokens,
    )
    chatStore.updateLastMessageMeta({
      intent: response.intent,
      route: response.route,
      tools: response.tools,
    })
  } catch (err) {
    console.error(err)
    chatStore.updateLastMessage('抱歉，服务暂不可用，请稍后重试。')
  } finally {
    chatStore.setIsLoading(false)
    const lastMsg = chatStore.getLastMessage()
    if (lastMsg) lastMsg.loading = false
  }
}

const handleNewChat = async () => {
  await createConversation({ title: '新会话', agentCode: activeAgent.value?.id || 'general-agent' })
  chatStore.createConversation('新会话')
  runTrace.value = []
  runMetrics.value = { promptTokens: 0, completionTokens: 0, latencyMs: 0 }
}

const handleDeleteChat = async (id: string) => {
  await deleteConversation(id)
  chatStore.deleteConversation(id)
}

const handleRegenerate = async () => {
  const msgs = chatStore.currentMessages
  if (msgs.length < 2) return
  const lastUser = msgs[msgs.length - 2]
  msgs.splice(-2, 2)
  await handleSend({ text: lastUser.content, files: lastUser.files || [] })
}

const truncate = (s: string, n: number) => s.length > n ? s.slice(0, n) + '...' : s
</script>

<template>
  <div class="workspace">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <router-link to="/" class="sidebar-brand">
        <div class="logo">AS</div>
        <span>AI Agent Studio</span>
      </router-link>

      <button class="new-chat-btn" @click="handleNewChat">
        <el-icon><Plus /></el-icon>
        <span>新建会话</span>
      </button>

      <div class="conv-list">
        <div
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: conv.id === chatStore.currentConversationId }"
          @click="chatStore.switchConversation(conv.id)"
        >
          <span class="conv-title">{{ truncate(conv.title, 16) }}</span>
          <button class="conv-delete" @click.stop="handleDeleteChat(conv.id)">&times;</button>
        </div>
      </div>

      <div class="sidebar-footer">
        <button class="settings-btn" @click="settingDrawer?.openDrawer()">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </button>
      </div>
    </aside>

    <!-- 主区域 -->
    <main class="main">
      <header class="topbar">
        <h2>{{ truncate(currentTitle, 20) }}</h2>
        <div class="topbar-right">
          <IntentBadge v-if="runTrace[0]?.intent" :intent="runTrace[0].intent" />
          <span class="agent-name" v-if="activeAgent">{{ activeAgent.name }}</span>
        </div>
      </header>

      <div class="content">
        <div class="chat-area">
          <div class="messages" ref="messagesContainer">
            <template v-if="currentMessages.length === 0">
              <div class="empty-chat">
                <div class="empty-icon">💬</div>
                <h3>开始对话</h3>
                <p>你可以问我任何问题，我会自动调用工具来帮你完成。</p>
              </div>
            </template>
            <ChatMessage
              v-for="(msg, idx) in currentMessages"
              :key="msg.id"
              :message="msg"
              :is-last-assistant-message="idx === currentMessages.length - 1 && msg.role === 'assistant'"
              @regenerate="handleRegenerate"
            />
          </div>
          <div class="input-wrap">
            <ChatInput :loading="isLoading" @send="handleSend" />
          </div>
        </div>

        <!-- 运行面板 -->
        <aside class="inspector">
          <div class="inspector-section">
            <div class="inspector-title">运行链路</div>
            <div v-if="runTrace.length === 0" class="inspector-empty">发送消息后查看运行详情</div>
            <div v-for="item in runTrace" :key="item.label" class="trace-item">
              <span class="trace-label">{{ item.label }}</span>
              <span class="trace-value">{{ item.intent || item.route || item.status }}</span>
            </div>
          </div>
          <div class="inspector-section">
            <div class="inspector-title">用量</div>
            <div class="metric-row">
              <span>Prompt</span><strong>{{ runMetrics.promptTokens }}</strong>
            </div>
            <div class="metric-row">
              <span>Completion</span><strong>{{ runMetrics.completionTokens }}</strong>
            </div>
            <div class="metric-row">
              <span>延迟</span><strong>{{ runMetrics.latencyMs }}ms</strong>
            </div>
          </div>
        </aside>
      </div>
    </main>

    <SettingsPanel ref="settingDrawer" />
  </div>
</template>

<style lang="scss" scoped>
.workspace {
  display: flex;
  height: 100vh;
  background: #f5f6f8;
  color: #1e293b;
}

// ---- 侧边栏 ----
.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e8ecf1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 4px 16px;
  text-decoration: none;
  color: inherit;
}

.logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-brand span {
  font-size: 16px;
  font-weight: 700;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 40px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: none;
  color: #2563eb;
  font-size: 14px;
  cursor: pointer;
  justify-content: center;
  margin-bottom: 16px;
  transition: all 0.15s;

  &:hover {
    background: #eff6ff;
    border-color: #2563eb;
  }
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.1s;

  &:hover, &.active {
    background: #f1f5f9;
  }

  &.active .conv-title {
    font-weight: 600;
    color: #2563eb;
  }
}

.conv-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #475569;
}

.conv-delete {
  opacity: 0;
  border: none;
  background: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 16px;
  padding: 0 4px;

  .conv-item:hover & { opacity: 1; }
  &:hover { color: #ef4444; }
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 38px;
  border: none;
  border-radius: 8px;
  background: none;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  padding: 0 12px;

  &:hover {
    background: #f1f5f9;
    color: #334155;
  }
}

// ---- 主区域 ----
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;

  h2 {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.agent-name {
  font-size: 13px;
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
}

// ---- 内容区 ----
.content {
  flex: 1;
  display: flex;
  min-height: 0;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #94a3b8;

  .empty-icon { font-size: 40px; margin-bottom: 12px; }
  h3 { font-size: 16px; color: #64748b; margin: 0 0 6px; }
  p { font-size: 13px; margin: 0; }
}

.input-wrap {
  padding: 16px 24px 20px;
}

// ---- 检查面板 ----
.inspector {
  width: 240px;
  background: #fff;
  border-left: 1px solid #e8ecf1;
  padding: 20px;
  overflow-y: auto;
  flex-shrink: 0;
}

.inspector-section {
  margin-bottom: 24px;
}

.inspector-title {
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 12px;
}

.inspector-empty {
  font-size: 13px;
  color: #cbd5e1;
}

.trace-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;

  .trace-label { color: #64748b; }
  .trace-value { color: #1e293b; font-weight: 500; }
}

.metric-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;

  span { color: #64748b; }
  strong { color: #1e293b; }
}
</style>
