export const operationalConfig = {
  city: '上海',
  campaignTitle: '全民机器人体验节',
  memberLevels: ['Lv.0 探索者', 'Lv.1 原住民', 'Lv.2 星舰长', 'Lv.3 共创官'],
  topics: ['为您推荐', '社区精选', '技能包', '新闻动态', '我要合作']
}

export const scenes = [
  { id: 'wedding', name: '婚礼庆典', image: 'linear-gradient(135deg,#eef6ff,#d4e6ff)' },
  { id: 'mall', name: '商场促销', image: 'linear-gradient(135deg,#fff4e5,#ffe2bf)' },
  { id: 'school', name: '学校活动', image: 'linear-gradient(135deg,#eefaf3,#d7f3e4)' },
  { id: 'culture', name: '文旅服务', image: 'linear-gradient(135deg,#f1efff,#ddd8ff)' }
]

export const banners = [
  { id: 'mayday', title: '五一限定品牌补贴', subtitle: '最高立减 700 元', color: 'linear-gradient(135deg,#111a35,#2f7cf6)' },
  { id: 'retail', title: '智慧新零售门店体验', subtitle: '科技伙伴到里服务零距离', color: 'linear-gradient(135deg,#ff8a1f,#ffcf70)' }
]

export const products = [
  {
    id: 'sku-rent-alpha',
    name: '科技伴侣 Alpha 表演机器人',
    type: 'rent',
    tag: '热门推荐',
    priceCent: 389900,
    originalPriceCent: 519900,
    cover: 'linear-gradient(135deg,#fff3c4,#f7b955)',
    specs: ['迎宾互动', '舞蹈表演', '语音讲解'],
    availability: 'available'
  },
  {
    id: 'sku-buy-retail',
    name: '智慧新零售导购机器人',
    type: 'buy',
    tag: '热门品牌',
    priceCent: 1289900,
    originalPriceCent: 1399900,
    cover: 'linear-gradient(135deg,#eaf3ff,#bcd8ff)',
    specs: ['商品讲解', '客流互动', '门店巡航'],
    availability: 'available'
  },
  {
    id: 'sku-soft-skill',
    name: '炫酷技能包无限畅玩',
    type: 'software',
    tag: '技能包',
    priceCent: 29900,
    originalPriceCent: 49900,
    cover: 'linear-gradient(135deg,#f4e9ff,#cfb3ff)',
    specs: ['动作编排', '音乐同步', '在线更新'],
    availability: 'available'
  },
  {
    id: 'sku-skin-planet',
    name: '机器人皮肤周边定制服务',
    type: 'software',
    tag: '特配服务',
    priceCent: 69900,
    originalPriceCent: 89900,
    cover: 'linear-gradient(135deg,#e8fff7,#9cf0d6)',
    specs: ['主题贴膜', '活动视觉', '品牌定制'],
    availability: 'limited'
  }
]

export const orders = [
  { id: 'order-001', title: '科技伴侣 Alpha 表演机器人', type: 'rent', status: '待付款', amountCent: 389900, allowedActions: ['去支付', '取消订单'] },
  { id: 'order-002', title: '智慧新零售导购机器人', type: 'buy', status: '服务中', amountCent: 1289900, allowedActions: ['查看物流', '申请售后'] }
]

export const posts = [
  { id: 'post-001', title: '年会机器人开场怎么做更抓眼', status: '已通过', likes: 128 },
  { id: 'post-002', title: '门店导购机器人体验记录', status: '审核中', likes: 42 }
]

/** 首页「场景应用」横向卡片（对齐设计稿 V3.0） */
export const homeSceneApplications = [
  {
    id: 'edu',
    name: '科研教育',
    image: 'url(/static/images/home/科研教育.png)',
    actions: ['可租', '可买', '配软件'] as const
  },
  {
    id: 'industry',
    name: '工业巡检',
    image: 'url(/static/images/home/工业巡检.png)',
    actions: ['可租', '可买', '配软件'] as const
  },
  {
    id: 'show',
    name: '表演娱乐',
    image: 'url(/static/images/home/表演娱乐.png)',
    actions: ['可租', '可买', '配软件'] as const
  },
  {
    id: 'expo',
    name: '展会导览',
    image: 'url(/static/images/home/展会导览.png)',
    actions: ['可租', '可买', '配软件'] as const
  },
  {
    id: 'security',
    name: '安防巡逻',
    image: 'linear-gradient(180deg,#c8f0e0 0%,#7dceb0 50%,#3da885 100%)',
    actions: ['可租', '可买', '配软件'] as const
  }
]

/** 首页为你推荐 - 可租赁列表 */
export const homeShowcaseRent = [
  {
    id: 'sku-home-g1',
    name: 'G1-u2 人形机器人',
    badge: '可租 16',
    pricePerDayYuan: 4900,
    tags: [ '人形机器人', '智能交互', '全身运动' ],
    cover: 'url(/static/images/home/G1-u2.png)',
    mode: 'rent' as const
  },
  {
    id: 'sku-home-go2-w',
    name: 'Go2-W 轮式机器人',
    badge: '可租 20',
    pricePerDayYuan: 699,
    tags: ['稳定可靠', '卓越性能', '多地形适应'],
    cover: 'url(/static/images/home/Go2-W.png)',
    mode: 'rent' as const
  },
  {
    id: 'sku-home-go2',
    name: 'Go2 四足机器人',
    badge: '可租 8',
    pricePerDayYuan: 1999,
    tags: ['高动态运动', '智能感知', '灵活敏捷'],
    cover: 'url(/static/images/home/Go2.png)',
    mode: 'rent' as const
  }
]

/** 首页为你推荐 - 可购买列表 */
export const homeShowcaseBuy = [
  {
    id: 'sku-buy-g1',
    name: 'G1-u2 人形机器人（购）',
    badge: '可买',
    priceBuyYuan: 99000,
    tags: ['人形机器人', '智能交互', '全身运动'],
    cover: 'url(/static/images/home/G1-u2.png)',
    mode: 'buy' as const
  },
  {
    id: 'sku-buy-go2',
    name: 'Go2 四足机器人（购）',
    badge: '新品',
    priceBuyYuan: 18999,
    tags: ['四足', '户外', '编程'],
    cover: 'url(/static/images/home/Go2.png)',
    mode: 'buy' as const
  }
]

/** 扩展 SKU，供首页跳转详情 */
export const homeExtraProducts = [
  {
    id: 'sku-home-g1',
    name: 'G1-u2 人形机器人',
    type: 'rent' as const,
    tag: '可租',
    priceCent: 39900,
    originalPriceCent: 49900,
    cover: 'linear-gradient(165deg,#eef2f7 0%,#b8c5d6 100%)',
    specs: ['人形机器人', '智能交互', '全身运动'],
    availability: 'available'
  },
  {
    id: 'sku-home-go2',
    name: 'Go2-W 轮式机器人',
    type: 'rent' as const,
    tag: '可租',
    priceCent: 29900,
    originalPriceCent: 35900,
    cover: 'linear-gradient(165deg,#e8f4ff 0%,#9bc4eb 100%)',
    specs: ['轮式机器人', '灵活移动', '室内外'],
    availability: 'available'
  },
  {
    id: 'sku-home-h1',
    name: 'H1 工业人形机器人',
    type: 'rent' as const,
    tag: '可租',
    priceCent: 89900,
    originalPriceCent: 99900,
    cover: 'linear-gradient(165deg,#eceff4 0%,#aebccf 100%)',
    specs: ['工业级', '负重', '长续航'],
    availability: 'available'
  },
  {
    id: 'sku-buy-go2',
    name: 'Go2 四足机器人（购）',
    type: 'buy' as const,
    tag: '新品',
    priceCent: 1899900,
    originalPriceCent: 1999900,
    cover: 'linear-gradient(165deg,#e6fff4 0%,#7dd9b8 100%)',
    specs: ['四足', '户外', '编程'],
    availability: 'available'
  }
]
