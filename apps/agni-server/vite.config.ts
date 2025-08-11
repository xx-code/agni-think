import { defineConfig } from 'vitest/config'
import path from 'path';

export default defineConfig({
  test: {
    globals: true, 
    include: ['src/**/*.spec.ts'],
    environment: 'node',
  },
   resolve: {
    alias: {
      '@core': path.resolve(__dirname, 'src/core'),
    },
  },
});