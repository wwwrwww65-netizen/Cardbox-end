import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput, Alert } from 'react-native';
import { usePos, eWalletOptions } from '../context/PosContext';
import { colors } from '../theme/colors';

export const WalletScreen: React.FC = () => {
  const { user, walletTransactions, topUpWallet } = usePos();
  const [selectedWallet, setSelectedWallet] = useState(eWalletOptions[0]);
  const [amount, setAmount] = useState('50000');
  const [refNum, setRefNum] = useState('');

  const handleTopUp = () => {
    const val = parseFloat(amount);
    if (!val || val < 1000) {
      Alert.alert('خطأ', 'أدخل مبلغاً صحيحاً');
      return;
    }
    if (!refNum.trim()) {
      Alert.alert('تنبيه', 'أدخل رقم الحوالة أو المرجع');
      return;
    }
    topUpWallet(val, selectedWallet.arabicName, refNum.trim());
    setRefNum('');
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>محفظة CardBox الرقمية</Text>

      <View style={styles.heroCard}>
        <Text style={styles.walletLabel}>الرصيد الكلي المتاح</Text>
        <Text style={styles.walletAmount}>{user.walletBalance.toLocaleString()} ر.ي</Text>
      </View>

      <Text style={styles.sectionTitle}>تغذية الرصيد عبر البنوك والمحافظ:</Text>
      <View style={styles.walletOptions}>
        {eWalletOptions.map(w => (
          <TouchableOpacity 
            key={w.id} 
            style={[styles.optCard, selectedWallet.id === w.id && styles.optCardActive]}
            onPress={() => setSelectedWallet(w)}
          >
            <Text style={styles.optTitle}>{w.arabicName}</Text>
            <Text style={styles.optAcc}>الحساب: {w.accountNumber}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <View style={styles.formCard}>
        <Text style={styles.formTitle}>تأكيد إيداع رصيد:</Text>
        <Text style={styles.label}>المبلغ بالريال اليمني:</Text>
        <TextInput 
          style={styles.input} 
          keyboardType="numeric" 
          value={amount} 
          onChangeText={setAmount} 
        />

        <Text style={styles.label}>رقم الحوالة / العملية:</Text>
        <TextInput 
          style={styles.input} 
          placeholder="مثال: KR-998231" 
          placeholderTextColor="#64748B"
          value={refNum} 
          onChangeText={setRefNum} 
        />

        <TouchableOpacity style={styles.submitBtn} onPress={handleTopUp}>
          <Text style={styles.submitBtnText}>إرسال إشعار الإيداع للتأكيد</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.textWhite, marginBottom: 16 },
  heroCard: { backgroundColor: '#064E3B', padding: 20, borderRadius: 20, borderWidth: 1, borderColor: colors.accent, marginBottom: 20 },
  walletLabel: { color: '#A7F3D0', fontSize: 12 },
  walletAmount: { color: '#FFFFFF', fontSize: 26, fontWeight: 'bold', marginTop: 4 },
  sectionTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 15, marginBottom: 10 },
  walletOptions: { marginBottom: 16 },
  optCard: { backgroundColor: colors.bgCard, padding: 12, borderRadius: 12, marginBottom: 8, borderWidth: 1, borderColor: colors.border },
  optCardActive: { borderColor: '#3B82F6', borderWidth: 2 },
  optTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 13 },
  optAcc: { color: colors.textMuted, fontSize: 11, marginTop: 2 },
  formCard: { backgroundColor: colors.bgCard, padding: 16, borderRadius: 18, borderWidth: 1, borderColor: colors.border },
  formTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 15, marginBottom: 12 },
  label: { color: colors.textMuted, fontSize: 12, marginBottom: 6 },
  input: { backgroundColor: colors.bgDark, color: colors.textWhite, padding: 12, borderRadius: 12, marginBottom: 12, borderWidth: 1, borderColor: colors.border },
  submitBtn: { backgroundColor: colors.accent, padding: 14, borderRadius: 14, alignItems: 'center', marginTop: 6 },
  submitBtnText: { color: '#0F172A', fontWeight: 'bold', fontSize: 14 }
});
