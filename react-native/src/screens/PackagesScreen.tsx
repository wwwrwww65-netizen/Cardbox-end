import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';
import { VoucherPackage, OrderTransaction } from '../types';

interface PackagesScreenProps {
  onOpenReceipt: (order: OrderTransaction) => void;
}

export const PackagesScreen: React.FC<PackagesScreenProps> = ({ onOpenReceipt }) => {
  const { selectedNetwork, packages, buyVouchers, setActiveScreen } = usePos();
  const [selectedPkg, setSelectedPkg] = useState<VoucherPackage | null>(null);
  const [qty, setQty] = useState<number>(1);
  const [paymentSource, setPaymentSource] = useState<'NETWORK_CREDIT' | 'WALLET'>('NETWORK_CREDIT');
  const [phone, setPhone] = useState<string>('');

  if (!selectedNetwork) {
    return (
      <View style={styles.center}>
        <Text style={{ color: colors.textWhite, marginBottom: 12 }}>يرجى اختيار شبكة أولاً</Text>
        <TouchableOpacity style={styles.btn} onPress={() => setActiveScreen('networks')}>
          <Text style={styles.btnText}>الشبكات</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const active = selectedPkg || packages[0];
  const total = active ? active.price * qty : 0;
  const cost = active ? active.posPrice * qty : 0;
  const profit = total - cost;

  const handleBuy = () => {
    if (!active) return;
    const order = buyVouchers(selectedNetwork, active, qty, paymentSource, phone || undefined);
    if (order) {
      onOpenReceipt(order);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.netHeader}>
        <Text style={styles.netTitle}>{selectedNetwork.name}</Text>
        <Text style={styles.netSubtitle}>السقف المتاح: {selectedNetwork.currentBalance.toLocaleString()} {selectedNetwork.currency}</Text>
      </View>

      <Text style={styles.sectionTitle}>اختر باقة الكرت:</Text>
      {packages.map((pkg) => {
        const isSel = active?.id === pkg.id;
        return (
          <TouchableOpacity 
            key={pkg.id} 
            style={[styles.pkgCard, isSel && styles.pkgCardActive]}
            onPress={() => setSelectedPkg(pkg)}
          >
            <View style={styles.rowBetween}>
              <Text style={styles.pkgName}>{pkg.name}</Text>
              <Text style={styles.pkgPrice}>{pkg.price} {pkg.currency}</Text>
            </View>
            <Text style={styles.pkgSpecs}>{pkg.dataQuota} • {pkg.duration} • صلاحية {pkg.validity}</Text>
            <Text style={styles.pkgCost}>التكلفة عليك: {pkg.posPrice} {pkg.currency}</Text>
          </TouchableOpacity>
        );
      })}

      {/* Buy Form */}
      {active && (
        <View style={styles.checkoutBox}>
          <Text style={styles.checkoutTitle}>إصدار: {active.name}</Text>
          
          <View style={styles.qtyRow}>
            <Text style={styles.label}>الكمية:</Text>
            <View style={styles.qtyControls}>
              <TouchableOpacity style={styles.qtyBtn} onPress={() => setQty(Math.max(1, qty - 1))}>
                <Text style={styles.qtyBtnText}>-</Text>
              </TouchableOpacity>
              <Text style={styles.qtyText}>{qty}</Text>
              <TouchableOpacity style={styles.qtyBtn} onPress={() => setQty(qty + 1)}>
                <Text style={styles.qtyBtnText}>+</Text>
              </TouchableOpacity>
            </View>
          </View>

          <View style={styles.summaryRow}>
            <Text style={styles.sumText}>الإجمالي: {total} ر.ي</Text>
            <Text style={[styles.sumText, { color: colors.accent }]}>ربحك: +{profit} ر.ي</Text>
          </View>

          <TouchableOpacity style={styles.buyBtn} onPress={handleBuy}>
            <Text style={styles.buyBtnText}>إصدار وطباعة الكرت الآن 🖨️</Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.bgDark },
  btn: { backgroundColor: colors.primary, padding: 12, borderRadius: 12 },
  btnText: { color: '#FFFFFF', fontWeight: 'bold' },
  netHeader: { backgroundColor: colors.bgCard, padding: 16, borderRadius: 18, marginBottom: 16, borderWidth: 1, borderColor: colors.border },
  netTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 18 },
  netSubtitle: { color: colors.accent, fontSize: 12, marginTop: 4 },
  sectionTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 15, marginBottom: 12 },
  pkgCard: { backgroundColor: colors.bgCard, padding: 14, borderRadius: 16, marginBottom: 10, borderWidth: 1, borderColor: colors.border },
  pkgCardActive: { borderColor: '#3B82F6', borderWidth: 2 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between' },
  pkgName: { color: colors.textWhite, fontWeight: 'bold', fontSize: 15 },
  pkgPrice: { color: colors.textWhite, fontWeight: 'bold', fontSize: 16 },
  pkgSpecs: { color: colors.textMuted, fontSize: 12, marginVertical: 4 },
  pkgCost: { color: colors.accent, fontSize: 11, fontWeight: 'bold' },
  checkoutBox: { backgroundColor: colors.bgCard, padding: 16, borderRadius: 20, marginTop: 16, borderWidth: 2, borderColor: '#1E3A8A' },
  checkoutTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 16, marginBottom: 12 },
  qtyRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  label: { color: colors.textMuted, fontSize: 13 },
  qtyControls: { flexDirection: 'row', alignItems: 'center' },
  qtyBtn: { backgroundColor: colors.bgDark, paddingHorizontal: 14, paddingVertical: 6, borderRadius: 8, borderWidth: 1, borderColor: colors.border },
  qtyBtnText: { color: colors.textWhite, fontSize: 18, fontWeight: 'bold' },
  qtyText: { color: colors.textWhite, fontSize: 16, fontWeight: 'bold', marginHorizontal: 12 },
  summaryRow: { flexDirection: 'row', justifyContent: 'space-between', backgroundColor: colors.bgDark, padding: 10, borderRadius: 10, marginVertical: 10 },
  sumText: { color: colors.textWhite, fontWeight: 'bold', fontSize: 13 },
  buyBtn: { backgroundColor: colors.primary, padding: 14, borderRadius: 14, alignItems: 'center', marginTop: 8 },
  buyBtnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 15 }
});
