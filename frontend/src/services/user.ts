import { request } from "../utils/request";

export interface UserSummary {
  id: string;
  nickname: string;
  avatarUrl: string;
  membershipLevel: number;
  isNativeResident: boolean;
}

export function getUserMe(): Promise<UserSummary> {
  return request<UserSummary>("/users/me");
}

export function updateUserMe(data: { nickname?: string; avatarUrl?: string }): Promise<UserSummary> {
  return request<UserSummary>("/users/me", { method: "PUT", data });
}

export function uploadAvatar(filePath: string): Promise<{ url: string }> {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/api/v1/upload/image`,
      filePath,
      name: "file",
      header: {
        Authorization: `Bearer ${uni.getStorageSync("xq_access_token")}`,
      },
      success(res) {
        try {
          const body = JSON.parse(res.data);
          if (body.success) {
            resolve(body.data as { url: string });
          } else {
            reject(new Error(body.error?.message || "上传失败"));
          }
        } catch {
          reject(new Error("上传返回格式异常"));
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || "上传失败"));
      },
    });
  });
}
