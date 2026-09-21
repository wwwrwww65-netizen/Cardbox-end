import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  History, 
  Search, 
  Printer, 
  Calendar, 
  Download, 
  Clock, 
  Filter, 
  Share2,
  TrendingUp,
  Tag
} from 'lucide-react';
import { OrderTransaction } from '../types';

interface SalesHistoryScreenProps {
  onSelectReceipt: (order: OrderTransaction) => void;
}

export const SalesHistoryScreen: React.FC<SalesHistoryScreenProps> = ({ onSelectReceipt }) => {
  const { salesHistory, networks, showAlert } = usePos();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedNetworkFilter, setSelectedNetworkFilter] = useState('ALL');

  const filteredSales = salesHistory.filter(sale => {
    const matchesSearch = 
      sale.packageName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      sale.networkName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      sale.voucherPin.includes(searchQuery) ||
      sale.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (sale.customerPhone && sale.customerPhone.includes(searchQuery));

    const matchesNetwork = selectedNetworkFilter === 'ALL' || sale.networkId === selectedNetworkFilter;

    return matchesSearch && matchesNetwork;
  });

  const totalSalesAmount = filteredSales.reduce((acc, s) => acc + s.totalAmount, 0);
  const totalCostAmount = filteredSales.reduce((acc, s) => acc + s.totalCost, 0);
  const totalProfitAmount = totalSalesAmount - totalCostAmount;

  const handleExportCSV = () => {
    if (salesHistory.length === 0) {
      showAlert('لا توجد مبيعات لتصديرها', 'warning');
      return;
    }

    const headers = "رقم العملية,الشبكة,الباقة,الكمية,المبلغ,التكلفة,الربح,كود الكرت,التاريخ,المصدر\n";
    const rows = salesHistory.map(s => 
      `"${s.id}","${s.networkName}","${s.packageName}",${s.quantity},${s.totalAmount},${s.totalCost},${s.totalAmount - s.totalCost},"${s.voucherPin}","${new Date(s.timestamp).toLocaleString('ar-YE')}","${s.paymentSource}"`
    ).join("\n");

    const blob = new Blob(["\uFEFF" + headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `CardBox_Sales_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showAlert('تم تصدير ملف المبيعات (CSV) بنجاح', 'success');
  };

  return (
    <div className="space-y-6 pb-20">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <History className="w-6 h-6 text-blue-400" />
            <span>سجل المبيعات والكروت</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            استعراض المبيعات، إعادة طباعة الإيصالات، وحساب الأرباح المحققة
          </p>
        </div>

        <button
          onClick={handleExportCSV}
          className="bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 px-4 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all self-start sm:self-auto"
        >
          <Download className="w-4 h-4" />
          <span>تصدير تقرير Excel / CSV</span>
        </button>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-850 border border-slate-800 rounded-2xl p-4">
          <div className="text-xs text-slate-400 mb-1">إجمالي المبيعات المعروضة</div>
          <div className="text-xl font-black text-white font-mono">
            {totalSalesAmount.toLocaleString()} <span className="text-xs text-slate-400">ر.ي</span>
          </div>
        </div>

        <div className="bg-slate-850 border border-slate-800 rounded-2xl p-4">
          <div className="text-xs text-slate-400 mb-1">إجمالي التكلفة</div>
          <div className="text-xl font-black text-slate-300 font-mono">
            {totalCostAmount.toLocaleString()} <span className="text-xs text-slate-400">ر.ي</span>
          </div>
        </div>

        <div className="bg-emerald-950/40 border border-emerald-500/30 rounded-2xl p-4">
          <div className="text-xs text-emerald-400 font-bold mb-1 flex items-center gap-1">
            <TrendingUp className="w-3.5 h-3.5" />
            <span>صافي الأرباح</span>
          </div>
          <div className="text-xl font-black text-emerald-400 font-mono">
            +{totalProfitAmount.toLocaleString()} <span className="text-xs text-emerald-300">ر.ي</span>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div className="relative">
          <Search className="w-5 h-5 text-slate-400 absolute right-4 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="ابحث بكود الكرت، رقم الفاتورة، أو الباقة..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-850 border border-slate-700/80 rounded-2xl py-3 pr-12 pl-4 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        <select
          value={selectedNetworkFilter}
          onChange={(e) => setSelectedNetworkFilter(e.target.value)}
          className="bg-slate-850 border border-slate-700/80 rounded-2xl py-3 px-4 text-xs text-white focus:outline-none focus:border-blue-500 transition-colors"
        >
          <option value="ALL">جميع الشبكات</option>
          {networks.map(n => (
            <option key={n.id} value={n.id}>{n.name}</option>
          ))}
        </select>
      </div>

      {/* Sales List */}
      <div className="space-y-3">
        {filteredSales.length === 0 ? (
          <div className="bg-slate-850 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 text-xs">
            لا توجد مبيعات مطابقة لمعايير البحث
          </div>
        ) : (
          filteredSales.map((sale) => (
            <div
              key={sale.id}
              onClick={() => onSelectReceipt(sale)}
              className="bg-slate-850 hover:bg-slate-800 border border-slate-800 hover:border-slate-700 rounded-2xl p-4 transition-all cursor-pointer shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-4"
            >
              <div className="flex items-start sm:items-center gap-3">
                <div className="w-12 h-12 rounded-2xl bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400 shrink-0">
                  <Printer className="w-6 h-6" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-sm text-white">{sale.packageName}</h3>
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-mono">
                      {sale.id}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-2 text-xs text-slate-400 mt-1">
                    <span className="text-slate-300 font-semibold">{sale.networkName}</span>
                    <span>•</span>
                    <span className="text-emerald-400 font-mono font-bold bg-emerald-500/10 px-2 py-0.5 rounded">
                      كود: {sale.voucherPin}
                    </span>
                  </div>
                </div>
              </div>

              {/* Price and Action */}
              <div className="flex items-center justify-between sm:justify-end gap-6 border-t sm:border-t-0 pt-3 sm:pt-0 border-slate-800">
                <div className="text-right sm:text-left">
                  <div className="font-black text-base text-white font-mono">
                    {sale.totalAmount.toLocaleString()} ر.ي
                  </div>
                  <div className="text-[11px] text-emerald-400 font-medium">
                    ربح: +{(sale.totalAmount - sale.totalCost).toLocaleString()} ر.ي
                  </div>
                  <div className="text-[10px] text-slate-500 mt-0.5">
                    {new Date(sale.timestamp).toLocaleString('ar-YE')}
                  </div>
                </div>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onSelectReceipt(sale);
                  }}
                  className="bg-blue-600/20 hover:bg-blue-600 border border-blue-500/40 text-blue-300 hover:text-white px-3 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5"
                >
                  <Printer className="w-3.5 h-3.5" />
                  <span>طباعة</span>
                </button>
              </div>
            </div>
          ))
        )}
      </div>

    </div>
  );
};
