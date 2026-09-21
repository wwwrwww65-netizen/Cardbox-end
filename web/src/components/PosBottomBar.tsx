import React from 'react';
import { usePos } from '../context/PosContext';
import { Home, Wifi, History, Wallet, User, PlusCircle } from 'lucide-react';

export const PosBottomBar: React.FC = () => {
  const { activeScreen, setActiveScreen } = usePos();

  const navItems = [
    { id: 'home', label: 'الرئيسية', icon: Home },
    { id: 'networks', label: 'الشبكات', icon: Wifi },
    { id: 'sales', label: 'المبيعات', icon: History },
    { id: 'wallet', label: 'المحفظة', icon: Wallet },
    { id: 'profile', label: 'حسابي', icon: User },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-slate-900/95 backdrop-blur-lg border-t border-slate-800/80 px-2 py-2">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeScreen === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveScreen(item.id)}
              className={`flex flex-col items-center justify-center flex-1 py-1 px-2 rounded-xl transition-all ${
                isActive
                  ? 'text-blue-400 font-bold'
                  : 'text-slate-400 hover:text-slate-200 font-medium'
              }`}
            >
              <div className={`p-1.5 rounded-xl transition-all ${isActive ? 'bg-blue-600/20 text-blue-400 scale-110' : ''}`}>
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] mt-0.5">{item.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
