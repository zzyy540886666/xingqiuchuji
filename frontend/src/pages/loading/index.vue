<template>
  <view class="splash-page">
    <image
      class="splash-image"
      src="/static/images/opening-screen-full.png"
      mode="aspectFill"
    />
  </view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { getActivities } from "../../services/activity";
import { getScenes, getSkuList } from "../../services/catalog";
import { useConfigStore } from "../../stores/config";
import { useSessionStore } from "../../stores/session";
import { redirectToPage } from "../../utils/navigation";

const configStore = useConfigStore();
const sessionStore = useSessionStore();
let targetUrl = "/pages/home/index";

onLoad((options) => {
  const redirect =
    typeof options?.redirect === "string" ? decodeURIComponent(options.redirect) : "";
  if (redirect.startsWith("/pages/")) {
    targetUrl = redirect;
  }
  startLoading();
});

async function startLoading() {
  const minSplashDuration = delay(900);

  const token = uni.getStorageSync("xq_access_token");
  if (token) {
    sessionStore.setToken(token);
  }

  await Promise.allSettled([
    minSplashDuration,
    warmConfig(),
    warmSession(),
    warmContent(),
  ]);

  enterApp();
}

async function warmConfig() {
  await withTimeout(configStore.fetchConfig(), 3500);
}

async function warmSession() {
  if (!sessionStore.token) return;
  try {
    await withTimeout(sessionStore.fetchUser(), 3500);
  } catch {
    sessionStore.clearSession();
  }
}

async function warmContent() {
  await withTimeout(
    Promise.allSettled([
      getActivities(),
      getScenes(),
      getSkuList({ type: "RENT", pageSize: 4 }),
      getSkuList({ type: "BUY", pageSize: 4 }),
    ]),
    5000
  );
}

function enterApp() {
  const url = targetUrl || "/pages/home/index";
  redirectToPage(url, "splash_ready");
}

function delay(ms: number) {
  return new Promise<void>((resolve) => setTimeout(resolve, ms));
}

async function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  let timer: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timer = setTimeout(() => reject(new Error("timeout")), ms);
  });

  try {
    return await Promise.race([promise, timeout]);
  } finally {
    if (timer) clearTimeout(timer);
  }
}
</script>

<style scoped lang="scss">
.splash-page {
  width: 100vw;
  height: 100vh;
  background: #eff3fb;
  overflow: hidden;
}

.splash-image {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
