'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  BookOpen,
  LayoutDashboard,
  GraduationCap,
  Award,
  User,
  Settings,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Menu,
  Bell,
  Search,
  CreditCard,
  Star,
  MessageSquare,
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
  badge?: number;
}

// Student Navigation Items - Strictly for learning tools only
const STUDENT_NAV: NavItem[] = [
  {
    title: 'Dashboard',
    href: ROUTES.STUDENT.DASHBOARD,
    icon: <LayoutDashboard className="h-5 w-5" />,
  },
  {
    title: 'Khóa học của tôi',
    href: ROUTES.STUDENT.MY_COURSES,
    icon: <BookOpen className="h-5 w-5" />,
  },
  {
    title: 'Tin nhắn',
    href: ROUTES.STUDENT.MESSAGES,
    icon: <MessageSquare className="h-5 w-5" />,
  },
  {
    title: 'Lịch sử giao dịch',
    href: ROUTES.STUDENT.TRANSACTIONS,
    icon: <CreditCard className="h-5 w-5" />,
  },
];

// Instructor Navigation Items - Strictly for management tools
const INSTRUCTOR_NAV: NavItem[] = [
  {
    title: 'Dashboard',
    href: ROUTES.INSTRUCTOR.DASHBOARD,
    icon: <LayoutDashboard className="h-5 w-5" />,
  },
  {
    title: 'Khóa học',
    href: ROUTES.INSTRUCTOR.COURSES,
    icon: <BookOpen className="h-5 w-5" />,
  },
  {
    title: 'Học viên',
    href: ROUTES.INSTRUCTOR.STUDENTS,
    icon: <GraduationCap className="h-5 w-5" />,
  },
  {
    title: 'Tin nhắn',
    href: ROUTES.INSTRUCTOR.MESSAGES,
    icon: <MessageSquare className="h-5 w-5" />,
  },
  {
    title: 'Doanh thu',
    href: ROUTES.INSTRUCTOR.EARNINGS,
    icon: <Award className="h-5 w-5" />,
  },
  {
    title: 'Đánh giá',
    href: '/instructor/reviews',
    icon: <Star className="h-5 w-5" />,
  },
  {
    title: 'Hồ sơ',
    href: ROUTES.INSTRUCTOR.PROFILE,
    icon: <User className="h-5 w-5" />,
  },
];

const adminNavItems: NavItem[] = [
  {
    title: 'Dashboard',
    href: ROUTES.ADMIN_DASHBOARD,
    icon: <LayoutDashboard className="h-5 w-5" />,
  },
  {
    title: 'Khóa học',
    href: ROUTES.ADMIN_COURSES,
    icon: <BookOpen className="h-5 w-5" />,
  },
  {
    title: 'Giảng viên',
    href: ROUTES.ADMIN_INSTRUCTORS,
    icon: <GraduationCap className="h-5 w-5" />,
  },
  {
    title: 'Học viên',
    href: ROUTES.ADMIN_STUDENTS,
    icon: <User className="h-5 w-5" />,
  },
  {
    title: 'Thống kê',
    href: ROUTES.ADMIN_ANALYTICS,
    icon: <Award className="h-5 w-5" />,
  },
  {
    title: 'Cài đặt',
    href: ROUTES.ADMIN_SETTINGS,
    icon: <Settings className="h-5 w-5" />,
  },
];

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
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
    refetchInterval: 30000, // Refetch every 30 seconds
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
  
  // Auth guard: Redirect to login if not authenticated
  React.useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      // Only redirect if we have a token but no user (invalid state)
      const token = typeof window !== 'undefined' ? localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN) : null;
      if (!token) {
        router.push(ROUTES.LOGIN);
      }
    }
  }, [isInitialized, isAuthenticated, router]);
  
  // Get navigation items based on user role
  const getNavItems = (): NavItem[] => {
    if (user?.role === 'ROLE_ADMIN') return adminNavItems;
    if (user?.role === 'ROLE_LECTURER') return INSTRUCTOR_NAV;
    return STUDENT_NAV;
  };
  
  const navItems = getNavItems();
  
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
  
  // Show loading state if not authenticated (will redirect)
  if (!isAuthenticated) {
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
                <BookOpen className="h-5 w-5 text-primary-foreground" />
              </div>
              <span className="font-poppins font-bold">E-learning</span>
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
            {navItems.map((item) => {
              // Improved active state: exact match OR pathname starts with item.href (for nested routes)
              // But exclude exact matches for parent routes (e.g., /instructor should not be active on /instructor/courses)
              const isExactMatch = pathname === item.href;
              const isNestedMatch = pathname.startsWith(item.href + '/');
              // Special case: Dashboard should only be active on exact match
              const isActive = item.href.endsWith('/instructor') || item.href.endsWith('/student')
                ? isExactMatch
                : (isExactMatch || isNestedMatch);
              
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
                    {!sidebarCollapsed && (
                      <>
                        <span className="flex-1">{item.title}</span>
                        {item.badge && item.badge > 0 && (
                          <Badge variant="secondary" className="h-5 px-1.5 text-xs">
                            {item.badge}
                          </Badge>
                        )}
                      </>
                    )}
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
                    {user?.fullName?.charAt(0).toUpperCase() || 'U'}
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
              <DropdownMenuLabel>Tài khoản của tôi</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => {
                if (user?.role === 'ROLE_ADMIN') {
                  router.push(ROUTES.ADMIN_SETTINGS);
                } else if (user?.role === 'ROLE_LECTURER') {
                  router.push(ROUTES.INSTRUCTOR.PROFILE);
                } else {
                  router.push(ROUTES.STUDENT.PROFILE);
                }
              }}>
                <User className="mr-2 h-4 w-4" />
                Hồ sơ
              </DropdownMenuItem>
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
        {/* Same content as desktop sidebar */}
        <div className="h-16 flex items-center justify-between px-4 border-b">
          <Link href={ROUTES.HOME} className="flex items-center space-x-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <BookOpen className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-poppins font-bold">E-learning</span>
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
            {navItems.map((item) => {
              // Improved active state: exact match OR pathname starts with item.href (for nested routes)
              const isExactMatch = pathname === item.href;
              const isNestedMatch = pathname.startsWith(item.href + '/');
              const isActive = item.href.endsWith('/instructor') || item.href.endsWith('/student')
                ? isExactMatch
                : (isExactMatch || isNestedMatch);
              
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
                    {item.badge && item.badge > 0 && (
                      <Badge variant="secondary">{item.badge}</Badge>
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>
      
      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 h-screen overflow-hidden">
        {/* Top Bar - Desktop */}
        <header className="hidden lg:flex h-16 border-b bg-card items-center justify-end px-6 gap-4">
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
              <BookOpen className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-poppins font-bold">E-learning</span>
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
        
        {/* Main Content Area - Add padding for proper spacing from sidebar */}
        <main className="flex-1 overflow-y-auto relative h-full">
          {children}
        </main>
      </div>
    </div>
  );
}

