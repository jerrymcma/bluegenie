import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0', // This allows access from other devices on your network
    open: true,
    strictPort: false // If port 3000 is busy, try the next available port
  },
  define: {
    // Make environment variables available in the app
    'process.env.VITE_GEMINI_API_KEY': JSON.stringify(process.env.VITE_GEMINI_API_KEY || ''),
    'process.env.VITE_CLAUDE_API_KEY': JSON.stringify(process.env.VITE_CLAUDE_API_KEY || ''),
    'process.env.VITE_OPENAI_API_KEY': JSON.stringify(process.env.VITE_OPENAI_API_KEY || ''),
    'process.env.VITE_REPLICATE_API_KEY': JSON.stringify(process.env.VITE_REPLICATE_API_KEY || '')
  }
})
