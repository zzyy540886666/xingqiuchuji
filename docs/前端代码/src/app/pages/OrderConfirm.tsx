import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  ChevronLeft,
  ChevronRight,
  MoreHorizontal,
  Circle,
  MapPin,
  Shield,
  Award,
  FileText,
  TrendingUp,
} from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

const productImage = "https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdCUyMGRvZ3xlbnwxfHx8fDE3NzkyODY2MDZ8MA&ixlib=rb-4.1.0&q=80&w=1080";

export default function OrderConfirm() {
  const navigate = useNavigate();
  const [selectedDuration, setSelectedDuration] = useState('1个月');
  const [couponUsed, setCouponUsed] = useState(true);

  return (
    <div className="max-w-[480px] mx-auto bg-[#F5F6F8] min-h-screen relative font-sans overflow-x-hidden text-gray-900 shadow-xl pb-[100px]">
      {/* Top Navigation Bar */}
      <div className="sticky top-0 z-50 bg-white border-b border-gray-100">
        {/* Status Bar */}
        <div className="flex justify-between items-center px-6 pt-3 pb-1 text-black font-semibold text-[15px]">
          <span>9:41</span>
          <div className="flex items-center gap-1.5">
            <svg className="w-[18px] h-[18px]" viewBox="0 0 24 24" fill="none">
              <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-5-5 1.41-1.41L11 14.17l7.59-7.59L20 8l-9 9z" fill="currentColor"/>
            </svg>
            <svg className="w-6 h-6" viewBox="0 0 24 24" fill="none">
              <rect x="2" y="7" width="20" height="11" rx="2" stroke="currentColor" strokeWidth="2"/>
              <path d="M22 10h1a1 1 0 011 1v2a1 1 0 01-1 1h-1" stroke="currentColor" strokeWidth="2"/>
            </svg>
          </div>
        </div>
        <div className="flex items-center justify-between px-4 pb-3">
          <ChevronLeft
            onClick={() => navigate(-1)}
            className="w-[22px] h-[22px] text-gray-900 cursor-pointer"
            strokeWidth={2}
          />
          <h1 className="text-[16px] font-bold text-gray-900">确认订单</h1>
          <div className="flex items-center gap-2">
            <MoreHorizontal className="w-[22px] h-[22px] text-gray-900 cursor-pointer" strokeWidth={2} />
            <Circle className="w-[22px] h-[22px] text-gray-900 cursor-pointer" strokeWidth={2} />
          </div>
        </div>
      </div>

      {/* Delivery Address Card */}
      <div className="bg-white mx-3 mt-3 rounded-xl px-3.5 py-3.5 flex items-start gap-2.5 cursor-pointer shadow-sm">
        <MapPin className="w-[18px] h-[18px] text-[#0A4BFE] mt-0.5 shrink-0" strokeWidth={2.5} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2.5 mb-1">
            <span className="text-[14px] font-bold text-gray-900">李明</span>
            <span className="text-[13px] text-gray-600">138****8888</span>
          </div>
          <p className="text-[12px] text-gray-600 leading-[1.6]">
            北京市朝阳区建国路93号万达广场写字楼A座2001室
          </p>
        </div>
        <ChevronRight className="w-[18px] h-[18px] text-gray-400 mt-0.5 shrink-0" strokeWidth={2} />
      </div>

      {/* Promotional Banner */}
      <div className="mx-3 mt-2.5 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg px-3 py-1.5 flex items-center gap-2">
        <div className="flex items-center gap-1.5 flex-1">
          <div className="w-1 h-1 rounded-full bg-[#0A4BFE]"></div>
          <span className="text-[11px] text-[#0A4BFE] font-medium">宇树机器狗</span>
          <div className="w-1 h-1 rounded-full bg-gray-300"></div>
          <span className="text-[11px] text-gray-600">全新优惠</span>
          <div className="w-1 h-1 rounded-full bg-gray-300"></div>
          <span className="text-[11px] text-gray-600">高配优选</span>
        </div>
      </div>

      {/* Product Info Card - Single Container */}
      <div className="bg-white mx-3 mt-2 rounded-xl px-3.5 py-3.5 shadow-sm">
        {/* Product Header */}
        <div className="flex gap-2.5 mb-3">
          <div className="w-[70px] h-[70px] rounded-lg bg-[#F5F6F8] flex items-center justify-center shrink-0 overflow-hidden">
            <ImageWithFallback
              src={productImage}
              className="w-full h-full object-cover"
              alt="Product"
            />
          </div>
          <div className="flex-1 flex flex-col min-w-0">
            <h3 className="text-[13px] font-bold text-gray-900 mb-1 leading-tight">
              Unitree Go2 Pro 机器狗
            </h3>
            <div className="flex items-center gap-1.5 mb-auto">
              <span className="bg-[#E8F3FF] text-[#0A4BFE] text-[10px] px-1.5 py-0.5 rounded font-bold">
                租赁
              </span>
              <span className="text-[10px] text-gray-500">机器狗</span>
            </div>
            <div className="flex items-end justify-between mt-1.5">
              <div className="flex items-baseline gap-0.5">
                <span className="text-[#F53F3F] text-[16px] font-bold leading-none">¥2,980.00</span>
                <span className="text-[11px] text-gray-500">/月</span>
              </div>
              <span className="text-[12px] text-gray-500">x 1</span>
            </div>
          </div>
        </div>

        {/* Duration Selection */}
        <div className="pt-2.5 border-t border-gray-100">
          <div className="text-[12px] text-gray-700 mb-2 font-medium">时长</div>
          <div className="grid grid-cols-4 gap-1.5">
            {['1个月', '2个月', '6个月', '12个月'].map((duration) => (
              <button
                key={duration}
                onClick={() => setSelectedDuration(duration)}
                className={`py-1.5 rounded-md text-[11px] border-2 transition-colors font-medium ${
                  selectedDuration === duration
                    ? 'border-[#0A4BFE] bg-[#F2F6FF] text-[#0A4BFE] font-bold'
                    : 'border-gray-200 bg-white text-gray-600'
                }`}
              >
                {duration}
              </button>
            ))}
          </div>
        </div>

        {/* Start Time */}
        <div className="flex items-center justify-between py-2.5 border-b border-gray-50">
          <span className="text-[12px] text-gray-600">开始时间</span>
          <div className="flex items-center gap-1.5">
            <span className="text-[12px] text-gray-900 font-medium">2025-06-16</span>
          </div>
        </div>

        {/* Pickup Date */}
        <div className="flex items-center justify-between py-2.5 border-b border-gray-50">
          <span className="text-[12px] text-gray-600">起始日期</span>
          <div className="flex items-center gap-1.5">
            <span className="text-[12px] text-gray-900 font-medium">2025-07-15</span>
            <span className="text-[11px] text-gray-400">30天起</span>
            <ChevronRight className="w-[15px] h-[15px] text-gray-400" strokeWidth={2} />
          </div>
        </div>

        {/* Deposit */}
        <div className="flex items-center justify-between py-2.5 border-b border-gray-50">
          <span className="text-[12px] text-gray-600">押金</span>
          <div className="flex items-center gap-1.5">
            <span className="text-[12px] text-[#F53F3F] font-bold">¥5,000.00</span>
          </div>
        </div>

        {/* Delivery Method */}
        <div className="flex items-start justify-between py-2.5 border-b border-gray-50 cursor-pointer">
          <div className="flex items-start gap-2 flex-1">
            <svg className="w-[16px] h-[16px] text-gray-400 mt-0.5 shrink-0" viewBox="0 0 24 24" fill="none">
              <path
                d="M3 8L10.5 12.5L3 17V8Z"
                fill="currentColor"
              />
              <path
                d="M10.5 12.5L21 17V8L10.5 12.5Z"
                stroke="currentColor"
                strokeWidth="2"
                fill="none"
              />
              <path d="M3 17H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <div className="flex-1 min-w-0">
              <div className="text-[12px] text-gray-900 mb-0.5">送货上门</div>
              <div className="text-[11px] text-gray-400 leading-[1.5]">
                含上门安装、送货培训等，7x24小时售后
              </div>
            </div>
          </div>
          <ChevronRight className="w-[15px] h-[15px] text-gray-400 mt-0.5 shrink-0 ml-2" strokeWidth={2} />
        </div>

        {/* Service Items */}
        <div className="flex items-start justify-between py-2.5 cursor-pointer">
          <div className="flex items-start gap-2 flex-1">
            <svg className="w-[16px] h-[16px] text-gray-400 mt-0.5 shrink-0" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2" fill="none"/>
              <path d="M8 3V21M16 3V21M3 8H21M3 16H21" stroke="currentColor" strokeWidth="2"/>
            </svg>
            <div className="flex-1 min-w-0">
              <div className="text-[12px] text-gray-900 mb-0.5">服务项目</div>
              <div className="text-[11px] text-gray-400 leading-[1.5]">
                含发票税、发送相关技术文档等，7x24小时销售
              </div>
            </div>
          </div>
          <ChevronRight className="w-[15px] h-[15px] text-gray-400 mt-0.5 shrink-0 ml-2" strokeWidth={2} />
        </div>
      </div>

      {/* Invoice Info Card */}
      <div className="bg-white mx-3 mt-2 rounded-xl px-3.5 py-2.5 shadow-sm">
        <div className="flex items-center justify-between cursor-pointer">
          <div className="flex items-center gap-2">
            <FileText className="w-[18px] h-[18px] text-gray-400" strokeWidth={2} />
            <span className="text-[12px] text-gray-900 font-medium">发票信息</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] text-gray-400">可选择</span>
            <ChevronRight className="w-[15px] h-[15px] text-gray-400" strokeWidth={2} />
          </div>
        </div>
      </div>

      {/* Price Details Card */}
      <div className="bg-white mx-3 mt-2 rounded-xl px-3.5 py-3 shadow-sm">
        <h3 className="text-[13px] font-bold text-gray-900 mb-2.5">费用信息</h3>
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-[12px] text-gray-600">商品总额</span>
            <span className="text-[12px] text-gray-900">¥2,980.00</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[12px] text-gray-600">服务费</span>
            <span className="text-[12px] text-gray-900">¥100.00</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[12px] text-gray-600">押金</span>
            <span className="text-[12px] text-gray-900">¥4,000.00</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[12px] text-gray-600">运费</span>
            <span className="text-[12px] text-gray-900">¥100.00</span>
          </div>
          <div className="flex items-center justify-between pt-1.5 border-t border-gray-100">
            <div className="flex items-center gap-1.5">
              <span className="text-[12px] text-gray-600">优惠券</span>
              {couponUsed && (
                <span className="text-[9px] text-white bg-[#F53F3F] px-1.5 py-0.5 rounded font-bold">
                  已使用
                </span>
              )}
            </div>
            <div className="flex items-center gap-1.5">
              <span className="text-[12px] text-[#F53F3F] font-bold">-¥300.00</span>
              <ChevronRight className="w-[15px] h-[15px] text-gray-400" strokeWidth={2} />
            </div>
          </div>
        </div>
      </div>

      {/* Order Note */}
      <div className="bg-white mx-3 mt-2 rounded-xl px-3.5 py-2.5 shadow-sm mb-3">
        <div className="flex items-start gap-2.5">
          <span className="text-[12px] text-gray-600 shrink-0">订单备注</span>
          <input
            type="text"
            placeholder="选填，请填写订单备注（50字以内）"
            className="flex-1 text-[12px] text-gray-900 outline-none bg-transparent placeholder:text-gray-400"
            maxLength={50}
          />
        </div>
      </div>

      {/* Bottom Fixed Bar */}
      <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] bg-white border-t border-gray-100 px-3 py-2 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.06)]">
        <div className="flex items-center justify-between mb-1.5">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-0.5 cursor-pointer">
              <Shield className="w-[16px] h-[16px] text-gray-400" strokeWidth={2} />
              <span className="text-[9px] text-gray-500">支付保障</span>
            </div>
            <div className="flex items-center gap-0.5 cursor-pointer">
              <Award className="w-[16px] h-[16px] text-gray-400" strokeWidth={2} />
              <span className="text-[9px] text-gray-500">售后保障</span>
            </div>
            <div className="flex items-center gap-0.5 cursor-pointer">
              <FileText className="w-[16px] h-[16px] text-gray-400" strokeWidth={2} />
              <span className="text-[9px] text-gray-500">服务条款</span>
            </div>
            <div className="flex items-center gap-0.5 cursor-pointer">
              <TrendingUp className="w-[16px] h-[16px] text-gray-400" strokeWidth={2} />
              <span className="text-[9px] text-gray-500">信用免押</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-baseline gap-0.5">
            <span className="text-[11px] text-gray-600">合计：</span>
            <span className="text-[#F53F3F] text-[20px] font-bold leading-none">¥7,979.00</span>
            <span className="text-[10px] text-gray-400 ml-0.5">包邮</span>
          </div>
          <button className="flex-1 bg-[#09B83E] hover:bg-green-600 transition-colors text-white text-[14px] py-2.5 rounded-full font-bold flex items-center justify-center gap-1.5 shadow-md shadow-green-500/25">
            <svg className="w-[18px] h-[18px]" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8.5 3C6.015 3 4 5.015 4 7.5S6.015 12 8.5 12 13 9.985 13 7.5 10.985 3 8.5 3zm0 7C6.57 10 5 8.43 5 6.5S6.57 3 8.5 3 12 4.57 12 6.5 10.43 10 8.5 10z"/>
              <path d="M15.5 12c-2.485 0-4.5 2.015-4.5 4.5s2.015 4.5 4.5 4.5 4.5-2.015 4.5-4.5-2.015-4.5-4.5-4.5zm0 7c-1.378 0-2.5-1.122-2.5-2.5s1.122-2.5 2.5-2.5 2.5 1.122 2.5 2.5-1.122 2.5-2.5 2.5z"/>
            </svg>
            微信支付
          </button>
        </div>
      </div>
    </div>
  );
}
