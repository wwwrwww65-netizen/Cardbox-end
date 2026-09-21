import React, { useRef } from 'react';
import { OrderTransaction, PrinterSettings } from '../types';
import { Printer, Share2, Download, X, Check, Copy, Wifi } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';

interface ReceiptModalProps {
  order: OrderTransaction | null;
  onClose: () => void;
  printerSettings: PrinterSettings;
  onPrintSuccess?: () => void;
}

export const ReceiptModal: React.FC<ReceiptModalProps> = ({
  order,
  onClose,
  printerSettings,
  onPrintSuccess
}) => {
  const receiptRef = useRef<HTMLDivElement>(null);
  const [copied, setCopied] = React.useState(false);

  if (!order) return null;

  const handleCopyPin = () => {
    navigator.clipboard.writeText(order.voucherPin);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handlePrint = () => {
    window.print();
    if (onPrintSuccess) onPrintSuccess();
  };

  const handleShare = () => {
    const text = `
🌟 كرت إنترنت - ${order.networkName}
🏷️ الباقة: ${order.packageName}
🔑 رمز الكرت (PIN): ${order.voucherPin}
⏱️ المدة: ${order.duration || 'غير محدد'}
📦 الرصيد: ${order.dataQuota || 'غير محدد'}
🏪 نقطة البيع: ${order.posStoreName}
شكراً لتعاملكم معنا!
    `.trim();

    if (navigator.share) {
      navigator.share({
        title: `كرت إنترنت - ${order.networkName}`,
        text: text,
      }).catch(() => {});
    } else {
      navigator.clipboard.writeText(text);
      alert('تم نسخ تفاصيل الكرت لمشاركتها عبر الواتساب أو الرسائل القصيرة!');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700 rounded-3xl max-w-sm w-full shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        
        {/* Header Modal Bar */}
        <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-850">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-blue-600/20 text-blue-400">
              <Printer className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base text-white">إيصال طباعة الكرت</h3>
              <p className="text-xs text-slate-400">معاينة الفاتورة الحرارية ({printerSettings.paperWidth})</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Receipt Body styled like real Thermal Receipt Paper */}
        <div className="p-6 overflow-y-auto flex-1 bg-slate-950 flex justify-center">
          <div 
            ref={receiptRef}
            className={`bg-white text-black p-5 rounded-md shadow-lg border border-gray-200 font-mono text-center select-text ${
              printerSettings.paperWidth === '58mm' ? 'w-[280px]' : 'w-[320px]'
            }`}
            style={{ fontFamily: "'Cairo', 'Courier New', monospace" }}
          >
            {/* Header / Store Name */}
            {printerSettings.showStoreName && (
              <div className="border-b-2 border-dashed border-gray-400 pb-3 mb-3">
                <div className="font-black text-lg text-black tracking-wide">
                  {order.posStoreName}
                </div>
                <div className="text-[11px] text-gray-700 mt-0.5">
                  نظام كارد بوكس للدفع الإلكتروني
                </div>
              </div>
            )}

            {/* Network & Package Details */}
            <div className="my-2">
              <div className="text-xs text-gray-600 font-semibold">شبكة الإنترنت</div>
              <div className="font-black text-base text-blue-900 leading-tight">
                {order.networkName}
              </div>
              <div className="inline-block px-2 py-0.5 mt-1 rounded bg-gray-100 text-xs font-bold text-gray-800">
                {order.packageName}
              </div>
            </div>

            {/* PIN Code Box (Highlight) */}
            <div className="my-4 p-3 bg-gray-50 border-2 border-black rounded-lg">
              <div className="text-[11px] text-gray-500 font-bold uppercase tracking-wider mb-1">
                رمز الكرت (PIN CODE)
              </div>
              <div className="font-black text-2xl tracking-widest text-black select-all">
                {order.voucherPin}
              </div>
            </div>

            {/* QR Code */}
            {printerSettings.showBarcode && (
              <div className="my-3 flex flex-col items-center justify-center">
                <QRCodeSVG value={order.voucherPin} size={110} level="M" />
                <span className="text-[10px] text-gray-500 mt-1 font-sans">امسح الكود لتسجيل الدخول السريع</span>
              </div>
            )}

            {/* Specs Grid */}
            <div className="text-xs text-gray-700 border-t border-b border-dashed border-gray-300 py-2.5 my-2 space-y-1">
              <div className="flex justify-between">
                <span className="text-gray-500">حجم البيانات:</span>
                <span className="font-bold text-black">{order.dataQuota || 'غير محدد'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">الوقت المتاح:</span>
                <span className="font-bold text-black">{order.duration || 'غير محدد'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">صلاحية الكرت:</span>
                <span className="font-bold text-black">{order.validity || 'غير محدد'}</span>
              </div>
              <div className="flex justify-between font-bold text-sm text-black pt-1 border-t border-gray-200">
                <span>سعر البيع:</span>
                <span>{order.totalAmount.toLocaleString()} ر.ي</span>
              </div>
            </div>

            {/* Receipt Meta */}
            <div className="text-[10px] text-gray-500 space-y-0.5 mt-2">
              <div>رقم العملية: {order.id}</div>
              <div>التاريخ: {new Date(order.timestamp).toLocaleString('ar-YE')}</div>
            </div>

            {/* Footer Note */}
            {printerSettings.showFooterNotes && (
              <div className="text-[10px] text-gray-600 mt-3 pt-2 border-t border-dashed border-gray-300">
                {printerSettings.customFooterNote || 'شكراً لتعاملكم معنا'}
              </div>
            )}
          </div>
        </div>

        {/* Action Buttons Footer */}
        <div className="p-4 bg-slate-900 border-t border-slate-800 flex items-center gap-2">
          <button
            onClick={handlePrint}
            className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-bold py-3 px-4 rounded-2xl shadow-lg shadow-blue-600/30 transition-all text-sm"
          >
            <Printer className="w-4 h-4" />
            طباعة الإيصال
          </button>

          <button
            onClick={handleCopyPin}
            className="p-3 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-2xl border border-slate-700 transition-colors"
            title="نسخ رمز الكرت"
          >
            {copied ? <Check className="w-5 h-5 text-emerald-400" /> : <Copy className="w-5 h-5" />}
          </button>

          <button
            onClick={handleShare}
            className="p-3 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-2xl border border-slate-700 transition-colors"
            title="مشاركة عبر الواتساب"
          >
            <Share2 className="w-5 h-5" />
          </button>
        </div>

      </div>
    </div>
  );
};
