export type JoinStatus = 'NOT_JOINED' | 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PosUser {
  storeName: string;
  phone: string;
  location: string;
  isLoggedIn: boolean;
  walletBalance: number;
}

export interface NetworkItem {
  id: string;
  code: string;
  name: string;
  ownerName: string;
  financialCeiling: number;
  currentBalance: number;
  currency: string;
  status: JoinStatus;
  location: string;
  packagesCount: number;
  description: string;
  isPinned?: boolean;
}

export interface VoucherPackage {
  id: string;
  networkId: string;
  name: string;
  price: number;
  posPrice: number;
  currency: string;
  duration: string;
  dataQuota: string;
  validity: string;
  colorHex: string;
  isAvailable: boolean;
  isPopular?: boolean;
}

export interface OrderTransaction {
  id: string;
  networkId: string;
  networkName: string;
  packageName: string;
  packagePrice: number;
  costPrice: number;
  quantity: number;
  totalAmount: number;
  totalCost: number;
  customerPhone?: string;
  voucherPin: string;
  timestamp: number;
  posStoreName: string;
  isPrinted: boolean;
  duration?: string;
  dataQuota?: string;
  validity?: string;
  paymentSource: 'NETWORK_CREDIT' | 'WALLET';
}

export type WalletTxType = 'DEPOSIT' | 'VOUCHER_PURCHASE' | 'NETWORK_SETTLEMENT' | 'TRANSFER';
export type WalletTxStatus = 'COMPLETED' | 'PENDING' | 'REJECTED';

export interface WalletTransaction {
  id: string;
  title: string;
  type: WalletTxType;
  amount: number;
  sellingPrice?: number;
  profit?: number;
  currency: string;
  referenceNumber: string;
  paymentMethod: string;
  status: WalletTxStatus;
  timestamp: number;
  networkName?: string;
}

export interface EWalletOption {
  id: string;
  name: string;
  arabicName: string;
  accountNumber: string;
  colorHex: string;
  subtitle: string;
  instructions: string;
  accountHolderName?: string;
  accountLabel?: string;
}

export type NotificationType = 
  | 'NETWORK_JOIN_APPROVED'
  | 'NETWORK_CREDIT_GRANTED'
  | 'NETWORK_LOW_BALANCE'
  | 'WALLET_TOPUP_SUCCESS'
  | 'WALLET_LOW_BALANCE'
  | 'SYSTEM_ANNOUNCEMENT';

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  timestamp: number;
  isRead: boolean;
  relatedEntityId?: string;
  amount?: number;
}

export interface PrinterSettings {
  printerName: string;
  paperWidth: '58mm' | '80mm';
  autoPrintAfterSale: boolean;
  showStoreName: boolean;
  showBarcode: boolean;
  showFooterNotes: boolean;
  customFooterNote: string;
  fontSize: 'small' | 'medium' | 'large';
}
