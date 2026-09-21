import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';
import { OrderTransaction } from '../types';

interface SalesHistoryScreenProps {
  onSelectReceipt: (order: OrderTransaction) => void;
}

export const SalesHistoryScreen: React.FC<SalesHistoryScreenProps> = ({ onSelectReceipt }) => {
  const { salesHistory } = usePos();
  const [search, setSearch] = useState('');

  const filtered = salesHistory.filter(s => 
    s.packageName.includes(search) || s.voucherPin.includes(search) || s.networkName.includes(search)
  );

  const total = filtered.reduce((a, b) => a + b.totalAmount, 0);
  const profit = filtered.reduce((a, b) => a + (b.totalAmount - b.totalCost), 0);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>سجل المبيعات والكروت</Text>

      <View style={styles.statsRow}>
        <View style={styles.statBox}>
          <Text style={styles.statLabel}>إجمالي المبيعات</Text>
          <Text style={styles.statValue}>{total.toLocaleString()} ر.ي</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statLabel}>صافي الربح</Text>
          <Text style={[styles.statValue, { color: colors.accent }]}>+{profit.toLocaleString()} ر.ي</Text>
        </View>
      </View>

      <TextInput
        style={styles.searchInput}
        placeholder="ابحث بكود الكرت أو الباقة..."
        placeholderTextColor="#64748B"
        value={search}
        onChangeText={setSearch}
      />

      {filtered.map((sale) => (
        <TouchableOpacity key={sale.id} style={styles.card} onPress={() => onSelectReceipt(sale)}>
          <View style={styles.rowBetween}>
            <Text style={styles.pkgName}>{sale.packageName}</Text>
            <Text style={styles.amount}>{sale.totalAmount} ر.ي</Text>
          </View>
          <Text style={styles.netInfo}>{sale.networkName} • PIN: {sale.voucherPin}</Text>
          <View style={styles.rowBetween}>
            <Text style={styles.time}>{new Date(sale.timestamp).toLocaleString('ar-YE')}</Text>
            <Text style={styles.reprint}>إعادة طباعة 🖨️</Text>
          </View>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.textWhite, marginBottom: 16 },
  statsRow: { flexDirection: 'row', gap: 10, marginBottom: 16 },
  statBox: { flex: 1, backgroundColor: colors.bgCard, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: colors.border },
  statLabel: { color: colors.textMuted, fontSize: 11 },
  statValue: { color: colors.textWhite, fontWeight: 'bold', fontSize: 16, marginTop: 4 },
  searchInput: { backgroundColor: colors.bgCard, color: colors.textWhite, borderRadius: 14, padding: 12, marginBottom: 16, borderWidth: 1, borderColor: colors.border },
  card: { backgroundColor: colors.bgCard, borderRadius: 16, padding: 14, marginBottom: 10, borderWidth: 1, borderColor: colors.border },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  pkgName: { color: colors.textWhite, fontWeight: 'bold', fontSize: 14 },
  amount: { color: colors.textWhite, fontWeight: 'bold', fontSize: 15 },
  netInfo: { color: colors.accent, fontSize: 12, marginVertical: 4 },
  time: { color: colors.textMuted, fontSize: 10 },
  reprint: { color: '#60A5FA', fontSize: 11, fontWeight: 'bold' }
});
