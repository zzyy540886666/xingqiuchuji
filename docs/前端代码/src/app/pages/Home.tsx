import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  Search,
  ChevronRight,
  ShoppingCart,
  MoreHorizontal,
  Circle,
  Battery,
  Wifi,
  Signal
} from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

// Mock Data
const SCENES = [
  {
    title: "科研教育",
    tags: ["可租", "可买", "配软件"],
    img: "https://images.unsplash.com/photo-1767716134807-646b08712f6d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzY2llbmNlJTIwZWR1Y2F0aW9uJTIwcm9ib3R8ZW58MXx8fHwxNzc5MzUxOTU2fDA&ixlib=rb-4.1.0&q=80&w=1080"
  },
  {
    title: "工业巡检",
    tags: ["可租", "可买", "配软件"],
    img: "https://images.unsplash.com/photo-1716191299980-a6e8827ba10b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxmYWN0b3J5JTIwaW5zcGVjdGlvbiUyMHJvYm90fGVufDF8fHx8MTc3OTM1MTk2MHww&ixlib=rb-4.1.0&q=80&w=1080"
  },
  {
    title: "表演娱乐",
    tags: ["可租", "可买", "配软件"],
    img: "https://images.unsplash.com/photo-1697564265161-647d73c3fdf9?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMHBlcmZvcm1hbmNlJTIwc3RhZ2V8ZW58MXx8fHwxNzc5MzUxOTYwfDA&ixlib=rb-4.1.0&q=80&w=1080"
  },
  {
    title: "展会导览",
    tags: ["可租", "可买", "配软件"],
    img: "https://images.unsplash.com/photo-1774874646356-31d77723ca3e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGV4aGliaXRpb24lMjBldmVudHxlbnwxfHx8fDE3NzkzNTE5NjB8MA&ixlib=rb-4.1.0&q=80&w=1080"
  }
];

const PRODUCTS = [
  {
    id: 1,
    title: "G1-u2 人形机器人",
    stock: 16,
    tags: ["人形机器人", "智能交互", "全身运动"],
    price: "4900",
    img: "https://images.unsplash.com/photo-1737644467636-6b0053476bb2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxodW1hbm9pZCUyMHJvYm90fGVufDF8fHx8MTc3OTM1MTk1Nnww&ixlib=rb-4.1.0&q=80&w=1080"
  },
  {
    id: 2,
    title: "Go2-W 轮式机器人",
    stock: 20,
    tags: ["稳定可靠", "卓越性能", "多地形适应"],
    price: "699",
    img: "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"
  },
  {
    id: 3,
    title: "Go2 四足机器人",
    stock: 8,
    tags: ["高动态运动", "智能感知", "灵活敏捷"],
    price: "1999",
    img: "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"
  }
];

const dogRobotImg = "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080";
const humanoidRobotImg = "https://images.unsplash.com/photo-1737644467636-6b0053476bb2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxodW1hbm9pZCUyMHJvYm90fGVufDF8fHx8MTc3OTM1MTk1Nnww&ixlib=rb-4.1.0&q=80&w=1080";
const laptopImg = "https://images.unsplash.com/photo-1649442278981-73ef051defa1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBzb2Z0d2FyZXxlbnwxfHx8fDE3NzkzNTE5NTZ8MA&ixlib=rb-4.1.0&q=80&w=1080";

export default function Home() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('rent');

  return (
    <div className="pb-[90px]">
      {/* Sticky Header Section */}
      <div className="sticky top-0 z-50 bg-white">
        {/* Fake iOS Status Bar */}
        <div className="flex justify-between items-center px-6 pt-3 pb-1 text-black font-semibold text-[15px]">
          <span>9:41</span>
          <div className="flex items-center gap-1.5">
            <Signal className="w-[18px] h-[18px]" />
            <Wifi className="w-4 h-4" />
            <Battery className="w-6 h-6" />
          </div>
        </div>

        {/* Title Bar */}
        <div className="flex items-center justify-between px-4 py-2">
          <div className="flex items-center gap-2.5">
            {/* Logo */}
            <div className="w-[34px] h-[34px] rounded-full bg-gradient-to-tr from-orange-600 via-orange-400 to-yellow-400 relative overflow-hidden shadow-[0_2px_8px_rgba(249,115,22,0.3)]">
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[42px] h-3.5 border-[1.5px] border-white/60 rounded-[50%] -rotate-[22deg]"></div>
            </div>
            <div className="flex flex-col justify-center">
              <h1 className="text-[17px] font-bold leading-tight tracking-tight">星球·出机</h1>
              <p className="text-[10px] text-gray-500 mt-[1px]">机器人租赁、购买、软件服务平台</p>
            </div>
          </div>
          {/* Mini Program Capsule */}
          <div className="flex items-center justify-between px-3 py-1.5 bg-white border border-gray-200/80 rounded-full shadow-sm w-[84px]">
            <MoreHorizontal className="w-[18px] h-[18px] text-black" />
            <div className="w-[1px] h-4 bg-gray-200"></div>
            <Circle className="w-[18px] h-[18px] text-black" />
          </div>
        </div>

        {/* Search Bar */}
        <div className="px-4 py-2 pb-3">
          <div
            onClick={() => navigate('/search')}
            className="flex items-center gap-2 bg-white rounded-full p-1 pl-4 border-[1.5px] border-blue-500/20 shadow-[0_2px_10px_rgba(0,68,255,0.04)] cursor-pointer"
          >
            <Search className="w-[18px] h-[18px] text-gray-400 shrink-0" />
            <div className="flex-1 bg-transparent text-[13px] text-gray-400">
              搜索机器人、场景、品牌、软件
            </div>
            <button className="bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[13px] px-6 py-2 rounded-full font-medium shrink-0 shadow-sm shadow-blue-500/30 pointer-events-none">
              搜索
            </button>
          </div>
        </div>
      </div>

      {/* Main Scrollable Content */}
      <div className="bg-white px-4 pb-4 rounded-b-2xl shadow-sm">
        {/* Hero Banner */}
        <div className="relative w-full h-[210px] rounded-2xl overflow-hidden bg-gray-900 shadow-md">
          <ImageWithFallback
            src={humanoidRobotImg}
            className="absolute inset-0 w-full h-full object-cover object-top opacity-70 mix-blend-screen"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-black/90 via-black/50 to-transparent"></div>

          <div className="relative z-10 p-6 flex flex-col h-full justify-center">
            <div className="flex items-center gap-2 mb-2.5">
              <span className="font-black italic text-lg tracking-widest text-white">UNITREE</span>
              <span className="text-[13px] font-medium text-white/90">宇树科技</span>
            </div>
            <h2 className="text-[26px] font-bold mb-2 tracking-wide text-white leading-tight">
              宇树科技·智能未来
            </h2>
            <p className="text-[11px] text-gray-300 mb-4 opacity-90">租赁 / 购买 / 软件 · 一站式机器人服务平台</p>

            <div className="flex gap-2 mb-5">
              <span className="bg-[#0A4BFE]/90 backdrop-blur-sm text-[10px] px-2 py-0.5 rounded text-white border border-blue-400/30 font-medium">
                春季焕新季
              </span>
              <span className="bg-white/15 backdrop-blur-sm text-[10px] px-2 py-0.5 rounded text-white border border-white/20">
                租赁低至5折
              </span>
            </div>

            <div>
              <button className="bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[12px] px-4 py-1.5 rounded-full font-medium flex items-center gap-1 shadow-lg shadow-blue-500/40">
                立即探索 <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>

        {/* Categories */}
        <div className="grid grid-cols-3 gap-2.5 mt-4">
          {/* Card 1: Rent */}
          <div className="bg-[#F2F6FF] rounded-xl p-3 relative h-[140px] overflow-hidden flex flex-col items-start cursor-pointer transition-transform active:scale-95 group">
            <h3 className="font-bold text-gray-900 text-[14px] mb-1 relative z-10">租机器人</h3>
            <p className="text-[10px] text-gray-500 leading-[1.3] relative z-10">灵活租赁<br/>按需使用</p>
            <div className="mt-auto relative z-10">
              <div className="w-[22px] h-[22px] rounded-full bg-[#0A4BFE] flex items-center justify-center shadow-md shadow-blue-200 group-hover:scale-110 transition-transform">
                <ChevronRight className="w-3.5 h-3.5 text-white ml-[1px]" />
              </div>
            </div>
            <ImageWithFallback
              src={dogRobotImg}
              className="absolute -bottom-2 -right-3 w-[85px] h-[85px] object-cover rounded-tl-2xl mix-blend-multiply opacity-90"
            />
          </div>

          {/* Card 2: Buy */}
          <div className="bg-[#EEF9F8] rounded-xl p-3 relative h-[140px] overflow-hidden flex flex-col items-start cursor-pointer transition-transform active:scale-95 group">
            <h3 className="font-bold text-gray-900 text-[14px] mb-1 relative z-10">买机器人</h3>
            <p className="text-[10px] text-gray-500 leading-[1.3] relative z-10">品质保障<br/>快速交付</p>
            <div className="mt-auto relative z-10">
              <div className="w-[22px] h-[22px] rounded-full bg-teal-500 flex items-center justify-center shadow-md shadow-teal-200 group-hover:scale-110 transition-transform">
                <ChevronRight className="w-3.5 h-3.5 text-white ml-[1px]" />
              </div>
            </div>
            <ImageWithFallback
              src={humanoidRobotImg}
              className="absolute -bottom-2 -right-3 w-[85px] h-[85px] object-cover rounded-tl-2xl mix-blend-multiply opacity-90"
            />
          </div>

          {/* Card 3: App Store */}
          <div className="bg-[#F5F6FA] rounded-xl p-3 relative h-[140px] overflow-hidden flex flex-col items-start cursor-pointer transition-transform active:scale-95 group">
            <h3 className="font-bold text-gray-900 text-[14px] mb-1 relative z-10">应用商店</h3>
            <p className="text-[10px] text-gray-500 leading-[1.3] relative z-10">场景软件<br/>定制开发</p>
            <div className="mt-auto relative z-10">
              <div className="w-[22px] h-[22px] rounded-full bg-[#0A4BFE] flex items-center justify-center shadow-md shadow-blue-200 group-hover:scale-110 transition-transform">
                <ChevronRight className="w-3.5 h-3.5 text-white ml-[1px]" />
              </div>
            </div>
            <ImageWithFallback
              src={laptopImg}
              className="absolute bottom-0 -right-2 w-[80px] h-[70px] object-cover rounded-tl-2xl mix-blend-multiply opacity-90"
            />
          </div>
        </div>
      </div>

      {/* Scene Application Section */}
      <div className="bg-white mt-2.5 pt-5 pb-5 rounded-2xl shadow-sm">
        <div className="flex items-center justify-between px-4 mb-3.5">
          <h2 className="font-bold text-[16px] text-gray-900">场景应用</h2>
          <span className="text-[12px] text-gray-400 flex items-center gap-0.5 cursor-pointer hover:text-gray-600">
            更多场景 <ChevronRight className="w-3.5 h-3.5" />
          </span>
        </div>

        <div className="flex overflow-x-auto gap-3 px-4 pb-2 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
          {SCENES.map(scene => (
            <div key={scene.title} className="w-[125px] shrink-0 cursor-pointer group">
              <div className="w-full h-[85px] rounded-lg overflow-hidden mb-2.5 relative">
                <ImageWithFallback
                  src={scene.img}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
              </div>
              <h4 className="text-[13px] font-bold text-gray-800 mb-1.5">{scene.title}</h4>
              <div className="flex gap-1.5">
                {scene.tags.map(tag => (
                   <span key={tag} className="text-[9px] font-medium text-[#0A4BFE] bg-[#F2F6FF] px-1.5 py-[2px] rounded-sm">
                     {tag}
                   </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recommendations Section */}
      <div className="bg-white mt-2.5 pt-5 pb-6 rounded-t-2xl shadow-sm">
        <div className="flex items-center justify-between px-4 mb-4">
          <div className="flex items-center gap-3">
            <h2 className="font-bold text-[16px] text-gray-900">为你推荐</h2>
            <div className="flex gap-1 bg-gray-50 p-0.5 rounded-full border border-gray-100">
              <button
                onClick={() => setActiveTab('rent')}
                className={`text-[11px] px-3 py-1 rounded-full transition-colors ${activeTab === 'rent' ? 'font-bold text-[#0A4BFE] bg-[#F2F6FF]' : 'font-medium text-gray-500 hover:text-gray-700'}`}
              >
                可租赁
              </button>
              <button
                onClick={() => setActiveTab('buy')}
                className={`text-[11px] px-3 py-1 rounded-full transition-colors ${activeTab === 'buy' ? 'font-bold text-[#0A4BFE] bg-[#F2F6FF]' : 'font-medium text-gray-500 hover:text-gray-700'}`}
              >
                可购买
              </button>
            </div>
          </div>
          <span className="text-[12px] text-gray-400 flex items-center gap-0.5 cursor-pointer hover:text-gray-600">
            更多推荐 <ChevronRight className="w-3.5 h-3.5" />
          </span>
        </div>

        <div className="grid grid-cols-3 gap-2.5 px-4">
          {PRODUCTS.map(product => (
            <div key={product.id} className="bg-white rounded-xl overflow-hidden flex flex-col group cursor-pointer border border-transparent hover:border-blue-100 transition-colors">
              <div className="bg-[#F6F8FA] rounded-xl relative h-[130px] flex items-center justify-center p-2 mb-2.5 overflow-hidden">
                <ImageWithFallback
                  src={product.img}
                  className="w-[90%] h-[90%] object-cover rounded-lg mix-blend-multiply group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute bottom-1.5 left-1.5 bg-blue-100/80 backdrop-blur-sm text-[#0A4BFE] text-[9px] px-1.5 py-0.5 rounded-sm font-bold shadow-sm">
                  可租 {product.stock}
                </div>
              </div>

              <div className="px-0.5 flex flex-col flex-1">
                <h4 className="font-bold text-[12px] text-gray-900 mb-1.5 leading-tight line-clamp-1">
                  {product.title}
                </h4>

                <div className="flex gap-1 mb-2.5 flex-wrap h-[34px] overflow-hidden">
                  {product.tags.map(tag => (
                    <span key={tag} className="text-[9px] text-gray-500 bg-[#F5F6F8] px-1.5 py-0.5 rounded-sm whitespace-nowrap">
                      {tag}
                    </span>
                  ))}
                </div>

                <div className="mt-auto">
                  <div className="text-[10px] text-gray-400 mb-2">
                    租赁价 <span className="text-[#F53F3F] font-bold text-[14px] tracking-tight">¥{product.price}</span> /天起
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => navigate(`/product/${product.id}`)}
                      className="flex-1 bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[11px] py-1.5 rounded-full font-medium"
                    >
                      立即租赁
                    </button>
                    <button className="w-[26px] h-[26px] rounded-full border border-[#0A4BFE] hover:bg-blue-50 transition-colors flex items-center justify-center text-[#0A4BFE] shrink-0">
                      <ShoppingCart className="w-3.5 h-3.5 ml-[-1px]" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
