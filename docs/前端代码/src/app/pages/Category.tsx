import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  Search,
  ShoppingCart,
  MoreHorizontal,
  Circle,
  Battery,
  Wifi,
  Signal,
  Filter,
  ArrowUpDown,
  ChevronDown
} from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

// Top Mode Tabs
const TOP_TABS = ['租机器人', '买机器人', '软件程序'];

// Nested Categories for the Left Sidebar
const LEFT_MENU = [
  {
    title: '品牌',
    subcategories: ['全部品牌', '宇树科技', '傅利叶', '智元机器人', '优必选', '达闼科技', '银河通用']
  },
  {
    title: '价格区间',
    subcategories: ['全部价格', '0-1000', '1000-5000', '5000-10000', '10000以上']
  },
  {
    title: '适配机型',
    subcategories: ['全部机型', 'G1系列', 'H1系列', 'Go2系列', 'Z1系列']
  },
  {
    title: '热门场景',
    subcategories: ['全部场景', '教育科研', '工业制造', '商业服务', '特种作业']
  }
];

// Mock Images
const dogRobotImg = "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080";
const humanoidRobotImg = "https://images.unsplash.com/photo-1737644467636-6b0053476bb2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxodW1hbm9pZCUyMHJvYm90fGVufDF8fHx8MTc3OTM1MTk1Nnww&ixlib=rb-4.1.0&q=80&w=1080";
const armImg = "https://images.unsplash.com/photo-1716191299980-a6e8827ba10b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdGljJTIwYXJtfGVufDF8fHx8MTc3OTI2MjU0Mnww&ixlib=rb-4.1.0&q=80&w=1080";

const ALL_PRODUCTS = [
  {
    id: 1,
    category: '宇树科技',
    title: "G1-u2 人形机器人",
    stock: 16,
    tags: ["智能交互", "全身运动"],
    price: "4900",
    img: humanoidRobotImg
  },
  {
    id: 2,
    category: '傅利叶',
    title: "GR-1 通用人形机器人",
    stock: 5,
    tags: ["全地形", "高负载", "教育科研"],
    price: "9900",
    img: humanoidRobotImg
  },
  {
    id: 3,
    category: '宇树科技',
    title: "Go2 四足机器人",
    stock: 8,
    tags: ["高动态运动", "智能感知"],
    price: "1999",
    img: dogRobotImg
  },
  {
    id: 4,
    category: '优必选',
    title: "Walker S 工业版",
    stock: 20,
    tags: ["稳定可靠", "卓越性能"],
    price: "699",
    img: dogRobotImg
  },
  {
    id: 5,
    category: '智元机器人',
    title: "远征 A1 智能机械臂",
    stock: 12,
    tags: ["高精度", "协作抓取"],
    price: "2599",
    img: armImg
  },
  {
    id: 6,
    category: '全部品牌',
    title: "G1-u2 人形机器人",
    stock: 16,
    tags: ["智能交互", "全身运动"],
    price: "4900",
    img: humanoidRobotImg
  },
  {
    id: 7,
    category: '全部品牌',
    title: "Go2 四足机器人",
    stock: 8,
    tags: ["高动态运动", "智能感知"],
    price: "1999",
    img: dogRobotImg
  },
  {
    id: 8,
    category: '全部品牌',
    title: "Z1 智能机械臂",
    stock: 12,
    tags: ["高精度", "协作抓取"],
    price: "2599",
    img: armImg
  }
];

export default function Category() {
  const navigate = useNavigate();
  const [activeTopTab, setActiveTopTab] = useState('租机器人');
  const [activeSubcategory, setActiveSubcategory] = useState('宇树科技');
  const [activeFilter, setActiveFilter] = useState('综合排序');

  // Expand/collapse states for the left menu
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>({
    '品牌': true,
    '价格区间': true,
    '适配机型': true,
    '热门场景': true,
  });

  const toggleSection = (title: string) => {
    setExpandedSections(prev => ({
      ...prev,
      [title]: !prev[title]
    }));
  };

  const filteredProducts = ALL_PRODUCTS.filter(p => p.category === activeSubcategory || (activeSubcategory.includes('全部') && ['全部品牌'].includes(p.category)));

  // If no products found for a category, just show recommendations to simulate data
  const displayProducts = filteredProducts.length > 0 ? filteredProducts : ALL_PRODUCTS.filter(p => p.category === '全部品牌');

  return (
    <div className="flex flex-col h-full absolute top-0 left-0 right-0 bottom-[90px] bg-white">
      {/* Sticky Header Section */}
      <div className="sticky top-0 z-50 bg-white shadow-sm">
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
          <div className="w-[84px]"></div> {/* Spacer for balance */}
          <h1 className="text-[17px] font-bold leading-tight tracking-tight text-center">分类</h1>
          {/* Mini Program Capsule */}
          <div className="flex items-center justify-between px-3 py-1.5 bg-white border border-gray-200/80 rounded-full shadow-sm w-[84px]">
            <MoreHorizontal className="w-[18px] h-[18px] text-black" />
            <div className="w-[1px] h-4 bg-gray-200"></div>
            <Circle className="w-[18px] h-[18px] text-black" />
          </div>
        </div>

        {/* Search Bar */}
        <div className="px-4 py-1 pb-2">
          <div
            onClick={() => navigate('/search')}
            className="flex items-center gap-2 bg-[#F5F6F8] rounded-full p-2 pl-4 cursor-pointer"
          >
            <Search className="w-[16px] h-[16px] text-gray-400 shrink-0" />
            <div className="flex-1 bg-transparent text-[13px] text-gray-400">
              搜索商品/型号
            </div>
          </div>
        </div>

        {/* Top Segmented Tabs: 租机器人 / 买机器人 / 软件程序 */}
        <div className="flex px-4 py-2 border-b border-gray-100 gap-6">
          {TOP_TABS.map(tab => {
            const isActive = activeTopTab === tab;
            return (
              <div
                key={tab}
                onClick={() => setActiveTopTab(tab)}
                className={`text-[14px] pb-1.5 relative cursor-pointer transition-colors ${
                  isActive ? 'font-bold text-[#0A4BFE]' : 'font-medium text-gray-500'
                }`}
              >
                {tab}
                {isActive && (
                  <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-[3px] bg-[#0A4BFE] rounded-full"></div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Main Content Split */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar Navigation */}
        <div className="w-[90px] bg-[#F7F8FA] overflow-y-auto shrink-0 pb-6 [&::-webkit-scrollbar]:hidden">
          {LEFT_MENU.map(section => (
            <div key={section.title} className="mb-2">
              {/* Section Header */}
              <div
                onClick={() => toggleSection(section.title)}
                className="py-3 px-2 flex items-center justify-between text-[13px] font-bold text-gray-800 cursor-pointer"
              >
                {section.title}
                <ChevronDown className={`w-3.5 h-3.5 text-gray-400 transition-transform ${expandedSections[section.title] ? 'rotate-180' : ''}`} />
              </div>

              {/* Subcategories */}
              {expandedSections[section.title] && (
                <div className="flex flex-col">
                  {section.subcategories.map(sub => {
                    const isActive = activeSubcategory === sub;
                    return (
                      <div
                        key={sub}
                        onClick={() => setActiveSubcategory(sub)}
                        className={`py-3 px-2 text-center text-[12px] cursor-pointer transition-colors relative ${
                          isActive
                            ? 'bg-white text-[#0A4BFE] font-bold'
                            : 'text-gray-600 font-medium hover:text-gray-800'
                        }`}
                      >
                        {isActive && (
                          <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-3.5 bg-[#0A4BFE] rounded-r-full"></div>
                        )}
                        {sub}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Right Content Area */}
        <div className="flex-1 bg-white overflow-y-auto [&::-webkit-scrollbar]:hidden flex flex-col">

          {/* Top Filters for Product List */}
          {/* 品牌, 价格, 适配机型, 综合排序, 筛选 */}
          <div className="flex items-center justify-between px-3 py-3 shrink-0 bg-white sticky top-0 z-10 border-b border-gray-50/50">
            {['综合排序', '品牌', '价格', '适配机型'].map(filter => (
              <div
                key={filter}
                onClick={() => setActiveFilter(filter)}
                className={`flex items-center gap-0.5 cursor-pointer ${
                  activeFilter === filter ? 'text-[#0A4BFE] font-bold' : 'text-gray-500 font-medium'
                }`}
              >
                <span className="text-[12px]">{filter}</span>
                {filter === '价格' && <ArrowUpDown className="w-2.5 h-2.5" />}
                {(filter === '品牌' || filter === '适配机型') && <ChevronDown className="w-2.5 h-2.5" />}
              </div>
            ))}

            <div className="flex items-center gap-0.5 cursor-pointer ml-1 pl-2 border-l border-gray-200">
              <span className="text-[12px] font-medium text-gray-500">筛选</span>
              <Filter className="w-2.5 h-2.5 text-gray-500" />
            </div>
          </div>

          {/* Product List - 1 Column */}
          <div className="flex flex-col px-3 pb-6 pt-1">
            {displayProducts.map(product => (
              <div key={product.id} className="flex gap-3 py-3 border-b border-gray-100 last:border-0 group cursor-pointer">
                <div className="w-[100px] h-[100px] bg-[#F6F8FA] rounded-lg relative flex items-center justify-center shrink-0 overflow-hidden">
                  <ImageWithFallback
                    src={product.img}
                    className="w-[90%] h-[90%] object-cover mix-blend-multiply group-hover:scale-105 transition-transform duration-300"
                  />
                  <div className="absolute bottom-1 left-1 bg-blue-100/90 backdrop-blur-sm text-[#0A4BFE] text-[9px] px-1.5 py-[2px] rounded font-bold shadow-sm">
                    可租 {product.stock}
                  </div>
                </div>

                <div className="flex flex-col flex-1 justify-between py-0.5">
                  <div>
                    <h4 className="font-bold text-[14px] text-gray-900 mb-1.5 leading-tight line-clamp-2">
                      {product.title}
                    </h4>

                    <div className="flex gap-1.5 mb-1.5 flex-wrap">
                      {product.tags.map(tag => (
                        <span key={tag} className="text-[10px] text-gray-500 bg-[#F5F6F8] px-1.5 py-0.5 rounded-sm whitespace-nowrap">
                          {tag}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-end justify-between mt-auto">
                    <div className="text-[10px] text-gray-400">
                      <span className="text-[#F53F3F] font-bold text-[16px] tracking-tight">¥{product.price}</span> /天起
                    </div>

                    <div className="flex items-center gap-1.5">
                      <button className="bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[12px] px-3 py-1.5 rounded-full font-medium">
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
    </div>
  );
}
