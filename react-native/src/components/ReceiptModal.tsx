import React from 'react';
import { View, Text, Modal, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import * as Print from 'expo-print';
import * as Sharing from 'expo-sharing';
import { OrderTransaction, PrinterSettings } from '../types';
import { colors } from '../theme/colors';

interface ReceiptModalProps {
  order: OrderTransaction | null;
  onClose: () => void;
  printerSettings: PrinterSettings;
}

export const ReceiptModal: React.FC<ReceiptModalProps> = ({ order, onClose, printerSettings }) => {
  if (!order) return null;

  const handlePrint = async () => {
    const html = `
      <html dir="rtl">
        <head>
          <style>
            body { font-family: monospace; text-align: center; padding: 20px; width: ${printerSettings.paperWidth === '58mm' ? '240px' : '300px'}; margin: auto; }
            .pin { font-size: 24px; font-weight: bold; border: 2px dashed #000; padding: 10px; margin: 10px 0; }
            .row { display: flex; justify-content: space-between; font-size: 12px; margin: 4px 0; }
          </style>
        </head>
        <body>
          <h3>${order.posStoreName}</h3>
          <h4>${order.networkName} - ${order.packageName}</h4>
          <div class="pin">${order.voucherPin}</div>
          <div class="row"><span>المدة:</span><span>${order.duration || ''}</span></div>
          <div class="row"><span>البيانات:</span><span>${order.dataQuota || ''}</span></div>
          <div class="row"><span>السعر:</span><span>${order.totalAmount} ر.ي</span></div>
          <p style="font-size: 10px; margin-top: 15px;">${printerSettings.customFooterNote}</p>
        </body>
      </html>
    `;
    await Print.printAsync({ html });
  };

  return (
    <Modal visible={!!order} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.container}>
          <View style={styles.header}>
            <Text style={styles.headerTitle}>إيصال الكرت الحراري</Text>
            <TouchableOpacity onPress={onClose} style={styles.closeBtn}>
              <Text style={styles.closeText}>✕</Text>
            </TouchableOpacity>
          </View>

          <ScrollView contentContainerStyle={styles.receiptPaper}>
            <Text style={styles.storeName}>{order.posStoreName}</Text>
            <Text style={styles.networkName}>{order.networkName}</Text>
            <Text style={styles.packageName}>{order.packageName}</Text>

            <View style={styles.pinBox}>
              <Text style={styles.pinLabel}>رمز الكرت (PIN)</Text>
              <Text style={styles.pinCode}>{order.voucherPin}</Text>
            </View>

            {printerSettings.showBarcode && (
              <View style={styles.qrContainer}>
                <QRCode value={order.voucherPin} size={110} />
              </View>
            )}

            <View style={styles.specsRow}>
              <Text style={styles.specLabel}>الوقت:</Text>
              <Text style={styles.specValue}>{order.duration || 'غير محدد'}</Text>
            </View>
            <View style={styles.specsRow}>
              <Text style={styles.specLabel}>البيانات:</Text>
              <Text style={styles.specValue}>{order.dataQuota || 'غير محدد'}</Text>
            </View>
            <View style={styles.specsRow}>
              <Text style={styles.specLabel}>المبلغ:</Text>
              <Text style={styles.specValue}>{order.totalAmount} ر.ي</Text>
            </View>

            <Text style={styles.footerNote}>{printerSettings.customFooterNote}</Text>
          </ScrollView>

          <View style={styles.actionsRow}>
            <TouchableOpacity style={styles.printButton} onPress={handlePrint}>
              <Text style={styles.printButtonText}>طباعة الإيصال 🖨️</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'center', alignItems: 'center', padding: 20 },
  container: { width: '100%', maxWidth: 360, backgroundColor: colors.bgCard, borderRadius: 24, overflow: 'hidden' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16, borderBottomWidth: 1, borderColor: colors.border },
  headerTitle: { color: colors.textWhite, fontWeight: 'bold', fontSize: 16 },
  closeBtn: { padding: 4 },
  closeText: { color: colors.textMuted, fontSize: 18 },
  receiptPaper: { backgroundColor: '#FFFFFF', padding: 20, margin: 16, borderRadius: 12, alignItems: 'center' },
  storeName: { fontSize: 16, fontWeight: 'bold', color: '#000000', marginBottom: 4 },
  networkName: { fontSize: 14, color: '#0F4C81', fontWeight: 'bold' },
  packageName: { fontSize: 12, color: '#475569', marginBottom: 10 },
  pinBox: { width: '100%', backgroundColor: '#F8FAFC', borderWidth: 2, borderColor: '#000000', borderRadius: 8, padding: 10, alignItems: 'center', marginVertical: 8 },
  pinLabel: { fontSize: 10, color: '#64748B', fontWeight: 'bold' },
  pinCode: { fontSize: 22, fontWeight: 'bold', color: '#000000', letterSpacing: 3 },
  qrContainer: { marginVertical: 10 },
  specsRow: { flexDirection: 'row', justifyContent: 'space-between', width: '100%', paddingVertical: 4, borderBottomWidth: 1, borderColor: '#E2E8F0' },
  specLabel: { color: '#64748B', fontSize: 12 },
  specValue: { fontWeight: 'bold', color: '#000000', fontSize: 12 },
  footerNote: { fontSize: 10, color: '#64748B', marginTop: 12, textAlign: 'center' },
  actionsRow: { padding: 16, borderTopWidth: 1, borderColor: colors.border },
  printButton: { backgroundColor: colors.primary, padding: 14, borderRadius: 16, alignItems: 'center' },
  printButtonText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 15 }
});
