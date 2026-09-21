import React from 'react';
import { usePos } from '../context/PosContext';
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';

export const TopAlertBanner: React.FC = () => {
  const { activeAlert } = usePos();

  if (!activeAlert) return null;

  const getIcon = () => {
    switch (activeAlert.type) {
      case 'success':
        return <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />;
      case 'error':
        return <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />;
      default:
        return <Info className="w-5 h-5 text-blue-400 shrink-0" />;
    }
  };

  const getBgClass = () => {
    switch (activeAlert.type) {
      case 'success':
        return 'bg-emerald-950/90 border-emerald-500/40 text-emerald-200';
      case 'error':
        return 'bg-rose-950/90 border-rose-500/40 text-rose-200';
      case 'warning':
        return 'bg-amber-950/90 border-amber-500/40 text-amber-200';
      default:
        return 'bg-blue-950/90 border-blue-500/40 text-blue-200';
    }
  };

  return (
    <div className="fixed top-4 left-4 right-4 z-50 max-w-lg mx-auto animate-in fade-in slide-in-from-top-4 duration-300">
      <div className={`flex items-center gap-3 p-3.5 rounded-2xl border shadow-xl backdrop-blur-md ${getBgClass()}`}>
        {getIcon()}
        <p className="text-sm font-medium flex-1 leading-relaxed">
          {activeAlert.message}
        </p>
      </div>
    </div>
  );
};
