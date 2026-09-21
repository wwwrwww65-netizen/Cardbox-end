import React from 'react';
import { usePos } from '../context/PosContext';
import { 
  Bell, 
  CheckCheck, 
  Wifi, 
  Wallet, 
  Info, 
  CheckCircle2, 
  Clock 
} from 'lucide-react';
import { NotificationType } from '../types';

export const NotificationsScreen: React.FC = () => {
  const { notifications, markNotificationRead, markAllNotificationsRead } = usePos();

  const getNotifIcon = (type: NotificationType) => {
    switch (type) {
      case 'NETWORK_JOIN_APPROVED':
      case 'NETWORK_CREDIT_GRANTED':
        return <Wifi className="w-5 h-5 text-blue-400" />;
      case 'WALLET_TOPUP_SUCCESS':
      case 'WALLET_LOW_BALANCE':
        return <Wallet className="w-5 h-5 text-emerald-400" />;
      default:
        return <Info className="w-5 h-5 text-amber-400" />;
    }
  };

  return (
    <div className="space-y-6 pb-20 max-w-3xl mx-auto">
      
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Bell className="w-6 h-6 text-blue-400" />
            <span>مركز الإشعارات والتنبيهات</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            إشعارات الموافقة على الشبكات وتغذية المحفظة والعمليات المالية
          </p>
        </div>

        {notifications.some(n => !n.isRead) && (
          <button
            onClick={markAllNotificationsRead}
            className="text-xs font-bold text-blue-400 hover:text-blue-300 flex items-center gap-1 bg-slate-850 px-3 py-2 rounded-xl border border-slate-700 transition-colors"
          >
            <CheckCheck className="w-4 h-4" />
            <span>قراءة الكل</span>
          </button>
        )}
      </div>

      {/* List */}
      <div className="space-y-3">
        {notifications.length === 0 ? (
          <div className="bg-slate-850 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 text-xs">
            لا توجد إشعارات حالياً
          </div>
        ) : (
          notifications.map((notif) => (
            <div
              key={notif.id}
              onClick={() => markNotificationRead(notif.id)}
              className={`p-4 rounded-2xl border transition-all cursor-pointer flex items-start gap-4 ${
                notif.isRead
                  ? 'bg-slate-850/60 border-slate-800'
                  : 'bg-slate-850 border-blue-500/40 shadow-lg'
              }`}
            >
              <div className="w-10 h-10 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-center shrink-0">
                {getNotifIcon(notif.type)}
              </div>

              <div className="flex-1">
                <div className="flex items-center justify-between gap-2">
                  <h3 className={`text-sm ${notif.isRead ? 'font-bold text-slate-300' : 'font-black text-white'}`}>
                    {notif.title}
                  </h3>
                  {!notif.isRead && (
                    <span className="w-2 h-2 rounded-full bg-blue-500 shrink-0" />
                  )}
                </div>

                <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                  {notif.message}
                </p>

                <div className="text-[10px] text-slate-500 mt-2 flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  <span>{new Date(notif.timestamp).toLocaleString('ar-YE')}</span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

    </div>
  );
};
