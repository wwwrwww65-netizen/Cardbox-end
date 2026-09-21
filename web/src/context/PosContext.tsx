import React, { createContext, useContext, useState, useEffect } from 'react';
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
  
  // Actions
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
  },
  {
    id: "pkg_5",
    networkId: "net_2",
    name: "باقة النجمة 1.5 جيجا",
    price: 400,
    posPrice: 360,
    currency: "ر.ي",
    duration: "6 ساعات",
    dataQuota: "1500 MB",
    validity: "5 أيام",
    colorHex: "#0F4C81",
    isAvailable: true,
    isPopular: true
  },
  {
    id: "pkg_6",
    networkId: "net_2",
    name: "باقة السهرة اللامحدودة",
    price: 600,
    posPrice: 540,
    currency: "ر.ي",
    duration: "12 ساعة",
    dataQuota: "3000 MB",
    validity: "7 أيام",
    colorHex: "#EC4899",
    isAvailable: true,
    isPopular: true
  }
];

const initialSales: OrderTransaction[] = [
  {
    id: "ORD-984210",
    networkId: "net_1",
    networkName: "شبكة الأمل المركزية",
    packageName: "كرت 2 جيجا تيربو",
    packagePrice: 500,
    costPrice: 450,
    quantity: 1,
    totalAmount: 500,
    totalCost: 450,
    customerPhone: "771234567",
    voucherPin: "849201948",
    timestamp: Date.now() - 1000 * 60 * 25,
    posStoreName: "بصمة العصر الحديث للاتصالات",
    isPrinted: true,
    duration: "8 ساعات",
    dataQuota: "2048 MB",
    validity: "7 أيام",
    paymentSource: "NETWORK_CREDIT"
  },
  {
    id: "ORD-984209",
    networkId: "net_1",
    networkName: "شبكة الأمل المركزية",
    packageName: "كرت 1 جيجا - سرعة كاملة",
    packagePrice: 300,
    costPrice: 270,
    quantity: 2,
    totalAmount: 600,
    totalCost: 540,
    customerPhone: "733445566",
    voucherPin: "392019482",
    timestamp: Date.now() - 1000 * 60 * 80,
    posStoreName: "بصمة العصر الحديث للاتصالات",
    isPrinted: true,
    duration: "4 ساعات",
    dataQuota: "1000 MB",
    validity: "5 أيام",
    paymentSource: "WALLET"
  }
];

const initialWalletTransactions: WalletTransaction[] = [
  {
    id: "WTX-101",
    title: "تغذية رصيد عبر بنك الكريمي",
    type: "DEPOSIT",
    amount: 100000,
    currency: "ريال",
    referenceNumber: "KR-99482103",
    paymentMethod: "حساب الكريمي المميز",
    status: "COMPLETED",
    timestamp: Date.now() - 1000 * 60 * 60 * 12
  },
  {
    id: "WTX-102",
    title: "شراء كروت - شبكة الواي فاي الذهبي",
    type: "VOUCHER_PURCHASE",
    amount: 360,
    sellingPrice: 400,
    profit: 40,
    currency: "ريال",
    referenceNumber: "ORD-984209",
    paymentMethod: "محفظة CardBox",
    status: "COMPLETED",
    timestamp: Date.now() - 1000 * 60 * 80,
    networkName: "شبكة الواي فاي الذهبي"
  }
];

const initialNotifications: AppNotification[] = [
  {
    id: "notif_1",
    title: "تم قبول انضمامك للشبكة بنجاح",
    message: "وافقت إدارة 'شبكة الأمل المركزية' على طلب الانضمام، ومنحتك سقفاً مالياً قدره 150,000 ريال.",
    type: "NETWORK_JOIN_APPROVED",
    timestamp: Date.now() - 1000 * 60 * 60 * 4,
    isRead: false,
    relatedEntityId: "net_1"
  },
  {
    id: "notif_2",
    title: "تغذية المحفظة بنجاح",
    message: "تم إيداع مبلغ 100,000 ريال في محفظة CardBox الخاصة بنقطة بيعك.",
    type: "WALLET_TOPUP_SUCCESS",
    timestamp: Date.now() - 1000 * 60 * 60 * 12,
    isRead: true,
    amount: 100000
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
  const [user, setUser] = useState<PosUser>(() => {
    const saved = localStorage.getItem('cb_pos_user');
    return saved ? JSON.parse(saved) : defaultUser;
  });

  const [networks, setNetworks] = useState<NetworkItem[]>(() => {
    const saved = localStorage.getItem('cb_pos_networks');
    return saved ? JSON.parse(saved) : initialNetworks;
  });

  const [packages, setPackages] = useState<VoucherPackage[]>(() => {
    const saved = localStorage.getItem('cb_pos_packages');
    return saved ? JSON.parse(saved) : initialPackages;
  });

  const [salesHistory, setSalesHistory] = useState<OrderTransaction[]>(() => {
    const saved = localStorage.getItem('cb_pos_sales');
    return saved ? JSON.parse(saved) : initialSales;
  });

  const [walletTransactions, setWalletTransactions] = useState<WalletTransaction[]>(() => {
    const saved = localStorage.getItem('cb_pos_wallet_txs');
    return saved ? JSON.parse(saved) : initialWalletTransactions;
  });

  const [notifications, setNotifications] = useState<AppNotification[]>(() => {
    const saved = localStorage.getItem('cb_pos_notifs');
    return saved ? JSON.parse(saved) : initialNotifications;
  });

  const [printerSettings, setPrinterSettings] = useState<PrinterSettings>(() => {
    const saved = localStorage.getItem('cb_pos_printer');
    return saved ? JSON.parse(saved) : {
      printerName: 'طابعة بلوتوث حرارية 58mm (مفعلة)',
      paperWidth: '58mm',
      autoPrintAfterSale: true,
      showStoreName: true,
      showBarcode: true,
      showFooterNotes: true,
      customFooterNote: 'شكراً لزيارتكم ونتمنى لكم تجربة تصفح سريعة وممتعة',
      fontSize: 'medium'
    };
  });

  const [activeScreen, setActiveScreen] = useState<string>('home');
  const [selectedNetwork, setSelectedNetwork] = useState<NetworkItem | null>(null);
  const [activeAlert, setActiveAlert] = useState<{ message: string; type: 'success' | 'error' | 'info' | 'warning' } | null>(null);

  // Sync to local storage
  useEffect(() => {
    localStorage.setItem('cb_pos_user', JSON.stringify(user));
  }, [user]);

  useEffect(() => {
    localStorage.setItem('cb_pos_networks', JSON.stringify(networks));
  }, [networks]);

  useEffect(() => {
    localStorage.setItem('cb_pos_sales', JSON.stringify(salesHistory));
  }, [salesHistory]);

  useEffect(() => {
    localStorage.setItem('cb_pos_wallet_txs', JSON.stringify(walletTransactions));
  }, [walletTransactions]);

  useEffect(() => {
    localStorage.setItem('cb_pos_notifs', JSON.stringify(notifications));
  }, [notifications]);

  useEffect(() => {
    localStorage.setItem('cb_pos_printer', JSON.stringify(printerSettings));
  }, [printerSettings]);

  const showAlert = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
    setActiveAlert({ message, type });
    setTimeout(() => {
      setActiveAlert(null);
    }, 4500);
  };

  const login = (storeName: string, phone: string, location: string) => {
    setUser({
      storeName,
      phone,
      location,
      isLoggedIn: true,
      walletBalance: user.walletBalance || 50000
    });
    showAlert('تم تسجيل الدخول بنجاح! مرحباً بك', 'success');
  };

  const logout = () => {
    setUser(prev => ({ ...prev, isLoggedIn: false }));
    showAlert('تم تسجيل الخروج بنجاح', 'info');
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
    showAlert('تم إرسال طلب الانضمام لمالك الشبكة بنجاح، سيصلك إشعار فور الموافقة', 'success');
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
        showAlert(`عذراً، رصيدك في سقف الشبكة (${network.currentBalance.toLocaleString()} ريال) لا يكفي لتغطية تكلفة الكروت (${totalCost.toLocaleString()} ريال)`, 'error');
        return null;
      }
      // Deduct from network balance
      setNetworks(prev => prev.map(net => 
        net.id === network.id 
          ? { ...net, currentBalance: Math.max(0, net.currentBalance - totalCost) }
          : net
      ));
    } else {
      if (user.walletBalance < totalCost) {
        showAlert(`عذراً، رصيد محفظة CardBox (${user.walletBalance.toLocaleString()} ريال) غير كافٍ. يرجى تغذية المحفظة أولاً.`, 'error');
        return null;
      }
      // Deduct from wallet
      setUser(prev => ({
        ...prev,
        walletBalance: prev.walletBalance - totalCost
      }));

      // Add wallet transaction
      const newWalletTx: WalletTransaction = {
        id: `WTX-${Date.now().toString().slice(-6)}`,
        title: `شراء كرت - ${network.name}`,
        type: 'VOUCHER_PURCHASE',
        amount: totalCost,
        sellingPrice: totalAmount,
        profit: totalAmount - totalCost,
        currency: "ريال",
        referenceNumber: `ORD-${Date.now().toString().slice(-6)}`,
        paymentMethod: "محفظة CardBox",
        status: "COMPLETED",
        timestamp: Date.now(),
        networkName: network.name
      };
      setWalletTransactions(prev => [newWalletTx, ...prev]);
    }

    // Generate Voucher Code / PIN
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
    showAlert(`تم إصدار الكرت بنجاح! كود الكرت: ${generatedPin} (ربحك: ${(totalAmount - totalCost).toLocaleString()} ريال)`, 'success');
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
    showAlert(`تم استلام طلب تغذية المحفظة بمبلغ ${amount.toLocaleString()} ريال، سيتم مراجعة الحوالة وتأكيد الرصيد خلال دقائق`, 'info');
  };

  const markNotificationRead = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
  };

  const markAllNotificationsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    showAlert('تم تحديد جميع الإشعارات كمقروءة', 'success');
  };

  const updatePrinterSettings = (newSettings: Partial<PrinterSettings>) => {
    setPrinterSettings(prev => ({ ...prev, ...newSettings }));
    showAlert('تم حفظ إعدادات الطباعة بنجاح', 'success');
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
  if (!context) throw new Error('usePos must be used within a PosProvider');
  return context;
};
