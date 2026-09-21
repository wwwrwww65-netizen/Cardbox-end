import React from 'react';
import { usePos } from '../context/PosContext';
import { 
  Wallet, 
  PlusCircle, 
  ArrowDownLeft, 
  ArrowUpRight, 
  TrendingUp, 
  ShieldCheck, 
  History, 
  Clock, 
  CheckCircle2, 
  AlertCircle 
} from 'lucide-react';

export const WalletScreen: React.FC = () => {
  const { user, walletTransactions, setActiveScreen } = usePos();

  const totalDeposits = walletTransactions
    .filter(t => t.type === 'DEPOSIT' && t.status === 'COMPLETED')
    .reduce((acc, t) => acc + t.amount, 0);

  const totalSpent = walletTransactions
    .filter(t => t.type === 'VOUCHER_PURCHASE' && t.status === 'COMPLETED')
    .reduce((acc, t) => acc + t.amount, 0);

  return (
    <div className="space-y-6 pb-20">
      
      {/* Wallet Balance Hero Card */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-emerald-900 via-teal-900 to-slate-900 p-6 sm:p-8 border border-emerald-500/30 shadow-2xl">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-emerald-300 text-xs font-bold uppercase tracking-wider">
              <ShieldCheck className="w-4 h-4" />
              <span>محفظة CardBox POS الرقمية</span>
            </div>
            <div className="text-3xl sm:text-4xl font-black text-white font-mono">
              {user.walletBalance.toLocaleString()}{' '}
              <span className="text-base font-normal text-emerald-300">ريال يمني</span>
            </div>
            <p className="text-xs text-slate-300">
              تُستخدم لتغذية وشراء كروت الإنترنت من جميع الشبكات بدون قيود السقف المالي
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <button
              onClick={() => setActiveScreen('wallet-topup')}
              className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black px-6 py-3.5 rounded-2xl shadow-xl shadow-emerald-500/30 flex items-center gap-2 transition-all text-sm hover:scale-105 active:scale-95"
            >
              <PlusCircle className="w-5 h-5" />
              <span>تغذية رصيد المحفظة</span>
            </button>
          </div>
        </div>

        {/* Mini stats inside card */}
        <div className="grid grid-cols-2 gap-3 mt-6 pt-5 border-t border-emerald-800/60">
          <div className="bg-slate-950/40 rounded-xl p-3 border border-emerald-500/20">
            <span className="text-[11px] text-emerald-200/80 block mb-0.5">إجمالي الإيداعات</span>
            <span className="font-bold text-sm sm:text-base text-white font-mono">
              +{totalDeposits.toLocaleString()} ر.ي
            </span>
          </div>

          <div className="bg-slate-950/40 rounded-xl p-3 border border-emerald-500/20">
            <span className="text-[11px] text-emerald-200/80 block mb-0.5">إجمالي المشتريات</span>
            <span className="font-bold text-sm sm:text-base text-white font-mono">
              -{totalSpent.toLocaleString()} ر.ي
            </span>
          </div>
        </div>
      </div>

      {/* Transactions History */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-bold text-lg text-white flex items-center gap-2">
            <History className="w-5 h-5 text-blue-400" />
            <span>حركات المحفظة والإيداعات</span>
          </h2>
          <span className="text-xs text-slate-400">
            {walletTransactions.length} حركة مسجلة
          </span>
        </div>

        {walletTransactions.length === 0 ? (
          <div className="bg-slate-850 border border-slate-800 rounded-2xl p-8 text-center text-slate-500 text-xs">
            لا توجد حركات سابقة في المحفظة
          </div>
        ) : (
          <div className="space-y-3">
            {walletTransactions.map((tx) => {
              const isDeposit = tx.type === 'DEPOSIT';

              return (
                <div
                  key={tx.id}
                  className="bg-slate-850 border border-slate-800 rounded-2xl p-4 transition-all shadow-md flex items-center justify-between gap-4"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-11 h-11 rounded-2xl flex items-center justify-center font-bold ${
                        isDeposit
                          ? 'bg-emerald-500/20 border border-emerald-500/30 text-emerald-400'
                          : 'bg-blue-500/20 border border-blue-500/30 text-blue-400'
                      }`}
                    >
                      {isDeposit ? (
                        <ArrowDownLeft className="w-5 h-5" />
                      ) : (
                        <ArrowUpRight className="w-5 h-5" />
                      )}
                    </div>
                    <div>
                      <h3 className="font-bold text-sm text-white">{tx.title}</h3>
                      <div className="flex flex-wrap items-center gap-2 text-xs text-slate-400 mt-0.5">
                        <span>{tx.paymentMethod}</span>
                        <span>•</span>
                        <span className="font-mono text-slate-500">مرجع: {tx.referenceNumber}</span>
                      </div>
                    </div>
                  </div>

                  <div className="text-left">
                    <div
                      className={`font-black text-sm sm:text-base font-mono ${
                        isDeposit ? 'text-emerald-400' : 'text-slate-200'
                      }`}
                    >
                      {isDeposit ? '+' : '-'}
                      {tx.amount.toLocaleString()} {tx.currency}
                    </div>

                    <div className="flex items-center gap-1.5 justify-end mt-1">
                      {tx.status === 'COMPLETED' ? (
                        <span className="text-[10px] text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full font-semibold flex items-center gap-1">
                          <CheckCircle2 className="w-3 h-3" />
                          <span>مكتملة</span>
                        </span>
                      ) : (
                        <span className="text-[10px] text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full font-semibold flex items-center gap-1">
                          <Clock className="w-3 h-3 animate-spin" />
                          <span>قيد التحقق</span>
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

    </div>
  );
};
