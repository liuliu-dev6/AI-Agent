/** 工具定义（JSON Schema 参数） */
export interface ToolParameterSchema {
  type: 'object'
  properties: Record<string, {
    type: string
    description: string
    enum?: string[]
  }>
  required?: string[]
}

/** 工具定义 */
export interface ToolDefinition {
  name: string
  description: string
  parameters: ToolParameterSchema
  execute?: (params: Record<string, unknown>) => Promise<ToolResult>
}

/** 工具定义（OpenAI 格式） */
export interface OpenAIToolDefinition {
  type: 'function'
  function: {
    name: string
    description: string
    parameters: ToolParameterSchema
  }
}

/** 工具调用 */
export interface ToolCall {
  id: string
  type: 'function'
  function: {
    name: string
    arguments: string
  }
}

/** 工具执行结果 */
export interface ToolResult {
  tool_call_id: string
  role: 'tool'
  name: string
  content: string
}
