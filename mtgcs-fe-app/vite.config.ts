import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': { target: 'http://localhost:8090', changeOrigin: true },
      '/oauth2': { target: 'http://localhost:8090', changeOrigin: true },
      '/login': { target: 'http://localhost:8090', changeOrigin: true },
      '/logout': { target: 'http://localhost:8090', changeOrigin: true },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
  },
})
