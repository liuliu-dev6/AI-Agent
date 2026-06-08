/** Agent 能力类型 */
export type AgentCapability = 'chat' | 'rag' | 'tool_calling' | 'workflow' | 'mcp' | 'code_analysis'

/** Agent 定义 */
export interface Agent {
  id: string
  name: string
  description?: string
  mode: string
  prompt: string
  capabilities: AgentCapability[]
  tools?: string[]
  knowledgeBases?: string[]
  status: 'online' | 'offline' | 'error'
  createdAt?: number
  updatedAt?: number
}

/** Agent 配置（用于创建/更新） */
export interface AgentConfig {
  name: string
  description?: string
  prompt: string
  capabilities: AgentCapability[]
  tools?: string[]
  knowledgeBases?: string[]
}

/** 默认 Agent 列表 */
export const DEFAULT_AGENTS: Agent[] = [
  {
    id: 'general-agent',
    name: 'General Agent',
    description: '通用助手，支持对话、工具调用和意图识别',
    mode: 'chat + tools',
    prompt: '你是一个企业级 Agent，优先识别用户意图并路由到正确的能力模块。你可以使用工具来完成任务。',
    capabilities: ['chat', 'tool_calling'],
    tools: ['calculator', 'get_weather'],
    status: 'online',
  },
  {
    id: 'knowledge-agent',
    name: 'Knowledge Agent',
    description: '知识库问答 Agent，优先基于知识库检索回答',
    mode: 'rag + search',
    prompt: '你是一个知识库问答 Agent。优先基于知识库进行检索和回答，必要时发起联网搜索和重排序。',
    capabilities: ['chat', 'rag'],
    status: 'online',
  },
  {
    id: 'workflow-agent',
    name: 'Workflow Agent',
    description: '工作流编排 Agent，将复杂任务分解执行',
    mode: 'automation + mcp',
    prompt: '你是一个工作流编排 Agent。将复杂任务分解为可执行的工作流节点，并可调用外部系统。',
    capabilities: ['chat', 'tool_calling', 'workflow', 'mcp'],
    tools: ['calculator'],
    status: 'online',
  },
]
