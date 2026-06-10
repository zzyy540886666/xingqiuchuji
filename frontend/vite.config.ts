import { defineConfig } from "vite";
import uniModule from "@dcloudio/vite-plugin-uni";

const uni = (uniModule as any).default || uniModule;

export default defineConfig({
  plugins: [uni()],
  css: {
    preprocessorOptions: {
      scss: {
        // Uni's current Vite integration still invokes Sass through the legacy API.
        silenceDeprecations: ["legacy-js-api"],
      },
    },
  },
});
