/** 意图类型 */
export type IntentType =
  | 'chat'
  | 'knowledge_search'
  | 'tool_call'
  | 'code_analysis'
  | 'workflow'
  | 'sql_query'

/** 意图分类结果 */
export interface IntentResult {
  intent: IntentType
  confidence: number
  params?: Record<string, string>
}

/** 意图分类器接口 */
export interface IntentClassifier {
  classify(input: string): Promise<IntentResult>
}

/** 意图类型的中文标签映射 */
export const INTENT_LABELS: Record<IntentType, string> = {
  chat: '对话',
  knowledge_search: '知识检索',
  tool_call: '工具调用',
  code_analysis: '代码分析',
  workflow: '工作流',
  sql_query: 'SQL 查询',
}

/** 意图类型的颜色映射 */
export const INTENT_COLORS: Record<IntentType, string> = {
  chat: '#3f7af1',
  knowledge_search: '#8b5cf6',
  tool_call: '#10b981',
  code_analysis: '#f59e0b',
  workflow: '#ef4444',
  sql_query: '#06b6d4',
}
