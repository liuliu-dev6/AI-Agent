import type { ToolCall, ToolResult } from './tool'

/** 文件附件 */
export interface FileAttachment {
  name: string
  url: string
  type: 'image' | 'file'
  size: number
}

/** 单条消息 */
export interface Message {
  id: string | number
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  reasoning_content?: string
  files?: FileAttachment[]
  tool_calls?: ToolCall[]
  tool_results?: ToolResult[]
  completion_tokens?: number
  speed?: number
  loading?: boolean
  timestamp?: string
  /** 运行元数据 */
  meta?: MessageMeta
}

/** 工具调用追踪信息 */
export interface ToolTrace {
  name: string
  status: string
  result?: string
}

/** 消息级别元数据 */
export interface MessageMeta {
  intent?: string
  route?: string
  tools?: ToolTrace[]
  knowledgeHits?: string[]
  workflow?: unknown
  memory?: unknown[]
}

/** 会话 */
export interface Conversation {
  id: string
  title: string
  agentCode?: string
  messages: Message[]
  createdAt: number
  updatedAt?: number
}

/** 发送消息的请求体 */
export interface SendMessageRequest {
  text: string
  files?: FileAttachment[]
}

/** 对话历史（API 格式） */
export interface ChatHistoryItem {
  role: string
  content: string
  reasoning_content?: string
  files?: FileAttachment[]
  tool_calls?: ToolCall[]
  tool_results?: ToolResult[]
}
