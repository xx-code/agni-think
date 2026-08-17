import { defineConfig } from 'vitest/config'
import { resolve } from 'path'
import { defineVitestProject } from '@nuxt/test-utils/config'

const alias = {
  '~': resolve(import.meta.dirname, '.'),
  '@': resolve(import.meta.dirname, '.'),
}

export default defineConfig({
  test: {
    projects: [
      {
        resolve: { alias },
        test: {
          name: 'unit',
          include: ['test/unit/*.{test,spec}.ts'],
          environment: 'node',
        },
      },
      {
        resolve: { alias },
        test: {
          name: 'e2e',
          include: ['test/e2e/*.{test,spec}.ts'],
          environment: 'node',
        },
      },
      await defineVitestProject({
        resolve: { alias },
        test: {
          name: 'nuxt',
          include: ['test/nuxt/*.{test,spec}.ts'],
          environment: 'nuxt',
        },
      }),
    ],
  },
})
