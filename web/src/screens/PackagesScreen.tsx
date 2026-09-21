import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  Wifi, 
  ArrowRight, 
  Clock, 
  Database, 
  Calendar, 
  Wallet, 
  CreditCard, 
  Plus, 
  Minus, 
  Printer, 
  CheckCircle2, 
  Smartphone,
  Sparkles
} from 'lucide-react';
import { VoucherPackage, OrderTransaction } from '../types';

interface PackagesScreenProps {
  onOpenReceipt: (order: OrderTransaction) => void;
}

export const PackagesScreen: React.FC<PackagesScreenProps> = ({ onOpenReceipt }) => {
  const { 
    selectedNetwork, 
    packages, 
    user, 
    buyVouchers, 
    setActiveScreen, 
    printerSettings 
  } = usePos();

  const [selectedPkg, setSelectedPkg] = useState<VoucherPackage | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [paymentSource, setPaymentSource] = useState<'NETWORK_CREDIT' | 'WALLET'>('NETWORK_CREDIT');
  const [customerPhone, setCustomerPhone] = useState<string>('');
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  if (!selectedNetwork) {
    return (
      <div className="text-center py-20 space-y-4">
        <Wifi className="w-16 h-16 text-slate-600 mx-auto" />
        <h2 className="text-lg font-bold text-white">يرجى اختيار شبكة أولاً</h2>
        <button
          onClick={() => setActiveScreen('networks')}
          className="bg-blue-600 hover:bg-blue-500 text-white font-bold px-6 py-2.5 rounded-xl text-sm transition-all"
        >
          الانتقال للشبكات
        </button>
      </div>
    );
  }

  // Filter packages for this network or default packages
  const networkPackages = packages.filter(p => p.networkId === selectedNetwork.id);
  const displayPackages = networkPackages.length > 0 ? networkPackages : packages.slice(0, 4);

  const activePackage = selectedPkg || displayPackages[0];

  const totalSellingPrice = activePackage ? activePackage.price * quantity : 0;
  const totalCostPrice = activePackage ? activePackage.posPrice * quantity : 0;
  const totalProfit = totalSellingPrice - totalCostPrice;

  const handleBuy = () => {
    if (!activePackage) return;
    setIsProcessing(true);

    setTimeout(() => {
      const order = buyVouchers(
        selectedNetwork,
        activePackage,
        quantity,
        paymentSource,
        customerPhone || undefined
      );

      setIsProcessing(false);

      if (order) {
        // Show receipt modal
        onOpenReceipt(order);
      }
    }, 400);
  };

  return (
    <div className="space-y-6 pb-20">
      
      {/* Network Header Banner */}
      <div className="bg-slate-850 border border-slate-800 rounded-3xl p-5 shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setActiveScreen('networks')}
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white transition-colors"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-black text-white">{selectedNetwork.name}</h1>
              <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-blue-500/20 text-blue-400 font-mono font-bold">
                {selectedNetwork.code}
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              مالك الشبكة: {selectedNetwork.ownerName} • رصيد السقف المتاح:{' '}
              <span className="text-emerald-400 font-bold font-mono">
                {selectedNetwork.currentBalance.toLocaleString()} {selectedNetwork.currency}
              </span>
            </p>
          </div>
        </div>

        {/* Quick Payment Mode Selector */}
        <div className="flex items-center gap-2 bg-slate-900/90 p-1.5 rounded-2xl border border-slate-700/80">
          <button
            onClick={() => setPaymentSource('NETWORK_CREDIT')}
            className={`flex-1 sm:flex-initial flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold transition-all ${
              paymentSource === 'NETWORK_CREDIT'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <CreditCard className="w-4 h-4" />
            <span>سقف الشبكة</span>
          </button>

          <button
            onClick={() => setPaymentSource('WALLET')}
            className={`flex-1 sm:flex-initial flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold transition-all ${
              paymentSource === 'WALLET'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Wallet className="w-4 h-4" />
            <span>محفظة CardBox</span>
          </button>
        </div>
      </div>

      {/* Packages Grid */}
      <div className="space-y-3">
        <h2 className="font-bold text-base text-white flex items-center gap-2">
          <span>اختر باقة الكرت</span>
          <span className="text-xs text-slate-400 font-normal">
            (انقر على الباقة لتحديدها)
          </span>
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {displayPackages.map((pkg) => {
            const isSelected = activePackage?.id === pkg.id;

            return (
              <div
                key={pkg.id}
                onClick={() => setSelectedPkg(pkg)}
                className={`relative rounded-3xl p-5 border-2 transition-all cursor-pointer shadow-lg flex flex-col justify-between overflow-hidden ${
                  isSelected
                    ? 'bg-slate-850 border-blue-500 ring-4 ring-blue-500/20'
                    : 'bg-slate-850/70 border-slate-800 hover:border-slate-700'
                }`}
              >
                {pkg.isPopular && (
                  <div className="absolute top-3 left-3 bg-gradient-to-r from-amber-500 to-orange-500 text-white text-[10px] font-black px-2.5 py-0.5 rounded-full shadow-md">
                    الأكثر طلباً ⭐
                  </div>
                )}

                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <span 
                      className="w-3 h-3 rounded-full" 
                      style={{ backgroundColor: pkg.colorHex || '#0F4C81' }} 
                    />
                    <h3 className="font-bold text-base text-white">{pkg.name}</h3>
                  </div>

                  {/* Specs Pill Badges */}
                  <div className="flex flex-wrap gap-2 my-3">
                    <div className="flex items-center gap-1 bg-slate-900 px-2.5 py-1 rounded-lg text-xs text-slate-300 border border-slate-800">
                      <Database className="w-3.5 h-3.5 text-blue-400" />
                      <span>{pkg.dataQuota}</span>
                    </div>

                    <div className="flex items-center gap-1 bg-slate-900 px-2.5 py-1 rounded-lg text-xs text-slate-300 border border-slate-800">
                      <Clock className="w-3.5 h-3.5 text-amber-400" />
                      <span>{pkg.duration}</span>
                    </div>

                    <div className="flex items-center gap-1 bg-slate-900 px-2.5 py-1 rounded-lg text-xs text-slate-300 border border-slate-800">
                      <Calendar className="w-3.5 h-3.5 text-purple-400" />
                      <span>{pkg.validity}</span>
                    </div>
                  </div>
                </div>

                {/* Price Details */}
                <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="text-[11px] text-slate-400">سعر البيع للمستهلك</div>
                    <div className="text-xl font-black text-white font-mono">
                      {pkg.price} <span className="text-xs font-normal text-slate-400">{pkg.currency}</span>
                    </div>
                  </div>

                  <div className="text-left bg-emerald-500/10 border border-emerald-500/20 px-2.5 py-1.5 rounded-xl">
                    <div className="text-[10px] text-emerald-300">سعر التكلفة عليك</div>
                    <div className="text-sm font-bold text-emerald-400 font-mono">
                      {pkg.posPrice} {pkg.currency}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Checkout & Quantity Form */}
      {activePackage && (
        <div className="bg-gradient-to-br from-slate-850 to-slate-900 border-2 border-blue-500/40 rounded-3xl p-6 shadow-2xl space-y-6">
          <div className="flex items-center justify-between border-b border-slate-800 pb-4">
            <div>
              <span className="text-xs text-blue-400 font-semibold">الباقة المحددة للإصدار</span>
              <h3 className="text-xl font-black text-white">{activePackage.name}</h3>
            </div>
            <div className="text-left">
              <span className="text-xs text-slate-400">طريقة الخصم</span>
              <div className="text-xs font-bold text-emerald-400">
                {paymentSource === 'NETWORK_CREDIT' ? 'سقف الشبكة' : 'محفظة CardBox'}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Quantity Selector */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-300 block">
                الكمية المطلوبة (عدد الكروت)
              </label>
              <div className="flex items-center gap-3">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="w-12 h-12 rounded-2xl bg-slate-800 hover:bg-slate-700 text-white flex items-center justify-center font-bold text-xl border border-slate-700 transition-colors"
                >
                  <Minus className="w-5 h-5" />
                </button>
                <div className="flex-1 bg-slate-900 border border-slate-700 rounded-2xl h-12 flex items-center justify-center text-xl font-black text-white font-mono">
                  {quantity}
                </div>
                <button
                  onClick={() => setQuantity(quantity + 1)}
                  className="w-12 h-12 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white flex items-center justify-center font-bold text-xl transition-colors shadow-lg shadow-blue-600/30"
                >
                  <Plus className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Customer Phone (Optional) */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-300 block">
                رقم هاتف العميل (اختياري - للإرسال عبر SMS/واتساب)
              </label>
              <div className="relative">
                <Smartphone className="w-5 h-5 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
                <input
                  type="tel"
                  placeholder="مثال: 777123456"
                  value={customerPhone}
                  onChange={(e) => setCustomerPhone(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 pr-12 pl-4 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors font-mono"
                />
              </div>
            </div>

          </div>

          {/* Pricing Summary */}
          <div className="bg-slate-950/80 rounded-2xl p-4 border border-slate-800 grid grid-cols-3 gap-3 text-center">
            <div>
              <span className="text-[11px] text-slate-400 block mb-1">المبلغ الإجمالي</span>
              <span className="text-lg font-black text-white font-mono">
                {totalSellingPrice.toLocaleString()} <span className="text-xs text-slate-400">ر.ي</span>
              </span>
            </div>

            <div>
              <span className="text-[11px] text-slate-400 block mb-1">التكلفة المخصومة</span>
              <span className="text-lg font-black text-slate-300 font-mono">
                {totalCostPrice.toLocaleString()} <span className="text-xs text-slate-400">ر.ي</span>
              </span>
            </div>

            <div className="bg-emerald-500/10 rounded-xl p-1 border border-emerald-500/20">
              <span className="text-[11px] text-emerald-400 block mb-1 font-bold">ربح نقطتك الصافي</span>
              <span className="text-lg font-black text-emerald-400 font-mono">
                +{totalProfit.toLocaleString()} <span className="text-xs text-emerald-300">ر.ي</span>
              </span>
            </div>
          </div>

          {/* Buy and Print Buttons */}
          <div className="flex flex-col sm:flex-row gap-3 pt-2">
            <button
              onClick={handleBuy}
              disabled={isProcessing}
              className="flex-1 bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-700 hover:from-blue-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl shadow-xl shadow-blue-600/30 flex items-center justify-center gap-3 transition-all text-base disabled:opacity-50"
            >
              <Printer className="w-5 h-5" />
              <span>{isProcessing ? 'جارِ إصدار الكرت...' : 'إصدار وطباعة الكرت الآن'}</span>
            </button>
          </div>

        </div>
      )}

    </div>
  );
};
