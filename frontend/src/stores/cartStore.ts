/**
 * Cart Store - Quản lý trạng thái giỏ hàng
 */
import { create } from 'zustand';
import { Cart, CartItem } from '@/services/cartService';
import { getCart, addToCart as addToCartAPI, removeFromCart as removeFromCartAPI, clearCart as clearCartAPI, checkout as checkoutAPI } from '@/services/cartService';
import { useUIStore } from './uiStore';

interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  fetchCart: () => Promise<void>;
  addToCart: (courseId: number) => Promise<void>;
  removeFromCart: (itemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  refreshCart: () => Promise<void>;
  checkout: () => Promise<{ paymentUrl: string; transactionCode: string; amount: string; cartId: string }>;
}

export const useCartStore = create<CartState>((set, get) => ({
  cart: null,
  isLoading: false,
  error: null,
  
  fetchCart: async () => {
    set({ isLoading: true, error: null });
    try {
      const cart = await getCart();
      set({ cart, isLoading: false });
    } catch (error: any) {
      // Don't log 400/401/403 errors - they're expected if user is not fully authenticated
      if (error.response?.status === 400 || error.response?.status === 401 || error.response?.status === 403) {
        // Set empty cart instead of error
        set({ cart: null, isLoading: false, error: null });
        return;
      }
      console.error('Error fetching cart:', error);
      set({ 
        error: error.response?.data?.message || 'Không thể tải giỏ hàng',
        isLoading: false 
      });
    }
  },
  
  addToCart: async (courseId: number) => {
    const { addToast } = useUIStore.getState();
    set({ isLoading: true, error: null });
    try {
      const cart = await addToCartAPI(courseId);
      set({ cart, isLoading: false });
      addToast({
        type: 'success',
        description: 'Đã thêm khóa học vào giỏ hàng',
      });
    } catch (error: any) {
      console.error('Error adding to cart:', error);
      const errorMessage = error.response?.data?.message || 
                          (error.response?.status === 400 && 'Khóa học đã có trong giỏ hàng hoặc bạn đã sở hữu khóa học này') ||
                          'Không thể thêm khóa học vào giỏ hàng';
      set({ error: errorMessage, isLoading: false });
      addToast({
        type: 'error',
        description: errorMessage,
      });
    }
  },
  
  removeFromCart: async (itemId: number) => {
    const { addToast } = useUIStore.getState();
    set({ isLoading: true, error: null });
    try {
      const cart = await removeFromCartAPI(itemId);
      set({ cart, isLoading: false });
      addToast({
        type: 'success',
        description: 'Đã xóa khóa học khỏi giỏ hàng',
      });
    } catch (error: any) {
      console.error('Error removing from cart:', error);
      set({ 
        error: error.response?.data?.message || 'Không thể xóa khóa học',
        isLoading: false 
      });
      addToast({
        type: 'error',
        description: 'Không thể xóa khóa học khỏi giỏ hàng',
      });
    }
  },
  
  clearCart: async () => {
    const { addToast } = useUIStore.getState();
    set({ isLoading: true, error: null });
    try {
      await clearCartAPI();
      set({ cart: null, isLoading: false });
      addToast({
        type: 'success',
        description: 'Đã xóa tất cả khóa học khỏi giỏ hàng',
      });
    } catch (error: any) {
      console.error('Error clearing cart:', error);
      set({ 
        error: error.response?.data?.message || 'Không thể xóa giỏ hàng',
        isLoading: false 
      });
      addToast({
        type: 'error',
        description: 'Không thể xóa giỏ hàng',
      });
    }
  },
  
  refreshCart: async () => {
    await get().fetchCart();
  },
  
  checkout: async () => {
    const { addToast } = useUIStore.getState();
    set({ isLoading: true, error: null });
    try {
      const response = await checkoutAPI();
      set({ isLoading: false });
      return response;
    } catch (error: any) {
      console.error('Error during checkout:', error);
      const errorMessage = error.response?.data?.error || 'Không thể tạo thanh toán';
      set({ error: errorMessage, isLoading: false });
      addToast({
        type: 'error',
        description: errorMessage,
      });
      throw error;
    }
  },
}));

