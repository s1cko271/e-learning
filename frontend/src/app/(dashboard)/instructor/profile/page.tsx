'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { 
  User, 
  Mail,
  Save,
  Upload,
  Camera,
  Phone,
  MapPin,
  Calendar,
  Moon,
  Sun
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuthStore } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';
import { getProfile, updateProfile, uploadAvatar, changePassword } from '@/services/userService';
import { toggleEmailNotification, getSubscriptionStatus } from '@/services/newsletterService';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { useSearchParams } from 'next/navigation';

const profileSchema = z.object({
  fullName: z.string().min(2, 'Tên phải có ít nhất 2 ký tự'),
  phone: z.string().optional(),
  location: z.string().optional(),
  bio: z.string().max(500, 'Bio không được vượt quá 500 ký tự').optional(),
  expertise: z.string().optional(),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Vui lòng nhập mật khẩu hiện tại'),
  newPassword: z.string()
    .min(6, 'Mật khẩu mới phải có ít nhất 6 ký tự')
    .max(40, 'Mật khẩu không được vượt quá 40 ký tự')
    .regex(/[a-zA-Z]/, 'Mật khẩu phải chứa ít nhất một chữ cái')
    .regex(/[0-9]/, 'Mật khẩu phải chứa ít nhất một số')
    .regex(/[@$!%*#?&]/, 'Mật khẩu phải chứa ít nhất một ký tự đặc biệt (@$!%*#?&)'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
});

type ProfileFormData = z.infer<typeof profileSchema>;
type PasswordFormData = z.infer<typeof passwordSchema>;

export default function InstructorProfilePage() {
  const { user, updateUser, isAuthenticated } = useAuthStore();
  const { addToast, theme, toggleTheme } = useUIStore();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = React.useState(false);
  const [avatarFile, setAvatarFile] = React.useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = React.useState<string | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [emailNotif, setEmailNotif] = React.useState(false);
  const [isLoadingEmailNotif, setIsLoadingEmailNotif] = React.useState(false);
  const [activeTab, setActiveTab] = React.useState('profile');
  const [changePassError, setChangePassError] = React.useState('');
  const [changePassSuccess, setChangePassSuccess] = React.useState('');
  const [profileSuccessMessage, setProfileSuccessMessage] = React.useState('');

  // Handle tab switching from query param
  React.useEffect(() => {
    const tab = searchParams.get('activeTab');
    if (tab === 'options' || tab === 'preferences') {
      setActiveTab('preferences');
    } else if (tab === 'security') {
      setActiveTab('security');
    } else if (tab === 'profile') {
      setActiveTab('profile');
    }
  }, [searchParams]);

  const {
    register: registerProfile,
    handleSubmit: handleSubmitProfile,
    reset: resetProfile,
    formState: { errors: profileErrors },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      fullName: user?.fullName || '',
      phone: user?.phone || '',
      location: '',
      bio: user?.bio || '',
      expertise: '',
    },
  });

  const {
    register: registerPassword,
    handleSubmit: handleSubmitPassword,
    reset: resetPassword,
    formState: { errors: passwordErrors },
  } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
  });

  // Load fresh profile data from database on component mount
  React.useEffect(() => {
    async function loadFreshData() {
      try {
        console.log('InstructorProfilePage: Loading fresh profile data from database...');
        const freshData = await getProfile();
        
        // Populate form fields with fetched data
        resetProfile({
          fullName: freshData.fullName || '',
          phone: freshData.phoneNumber || '',
          location: freshData.address || '',
          bio: freshData.bio || '',
          expertise: freshData.expertise || '',
        });
        
        // Update avatar preview
        if (freshData.avatarUrl) {
          setAvatarPreview(freshData.avatarUrl);
        } else {
          setAvatarPreview(null);
        }

        // Load email notification status
        if (isAuthenticated) {
          try {
            const status = await getSubscriptionStatus();
            setEmailNotif(status.enabled);
          } catch (error) {
            console.error('Failed to load email notification status', error);
          }
        }
        
        // Update user in store
        updateUser({
          fullName: freshData.fullName,
          email: freshData.email,
          phone: freshData.phoneNumber,
          bio: freshData.bio,
          avatar: freshData.avatarUrl,
        });
        
        console.log('InstructorProfilePage: Fresh profile data loaded successfully', freshData);
      } catch (error) {
        console.error('InstructorProfilePage: Failed to load fresh profile data', error);
      }
    }
    
    loadFreshData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        addToast({
          type: 'error',
          description: 'Chỉ chấp nhận file ảnh (JPG, PNG, GIF)',
        });
        // Reset input
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        return;
      }
      
      // Validate file size (5MB max - matching backend)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        addToast({
          type: 'error',
          description: 'Kích thước file không được vượt quá 5MB',
        });
        // Reset input
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        return;
      }
      
      setAvatarFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatarPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const onProfileSubmit = async (data: ProfileFormData) => {
    setIsLoading(true);
    setProfileSuccessMessage('');
    
    try {
      let avatarUrl: string | undefined;
      
      // Step 1: Upload avatar if a new file was selected
      if (avatarFile) {
        try {
          const avatarResponse = await uploadAvatar(avatarFile);
          avatarUrl = avatarResponse.avatarUrl;
        } catch (error: any) {
          addToast({
            type: 'error',
            description: error?.response?.data?.message || 'Lỗi khi tải ảnh đại diện',
          });
          setIsLoading(false);
          return;
        }
      }
      
      // Step 2: Update profile with all fields
      const updateData: any = {
        fullName: data.fullName,
        phoneNumber: data.phone || undefined,
        address: data.location || undefined,
        bio: data.bio || undefined,
        expertise: data.expertise || undefined,
      };
      
      // Add avatarUrl if avatar was uploaded
      if (avatarUrl) {
        updateData.avatarUrl = avatarUrl;
      }
      
      await updateProfile(updateData);
      
      // Step 3: Show success message and reload page
      setProfileSuccessMessage('Cập nhật hồ sơ thành công! Đang tải lại...');
      addToast({
        type: 'success',
        description: 'Cập nhật hồ sơ thành công!',
      });
      
      // Clear avatar file state and reset input
      setAvatarFile(null);
      setAvatarPreview(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      
      // Reload page to reflect changes
      setTimeout(() => {
        window.location.reload();
      }, 1500);
      
    } catch (error: any) {
      addToast({
        type: 'error',
        description: error?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật hồ sơ.',
      });
      setIsLoading(false);
    }
  };

  const onPasswordSubmit = async (data: PasswordFormData) => {
    // Reset error and success messages
    setChangePassError('');
    setChangePassSuccess('');

    // Validate password match
    if (data.newPassword !== data.confirmPassword) {
      setChangePassError('Mật khẩu xác nhận không khớp');
      return;
    }

    try {
      await changePassword({
        oldPassword: data.currentPassword,
        newPassword: data.newPassword,
      });

      // Success: Set success message and clear form
      setChangePassSuccess('Đổi mật khẩu thành công!');
      resetPassword();
      
      // Also show toast for consistency
      addToast({
        type: 'success',
        description: 'Đổi mật khẩu thành công',
      });
    } catch (error: any) {
      // Handle error - capture error message from backend
      const errorMessage = error?.response?.data?.message || 'Đã có lỗi xảy ra khi đổi mật khẩu';
      setChangePassError(errorMessage);
      
      // Also show toast for consistency
      addToast({
        type: 'error',
        description: errorMessage,
      });
    }
  };

  // Clear error/success messages when user starts typing
  const handlePasswordFieldChange = () => {
    if (changePassError) {
      setChangePassError('');
    }
    if (changePassSuccess) {
      setChangePassSuccess('');
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-4xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold font-poppins mb-2">Hồ sơ giảng viên</h1>
          <p className="text-muted-foreground">
            Quản lý thông tin cá nhân và cài đặt tài khoản
          </p>
        </div>
        
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList>
            <TabsTrigger value="profile">Thông tin cá nhân</TabsTrigger>
            <TabsTrigger value="security">Bảo mật</TabsTrigger>
            <TabsTrigger value="preferences">Tùy chọn</TabsTrigger>
          </TabsList>
          
          {/* Profile Tab */}
          <TabsContent value="profile">
            <Card>
              <CardHeader>
                <CardTitle>Thông tin cá nhân</CardTitle>
                <CardDescription>
                  Cập nhật thông tin và ảnh đại diện của bạn
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmitProfile(onProfileSubmit)} className="space-y-6">
                  {/* Success Alert */}
                  {profileSuccessMessage && (
                    <div className="p-3 mb-4 text-sm rounded-md bg-green-50 border border-green-200 text-green-600">
                      {profileSuccessMessage}
                    </div>
                  )}
                  
                  {/* Avatar Upload */}
                  <div className="flex items-center gap-6">
                    <Avatar className="h-24 w-24">
                      <AvatarImage src={avatarPreview || user?.avatar} />
                      <AvatarFallback className="text-2xl">
                        {user?.fullName?.charAt(0).toUpperCase() || 'U'}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <Button
                        type="button"
                        onClick={handleAvatarClick}
                        className="inline-flex items-center gap-2"
                      >
                        <Camera className="h-4 w-4" />
                        Thay đổi ảnh đại diện
                      </Button>
                      <input
                        ref={fileInputRef}
                        id="avatar"
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={handleAvatarChange}
                      />
                      <p className="text-sm text-muted-foreground mt-2">
                        JPG, PNG hoặc GIF. Tối đa 5MB.
                      </p>
                    </div>
                  </div>
                  
                  <Separator />
                  
                  {/* Form Fields */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <Label htmlFor="fullName">
                        Họ và tên <span className="text-destructive">*</span>
                      </Label>
                      <div className="relative">
                        <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="fullName"
                          placeholder="Nguyễn Văn A"
                          className="pl-10"
                          {...registerProfile('fullName')}
                        />
                      </div>
                      {profileErrors.fullName && (
                        <p className="text-sm text-destructive">{profileErrors.fullName.message}</p>
                      )}
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="email">Email</Label>
                      <div className="relative">
                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="email"
                          type="email"
                          placeholder="email@example.com"
                          className="pl-10 bg-gray-100 text-gray-500 cursor-not-allowed"
                          value={user?.email || ''}
                          disabled
                          readOnly
                        />
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="phone">Số điện thoại</Label>
                      <div className="relative">
                        <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="phone"
                          placeholder="0123456789"
                          className="pl-10"
                          {...registerProfile('phone')}
                        />
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="location">Vị trí</Label>
                      <div className="relative">
                        <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="location"
                          placeholder="Hà Nội, Việt Nam"
                          className="pl-10"
                          {...registerProfile('location')}
                        />
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="bio">Giới thiệu bản thân</Label>
                    <Textarea
                      id="bio"
                      placeholder="Viết vài dòng về bản thân..."
                      rows={4}
                      {...registerProfile('bio')}
                    />
                    {profileErrors.bio && (
                      <p className="text-sm text-destructive">{profileErrors.bio.message}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="expertise">Chuyên môn</Label>
                    <Input
                      id="expertise"
                      {...registerProfile('expertise')}
                      placeholder="Ví dụ: React, Node.js, Python, UI/UX Design..."
                    />
                    <p className="text-xs text-muted-foreground mt-1">
                      Phân cách các kỹ năng bằng dấu phẩy
                    </p>
                  </div>
                  
                  <div className="flex items-center justify-between pt-4">
                    <p className="text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4 inline mr-1" />
                      Tham gia ngày {new Date(user?.createdAt || '').toLocaleDateString('vi-VN')}
                    </p>
                    <Button type="submit" disabled={isLoading}>
                      <Save className="h-4 w-4 mr-2" />
                      {isLoading ? 'Đang lưu...' : 'Lưu thay đổi'}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </TabsContent>
          
          {/* Security Tab */}
          <TabsContent value="security">
            <Card>
              <CardHeader>
                <CardTitle>Đổi mật khẩu</CardTitle>
                <CardDescription>
                  Cập nhật mật khẩu để bảo mật tài khoản
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmitPassword(onPasswordSubmit)} className="space-y-6">
                  {/* Error Alert */}
                  {changePassError && (
                    <div className="p-3 mb-4 text-sm rounded-md bg-red-50 border border-red-200 text-red-600">
                      {changePassError}
                    </div>
                  )}
                  
                  {/* Success Alert */}
                  {changePassSuccess && (
                    <div className="p-3 mb-4 text-sm rounded-md bg-green-50 border border-green-200 text-green-600">
                      {changePassSuccess}
                    </div>
                  )}
                  
                  <div className="space-y-2">
                    <Label htmlFor="currentPassword">
                      Mật khẩu hiện tại <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="currentPassword"
                      type="password"
                      {...registerPassword('currentPassword', {
                        onChange: handlePasswordFieldChange,
                      })}
                      className={changePassError ? 'border-red-500' : ''}
                    />
                    {passwordErrors.currentPassword && (
                      <p className="text-sm text-destructive">{passwordErrors.currentPassword.message}</p>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="newPassword">
                      Mật khẩu mới <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="newPassword"
                      type="password"
                      {...registerPassword('newPassword', {
                        onChange: handlePasswordFieldChange,
                      })}
                      className={changePassError ? 'border-red-500' : ''}
                    />
                    <p className="text-xs text-muted-foreground">
                      Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái, số và ký tự đặc biệt (@$!%*#?&)
                    </p>
                    {passwordErrors.newPassword && (
                      <p className="text-sm text-destructive">{passwordErrors.newPassword.message}</p>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword">
                      Xác nhận mật khẩu mới <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="confirmPassword"
                      type="password"
                      {...registerPassword('confirmPassword', {
                        onChange: handlePasswordFieldChange,
                      })}
                      className={changePassError ? 'border-red-500' : ''}
                    />
                    {passwordErrors.confirmPassword && (
                      <p className="text-sm text-destructive">{passwordErrors.confirmPassword.message}</p>
                    )}
                  </div>
                  
                  <Button type="submit">
                    Đổi mật khẩu
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>
          
          {/* Preferences Tab */}
          <TabsContent value="preferences">
            <Card>
              <CardHeader>
                <CardTitle>Tùy chọn</CardTitle>
                <CardDescription>
                  Cài đặt hiển thị và thông báo
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Thông báo email</p>
                      <p className="text-sm text-muted-foreground">
                        Nhận email về khóa học và cập nhật
                      </p>
                    </div>
                    <Button 
                      variant="outline"
                      disabled={isLoadingEmailNotif}
                      onClick={async () => {
                        setIsLoadingEmailNotif(true);
                        try {
                          const newStatus = !emailNotif;
                          await toggleEmailNotification(newStatus);
                          setEmailNotif(newStatus);
                          addToast({
                            type: 'success',
                            description: newStatus ? 'Đã bật thông báo email' : 'Đã tắt thông báo email',
                          });
                        } catch (error: any) {
                          addToast({
                            type: 'error',
                            description: error?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật',
                          });
                        } finally {
                          setIsLoadingEmailNotif(false);
                        }
                      }}
                    >
                      {isLoadingEmailNotif ? 'Đang xử lý...' : (emailNotif ? 'Tắt' : 'Bật')}
                    </Button>
                  </div>
                  
                  <Separator />
                  
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Chế độ tối</p>
                      <p className="text-sm text-muted-foreground">
                        Tự động chuyển đổi giao diện
                      </p>
                    </div>
                    <Button 
                      variant="outline"
                      onClick={toggleTheme}
                      className="flex items-center gap-2"
                    >
                      {theme === 'dark' ? (
                        <>
                          <Sun className="h-4 w-4" />
                          <span>Bật sáng</span>
                        </>
                      ) : (
                        <>
                          <Moon className="h-4 w-4" />
                          <span>Bật tối</span>
                        </>
                      )}
                    </Button>
                  </div>
                  
                  <Separator />
                  
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Ngôn ngữ</p>
                      <p className="text-sm text-muted-foreground">
                        Tiếng Việt
                      </p>
                    </div>
                    <Button 
                      variant="outline"
                      onClick={() => {
                        addToast({
                          type: 'info',
                          description: 'Tính năng đa ngôn ngữ đang được phát triển.',
                        });
                      }}
                    >
                      Thay đổi
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

