import React, { useState } from 'react';
import { usePos, eWalletOptions } from '../context/PosContext';
import { 
  Wallet, 
  ArrowRight, 
  Copy, 
  Check, 
  Upload, 
  CheckCircle2, 
  ShieldCheck, 
  AlertCircle,
  HelpCircle
} from 'lucide-react';
import { EWalletOption } from '../types';

export const WalletTopUpScreen: React.FC = () => {
  const { topUpWallet, setActiveScreen, showAlert } = usePos();
  
  const [selectedWallet, setSelectedWallet] = useState<EWalletOption>(eWalletOptions[0]);
  const [amount, setAmount] = useState<string>('50000');
  const [refNumber, setRefNumber] = useState<string>('');
  const [copied, setCopied] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  const handleCopyAccount = () => {
    navigator.clipboard.writeText(selectedWallet.accountNumber);
    setCopied(true);
    showAlert('تم نسخ رقم الحساب بنجاح', 'success');
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const numAmount = parseFloat(amount);

    if (isNaN(numAmount) || numAmount < 1000) {
      showAlert('يرجى إدخال مبلغ صحيح (الحد الأدنى 1,000 ريال)', 'warning');
      return;
    }

    if (!refNumber.trim()) {
      showAlert('يرجى إدخال رقم الحوالة أو رقم الإشعار للتأكيد', 'warning');
      return;
    }

    setIsSubmitting(true);
    setTimeout(() => {
      topUpWallet(numAmount, selectedWallet.arabicName, refNumber.trim());
      setIsSubmitting(false);
      setActiveScreen('wallet');
    }, 600);
  };

  return (
    <div className="space-y-6 pb-20 max-w-3xl mx-auto">
      
      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => setActiveScreen('wallet')}
          className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white transition-colors"
        >
          <ArrowRight className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-black text-white">تغذية رصيد المحفظة</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            اختر الحساب البنكي أو المحفظة الإلكترونية لإتمام التحويل
          </p>
        </div>
      </div>

      {/* Step 1: Select E-Wallet / Bank */}
      <div className="space-y-3">
        <label className="text-xs font-bold text-slate-300 block">
          1. اختر طريقة التحويل البنكي / المحفظة:
        </label>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {eWalletOptions.map((w) => {
            const isSelected = selectedWallet.id === w.id;
            return (
              <div
                key={w.id}
                onClick={() => setSelectedWallet(w)}
                className={`p-4 rounded-2xl border-2 transition-all cursor-pointer flex items-center justify-between ${
                  isSelected
                    ? 'bg-slate-850 border-blue-500 ring-4 ring-blue-500/20'
                    : 'bg-slate-850/60 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div>
                  <h3 className="font-bold text-sm text-white">{w.arabicName}</h3>
                  <p className="text-[11px] text-slate-400 mt-0.5">{w.subtitle}</p>
                </div>
                <div 
                  className="w-4 h-4 rounded-full border-2 flex items-center justify-center"
                  style={{ borderColor: isSelected ? '#3B82F6' : '#64748B' }}
                >
                  {isSelected && <div className="w-2 h-2 rounded-full bg-blue-500" />}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Account Details Box */}
      <div className="bg-gradient-to-br from-blue-950/60 to-slate-900 border-2 border-blue-500/30 rounded-3xl p-6 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-xs text-blue-300 font-bold">بيانات الحساب المعتمد للتحويل:</span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/30">
            حساب معتمد رسمي
          </span>
        </div>

        <div className="bg-slate-950/80 rounded-2xl p-4 border border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <span className="text-[11px] text-slate-400 block">{selectedWallet.accountLabel || 'رقم الحساب'}</span>
            <span className="text-2xl font-black text-white font-mono tracking-wider select-all">
              {selectedWallet.accountNumber}
            </span>
            <span className="text-xs text-slate-400 block mt-1">
              اسم المستلم: <strong className="text-slate-200">{selectedWallet.accountHolderName}</strong>
            </span>
          </div>

          <button
            onClick={handleCopyAccount}
            className="bg-blue-600 hover:bg-blue-500 text-white font-bold px-4 py-3 rounded-xl text-xs flex items-center justify-center gap-2 transition-all shadow-lg shadow-blue-600/30 self-stretch sm:self-auto"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
            <span>{copied ? 'تم النسخ' : 'نسخ رقم الحساب'}</span>
          </button>
        </div>

        <div className="text-xs text-slate-400 flex items-start gap-2 bg-slate-900/50 p-3 rounded-xl border border-slate-800">
          <HelpCircle className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
          <span>{selectedWallet.instructions}</span>
        </div>
      </div>

      {/* Step 2: Confirm Form */}
      <form onSubmit={handleSubmit} className="bg-slate-850 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
        <h3 className="font-bold text-base text-white">2. تأكيد بيانات الإيداع:</h3>

        {/* Amount */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-300 block">
            المبلغ المحول (ريال يمني)
          </label>
          <input
            type="number"
            min="1000"
            step="500"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 px-4 text-base font-bold text-white font-mono focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        {/* Quick Amount Pills */}
        <div className="flex flex-wrap gap-2">
          {['10000', '25000', '50000', '100000', '200000'].map((val) => (
            <button
              type="button"
              key={val}
              onClick={() => setAmount(val)}
              className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 transition-colors"
            >
              {parseInt(val).toLocaleString()} ر.ي
            </button>
          ))}
        </div>

        {/* Reference / Transaction Number */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-300 block">
            رقم الحوالة / رقم العملية المرجعي من تطبيق البنك
          </label>
          <input
            type="text"
            required
            placeholder="مثال: KR-99382103 أو 77123490"
            value={refNumber}
            onChange={(e) => setRefNumber(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 px-4 text-sm text-white font-mono focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-black py-4 px-6 rounded-2xl shadow-xl shadow-emerald-600/30 flex items-center justify-center gap-2 transition-all text-base disabled:opacity-50"
        >
          <CheckCircle2 className="w-5 h-5" />
          <span>{isSubmitting ? 'جارِ إرسال طلب التأكيد...' : 'إرسال إشعار الإيداع للتأكيد'}</span>
        </button>
      </form>

    </div>
  );
};
