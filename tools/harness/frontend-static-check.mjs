import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import process from "node:process";

const root = process.cwd();

const requiredFiles = [
  "frontend/package.json",
  "frontend/src/main.ts",
  "frontend/src/App.vue",
  "frontend/src/pages.json",
  "frontend/src/manifest.json",
  "frontend/src/components/BottomNav.vue",
  "frontend/src/components/ProductCard.vue",
  "frontend/src/utils/request.ts",
  "frontend/src/utils/format.ts",
  "frontend/src/pages/home/index.vue",
  "frontend/src/pages/category/index.vue",
  "frontend/src/pages/community/index.vue",
  "frontend/src/pages/profile/index.vue",
  "frontend/src/pages/search/index.vue",
  "frontend/src/pages/product-detail/index.vue",
  "frontend/src/pages/order-confirm/index.vue",
  "frontend/src/pages/membership-center/index.vue",
];

const missing = requiredFiles.filter((file) => !existsSync(path.join(root, file)));
if (missing.length > 0) {
  throw new Error(`Missing required frontend files: ${missing.join(", ")}`);
}

const pagesJson = JSON.parse(readFileSync(path.join(root, "frontend/src/pages.json"), "utf8"));
const pagePaths = pagesJson.pages.map((item) => item.path);
const requiredPages = [
  "pages/home/index",
  "pages/category/index",
  "pages/community/index",
  "pages/profile/index",
  "pages/search/index",
  "pages/product-detail/index",
  "pages/order-confirm/index",
  "pages/membership-center/index",
];

const missingPages = requiredPages.filter((page) => !pagePaths.includes(page));
if (missingPages.length > 0) {
  throw new Error(`Missing page registrations: ${missingPages.join(", ")}`);
}

const requestSource = readFileSync(path.join(root, "frontend/src/utils/request.ts"), "utf8");
for (const required of ["/api/v1", "Authorization", "Bearer", "Idempotency-Key", "timeout: 10000"]) {
  if (!requestSource.includes(required)) {
    throw new Error(`request.ts missing contract fragment: ${required}`);
  }
}

const formatSource = readFileSync(path.join(root, "frontend/src/utils/format.ts"), "utf8");
if (!formatSource.includes("Number.isInteger")) {
  throw new Error("format.ts must validate integer minor-unit amounts");
}

console.log("frontend static check passed");
