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
  const config = JSON.parse(read("frontend/src/pages.json"));
  const pages = config.pages.map((item) => item.path);
  for (const page of ["pages/home/index", "pages/category/index", "pages/community/index", "pages/profile/index", "pages/search/index", "pages/product-detail/index", "pages/activity-detail/index", "pages/order-confirm/index", "pages/membership-center/index"]) {
    assert(pages.includes(page), `missing ${page}`);
  }
  assert(config.preloadRule?.["pages/profile/index"], "profile preload rule missing");
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

  for (const label of ["星球", "出机", "场景应用", "为你推荐", "分类", "社区", "会员权益", "星球卡"]) {
    assert(bundle.includes(label), `missing label: ${label}`);
  }
  const profile = read("frontend/src/pages/profile/index.vue");
  assert(profile.includes("登录 / 注册"), "profile login entry missing");
  assert(profile.includes("loginByWechat"), "wechat login action missing");
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

test("order center uses live paginated data instead of mock orders", () => {
  const source = read("frontend/src/pages/order/list.vue");
  for (const required of ["getOrderList", "getSkuDetail", "fetchOrders", "hasMore", "refresher-enabled"]) {
    assert(source.includes(required), `order list missing ${required}`);
  }
  assert(!source.includes("mockOrders"), "order list must not render mock orders");
  assert(source.includes("/pages/order/detail?id="), "order detail navigation missing");
});

test("home supports pull-down refresh with request race protection", () => {
  const source = read("frontend/src/pages/home/index.vue");
  const pages = JSON.parse(read("frontend/src/pages.json"));
  const home = pages.pages.find((item) => item.path === "pages/home/index");
  assert(home?.style?.enablePullDownRefresh === true, "home pull-down refresh not enabled");
  assert(source.includes("onPullDownRefresh"), "home refresh lifecycle missing");
  assert(source.includes("productRequestId"), "home product request race protection missing");
  assert(source.includes("uni.stopPullDownRefresh()"), "home refresh completion missing");
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
