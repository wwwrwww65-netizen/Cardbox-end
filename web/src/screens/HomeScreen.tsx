import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  Wifi, 
  Wallet, 
  TrendingUp, 
  History, 
  ArrowUpRight, 
  Printer, 
  PlusCircle, 
  Pin, 
  CheckCircle2, 
  ChevronLeft,
  Sparkles,
  ShoppingBag,
  Clock,
  Radio
} from 'lucide-react';
import { NetworkItem, OrderTransaction } from '../types';

interface HomeScreenProps {
  onSelectReceipt: (order: OrderTransaction) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onSelectReceipt }) => {
  const { 
    user, 
    networks, 
    salesHistory, 
    setActiveScreen, 
    setSelectedNetwork, 
    togglePinNetwork 
  } = usePos();

  const joinedApprovedNetworks = networks.filter(n => n.status === 'APPROVED');
  const pinnedNetworks = joinedApprovedNetworks.filter(n => n.isPinned);
  const otherNetworks = joinedApprovedNetworks.filter(n => !n.isPinned);
  const displayNetworks = [...pinnedNetworks, ...otherNetworks];

  // Calculate statistics
  const todaySales = salesHistory.filter(s => {
    const today = new Date();
    const saleDate = new Date(s.timestamp);
    return today.toDateString() === saleDate.toDateString();
  });

  const totalTodayRevenue = todaySales.reduce((sum, s) => sum + s.totalAmount, 0);
  const totalTodayProfit = todaySales.reduce((sum, s) => sum + (s.totalAmount - s.totalCost), 0);
  const totalTodayVouchers = todaySales.reduce((sum, s) => sum + s.quantity, 0);

  const handleOpenNetwork = (net: NetworkItem) => {
    setSelectedNetwork(net);
    setActiveScreen('packages');
  };

  return (
    <div className="space-y-6 pb-20">
      
      {/* Welcome & Wallet Header Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-blue-900 via-indigo-900 to-slate-900 p-6 border border-blue-500/30 shadow-2xl">
        <div className="absolute top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-blue-500/10 via-transparent to-transparent pointer-events-none" />
        
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2 text-blue-300 text-xs font-semibold">
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>لوحة التحكم المباشرة</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white">
              {user.storeName}
            </h1>
            <p className="text-xs text-slate-300 flex items-center gap-1.5">
              <span>📍 {user.location}</span>
              <span>•</span>
              <span>📞 {user.phone}</span>
            </p>
          </div>

          {/* Quick Wallet Stats Box */}
          <div className="flex flex-wrap items-center gap-3">
            <div className="bg-slate-900/80 backdrop-blur-md rounded-2xl p-4 border border-slate-700/60 flex-1 sm:flex-initial min-w-[200px]">
              <div className="text-xs text-slate-400 flex items-center justify-between mb-1">
                <span>رصيد محفظة CardBox</span>
                <Wallet className="w-4 h-4 text-emerald-400" />
              </div>
              <div className="text-2xl font-black font-mono text-emerald-400">
                {user.walletBalance.toLocaleString()}{' '}
                <span className="text-xs font-normal text-slate-300">ر.ي</span>
              </div>
            </div>

            <button
              onClick={() => setActiveScreen('wallet-topup')}
              className="bg-emerald-600 hover:bg-emerald-500 text-white font-bold px-4 py-4 rounded-2xl shadow-lg shadow-emerald-600/30 flex items-center justify-center gap-2 transition-all hover:scale-105 active:scale-95 text-sm"
            >
              <PlusCircle className="w-5 h-5" />
              <span>تغذية الرصيد</span>
            </button>
          </div>
        </div>

        {/* Quick Stats Grid */}
        <div className="grid grid-cols-3 gap-3 mt-6 pt-5 border-t border-slate-800/80">
          <div className="bg-slate-950/40 rounded-xl p-3 border border-slate-800/50">
            <span className="text-[11px] text-slate-400 block mb-0.5">مبيعات اليوم</span>
            <span className="font-bold text-base text-white font-mono">
              {totalTodayRevenue.toLocaleString()} <span className="text-[10px] text-slate-400">ر.ي</span>
            </span>
          </div>

          <div className="bg-slate-950/40 rounded-xl p-3 border border-slate-800/50">
            <span className="text-[11px] text-slate-400 block mb-0.5">أرباح اليوم</span>
            <span className="font-bold text-base text-emerald-400 font-mono">
              +{totalTodayProfit.toLocaleString()} <span className="text-[10px] text-slate-400">ر.ي</span>
            </span>
          </div>

          <div className="bg-slate-950/40 rounded-xl p-3 border border-slate-800/50">
            <span className="text-[11px] text-slate-400 block mb-0.5">الكروت المباعة</span>
            <span className="font-bold text-base text-blue-400 font-mono">
              {totalTodayVouchers} <span className="text-[10px] text-slate-400">كرت</span>
            </span>
          </div>
        </div>
      </div>

      {/* Networks Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-blue-600/20 text-blue-400">
              <Wifi className="w-4 h-4" />
            </div>
            <h2 className="font-bold text-lg text-white">الشبكات المعتمدة لنقطتك</h2>
            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-mono">
              {displayNetworks.length}
            </span>
          </div>

          <button
            onClick={() => setActiveScreen('networks')}
            className="text-xs font-bold text-blue-400 hover:text-blue-300 flex items-center gap-1 transition-colors"
          >
            <span>استعراض كل الشبكات</span>
            <ChevronLeft className="w-4 h-4" />
          </button>
        </div>

        {displayNetworks.length === 0 ? (
          <div className="bg-slate-800/40 border border-slate-800 rounded-2xl p-8 text-center space-y-3">
            <Radio className="w-12 h-12 text-slate-600 mx-auto" />
            <p className="text-slate-300 font-semibold">لم تنضم لأي شبكة بعد</p>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              تصفح دليل شبكات الإنترنت في منطقتك وانضم إليها للبدء في بيع الكروت
            </p>
            <button
              onClick={() => setActiveScreen('networks')}
              className="bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold px-4 py-2 rounded-xl transition-all"
            >
              دليل الشبكات
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {displayNetworks.map((network) => (
              <div
                key={network.id}
                className="group relative bg-slate-850 hover:bg-slate-800/90 border border-slate-800 hover:border-blue-500/50 rounded-2xl p-5 transition-all duration-200 shadow-lg flex flex-col justify-between"
              >
                {/* Network Header */}
                <div>
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600/30 to-indigo-600/30 border border-blue-500/30 flex items-center justify-center text-blue-400">
                        <Wifi className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="font-bold text-base text-white group-hover:text-blue-400 transition-colors">
                          {network.name}
                        </h3>
                        <span className="text-[11px] text-slate-400">
                          المالك: {network.ownerName}
                        </span>
                      </div>
                    </div>

                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        togglePinNetwork(network.id);
                      }}
                      className={`p-1.5 rounded-lg transition-colors ${
                        network.isPinned
                          ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                          : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800'
                      }`}
                      title={network.isPinned ? 'إلغاء التثبيت' : 'تثبيت في الأعلى'}
                    >
                      <Pin className="w-4 h-4" />
                    </button>
                  </div>

                  {/* Ceiling & Remaining Balance Bar */}
                  <div className="mt-3 p-3 rounded-xl bg-slate-900/90 border border-slate-800 space-y-2">
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">الرصيد المتبقي بالسقف:</span>
                      <span className="font-black text-emerald-400 font-mono">
                        {network.currentBalance.toLocaleString()} {network.currency}
                      </span>
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                      <div
                        className="bg-gradient-to-r from-emerald-500 to-teal-400 h-2 rounded-full transition-all duration-500"
                        style={{
                          width: `${Math.min(
                            100,
                            Math.max(0, (network.currentBalance / (network.financialCeiling || 1)) * 100)
                          )}%`,
                        }}
                      />
                    </div>

                    <div className="flex justify-between text-[10px] text-slate-500">
                      <span>السقف المالي الممنوح:</span>
                      <span className="font-semibold text-slate-300">
                        {network.financialCeiling.toLocaleString()} {network.currency}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Sell Action Button */}
                <button
                  onClick={() => handleOpenNetwork(network)}
                  className="mt-4 w-full bg-blue-600/20 hover:bg-blue-600 border border-blue-500/40 hover:border-blue-600 text-blue-300 hover:text-white font-bold py-2.5 px-4 rounded-xl transition-all flex items-center justify-center gap-2 text-xs"
                >
                  <ShoppingBag className="w-4 h-4" />
                  <span>بيع كروت هذه الشبكة</span>
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Recent Sales History */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-600/20 text-emerald-400">
              <History className="w-4 h-4" />
            </div>
            <h2 className="font-bold text-lg text-white">آخر المبيعات</h2>
          </div>

          <button
            onClick={() => setActiveScreen('sales')}
            className="text-xs font-bold text-blue-400 hover:text-blue-300 flex items-center gap-1 transition-colors"
          >
            <span>سجل المبيعات الكامل</span>
            <ChevronLeft className="w-4 h-4" />
          </button>
        </div>

        {salesHistory.length === 0 ? (
          <div className="bg-slate-850 border border-slate-800 rounded-2xl p-6 text-center text-slate-500 text-xs">
            لا توجد مبيعات سابقة مسجلة حتى الآن
          </div>
        ) : (
          <div className="space-y-2">
            {salesHistory.slice(0, 4).map((sale) => (
              <div
                key={sale.id}
                onClick={() => onSelectReceipt(sale)}
                className="bg-slate-850 hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 transition-colors cursor-pointer flex items-center justify-between gap-4"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-blue-400 font-bold">
                    <Printer className="w-5 h-5 text-slate-300" />
                  </div>
                  <div>
                    <div className="font-bold text-sm text-white">{sale.packageName}</div>
                    <div className="text-xs text-slate-400 flex items-center gap-2 mt-0.5">
                      <span>{sale.networkName}</span>
                      <span>•</span>
                      <span className="font-mono text-emerald-400 font-semibold">
                        كود: {sale.voucherPin}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="text-left">
                  <div className="font-bold text-sm text-white font-mono">
                    {sale.totalAmount.toLocaleString()} ر.ي
                  </div>
                  <div className="text-[11px] text-slate-400 flex items-center gap-1 justify-end mt-0.5">
                    <Clock className="w-3 h-3 text-slate-500" />
                    <span>{new Date(sale.timestamp).toLocaleTimeString('ar-YE', { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
};
