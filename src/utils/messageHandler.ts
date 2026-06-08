import type { Message, FileAttachment } from '@/types'

type UpdateCallback = (content: string, reasoning: string, tokens: number, speed: string) => void

export const messageHandler = {
  formatMessage(role: Message['role'], content: string, reasoning_content = '', files: FileAttachment[] = []): Message {
    return {
      id: Date.now(),
      role,
      content,
      reasoning_content,
      files,
      completion_tokens: 0,
      speed: 0,
      loading: false,
    }
  },

  // 处理流式响应
  async handleStreamResponse(response: Response, updateCallback: UpdateCallback) {
    const reader = response.body?.getReader()
    if (!reader) throw new Error('Response body is not readable')

    const decoder = new TextDecoder()
    let accumulatedContent = ''
    let accumulatedReasoning = ''
    const startTime = Date.now()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n').filter((line) => line.trim() !== '')

      for (const line of lines) {
        if (line === 'data: [DONE]') continue
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.slice(5))
            const content = data.choices?.[0]?.delta?.content || ''
            const reasoning = data.choices?.[0]?.delta?.reasoning_content || ''

            accumulatedContent += content
            accumulatedReasoning += reasoning

            const tokens = data.usage?.completion_tokens || 0
            const elapsed = (Date.now() - startTime) / 1000
            const speed = elapsed > 0 ? (tokens / elapsed).toFixed(2) : '0'

            updateCallback(accumulatedContent, accumulatedReasoning, tokens, speed)
          } catch {
            // Skip invalid JSON chunks
          }
        }
      }
    }
  },

  // 处理非流式响应
  handleNormalResponse(
    response: { choices: Array<{ message: { content: string; reasoning_content?: string } }>; usage?: { completion_tokens: number } },
    updateCallback: UpdateCallback,
  ) {
    updateCallback(
      response.choices[0].message.content,
      response.choices[0].message.reasoning_content || '',
      response.usage?.completion_tokens || 0,
      '0',
    )
  },

  // 统一的响应处理函数
  async handleResponse(
    response: Response | { choices: Array<{ message: { content: string; reasoning_content?: string } }>; usage?: { completion_tokens: number } },
    isStream: boolean,
    updateCallback: UpdateCallback,
  ) {
    if (isStream && response instanceof Response) {
      await this.handleStreamResponse(response, updateCallback)
    } else if (!(response instanceof Response)) {
      this.handleNormalResponse(response, updateCallback)
    }
  },
}
