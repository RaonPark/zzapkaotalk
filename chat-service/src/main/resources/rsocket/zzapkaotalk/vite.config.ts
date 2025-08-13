import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    'global': {},
  },
  server: {
    proxy: {
        '/checkLogin': {
            target: 'http://localhost:8084',
            changeOrigin: true,
            secure: false
        },
        '/chat-service': {
            target: 'http://localhost:8084',
            changeOrigin: true,
            secure: false,
        }
    }
  }
})
