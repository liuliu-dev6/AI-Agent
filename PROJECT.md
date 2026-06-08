# AI Agent Studio

> 从聊天机器人升级为可落地的企业级 AI Agent 平台

## 项目定位

一个具备意图识别、工具调用（Tool Calling）、多模型网关的 AI Agent 运行平台。区别于普通聊天应用，它实现了完整的 **Agent 执行循环**（感知 → 意图路由 → 工具执行 → 再决策 → 回复），可以在面试中充分展示 AI 工程化的技术深度。

## 技术架构

```
┌─────────────────────────────────────────────┐
│                  前端 (Vue 3)                │
│  TypeScript + Pinia + Element Plus + Vite   │
│  ChatView / SettingsPanel / IntentBadge     │
│  ToolCallCard / ChatMessage (Markdown)      │
└──────────────────┬──────────────────────────┘
                   │ REST API (Vite Proxy)
┌──────────────────▼──────────────────────────┐
│              Spring Boot 3.3                │
│  ┌──────────┐ ┌───────────┐ ┌────────────┐ │
│  │ Intent   │ │  Model    │ │   Tool     │ │
│  │ Service  │ │  Gateway  │ │  Service   │ │
│  │ 关键词+  │ │ 统一适配  │ │  注册+执行  │ │
│  │ LLM分类  │ │ OpenAI兼容│ │  calculator│ │
│  └──────────┘ └───────────┘ │  weather   │ │
│                              └────────────┘ │
│              MySQL (会话/消息/运行记录)        │
└─────────────────────────────────────────────┘
```

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + TypeScript | Composition API + `<script setup>` |
| 状态管理 | Pinia + persist 插件 | 模块化 Store，localStorage 自动持久化 |
| UI 组件库 | Element Plus | 抽屉、对话框、表单、消息提示 |
| 构建工具 | Vite 6 | HMR、路径别名、API 代理 |
| Markdown | markdown-it + highlight.js | 代码高亮、emoji、链接自动识别 |
| 后端框架 | Spring Boot 3.3 | REST API + JPA + Actuator |
| 数据库 | MySQL 8.0 + Hibernate | DDL 自动建表、MEDIUMTEXT 大字段 |
| HTTP 客户端 | RestClient + HttpURLConnection | 120s 读取超时，多供应商代理 |
| LLM 协议 | OpenAI Compatible API | 一套适配器覆盖 Doubao/DeepSeek/Qwen/GLM |

## 核心模块

### 1. 意图识别引擎（IntentService）

```
用户输入 → 关键词匹配（毫秒级）→ 命中 → 直接路由
                              → 未命中 → LLM 分类（仅当有 API Key）→ 路由
                              → 缓存命中 → 直接返回
```

- 支持 6 种意图：chat / knowledge_search / tool_call / code_analysis / workflow / sql_query
- LRU 缓存（LinkedHashMap access-order，上限 500 条）
- 关键词预判覆盖 85%+ 场景（天气、计算、代码、SQL 等）

### 2. 多模型统一网关（ModelGatewayService）

- 根据 model 名称自动解析供应商（doubao → 豆包、gpt → OpenAI、deepseek → DeepSeek）
- 全部使用 OpenAI 兼容格式，一套代码适配所有模型
- HttpURLConnection 底层实现，10s 连接超时 + 120s 读取超时
- 支持从请求中动态传入 API Key 和 Model

### 3. Tool Calling 系统

完整的 Agent 工具调用循环：

```
LLM 请求（带 tools 定义）
  → LLM 返回 finish_reason=tool_calls
  → ToolService 执行对应工具
  → 工具结果追加到对话历史
  → 再次调用 LLM 生成最终回复
  → 最多 5 轮循环
```

内置工具：
- **Calculator**：安全的数学表达式计算（Javax Script 引擎，沙箱隔离）
- **Weather**：wttr.in 免费 API，支持全球城市实时天气

### 4. Agent 执行循环（AgentWorkspaceService.run）

```
1. 提取用户输入 + API Key + Model
2. 意图识别（关键词匹配）
3. 构建对话消息（System Prompt + 历史 + 用户输入）
4. 调用 LLM（带工具定义）
5. 如果有 tool_calls → 执行工具 → 回到步骤 4
6. 返回最终回复 + 运行链路
7. 持久化消息和运行记录到 MySQL
```

### 5. 前端亮点

- **TypeScript 全量覆盖**：完整的类型定义体系（Agent/Chat/LLM/Intent/Tool）
- **IntentBadge 组件**：根据意图类型显示不同颜色徽章
- **ToolCallCard 组件**：可展开/收起的工具调用卡片，展示参数和结果
- **SettingsPanel**：支持多供应商独立 API Key 配置
- **响应式布局**：侧边栏 + 对话区 + 检查面板三栏布局

## 数据流

```
浏览器输入"长沙的天气"
  → POST /api/agent-workspace/runs
    body: { message, history, apiKey, model }
  → Vite Proxy → Spring Boot
  → IntentService.classify() → intent: "tool_call"
  → ModelGateway.chatSync() → POST https://ark.cn-beijing.volces.com/api/v3/chat/completions
    body: { model, messages, tools: [calculator, get_weather] }
  → LLM Response: { tool_calls: [{ function: { name: "get_weather", arguments: {city:"长沙"} } }] }
  → WeatherTool.execute({city:"长沙"}) → GET https://wttr.in/长沙?format=j1
  → 将工具结果加入对话 → 再次调用 LLM 生成自然语言回复
  → Response: { assistantMessage: "长沙今天晴朗，温度22°C...", intent, tools, metrics }
  → 前端渲染 Markdown + IntentBadge + ToolCallCard
```

## 快速启动

```bash
# 1. 启动后端（需要 Java 17+ / MySQL 8.0+）
cd backend
mvn spring-boot:run

# 2. 启动前端
npm run dev

# 3. 打开 http://localhost:5173

# 4. 首次启动后，将 application.yml 中 ddl-auto 改回 update
```

## 项目结构

```
llm-chat-box2.0-main/
├── src/                          # 前端
│   ├── types/                    # TypeScript 类型定义
│   │   ├── agent.ts / chat.ts / llm.ts / intent.ts / tool.ts
│   ├── services/api.ts           # API 调用（对接后端）
│   ├── stores/                   # Pinia 状态管理
│   │   ├── chat.ts / setting.ts / agent.ts
│   ├── views/
│   │   ├── HomePage.vue          # 简洁卡片式首页
│   │   └── ChatView.vue          # 主工作台
│   ├── components/
│   │   ├── ChatMessage.vue       # 消息气泡（Markdown + 工具卡片 + 意图标签）
│   │   ├── ChatInput.vue         # 输入框（支持文件上传）
│   │   ├── SettingsPanel.vue     # 多模型设置抽屉
│   │   ├── ToolCallCard.vue      # 工具调用卡片
│   │   └── IntentBadge.vue       # 意图标签
│   └── utils/
│       ├── messageHandler.ts     # 流式/非流式响应处理
│       └── markdown.ts           # markdown-it 渲染
├── backend/                      # 后端 Spring Boot
│   └── src/main/java/com/aiagentstudio/
│       ├── controller/           # REST API
│       ├── service/              # Agent/Intent/ModelGateway/Tool
│       ├── tool/                 # Calculator / Weather
│       ├── model/ / repo/        # JPA Entity + Repository
│       └── dto/                  # Request/Response DTO
└── vite.config.ts                # Vite 配置（API 代理）
```
