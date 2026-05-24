import React from 'react';
import { useNavigate } from 'react-router';
import { ChevronRight } from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

export default function Profile() {
  const navigate = useNavigate();

  return (
    <div className="w-full min-h-screen bg-[#F2F4F7] pb-[120px] relative">
      {/* Blue Gradient Header Background */}
      <div className="absolute top-0 left-0 right-0 h-[340px] bg-gradient-to-b from-[#1E6FFF] via-[#3A82FF] to-[#5A9BFF] overflow-hidden">
        {/* Decorative curves */}
        <svg className="absolute top-[80px] right-[-30px] w-[200px] h-[160px] opacity-20" viewBox="0 0 200 160" fill="none">
          <path d="M0 80 Q50 20 100 60 T200 40" stroke="white" strokeWidth="1" fill="none"/>
          <path d="M0 110 Q60 50 120 90 T200 70" stroke="white" strokeWidth="1" fill="none"/>
        </svg>
      </div>

      <div className="relative z-10">
        {/* Status Bar */}
        <div className="h-[44px] flex items-center justify-between px-[15px] pt-[8px]">
          <div className="text-[14px] font-semibold text-white">9:43</div>
          <div className="flex items-center gap-[6px]">
            <svg className="w-[17px] h-[11px]" viewBox="0 0 17 11" fill="none">
              <rect x="0.5" y="0.5" width="15" height="10" rx="2" stroke="white" strokeWidth="1" fill="none"/>
              <rect x="2" y="2" width="13" height="8" fill="white"/>
              <rect x="16" y="3.5" width="1" height="4" fill="white"/>
            </svg>
            <svg className="w-[15px] h-[11px]" viewBox="0 0 15 11" fill="none">
              <path d="M7.5 2C10 2 12 3.5 12 5.5L13.5 4C13 1.5 10.5 0 7.5 0C4.5 0 2 1.5 1.5 4L3 5.5C3 3.5 5 2 7.5 2Z" fill="white"/>
              <circle cx="7.5" cy="9" r="1.5" fill="white"/>
            </svg>
            <svg className="w-[25px] h-[12px]" viewBox="0 0 25 12" fill="none">
              <rect x="0.5" y="0.5" width="21" height="11" rx="2.5" stroke="white" strokeWidth="1" fill="none"/>
              <rect x="2" y="2" width="18" height="8" rx="1" fill="white"/>
              <rect x="23" y="4" width="2" height="4" rx="1" fill="white"/>
            </svg>
          </div>
        </div>

        {/* Top Action Bar */}
        <div className="h-[40px] flex items-center justify-end px-[15px] gap-[10px]">
          <button
            onClick={() => navigate('/settings')}
            className="w-[28px] h-[28px] rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
          >
            <svg className="w-[16px] h-[16px]" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="2" stroke="white" strokeWidth="1.5"/>
              <path d="M8 1V3M8 13V15M15 8H13M3 8H1M12.95 3.05L11.54 4.46M4.46 11.54L3.05 12.95M12.95 12.95L11.54 11.54M4.46 4.46L3.05 3.05" stroke="white" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </button>
          <button
            onClick={() => navigate('/notifications')}
            className="w-[28px] h-[28px] rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
          >
            <svg className="w-[16px] h-[16px]" viewBox="0 0 16 16" fill="none">
              <path d="M8 1.5C5.5 1.5 3.5 3.5 3.5 6V9L2 11H14L12.5 9V6C12.5 3.5 10.5 1.5 8 1.5Z" stroke="white" strokeWidth="1.5" strokeLinejoin="round" fill="none"/>
              <path d="M6.5 13C6.5 13.8 7.2 14.5 8 14.5C8.8 14.5 9.5 13.8 9.5 13" stroke="white" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        {/* User Info Section */}
        <div className="px-[20px] mt-[8px] flex items-center gap-[12px]">
          <button
            onClick={() => navigate('/profile/edit')}
            className="w-[60px] h-[60px] rounded-full overflow-hidden bg-white/20 border-[2px] border-white/40 flex-shrink-0 active:scale-95 transition-transform"
          >
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
              alt="avatar"
              className="w-full h-full object-cover"
            />
          </button>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-[6px] mb-[6px]">
              <h2 className="text-[18px] font-semibold text-white">星球玩家小白</h2>
              <div className="flex items-center gap-[3px] px-[6px] py-[2px] bg-gradient-to-r from-[#FFB84D] to-[#FF8A00] rounded-[10px]">
                <svg className="w-[10px] h-[10px]" viewBox="0 0 10 10" fill="none">
                  <path d="M5 0.5L6.2 3.5L9.5 3.8L7 5.9L7.7 9L5 7.5L2.3 9L3 5.9L0.5 3.8L3.8 3.5L5 0.5Z" fill="white"/>
                </svg>
                <span className="text-[10px] font-semibold text-white whitespace-nowrap">Lv.1 微信积分</span>
              </div>
            </div>
            <p className="text-[12px] text-white/85">ID: XGWJX8270S0001 复制邀请码 成为星球玩家</p>
          </div>
        </div>

        {/* Membership Card */}
        <div className="mx-[15px] mt-[16px] bg-gradient-to-br from-[#E8F1FF] via-[#F0F5FF] to-[#DBE8FF] rounded-[14px] p-[14px] shadow-lg relative overflow-hidden">
          <svg className="absolute top-0 right-0 w-[120px] h-[80px] opacity-30" viewBox="0 0 120 80" fill="none">
            <path d="M0 40 Q30 10 60 30 T120 20" stroke="#1E6FFF" strokeWidth="0.5" fill="none"/>
            <path d="M0 60 Q40 30 80 50 T120 40" stroke="#1E6FFF" strokeWidth="0.5" fill="none"/>
          </svg>
          <div className="flex items-center justify-between mb-[10px] relative z-10">
            <div className="flex items-center gap-[6px]">
              <svg className="w-[16px] h-[16px]" viewBox="0 0 16 16" fill="none">
                <path d="M8 1L10 5.5L15 6L11 9.5L12 14.5L8 12L4 14.5L5 9.5L1 6L6 5.5L8 1Z" fill="#1E6FFF"/>
              </svg>
              <span className="text-[13px] font-semibold text-[#1A1A1A]">星球住民卡</span>
              <span className="text-[10px] text-[#999999]">有效期至：2026-06-15</span>
            </div>
            <button
              onClick={() => navigate('/membership')}
              className="flex items-center text-[11px] text-[#1E6FFF]"
            >
              查看会员权益
              <ChevronRight className="w-[10px] h-[10px]" />
            </button>
          </div>

          <div className="flex items-center justify-between relative z-10">
            <div className="flex-1 mr-[10px]">
              <div className="flex items-center justify-between mb-[6px]">
                <div className="flex items-center gap-[4px]">
                  <span className="px-[6px] py-[1px] bg-gradient-to-r from-[#FFB84D] to-[#FF8A00] rounded-[8px] text-[10px] text-white font-semibold">Lv.1</span>
                  <span className="text-[10px] text-[#666666]">当前等级</span>
                </div>
                <span className="text-[10px] text-[#999999]">320/1000</span>
              </div>
              <div className="relative h-[6px] bg-white/60 rounded-full overflow-hidden mb-[4px]">
                <div className="absolute left-0 top-0 h-full w-[32%] bg-gradient-to-r from-[#FFB84D] to-[#FF8A00] rounded-full" />
              </div>
              <p className="text-[10px] text-[#999999]">再升级680积分将晋升 Lv.2</p>
            </div>
            <button
              onClick={() => navigate('/growth')}
              className="px-[14px] py-[6px] bg-gradient-to-r from-[#FFB84D] to-[#FF8A00] rounded-[14px] text-[11px] font-semibold text-white shadow-md active:scale-95 transition-transform whitespace-nowrap"
            >
              成长中心
            </button>
          </div>
        </div>
      </div>

      {/* Stats Row */}
      <div className="mx-[15px] mt-[12px] bg-white rounded-[12px] py-[16px] shadow-sm relative z-10">
        <div className="grid grid-cols-4">
          {[
            { value: '2次', label: '剩余先享', onClick: () => navigate('/stats/preview') },
            { value: '1次', label: '续保次数', onClick: () => navigate('/stats/renewal') },
            { value: '9.5折', label: '配件折扣', onClick: () => navigate('/stats/discount') },
            { value: '5,680', label: '元享币余', onClick: () => navigate('/stats/coins') },
          ].map((item, index) => (
            <button
              key={index}
              onClick={item.onClick}
              className="flex flex-col items-center active:scale-95 transition-transform"
            >
              <p className="text-[18px] font-semibold text-[#1A1A1A] mb-[2px]">{item.value}</p>
              <p className="text-[11px] text-[#999999]">{item.label}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Function Grid */}
      <div className="mx-[15px] mt-[12px] bg-white rounded-[12px] p-[16px] shadow-sm">
        <div className="grid grid-cols-4 gap-y-[18px]">
          {[
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <rect x="4" y="6" width="18" height="14" rx="2" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <path d="M4 10H22" stroke="#1E6FFF" strokeWidth="1.5"/>
                  <path d="M8 14H14" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
              ),
              label: '我的订单',
              onClick: () => navigate('/orders')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <rect x="3" y="7" width="20" height="13" rx="2" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <circle cx="18" cy="13.5" r="1.5" fill="#1E6FFF"/>
                  <path d="M3 10H23" stroke="#1E6FFF" strokeWidth="1.5"/>
                </svg>
              ),
              label: '钱包',
              onClick: () => navigate('/wallet')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <path d="M13 3L15.5 8.5L21.5 9.3L17.2 13.5L18.3 19.5L13 16.7L7.7 19.5L8.8 13.5L4.5 9.3L10.5 8.5L13 3Z" stroke="#1E6FFF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              label: '会员中心',
              onClick: () => navigate('/membership/center')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <circle cx="8" cy="8" r="3" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <circle cx="18" cy="8" r="3" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <circle cx="13" cy="18" r="3" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <path d="M10 10L11.5 15.5M16 10L14.5 15.5" stroke="#1E6FFF" strokeWidth="1.5"/>
                </svg>
              ),
              label: '分享资产',
              onClick: () => navigate('/share-assets')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <path d="M4 13C4 8 8 4 13 4C18 4 22 8 22 13C22 18 18 22 13 22" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <path d="M13 8V13L16 15" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                  <path d="M4 13L7 16L4 19" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
                </svg>
              ),
              label: '托管收益',
              onClick: () => navigate('/trust-income')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <circle cx="13" cy="13" r="9" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <circle cx="13" cy="13" r="2" fill="#1E6FFF"/>
                  <path d="M13 4V8M13 18V22M4 13H8M18 13H22" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
              ),
              label: '分销中心',
              onClick: () => navigate('/distribution')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <circle cx="9" cy="9" r="3.5" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <path d="M3 21C3 17.5 5.5 15 9 15C12.5 15 15 17.5 15 21" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                  <path d="M18 9V15M15 12H21" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
              ),
              label: '邀请好友',
              onClick: () => navigate('/invite')
            },
            {
              icon: (
                <svg className="w-[26px] h-[26px]" viewBox="0 0 26 26" fill="none">
                  <path d="M5 11C5 7 8.5 4 13 4C17.5 4 21 7 21 11V16C21 17 20 18 19 18H17V12H21" stroke="#1E6FFF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <path d="M5 12H9V18H7C6 18 5 17 5 16V12Z" stroke="#1E6FFF" strokeWidth="1.5" fill="none"/>
                  <path d="M17 18V19C17 20 16 21 15 21H12" stroke="#1E6FFF" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
              ),
              label: '客服与设置',
              onClick: () => navigate('/support')
            },
          ].map((item, index) => (
            <button
              key={index}
              onClick={item.onClick}
              className="flex flex-col items-center gap-[6px] active:scale-95 transition-transform"
            >
              <div className="w-[44px] h-[44px] rounded-[12px] bg-[#EEF4FF] flex items-center justify-center">
                {item.icon}
              </div>
              <span className="text-[11px] text-[#333333]">{item.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Assets Overview */}
      <div className="mx-[15px] mt-[12px]">
        <div className="bg-white rounded-[12px] p-[16px] shadow-sm">
          <div className="flex items-center justify-between mb-[14px]">
            <h3 className="text-[14px] font-semibold text-[#1A1A1A]">资产概览</h3>
            <button
              onClick={() => navigate('/assets/flow')}
              className="text-[11px] text-[#999999] flex items-center gap-[2px]"
            >
              流水入口
              <ChevronRight className="w-[10px] h-[10px]" />
            </button>
          </div>
          <div className="grid grid-cols-4">
            {[
              { value: '5,680', label: '资产余额', onClick: () => navigate('/assets/balance') },
              { value: '12,860.50', label: '购物金币', onClick: () => navigate('/assets/coins') },
              { value: '680.00', label: '新手优惠券', onClick: () => navigate('/assets/coupons') },
              { value: '3台', label: '已购买数', onClick: () => navigate('/assets/purchased') },
            ].map((item, index) => (
              <button
                key={index}
                onClick={item.onClick}
                className="flex flex-col items-center active:scale-95 transition-transform"
              >
                <p className="text-[15px] font-semibold text-[#1A1A1A] mb-[4px]">{item.value}</p>
                <p className="text-[10px] text-[#999999]">{item.label}</p>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* My Robots / Assets */}
      <div className="mx-[15px] mt-[12px] mb-[20px]">
        <div className="bg-white rounded-[12px] p-[16px] shadow-sm">
          <div className="flex items-center justify-between mb-[12px]">
            <h3 className="text-[14px] font-semibold text-[#1A1A1A]">我的房产资产</h3>
            <button
              onClick={() => navigate('/robots')}
              className="text-[11px] text-[#999999] flex items-center gap-[2px]"
            >
              <ChevronRight className="w-[10px] h-[10px]" />
            </button>
          </div>
          <div className="grid grid-cols-3 gap-[10px]">
            {[
              { count: '1', label: '空闲', image: 'https://images.unsplash.com/photo-1535378917042-10a22c95931a?w=400', bg: 'from-[#E8F1FF] to-[#F0F5FF]', onClick: () => navigate('/robots/idle') },
              { count: '1', label: '已租', image: 'https://images.unsplash.com/photo-1561557944-6e7860d1a7eb?w=400', bg: 'from-[#FFF7E6] to-[#FFFBE6]', onClick: () => navigate('/robots/rented') },
              { count: '0', label: '维护中', image: 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400', bg: 'from-[#F0FFF4] to-[#F6FFED]', onClick: () => navigate('/robots/maintenance') },
            ].map((item, index) => (
              <button
                key={index}
                onClick={item.onClick}
                className={`bg-gradient-to-br ${item.bg} rounded-[10px] p-[10px] active:scale-95 transition-transform`}
              >
                <div className="flex items-center justify-between mb-[6px]">
                  <p className="text-[11px] text-[#666666]">{item.label}</p>
                  <p className="text-[13px] font-semibold text-[#1A1A1A]">{item.count}台</p>
                </div>
                <div className="w-full aspect-[4/3] rounded-[8px] overflow-hidden">
                  <ImageWithFallback
                    src={item.image}
                    alt={item.label}
                    className="w-full h-full object-cover"
                  />
                </div>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
