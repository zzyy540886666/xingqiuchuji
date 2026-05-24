import React from 'react';
import { Outlet, useNavigate, useLocation } from "react-router";
import { Home, LayoutGrid, MessageSquare, User } from 'lucide-react';

export function Layout() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div className="max-w-[480px] mx-auto bg-[#F5F6F8] min-h-screen relative font-sans overflow-x-hidden text-gray-900 shadow-xl">
      <Outlet />

      {/* Bottom Navigation Bar */}
      <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] bg-white border-t border-gray-100 flex justify-around pt-2.5 pb-6 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.03)] pb-[calc(env(safe-area-inset-bottom)+24px)]">
        <div
          onClick={() => navigate("/")}
          className={`flex flex-col items-center gap-1 cursor-pointer transition-colors ${location.pathname === '/' ? 'text-[#0A4BFE]' : 'text-gray-400 hover:text-gray-600'}`}
        >
          <Home className={`w-6 h-6 ${location.pathname === '/' ? 'fill-current' : ''}`} />
          <span className={`text-[10px] ${location.pathname === '/' ? 'font-bold' : 'font-medium'}`}>首页</span>
        </div>
        <div
          onClick={() => navigate("/category")}
          className={`flex flex-col items-center gap-1 cursor-pointer transition-colors ${location.pathname === '/category' ? 'text-[#0A4BFE]' : 'text-gray-400 hover:text-gray-600'}`}
        >
          <LayoutGrid className={`w-6 h-6 ${location.pathname === '/category' ? 'fill-current' : ''}`} />
          <span className={`text-[10px] ${location.pathname === '/category' ? 'font-bold' : 'font-medium'}`}>分类</span>
        </div>
        <div
          onClick={() => navigate("/community")}
          className={`flex flex-col items-center gap-1 cursor-pointer transition-colors ${location.pathname === '/community' ? 'text-[#0A4BFE]' : 'text-gray-400 hover:text-gray-600'}`}
        >
          <MessageSquare className={`w-6 h-6 ${location.pathname === '/community' ? 'fill-current' : ''}`} />
          <span className={`text-[10px] ${location.pathname === '/community' ? 'font-bold' : 'font-medium'}`}>社区</span>
        </div>
        <div
          onClick={() => navigate("/profile")}
          className={`flex flex-col items-center gap-1 cursor-pointer transition-colors ${location.pathname === '/profile' ? 'text-[#0A4BFE]' : 'text-gray-400 hover:text-gray-600'}`}
        >
          <User className={`w-6 h-6 ${location.pathname === '/profile' ? 'fill-current' : ''}`} />
          <span className={`text-[10px] ${location.pathname === '/profile' ? 'font-bold' : 'font-medium'}`}>我的</span>
        </div>
      </div>
    </div>
  );
}
