import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { Conversation, Message, MessageMeta } from '@/types'

const now = () => Date.now()

export const useChatStore = defineStore(
  'llm-chat',
  () => {
    const conversations = ref<Conversation[]>([
      {
        id: '1',
        title: '日常问候',
        messages: [],
        createdAt: now(),
      },
    ])

    const currentConversationId = ref<string>('1')
    const isLoading = ref<boolean>(false)

    const currentConversation = computed<Conversation | undefined>(() => {
      return conversations.value.find((conv) => conv.id === currentConversationId.value)
    })

    const currentMessages = computed<Message[]>(() => currentConversation.value?.messages || [])

    const createConversation = (title = '新 Agent 会话') => {
      const newConversation: Conversation = {
        id: now().toString(),
        title,
        messages: [],
        createdAt: now(),
      }
      conversations.value.unshift(newConversation)
      currentConversationId.value = newConversation.id
    }

    const switchConversation = (conversationId: string) => {
      currentConversationId.value = conversationId
    }

    const addMessage = (message: Message) => {
      if (currentConversation.value) {
        currentConversation.value.messages.push({
          timestamp: new Date().toISOString(),
          ...message,
          id: message.id || now(),
        })
      }
    }

    const setIsLoading = (value: boolean) => {
      isLoading.value = value
    }

    const updateLastMessage = (content: string, reasoning_content?: string, completion_tokens?: number, speed?: number | string) => {
      const messages = currentConversation.value?.messages
      if (messages && messages.length > 0) {
        const lastMessage = messages[messages.length - 1]
        lastMessage.content = content
        lastMessage.reasoning_content = reasoning_content
        lastMessage.completion_tokens = completion_tokens
        lastMessage.speed = typeof speed === 'string' ? parseFloat(speed) : speed
      }
    }

    const updateLastMessageMeta = (meta: Partial<MessageMeta>) => {
      const messages = currentConversation.value?.messages
      if (messages && messages.length > 0) {
        const lastMessage = messages[messages.length - 1]
        lastMessage.meta = { ...(lastMessage.meta || {}), ...meta }
      }
    }

    const getLastMessage = (): Message | null => {
      const messages = currentConversation.value?.messages
      if (messages && messages.length > 0) {
        return messages[messages.length - 1]
      }
      return null
    }

    const updateConversationTitle = (conversationId: string, newTitle: string) => {
      const conversation = conversations.value.find((c) => c.id === conversationId)
      if (conversation) conversation.title = newTitle
    }

    const deleteConversation = (conversationId: string) => {
      const index = conversations.value.findIndex((c) => c.id === conversationId)
      if (index !== -1) {
        conversations.value.splice(index, 1)
        if (conversations.value.length === 0) {
          createConversation()
        } else if (conversationId === currentConversationId.value) {
          currentConversationId.value = conversations.value[0].id
        }
      }
    }

    return {
      conversations,
      currentConversationId,
      currentConversation,
      currentMessages,
      isLoading,
      addMessage,
      setIsLoading,
      updateLastMessage,
      updateLastMessageMeta,
      getLastMessage,
      createConversation,
      switchConversation,
      updateConversationTitle,
      deleteConversation,
    }
  },
  { persist: true },
)
