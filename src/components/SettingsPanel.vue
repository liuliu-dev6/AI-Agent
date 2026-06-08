<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useSettingStore, modelOptions, providerBaseURLs } from '@/stores/setting'
import { QuestionFilled } from '@element-plus/icons-vue'
import type { ModelProvider } from '@/types'

const settingStore = useSettingStore()

// 控制抽屉显示
const visible = ref(false)

// 当前选中的供应商标签页
const activeProviderTab = ref<ModelProvider>('deepseek')

// 计算当前选中模型的最大 tokens
const currentMaxTokens = computed(() => {
  const selectedModel = modelOptions.find((option) => option.value === settingStore.settings.model)
  return selectedModel ? selectedModel.maxTokens : 4096
})

// 当前选中模型的供应商
const currentProvider = computed<ModelProvider>(() => {
  const selectedModel = modelOptions.find((option) => option.value === settingStore.settings.model)
  return selectedModel?.provider || 'deepseek'
})

// 获取供应商 API Key
const getProviderApiKey = (provider: string) => {
  return settingStore.settings.providerApiKeys?.[provider] || ''
}

// 设置供应商 API Key
const setProviderApiKey = (provider: string, key: string) => {
  settingStore.settings.providerApiKeys = {
    ...settingStore.settings.providerApiKeys,
    [provider]: key,
  }
}

// 监听模型变化
watch(
  () => settingStore.settings.model,
  (newModel) => {
    const selectedModel = modelOptions.find((option) => option.value === newModel)
    if (selectedModel) {
      activeProviderTab.value = selectedModel.provider
      // 更新 maxTokens，并确保不超过模型的最大值
      settingStore.settings.maxTokens = Math.min(
        settingStore.settings.maxTokens,
        selectedModel.maxTokens,
      )
    }
  },
)

// 打开抽屉
const openDrawer = () => {
  visible.value = true
}

// 供应商标签
const providerLabel = (provider: string): string => {
  const labels: Record<string, string> = {
    openai: 'OpenAI',
    deepseek: 'DeepSeek',
    qwen: 'Qwen（通义千问）',
    zhipu: 'Zhipu（智谱 GLM）',
    doubao: 'Doubao（豆包）',
    anthropic: 'Anthropic',
  }
  return labels[provider] || provider
}

// 导出方法供父组件调用
defineExpose({
  openDrawer,
})
</script>

<template>
  <el-drawer v-model="visible" title="Agent 设置" direction="rtl" size="420px">
    <div class="setting-container">
      <!-- 模型选择 -->
      <div class="setting-item">
        <div class="setting-label">Model</div>
        <el-select
          v-model="settingStore.settings.model"
          class="model-select"
          placeholder="选择模型"
        >
          <el-option-group
            v-for="provider in ['openai', 'deepseek', 'qwen', 'zhipu', 'doubao']"
            :key="provider"
            :label="providerLabel(provider)"
          >
            <el-option
              v-for="option in modelOptions.filter(o => o.provider === provider)"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-option-group>
        </el-select>
      </div>

      <!-- API Key 配置 -->
      <div class="setting-item">
        <div class="setting-label">API Key（当前模型）</div>
        <el-input
          v-model="settingStore.settings.apiKey"
          type="password"
          placeholder="通用 API Key（当前模型供应商）"
          show-password
        />
        <div class="setting-hint">
          供应商: {{ currentProvider }} |
          <a :href="providerBaseURLs[currentProvider]" target="_blank" class="get-key-link">
            获取 API Key
          </a>
        </div>
      </div>

      <!-- 各供应商独立 API Key -->
      <div class="setting-item">
        <div class="setting-label">供应商独立 API Key</div>
        <div class="provider-key-list">
          <div v-for="provider in ['openai', 'deepseek', 'qwen', 'zhipu', 'doubao'] as const" :key="provider" class="provider-key-item">
            <span class="provider-label">{{ providerLabel(provider) }}</span>
            <el-input
              :model-value="getProviderApiKey(provider)"
              @update:model-value="(val: string) => setProviderApiKey(provider, val)"
              type="password"
              :placeholder="`${providerLabel(provider)} API Key`"
              show-password
              size="small"
            />
          </div>
        </div>
      </div>

      <el-divider />

      <!-- 流式响应开关 -->
      <div class="setting-item">
        <div class="setting-label-row">
          <div class="label-with-tooltip">
            <span>流式响应</span>
            <el-tooltip content="开启后将流式响应 AI 的回复" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <el-switch v-model="settingStore.settings.stream" />
        </div>
      </div>

      <!-- Max Tokens -->
      <div class="setting-item">
        <div class="setting-label">
          Max Tokens
          <el-tooltip content="生成文本的最大长度" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="setting-control">
          <el-slider
            v-model="settingStore.settings.maxTokens"
            :min="1"
            :max="currentMaxTokens"
            :step="1"
            :show-tooltip="false"
            class="setting-slider"
          />
          <el-input-number
            v-model="settingStore.settings.maxTokens"
            :min="1"
            :max="currentMaxTokens"
            :step="1"
            controls-position="right"
          />
        </div>
      </div>

      <!-- Temperature -->
      <div class="setting-item">
        <div class="setting-label">
          Temperature
          <el-tooltip content="值越高，回答越随机" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="setting-control">
          <el-slider
            v-model="settingStore.settings.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            :show-tooltip="false"
            class="setting-slider"
          />
          <el-input-number
            v-model="settingStore.settings.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            controls-position="right"
          />
        </div>
      </div>

      <!-- Top-P -->
      <div class="setting-item">
        <div class="setting-label">
          Top-P
          <el-tooltip content="核采样阈值" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="setting-control">
          <el-slider
            v-model="settingStore.settings.topP"
            :min="0"
            :max="1"
            :step="0.1"
            :show-tooltip="false"
            class="setting-slider"
          />
          <el-input-number
            v-model="settingStore.settings.topP"
            :min="0"
            :max="1"
            :step="0.1"
            controls-position="right"
          />
        </div>
      </div>

      <!-- Top-K -->
      <div class="setting-item">
        <div class="setting-label">
          Top-K
          <el-tooltip content="保留概率最高的 K 个词" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="setting-control">
          <el-slider
            v-model="settingStore.settings.topK"
            :min="1"
            :max="100"
            :step="1"
            :show-tooltip="false"
            class="setting-slider"
          />
          <el-input-number
            v-model="settingStore.settings.topK"
            :min="1"
            :max="100"
            :step="1"
            controls-position="right"
          />
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<style lang="scss" scoped>
.setting-container {
  padding: 20px;
  color: #27272a;
}

.setting-item {
  margin-bottom: 24px;

  // 基础标签样式
  .setting-label {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-weight: 500;
    color: #27272a;
  }

  // 水平布局的标签行，用于标签和控件在同一行的情况
  .setting-label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    color: #27272a;

    // 标签和提示图标的容器
    .label-with-tooltip {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    // 获取 API Key 链接样式
    .get-key-link {
      font-size: 14px;
      color: #3f7af1;
      text-decoration: none;
    }
  }

  // 控件容器样式，用于包含滑块和数字输入框
  .setting-control {
    display: flex;
    align-items: center;
    gap: 16px;

    // 滑块占据剩余空间
    .setting-slider {
      flex: 1;
    }

    // 数字输入框固定宽度
    :deep(.el-input-number) {
      width: 120px;
    }
  }

  // 模型选择下拉框宽度
  .model-select {
    width: 100%;
  }

  // 下拉选项文字颜色
  :deep(.el-select-dropdown__item) {
    color: #27272a;
  }
}

.setting-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.setting-hint a {
  color: #3f7af1;
  text-decoration: none;
}

.get-key-link {
  font-size: 13px;
  color: #3f7af1;
  text-decoration: none;
}

.provider-key-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}

.provider-key-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .provider-label {
    min-width: 80px;
    font-size: 13px;
    color: #606266;
    font-weight: 500;
  }

  :deep(.el-input) {
    flex: 1;
  }
}
</style>
