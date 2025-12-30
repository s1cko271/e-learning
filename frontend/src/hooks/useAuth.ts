/**
 * Custom hook cho Authentication
 */
import { useMutation, useQuery } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import apiClient, { setAuthToken, removeAuthToken, handleApiError } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '@/types';
import { ROUTES } from '@/lib/constants';

export const useAuth = () => {
  const router = useRouter();
  const { user, setUser, login: storeLogin, logout: storeLogout, isAuthenticated } = useAuthStore();
  const { addToast } = useUIStore();
  
  // Login mutation
  const loginMutation = useMutation({
    mutationFn: async (credentials: LoginRequest): Promise<AuthResponse> => {
      // Backend expects 'usernameOrEmail' field, not 'username'
      const requestBody = {
        usernameOrEmail: credentials.username,
        password: credentials.password,
      };
      
      console.log('Login Request:', {
        url: '/auth/login',
        body: { ...requestBody, password: '***' }, // Hide password in logs
        headers: { 'Content-Type': 'application/json' },
      });
      
      try {
        const response = await apiClient.post('/auth/login', requestBody);
        return response.data;
      } catch (error: any) {
        // Only log actual system crashes (500) or network errors.
        // Do NOT log 400 (Bad Credentials) or 401 as these are expected user errors.
        const status = error.response?.status;
        if (status && status !== 400 && status !== 401) {
          console.error('Login System Error:', {
            status,
            statusText: error.response?.statusText,
            data: error.response?.data,
            message: error.response?.data?.message || error.message,
          });
        }
        // We must throw it so the Login Page can catch and show the UI alert
        throw error;
      }
    },
    onSuccess: (data) => {
      const userData: User = {
        id: data.id,
        username: data.username,
        email: data.email,
        fullName: data.username, // Backend sẽ trả về fullName
        role: data.roles[0] as any,
        createdAt: new Date().toISOString(),
      };
      
      // Save token to both localStorage and cookie
      storeLogin(userData, data.token);
      
      addToast({
        type: 'success',
        description: 'Đăng nhập thành công!',
      });
      
      // Determine redirect URL based on role
      let redirectUrl: string;
      if (data.roles.includes('ROLE_ADMIN')) {
        redirectUrl = ROUTES.ADMIN_DASHBOARD;
      } else if (data.roles.includes('ROLE_LECTURER')) {
        redirectUrl = ROUTES.INSTRUCTOR.DASHBOARD;
      } else {
        redirectUrl = ROUTES.STUDENT.DASHBOARD;
      }
      
      // Use window.location.href for the first redirect to ensure middleware detects the cookie
      // Small delay to ensure token is saved to both localStorage and cookie
      setTimeout(() => {
        window.location.href = redirectUrl;
      }, 100);
    },
    onError: (error: any) => {
      addToast({
        type: 'error',
        description: handleApiError(error),
      });
    },
  });
  
  // Register mutation
  const registerMutation = useMutation({
    mutationFn: async (data: RegisterRequest) => {
      const response = await apiClient.post('/auth/register', data);
      return response.data;
    },
    onSuccess: () => {
      addToast({
        type: 'success',
        description: 'Đăng ký thành công! Vui lòng đăng nhập.',
      });
      router.push(ROUTES.LOGIN);
    },
    onError: (error: any) => {
      // Don't show toast for 400 validation errors - they're displayed in the form fields
      if (error?.response?.status === 400 && error?.response?.data?.validationErrors) {
        // Validation errors are handled in the component, no need for toast
        return;
      }
      // Show toast for other errors (500, network errors, etc.)
      addToast({
        type: 'error',
        description: handleApiError(error),
      });
    },
  });
  
  // Forgot password mutation
  const forgotPasswordMutation = useMutation({
    mutationFn: async (email: string) => {
      const response = await apiClient.post('/auth/forgot-password', { email });
      return response.data;
    },
    onSuccess: () => {
      addToast({
        type: 'success',
        description: 'Link đặt lại mật khẩu đã được gửi đến email của bạn!',
      });
    },
    onError: (error: any) => {
      addToast({
        type: 'error',
        description: handleApiError(error),
      });
    },
  });
  
  // Reset password mutation
  const resetPasswordMutation = useMutation({
    mutationFn: async ({ token, password }: { token: string; password: string }) => {
      const response = await apiClient.post('/auth/reset-password', { token, newPassword: password });
      return response.data;
    },
    onSuccess: () => {
      addToast({
        type: 'success',
        description: 'Đặt lại mật khẩu thành công! Vui lòng đăng nhập.',
      });
      router.push(ROUTES.LOGIN);
    },
    onError: (error: any) => {
      addToast({
        type: 'error',
        description: handleApiError(error),
      });
    },
  });
  
  // Logout
  const logout = () => {
    storeLogout();
    removeAuthToken();
    addToast({
      type: 'success',
      description: 'Đăng xuất thành công!',
    });
    router.push(ROUTES.LOGIN);
  };
  
  // Update profile mutation
  const updateProfileMutation = useMutation({
    mutationFn: async (data: Partial<User>) => {
      const response = await apiClient.put(`/users/${user?.id}`, data);
      return response.data;
    },
    onSuccess: (data) => {
      setUser(data);
      addToast({
        type: 'success',
        description: 'Cập nhật thông tin thành công!',
      });
    },
    onError: (error: any) => {
      addToast({
        type: 'error',
        description: handleApiError(error),
      });
    },
  });
  
  return {
    user,
    isAuthenticated,
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    register: registerMutation.mutate,
    registerAsync: registerMutation.mutateAsync,
    forgotPassword: forgotPasswordMutation.mutate,
    forgotPasswordAsync: forgotPasswordMutation.mutateAsync,
    resetPassword: resetPasswordMutation.mutate,
    logout,
    updateProfile: updateProfileMutation.mutate,
    isLoginLoading: loginMutation.isPending,
    isRegisterLoading: registerMutation.isPending,
    isForgotPasswordLoading: forgotPasswordMutation.isPending,
    isResetPasswordLoading: resetPasswordMutation.isPending,
    isUpdateProfileLoading: updateProfileMutation.isPending,
  };
};

