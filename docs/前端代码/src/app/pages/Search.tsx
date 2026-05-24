import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  Search as SearchIcon,
  ChevronLeft,
  X,
  Trash2,
  Eye,
  EyeOff,
  Battery,
  Wifi,
  Signal,
} from 'lucide-react';

const SEARCH_HISTORY = ["四足", "宇树", "机械狗"];

const DISCOVERY_ITEMS = [
  { id: 1, text: "狗", isHot: true },
  { id: 2, text: "H1系列" },
  { id: 3, text: "机械臂" },
  { id: 4, text: "Z1" },
  { id: 5, text: "人形机器人" },
  { id: 6, text: "B2" },
];

export default function Search() {
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState('Go2');
  const [history, setHistory] = useState(SEARCH_HISTORY);
  const [showDiscovery, setShowDiscovery] = useState(true);

  const getRankStyle = (rank: number) => {
    switch(rank) {
      case 1: return 'bg-[#F53F3F] text-white'; // Red
      case 2: return 'bg-[#FF7D00] text-white'; // Orange
      case 3: return 'bg-[#F7BA1E] text-white'; // Yellow
      default: return 'bg-transparent text-gray-400'; // Gray no bg
    }
  };

  return (
    <div className="max-w-[480px] mx-auto bg-white min-h-screen relative font-sans overflow-x-hidden text-gray-900 shadow-xl flex flex-col">
      {/* Sticky Header Section */}
      <div className="sticky top-0 z-50 bg-white border-b border-gray-100">
        {/* Fake iOS Status Bar */}
        <div className="flex justify-between items-center px-6 pt-3 pb-1 text-black font-semibold text-[15px]">
          <span>9:41</span>
          <div className="flex items-center gap-1.5">
            <Signal className="w-[18px] h-[18px]" />
            <Wifi className="w-4 h-4" />
            <Battery className="w-6 h-6" />
          </div>
        </div>

        {/* Search Header */}
        <div className="flex items-center gap-2.5 px-4 py-2.5">
          <ChevronLeft
            className="w-[22px] h-[22px] text-gray-900 cursor-pointer shrink-0"
            strokeWidth={2}
            onClick={() => navigate(-1)}
          />

          <div className="flex-1 flex items-center gap-2.5 bg-[#F5F6F8] rounded-full px-4 h-[36px]">
            <SearchIcon className="w-[17px] h-[17px] text-gray-400 shrink-0" strokeWidth={2} />
            <input
              type="text"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              placeholder="搜索商品/型号"
              className="flex-1 bg-transparent text-[14px] outline-none text-gray-900 placeholder:text-gray-400 h-full"
            />
            {searchValue && (
              <div
                className="w-[17px] h-[17px] rounded-full bg-[#C9CDD4] flex items-center justify-center cursor-pointer shrink-0"
                onClick={() => setSearchValue('')}
              >
                <X className="w-[11px] h-[11px] text-white" strokeWidth={2.5} />
              </div>
            )}
          </div>

          <span className="text-[15px] text-gray-900 shrink-0 cursor-pointer">
            搜索
          </span>
        </div>
      </div>

      <div className="px-4 pt-5 pb-4 flex-1 overflow-y-auto bg-white">
        {/* Search History */}
        {history.length > 0 && (
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-[15px] font-bold text-gray-900">历史搜索</h3>
              <Trash2
                className="w-[17px] h-[17px] text-gray-400 cursor-pointer"
                strokeWidth={2}
                onClick={() => setHistory([])}
              />
            </div>
            <div className="flex flex-wrap gap-2">
              {history.map(item => (
                <div
                  key={item}
                  onClick={() => setSearchValue(item)}
                  className="bg-[#F5F6F8] px-4 py-2 rounded-full text-[13px] text-gray-700 cursor-pointer transition-colors hover:bg-gray-200"
                >
                  {item}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Discovery / Popular Searches */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-[15px] font-bold text-gray-900">搜索发现</h3>
            {showDiscovery ? (
              <Eye
                className="w-[17px] h-[17px] text-gray-400 cursor-pointer"
                strokeWidth={2}
                onClick={() => setShowDiscovery(false)}
              />
            ) : (
              <EyeOff
                className="w-[17px] h-[17px] text-gray-400 cursor-pointer"
                strokeWidth={2}
                onClick={() => setShowDiscovery(true)}
              />
            )}
          </div>

          {showDiscovery && (
            <div className="grid grid-cols-2 gap-x-6 gap-y-4">
              {DISCOVERY_ITEMS.map(item => (
                <div
                  key={item.id}
                  className="flex items-center gap-3 cursor-pointer group"
                  onClick={() => setSearchValue(item.text)}
                >
                  <div className={`w-[17px] h-[17px] rounded-[3px] flex items-center justify-center text-[11px] font-bold shrink-0 ${getRankStyle(item.id)}`}>
                    {item.id}
                  </div>
                  <div className="flex items-center gap-1.5 flex-1 min-w-0">
                    <span className={`text-[14px] group-hover:text-[#0A4BFE] transition-colors truncate ${item.id <= 3 ? 'text-gray-900 font-medium' : 'text-gray-600'}`}>
                      {item.text}
                    </span>
                    {item.isHot && (
                      <span className="text-[9px] text-[#F53F3F] bg-[#FFECE8] px-1.5 py-[1px] rounded font-bold italic shrink-0">
                        HOT
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
          {!showDiscovery && (
            <div className="text-[13px] text-gray-400 text-center py-6 bg-[#F8F9FA] rounded-xl mt-2">
              当前搜索发现已隐藏
            </div>
          )}
        </div>
      </div>
    </div>
  );
}