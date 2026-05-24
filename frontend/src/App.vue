<script setup lang="ts">
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useSessionStore } from "./stores/session";
import { useConfigStore } from "./stores/config";

onLaunch((options: Record<string, string> | undefined) => {
  const sessionStore = useSessionStore();
  const configStore = useConfigStore();

  sessionStore.parseInviteCode(options as Record<string, string>);
  configStore.fetchConfig();

  const token = uni.getStorageSync("xq_access_token");
  if (token) {
    sessionStore.setToken(token);
    sessionStore.fetchUser().catch(() => {});
  }
});

onShow(() => {
  uni.setStorageSync("xq_last_show_at", new Date().toISOString());
});
</script>

<style lang="scss">
page {
  background: #f5f6f8;
  color: #111827;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Arial, sans-serif;
}
</style>
