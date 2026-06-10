<template>
  <view class="page-skeleton" :class="resolvedVariant">
    <template v-if="resolvedVariant === 'home'">
      <view class="s-search"></view>
      <view class="s-hero"></view>
      <view class="s-grid three mt-20">
        <view v-for="i in 3" :key="i" class="s-card mini"></view>
      </view>
      <view class="s-section-head"></view>
      <view class="s-grid two">
        <view v-for="i in 2" :key="i" class="s-card product">
          <view class="s-thumb"></view>
          <view class="s-line w-76"></view>
          <view class="s-line w-52 sm"></view>
          <view class="s-row gap-8">
            <view class="s-pill w-36"></view>
            <view class="s-pill w-30"></view>
          </view>
          <view class="s-line w-40"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'category'">
      <view class="s-search"></view>
      <view class="s-row gap-12 mt-20">
        <view v-for="i in 3" :key="i" class="s-pill w-24 lg"></view>
      </view>
      <view class="s-category-shell">
        <view class="s-side">
          <view v-for="i in 5" :key="i" class="s-side-item"></view>
        </view>
        <view class="s-main">
          <view class="s-filter-row">
            <view v-for="i in 4" :key="i" class="s-pill w-20"></view>
          </view>
          <view v-for="i in 3" :key="i" class="s-card list-card">
            <view class="s-thumb wide"></view>
            <view class="s-col grow">
              <view class="s-line w-80"></view>
              <view class="s-line w-52 sm"></view>
              <view class="s-row gap-8">
                <view class="s-pill w-32"></view>
                <view class="s-pill w-28"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'search'">
      <view class="s-block">
        <view class="s-section-head"></view>
        <view class="s-chip-wrap">
          <view v-for="i in 8" :key="i" class="s-chip"></view>
        </view>
      </view>
      <view class="s-block">
        <view class="s-section-head short"></view>
        <view class="s-search-list">
          <view v-for="i in 6" :key="i" class="s-row search-item">
            <view class="s-rank"></view>
            <view class="s-line grow"></view>
            <view class="s-pill w-12"></view>
          </view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'product-detail'">
      <view class="s-hero detail-hero"></view>
      <view class="s-row detail-float">
        <view class="s-pill w-16"></view>
        <view class="s-pill w-16"></view>
      </view>
      <view class="s-card detail-card">
        <view class="s-row gap-12">
          <view class="s-line w-70"></view>
          <view class="s-pill w-18"></view>
        </view>
        <view class="s-line w-48 sm"></view>
        <view class="s-row gap-8 mt-12">
          <view v-for="i in 4" :key="i" class="s-pill w-18"></view>
        </view>
        <view class="s-line w-88 mt-12"></view>
        <view class="s-line w-72 sm"></view>
        <view class="s-row gap-12 mt-16">
          <view v-for="i in 3" :key="i" class="s-pill w-20 lg"></view>
        </view>
        <view class="s-card light mt-16">
          <view class="s-line w-36 sm"></view>
          <view class="s-line w-72"></view>
          <view class="s-line w-56 sm"></view>
        </view>
        <view class="s-card light mt-16">
          <view class="s-line w-30 sm"></view>
          <view class="s-line w-84"></view>
          <view class="s-line w-46 sm"></view>
        </view>
      </view>
      <view class="s-bottom-bar">
        <view class="s-pill w-18 lg"></view>
        <view class="s-pill w-18 lg"></view>
        <view class="s-pill w-28 lg primary"></view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'order-confirm'">
      <view class="s-card detail-card">
        <view class="s-row gap-10">
          <view class="s-avatar sm"></view>
          <view class="s-col grow">
            <view class="s-line w-38"></view>
            <view class="s-line w-70 sm"></view>
          </view>
          <view class="s-pill w-16"></view>
        </view>
      </view>
      <view class="s-card detail-card">
        <view class="s-row gap-12">
          <view class="s-thumb wide small"></view>
          <view class="s-col grow">
            <view class="s-line w-64"></view>
            <view class="s-line w-48 sm"></view>
            <view class="s-row gap-8 mt-8">
              <view class="s-pill w-20"></view>
              <view class="s-pill w-20"></view>
            </view>
            <view class="s-line w-30 sm mt-10"></view>
          </view>
        </view>
        <view class="s-line w-42 sm mt-16"></view>
        <view class="s-row gap-10 mt-10">
          <view v-for="i in 4" :key="i" class="s-pill w-18"></view>
        </view>
        <view class="s-row list-row" v-for="i in 4" :key="i">
          <view class="s-line w-26 sm"></view>
          <view class="s-line w-34"></view>
        </view>
      </view>
      <view class="s-card detail-card">
        <view class="s-line w-28 sm"></view>
        <view class="s-row list-row" v-for="i in 3" :key="i">
          <view class="s-line w-26 sm"></view>
          <view class="s-line w-34"></view>
        </view>
      </view>
      <view class="s-bottom-bar">
        <view class="s-col grow">
          <view class="s-line w-20 sm"></view>
          <view class="s-line w-36"></view>
        </view>
        <view class="s-pill w-28 lg primary"></view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'order-list'">
      <view class="s-row gap-10 mt-20">
        <view v-for="i in 4" :key="i" class="s-pill w-18 lg"></view>
      </view>
      <view class="s-row gap-10 mt-16">
        <view v-for="i in 6" :key="i" class="s-pill w-16"></view>
      </view>
      <view v-for="i in 3" :key="i" class="s-card list-card">
        <view class="s-row">
          <view class="s-line w-28 sm"></view>
          <view class="s-pill w-18"></view>
        </view>
        <view class="s-row gap-12 mt-12">
          <view class="s-thumb small"></view>
          <view class="s-col grow">
            <view class="s-line w-68"></view>
            <view class="s-line w-42 sm"></view>
          </view>
        </view>
        <view class="s-row gap-8 mt-16">
          <view class="s-pill w-22"></view>
          <view class="s-pill w-22"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'community'">
      <view class="s-row gap-8 mt-18">
        <view v-for="i in 5" :key="i" class="s-pill w-16 lg"></view>
      </view>
      <view class="s-row gap-8 mt-16">
        <view v-for="i in 6" :key="i" class="s-pill w-18"></view>
      </view>
      <view v-for="i in 2" :key="i" class="s-card feed-card">
        <view class="s-row gap-10">
          <view class="s-avatar sm"></view>
          <view class="s-col grow">
            <view class="s-line w-34"></view>
            <view class="s-line w-22 sm"></view>
          </view>
          <view class="s-pill w-16"></view>
        </view>
        <view class="s-line w-88 mt-14"></view>
        <view class="s-line w-76 sm"></view>
        <view class="s-row gap-8 mt-14">
          <view v-for="j in 3" :key="j" class="s-thumb square"></view>
        </view>
        <view class="s-row gap-18 mt-14">
          <view v-for="j in 4" :key="j" class="s-pill w-14"></view>
        </view>
      </view>
      <view class="s-fab"></view>
    </template>

    <template v-else-if="resolvedVariant === 'profile'">
      <view class="s-hero profile-hero"></view>
      <view class="s-card profile-head">
        <view class="s-row gap-12">
          <view class="s-avatar"></view>
          <view class="s-col grow">
            <view class="s-line w-36"></view>
            <view class="s-line w-56 sm"></view>
          </view>
        </view>
        <view class="s-grid four mt-16">
          <view v-for="i in 4" :key="i" class="s-stat"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-grid four">
          <view v-for="i in 8" :key="i" class="s-menu"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-28"></view>
        <view class="s-grid four mt-14">
          <view v-for="i in 4" :key="i" class="s-stat"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-34"></view>
        <view class="s-grid three mt-14">
          <view v-for="i in 3" :key="i" class="s-card mini"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'wallet'">
      <view class="s-card wallet-head">
        <view class="s-line w-44"></view>
        <view class="s-line w-62 big"></view>
        <view class="s-line w-48 sm"></view>
        <view class="s-row gap-12 mt-18">
          <view class="s-pill w-20 lg"></view>
          <view class="s-pill w-20 lg"></view>
        </view>
      </view>
      <view class="s-row gap-10 mt-18">
        <view v-for="i in 4" :key="i" class="s-pill w-16"></view>
      </view>
      <view v-for="i in 4" :key="i" class="s-row ledger-row">
        <view class="s-avatar sm"></view>
        <view class="s-col grow">
          <view class="s-row">
            <view class="s-line w-40"></view>
            <view class="s-line w-18"></view>
          </view>
          <view class="s-line w-72 sm"></view>
          <view class="s-row">
            <view class="s-line w-26 sm"></view>
            <view class="s-pill w-16"></view>
          </view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-34"></view>
        <view class="s-line w-88 sm"></view>
        <view class="s-line w-76 sm"></view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'distribution'">
      <view class="s-card distribution-head">
        <view class="s-line w-36 light"></view>
        <view class="s-row gap-14 mt-18">
          <view v-for="i in 3" :key="i" class="s-stat light"></view>
        </view>
      </view>
      <view class="s-card">
        <view v-for="i in 4" :key="i" class="s-row list-row">
          <view class="s-row gap-10 grow">
            <view class="s-avatar sm"></view>
            <view class="s-col grow">
              <view class="s-line w-28"></view>
              <view class="s-line w-56 sm"></view>
            </view>
          </view>
          <view class="s-pill w-12"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-28"></view>
        <view class="s-line w-88 sm"></view>
        <view class="s-line w-72 sm"></view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'member'">
      <view class="s-card member-head">
        <view class="s-row gap-12">
          <view class="s-avatar"></view>
          <view class="s-col grow">
            <view class="s-line w-40 light"></view>
            <view class="s-line w-52 sm light"></view>
          </view>
        </view>
        <view class="s-card inner mt-18">
          <view class="s-line w-20"></view>
          <view class="s-line w-62 sm"></view>
          <view class="s-row gap-8 mt-12">
            <view v-for="i in 4" :key="i" class="s-pill w-16"></view>
          </view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-30"></view>
        <view class="s-grid three mt-14">
          <view v-for="i in 3" :key="i" class="s-stat"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-36"></view>
        <view class="s-grid two mt-14">
          <view v-for="i in 2" :key="i" class="s-card mini tall"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-30"></view>
        <view v-for="i in 4" :key="i" class="s-row list-row">
          <view class="s-line w-48"></view>
          <view class="s-pill w-16"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'repair-mine'">
      <view class="s-search"></view>
      <view class="s-row gap-10 mt-18">
        <view v-for="i in 4" :key="i" class="s-pill w-16"></view>
      </view>
      <view v-for="i in 3" :key="i" class="s-card list-card">
        <view class="s-row">
          <view class="s-col grow">
            <view class="s-line w-34"></view>
            <view class="s-line w-28 sm"></view>
          </view>
          <view class="s-pill w-18"></view>
        </view>
        <view class="s-line w-90 sm mt-12"></view>
        <view class="s-row gap-8 mt-12">
          <view class="s-pill w-18"></view>
          <view class="s-pill w-18"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'repair-create'">
      <view class="s-card form-card">
        <view class="s-line w-28"></view>
        <view class="s-line w-22 sm mt-14"></view>
        <view class="s-card input-line mt-8"></view>
        <view class="s-line w-28 sm mt-18"></view>
        <view class="s-card input-area mt-8"></view>
        <view class="s-line w-28 sm mt-18"></view>
        <view class="s-grid three mt-10">
          <view v-for="i in 4" :key="i" class="s-thumb square small"></view>
          <view class="s-thumb square small add"></view>
        </view>
        <view class="s-line w-28 sm mt-18"></view>
        <view class="s-card input-line mt-8"></view>
      </view>
      <view class="s-bottom-bar">
        <view class="s-pill w-100 lg primary"></view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'asset-list'">
      <view class="s-row gap-8 mt-18">
        <view v-for="i in 4" :key="i" class="s-pill w-16"></view>
      </view>
      <view class="s-card">
        <view class="s-row gap-12">
          <view class="s-avatar sm"></view>
          <view class="s-col grow">
            <view class="s-line w-34"></view>
            <view class="s-line w-54 sm"></view>
          </view>
          <view class="s-pill w-18"></view>
        </view>
      </view>
      <view v-for="i in 3" :key="i" class="s-card list-card">
        <view class="s-row gap-12">
          <view class="s-thumb wide small"></view>
          <view class="s-col grow">
            <view class="s-row">
              <view class="s-line w-48"></view>
              <view class="s-pill w-16"></view>
            </view>
            <view class="s-line w-32 sm"></view>
            <view class="s-row gap-8 mt-12">
              <view class="s-pill w-18"></view>
              <view class="s-pill w-18"></view>
            </view>
          </view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'asset-earnings'">
      <view class="s-card earnings-head">
        <view class="s-row gap-12">
          <view class="s-line w-30 light"></view>
          <view class="s-pill w-16 light"></view>
        </view>
        <view class="s-row gap-14 mt-16">
          <view v-for="i in 3" :key="i" class="s-stat light"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-30"></view>
        <view class="s-chart">
          <view v-for="i in 7" :key="i" class="s-bar"></view>
        </view>
      </view>
      <view class="s-card">
        <view class="s-line w-28"></view>
        <view v-for="i in 4" :key="i" class="s-row list-row">
          <view class="s-col grow">
            <view class="s-line w-42"></view>
            <view class="s-line w-24 sm"></view>
          </view>
          <view class="s-pill w-16"></view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'notifications'">
      <view class="s-row gap-8 mt-18">
        <view v-for="i in 4" :key="i" class="s-pill w-18"></view>
      </view>
      <view v-for="i in 5" :key="i" class="s-card list-card">
        <view class="s-row gap-12">
          <view class="s-avatar sm"></view>
          <view class="s-col grow">
            <view class="s-row">
              <view class="s-line w-40"></view>
              <view class="s-line w-18"></view>
            </view>
            <view class="s-line w-88 sm"></view>
          </view>
        </view>
      </view>
    </template>

    <template v-else-if="resolvedVariant === 'order-detail' || resolvedVariant === 'asset-detail'">
      <view class="s-hero detail-hero"></view>
      <view class="s-card detail-card mt-16">
        <view class="s-line w-48"></view>
        <view class="s-line w-72 sm"></view>
        <view class="s-row gap-12 mt-16">
          <view v-for="i in 4" :key="i" class="s-pill w-20"></view>
        </view>
      </view>
      <view class="s-card detail-card mt-16">
        <view class="s-line w-36 sm"></view>
        <view class="s-row list-row" v-for="i in 3" :key="i">
          <view class="s-line w-26 sm"></view>
          <view class="s-line w-42"></view>
        </view>
      </view>
    </template>

    <template v-else-if="['distribution-team', 'distribution-commission', 'admin-dashboard', 'admin-notifications', 'workorder-hall', 'workorder-process', 'inspection-task-list', 'repair-progress'].includes(resolvedVariant)">
      <view class="s-row gap-10 mt-18">
        <view v-for="i in 4" :key="i" class="s-pill w-16 lg"></view>
      </view>
      <view v-for="i in 4" :key="i" class="s-card list-card mt-16">
        <view class="s-line w-36"></view>
        <view class="s-line w-72 sm mt-12"></view>
        <view class="s-row gap-12 mt-16">
          <view class="s-pill w-18"></view>
          <view class="s-pill w-18"></view>
        </view>
      </view>
    </template>

    <template v-else>
      <view v-for="i in rows" :key="i" class="s-line" :class="i === rows ? 'w-60' : 'w-100'"></view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

type SkeletonVariant =
  | "home"
  | "category"
  | "search"
  | "product-detail"
  | "order-confirm"
  | "order-list"
  | "community"
  | "profile"
  | "wallet"
  | "distribution"
  | "member"
  | "repair-mine"
  | "repair-create"
  | "asset-list"
  | "asset-earnings"
  | "notifications"
  | "order-detail"
  | "asset-detail"
  | "distribution-team"
  | "distribution-commission"
  | "admin-dashboard"
  | "admin-notifications"
  | "workorder-hall"
  | "workorder-process"
  | "inspection-task-list"
  | "repair-progress"
  | "generic";

const props = withDefaults(defineProps<{ rows?: number; variant?: SkeletonVariant }>(), {
  rows: 3,
  variant: "generic",
});

const currentRoute = ref("");

onMounted(() => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  currentRoute.value = String(pages.length > 0 ? pages[pages.length - 1].route : "");
});

const resolvedVariant = computed<SkeletonVariant>(() => {
  if (props.variant && props.variant !== "generic") return props.variant;
  const route = currentRoute.value;
  const map: Record<string, SkeletonVariant> = {
    "pages/home/index": "home",
    "pages/category/index": "category",
    "pages/search/index": "search",
    "pages/product-detail/index": "product-detail",
    "pages/order/confirm": "order-confirm",
    "pages/order/list": "order-list",
    "pages/order/detail": "order-detail",
    "pages/community/index": "community",
    "pages/profile/index": "profile",
    "pages/wallet/index": "wallet",
    "pages/distribution/home": "distribution",
    "pages/distribution/team": "distribution-team",
    "pages/distribution/commission": "distribution-commission",
    "pages/member/index": "member",
    "pages/repair/mine": "repair-mine",
    "pages/repair/create": "repair-create",
    "pages/repair/progress": "repair-progress",
    "pages/asset/list": "asset-list",
    "pages/asset/detail": "asset-detail",
    "pages/asset/earnings": "asset-earnings",
    "pages/notifications/index": "notifications",
    "pages/admin/dashboard": "admin-dashboard",
    "pages/admin/notifications": "admin-notifications",
    "pages/im/conversations": "notifications",
    "pages/inspection/task-list": "inspection-task-list",
    "pages/workorder/hall": "workorder-hall",
    "pages/workorder/process": "workorder-process",
  };
  return map[route] || "generic";
});
</script>

<style scoped lang="scss">
.page-skeleton {
  padding: 28rpx;
  background: #f5f6f8;
  min-height: 100vh;
}

.page-skeleton.member,
.page-skeleton.distribution,
.page-skeleton.home {
  background: linear-gradient(180deg, #f8fafc 0%, #f5f6f8 100%);
}

.s-row { display: flex; align-items: center; }
.s-col { display: flex; flex-direction: column; }
.grow { flex: 1; min-width: 0; }
.gap-8 { gap: 8rpx; }
.gap-10 { gap: 10rpx; }
.gap-12 { gap: 12rpx; }
.gap-14 { gap: 14rpx; }
.gap-18 { gap: 18rpx; }
.mt-8 { margin-top: 8rpx; }
.mt-10 { margin-top: 10rpx; }
.mt-12 { margin-top: 12rpx; }
.mt-14 { margin-top: 14rpx; }
.mt-16 { margin-top: 16rpx; }
.mt-18 { margin-top: 18rpx; }
.mt-20 { margin-top: 20rpx; }

.s-line,
.s-avatar,
.s-back,
.s-search,
.s-hero,
.s-thumb,
.s-pill,
.s-card,
.s-side-item,
.s-stat,
.s-menu,
.s-rank,
.s-bar {
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #eef2f7 25%, #e3e8ef 50%, #eef2f7 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.s-line,
.s-pill,
.s-back,
.s-search,
.s-hero,
.s-thumb,
.s-card,
.s-side-item,
.s-stat,
.s-menu,
.s-rank,
.s-bar {
  border-radius: 16rpx;
}

.s-line { height: 22rpx; border-radius: 999rpx; }
.s-line.sm { height: 16rpx; }
.s-line.big { height: 48rpx; }
.s-line.light { background: linear-gradient(90deg, rgba(255,255,255,0.22) 25%, rgba(255,255,255,0.35) 50%, rgba(255,255,255,0.22) 75%); }
.w-100 { width: 100%; }
.w-90 { width: 90%; }
.w-88 { width: 88%; }
.w-84 { width: 84%; }
.w-80 { width: 80%; }
.w-76 { width: 76%; }
.w-72 { width: 72%; }
.w-70 { width: 70%; }
.w-68 { width: 68%; }
.w-64 { width: 64%; }
.w-62 { width: 62%; }
.w-60 { width: 60%; }
.w-56 { width: 56%; }
.w-54 { width: 54%; }
.w-52 { width: 52%; }
.w-48 { width: 48%; }
.w-46 { width: 46%; }
.w-44 { width: 44%; }
.w-42 { width: 42%; }
.w-40 { width: 40%; }
.w-38 { width: 38%; }
.w-36 { width: 36%; }
.w-34 { width: 34%; }
.w-32 { width: 32%; }
.w-30 { width: 30%; }
.w-28 { width: 28%; }
.w-26 { width: 26%; }
.w-24 { width: 24%; }
.w-22 { width: 22%; }
.w-20 { width: 20%; }
.w-18 { width: 18%; }
.w-16 { width: 16%; }
.w-14 { width: 14%; }
.w-12 { width: 12%; }
.w-10 { width: 10%; }

.s-avatar,
.s-back,
.s-rank { width: 72rpx; height: 72rpx; border-radius: 50%; flex-shrink: 0; }
.s-avatar.sm { width: 56rpx; height: 56rpx; }
.s-back { width: 58rpx; height: 58rpx; background: linear-gradient(90deg, #edf2f7 25%, #e5ebf2 50%, #edf2f7 75%); }
.s-back.ghost { opacity: 0.55; }
.s-search { height: 72rpx; border-radius: 999rpx; flex: 1; }
.s-hero { height: 320rpx; border-radius: 28rpx; }
.s-hero.detail-hero { height: 560rpx; border-radius: 0; margin: -28rpx -28rpx 0; }
.s-thumb { width: 100%; height: 184rpx; border-radius: 18rpx; }
.s-thumb.small { width: 120rpx; height: 120rpx; }
.s-thumb.wide { width: 148rpx; height: 148rpx; }
.s-thumb.square { width: 31%; height: 0; padding-bottom: 31%; }
.s-thumb.square.small { width: 120rpx; height: 120rpx; }
.s-thumb.add { background: linear-gradient(90deg, #eef2f7 25%, #f3f6fa 50%, #eef2f7 75%); }
.s-card { padding: 22rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.04); margin-top: 18rpx; }
.s-card.light { background: rgba(255,255,255,0.7); box-shadow: none; }
.s-pill { height: 44rpx; border-radius: 999rpx; }
.s-pill.lg { height: 52rpx; }
.s-pill.primary { background: linear-gradient(90deg, #0a4bfe 25%, #3b82f6 50%, #0a4bfe 75%); }
.s-chip {
  width: 160rpx;
  height: 68rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #eef2f7 25%, #e3e8ef 50%, #eef2f7 75%);
  animation: shimmer 1.4s infinite;
}
.s-chip-wrap { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 16rpx; }
.s-grid { display: grid; gap: 14rpx; }
.s-grid.two { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.s-grid.three { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.s-grid.four { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.s-card.mini { height: 110rpx; border-radius: 18rpx; }
.s-card.mini.tall { height: 172rpx; }
.s-card.product { padding: 18rpx; }
.s-card.list-card { display: flex; flex-direction: column; gap: 12rpx; }
.s-card.feed-card { display: flex; flex-direction: column; gap: 12rpx; }
.s-bottom-bar {
  display: flex;
  gap: 12rpx;
  align-items: center;
  margin-top: 18rpx;
  padding: 18rpx 0 calc(18rpx + env(safe-area-inset-bottom));
}
.s-fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(132rpx + env(safe-area-inset-bottom));
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: linear-gradient(90deg, #0a4bfe 25%, #3b82f6 50%, #0a4bfe 75%);
  box-shadow: 0 12rpx 28rpx rgba(10, 75, 254, 0.24);
}
.s-section-head { width: 38%; height: 28rpx; border-radius: 999rpx; background: linear-gradient(90deg, #e9edf3 25%, #dfe6ee 50%, #e9edf3 75%); animation: shimmer 1.4s infinite; }
.s-section-head.short { width: 24%; }
.s-category-shell { display: flex; gap: 16rpx; margin-top: 18rpx; }
.s-side { width: 178rpx; display: flex; flex-direction: column; gap: 14rpx; }
.s-side-item { height: 84rpx; }
.s-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 14rpx; }
.s-filter-row { display: flex; gap: 10rpx; flex-wrap: wrap; }
.s-search-list { display: flex; flex-direction: column; gap: 16rpx; margin-top: 18rpx; }
.search-item { gap: 14rpx; }
.s-rank { width: 34rpx; height: 34rpx; border-radius: 8rpx; }
.s-hero.profile-hero { height: 520rpx; border-radius: 0; margin: -28rpx -28rpx 0; }
.s-card.profile-head { margin-top: -160rpx; }
.s-stat { height: 90rpx; border-radius: 18rpx; }
.s-menu { height: 76rpx; border-radius: 16rpx; }
.s-card.wallet-head { background: linear-gradient(135deg, #0a4bfe, #3b82f6); color: #fff; }
.s-card.wallet-head .s-line,
.s-card.distribution-head .s-line,
.s-card.member-head .s-line { opacity: 0.9; }
.s-card.distribution-head { background: linear-gradient(135deg, #0a4bfe, #6366f1); color: #fff; }
.s-card.member-head { background: linear-gradient(180deg, #0a1c3f, #08285a); color: #fff; }
.s-card.member-head .inner { background: rgba(255,255,255,0.06); box-shadow: none; }
.s-chart { display: flex; gap: 10rpx; align-items: flex-end; height: 180rpx; margin-top: 16rpx; }
.s-bar { flex: 1; min-height: 22rpx; border-radius: 12rpx 12rpx 6rpx 6rpx; height: 100%; }
.ledger-row { gap: 14rpx; padding: 18rpx 0; border-bottom: 1rpx solid #edf2f7; }
.list-row { justify-content: space-between; gap: 12rpx; padding: 12rpx 0; }
.s-card.form-card { display: block; }
.input-line { height: 78rpx; border-radius: 14rpx; }
.input-area { height: 180rpx; border-radius: 18rpx; }
.detail-float {
  position: absolute;
  right: 28rpx;
  top: 430rpx;
  gap: 12rpx;
}
.detail-card { margin-top: 18rpx; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
