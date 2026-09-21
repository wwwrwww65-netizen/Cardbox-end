import React, { useState } from 'react';
import { PosProvider, usePos } from './context/PosContext';
import { Navbar } from './components/Navbar';
import { PosBottomBar } from './components/PosBottomBar';
import { TopAlertBanner } from './components/TopAlertBanner';
import { ReceiptModal } from './components/ReceiptModal';
import { HomeScreen } from './screens/HomeScreen';
import { NetworksScreen } from './screens/NetworksScreen';
import { PackagesScreen } from './screens/PackagesScreen';
import { SalesHistoryScreen } from './screens/SalesHistoryScreen';
import { WalletScreen } from './screens/WalletScreen';
import { WalletTopUpScreen } from './screens/WalletTopUpScreen';
import { PrinterSettingsScreen } from './screens/PrinterSettingsScreen';
import { NotificationsScreen } from './screens/NotificationsScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { AuthScreen } from './screens/AuthScreen';
import { OrderTransaction } from './types';

const MainAppContent: React.FC = () => {
  const { user, activeScreen, printerSettings } = usePos();
  const [selectedReceiptOrder, setSelectedReceiptOrder] = useState<OrderTransaction | null>(null);

  if (!user.isLoggedIn) {
    return <AuthScreen />;
  }

  const renderScreen = () => {
    switch (activeScreen) {
      case 'home':
        return <HomeScreen onSelectReceipt={setSelectedReceiptOrder} />;
      case 'networks':
        return <NetworksScreen />;
      case 'packages':
        return <PackagesScreen onOpenReceipt={setSelectedReceiptOrder} />;
      case 'sales':
        return <SalesHistoryScreen onSelectReceipt={setSelectedReceiptOrder} />;
      case 'wallet':
        return <WalletScreen />;
      case 'wallet-topup':
        return <WalletTopUpScreen />;
      case 'printer':
        return <PrinterSettingsScreen />;
      case 'notifications':
        return <NotificationsScreen />;
      case 'profile':
        return <ProfileScreen />;
      default:
        return <HomeScreen onSelectReceipt={setSelectedReceiptOrder} />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col text-slate-100 font-sans" dir="rtl">
      <TopAlertBanner />
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 md:p-8">
        {renderScreen()}
      </main>

      <PosBottomBar />

      <ReceiptModal
        order={selectedReceiptOrder}
        onClose={() => setSelectedReceiptOrder(null)}
        printerSettings={printerSettings}
      />
    </div>
  );
};

export function App() {
  return (
    <PosProvider>
      <MainAppContent />
    </PosProvider>
  );
}

export default App;
