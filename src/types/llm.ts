import type { ToolCall, ToolDefinition } from './tool'

/** 模型供应商 */
export type ModelProvider = 'openai' | 'anthropic' | 'deepseek' | 'qwen' | 'zhipu' | 'doubao'

/** 模型选项 */
export interface ModelOption {
  value: string
  label: string
  provider: ModelProvider
  maxTokens: number
}

/** LLM 聊天请求 */
export interface ChatRequest {
  model: string
  messages: ChatMessage[]
  tools?: ToolDefinition[]
  tool_choice?: 'auto' | 'none' | 'required'
  stream?: boolean
  temperature?: number
  max_tokens?: number
  top_p?: number
}

/** LLM 聊天消息 */
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string | null
  name?: string
  tool_calls?: ToolCall[]
  tool_call_id?: string
  reasoning_content?: string
}

/** 非流式响应 */
export interface ChatResponse {
  id: string
  model: string
  choices: ChatChoice[]
  usage?: TokenUsage
}

export interface ChatChoice {
  index: number
  message: {
    role: string
    content: string | null
    tool_calls?: ToolCall[]
    reasoning_content?: string
  }
  finish_reason: 'stop' | 'length' | 'tool_calls' | 'content_filter'
}

/** 流式响应块 */
export interface StreamChunk {
  id: string
  model: string
  choices: StreamChoice[]
  usage?: TokenUsage
}

export interface StreamChoice {
  index: number
  delta: {
    role?: string
    content?: string
    tool_calls?: Partial<ToolCall>[]
    reasoning_content?: string
  }
  finish_reason: string | null
}

/** Token 使用统计 */
export interface TokenUsage {
  prompt_tokens: number
  completion_tokens: number
  total_tokens: number
}

/** Agent 运行指标 */
export interface RunMetrics {
  promptTokens: number
  completionTokens: number
  latencyMs: number
}
