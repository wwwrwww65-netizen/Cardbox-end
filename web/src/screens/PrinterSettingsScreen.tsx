import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  Printer, 
  Bluetooth, 
  Settings, 
  CheckCircle2, 
  FileText, 
  QrCode, 
  Sliders, 
  RefreshCw 
} from 'lucide-react';

export const PrinterSettingsScreen: React.FC = () => {
  const { printerSettings, updatePrinterSettings, user } = usePos();
  const [isTesting, setIsTesting] = useState(false);

  const handleTestPrint = () => {
    setIsTesting(true);
    setTimeout(() => {
      window.print();
      setIsTesting(false);
    }, 300);
  };

  return (
    <div className="space-y-6 pb-20 max-w-3xl mx-auto">
      
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-white flex items-center gap-2">
          <Printer className="w-6 h-6 text-blue-400" />
          <span>إعدادات الطابعة الحرارية</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          ضبط مقاس ورق الفاتورة (58mm أو 80mm)، كود الاستجابة السريعة (QR)، والملاحظات المطبوعة
        </p>
      </div>

      {/* Printer Status Card */}
      <div className="bg-slate-850 border border-slate-800 rounded-3xl p-5 shadow-xl flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <Bluetooth className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-base text-white">{printerSettings.printerName}</h3>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold">
                جاهزة للطباعة
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              متوافقة مع الطباعة المباشرة من المتصفح وأجهزة البلوتوث ESC/POS
            </p>
          </div>
        </div>

        <button
          onClick={handleTestPrint}
          disabled={isTesting}
          className="bg-blue-600 hover:bg-blue-500 text-white font-bold px-4 py-2.5 rounded-xl text-xs flex items-center gap-1.5 transition-all shadow-lg shadow-blue-600/30"
        >
          <Printer className="w-4 h-4" />
          <span>{isTesting ? 'جارِ الاختبار...' : 'طباعة تجريبية'}</span>
        </button>
      </div>

      {/* Settings Form */}
      <div className="bg-slate-850 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
        
        {/* Paper Size Selection */}
        <div className="space-y-3">
          <label className="text-xs font-bold text-slate-300 block">
            عرض ورق الطابعة (Paper Width):
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              onClick={() => updatePrinterSettings({ paperWidth: '58mm' })}
              className={`p-4 rounded-2xl border-2 text-center transition-all ${
                printerSettings.paperWidth === '58mm'
                  ? 'bg-blue-600/20 border-blue-500 text-white'
                  : 'bg-slate-900 border-slate-700 text-slate-400 hover:text-white'
              }`}
            >
              <div className="font-black text-lg font-mono">58mm</div>
              <div className="text-xs text-slate-400 mt-1">طابعات البلوتوث المحمولة الصغيرة</div>
            </button>

            <button
              onClick={() => updatePrinterSettings({ paperWidth: '80mm' })}
              className={`p-4 rounded-2xl border-2 text-center transition-all ${
                printerSettings.paperWidth === '80mm'
                  ? 'bg-blue-600/20 border-blue-500 text-white'
                  : 'bg-slate-900 border-slate-700 text-slate-400 hover:text-white'
              }`}
            >
              <div className="font-black text-lg font-mono">80mm</div>
              <div className="text-xs text-slate-400 mt-1">طابعات الكاشير المكتبية الكبيرة</div>
            </button>
          </div>
        </div>

        {/* Toggle Options */}
        <div className="space-y-4 pt-4 border-t border-slate-800">
          
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-bold text-sm text-white">الطباعة التلقائية بعد الإصدار</h4>
              <p className="text-xs text-slate-400">فتح نافذة الطباعة فور النقر على زر إصدار الكرت</p>
            </div>
            <input
              type="checkbox"
              checked={printerSettings.autoPrintAfterSale}
              onChange={(e) => updatePrinterSettings({ autoPrintAfterSale: e.target.checked })}
              className="w-5 h-5 accent-blue-600 rounded cursor-pointer"
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-bold text-sm text-white">طباعة اسم نقطة البيع بالترويسة</h4>
              <p className="text-xs text-slate-400">إظهار: ({user.storeName}) أعلى الفاتورة</p>
            </div>
            <input
              type="checkbox"
              checked={printerSettings.showStoreName}
              onChange={(e) => updatePrinterSettings({ showStoreName: e.target.checked })}
              className="w-5 h-5 accent-blue-600 rounded cursor-pointer"
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-bold text-sm text-white">طباعة باركود الاستجابة السريعة (QR Code)</h4>
              <p className="text-xs text-slate-400">يسهل على العميل مسح الكود وتسجيل الدخول المباشر بالشبكة</p>
            </div>
            <input
              type="checkbox"
              checked={printerSettings.showBarcode}
              onChange={(e) => updatePrinterSettings({ showBarcode: e.target.checked })}
              className="w-5 h-5 accent-blue-600 rounded cursor-pointer"
            />
          </div>

        </div>

        {/* Custom Footer Notes */}
        <div className="space-y-2 pt-4 border-t border-slate-800">
          <label className="text-xs font-bold text-slate-300 block">
            ملاحظات أسفل الفاتورة (Footer Note)
          </label>
          <input
            type="text"
            value={printerSettings.customFooterNote}
            onChange={(e) => updatePrinterSettings({ customFooterNote: e.target.value })}
            placeholder="مثال: شكراً لتعاملكم معنا ونتمنى لكم تصفحاً ممتعاً"
            className="w-full bg-slate-900 border border-slate-700 rounded-2xl py-3 px-4 text-xs text-white focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

      </div>

    </div>
  );
};
