/**
 * User Service - Handle user profile operations
 */
import apiClient from '@/lib/api';

export interface UpdateProfileRequest {
  fullName?: string;
  phoneNumber?: string; // Backend field name
  address?: string; // Backend field name
  bio?: string;
  expertise?: string;
  avatarUrl?: string; // Set after avatar upload
  emailNotificationEnabled?: boolean;
  // Note: email is NOT included to avoid security issues
}

export interface UpdateProfileResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  bio?: string;
  avatarUrl?: string;
  createdAt: string;
}

export interface ProfileResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  address?: string;
  bio?: string;
  expertise?: string;
  avatarUrl?: string;
  emailNotificationEnabled?: boolean;
  createdAt: string;
}

/**
 * Get user profile - Fetch fresh data from database
 * @returns User profile data
 */
export const getProfile = async (): Promise<ProfileResponse> => {
  const response = await apiClient.get<ProfileResponse>('/user/profile');
  return response.data;
};

/**
 * Upload avatar file
 * @param file - The image file to upload
 * @returns Response with avatarUrl
 */
export const uploadAvatar = async (file: File): Promise<{ avatarUrl: string; user: any }> => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await apiClient.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  
  return response.data;
};

/**
 * Update user profile
 * @param data - Profile data to update (excluding email)
 * @returns Updated user data
 */
export const updateProfile = async (data: UpdateProfileRequest): Promise<UpdateProfileResponse> => {
  const response = await apiClient.put<UpdateProfileResponse>('/user/profile', data);
  return response.data;
};

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface ChangePasswordResponse {
  message: string;
}

/**
 * Change user password
 * @param data - Old and new password
 * @returns Success message
 */
export const changePassword = async (data: ChangePasswordRequest): Promise<ChangePasswordResponse> => {
  const response = await apiClient.put<ChangePasswordResponse>('/user/change-password', data);
  return response.data;
};

