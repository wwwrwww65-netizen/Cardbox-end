import React, { useState } from 'react';
import { SafeAreaView, StatusBar, View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { PosProvider, usePos } from './src/context/PosContext';
import { HomeScreen } from './src/screens/HomeScreen';
import { NetworksScreen } from './src/screens/NetworksScreen';
import { PackagesScreen } from './src/screens/PackagesScreen';
import { SalesHistoryScreen } from './src/screens/SalesHistoryScreen';
import { WalletScreen } from './src/screens/WalletScreen';
import { PrinterSettingsScreen } from './src/screens/PrinterSettingsScreen';
import { AuthScreen } from './src/screens/AuthScreen';
import { ReceiptModal } from './src/components/ReceiptModal';
import { colors } from './src/theme/colors';
import { OrderTransaction } from './src/types';

const MainNavigation = () => {
  const { user, activeScreen, setActiveScreen, printerSettings } = usePos();
  const [selectedReceipt, setSelectedReceipt] = useState<OrderTransaction | null>(null);

  if (!user.isLoggedIn) {
    return <AuthScreen />;
  }

  const renderScreen = () => {
    switch (activeScreen) {
      case 'home':
        return <HomeScreen onSelectReceipt={setSelectedReceipt} />;
      case 'networks':
        return <NetworksScreen />;
      case 'packages':
        return <PackagesScreen onOpenReceipt={setSelectedReceipt} />;
      case 'sales':
        return <SalesHistoryScreen onSelectReceipt={setSelectedReceipt} />;
      case 'wallet':
        return <WalletScreen />;
      case 'printer':
        return <PrinterSettingsScreen />;
      default:
        return <HomeScreen onSelectReceipt={setSelectedReceipt} />;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={colors.bgDark} />

      {/* Top Navbar */}
      <View style={styles.topBar}>
        <Text style={styles.logoTitle}>CardBox POS</Text>
        <TouchableOpacity 
          style={styles.printerBtn} 
          onPress={() => setActiveScreen('printer')}
        >
          <Text style={styles.printerBtnText}>🖨️ الطابعة</Text>
        </TouchableOpacity>
      </View>

      {/* Main Body */}
      <View style={styles.body}>
        {renderScreen()}
      </View>

      {/* Bottom Nav Bar */}
      <View style={styles.bottomNav}>
        {[
          { id: 'home', label: 'الرئيسية', icon: '🏠' },
          { id: 'networks', label: 'الشبكات', icon: '📡' },
          { id: 'sales', label: 'المبيعات', icon: '📜' },
          { id: 'wallet', label: 'المحفظة', icon: '💳' },
        ].map((item) => {
          const isActive = activeScreen === item.id;
          return (
            <TouchableOpacity 
              key={item.id} 
              style={[styles.navItem, isActive && styles.navItemActive]}
              onPress={() => setActiveScreen(item.id)}
            >
              <Text style={styles.navIcon}>{item.icon}</Text>
              <Text style={[styles.navLabel, isActive && styles.navLabelActive]}>{item.label}</Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Receipt Modal */}
      <ReceiptModal 
        order={selectedReceipt} 
        onClose={() => setSelectedReceipt(null)} 
        printerSettings={printerSettings} 
      />
    </SafeAreaView>
  );
};

export default function App() {
  return (
    <PosProvider>
      <MainNavigation />
    </PosProvider>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 12, backgroundColor: colors.bgCard, borderBottomWidth: 1, borderColor: colors.border },
  logoTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 18 },
  printerBtn: { backgroundColor: colors.bgDark, paddingHorizontal: 10, paddingVertical: 6, borderRadius: 8, borderWidth: 1, borderColor: colors.border },
  printerBtnText: { color: colors.textWhite, fontSize: 12 },
  body: { flex: 1 },
  bottomNav: { flexDirection: 'row', justifyContent: 'space-around', backgroundColor: colors.bgCard, borderTopWidth: 1, borderColor: colors.border, paddingVertical: 8 },
  navItem: { alignItems: 'center', paddingHorizontal: 12, paddingVertical: 4 },
  navItemActive: { borderRadius: 8 },
  navIcon: { fontSize: 18 },
  navLabel: { color: colors.textMuted, fontSize: 11, marginTop: 2 },
  navLabelActive: { color: '#60A5FA', fontWeight: 'bold' }
});
