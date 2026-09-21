import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';
import { NetworkItem, OrderTransaction } from '../types';

interface HomeScreenProps {
  onSelectReceipt: (order: OrderTransaction) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onSelectReceipt }) => {
  const { user, networks, salesHistory, setActiveScreen, setSelectedNetwork, togglePinNetwork } = usePos();

  const joinedNetworks = networks.filter(n => n.status === 'APPROVED');
  const todaySales = salesHistory.filter(s => new Date(s.timestamp).toDateString() === new Date().toDateString());
  const todayRevenue = todaySales.reduce((acc, s) => acc + s.totalAmount, 0);
  const todayProfit = todaySales.reduce((acc, s) => acc + (s.totalAmount - s.totalCost), 0);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      
      {/* Hero Store & Wallet Header */}
      <View style={styles.heroCard}>
        <Text style={styles.badgeText}>لوحة تحكم نقاط البيع</Text>
        <Text style={styles.storeName}>{user.storeName}</Text>
        <Text style={styles.storeLocation}>📍 {user.location}</Text>

        <View style={styles.walletBox}>
          <Text style={styles.walletLabel}>رصيد محفظة CardBox</Text>
          <Text style={styles.walletAmount}>
            {user.walletBalance.toLocaleString()} <Text style={styles.currency}>ر.ي</Text>
          </Text>
        </View>

        <View style={styles.statsRow}>
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>مبيعات اليوم</Text>
            <Text style={styles.statValue}>{todayRevenue.toLocaleString()} ر.ي</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>أرباح اليوم</Text>
            <Text style={[styles.statValue, { color: colors.accent }]}>+{todayProfit.toLocaleString()} ر.ي</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>الكروت</Text>
            <Text style={styles.statValue}>{todaySales.length} كرت</Text>
          </View>
        </View>
      </View>

      {/* Networks Section */}
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>الشبكات المعتمدة لنقطتك</Text>
        <TouchableOpacity onPress={() => setActiveScreen('networks')}>
          <Text style={styles.viewAllText}>كل الشبكات ←</Text>
        </TouchableOpacity>
      </View>

      {joinedNetworks.map((net) => (
        <View key={net.id} style={styles.networkCard}>
          <View style={styles.netHeader}>
            <Text style={styles.netName}>{net.name}</Text>
            <Text style={styles.netCode}>{net.code}</Text>
          </View>
          <Text style={styles.netOwner}>المالك: {net.ownerName}</Text>
          
          <View style={styles.balanceRow}>
            <Text style={styles.balanceLabel}>الرصيد المتاح بالسقف:</Text>
            <Text style={styles.balanceValue}>{net.currentBalance.toLocaleString()} {net.currency}</Text>
          </View>

          <TouchableOpacity 
            style={styles.sellBtn}
            onPress={() => {
              setSelectedNetwork(net);
              setActiveScreen('packages');
            }}
          >
            <Text style={styles.sellBtnText}>بيع كروت هذه الشبكة</Text>
          </TouchableOpacity>
        </View>
      ))}

      {/* Recent Sales */}
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>آخر المبيعات</Text>
        <TouchableOpacity onPress={() => setActiveScreen('sales')}>
          <Text style={styles.viewAllText}>سجل المبيعات ←</Text>
        </TouchableOpacity>
      </View>

      {salesHistory.slice(0, 3).map((sale) => (
        <TouchableOpacity key={sale.id} style={styles.saleItem} onPress={() => onSelectReceipt(sale)}>
          <View>
            <Text style={styles.salePkg}>{sale.packageName}</Text>
            <Text style={styles.saleNet}>{sale.networkName} • كود: {sale.voucherPin}</Text>
          </View>
          <View style={{ alignItems: 'flex-end' }}>
            <Text style={styles.saleAmount}>{sale.totalAmount} ر.ي</Text>
            <Text style={styles.saleTime}>{new Date(sale.timestamp).toLocaleTimeString('ar-YE')}</Text>
          </View>
        </TouchableOpacity>
      ))}

    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  heroCard: { backgroundColor: colors.bgCard, borderRadius: 24, padding: 20, borderWidth: 1, borderColor: colors.border, marginBottom: 20 },
  badgeText: { color: '#60A5FA', fontSize: 11, fontWeight: 'bold' },
  storeName: { color: colors.textWhite, fontSize: 20, fontWeight: 'bold', marginVertical: 4 },
  storeLocation: { color: colors.textMuted, fontSize: 12, marginBottom: 16 },
  walletBox: { backgroundColor: colors.bgDark, padding: 14, borderRadius: 16, borderWidth: 1, borderColor: colors.border },
  walletLabel: { color: colors.textMuted, fontSize: 11 },
  walletAmount: { color: colors.accent, fontSize: 24, fontWeight: 'bold', marginTop: 2 },
  currency: { fontSize: 14, color: colors.textMuted },
  statsRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 16, paddingTop: 14, borderTopWidth: 1, borderColor: colors.border },
  statItem: { alignItems: 'center' },
  statLabel: { color: colors.textMuted, fontSize: 10 },
  statValue: { color: colors.textWhite, fontSize: 13, fontWeight: 'bold', marginTop: 2 },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginVertical: 12 },
  sectionTitle: { color: colors.textWhite, fontSize: 16, fontWeight: 'bold' },
  viewAllText: { color: '#60A5FA', fontSize: 12 },
  networkCard: { backgroundColor: colors.bgCard, padding: 16, borderRadius: 18, marginBottom: 12, borderWidth: 1, borderColor: colors.border },
  netHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  netName: { color: colors.textWhite, fontSize: 16, fontWeight: 'bold' },
  netCode: { color: '#60A5FA', fontSize: 11, backgroundColor: '#1E3A8A', paddingHorizontal: 6, paddingVertical: 2, borderRadius: 6 },
  netOwner: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  balanceRow: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 12, backgroundColor: colors.bgDark, padding: 10, borderRadius: 10 },
  balanceLabel: { color: colors.textMuted, fontSize: 12 },
  balanceValue: { color: colors.accent, fontWeight: 'bold', fontSize: 13 },
  sellBtn: { backgroundColor: colors.primary, padding: 12, borderRadius: 12, alignItems: 'center' },
  sellBtnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 },
  saleItem: { backgroundColor: colors.bgCard, padding: 14, borderRadius: 14, flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8, borderWidth: 1, borderColor: colors.border },
  salePkg: { color: colors.textWhite, fontWeight: 'bold', fontSize: 13 },
  saleNet: { color: colors.textMuted, fontSize: 11, marginTop: 2 },
  saleAmount: { color: colors.textWhite, fontWeight: 'bold', fontSize: 14 },
  saleTime: { color: colors.textMuted, fontSize: 10, marginTop: 2 }
});
