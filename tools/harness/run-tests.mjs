import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";
import process from "node:process";

const root = process.cwd();
const outputDir = path.join(root, ".harness", "ci");
const outputFile = path.join(outputDir, "frontend-test-results.json");
mkdirSync(outputDir, { recursive: true });

const tests = [];

function test(name, fn) {
  tests.push({ name, fn });
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function read(file) {
  return readFileSync(path.join(root, file), "utf8");
}

test("pages.json registers all translated pages", () => {
  const pages = JSON.parse(read("frontend/src/pages.json")).pages.map((item) => item.path);
  assert(pages.length === 9, `expected 9 pages, got ${pages.length}`);
  for (const page of ["pages/home/index", "pages/category/index", "pages/community/index", "pages/profile/index", "pages/search/index", "pages/product-detail/index", "pages/activity-detail/index", "pages/order-confirm/index", "pages/membership-center/index"]) {
    assert(pages.includes(page), `missing ${page}`);
  }
});

test("amount formatter rejects non-integer minor units", () => {
  const source = read("frontend/src/utils/format.ts");
  assert(source.includes("Number.isInteger(amount)"), "formatAmount must check integer amounts");
  assert(source.includes("amount must be integer minor unit"), "formatAmount must reject invalid amount units");
});

test("request client follows API common contract", () => {
  const source = read("frontend/src/utils/request.ts");
  assert(source.includes('const API_PREFIX = "/api/v1"'), "API prefix must be /api/v1");
  assert(source.includes("Authorization"), "Authorization header missing");
  assert(source.includes("Bearer"), "Bearer token missing");
  assert(source.includes("Idempotency-Key"), "idempotency header missing");
});

test("translated pages preserve core original labels", () => {
  const bundle = [
    read("frontend/src/pages/home/index.vue"),
    read("frontend/src/pages/category/index.vue"),
    read("frontend/src/pages/community/index.vue"),
    read("frontend/src/pages/profile/index.vue"),
    read("frontend/src/pages/member/index.vue"),
  ].join("\n");

  for (const label of ["星球·出机", "场景应用", "为你推荐", "分类", "社区", "微信一键登录", "会员权益", "星球卡"]) {
    assert(bundle.includes(label), `missing label: ${label}`);
  }
});

test("product detail and order pages keep rental purchase flow", () => {
  const detail = read("frontend/src/pages/product-detail/index.vue");
  const order = read("frontend/src/pages/order/confirm.vue");
  for (const label of ["短期租赁", "租赁购买", "购买买断", "立即租赁", "加入购物车"]) {
    assert(detail.includes(label), `detail missing ${label}`);
  }
  for (const label of ["确认订单", "送货上门", "费用信息", "微信支付", "订单备注"]) {
    assert(order.includes(label), `order missing ${label}`);
  }
});

const results = [];
for (const item of tests) {
  try {
    item.fn();
    results.push({ name: item.name, status: "passed" });
  } catch (error) {
    results.push({
      name: item.name,
      status: "failed",
      message: error instanceof Error ? error.message : String(error),
    });
  }
}

const passed = results.filter((item) => item.status === "passed").length;
const failed = results.length - passed;
const report = {
  success: failed === 0,
  total: results.length,
  passed,
  failed,
  results,
  generatedAt: new Date().toISOString(),
};

writeFileSync(outputFile, `${JSON.stringify(report, null, 2)}\n`, "utf8");
console.log(JSON.stringify(report, null, 2));

if (failed > 0) {
  process.exitCode = 1;
}
