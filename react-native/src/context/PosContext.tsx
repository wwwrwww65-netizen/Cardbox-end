import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { 
  PosUser, 
  NetworkItem, 
  VoucherPackage, 
  OrderTransaction, 
  WalletTransaction, 
  AppNotification, 
  PrinterSettings,
  EWalletOption 
} from '../types';

interface PosContextType {
  user: PosUser;
  networks: NetworkItem[];
  packages: VoucherPackage[];
  salesHistory: OrderTransaction[];
  walletTransactions: WalletTransaction[];
  notifications: AppNotification[];
  printerSettings: PrinterSettings;
  activeScreen: string;
  selectedNetwork: NetworkItem | null;
  activeAlert: { message: string; type: 'success' | 'error' | 'info' | 'warning' } | null;
  
  setActiveScreen: (screen: string) => void;
  setSelectedNetwork: (net: NetworkItem | null) => void;
  showAlert: (message: string, type?: 'success' | 'error' | 'info' | 'warning') => void;
  login: (storeName: string, phone: string, location: string) => void;
  logout: () => void;
  togglePinNetwork: (networkId: string) => void;
  requestJoinNetwork: (networkId: string) => void;
  buyVouchers: (
    network: NetworkItem, 
    pkg: VoucherPackage, 
    quantity: number, 
    paymentSource: 'NETWORK_CREDIT' | 'WALLET',
    customerPhone?: string
  ) => OrderTransaction | null;
  topUpWallet: (amount: number, method: string, refNum: string) => void;
  markNotificationRead: (id: string) => void;
  markAllNotificationsRead: () => void;
  updatePrinterSettings: (settings: Partial<PrinterSettings>) => void;
}

const defaultUser: PosUser = {
  storeName: "بصمة العصر الحديث للاتصالات",
  phone: "777889900",
  location: "صنعاء - شارع تعز",
  isLoggedIn: true,
  walletBalance: 125000.0,
};

const initialNetworks: NetworkItem[] = [
  {
    id: "net_1",
    code: "CB-SAN-01",
    name: "شبكة الأمل المركزية",
    ownerName: "م. عادل الشميري",
    financialCeiling: 150000.0,
    currentBalance: 94500.0,
    currency: "ريال",
    status: "APPROVED",
    location: "صنعاء - شارع الستين الغربي",
    packagesCount: 6,
    description: "تغطية ميكروتك عالية السرعة متصلة بالألياف الضوئية",
    isPinned: true
  },
  {
    id: "net_2",
    code: "CB-SAN-02",
    name: "شبكة الواي فاي الذهبي",
    ownerName: "أ. ماجد الريمي",
    financialCeiling: 80000.0,
    currentBalance: 42000.0,
    currency: "ريال",
    status: "APPROVED",
    location: "صنعاء - حدة المدينة",
    packagesCount: 5,
    description: "إنترنت كروت مسبقة الدفع بدون تقطيع",
    isPinned: true
  },
  {
    id: "net_3",
    code: "CB-ADE-01",
    name: "شبكة النورس برو",
    ownerName: "خالد بن ثابت",
    financialCeiling: 200000.0,
    currentBalance: 185000.0,
    currency: "ريال",
    status: "APPROVED",
    location: "عدن - المنصورة",
    packagesCount: 4,
    description: "شبكة ميكروتك للأبراج الحديثة مع دعم فني 24/7",
    isPinned: false
  },
  {
    id: "net_4",
    code: "CB-TAZ-01",
    name: "شبكة قلعة القاهرة",
    ownerName: "فؤاد المخلافي",
    financialCeiling: 50000.0,
    currentBalance: 12000.0,
    currency: "ريال",
    status: "APPROVED",
    location: "تعز - شارع جمال",
    packagesCount: 4,
    description: "كروت إنترنت بأسعار منافسة وسرعات تيربو",
    isPinned: false
  },
  {
    id: "net_5",
    code: "CB-IBB-01",
    name: "شبكة اللواء الأخضر",
    ownerName: "صالح الحبيشي",
    financialCeiling: 100000.0,
    currentBalance: 0.0,
    currency: "ريال",
    status: "NOT_JOINED",
    location: "إب - الدائري الغربي",
    packagesCount: 6,
    description: "تغطية ممتازة لمدينة إب وضواحيها",
    isPinned: false
  }
];

const initialPackages: VoucherPackage[] = [
  {
    id: "pkg_1",
    networkId: "net_1",
    name: "كرت 1 جيجا - سرعة كاملة",
    price: 300,
    posPrice: 270,
    currency: "ر.ي",
    duration: "4 ساعات",
    dataQuota: "1000 MB",
    validity: "5 أيام",
    colorHex: "#0F4C81",
    isAvailable: true,
    isPopular: true
  },
  {
    id: "pkg_2",
    networkId: "net_1",
    name: "كرت 2 جيجا تيربو",
    price: 500,
    posPrice: 450,
    currency: "ر.ي",
    duration: "8 ساعات",
    dataQuota: "2048 MB",
    validity: "7 أيام",
    colorHex: "#10B981",
    isAvailable: true,
    isPopular: true
  },
  {
    id: "pkg_3",
    networkId: "net_1",
    name: "كرت اقتصادي 500 ميجا",
    price: 150,
    posPrice: 135,
    currency: "ر.ي",
    duration: "2 ساعة",
    dataQuota: "500 MB",
    validity: "3 أيام",
    colorHex: "#F59E0B",
    isAvailable: true,
    isPopular: false
  },
  {
    id: "pkg_4",
    networkId: "net_1",
    name: "كرت مفتوح أسبوعي 5GB",
    price: 1000,
    posPrice: 900,
    currency: "ر.ي",
    duration: "24 ساعة",
    dataQuota: "5120 MB",
    validity: "10 أيام",
    colorHex: "#8B5CF6",
    isAvailable: true,
    isPopular: false
  }
];

export const eWalletOptions: EWalletOption[] = [
  {
    id: "kuraimi",
    name: "Kuraimi Bank",
    arabicName: "حساب الكريمي (Kuraimi)",
    accountNumber: "3004829103",
    colorHex: "#1E3A8A",
    subtitle: "إيداع أو تحويل عبر تطبيق الكريمي جوال",
    instructions: "قم بالتحويل للحساب أعلاه وأدخل رقم الحوالة في الحقل أدناه ليتم التأكيد فورا",
    accountHolderName: "شركة كارد بوكس للدفع الإلكتروني",
    accountLabel: "رقم الحساب المميز"
  },
  {
    id: "jawal",
    name: "Jawal Pay",
    arabicName: "محفظة جوال باي (Jawal)",
    accountNumber: "777889900",
    colorHex: "#0284C7",
    subtitle: "تحويل مباشر لرقم المحفظة",
    instructions: "حول المبلغ إلى رقم المحفظة وانسخ رقم العملية من إشعار الرسالة",
    accountHolderName: "كارد بوكس بوس",
    accountLabel: "رقم هاتف المحفظة"
  },
  {
    id: "floosak",
    name: "Floosak",
    arabicName: "محفظة فلوسك (Floosak)",
    accountNumber: "771239841",
    colorHex: "#059669",
    subtitle: "بنك اليمن والكويت",
    instructions: "قم بالسداد عبر رمز التاجر أو رقم الحساب الخاص بفلوسك",
    accountHolderName: "بصمة كارد بوكس",
    accountLabel: "رمز التاجر / الحساب"
  },
  {
    id: "onecash",
    name: "OneCash",
    arabicName: "محفظة ون كاش (OneCash)",
    accountNumber: "8899120",
    colorHex: "#D97706",
    subtitle: "الدفع السريع المباشر",
    instructions: "حول عبر تطبيق OneCash وأرفق رقم المرجع للتأكيد الفوري",
    accountHolderName: "CardBox POS Main",
    accountLabel: "رقم الحساب"
  }
];

const PosContext = createContext<PosContextType | undefined>(undefined);

export const PosProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<PosUser>(defaultUser);
  const [networks, setNetworks] = useState<NetworkItem[]>(initialNetworks);
  const [packages, setPackages] = useState<VoucherPackage[]>(initialPackages);
  const [salesHistory, setSalesHistory] = useState<OrderTransaction[]>([]);
  const [walletTransactions, setWalletTransactions] = useState<WalletTransaction[]>([]);
  const [notifications, setNotifications] = useState<AppNotification[]>([
    {
      id: "notif_1",
      title: "تم قبول انضمامك للشبكة بنجاح",
      message: "وافقت إدارة 'شبكة الأمل المركزية' على طلب الانضمام، ومنحتك سقفاً مالياً قدره 150,000 ريال.",
      type: "NETWORK_JOIN_APPROVED",
      timestamp: Date.now() - 1000 * 60 * 60 * 4,
      isRead: false,
      relatedEntityId: "net_1"
    }
  ]);

  const [printerSettings, setPrinterSettings] = useState<PrinterSettings>({
    printerName: 'طابعة بلوتوث حرارية 58mm (مفعلة)',
    paperWidth: '58mm',
    autoPrintAfterSale: true,
    showStoreName: true,
    showBarcode: true,
    showFooterNotes: true,
    customFooterNote: 'شكراً لزيارتكم ونتمنى لكم تصفحاً سريعاً وممتعاً',
    fontSize: 'medium'
  });

  const [activeScreen, setActiveScreen] = useState<string>('home');
  const [selectedNetwork, setSelectedNetwork] = useState<NetworkItem | null>(null);
  const [activeAlert, setActiveAlert] = useState<{ message: string; type: 'success' | 'error' | 'info' | 'warning' } | null>(null);

  // Load from AsyncStorage
  useEffect(() => {
    (async () => {
      try {
        const u = await AsyncStorage.getItem('rn_cb_user');
        if (u) setUser(JSON.parse(u));
        const n = await AsyncStorage.getItem('rn_cb_networks');
        if (n) setNetworks(JSON.parse(n));
        const s = await AsyncStorage.getItem('rn_cb_sales');
        if (s) setSalesHistory(JSON.parse(s));
      } catch (e) {}
    })();
  }, []);

  const showAlert = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
    setActiveAlert({ message, type });
    setTimeout(() => setActiveAlert(null), 4000);
  };

  const login = (storeName: string, phone: string, location: string) => {
    const updated = { ...user, storeName, phone, location, isLoggedIn: true };
    setUser(updated);
    AsyncStorage.setItem('rn_cb_user', JSON.stringify(updated));
    showAlert('تم تسجيل الدخول بنجاح', 'success');
  };

  const logout = () => {
    const updated = { ...user, isLoggedIn: false };
    setUser(updated);
    AsyncStorage.setItem('rn_cb_user', JSON.stringify(updated));
  };

  const togglePinNetwork = (networkId: string) => {
    setNetworks(prev => prev.map(net => 
      net.id === networkId ? { ...net, isPinned: !net.isPinned } : net
    ));
  };

  const requestJoinNetwork = (networkId: string) => {
    setNetworks(prev => prev.map(net => 
      net.id === networkId ? { ...net, status: 'PENDING' } : net
    ));
    showAlert('تم إرسال طلب الانضمام لمالك الشبكة بنجاح', 'success');
  };

  const buyVouchers = (
    network: NetworkItem, 
    pkg: VoucherPackage, 
    quantity: number, 
    paymentSource: 'NETWORK_CREDIT' | 'WALLET',
    customerPhone?: string
  ): OrderTransaction | null => {
    const totalCost = pkg.posPrice * quantity;
    const totalAmount = pkg.price * quantity;

    if (paymentSource === 'NETWORK_CREDIT') {
      if (network.currentBalance < totalCost) {
        showAlert(`الرصيد في سقف الشبكة (${network.currentBalance} ريال) لا يكفي`, 'error');
        return null;
      }
      setNetworks(prev => prev.map(net => 
        net.id === network.id 
          ? { ...net, currentBalance: Math.max(0, net.currentBalance - totalCost) }
          : net
      ));
    } else {
      if (user.walletBalance < totalCost) {
        showAlert(`رصيد المحفظة (${user.walletBalance} ريال) غير كافٍ`, 'error');
        return null;
      }
      setUser(prev => ({ ...prev, walletBalance: prev.walletBalance - totalCost }));
    }

    const generatedPin = Math.floor(100000000 + Math.random() * 900000000).toString();
    const newOrder: OrderTransaction = {
      id: `ORD-${Date.now().toString().slice(-6)}`,
      networkId: network.id,
      networkName: network.name,
      packageName: pkg.name,
      packagePrice: pkg.price,
      costPrice: pkg.posPrice,
      quantity,
      totalAmount,
      totalCost,
      customerPhone,
      voucherPin: generatedPin,
      timestamp: Date.now(),
      posStoreName: user.storeName,
      isPrinted: false,
      duration: pkg.duration,
      dataQuota: pkg.dataQuota,
      validity: pkg.validity,
      paymentSource
    };

    setSalesHistory(prev => [newOrder, ...prev]);
    AsyncStorage.setItem('rn_cb_sales', JSON.stringify([newOrder, ...salesHistory]));
    showAlert(`تم إصدار الكرت بنجاح! كود: ${generatedPin}`, 'success');
    return newOrder;
  };

  const topUpWallet = (amount: number, method: string, refNum: string) => {
    const newTx: WalletTransaction = {
      id: `WTX-${Date.now().toString().slice(-6)}`,
      title: `تغذية رصيد عبر ${method}`,
      type: 'DEPOSIT',
      amount,
      currency: "ريال",
      referenceNumber: refNum,
      paymentMethod: method,
      status: 'PENDING',
      timestamp: Date.now()
    };
    setWalletTransactions(prev => [newTx, ...prev]);
    showAlert(`تم إرسال إشعار الإيداع بمبلغ ${amount.toLocaleString()} ريال للتأكيد`, 'info');
  };

  const markNotificationRead = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
  };

  const markAllNotificationsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
  };

  const updatePrinterSettings = (newSettings: Partial<PrinterSettings>) => {
    setPrinterSettings(prev => ({ ...prev, ...newSettings }));
  };

  return (
    <PosContext.Provider value={{
      user,
      networks,
      packages,
      salesHistory,
      walletTransactions,
      notifications,
      printerSettings,
      activeScreen,
      selectedNetwork,
      activeAlert,
      setActiveScreen,
      setSelectedNetwork,
      showAlert,
      login,
      logout,
      togglePinNetwork,
      requestJoinNetwork,
      buyVouchers,
      topUpWallet,
      markNotificationRead,
      markAllNotificationsRead,
      updatePrinterSettings
    }}>
      {children}
    </PosContext.Provider>
  );
};

export const usePos = () => {
  const context = useContext(PosContext);
  if (!context) throw new Error('usePos must be used within PosProvider');
  return context;
};
