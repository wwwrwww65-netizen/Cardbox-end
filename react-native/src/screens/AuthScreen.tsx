import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';

export const AuthScreen: React.FC = () => {
  const { login } = usePos();
  const [storeName, setStoreName] = useState('بصمة العصر الحديث للاتصالات');
  const [phone, setPhone] = useState('777889900');
  const [location, setLocation] = useState('صنعاء - شارع تعز');

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.logoBox}>
        <Text style={styles.logoText}>CB</Text>
      </View>
      <Text style={styles.title}>CardBox POS</Text>
      <Text style={styles.subtitle}>نظام نقاط البيع وإصدار كروت الإنترنت</Text>

      <View style={styles.card}>
        <Text style={styles.label}>اسم نقطة البيع / المتجر:</Text>
        <TextInput style={styles.input} value={storeName} onChangeText={setStoreName} />

        <Text style={styles.label}>رقم هاتف الاتصال:</Text>
        <TextInput style={styles.input} keyboardType="phone-pad" value={phone} onChangeText={setPhone} />

        <Text style={styles.label}>الموقع والمدينة:</Text>
        <TextInput style={styles.input} value={location} onChangeText={setLocation} />

        <TouchableOpacity style={styles.btn} onPress={() => login(storeName, phone, location)}>
          <Text style={styles.btnText}>دخول لنظام نقاط البيع</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 24, justifyContent: 'center', minHeight: '100%' },
  logoBox: { width: 64, height: 64, borderRadius: 20, backgroundColor: colors.primary, alignSelf: 'center', justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  logoText: { color: '#FFFFFF', fontSize: 24, fontWeight: 'bold' },
  title: { fontSize: 24, fontWeight: 'bold', color: colors.textWhite, textAlign: 'center' },
  subtitle: { fontSize: 12, color: colors.textMuted, textAlign: 'center', marginBottom: 24, marginTop: 4 },
  card: { backgroundColor: colors.bgCard, padding: 20, borderRadius: 24, borderWidth: 1, borderColor: colors.border },
  label: { color: colors.textMuted, fontSize: 12, marginBottom: 6 },
  input: { backgroundColor: colors.bgDark, color: colors.textWhite, padding: 12, borderRadius: 12, marginBottom: 14, borderWidth: 1, borderColor: colors.border },
  btn: { backgroundColor: colors.primary, padding: 14, borderRadius: 14, alignItems: 'center', marginTop: 8 },
  btnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 15 }
});
