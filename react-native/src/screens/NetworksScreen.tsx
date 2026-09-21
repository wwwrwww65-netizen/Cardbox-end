import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput } from 'react-native';
import { usePos } from '../context/PosContext';
import { colors } from '../theme/colors';

export const NetworksScreen: React.FC = () => {
  const { networks, setSelectedNetwork, setActiveScreen, requestJoinNetwork } = usePos();
  const [search, setSearch] = useState('');

  const filtered = networks.filter(n => 
    n.name.includes(search) || n.code.includes(search) || n.ownerName.includes(search)
  );

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>دليل شبكات الإنترنت</Text>
      
      <TextInput
        style={styles.searchInput}
        placeholder="ابحث بالاسم أو الرمز..."
        placeholderTextColor="#64748B"
        value={search}
        onChangeText={setSearch}
      />

      {filtered.map((net) => {
        const isApproved = net.status === 'APPROVED';
        return (
          <View key={net.id} style={styles.card}>
            <View style={styles.rowBetween}>
              <Text style={styles.netName}>{net.name}</Text>
              <Text style={styles.badge}>{net.code}</Text>
            </View>
            <Text style={styles.owner}>المالك: {net.ownerName} • {net.location}</Text>
            <Text style={styles.desc}>{net.description}</Text>

            {isApproved ? (
              <View style={styles.statusBox}>
                <Text style={styles.statusText}>الرصيد المتاح بالسقف: {net.currentBalance.toLocaleString()} {net.currency}</Text>
                <TouchableOpacity 
                  style={styles.actionBtn}
                  onPress={() => {
                    setSelectedNetwork(net);
                    setActiveScreen('packages');
                  }}
                >
                  <Text style={styles.actionBtnText}>فتح الباقات والبيع ←</Text>
                </TouchableOpacity>
              </View>
            ) : (
              <TouchableOpacity 
                style={[styles.actionBtn, { backgroundColor: colors.accent }]}
                onPress={() => requestJoinNetwork(net.id)}
              >
                <Text style={styles.actionBtnText}>طلب انضمام واعتماد سقف</Text>
              </TouchableOpacity>
            )}
          </View>
        );
      })}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bgDark },
  content: { padding: 16, paddingBottom: 80 },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.textWhite, marginBottom: 16 },
  searchInput: { backgroundColor: colors.bgCard, color: colors.textWhite, borderRadius: 14, padding: 12, marginBottom: 16, borderWidth: 1, borderColor: colors.border },
  card: { backgroundColor: colors.bgCard, borderRadius: 18, padding: 16, marginBottom: 12, borderWidth: 1, borderColor: colors.border },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  netName: { color: colors.textWhite, fontWeight: 'bold', fontSize: 16 },
  badge: { color: '#60A5FA', fontSize: 11, backgroundColor: '#1E3A8A', paddingHorizontal: 6, paddingVertical: 2, borderRadius: 6 },
  owner: { color: colors.textMuted, fontSize: 12, marginVertical: 4 },
  desc: { color: '#CBD5E1', fontSize: 12, marginBottom: 12 },
  statusBox: { backgroundColor: colors.bgDark, padding: 12, borderRadius: 12 },
  statusText: { color: colors.accent, fontWeight: 'bold', fontSize: 12, marginBottom: 8 },
  actionBtn: { backgroundColor: colors.primary, padding: 12, borderRadius: 12, alignItems: 'center' },
  actionBtnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 }
});
