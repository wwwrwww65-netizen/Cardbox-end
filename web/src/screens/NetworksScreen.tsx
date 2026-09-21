import React, { useState } from 'react';
import { usePos } from '../context/PosContext';
import { 
  Wifi, 
  Search, 
  Plus, 
  CheckCircle2, 
  Clock, 
  MapPin, 
  ShieldCheck, 
  ArrowRight,
  Pin,
  Filter
} from 'lucide-react';
import { NetworkItem } from '../types';

export const NetworksScreen: React.FC = () => {
  const { 
    networks, 
    setSelectedNetwork, 
    setActiveScreen, 
    requestJoinNetwork, 
    togglePinNetwork 
  } = usePos();
  
  const [searchQuery, setSearchQuery] = useState('');
  const [filterTab, setFilterTab] = useState<'ALL' | 'JOINED' | 'DISCOVER'>('ALL');

  const filteredNetworks = networks.filter(net => {
    const matchesSearch = 
      net.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      net.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
      net.ownerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      net.location.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    if (filterTab === 'JOINED') return net.status === 'APPROVED';
    if (filterTab === 'DISCOVER') return net.status !== 'APPROVED';
    return true;
  });

  const handleSelectNetwork = (net: NetworkItem) => {
    if (net.status === 'APPROVED') {
      setSelectedNetwork(net);
      setActiveScreen('packages');
    }
  };

  return (
    <div className="space-y-6 pb-20">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Wifi className="w-6 h-6 text-blue-400" />
            <span>دليل شبكات الإنترنت</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            إدارة الشبكات المعتمدة، استعراض الأسقف المالية، والانضمام لشبكات جديدة
          </p>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="w-5 h-5 text-slate-400 absolute right-4 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="ابحث بالاسم، رمز الشبكة، أو المنطقة..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-850 border border-slate-700/80 rounded-2xl py-3.5 pr-12 pl-4 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        {/* Filter Tabs */}
        <div className="flex items-center gap-2 p-1 bg-slate-850 rounded-2xl border border-slate-800">
          <button
            onClick={() => setFilterTab('ALL')}
            className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all ${
              filterTab === 'ALL'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            كل الشبكات ({networks.length})
          </button>
          <button
            onClick={() => setFilterTab('JOINED')}
            className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all ${
              filterTab === 'JOINED'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            شبكاتي المعتمدة ({networks.filter(n => n.status === 'APPROVED').length})
          </button>
          <button
            onClick={() => setFilterTab('DISCOVER')}
            className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all ${
              filterTab === 'DISCOVER'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            استكشاف وانضمام ({networks.filter(n => n.status !== 'APPROVED').length})
          </button>
        </div>
      </div>

      {/* Network Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredNetworks.map((net) => {
          const isJoined = net.status === 'APPROVED';
          const isPending = net.status === 'PENDING';

          return (
            <div
              key={net.id}
              className={`relative bg-slate-850 border rounded-2xl p-5 transition-all shadow-lg flex flex-col justify-between ${
                isJoined
                  ? 'border-slate-800 hover:border-blue-500/50 cursor-pointer'
                  : 'border-slate-800/80 bg-slate-900/60'
              }`}
              onClick={() => handleSelectNetwork(net)}
            >
              <div>
                {/* Header Row */}
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-600/30 to-indigo-600/30 border border-blue-500/30 flex items-center justify-center text-blue-400 font-bold">
                      <Wifi className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-base text-white">{net.name}</h3>
                        <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-mono">
                          {net.code}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        المالك: {net.ownerName}
                      </p>
                    </div>
                  </div>

                  {isJoined && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        togglePinNetwork(net.id);
                      }}
                      className={`p-2 rounded-xl transition-colors ${
                        net.isPinned
                          ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                          : 'text-slate-500 hover:text-slate-300 bg-slate-800/50'
                      }`}
                      title={net.isPinned ? 'إلغاء التثبيت' : 'تثبيت'}
                    >
                      <Pin className="w-4 h-4" />
                    </button>
                  )}
                </div>

                <p className="text-xs text-slate-300 leading-relaxed line-clamp-2 mb-3">
                  {net.description}
                </p>

                <div className="flex items-center gap-2 text-xs text-slate-400 mb-4">
                  <MapPin className="w-3.5 h-3.5 text-slate-500" />
                  <span>{net.location}</span>
                  <span>•</span>
                  <span>{net.packagesCount} باقات متوفرة</span>
                </div>

                {/* Financial Status for Joined Networks */}
                {isJoined ? (
                  <div className="bg-slate-900/90 rounded-xl p-3.5 border border-slate-800 space-y-2">
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">الرصيد المتاح للبيع:</span>
                      <span className="font-black text-emerald-400 font-mono">
                        {net.currentBalance.toLocaleString()} {net.currency}
                      </span>
                    </div>

                    <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                      <div
                        className="bg-emerald-500 h-2 rounded-full"
                        style={{
                          width: `${Math.min(
                            100,
                            Math.max(0, (net.currentBalance / (net.financialCeiling || 1)) * 100)
                          )}%`,
                        }}
                      />
                    </div>

                    <div className="flex justify-between text-[11px] text-slate-400">
                      <span>السقف المالي المعتمد:</span>
                      <span className="font-semibold text-slate-200">
                        {net.financialCeiling.toLocaleString()} {net.currency}
                      </span>
                    </div>
                  </div>
                ) : (
                  <div className="bg-slate-900/60 rounded-xl p-3.5 border border-slate-800/60 text-xs text-slate-400 flex items-center justify-between">
                    <span>السقف المقترح للنقاط الجديدة:</span>
                    <span className="font-bold text-slate-200 font-mono">
                      {net.financialCeiling.toLocaleString()} {net.currency}
                    </span>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div className="mt-4 pt-3 border-t border-slate-800/60">
                {isJoined ? (
                  <button
                    onClick={() => handleSelectNetwork(net)}
                    className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-2.5 px-4 rounded-xl transition-all flex items-center justify-center gap-2 text-xs shadow-lg shadow-blue-600/20"
                  >
                    <span>فتح باقات الكروت والبيع</span>
                    <ArrowRight className="w-4 h-4 rotate-180" />
                  </button>
                ) : isPending ? (
                  <div className="w-full bg-amber-500/10 border border-amber-500/30 text-amber-300 font-semibold py-2.5 px-4 rounded-xl text-xs flex items-center justify-center gap-2">
                    <Clock className="w-4 h-4 animate-spin text-amber-400" />
                    <span>طلب الانضمام قيد المراجعة من مالك الشبكة</span>
                  </div>
                ) : (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      requestJoinNetwork(net.id);
                    }}
                    className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 px-4 rounded-xl transition-all flex items-center justify-center gap-2 text-xs shadow-lg shadow-emerald-600/20"
                  >
                    <Plus className="w-4 h-4" />
                    <span>إرسال طلب انضمام واعتماد سقف مالي</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
};
