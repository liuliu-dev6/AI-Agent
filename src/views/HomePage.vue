<script setup lang="ts">
import { ArrowRight, ChatDotRound, DataAnalysis, Coin, Tools } from '@element-plus/icons-vue'

const features = [
  { icon: ChatDotRound, title: '多模型对话', desc: 'Doubao / DeepSeek / GPT / Qwen / GLM' },
  { icon: Tools, title: 'Tool Calling', desc: '计算器、天气查询，Agent 自动调用工具' },
  { icon: DataAnalysis, title: '意图识别', desc: '自动识别用户意图并路由到正确能力' },
  { icon: Coin, title: '低成本运行', desc: 'Prompt 分类 + 缓存，减少重复调用' },
]
</script>

<template>
  <div class="home">
    <div class="home-card">
      <div class="brand">
        <div class="logo">AS</div>
        <h1>AI Agent Studio</h1>
      </div>

      <p class="tagline">不只是聊天，而是真正的 AI Agent</p>

      <router-link to="/chat" class="enter-btn">
        <span>进入工作台</span>
        <el-icon><ArrowRight /></el-icon>
      </router-link>

      <div class="features">
        <div v-for="f in features" :key="f.title" class="feature-item">
          <el-icon class="feature-icon"><component :is="f.icon" /></el-icon>
          <div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  padding: 24px;
}

.home-card {
  width: 480px;
  padding: 48px 40px 40px;
  background: #fff;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 12px;
}

.logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #2563eb;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand h1 {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.tagline {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 32px;
}

.enter-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 48px;
  padding: 0 32px;
  border-radius: 12px;
  background: #2563eb;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s;

  &:hover {
    background: #1d4ed8;
  }
}

.features {
  margin-top: 40px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.feature-icon {
  font-size: 20px;
  color: #2563eb;
  margin-top: 1px;
  flex-shrink: 0;
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.feature-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
</style>
