import React from 'react';
import { usePos } from '../context/PosContext';
import { Bell, Wallet, Printer, Store, User, Wifi } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, activeScreen, setActiveScreen, notifications, networks } = usePos();
  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <header className="sticky top-0 z-40 bg-slate-900/95 backdrop-blur-md border-b border-slate-800 text-white px-4 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        
        {/* Brand & Store Name */}
        <div className="flex items-center gap-3 cursor-pointer" onClick={() => setActiveScreen('home')}>
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-700 flex items-center justify-center shadow-lg shadow-blue-500/20 text-white font-black text-lg border border-blue-400/30">
            CB
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-base bg-gradient-to-r from-blue-400 via-sky-300 to-emerald-400 bg-clip-text text-transparent">
                CardBox POS
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-semibold">
                متصل
              </span>
            </div>
            <p className="text-xs text-slate-400 truncate max-w-[180px] sm:max-w-xs">
              {user.storeName}
            </p>
          </div>
        </div>

        {/* Action Badges & Short Nav */}
        <div className="flex items-center gap-2">
          
          {/* Wallet Balance Badge */}
          <button 
            onClick={() => setActiveScreen('wallet')}
            className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-750 border border-slate-700/80 transition-all text-xs font-semibold"
            title="رصيد المحفظة"
          >
            <Wallet className="w-4 h-4 text-emerald-400" />
            <span className="text-slate-300 hidden sm:inline">المحفظة:</span>
            <span className="text-emerald-400 font-mono font-bold">
              {user.walletBalance.toLocaleString()} <span className="text-[10px]">ر.ي</span>
            </span>
          </button>

          {/* Notifications Button */}
          <button 
            onClick={() => setActiveScreen('notifications')}
            className="relative p-2 rounded-xl bg-slate-800/80 hover:bg-slate-700 border border-slate-700/80 text-slate-300 transition-colors"
            title="الإشعارات"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-rose-500 text-[10px] text-white font-bold flex items-center justify-center animate-pulse">
                {unreadCount}
              </span>
            )}
          </button>

          {/* Printer Quick Button */}
          <button 
            onClick={() => setActiveScreen('printer')}
            className={`p-2 rounded-xl border transition-colors ${
              activeScreen === 'printer' 
                ? 'bg-blue-600/30 border-blue-500 text-blue-400' 
                : 'bg-slate-800/80 hover:bg-slate-700 border-slate-700/80 text-slate-400'
            }`}
            title="إعدادات الطابعة الحرارية"
          >
            <Printer className="w-4 h-4" />
          </button>

          {/* Profile Quick Button */}
          <button 
            onClick={() => setActiveScreen('profile')}
            className={`p-2 rounded-xl border transition-colors ${
              activeScreen === 'profile' 
                ? 'bg-blue-600/30 border-blue-500 text-blue-400' 
                : 'bg-slate-800/80 hover:bg-slate-700 border-slate-700/80 text-slate-400'
            }`}
            title="الملف الشخصي والحساب"
          >
            <User className="w-4 h-4" />
          </button>

        </div>

      </div>
    </header>
  );
};
