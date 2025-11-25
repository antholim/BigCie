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
        target: "https://backend.cimpo1.com",
        changeOrigin: true,
        secure: false,
      },
      "/ws": {
        target: "wss://backend.cimpo1.com",
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
