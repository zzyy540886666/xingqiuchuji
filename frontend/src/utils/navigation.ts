import { useNavigationStore, type NavigationMethod } from "../stores/navigation";

const TAB_PAGE_PATHS = new Set([
  "/pages/home/index",
  "/pages/category/index",
  "/pages/community/index",
  "/pages/profile/index",
]);

type NavigateOptions = {
  replace?: boolean;
  relaunch?: boolean;
  reason?: string;
};

type NavigationCallbacks = {
  success: () => void;
  complete: () => void;
  fail: (error: UniApp.GeneralCallbackResult) => void;
};

let lockTimer: ReturnType<typeof setTimeout> | undefined;

export function isTabPageUrl(url: string) {
  return TAB_PAGE_PATHS.has(splitUrl(url).path);
}

export function navigateToPage(url: string, options: NavigateOptions = {}) {
  const method = resolveMethod(url, options);
  if (!method) return;

  runNavigation(url, method, options.reason);
}

export function redirectToPage(url: string, reason?: string) {
  navigateToPage(url, { replace: true, reason });
}

export function reLaunchToPage(url: string, reason?: string) {
  navigateToPage(url, { relaunch: true, reason });
}

export function navigateBackOrHome(reason?: string) {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    runNavigation("BACK", "navigateBack", reason);
    return;
  }

  reLaunchToPage("/pages/home/index", reason || "fallback_home");
}

function resolveMethod(url: string, options: NavigateOptions): NavigationMethod | null {
  if (!url) return null;

  const { path, query } = splitUrl(url);
  if (options.relaunch) return "reLaunch";
  if (TAB_PAGE_PATHS.has(path)) {
    return query ? "reLaunch" : "switchTab";
  }
  return options.replace ? "redirectTo" : "navigateTo";
}

function runNavigation(url: string, method: NavigationMethod, reason?: string) {
  const store = useNavigationStore();
  if (store.isNavigating) {
    store.block(url, "navigation_in_flight");
    return;
  }

  const processId = store.start(url, method, reason);
  const callbacks = createCallbacks(processId);

  if (lockTimer) clearTimeout(lockTimer);
  lockTimer = setTimeout(() => {
    store.resetActive();
  }, 1500);

  if (method === "switchTab") {
    uni.switchTab({ url, ...callbacks });
    return;
  }
  if (method === "redirectTo") {
    uni.redirectTo({ url, ...callbacks });
    return;
  }
  if (method === "reLaunch") {
    uni.reLaunch({ url, ...callbacks });
    return;
  }
  if (method === "navigateBack") {
    uni.navigateBack({ delta: 1, ...callbacks });
    return;
  }

  uni.navigateTo({ url, ...callbacks });
}

function createCallbacks(processId: number): NavigationCallbacks {
  const store = useNavigationStore();
  const release = () => {
    if (lockTimer) {
      clearTimeout(lockTimer);
      lockTimer = undefined;
    }
    setTimeout(() => store.resetActive(), 120);
  };

  return {
    success() {
      store.settle(processId);
    },
    complete() {
      release();
    },
    fail(error) {
      store.fail(processId, error.errMsg);
    },
  };
}

function splitUrl(url: string) {
  const [path, query = ""] = url.split("?", 2);
  return { path, query };
}
