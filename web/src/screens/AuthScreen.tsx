import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { Store, Phone, MapPin, Lock, ArrowLeft, Sparkles, ShieldCheck } from 'lucide-react';

export const AuthScreen: React.FC = () => {
  const { login } = usePos();
  const [isRegister, setIsRegister] = useState(false);
  const [storeName, setStoreName] = useState('بصمة العصر الحديث للاتصالات');
  const [phone, setPhone] = useState('777889900');
  const [location, setLocation] = useState('صنعاء - شارع تعز');
  const [password, setPassword] = useState('123456');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login(storeName, phone, location);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center p-4">
      
      <div className="w-full max-w-md space-y-6">
        
        {/* Logo & Brand */}
        <div className="text-center space-y-2">
          <div className="w-16 h-16 rounded-3xl bg-gradient-to-br from-blue-600 to-indigo-700 mx-auto flex items-center justify-center text-white font-black text-2xl shadow-2xl shadow-blue-500/30 border border-blue-400/30">
            CB
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">
            CardBox POS
          </h1>
          <p className="text-xs text-slate-400">
            نظام نقاط البيع المعتمد لإدارة وبيع كروت شبكات الإنترنت والطباعة الحرارية
          </p>
        </div>

        {/* Card Form */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 shadow-2xl space-y-6">
          
          <div className="flex border-b border-slate-800 pb-3">
            <button
              onClick={() => setIsRegister(false)}
              className={`flex-1 py-2 text-center text-sm font-bold border-b-2 transition-all ${
                !isRegister
                  ? 'border-blue-500 text-blue-400'
                  : 'border-transparent text-slate-400 hover:text-slate-300'
              }`}
            >
              تسجيل الدخول
            </button>
            <button
              onClick={() => setIsRegister(true)}
              className={`flex-1 py-2 text-center text-sm font-bold border-b-2 transition-all ${
                isRegister
                  ? 'border-blue-500 text-blue-400'
                  : 'border-transparent text-slate-400 hover:text-slate-300'
              }`}
            >
              إنشاء نقطة بيع جديدة
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-300">اسم المتجر / المحل</label>
              <div className="relative">
                <Store className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  placeholder="مثال: بصمة العصر الحديث"
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-2xl py-3 pr-11 pl-4 text-sm text-white focus:outline-none focus:border-blue-500 transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-300">رقم الهاتف</label>
              <div className="relative">
                <Phone className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
                <input
                  type="tel"
                  required
                  placeholder="مثال: 777889900"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-2xl py-3 pr-11 pl-4 text-sm text-white font-mono focus:outline-none focus:border-blue-500 transition-colors"
                />
              </div>
            </div>

            {isRegister && (
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">المحافظة والمنطقة</label>
                <div className="relative">
                  <MapPin className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    required
                    placeholder="مثال: صنعاء - شارع تعز"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-2xl py-3 pr-11 pl-4 text-sm text-white focus:outline-none focus:border-blue-500 transition-colors"
                  />
                </div>
              </div>
            )}

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-300">كلمة المرور</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute right-4 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-2xl py-3 pr-11 pl-4 text-sm text-white focus:outline-none focus:border-blue-500 transition-colors"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-black py-3.5 px-6 rounded-2xl shadow-xl shadow-blue-600/30 flex items-center justify-center gap-2 transition-all text-sm mt-4"
            >
              <span>{isRegister ? 'تسجيل واعتماد الحساب' : 'دخول لنظام نقاط البيع'}</span>
              <ArrowLeft className="w-4 h-4" />
            </button>

          </form>

        </div>

      </div>

    </div>
  );
};
