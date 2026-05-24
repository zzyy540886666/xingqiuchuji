import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  ChevronLeft,
  Share2,
  MoreHorizontal,
  Play,
  Heart,
  MessageCircle,
  ShoppingCart,
  ChevronRight,
} from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

const productImages = [
  "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080",
  "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080",
  "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080",
  "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080",
];

export default function ProductDetail() {
  const navigate = useNavigate();
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [rentalPlan, setRentalPlan] = useState<'short' | 'rental-buy' | 'buy'>('short');
  const [selectedDuration, setSelectedDuration] = useState('30天');
  const [isFavorite, setIsFavorite] = useState(false);
  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const services = [
    { id: 1, name: '信息登记与培训', price: '免费', checked: true },
    { id: 2, name: '技术支持与保障', price: '免费', checked: true },
    { id: 3, name: '场景应用软件配置', price: '免费', checked: true },
  ];

  const detailSections = [
    { id: 1, title: '客服服务功能详细介绍与服务条款', content: '服务详情内容...' },
    { id: 2, title: '产品包装清单及配送说明', content: '包装清单内容...' },
    { id: 3, title: '销售/云端相关技术文档及购买须知资格', content: '技术文档内容...' },
  ];

  const getPriceByPlan = () => {
    switch (rentalPlan) {
      case 'short':
        return { price: '999', unit: '/天起', original: '19999' };
      case 'rental-buy':
        return { price: '6999', unit: '/年起', original: null };
      case 'buy':
        return { price: '19999', unit: '/台起', original: null };
    }
  };

  const priceInfo = getPriceByPlan();

  return (
    <div className="max-w-[480px] mx-auto bg-[#F5F6F8] min-h-screen relative font-sans overflow-x-hidden text-gray-900 shadow-xl pb-[100px]">
      {/* Top Navigation Bar */}
      <div className="absolute top-0 left-0 right-0 z-50 bg-gradient-to-b from-black/50 to-transparent">
        <div className="flex items-center justify-between px-4 pt-12 pb-4">
          <div className="flex items-center gap-3">
            <div
              onClick={() => navigate(-1)}
              className="w-8 h-8 rounded-full bg-black/30 backdrop-blur-sm flex items-center justify-center cursor-pointer"
            >
              <ChevronLeft className="w-5 h-5 text-white" strokeWidth={2} />
            </div>
            <div className="flex flex-col">
              <span className="text-white text-[15px] font-bold">Unitree Go2</span>
              <span className="text-white/80 text-[11px]">宇树科技</span>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-black/30 backdrop-blur-sm flex items-center justify-center cursor-pointer">
              <Share2 className="w-[18px] h-[18px] text-white" strokeWidth={2} />
            </div>
            <div className="w-8 h-8 rounded-full bg-black/30 backdrop-blur-sm flex items-center justify-center cursor-pointer">
              <MoreHorizontal className="w-[18px] h-[18px] text-white" strokeWidth={2} />
            </div>
          </div>
        </div>
      </div>

      {/* Product Image Carousel */}
      <div className="relative w-full h-[380px] bg-gradient-to-br from-slate-800 via-slate-850 to-slate-900">
        <ImageWithFallback
          src={productImages[currentImageIndex]}
          className="w-full h-full object-cover opacity-90"
          alt="Product"
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="w-[60px] h-[60px] rounded-full bg-black/30 backdrop-blur-md flex items-center justify-center cursor-pointer border-[2.5px] border-white/40 shadow-xl">
            <Play className="w-7 h-7 text-white ml-0.5" fill="white" strokeWidth={0} />
          </div>
        </div>
        <div className="absolute bottom-3.5 right-3.5 bg-black/50 backdrop-blur-md px-2.5 py-1 rounded-full text-white text-[11px] font-medium">
          {currentImageIndex + 1}/{productImages.length}
        </div>
      </div>

      {/* Product Info Card */}
      <div className="bg-white rounded-t-[24px] -mt-5 relative z-10 px-4 pt-4 pb-4 shadow-sm">
        {/* Title and Tags */}
        <div className="mb-3">
          <div className="flex items-start gap-2 mb-1.5">
            <h1 className="flex-1 text-[17px] font-bold text-gray-900 leading-tight">
              宇树科技 四足机器人 Go2
            </h1>
            <span className="bg-[#E8F3FF] text-[#0A4BFE] text-[11px] px-2 py-0.5 rounded font-bold shrink-0">
              租
            </span>
            <span className="bg-[#E8F9F6] text-[#00B578] text-[11px] px-2 py-0.5 rounded font-bold shrink-0">
              购
            </span>
          </div>
          <p className="text-[12px] text-gray-500 mb-2.5">Unitree Go2</p>
          <div className="flex flex-wrap gap-1.5">
            {['性能强悍', '高效轻便', '多环境适配', '强大算力', '灵活开发'].map((tag) => (
              <span
                key={tag}
                className="bg-[#F2F6FF] text-[#0A4BFE] text-[11px] px-2 py-0.5 rounded font-medium"
              >
                {tag}
              </span>
            ))}
          </div>

          {/* Scene and Stock Info */}
          <div className="space-y-2 mt-3">
            <div className="text-[12px] text-gray-600">
              <span className="text-gray-700 font-medium">适配场景：</span>
              <span className="text-gray-600">工业巡检、户外安防、应急救援、科研教育</span>
            </div>
            <div className="text-[12px]">
              <span className="text-gray-700 font-medium">库存状态：</span>
              <span className="text-[#00B578] font-bold">现货充足</span>
              <span className="text-gray-600"> · </span>
              <span className="text-gray-600">全国可租可发</span>
            </div>
          </div>
        </div>

        {/* Rental Plan Tabs */}
        <div className="flex gap-1.5 mb-3.5 bg-[#F5F6F8] p-1 rounded-lg">
          <button
            onClick={() => setRentalPlan('short')}
            className={`flex-1 text-[12px] py-2 rounded-md transition-all ${
              rentalPlan === 'short'
                ? 'bg-white text-[#0A4BFE] font-bold shadow-sm'
                : 'text-gray-600 font-medium'
            }`}
          >
            短期租赁
          </button>
          <button
            onClick={() => setRentalPlan('rental-buy')}
            className={`flex-1 text-[12px] py-2 rounded-md transition-all ${
              rentalPlan === 'rental-buy'
                ? 'bg-white text-[#0A4BFE] font-bold shadow-sm'
                : 'text-gray-600 font-medium'
            }`}
          >
            租赁购买
          </button>
          <button
            onClick={() => setRentalPlan('buy')}
            className={`flex-1 text-[12px] py-2 rounded-md transition-all ${
              rentalPlan === 'buy'
                ? 'bg-white text-[#F53F3F] font-bold shadow-sm'
                : 'text-gray-600 font-medium'
            }`}
          >
            购买买断
          </button>
        </div>

        {/* Price Display */}
        <div className="flex items-center gap-3 mb-4 bg-gradient-to-r from-red-50/50 to-orange-50/30 -mx-4 px-4 py-3 rounded-xl">
          <div className="flex-1 text-center">
            <div className="text-[10px] text-gray-500 mb-1">短期租赁</div>
            <div className="flex items-baseline justify-center gap-1">
              <span className={`text-[20px] font-bold leading-none ${rentalPlan === 'short' ? 'text-[#F53F3F]' : 'text-gray-400'}`}>
                ¥999
              </span>
              <span className="text-[11px] text-gray-500">/天起</span>
            </div>
          </div>
          <div className="w-[1px] h-8 bg-gray-200"></div>
          <div className="flex-1 text-center">
            <div className="text-[10px] text-gray-500 mb-1">租赁购买</div>
            <div className="flex items-baseline justify-center gap-1">
              <span className={`text-[20px] font-bold leading-none ${rentalPlan === 'rental-buy' ? 'text-[#F53F3F]' : 'text-gray-400'}`}>
                ¥6999
              </span>
              <span className="text-[11px] text-gray-500">/年起</span>
            </div>
          </div>
          <div className="w-[1px] h-8 bg-gray-200"></div>
          <div className="flex-1 text-center">
            <div className="text-[10px] text-gray-500 mb-1">购买买断</div>
            <div className="flex items-baseline justify-center gap-1">
              <span className={`text-[20px] font-bold leading-none ${rentalPlan === 'buy' ? 'text-[#F53F3F]' : 'text-gray-400'}`}>
                ¥19999
              </span>
              <span className="text-[11px] text-gray-500">/台起</span>
            </div>
          </div>
        </div>
        {rentalPlan === 'short' && (
          <div className="text-[11px] text-gray-400 mb-3 -mt-2">
            原价 <span className="line-through">¥19999</span>
          </div>
        )}

        {/* Duration Selection */}
        {rentalPlan === 'short' && (
          <div className="mb-4 pb-4 border-b border-gray-100">
            <div className="text-[13px] text-gray-700 mb-2.5 font-medium">时长</div>
            <div className="flex gap-2.5 mb-3">
              {['30天', '7天', '1天'].map((duration) => (
                <button
                  key={duration}
                  onClick={() => setSelectedDuration(duration)}
                  className={`flex-1 py-2.5 rounded-lg text-[13px] border-2 transition-colors font-medium ${
                    selectedDuration === duration
                      ? 'border-[#0A4BFE] bg-[#F2F6FF] text-[#0A4BFE] font-bold'
                      : 'border-gray-200 bg-white text-gray-600'
                  }`}
                >
                  {duration}
                </button>
              ))}
            </div>

            {/* Service Tags */}
            <div className="flex flex-wrap gap-2 pt-1">
              <div className="flex items-center gap-1.5 bg-[#F2F6FF] px-2.5 py-1.5 rounded-md">
                <svg className="w-3.5 h-3.5 text-[#0A4BFE]" viewBox="0 0 16 16" fill="none">
                  <path d="M8 1L10.5 6L16 6.75L12 10.5L13 16L8 13.25L3 16L4 10.5L0 6.75L5.5 6L8 1Z" fill="currentColor"/>
                </svg>
                <span className="text-[11px] text-[#0A4BFE] font-medium">押金 ¥5000 (可免押)</span>
              </div>
              <div className="flex items-center gap-1.5 bg-[#F2F6FF] px-2.5 py-1.5 rounded-md">
                <svg className="w-3.5 h-3.5 text-[#0A4BFE]" viewBox="0 0 16 16" fill="none">
                  <path d="M8 14C11.3137 14 14 11.3137 14 8C14 4.68629 11.3137 2 8 2C4.68629 2 2 4.68629 2 8C2 11.3137 4.68629 14 8 14Z" stroke="currentColor" strokeWidth="1.5" fill="none"/>
                  <path d="M8 5V8L10 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
                <span className="text-[11px] text-[#0A4BFE] font-medium">运费险·全程保障</span>
              </div>
              <div className="flex items-center gap-1.5 bg-[#F2F6FF] px-2.5 py-1.5 rounded-md">
                <svg className="w-3.5 h-3.5 text-[#0A4BFE]" viewBox="0 0 16 16" fill="none">
                  <path d="M2 8L6 12L14 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span className="text-[11px] text-[#0A4BFE] font-medium">开箱售后换货品</span>
              </div>
            </div>
          </div>
        )}

        {/* Service Options */}
        <div className="mb-3 pb-3 border-b border-gray-100">
          <div className="text-[13px] text-gray-700 mb-3 font-medium">服务</div>
          <div className="space-y-2.5">
            {services.map((service) => (
              <div
                key={service.id}
                className="flex items-center justify-between cursor-pointer"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-[18px] h-[18px] rounded border-2 border-[#0A4BFE] bg-[#0A4BFE] flex items-center justify-center shrink-0">
                    <svg className="w-3 h-3 text-white" viewBox="0 0 12 12" fill="none">
                      <path
                        d="M2 6L5 9L10 3"
                        stroke="currentColor"
                        strokeWidth="2.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  </div>
                  <span className="text-[13px] text-gray-900">{service.name}</span>
                </div>
                <span className="text-[12px] text-[#00B578] font-bold">{service.price}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Product Parameters */}
        <div className="mb-3 pb-3 border-b border-gray-100">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-[13px] text-gray-700 font-medium">产品参数</h3>
            <span className="text-[12px] text-gray-400 flex items-center gap-1 cursor-pointer">
              更多参数 <ChevronRight className="w-3.5 h-3.5" strokeWidth={2} />
            </span>
          </div>
          <div className="flex items-start justify-between gap-2">
            <div className="flex flex-col items-center flex-1">
              <svg className="w-4 h-4 text-gray-400 mb-1" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="4" width="16" height="16" stroke="currentColor" strokeWidth="2" rx="2"/>
                <path d="M9 4V20M15 4V20M4 9H20M4 15H20" stroke="currentColor" strokeWidth="2"/>
              </svg>
              <span className="text-[10px] text-gray-500 mb-0.5">尺寸</span>
              <span className="text-[10px] text-gray-900 font-medium text-center leading-tight">Unitree Go2</span>
              <span className="text-[9px] text-gray-400 text-center mt-0.5">700×400×400mm</span>
            </div>
            <div className="flex flex-col items-center flex-1">
              <svg className="w-4 h-4 text-gray-400 mb-1" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15 8L22 9L17 14L18 21L12 18L6 21L7 14L2 9L9 8L12 2Z" stroke="currentColor" strokeWidth="2" fill="none"/>
              </svg>
              <span className="text-[10px] text-gray-500 mb-0.5">续航</span>
              <span className="text-[10px] text-gray-900 font-medium text-center">2~4 小时</span>
            </div>
            <div className="flex flex-col items-center flex-1">
              <svg className="w-4 h-4 text-gray-400 mb-1" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="3" fill="currentColor"/>
                <path d="M12 2V6M12 18V22M22 12H18M6 12H2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <span className="text-[10px] text-gray-500 mb-0.5">负载</span>
              <span className="text-[10px] text-gray-900 font-medium text-center">≤5kg</span>
            </div>
            <div className="flex flex-col items-center flex-1">
              <svg className="w-4 h-4 text-gray-400 mb-1" viewBox="0 0 24 24" fill="none">
                <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" strokeWidth="2" fill="none"/>
              </svg>
              <span className="text-[10px] text-gray-500 mb-0.5">最大速度</span>
              <span className="text-[10px] text-gray-900 font-medium text-center">3.5 m/s</span>
            </div>
          </div>
        </div>
      </div>

      {/* Product Details */}
      <div className="bg-white mt-2 px-4 py-4">
        <h3 className="text-[15px] font-bold text-gray-900 mb-2">产品说明</h3>
        <div className="space-y-0">
          {detailSections.map((section, index) => (
            <div key={section.id} className={`${index !== detailSections.length - 1 ? 'border-b border-gray-100' : ''}`}>
              <div
                onClick={() =>
                  setExpandedSection(expandedSection === section.title ? null : section.title)
                }
                className="flex items-start justify-between py-3.5 cursor-pointer gap-3"
              >
                <span className="text-[13px] text-gray-700 flex-1 leading-relaxed">{section.title}</span>
                <ChevronRight
                  className={`w-4 h-4 text-gray-400 transition-transform shrink-0 mt-0.5 ${
                    expandedSection === section.title ? 'rotate-90' : ''
                  }`}
                  strokeWidth={2}
                />
              </div>
              {expandedSection === section.title && (
                <div className="pb-4 pt-0 text-[13px] text-gray-600 leading-relaxed">{section.content}</div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Bottom Action Bar */}
      <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] bg-white border-t border-gray-100 px-3 py-2.5 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.06)]">
        <div className="flex items-center gap-2.5">
          <div className="flex items-center gap-3">
            <div
              onClick={() => setIsFavorite(!isFavorite)}
              className="flex flex-col items-center gap-0.5 cursor-pointer min-w-[44px]"
            >
              <Heart
                className={`w-[21px] h-[21px] ${
                  isFavorite ? 'fill-[#F53F3F] text-[#F53F3F]' : 'text-gray-400'
                }`}
                strokeWidth={2}
              />
              <span className="text-[10px] text-gray-500 font-medium">收藏</span>
            </div>
            <div className="flex flex-col items-center gap-0.5 cursor-pointer min-w-[44px]">
              <MessageCircle className="w-[21px] h-[21px] text-gray-400" strokeWidth={2} />
              <span className="text-[10px] text-gray-500 font-medium">客服</span>
            </div>
            <div className="flex flex-col items-center gap-0.5 cursor-pointer min-w-[44px]">
              <Share2 className="w-[21px] h-[21px] text-gray-400" strokeWidth={2} />
              <span className="text-[10px] text-gray-500 font-medium">分享</span>
            </div>
          </div>
          <div className="flex-1 flex gap-2">
            <button
              onClick={() => navigate('/order/confirm')}
              className="flex-1 bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[14px] py-2.5 rounded-full font-bold shadow-md shadow-blue-500/25"
            >
              立即租赁
            </button>
            <button className="flex-1 bg-white border-[1.5px] border-[#0A4BFE] hover:bg-blue-50 transition-colors text-[#0A4BFE] text-[13px] py-2.5 rounded-full font-bold flex items-center justify-center gap-1">
              加入购物车
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
