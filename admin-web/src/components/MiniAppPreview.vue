<template>
  <aside class="preview-panel">
    <div class="preview-title">小程序实时预览</div>
    <div class="phone">
      <div class="phone-status">
        <span>9:41</span>
        <span class="status-dots">5G 100%</span>
      </div>
      <div class="phone-nav">
        <span>{{ navTitle }}</span>
        <span class="capsule"></span>
      </div>
      <div class="phone-body">
        <template v-if="mode === 'product'">
          <div class="hero-media product-media">
            <img v-if="primaryImage" :src="primaryImage" alt="" />
            <div v-else class="empty-media">商品图</div>
          </div>
          <section class="content-card">
            <div class="tag-row">
              <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
              <span v-if="!tags.length" class="tag muted">商品标签</span>
            </div>
            <h3>{{ data.name || '商品名称' }}</h3>
            <p>{{ data.subtitle || data.description || '商品副标题与简介会显示在这里' }}</p>
            <div class="price-row">
              <strong>{{ priceLabel }}</strong>
              <span v-if="data.originalPriceMinor">原价 {{ formatMinor(data.originalPriceMinor) }}</span>
            </div>
            <div v-if="pricePlans.length" class="preview-plan-tabs">
              <span v-for="plan in pricePlans" :key="plan.type">{{ plan.label }}</span>
            </div>
            <div v-if="pricePlans.length" class="preview-price-grid">
              <div v-for="plan in pricePlans" :key="plan.type">
                <small>{{ plan.label }}</small>
                <b>{{ formatMinor(plan.priceMinor) }}<em>{{ plan.unit }}</em></b>
              </div>
            </div>
            <div class="info-grid">
              <div><b>{{ stockText }}</b><span>{{ data.stockStatusText || '库存数量' }}</span></div>
              <div><b>{{ data.deliveryText || '极速配送' }}</b><span>配送</span></div>
            </div>
          </section>
          <section class="content-card compact-card">
            <div class="section-title">适配场景</div>
            <p>{{ data.adaptedScenesText || '工业巡检、科研教育、展厅讲解' }}</p>
          </section>
          <section v-if="services.length" class="content-card compact-card">
            <div class="section-title">服务</div>
            <div v-for="service in services" :key="service.name" class="preview-service-line">
              <span>✓ {{ service.name }}</span><b>{{ service.priceLabel || '免费' }}</b>
            </div>
          </section>
          <section v-if="specItems.length" class="content-card compact-card">
            <div class="section-title">产品参数</div>
            <div v-for="item in specItems" :key="item.label" class="spec-line">
              <span>{{ item.label }}</span><b>{{ item.value }}</b>
            </div>
          </section>
          <section v-if="detailSections.length" class="content-card compact-card">
            <div class="section-title">产品说明</div>
            <div v-for="section in detailSections" :key="section.title || section.content || section.imageUrl" class="detail-block">
              <b>{{ section.title || '说明区块' }}</b>
              <p>{{ section.content || '区块内容' }}</p>
            </div>
          </section>
        </template>

        <template v-else-if="mode === 'activity'">
          <div v-if="detailHeroImage" class="hero-media activity-media">
            <img :src="detailHeroImage" alt="" />
          </div>
          <div v-if="data.videoUrl" class="video-card">
            <span class="play-dot">▶</span>
            <span>活动视频</span>
          </div>
          <section class="content-card">
            <span class="activity-tag">{{ data.tag || '活动' }}</span>
            <h3>{{ data.title || '活动标题' }}</h3>
            <p>{{ data.subtitle || '活动副标题会显示在这里' }}</p>
            <div class="time-line">{{ timeRange }}</div>
            <p class="desc">{{ data.description || '活动简介内容会在小程序详情页展示。' }}</p>
          </section>
          <section class="content-card compact-card">
            <div class="section-title">详情内容</div>
            <div v-for="section in detailSections" :key="section.title || section.content" class="detail-block">
              <img v-if="section.imageUrl" :src="section.imageUrl" alt="" class="detail-image" />
              <b>{{ section.title || '详情区块' }}</b>
              <p>{{ section.content || '区块内容' }}</p>
            </div>
            <div v-if="!detailSections.length" class="placeholder-line">新增详情区块后会实时出现在这里</div>
          </section>
        </template>

        <template v-else>
          <section class="mini-brand">
            <div>
              <b>星球·出机</b>
              <span>机器人租赁、购买、软件服务平台</span>
            </div>
          </section>
          <section class="home-search">搜索机器人、场景、品牌、软件</section>
          <section class="mini-hero-shell">
            <div class="mini-hero">
              <div class="hero-mask"></div>
              <div class="mini-hero-content">
                <span>精选活动</span>
                <b>机器人场景服务</b>
                <p>租赁 / 购买 / 软件 · 一站式机器人服务平台</p>
                <em>查看活动详情</em>
              </div>
            </div>

            <div class="mini-quick-grid">
              <div class="mini-quick-card rent" :class="{ selected: bannerPosition === 'HOME_RENT_CARD' }">
                <b>{{ bannerPosition === 'HOME_RENT_CARD' ? (data.title || '租机器人') : '租机器人' }}</b>
                <span>灵活租赁<br />按需使用</span>
                <img v-if="bannerPosition === 'HOME_RENT_CARD' && primaryImage" :src="primaryImage" alt="" />
              </div>
              <div class="mini-quick-card buy" :class="{ selected: bannerPosition === 'HOME_BUY_CARD' }">
                <b>{{ bannerPosition === 'HOME_BUY_CARD' ? (data.title || '买机器人') : '买机器人' }}</b>
                <span>品质保障<br />快速交付</span>
                <img v-if="bannerPosition === 'HOME_BUY_CARD' && primaryImage" :src="primaryImage" alt="" />
              </div>
              <div class="mini-quick-card app" :class="{ selected: bannerPosition === 'HOME_APP_CARD' }">
                <b>{{ bannerPosition === 'HOME_APP_CARD' ? (data.title || '应用商店') : '应用商店' }}</b>
                <span>场景软件<br />定制开发</span>
                <img v-if="bannerPosition === 'HOME_APP_CARD' && primaryImage" :src="primaryImage" alt="" />
              </div>
            </div>
          </section>

          <section class="mini-section">
            <div class="section-title-row"><b>场景应用</b><em>更多场景</em></div>
            <div class="mini-scene-row">
              <div class="mini-scene-card"><div></div><b>科研教育</b><span>可租 科研</span></div>
              <div class="mini-scene-card"><div></div><b>工业巡检</b><span>可租 巡检</span></div>
            </div>
          </section>

          <section class="mini-section">
            <div class="section-title-row">
              <b>为你推荐</b>
              <em>更多推荐</em>
            </div>
            <div class="mini-product-grid">
              <div class="mini-product-card">
                <div class="mini-product-img">
                  <span>可租 20</span>
                </div>
                <b>G1 人形机器人</b>
                <p>热门 智能</p>
                <strong>¥99 /天起</strong>
              </div>
              <div class="mini-product-card">
                <div class="mini-product-img"><span>可租 12</span></div>
                <b>Go2 四足机器人</b>
                <p>巡检 教育</p>
                <strong>¥79 /天起</strong>
              </div>
            </div>
          </section>
        </template>
      </div>
      <div class="phone-tabbar">
        <span class="active">首页</span>
        <span>分类</span>
        <span>社区</span>
        <span>我的</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  mode: 'product' | 'activity' | 'banner'
  data: any
}>()

const bannerPositions = ['HOME_RENT_CARD', 'HOME_BUY_CARD', 'HOME_APP_CARD'] as const
const positionAliases: Record<string, (typeof bannerPositions)[number]> = {
  HOME_RENT_CARD: 'HOME_RENT_CARD',
  HOME_CARD: 'HOME_RENT_CARD',
  CARD: 'HOME_RENT_CARD',
  首页卡片: 'HOME_RENT_CARD',
  HOME_BUY_CARD: 'HOME_BUY_CARD',
  HOME_APP_CARD: 'HOME_APP_CARD',
  PLANET_CARD: 'HOME_APP_CARD',
}

const navTitle = computed(() => {
  if (props.mode === 'product') return '商品详情'
  if (props.mode === 'activity') return '活动详情'
  return '星球出机'
})

const primaryImage = computed(() => {
  const media = Array.isArray(props.data?.media)
    ? props.data.media.find((item: any) => item?.type !== 'VIDEO' && item?.url)
    : null
  return props.data?.coverUrl || props.data?.imageUrl || media?.url || ''
})

const detailHeroImage = computed(() => {
  if (props.data?.detailImageUrl) return props.data.detailImageUrl
  const sections = Array.isArray(props.data?.content) ? props.data.content : []
  const imageSection = sections.find((section: any) => section?.kind === 'DETAIL_IMAGE' || (section?.imageUrl && !section?.title && !section?.content))
  return imageSection?.imageUrl || ''
})

const tags = computed<string[]>(() => {
  if (Array.isArray(props.data?.tags)) return props.data.tags.filter(Boolean).slice(0, 3)
  return []
})

const stockText = computed(() => {
  const stock = Number(props.data?.stock || 0)
  return stock > 0 ? `${stock} 件可用` : '现货可约'
})

const priceLabel = computed(() => {
  const prices = Array.isArray(props.data?.prices) ? props.data.prices : []
  const validPrices = prices.map((item: any) => Number(item?.priceMinor || 0)).filter((price: number) => price > 0)
  const price = validPrices.length ? Math.min(...validPrices) : Number(props.data?.originalPriceMinor || 0)
  return price > 0 ? `${formatMinor(price)} 起` : '价格待配置'
})

const pricePlans = computed(() => {
  const prices = Array.isArray(props.data?.prices) ? props.data.prices : []
  return prices
    .filter((item: any) => Number(item?.priceMinor || 0) > 0)
    .map((item: any) => ({
      type: item.priceType,
      label: item.priceType === 'DAILY_RENT' ? '短期租赁' : item.priceType === 'LEASE_BUY' ? '租赁购买' : item.priceType === 'BUY' ? '购买买断' : '订阅服务',
      priceMinor: Number(item.priceMinor),
      unit: item.priceType === 'DAILY_RENT' ? '/天起' : item.priceType === 'LEASE_BUY' ? '/年起' : item.priceType === 'SUBSCRIPTION' ? '/周期' : '/台起',
    }))
})

const services = computed<Array<{ name: string; priceLabel?: string }>>(() => {
  return Array.isArray(props.data?.services)
    ? props.data.services.filter((item: any) => item?.name).slice(0, 4)
    : []
})

const specItems = computed(() => {
  try {
    const parsed = typeof props.data?.specsJson === 'string' ? JSON.parse(props.data.specsJson || '{}') : {}
    return Object.entries(parsed).slice(0, 4).map(([label, value]) => ({ label, value: String(value) }))
  } catch {
    return []
  }
})

const detailSections = computed<Array<{ title?: string; content?: string; imageUrl?: string; kind?: string }>>(() => {
  const sections = props.mode === 'product' ? props.data?.detailSections : props.data?.content
  return Array.isArray(sections) ? sections.filter((item) => item?.kind !== 'DETAIL_IMAGE' && (item?.title || item?.content || item?.imageUrl)).slice(0, 4) : []
})

const timeRange = computed(() => {
  const start = formatDate(props.data?.startAt)
  const end = formatDate(props.data?.endAt)
  if (start && end) return `${start} - ${end}`
  return start || end || '活动时间待配置'
})

const bannerPosition = computed(() => {
  const rawPosition = String(props.data?.position || 'HOME_RENT_CARD').trim()
  const normalized = rawPosition.toUpperCase().replace(/[\s-]+/g, '_')
  return positionAliases[rawPosition] || positionAliases[normalized] || 'HOME_RENT_CARD'
})

function formatMinor(value: unknown) {
  const numberValue = Number(value || 0)
  return `￥${(numberValue / 100).toFixed(2)}`
}

function formatDate(value: unknown) {
  if (!value) return ''
  const text = String(value).replace('T', ' ')
  return text.slice(5, 16)
}
</script>

<style scoped>
.preview-panel {
  width: 340px;
  flex: 0 0 340px;
}

.preview-title {
  margin-bottom: 12px;
  color: #475467;
  font-size: 14px;
  font-weight: 600;
}

.phone {
  position: sticky;
  top: 16px;
  height: 720px;
  border: 10px solid #1f2937;
  border-radius: 34px;
  background: #f6f8fb;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.phone-status,
.phone-nav,
.phone-tabbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.phone-status {
  height: 28px;
  padding: 0 20px;
  color: #111827;
  font-size: 12px;
}

.status-dots {
  color: #667085;
}

.phone-nav {
  height: 44px;
  padding: 0 16px;
  border-bottom: 1px solid #eef0f4;
  color: #111827;
  font-weight: 600;
}

.capsule {
  width: 54px;
  height: 22px;
  border: 1px solid #d0d5dd;
  border-radius: 999px;
}

.phone-body {
  height: calc(100% - 122px);
  overflow: auto;
  padding: 12px;
}

.phone-tabbar {
  height: 50px;
  padding: 0 22px;
  border-top: 1px solid #eef0f4;
  color: #98a2b3;
  font-size: 12px;
}

.phone-tabbar .active {
  color: #1677ff;
  font-weight: 600;
}

.hero-media {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: linear-gradient(135deg, #dbeafe, #f1f5f9);
}

.hero-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.product-media {
  height: 250px;
}

.activity-media,
.banner-media {
  height: 180px;
}

.empty-media {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667085;
  font-size: 14px;
}

.content-card {
  margin-top: 10px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.05);
}

.content-card h3 {
  margin: 8px 0 6px;
  color: #101828;
  font-size: 18px;
  line-height: 1.35;
}

.content-card p {
  margin: 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.6;
}

.compact-card {
  padding: 12px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 3px 8px;
  border-radius: 999px;
  background: #e8f2ff;
  color: #1677ff;
  font-size: 11px;
}

.tag.muted {
  background: #f2f4f7;
  color: #98a2b3;
}

.price-row {
  margin-top: 12px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-row strong {
  color: #f04438;
  font-size: 20px;
}

.price-row span {
  color: #98a2b3;
  font-size: 12px;
  text-decoration: line-through;
}

.preview-plan-tabs {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #edf2f7;
}

.preview-plan-tabs span {
  padding: 5px 0;
  color: #0a4bfe;
  font-size: 11px;
  text-decoration: none;
  border-bottom: 2px solid #0a4bfe;
}

.preview-price-grid {
  padding: 10px 0 2px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.preview-price-grid small {
  display: block;
  color: #667085;
  font-size: 11px;
}

.preview-price-grid b {
  display: block;
  margin-top: 4px;
  color: #f04438;
  font-size: 14px;
}

.preview-price-grid em {
  color: #667085;
  font-size: 10px;
  font-style: normal;
  font-weight: 400;
}

.info-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.info-grid div {
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
}

.info-grid b,
.operation-tile b,
.detail-block b {
  display: block;
  color: #101828;
  font-size: 13px;
}

.info-grid span {
  color: #98a2b3;
  font-size: 12px;
}

.section-title {
  margin-bottom: 8px;
  color: #101828;
  font-size: 14px;
  font-weight: 600;
}

.spec-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px solid #f2f4f7;
  color: #667085;
  font-size: 12px;
}

.spec-line b {
  color: #101828;
  font-weight: 500;
}

.preview-service-line {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 7px 0;
  color: #101828;
  font-size: 12px;
}

.preview-service-line b {
  color: #16a34a;
  font-weight: 500;
}

.detail-block + .detail-block {
  margin-top: 10px;
}

.detail-block p {
  margin-top: 4px;
}

.floating-tag {
  position: absolute;
  left: 12px;
  bottom: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(16, 24, 40, 0.76);
  color: #fff;
  font-size: 12px;
}

.activity-tag {
  display: inline-block;
  padding: 4px 9px;
  border-radius: 999px;
  background: #e0edff;
  color: #0a4bfe;
  font-size: 11px;
  font-weight: 600;
}

.video-card {
  margin-top: 10px;
  height: 46px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 12px;
  background: #111827;
  color: #fff;
  font-size: 13px;
}

.play-dot {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, .18);
  font-size: 11px;
}

.detail-image {
  width: 100%;
  height: 128px;
  margin-bottom: 8px;
  display: block;
  border-radius: 12px;
  object-fit: cover;
}

.time-line {
  margin: 10px 0;
  padding: 9px 10px;
  border-radius: 10px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 12px;
}

.desc {
  color: #344054;
}

.placeholder-line {
  padding: 18px 0;
  color: #98a2b3;
  text-align: center;
  font-size: 12px;
}

.home-search {
  height: 36px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  border-radius: 999px;
  background: #fff;
  color: #98a2b3;
  font-size: 12px;
}

.mini-brand {
  padding: 6px 2px 10px;
}

.mini-brand b,
.mini-brand span {
  display: block;
}

.mini-brand b {
  color: #111827;
  font-size: 18px;
}

.mini-brand span {
  margin-top: 4px;
  color: #6b7280;
  font-size: 11px;
}

.mini-hero-shell {
  margin-top: 12px;
  padding: 0 0 14px;
  border-radius: 0 0 16px 16px;
  background: #fff;
}

.mini-hero {
  position: relative;
  height: 210px;
  overflow: hidden;
  border-radius: 16px;
  background: #111827;
}

.mini-hero img,
.hero-mask {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.mini-hero img {
  object-fit: cover;
  opacity: .72;
}

.hero-mask {
  background: linear-gradient(90deg, rgba(0,0,0,.72), transparent);
}

.mini-hero-content {
  position: relative;
  padding: 27px 16px;
  color: #fff;
}

.mini-hero-content span,
.mini-hero-content b,
.mini-hero-content p {
  display: block;
}

.mini-hero-content span {
  font-size: 12px;
  opacity: .92;
}

.mini-hero-content b {
  margin-top: 10px;
  max-width: 220px;
  font-size: 20px;
  line-height: 1.25;
}

.mini-hero-content p {
  width: 220px;
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.55;
}

.mini-hero-content em {
  display: inline-block;
  margin-top: 14px;
  padding: 6px 15px;
  border-radius: 16px;
  background: #fff;
  color: #111827;
  font-size: 12px;
  font-style: normal;
  font-weight: 600;
}

.mini-quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 7px;
  margin-top: 11px;
}

.mini-quick-card {
  position: relative;
  height: 91px;
  overflow: hidden;
  border-radius: 9px;
  padding: 9px;
}

.mini-quick-card.rent { background: #eff6ff; }
.mini-quick-card.buy { background: #ecfeff; }
.mini-quick-card.app { background: #f5f3ff; }

.mini-quick-card b,
.mini-quick-card span {
  position: relative;
  z-index: 2;
  display: block;
}

.mini-quick-card b {
  color: #111827;
  font-size: 13px;
}

.mini-quick-card span {
  margin-top: 5px;
  color: #6b7280;
  font-size: 10px;
  line-height: 1.5;
}

.mini-quick-card img {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 49px;
  height: 49px;
  border-radius: 10px;
  object-fit: cover;
  opacity: .86;
}

.mini-section {
  margin-top: 9px;
  padding: 14px;
  background: #fff;
}

.section-title-row b {
  color: #111827;
  font-size: 15px;
}

.mini-scene-row {
  display: flex;
  gap: 8px;
}

.mini-scene-card {
  width: 121px;
  flex-shrink: 0;
}

.mini-scene-card div {
  height: 80px;
  border-radius: 8px;
  background: #f0f4f8;
}

.mini-scene-card b,
.mini-scene-card span {
  display: block;
}

.mini-scene-card b {
  margin-top: 6px;
  color: #111827;
  font-size: 13px;
}

.mini-scene-card span {
  margin-top: 4px;
  color: #2563eb;
  font-size: 10px;
}

.mini-product-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0,1fr));
  gap: 9px;
}

.mini-product-card {
  min-width: 0;
}

.mini-product-img {
  position: relative;
  height: 130px;
  border-radius: 10px;
  overflow: hidden;
  background: #f6f8fa;
}

.mini-product-img img {
  width: 92%;
  height: 92%;
  margin: 4%;
  border-radius: 8px;
  object-fit: cover;
}

.mini-product-img span {
  position: absolute;
  left: 6px;
  bottom: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(219, 234, 254, .92);
  color: #0a4bfe;
  font-size: 9px;
  font-weight: 700;
}

.mini-product-card b,
.mini-product-card p,
.mini-product-card strong {
  display: block;
}

.mini-product-card b {
  margin-top: 8px;
  color: #111827;
  font-size: 12px;
  line-height: 1.35;
}

.mini-product-card p {
  min-height: 18px;
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 10px;
}

.mini-product-card strong {
  margin-top: 4px;
  color: #0a4bfe;
  font-size: 12px;
}

.selected {
  outline: 2px solid #0a4bfe;
  outline-offset: 2px;
}

.banner-media {
  margin-top: 12px;
}

.banner-copy {
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 14px;
  color: #fff;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.3);
}

.banner-copy span,
.banner-copy b {
  display: block;
}

.banner-copy span {
  font-size: 12px;
}

.banner-copy b {
  margin-top: 4px;
  font-size: 18px;
}

.operation-tile {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 10px;
  align-items: center;
}

.quick-preview {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.quick-preview-card {
  min-height: 94px;
  border-radius: 12px;
  padding: 10px;
  overflow: hidden;
}

.active-card {
  position: relative;
  grid-column: span 2;
  background: #eff6ff;
}

.active-card span,
.planet-entry span,
.recommend-preview span,
.position-summary span {
  display: block;
  color: #1677ff;
  font-size: 11px;
  font-weight: 600;
}

.active-card b,
.planet-entry b,
.recommend-preview b,
.position-summary b {
  display: block;
  margin-top: 5px;
  color: #101828;
  font-size: 15px;
  line-height: 1.35;
}

.active-card img {
  position: absolute;
  right: -6px;
  bottom: -8px;
  width: 82px;
  height: 82px;
  object-fit: cover;
  border-radius: 14px;
}

.muted-card {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f4f7;
  color: #98a2b3;
  font-size: 12px;
}

.planet-entry {
  position: relative;
  margin-top: 12px;
  min-height: 156px;
  padding: 18px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(135deg, #101828, #1d4ed8);
}

.planet-entry span,
.planet-entry b,
.planet-entry p {
  position: relative;
  z-index: 2;
  color: #fff;
}

.planet-entry p {
  width: 170px;
  margin: 8px 0 0;
  opacity: .78;
  font-size: 12px;
  line-height: 1.5;
}

.planet-entry img {
  position: absolute;
  right: 12px;
  bottom: 12px;
  width: 112px;
  height: 112px;
  border-radius: 24px;
  object-fit: cover;
}

.planet-orbit {
  position: absolute;
  right: -36px;
  top: -42px;
  width: 148px;
  height: 148px;
  border: 1px solid rgba(255, 255, 255, .28);
  border-radius: 50%;
}

.recommend-preview {
  margin-top: 12px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
}

.section-title-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title-row em {
  color: #98a2b3;
  font-size: 12px;
  font-style: normal;
}

.position-summary {
  display: grid;
  gap: 4px;
}

.tile-thumb {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: #edf2f7;
  overflow: hidden;
}

.tile-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 1180px) {
  .preview-panel {
    width: 100%;
    flex-basis: auto;
  }

  .phone {
    position: static;
    height: 680px;
    max-width: 340px;
  }
}
</style>
