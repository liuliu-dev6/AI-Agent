/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'markdown-it-link-attributes' {
  import type MarkdownIt from 'markdown-it'
  interface LinkAttributesOptions {
    attrs?: Record<string, string>
  }
  function mdLinkAttributes(md: MarkdownIt, options?: LinkAttributesOptions): void
  export default mdLinkAttributes
}

declare module 'markdown-it-emoji' {
  import type MarkdownIt from 'markdown-it'
  interface EmojiOptions {
    defs?: Record<string, string>
    shortcuts?: Record<string, string>
  }
  const full: { (md: MarkdownIt, options?: EmojiOptions): void }
  export { full }
}
