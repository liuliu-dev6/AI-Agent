import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ModelOption, ModelProvider } from '@/types'

export const modelOptions: ModelOption[] = [
  { value: 'doubao-seed-2-0-pro-260215', label: 'Doubao Seed 2.0 Pro', provider: 'doubao', maxTokens: 65536 },
  { value: 'deepseek-v3', label: 'DeepSeek V3', provider: 'deepseek', maxTokens: 65536 },
  { value: 'deepseek-r1', label: 'DeepSeek R1', provider: 'deepseek', maxTokens: 65536 },
  { value: 'gpt-4o-mini', label: 'GPT-4o Mini', provider: 'openai', maxTokens: 128000 },
  { value: 'gpt-4o', label: 'GPT-4o', provider: 'openai', maxTokens: 128000 },
  { value: 'qwen-max', label: 'Qwen Max', provider: 'qwen', maxTokens: 32768 },
  { value: 'glm-4', label: 'GLM-4', provider: 'zhipu', maxTokens: 128000 },
]

/** 供应商 API 地址 */
export const providerBaseURLs: Record<ModelProvider, string> = {
  openai: 'https://api.openai.com/v1',
  anthropic: 'https://api.anthropic.com/v1',
  deepseek: 'https://api.deepseek.com/v1',
  qwen: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
  zhipu: 'https://open.bigmodel.cn/api/paas/v4',
  doubao: 'https://ark.cn-beijing.volces.com/api/v3',
}

export interface Settings {
  model: string
  apiKey: string
  baseURL: string
  agentBaseURL: string
  stream: boolean
  maxTokens: number
  temperature: number
  topP: number
  topK: number
  workspaceName: string
  mysqlHost: string
  mysqlPort: number
  mysqlDatabase: string
  /** 各供应商独立的 API Key */
  providerApiKeys: Record<string, string>
}

export const useSettingStore = defineStore(
  'llm-setting',
  () => {
    const settings = ref<Settings>({
      model: 'doubao-seed-2-0-pro-260215',
      apiKey: 'ark-ee5f373c-4db2-45a7-bcfd-437b227e8219-ce356',
      baseURL: '/api',
      agentBaseURL: '/api/agent-workspace',
      stream: true,
      maxTokens: 4096,
      temperature: 0.7,
      topP: 0.7,
      topK: 50,
      workspaceName: 'AI Agent Studio',
      mysqlHost: '127.0.0.1',
      mysqlPort: 3306,
      mysqlDatabase: 'ai_agent_studio',
      providerApiKeys: {},
    })

    return { settings }
  },
  { persist: true },
)
