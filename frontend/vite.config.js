import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
export default defineConfig({ plugins: [react()], test: { environment: 'jsdom' }, build: { rollupOptions: { output: { manualChunks(id) { if (id.includes('/node_modules/recharts/')) return 'recharts'; if (id.includes('/node_modules/d3-')) return 'd3'; } } } } })
