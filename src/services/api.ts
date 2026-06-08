import { useSettingStore } from '@/stores/setting'
import type { Agent, SendMessageRequest, ChatHistoryItem, RunMetrics, ToolTrace } from '@/types'
import { DEFAULT_AGENTS } from '@/types'

/** Agent 执行结果（后端返回） */
export interface RunResponse {
  agentId: string
  intent: string
  route: string
  assistantMessage: string
  reasoning?: string
  tools: ToolTrace[]
  knowledgeHits: string[]
  workflow: unknown | null
  memory: unknown[]
  metrics: RunMetrics
}

/** 工具 */
export interface ToolItem {
  id: string
  name: string
  type: string
  status: string
  description?: string
}

/** 知识库 */
export interface KnowledgeBaseItem {
  id: string
  name: string
  description?: string
  status: string
}

/** 会话（后端格式） */
export interface ConversationItem {
  id: string
  title: string
  agentCode?: string
  createdAt: number
  updatedAt: number
}

/** 消息（后端格式） */
export interface MessageItem {
  id: string
  role: string
  content: string
  reasoningContent?: string
  createdAt: string
}

/** Run 记录 */
export interface RunItem {
  intent: string
  route: string
  tools?: string[]
  knowledgeHits?: string[]
  status: string
}

/** 概览 */
export interface OverviewData {
  agents: number
  knowledgeBases: number
  tools: number
  status: string
}

const requestJson = async <T>(url: string, options?: RequestInit): Promise<T> => {
  const response = await fetch(url, options)
  if (!response.ok) throw new Error(`Request failed: ${response.status}`)
  return await response.json() as T
}

const baseUrl = () => (useSettingStore().settings.agentBaseURL || '/api/agent-workspace')

export const getAgentCatalog = async (): Promise<Agent[]> => {
  try { return await requestJson<Agent[]>(`${baseUrl()}/agents`) } catch { return DEFAULT_AGENTS }
}

export const getConversationList = async (): Promise<ConversationItem[]> => {
  try { return await requestJson<ConversationItem[]>(`${baseUrl()}/conversations`) } catch { return [] }
}

export const getConversationMessages = async (conversationId: string): Promise<MessageItem[]> => {
  try { return await requestJson<MessageItem[]>(`${baseUrl()}/conversations/${conversationId}/messages`) } catch { return [] }
}

export const getConversationRuns = async (conversationId: string): Promise<RunItem[]> => {
  try { return await requestJson<RunItem[]>(`${baseUrl()}/conversations/${conversationId}/runs`) } catch { return [] }
}

export const getKnowledgeBases = async (): Promise<KnowledgeBaseItem[]> => {
  try { return await requestJson<KnowledgeBaseItem[]>(`${baseUrl()}/knowledge-bases`) } catch { return [] }
}

export const getTools = async (): Promise<ToolItem[]> => {
  try { return await requestJson<ToolItem[]>(`${baseUrl()}/tools`) } catch { return [] }
}

export const getOverview = async (): Promise<OverviewData> => {
  try { return await requestJson<OverviewData>(`${baseUrl()}/overview`) } catch {
    return { agents: DEFAULT_AGENTS.length, knowledgeBases: 0, tools: 2, status: 'ready' }
  }
}

export const createConversation = async (payload: { title?: string; agentCode?: string }): Promise<ConversationItem> => {
  try {
    return await requestJson<ConversationItem>(`${baseUrl()}/conversations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch {
    return {
      id: String(Date.now()),
      title: payload.title || '新 Agent 会话',
      agentCode: payload.agentCode || 'general-agent',
      createdAt: Date.now() / 1000,
      updatedAt: Date.now() / 1000,
    }
  }
}

export const createKnowledgeBase = async (payload: { name: string; description?: string; status?: string }): Promise<KnowledgeBaseItem> => {
  try {
    return await requestJson<KnowledgeBaseItem>(`${baseUrl()}/knowledge-bases`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch { return { id: String(Date.now()), name: payload.name, status: 'online' } }
}

export const createTool = async (payload: { name: string; type: string; status?: string }): Promise<ToolItem> => {
  try {
    return await requestJson<ToolItem>(`${baseUrl()}/tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch { return { id: String(Date.now()), name: payload.name, type: 'http', status: 'enabled' } }
}

export const deleteConversation = async (id: string): Promise<void> => {
  try { await requestJson(`${baseUrl()}/conversations/${id}`, { method: 'DELETE' }) } catch { /* noop */ }
}

export const deleteKnowledgeBase = async (id: string): Promise<void> => {
  try { await requestJson(`${baseUrl()}/knowledge-bases/${id}`, { method: 'DELETE' }) } catch { /* noop */ }
}

export const deleteTool = async (id: string): Promise<void> => {
  try { await requestJson(`${baseUrl()}/tools/${id}`, { method: 'DELETE' }) } catch { /* noop */ }
}

/**
 * 简单聊天补全（非 Agent 模式，直接调用后端网关）
 * 用于 SearchDialog 等简单对话场景
 */
export const createChatCompletion = async (
  messages: Array<{ role: string; content: string }>,
  options?: { model?: string; stream?: boolean },
): Promise<Response> => {
  const settingStore = useSettingStore()
  const model = options?.model || settingStore.settings.model
  const stream = options?.stream ?? settingStore.settings.stream

  return await fetch(`${baseUrl()}/chat/completions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model,
      messages,
      stream,
      temperature: settingStore.settings.temperature,
      max_tokens: settingStore.settings.maxTokens,
      top_p: settingStore.settings.topP,
    }),
  })
}

export const runAgentWorkspace = async (params: {
  conversationId: string
  agentId: string
  message: SendMessageRequest
  history: ChatHistoryItem[]
}): Promise<RunResponse> => {
  const settingStore = useSettingStore()
  return await requestJson<RunResponse>(`${baseUrl()}/runs`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ...params,
      apiKey: settingStore.settings.apiKey,
      model: settingStore.settings.model,
    }),
  })
}
