import React from 'react';
import { useNavigate } from 'react-router';
import { ChevronLeft, ChevronRight, Copy } from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

export default function MembershipCenter() {
  const navigate = useNavigate();

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="w-full min-h-screen bg-[#F4F8FF] pb-[40px] relative">
      {/* Deep Blue Space Background Header */}
      <div className="absolute top-0 left-0 right-0 h-[460px] bg-gradient-to-b from-[#061B3F] via-[#08285A] to-[#0A2F6C] overflow-hidden">
        {/* Stars Effect */}
        <div className="absolute inset-0">
          {[...Array(30)].map((_, i) => (
            <div
              key={i}
              className="absolute w-[2px] h-[2px] bg-white rounded-full opacity-60"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
                animation: `twinkle ${2 + Math.random() * 3}s infinite`,
              }}
            />
          ))}
        </div>

        {/* Planet Card Image - Tilted */}
        <div className="absolute right-[-20px] top-[80px] w-[200px] h-[120px] opacity-90 transform rotate-[-12deg]">
          <div className="w-full h-full bg-gradient-to-br from-[#2F8CFF]/30 via-[#1677FF]/20 to-transparent rounded-[16px] border border-[#2F8CFF]/40 backdrop-blur-sm relative overflow-hidden">
            {/* Planet Arc */}
            <svg className="absolute right-[-30px] top-[-20px] w-[100px] h-[100px]" viewBox="0 0 100 100" fill="none">
              <circle cx="50" cy="50" r="35" fill="url(#planetGradient)" opacity="0.8"/>
              <circle cx="50" cy="50" r="40" stroke="#2F8CFF" strokeWidth="1" fill="none" opacity="0.3"/>
              <defs>
                <radialGradient id="planetGradient">
                  <stop offset="0%" stopColor="#5A9BFF"/>
                  <stop offset="100%" stopColor="#1677FF"/>
                </radialGradient>
              </defs>
            </svg>
            {/* Glow Effect */}
            <div className="absolute top-[10px] right-[20px] w-[60px] h-[60px] bg-[#2F8CFF] rounded-full blur-[30px] opacity-40" />
          </div>
        </div>

        {/* Blue Light Glow */}
        <div className="absolute top-[100px] right-[40px] w-[150px] h-[150px] bg-[#1677FF] rounded-full blur-[80px] opacity-20" />
      </div>

      <div className="relative z-10">
        {/* Status Bar */}
        <div className="h-[44px] flex items-center justify-between px-[15px] pt-[8px]">
          <div className="text-[14px] font-semibold text-white">9:43</div>
          <div className="flex items-center gap-[6px]">
            {/* Signal Icon */}
            <svg className="w-[17px] h-[11px]" viewBox="0 0 17 11" fill="none">
              <rect x="0.5" y="7" width="3" height="4" rx="0.5" fill="white"/>
              <rect x="4.5" y="5" width="3" height="6" rx="0.5" fill="white"/>
              <rect x="8.5" y="3" width="3" height="8" rx="0.5" fill="white"/>
              <rect x="12.5" y="1" width="3" height="10" rx="0.5" fill="white"/>
            </svg>
            {/* WiFi Icon */}
            <svg className="w-[15px] h-[11px]" viewBox="0 0 15 11" fill="none">
              <path d="M7.5 2C10 2 12 3.5 12 5.5L13.5 4C13 1.5 10.5 0 7.5 0C4.5 0 2 1.5 1.5 4L3 5.5C3 3.5 5 2 7.5 2Z" fill="white"/>
              <circle cx="7.5" cy="9" r="1.5" fill="white"/>
            </svg>
            {/* Battery Icon */}
            <svg className="w-[25px] h-[12px]" viewBox="0 0 25 12" fill="none">
              <rect x="0.5" y="0.5" width="21" height="11" rx="2.5" stroke="white" strokeWidth="1" fill="none"/>
              <rect x="2" y="2" width="18" height="8" rx="1" fill="white"/>
              <rect x="23" y="4" width="2" height="4" rx="1" fill="white"/>
            </svg>
          </div>
        </div>

        {/* Navigation Bar */}
        <div className="h-[44px] flex items-center justify-between px-[15px]">
          <button
            onClick={() => navigate(-1)}
            className="w-[32px] h-[32px] rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
          >
            <ChevronLeft className="w-[20px] h-[20px] text-white" strokeWidth={2} />
          </button>
          <h1 className="text-[17px] font-semibold text-white">会员中心</h1>
          <div className="flex items-center gap-[8px]">
            <button
              onClick={() => navigate('/menu')}
              className="w-[32px] h-[32px] rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              <svg className="w-[16px] h-[16px]" viewBox="0 0 16 16" fill="none">
                <circle cx="3" cy="3" r="1.5" fill="white"/>
                <circle cx="8" cy="3" r="1.5" fill="white"/>
                <circle cx="13" cy="3" r="1.5" fill="white"/>
                <circle cx="3" cy="8" r="1.5" fill="white"/>
                <circle cx="8" cy="8" r="1.5" fill="white"/>
                <circle cx="13" cy="8" r="1.5" fill="white"/>
                <circle cx="3" cy="13" r="1.5" fill="white"/>
                <circle cx="8" cy="13" r="1.5" fill="white"/>
                <circle cx="13" cy="13" r="1.5" fill="white"/>
              </svg>
            </button>
            <button
              onClick={() => navigate('/settings')}
              className="w-[32px] h-[32px] rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              <svg className="w-[16px] h-[16px]" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="8" fill="white"/>
              </svg>
            </button>
          </div>
        </div>

        {/* User Info Section */}
        <div className="px-[20px] mt-[16px] flex items-center gap-[12px]">
          <div className="w-[56px] h-[56px] rounded-full overflow-hidden bg-white/20 border-[3px] border-white flex-shrink-0">
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
              alt="avatar"
              className="w-full h-full object-cover"
            />
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-[6px] mb-[4px]">
              <h2 className="text-[16px] font-bold text-white">星球玩家小白</h2>
              <div className="flex items-center gap-[3px] px-[8px] py-[3px] bg-[#08285A]/60 border border-[#2F8CFF]/40 rounded-[12px] backdrop-blur-sm">
                <svg className="w-[10px] h-[10px]" viewBox="0 0 10 10" fill="none">
                  <path d="M5 0.5L6.2 3.5L9.5 3.8L7 5.9L7.7 9L5 7.5L2.3 9L3 5.9L0.5 3.8L3.8 3.5L5 0.5Z" fill="#2F8CFF"/>
                </svg>
                <span className="text-[11px] font-semibold text-[#5A9BFF]">Lv.1 原住民</span>
              </div>
            </div>
            <div className="flex items-center gap-[6px]">
              <p className="text-[12px] text-white/80">ID: XQWJXB20250001</p>
              <button
                onClick={() => copyToClipboard('XQWJXB20250001')}
                className="active:scale-95 transition-transform"
              >
                <Copy className="w-[12px] h-[12px] text-white/70" strokeWidth={2} />
              </button>
            </div>
          </div>
        </div>

        {/* Level Growth Card - 居民信息卡 */}
        <div className="mx-[15px] mt-[20px] bg-[#0B2855] backdrop-blur-md rounded-[14px] p-[14px] border border-[#1E4A7A] shadow-lg">
          {/* Top Row: Level and Progress */}
          <div className="flex items-center justify-between mb-[8px]">
            <div className="flex items-center gap-[6px]">
              <span className="text-[16px] font-bold text-white">Lv.1</span>
              <span className="text-[13px] text-white/90">原住民</span>
            </div>
            <span className="text-[13px] text-white/80">320/1000</span>
          </div>

          {/* Progress Text and Button */}
          <div className="flex items-center justify-between mb-[14px]">
            <p className="text-[12px] text-white/75">再获得680经验升级 Lv.2 探索者</p>
            <button
              onClick={() => navigate('/growth-center')}
              className="px-[12px] py-[5px] border border-white/50 rounded-full text-[11px] text-white active:scale-95 transition-transform whitespace-nowrap"
            >
              成长中心 &gt;
            </button>
          </div>

          {/* Level Timeline */}
          <div className="relative mb-[12px]">
            <div className="flex items-center justify-between relative px-[8px]">
              {/* Timeline Line */}
              <div className="absolute left-[8px] right-[8px] top-[8px] h-[2px] bg-[#1E4A7A]" />

              {/* Level Nodes */}
              {[
                { level: 'Lv.0', name: '观察者', active: false },
                { level: 'Lv.1', name: '原住民', active: true },
                { level: 'Lv.2', name: '探索者', active: false },
                { level: 'Lv.3', name: '开拓者', active: false },
              ].map((node, index) => (
                <button
                  key={index}
                  onClick={() => navigate(`/level/${index}`)}
                  className="flex flex-col items-center relative z-10 active:scale-95 transition-transform"
                >
                  {node.active ? (
                    <div className="relative mb-[8px]">
                      <div className="absolute inset-0 bg-gradient-to-br from-[#5A9BFF] to-[#1677FF] rounded-full blur-[8px] opacity-60" />
                      <div className="relative w-[36px] h-[18px] bg-gradient-to-br from-[#5A9BFF] to-[#1677FF] rounded-full flex items-center justify-center border-[1.5px] border-[#7DB3FF]">
                        <span className="text-[10px] font-bold text-white">Lv.1</span>
                      </div>
                    </div>
                  ) : (
                    <div className="w-[16px] h-[16px] rounded-full border-[2px] border-[#2F5A7A] bg-[#0B2855] mb-[8px]" />
                  )}
                  <span className={`text-[9px] ${node.active ? 'text-white font-medium' : 'text-white/40'} whitespace-nowrap`}>
                    {node.name}
                  </span>
                </button>
              ))}

              {/* Star Partner Node */}
              <button
                onClick={() => navigate('/partner')}
                className="flex flex-col items-center relative z-10 active:scale-95 transition-transform"
              >
                <div className="mb-[8px]">
                  <svg className="w-[18px] h-[18px]" viewBox="0 0 18 18" fill="none">
                    <path d="M9 1L11 6.5L17 7.3L13 11.5L14 17L9 14L4 17L5 11.5L1 7.3L7 6.5L9 1Z" stroke="#2F5A7A" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  </svg>
                </div>
                <span className="text-[9px] text-white/40 whitespace-nowrap">星球合伙人</span>
              </button>
            </div>
          </div>

          {/* Upgrade Conditions */}
          <div className="flex items-center justify-between pt-[10px] border-t border-[#1E4A7A]">
            <div className="flex items-center gap-[6px] flex-1 min-w-0">
              <svg className="w-[13px] h-[13px] flex-shrink-0" viewBox="0 0 13 13" fill="none">
                <circle cx="6.5" cy="6.5" r="5.5" stroke="#5A9BFF" strokeWidth="1.2" fill="none"/>
                <path d="M6.5 4V6.5L8.5 8.5" stroke="#5A9BFF" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <p className="text-[11px] text-white/70">升级条件：累计消费满 ¥10,000 或开通年费星球卡</p>
            </div>
            <button
              onClick={() => navigate('/level-rules')}
              className="text-[11px] text-[#5A9BFF] whitespace-nowrap active:scale-95 transition-transform ml-[8px]"
            >
              查看等级规则 &gt;
            </button>
          </div>
        </div>
      </div>

      {/* Member Benefits Section */}
      <div className="mx-[15px] mt-[16px] bg-white rounded-[16px] p-[16px] shadow-sm relative z-10">
        <h3 className="text-[16px] font-bold text-[#1A1A1A] mb-[16px]">会员权益</h3>

        <div className="grid grid-cols-4 gap-y-[20px]">
          {[
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L4 5V11C4 16 12 21 12 21C12 21 20 16 20 11V5L12 2Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              value: '2 次',
              label: '剩余免押次数',
              onClick: () => navigate('/benefits/deposit')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M14.7 6.3C15.1 5.9 15.1 5.3 14.7 4.9C14.3 4.5 13.7 4.5 13.3 4.9L9 9.2L7.7 7.9C7.3 7.5 6.7 7.5 6.3 7.9C5.9 8.3 5.9 8.9 6.3 9.3L8.3 11.3C8.7 11.7 9.3 11.7 9.7 11.3L14.7 6.3Z M21 6L15 21L9 15L3 18V3L9 6L15 3L21 6Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              value: '1 次',
              label: '剩余维保次数',
              onClick: () => navigate('/benefits/maintenance')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M21 8H18L15 3H9L6 8H3C2 8 1 9 1 10V19C1 20 2 21 3 21H21C22 21 23 20 23 19V10C23 9 22 8 21 8Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <path d="M12 17C14 17 16 15 16 13C16 11 14 9 12 9C10 9 8 11 8 13C8 15 10 17 12 17Z" stroke="#1677FF" strokeWidth="1.5" fill="none"/>
                </svg>
              ),
              value: '9.5 折',
              label: '配件专属折扣',
              onClick: () => navigate('/benefits/discount')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <rect x="3" y="3" width="7" height="7" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <rect x="3" y="14" width="7" height="7" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <rect x="14" y="3" width="7" height="7" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <rect x="14" y="14" width="7" height="7" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              value: '2 级',
              label: '分润层级',
              onClick: () => navigate('/benefits/level')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="9" stroke="#1677FF" strokeWidth="1.5" fill="none"/>
                  <path d="M12 6V12L16 14" stroke="#1677FF" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              ),
              value: '5,680',
              label: '光年币余额 >',
              onClick: () => navigate('/coins'),
              valueSize: 'normal'
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M20 12C20 16.4 16.4 20 12 20C10.4 20 9 19.5 7.8 18.7L4 20L5.3 16.2C4.5 15 4 13.6 4 12C4 7.6 7.6 4 12 4C16.4 4 20 7.6 20 12Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <circle cx="9" cy="12" r="1" fill="#1677FF"/>
                  <circle cx="12" cy="12" r="1" fill="#1677FF"/>
                  <circle cx="15" cy="12" r="1" fill="#1677FF"/>
                </svg>
              ),
              value: '新物种体验',
              label: '优先体验资格',
              onClick: () => navigate('/benefits/experience'),
              valueSize: 'small'
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M5 11C5 7 8.5 4 13 4C17.5 4 21 7 21 11V16C21 17 20 18 19 18H17V12H21M5 12H9V18H7C6 18 5 17 5 16V12Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              value: '专属客服',
              label: '7×12小时服务',
              onClick: () => navigate('/benefits/service'),
              valueSize: 'small'
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L4 5V11C4 16 12 21 12 21C12 21 20 16 20 11V5L12 2Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                  <path d="M9 12L11 14L15 10" stroke="#1677FF" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              ),
              value: '活动特权',
              label: '专属活动参与权',
              onClick: () => navigate('/benefits/activity'),
              valueSize: 'small'
            },
          ].map((item, index) => (
            <button
              key={index}
              onClick={item.onClick}
              className="flex flex-col items-center gap-[6px] active:scale-95 transition-transform"
            >
              {item.icon}
              <p className={`${item.valueSize === 'small' ? 'text-[11px]' : 'text-[14px]'} font-semibold text-[#1A1A1A]`}>
                {item.value}
              </p>
              <p className="text-[10px] text-[#999999] text-center leading-[14px]">{item.label}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Planet Cards Section */}
      <div className="mx-[15px] mt-[16px] bg-white rounded-[16px] p-[16px] shadow-sm relative z-10">
        <div className="flex items-center justify-between mb-[16px]">
          <div className="flex items-center gap-[8px]">
            <h3 className="text-[16px] font-bold text-[#1A1A1A]">星球卡</h3>
            <span className="text-[11px] text-[#999999]">选择适合你的会员卡</span>
          </div>
          <button
            onClick={() => navigate('/membership/compare')}
            className="text-[11px] text-[#1677FF] flex items-center gap-[2px] active:scale-95 transition-transform"
          >
            对比权益
            <ChevronRight className="w-[10px] h-[10px]" strokeWidth={2} />
          </button>
        </div>

        <div className="grid grid-cols-3 gap-[8px]">
          {/* Monthly Card */}
          <button
            onClick={() => navigate('/membership/purchase?plan=monthly')}
            className="w-full active:scale-[0.98] transition-transform"
          >
            <div className="bg-gradient-to-b from-[#08285A] to-[#0A2F6C] rounded-[12px] overflow-hidden relative">
              {/* Top Section with Space Background */}
              <div className="relative h-[70px] overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-br from-[#2F8CFF]/20 to-transparent" />
                {[...Array(10)].map((_, i) => (
                  <div
                    key={i}
                    className="absolute w-[1px] h-[1px] bg-white rounded-full opacity-50"
                    style={{
                      left: `${Math.random() * 100}%`,
                      top: `${Math.random() * 100}%`,
                    }}
                  />
                ))}
                <div className="absolute top-[10px] left-[10px]">
                  <h4 className="text-[13px] font-bold text-white mb-[1px]">月度星球卡</h4>
                  <p className="text-[10px] text-white/70">30天有效期</p>
                </div>
                <div className="absolute top-[10px] right-[10px] px-[6px] py-[2px] bg-[#1677FF] rounded-[8px]">
                  <span className="text-[9px] font-semibold text-white">推荐</span>
                </div>
              </div>

              {/* Bottom Section with White Background */}
              <div className="bg-white relative">
                {/* Wave Divider */}
                <svg className="w-full h-[10px]" preserveAspectRatio="none" viewBox="0 0 200 10" fill="none">
                  <path d="M0 5 Q50 0 100 5 T200 5 V0 H0 V5Z" fill="#0A2F6C"/>
                </svg>

                <div className="px-[10px] pb-[10px]">
                  {/* Benefits */}
                  <div className="space-y-[4px] mb-[8px]">
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">免押次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">2次</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">维保次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">1次</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">配件折扣</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">9.5折</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">分润层级</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">2级</span>
                    </div>
                  </div>

                  {/* Price */}
                  <div className="mb-[8px]">
                    <div className="flex items-baseline gap-[2px]">
                      <span className="text-[10px] text-[#FF6B00]">¥</span>
                      <span className="text-[22px] font-bold text-[#FF6B00]">99</span>
                    </div>
                  </div>

                  {/* Button */}
                  <div className="w-full py-[8px] bg-gradient-to-r from-[#1677FF] to-[#2F8CFF] rounded-[16px] text-[12px] font-semibold text-white shadow-md text-center">
                    立即开通
                  </div>
                </div>
              </div>
            </div>
          </button>

          {/* Yearly Card */}
          <button
            onClick={() => navigate('/membership/purchase?plan=yearly')}
            className="w-full active:scale-[0.98] transition-transform"
          >
            <div className="bg-gradient-to-b from-[#8B6914] to-[#A67C1A] rounded-[12px] overflow-hidden relative">
              {/* Top Section with Gold Space Background */}
              <div className="relative h-[70px] overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-br from-[#DFA83A]/30 to-transparent" />
                {[...Array(10)].map((_, i) => (
                  <div
                    key={i}
                    className="absolute w-[1px] h-[1px] bg-[#FFE4A0] rounded-full opacity-70"
                    style={{
                      left: `${Math.random() * 100}%`,
                      top: `${Math.random() * 100}%`,
                    }}
                  />
                ))}
                <div className="absolute top-[10px] left-[10px]">
                  <h4 className="text-[13px] font-bold text-white mb-[1px]">年度星球卡</h4>
                  <p className="text-[10px] text-white/80">365天有效期</p>
                </div>
                <div className="absolute top-[10px] right-[10px] px-[6px] py-[2px] bg-[#FFB84D] rounded-[8px]">
                  <span className="text-[9px] font-semibold text-[#8B6914]">超值</span>
                </div>
              </div>

              {/* Bottom Section with White Background */}
              <div className="bg-white relative">
                {/* Wave Divider */}
                <svg className="w-full h-[10px]" preserveAspectRatio="none" viewBox="0 0 200 10" fill="none">
                  <path d="M0 5 Q50 0 100 5 T200 5 V0 H0 V5Z" fill="#A67C1A"/>
                </svg>

                <div className="px-[10px] pb-[10px]">
                  {/* Benefits */}
                  <div className="space-y-[4px] mb-[8px]">
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">免押次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">12次</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">维保次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">6次</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">配件折扣</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">9折</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">分润层级</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">2级</span>
                    </div>
                  </div>

                  {/* Price */}
                  <div className="mb-[8px]">
                    <div className="flex items-baseline gap-[2px]">
                      <span className="text-[10px] text-[#FF6B00]">¥</span>
                      <span className="text-[22px] font-bold text-[#FF6B00]">899</span>
                    </div>
                    <div className="text-[9px] text-[#999999] line-through">¥1299</div>
                  </div>

                  {/* Button */}
                  <div className="w-full py-[8px] bg-gradient-to-r from-[#DFA83A] to-[#F5C76A] rounded-[16px] text-[12px] font-semibold text-white shadow-md text-center">
                    立即开通
                  </div>
                </div>
              </div>
            </div>
          </button>

          {/* Lifetime Card */}
          <button
            onClick={() => navigate('/membership/purchase?plan=lifetime')}
            className="w-full active:scale-[0.98] transition-transform"
          >
            <div className="bg-gradient-to-b from-[#08285A] to-[#0A2F6C] rounded-[12px] overflow-hidden relative">
              {/* Top Section with Space Station Background */}
              <div className="relative h-[70px] overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-br from-[#5A9BFF]/20 to-transparent" />
                {[...Array(10)].map((_, i) => (
                  <div
                    key={i}
                    className="absolute w-[1px] h-[1px] bg-white rounded-full opacity-50"
                    style={{
                      left: `${Math.random() * 100}%`,
                      top: `${Math.random() * 100}%`,
                    }}
                  />
                ))}
                {/* Space Station Icon */}
                <svg className="absolute top-[20px] right-[15px] w-[30px] h-[30px] opacity-30" viewBox="0 0 30 30" fill="none">
                  <circle cx="15" cy="15" r="5" fill="white"/>
                  <rect x="6" y="14" width="18" height="2" fill="white"/>
                  <rect x="14" y="6" width="2" height="18" fill="white"/>
                </svg>
                <div className="absolute top-[10px] left-[10px]">
                  <h4 className="text-[13px] font-bold text-white mb-[1px]">终身星球卡</h4>
                  <p className="text-[10px] text-white/70">永久有效</p>
                </div>
                <div className="absolute top-[10px] right-[10px] px-[6px] py-[2px] bg-[#5A9BFF]/80 backdrop-blur-sm rounded-[8px]">
                  <span className="text-[9px] font-semibold text-white">尊享</span>
                </div>
              </div>

              {/* Bottom Section with White Background */}
              <div className="bg-white relative">
                {/* Wave Divider */}
                <svg className="w-full h-[10px]" preserveAspectRatio="none" viewBox="0 0 200 10" fill="none">
                  <path d="M0 5 Q50 0 100 5 T200 5 V0 H0 V5Z" fill="#0A2F6C"/>
                </svg>

                <div className="px-[10px] pb-[10px]">
                  {/* Benefits */}
                  <div className="space-y-[4px] mb-[8px]">
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">免押次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">不限</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">维保次数</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">不限</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">配件折扣</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">8.5折</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[9px] text-[#666666]">分润层级</span>
                      <span className="text-[10px] font-semibold text-[#1A1A1A]">3级</span>
                    </div>
                  </div>

                  {/* Price */}
                  <div className="mb-[8px]">
                    <div className="flex items-baseline gap-[2px]">
                      <span className="text-[10px] text-[#FF6B00]">¥</span>
                      <span className="text-[22px] font-bold text-[#FF6B00]">2999</span>
                    </div>
                  </div>

                  {/* Button */}
                  <div className="w-full py-[8px] bg-gradient-to-r from-[#1677FF] to-[#2F8CFF] rounded-[16px] text-[12px] font-semibold text-white shadow-md text-center">
                    立即开通
                  </div>
                </div>
              </div>
            </div>
          </button>
        </div>
      </div>

      {/* Rights Description Section */}
      <div className="mx-[15px] mt-[16px] bg-white rounded-[16px] p-[16px] shadow-sm relative z-10">
        <div className="flex items-center justify-between mb-[16px]">
          <h3 className="text-[16px] font-bold text-[#1A1A1A]">权益说明</h3>
          <button
            onClick={() => navigate('/membership/rights')}
            className="text-[11px] text-[#1677FF] flex items-center gap-[2px] active:scale-95 transition-transform"
          >
            更多说明
            <ChevronRight className="w-[10px] h-[10px]" strokeWidth={2} />
          </button>
        </div>

        <div className="flex gap-[12px]">
          {[
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L15.5 8.5L22 9.3L17 14.5L18.3 21L12 17.7L5.7 21L7 14.5L2 9.3L8.5 8.5L12 2Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              title: '免押特权',
              desc: '租赁免押，轻松体验',
              onClick: () => navigate('/rights/deposit')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M14.7 6.3C15.1 5.9 15.1 5.3 14.7 4.9C14.3 4.5 13.7 4.5 13.3 4.9L9 9.2L7.7 7.9C7.3 7.5 6.7 7.5 6.3 7.9C5.9 8.3 5.9 8.9 6.3 9.3L8.3 11.3C8.7 11.7 9.3 11.7 9.7 11.3L14.7 6.3Z M21 6L15 21L9 15L3 18V3L9 6L15 3L21 6Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              title: '维保特权',
              desc: '免费维保，省心省力',
              onClick: () => navigate('/rights/maintenance')
            },
            {
              icon: (
                <svg className="w-[24px] h-[24px]" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L4 5V11C4 16 12 21 12 21C12 21 20 16 20 11V5L12 2Z" stroke="#1677FF" strokeWidth="1.5" fill="none" strokeLinejoin="round"/>
                </svg>
              ),
              title: '折扣特权',
              desc: '配件优惠，持续省钱',
              onClick: () => navigate('/rights/discount')
            },
          ].map((item, index) => (
            <React.Fragment key={index}>
              <button
                onClick={item.onClick}
                className="flex-1 flex flex-col items-center active:scale-95 transition-transform"
              >
                <div className="w-[48px] h-[48px] rounded-full bg-[#EEF4FF] flex items-center justify-center mb-[8px]">
                  {item.icon}
                </div>
                <h4 className="text-[13px] font-semibold text-[#1A1A1A] mb-[4px]">{item.title}</h4>
                <p className="text-[11px] text-[#999999] text-center">{item.desc}</p>
              </button>
              {index < 2 && <div className="w-[1px] bg-[#F0F0F0]" />}
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* FAQ Section */}
      <div className="mx-[15px] mt-[16px] mb-[16px] bg-white rounded-[16px] p-[16px] shadow-sm relative z-10">
        <div className="flex items-center justify-between mb-[14px]">
          <div className="flex items-center gap-[6px]">
            <h3 className="text-[16px] font-bold text-[#1A1A1A]">常见问题</h3>
            <svg className="w-[14px] h-[14px]" viewBox="0 0 14 14" fill="none">
              <circle cx="7" cy="7" r="6" stroke="#999999" strokeWidth="1.2" fill="none"/>
              <path d="M7 10V10.5M7 4C5.9 4 5 4.9 5 6H6.5C6.5 5.7 6.7 5.5 7 5.5C7.3 5.5 7.5 5.7 7.5 6C7.5 6.3 7.3 6.5 7 6.5C6.7 6.5 6.5 6.7 6.5 7V8H7.5V7.2C8.4 7 9 6.2 9 6C9 4.9 8.1 4 7 4Z" fill="#999999"/>
            </svg>
          </div>
          <button
            onClick={() => navigate('/faq')}
            className="text-[11px] text-[#1677FF] flex items-center gap-[2px] active:scale-95 transition-transform"
          >
            查看全部
            <ChevronRight className="w-[10px] h-[10px]" strokeWidth={2} />
          </button>
        </div>

        <div className="space-y-[12px]">
          {[
            { question: '星球卡如何使用？', id: 1 },
            { question: '免押次数如何计算？', id: 2 },
            { question: '升级规则是什么？', id: 3 },
          ].map((item, index) => (
            <button
              key={index}
              onClick={() => navigate(`/faq/${item.id}`)}
              className="w-full text-left flex items-center justify-between py-[10px] border-b border-[#F5F5F5] last:border-0 active:scale-[0.99] transition-transform"
            >
              <span className="text-[13px] text-[#666666]">{item.question}</span>
              <ChevronRight className="w-[12px] h-[12px] text-[#CCCCCC] flex-shrink-0" strokeWidth={2} />
            </button>
          ))}
        </div>
      </div>

      <style>{`
        @keyframes twinkle {
          0%, 100% { opacity: 0.3; }
          50% { opacity: 1; }
        }
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
        .scrollbar-hide {
          -ms-overflow-style: none;
          scrollbar-width: none;
        }
      `}</style>
    </div>
  );
}
