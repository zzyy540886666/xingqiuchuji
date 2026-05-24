import { defineStore } from "pinia";
import { ref } from "vue";
import { request } from "../utils/request";

export interface CurrentUser {
  id: string;
  nickname: string;
  avatarUrl: string;
  membershipLevel: number;
  isNativeResident: boolean;
}

export const useSessionStore = defineStore("session", () => {
  const token = ref(uni.getStorageSync("xq_access_token") || "");
  const user = ref<CurrentUser | null>(null);
  const inviteCode = ref("");

  function setToken(t: string) {
    token.value = t;
    uni.setStorageSync("xq_access_token", t);
  }

  function clearSession() {
    token.value = "";
    user.value = null;
    uni.removeStorageSync("xq_access_token");
  }

  async function loginByWechat() {
    const loginResult = await uni.login({ provider: "weixin" });
    if (!loginResult.code) {
      throw new Error("未获得微信登录凭证");
    }
    const res = await request<{ accessToken: string; user: CurrentUser }>("/auth/wechat", {
      method: "POST",
      data: { code: loginResult.code },
    });
    setToken(res.accessToken);
    user.value = res.user;
    await fetchUser();
    if (inviteCode.value) {
      await bindInvite();
    }
  }

  async function fetchUser() {
    user.value = await request<CurrentUser>("/users/me");
  }

  async function bindInvite() {
    if (!inviteCode.value) return;
    try {
      await request("/distribution/bind", {
        method: "POST",
        data: { inviteCode: inviteCode.value },
      });
    } catch {
      // non-critical
    }
  }

  function parseInviteCode(options?: Record<string, string>) {
    const code = options?.inviteCode || options?.scene || "";
    if (code) {
      inviteCode.value = code;
      uni.setStorageSync("xq_invite_code", code);
    }
  }

  return { token, user, inviteCode, setToken, clearSession, loginByWechat, fetchUser, parseInviteCode, bindInvite };
});
