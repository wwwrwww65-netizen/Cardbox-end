import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  User, 
  Store, 
  Phone, 
  MapPin, 
  ShieldCheck, 
  LogOut, 
  Save, 
  Printer, 
  Lock, 
  CreditCard 
} from 'lucide-react';

export const ProfileScreen: React.FC = () => {
  const { user, login, logout, setActiveScreen, showAlert } = usePos();
  
  const [storeName, setStoreName] = useState(user.storeName);
  const [phone, setPhone] = useState(user.phone);
  const [location, setLocation] = useState(user.location);
  const [isSaved, setIsSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    login(storeName, phone, location);
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 2000);
  };

  return (
    <div className="space-y-6 pb-20 max-w-3xl mx-auto">
      
      {/* Profile Banner */}
      <div className="bg-slate-850 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-3xl bg-gradient-to-br from-blue-600 to-indigo-700 border-2 border-blue-400/40 flex items-center justify-center text-2xl font-black text-white shadow-xl shadow-blue-600/30">
            {user.storeName.charAt(0)}
          </div>
          <div>
            <h1 className="text-xl font-black text-white">{user.storeName}</h1>
            <p className="text-xs text-slate-400 mt-0.5">نقطة بيع معتمدة • حساب نشط</p>
            <span className="inline-block mt-2 text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-bold">
              معتمد وموثق بنظام CardBox
            </span>
          </div>
        </div>

        <button
          onClick={logout}
          className="bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 px-4 py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-colors self-start sm:self-auto"
        >
          <LogOut className="w-4 h-4" />
          <span>تسجيل الخروج</span>
        </button>
      </div>

      {/* Profile Form */}
      <form onSubmit={handleSave} className="bg-slate-850 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <h3 className="font-bold text-base text-white border-b border-slate-800 pb-3">
          بيانات نقطة البيع
        </h3>

        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-300 block">اسم المتجر / المحل</label>
          <div className="relative">
            <Store className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              required
              value={storeName}
              onChange={(e) => setStoreName(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 pr-11 pl-4 text-sm text-white focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>
        </div>

        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-300 block">رقم هاتف الاتصال / الواتساب</label>
          <div className="relative">
            <Phone className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
            <input
              type="tel"
              required
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 pr-11 pl-4 text-sm text-white font-mono focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>
        </div>

        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-300 block">الموقع / العنوان</label>
          <div className="relative">
            <MapPin className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              required
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 pr-11 pl-4 text-sm text-white focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>
        </div>

        <button
          type="submit"
          className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3.5 px-6 rounded-2xl shadow-lg shadow-blue-600/30 flex items-center justify-center gap-2 transition-all text-sm mt-4"
        >
          <Save className="w-4 h-4" />
          <span>حفظ التعديلات</span>
        </button>
      </form>

      {/* Quick Links */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <button
          onClick={() => setActiveScreen('printer')}
          className="bg-slate-850 hover:bg-slate-800 border border-slate-800 p-4 rounded-2xl flex items-center justify-between text-right transition-all"
        >
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-blue-500/10 text-blue-400">
              <Printer className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-sm text-white">إعدادات الطابعة الحرارية</div>
              <div className="text-[11px] text-slate-400">تخصيص الورق والترويسة</div>
            </div>
          </div>
        </button>

        <button
          onClick={() => setActiveScreen('wallet')}
          className="bg-slate-850 hover:bg-slate-800 border border-slate-800 p-4 rounded-2xl flex items-center justify-between text-right transition-all"
        >
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400">
              <CreditCard className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-sm text-white">إدارة المحفظة والرصيد</div>
              <div className="text-[11px] text-slate-400">سجل الإيداعات والتحويلات</div>
            </div>
          </div>
        </button>
      </div>

    </div>
  );
};
