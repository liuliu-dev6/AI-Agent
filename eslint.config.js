import js from '@eslint/js'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default [
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,mjs,jsx}'],
  },

  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/dist-ssr/**', '**/coverage/**', '**/*.ts', '**/*.tsx', '**/*.d.ts', '**/*.vue'],
  },

  js.configs.recommended,
  skipFormatting,
]
