import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";

export default defineConfig({
  plugins: [react()],
  define: {
    global: "window",
  },
  server: {
    allowedHosts: ["bigcie.cimpo1.com", "localhost", "127.0.0.1"],
    proxy: {
      "/api": {
        target: "http://107.172.139.134:8080",
        changeOrigin: true,
        secure: false,
      },
      "/ws": {
        target: "ws://107.172.139.134:8080",
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
