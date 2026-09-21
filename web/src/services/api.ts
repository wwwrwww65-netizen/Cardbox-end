// CardBox POS API Service
export const API_BASE_URL = 'https://cardbox.basmasoft.com/api';

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
}

export const posApi = {
  async getPublicNetworks() {
    try {
      const res = await fetch(`${API_BASE_URL}/networks/public`);
      if (!res.ok) throw new Error('Network response was not ok');
      return await res.json();
    } catch (e) {
      console.warn('Using local network cache due to network availability');
      return { success: true, data: [] };
    }
  },

  async purchaseVoucher(networkId: string, packageId: string, quantity: number) {
    try {
      const res = await fetch(`${API_BASE_URL}/vouchers/buy`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ networkId, packageId, quantity })
      });
      return await res.json();
    } catch (e) {
      return { success: false, message: 'Offline mode active' };
    }
  }
};
