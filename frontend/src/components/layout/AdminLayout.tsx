'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  LayoutDashboard,
  Users,
  BookOpen,
  CreditCard,
  FolderTree,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Menu,
  Bell,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Badge } from '@/components/ui/badge';
import { useAuthStore } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';
import { cn } from '@/lib/utils';
import { ROUTES, STORAGE_KEYS } from '@/lib/constants';
import { useQuery } from '@tanstack/react-query';
import { getUnreadCount, getNotifications, markAsRead, markAllAsRead } from '@/services/notificationService';

interface NavItem {
  title: string;
  href: string;
  icon: React.ReactNode;
}

const ADMIN_NAV: NavItem[] = [
  {
    title: 'Dashboard',
    href: ROUTES.ADMIN.DASHBOARD,
    icon: <LayoutDashboard className="h-5 w-5" />,
  },
  {
    title: 'Người dùng',
    href: ROUTES.ADMIN.USERS,
    icon: <Users className="h-5 w-5" />,
  },
  {
    title: 'Danh mục',
    href: ROUTES.ADMIN.CATEGORIES,
    icon: <FolderTree className="h-5 w-5" />,
  },
  {
    title: 'Khóa học',
    href: ROUTES.ADMIN.COURSES,
    icon: <BookOpen className="h-5 w-5" />,
  },
  {
    title: 'Giao dịch',
    href: ROUTES.ADMIN.TRANSACTIONS,
    icon: <CreditCard className="h-5 w-5" />,
  },
];

interface AdminLayoutProps {
  children: React.ReactNode;
}

export function AdminLayout({ children }: AdminLayoutProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAuthenticated } = useAuthStore();
  const { sidebarCollapsed, toggleSidebarCollapse } = useUIStore();
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);
  const [isInitialized, setIsInitialized] = React.useState(false);
  
  // Check if token exists and is valid
  const hasValidToken = React.useMemo(() => {
    if (typeof window === 'undefined') return false;
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    return !!token && isAuthenticated;
  }, [isAuthenticated]);
  
  // Fetch unread notification count
  const { data: unreadCount = 0, refetch: refetchUnreadCount } = useQuery({
    queryKey: ['notifications-unread-count'],
    queryFn: async () => {
      try {
        return await getUnreadCount();
      } catch (error: any) {
        // Silently handle 403 errors
        if (error?.response?.status === 403) {
          return 0;
        }
        throw error;
      }
    },
    enabled: hasValidToken,
    refetchInterval: 30000,
    retry: false, // Don't retry on any error
  });
  
  // Fetch notifications
  const { data: notificationsData, refetch: refetchNotifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: async () => {
      try {
        return await getNotifications(0, 10);
      } catch (error: any) {
        // Silently handle 403 errors
        if (error?.response?.status === 403) {
          return { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 };
        }
        throw error;
      }
    },
    enabled: hasValidToken,
    retry: false, // Don't retry on any error
  });
  
  const notifications = notificationsData?.content || [];
  
  // Initialize auth state from localStorage on mount
  React.useEffect(() => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER_DATA);
      
      if (token && storedUser) {
        try {
          const userData = JSON.parse(storedUser);
          useAuthStore.setState({ user: userData, isAuthenticated: true });
        } catch (error) {
          console.error('Failed to parse stored user data:', error);
          localStorage.removeItem(STORAGE_KEYS.USER_DATA);
          localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
        }
      }
      setIsInitialized(true);
    }
  }, []);
  
  // Auth guard: Redirect to login if not authenticated or not admin
  // Note: This is a secondary check. Primary protection is in admin/layout.tsx
  React.useEffect(() => {
    if (isInitialized) {
      if (!isAuthenticated) {
        const token = typeof window !== 'undefined' ? localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN) : null;
        if (!token) {
          router.replace(ROUTES.LOGIN);
          return;
        }
      } else if (user?.role !== 'ROLE_ADMIN') {
        // Redirect non-admin users to their appropriate dashboard
        if (user?.role === 'ROLE_LECTURER') {
          router.replace(ROUTES.INSTRUCTOR.DASHBOARD);
        } else {
          router.replace(ROUTES.STUDENT.DASHBOARD);
        }
        return;
      }
    }
  }, [isInitialized, isAuthenticated, user?.role, router]);
  
  const handleLogout = () => {
    logout();
    router.push(ROUTES.LOGIN);
  };
  
  // Show loading state while initializing
  if (!isInitialized) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Đang tải...</p>
        </div>
      </div>
    );
  }
  
  // Show loading state if not authenticated or not admin (will redirect)
  if (!isAuthenticated || user?.role !== 'ROLE_ADMIN') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Đang chuyển hướng...</p>
        </div>
      </div>
    );
  }
  
  return (
    <div className="min-h-screen flex">
      {/* Sidebar - Desktop */}
      <aside
        className={cn(
          'hidden lg:flex flex-col border-r bg-card transition-all duration-300',
          sidebarCollapsed ? 'w-20' : 'w-64'
        )}
      >
        {/* Logo */}
        <div className="h-16 flex items-center justify-between px-4 border-b">
          {!sidebarCollapsed && (
            <Link href={ROUTES.HOME} className="flex items-center space-x-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
                <LayoutDashboard className="h-5 w-5 text-primary-foreground" />
              </div>
              <span className="font-poppins font-bold">Admin Panel</span>
            </Link>
          )}
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleSidebarCollapse}
            className={cn(sidebarCollapsed && 'mx-auto')}
          >
            {sidebarCollapsed ? (
              <ChevronRight className="h-4 w-4" />
            ) : (
              <ChevronLeft className="h-4 w-4" />
            )}
          </Button>
        </div>
        
        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto p-4">
          <ul className="space-y-1">
            {ADMIN_NAV.map((item) => {
              const isExactMatch = pathname === item.href;
              const isNestedMatch = pathname.startsWith(item.href + '/');
              const isActive = isExactMatch || isNestedMatch;
              
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={cn(
                      'flex items-center gap-3 rounded-lg px-3 py-2 transition-colors',
                      'hover:bg-accent hover:text-accent-foreground',
                      isActive && 'bg-accent text-accent-foreground font-medium',
                      sidebarCollapsed && 'justify-center'
                    )}
                  >
                    {item.icon}
                    {!sidebarCollapsed && <span className="flex-1">{item.title}</span>}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
        
        {/* User Profile */}
        <div className="border-t p-4">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className={cn(
                  'flex w-full items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-accent',
                  sidebarCollapsed && 'justify-center'
                )}
              >
                <Avatar className="h-8 w-8">
                  <AvatarImage src={user?.avatar} />
                  <AvatarFallback>
                    {user?.fullName?.charAt(0).toUpperCase() || 'A'}
                  </AvatarFallback>
                </Avatar>
                {!sidebarCollapsed && (
                  <div className="flex-1 text-left min-w-0">
                    <p className="text-sm font-medium truncate">{user?.fullName}</p>
                    <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
                  </div>
                )}
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>Tài khoản Admin</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleLogout}>
                <LogOut className="mr-2 h-4 w-4" />
                Đăng xuất
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </aside>
      
      {/* Mobile Menu Overlay */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}
      
      {/* Mobile Sidebar */}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 w-64 bg-card border-r transform transition-transform duration-300 lg:hidden',
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="h-16 flex items-center justify-between px-4 border-b">
          <Link href={ROUTES.HOME} className="flex items-center space-x-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <LayoutDashboard className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-poppins font-bold">Admin Panel</span>
          </Link>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setMobileMenuOpen(false)}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
        </div>
        
        <nav className="flex-1 overflow-y-auto p-4">
          <ul className="space-y-1">
            {ADMIN_NAV.map((item) => {
              const isExactMatch = pathname === item.href;
              const isNestedMatch = pathname.startsWith(item.href + '/');
              const isActive = isExactMatch || isNestedMatch;
              
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    onClick={() => setMobileMenuOpen(false)}
                    className={cn(
                      'flex items-center gap-3 rounded-lg px-3 py-2 transition-colors',
                      'hover:bg-accent hover:text-accent-foreground',
                      isActive && 'bg-accent text-accent-foreground font-medium'
                    )}
                  >
                    {item.icon}
                    <span className="flex-1">{item.title}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>
      
      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Bar - Mobile */}
        <header className="lg:hidden h-16 border-b bg-card flex items-center justify-between px-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setMobileMenuOpen(true)}
          >
            <Menu className="h-5 w-5" />
          </Button>
          
          <Link href={ROUTES.HOME} className="flex items-center space-x-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <LayoutDashboard className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-poppins font-bold">Admin Panel</span>
          </Link>
          
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="relative">
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                  <Badge 
                    variant="destructive" 
                    className="absolute -top-1 -right-1 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs"
                  >
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </Badge>
                )}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-80 p-0" align="end">
              <div className="flex items-center justify-between p-4 border-b">
                <h3 className="font-semibold">Thông báo</h3>
                {unreadCount > 0 && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={async (e) => {
                      e.stopPropagation();
                      try {
                        await markAllAsRead();
                        refetchUnreadCount();
                        refetchNotifications();
                      } catch (error) {
                        console.error('Error marking all as read:', error);
                      }
                    }}
                  >
                    Đánh dấu tất cả đã đọc
                  </Button>
                )}
              </div>
              <div className="max-h-[400px] overflow-y-auto">
                {notifications.length === 0 ? (
                  <div className="p-8 text-center text-muted-foreground">
                    <Bell className="h-8 w-8 mx-auto mb-2 opacity-50" />
                    <p>Chưa có thông báo nào</p>
                  </div>
                ) : (
                  <div className="divide-y">
                    {notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className={`p-4 hover:bg-muted/50 cursor-pointer transition-colors ${
                          !notification.isRead ? 'bg-primary/5' : ''
                        }`}
                        onClick={async () => {
                          // Đánh dấu đã đọc
                          if (!notification.isRead) {
                            try {
                              await markAsRead(notification.id);
                              refetchUnreadCount();
                              refetchNotifications();
                            } catch (error) {
                              console.error('Error marking as read:', error);
                            }
                          }
                          
                          // Chuyển đến trang liên quan nếu có actionUrl
                          if (notification.actionUrl) {
                            // Normalize URL - ensure it starts with / and handle relative paths
                            let url = notification.actionUrl;
                            if (!url.startsWith('/')) {
                              url = '/' + url;
                            }
                            // If URL is just /messages, redirect to appropriate messages page based on user role
                            if (url.startsWith('/messages') && !url.startsWith('/student/messages') && !url.startsWith('/instructor/messages')) {
                              if (user?.role === 'ROLE_STUDENT') {
                                url = url.replace('/messages', '/student/messages');
                              } else if (user?.role === 'ROLE_LECTURER') {
                                url = url.replace('/messages', '/instructor/messages');
                              }
                            }
                            router.push(url);
                          }
                        }}
                      >
                        <div className="flex items-start gap-3">
                          <div className={`h-2 w-2 rounded-full mt-2 flex-shrink-0 ${
                            !notification.isRead ? 'bg-primary' : 'bg-transparent'
                          }`} />
                          <div className="flex-1 min-w-0">
                            <p className={`text-sm ${!notification.isRead ? 'font-semibold' : ''}`}>
                              {notification.message}
                            </p>
                            {notification.courseTitle && (
                              <p className="text-xs text-muted-foreground mt-1">
                                {notification.courseTitle}
                              </p>
                            )}
                            <p className="text-xs text-muted-foreground mt-1">
                              {new Date(notification.createdAt).toLocaleString('vi-VN')}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </DropdownMenuContent>
          </DropdownMenu>
        </header>
        
        {/* Top Bar - Desktop */}
        <header className="hidden lg:flex h-16 border-b bg-card items-center justify-between px-6">
          <div className="flex items-center gap-4">
            <h1 className="text-lg font-semibold">Quản trị hệ thống</h1>
          </div>
          
          <div className="flex items-center gap-4">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="relative">
                  <Bell className="h-5 w-5" />
                  {unreadCount > 0 && (
                    <Badge 
                      variant="destructive" 
                      className="absolute -top-1 -right-1 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs"
                    >
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </Badge>
                  )}
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-80 p-0" align="end">
                <div className="flex items-center justify-between p-4 border-b">
                  <h3 className="font-semibold">Thông báo</h3>
                  {unreadCount > 0 && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={async (e) => {
                        e.stopPropagation();
                        try {
                          await markAllAsRead();
                          refetchUnreadCount();
                          refetchNotifications();
                        } catch (error) {
                          console.error('Error marking all as read:', error);
                        }
                      }}
                    >
                      Đánh dấu tất cả đã đọc
                    </Button>
                  )}
                </div>
                <div className="max-h-[400px] overflow-y-auto">
                  {notifications.length === 0 ? (
                    <div className="p-8 text-center text-muted-foreground">
                      <Bell className="h-8 w-8 mx-auto mb-2 opacity-50" />
                      <p>Chưa có thông báo nào</p>
                    </div>
                  ) : (
                    <div className="divide-y">
                      {notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`p-4 hover:bg-muted/50 cursor-pointer transition-colors ${
                            !notification.isRead ? 'bg-primary/5' : ''
                          }`}
                          onClick={async () => {
                            // Đánh dấu đã đọc
                            if (!notification.isRead) {
                              try {
                                await markAsRead(notification.id);
                                refetchUnreadCount();
                                refetchNotifications();
                              } catch (error) {
                                console.error('Error marking as read:', error);
                              }
                            }
                            
                            // Chuyển đến trang liên quan nếu có actionUrl
                            if (notification.actionUrl) {
                              // Normalize URL - ensure it starts with / and handle relative paths
                              let url = notification.actionUrl;
                              if (!url.startsWith('/')) {
                                url = '/' + url;
                              }
                              // If URL is just /messages, redirect to appropriate messages page based on user role
                              if (url.startsWith('/messages') && !url.startsWith('/student/messages') && !url.startsWith('/instructor/messages')) {
                                if (user?.role === 'ROLE_STUDENT') {
                                  url = url.replace('/messages', '/student/messages');
                                } else if (user?.role === 'ROLE_LECTURER') {
                                  url = url.replace('/messages', '/instructor/messages');
                                }
                              }
                              router.push(url);
                            }
                          }}
                        >
                          <div className="flex items-start gap-3">
                            <div className={`h-2 w-2 rounded-full mt-2 flex-shrink-0 ${
                              !notification.isRead ? 'bg-primary' : 'bg-transparent'
                            }`} />
                            <div className="flex-1 min-w-0">
                              <p className={`text-sm ${!notification.isRead ? 'font-semibold' : ''}`}>
                                {notification.message}
                              </p>
                              {notification.courseTitle && (
                                <p className="text-xs text-muted-foreground mt-1">
                                  {notification.courseTitle}
                                </p>
                              )}
                              <p className="text-xs text-muted-foreground mt-1">
                                {new Date(notification.createdAt).toLocaleString('vi-VN')}
                              </p>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>
        
        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
}

