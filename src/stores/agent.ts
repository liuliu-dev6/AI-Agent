import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { Agent } from '@/types'
import { DEFAULT_AGENTS } from '@/types'

export const useAgentStore = defineStore(
  'agent',
  () => {
    const agents = ref<Agent[]>([...DEFAULT_AGENTS])
    const currentAgentId = ref<string>('general-agent')

    const currentAgent = computed<Agent | undefined>(() => {
      return agents.value.find((a) => a.id === currentAgentId.value)
    })

    const setCurrentAgent = (agentId: string) => {
      currentAgentId.value = agentId
    }

    const addAgent = (agent: Agent) => {
      agents.value.push(agent)
    }

    const removeAgent = (agentId: string) => {
      const index = agents.value.findIndex((a) => a.id === agentId)
      if (index !== -1) {
        agents.value.splice(index, 1)
        if (currentAgentId.value === agentId) {
          currentAgentId.value = agents.value[0]?.id || 'general-agent'
        }
      }
    }

    const updateAgent = (agentId: string, updates: Partial<Agent>) => {
      const agent = agents.value.find((a) => a.id === agentId)
      if (agent) {
        Object.assign(agent, updates)
      }
    }

    return {
      agents,
      currentAgentId,
      currentAgent,
      setCurrentAgent,
      addAgent,
      removeAgent,
      updateAgent,
    }
  },
  { persist: true },
)
