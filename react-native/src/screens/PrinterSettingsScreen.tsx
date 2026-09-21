import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Switch, TextInput } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';

export const PrinterSettingsScreen: React.FC = () => {
  const { printerSettings, updatePrinterSettings } = usePos();

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>إعدادات الطابعة الحرارية</Text>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>مقاس ورق الطباعة (Paper Width):</Text>
        <View style={styles.row}>
          <TouchableOpacity 
            style={[styles.sizeBtn, printerSettings.paperWidth === '58mm' && styles.sizeBtnActive]}
            onPress={() => updatePrinterSettings({ paperWidth: '58mm' })}
          >
            <Text style={styles.sizeText}>58mm (طابعة محمولة)</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={[styles.sizeBtn, printerSettings.paperWidth === '80mm' && styles.sizeBtnActive]}
            onPress={() => updatePrinterSettings({ paperWidth: '80mm' })}
          >
            <Text style={styles.sizeText}>80mm (طابعة كاشير)</Text>
          </TouchableOpacity>
        </View>
      </View>

      <View style={styles.card}>
        <View style={styles.switchRow}>
          <Text style={styles.switchLabel}>طباعة باركود الاستجابة السريعة (QR)</Text>
          <Switch 
            value={printerSettings.showBarcode} 
            onValueChange={(val) => updatePrinterSettings({ showBarcode: val })} 
          />
        </View>

        <View style={styles.switchRow}>
          <Text style={styles.switchLabel}>طباعة اسم المتجر بالترويسة</Text>
          <Switch 
            value={printerSettings.showStoreName} 
            onValueChange={(val) => updatePrinterSettings({ showStoreName: val })} 
          />
        </View>

        <View style={styles.switchRow}>
          <Text style={styles.switchLabel}>الطباعة التلقائية فور الإصدار</Text>
          <Switch 
            value={printerSettings.autoPrintAfterSale} 
            onValueChange={(val) => updatePrinterSettings({ autoPrintAfterSale: val })} 
          />
        </View>
      </View>

      <View style={styles.card}>
        <Text style={styles.label}>ملاحظات أسفل الفاتورة (Footer Note):</Text>
        <TextInput 
          style={styles.input} 
          value={printerSettings.customFooterNote} 
          onChangeText={(val) => updatePrinterSettings({ customFooterNote: val })} 
        />
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.textWhite, marginBottom: 16 },
  card: { backgroundColor: colors.bgCard, padding: 16, borderRadius: 18, marginBottom: 14, borderWidth: 1, borderColor: colors.border },
  cardTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 14, marginBottom: 12 },
  row: { flexDirection: 'row', gap: 10 },
  sizeBtn: { flex: 1, backgroundColor: colors.bgDark, padding: 12, borderRadius: 12, alignItems: 'center', borderWidth: 1, borderColor: colors.border },
  sizeBtnActive: { borderColor: '#3B82F6', backgroundColor: '#1E3A8A' },
  sizeText: { color: colors.textWhite, fontSize: 12, fontWeight: 'bold' },
  switchRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8 },
  switchLabel: { color: colors.textWhite, fontSize: 13 },
  label: { color: colors.textMuted, fontSize: 12, marginBottom: 6 },
  input: { backgroundColor: colors.bgDark, color: colors.textWhite, padding: 12, borderRadius: 12, borderWidth: 1, borderColor: colors.border }
});
