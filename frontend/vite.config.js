import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
export default defineConfig({
  // GitHub Pages hosts this project under /campus-library/. Keep the normal
  // root path for local development and other hosts such as Vercel.
  base: process.env.GITHUB_PAGES === 'true' ? '/campus-library/' : '/',
  plugins: [react()],
  test: { environment: 'jsdom' },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('/node_modules/recharts/')) return 'recharts'
          if (id.includes('/node_modules/d3-')) return 'd3'
        },
      },
    },
  },
})
