/**
 * TypeScript Types cho ứng dụng E-Learning
 */

// User Types
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  role: 'ROLE_STUDENT' | 'ROLE_LECTURER' | 'ROLE_ADMIN';
  bio?: string;
  phone?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface UserProfile extends User {
  enrolledCourses?: number[];
  completedCourses?: number[];
  totalCoursesCreated?: number;
  totalStudents?: number;
}

// Auth Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  roles?: Array<'student' | 'lecturer' | 'admin'>;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// Course Types
export interface Course {
  id: number;
  title: string;
  slug?: string;
  description: string;
  shortDescription?: string;
  thumbnail?: string; // Deprecated: Use imageUrl instead
  imageUrl?: string; // Image URL from Backend CourseResponse (matches Backend field name)
  promoVideo?: string;
  price: number;
  discountPrice?: number;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  language: string;
  category?: Category;
  categoryId?: number;
  tags?: string[];
  instructor: Instructor;
  instructorId: number;
  rating?: number;
  reviewCount?: number;
  enrollmentCount: number; // Số lượng học viên đã đăng ký (từ Backend CourseResponse)
  duration?: string;
  lastUpdated?: string;
  whatYouLearn?: string[];
  requirements?: string[];
  isFeatured: boolean;
  isPublished?: boolean; // Khóa học đã được publish
  isEnrolled?: boolean; // Người dùng hiện tại đã đăng ký khóa học này chưa
  status: 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  updatedAt?: string;
  // Enrollment progress (chỉ có khi lấy my-courses)
  enrollmentProgress?: number; // Tiến độ học tập 0-100%
  enrollmentStatus?: 'IN_PROGRESS' | 'COMPLETED'; // Trạng thái enrollment
}

export interface CourseRequest {
  title: string;
  description: string;
  price: number;
  imageUrl?: string;
  totalDurationInHours?: number;
  categoryId: number;
  // Optional fields for future use
  shortDescription?: string;
  thumbnail?: string;
  promoVideo?: string;
  discountPrice?: number;
  level?: string;
  language?: string;
  tags?: string[];
  whatYouLearn?: string[];
  requirements?: string[];
  isFeatured?: boolean;
}

export interface CourseStatistics {
  courseId: number;
  courseName: string;
  totalEnrollments: number;
  completionRate: number;
  averageScore: number;
  totalRevenue: number;
  activeStudents: number;
}

// Category Types
export interface Category {
  id: number;
  name: string;
  slug?: string;
  description?: string;
  icon?: string;
  parentId?: number;
  courseCount?: number;
}

// Instructor Types
export interface Instructor {
  id: number;
  fullName: string;
  username: string;
  email: string;
  avatar?: string;
  bio?: string;
  expertise?: string[];
  totalCourses?: number;
  totalStudents?: number;
  averageRating?: number;
}

// Lesson/Content Types
export interface Section {
  id: number;
  title: string;
  description?: string;
  order: number;
  courseId: number;
  lessons: Lesson[];
  totalDuration?: number;
}

export interface Lesson {
  id: number;
  title: string;
  description?: string;
  type: 'VIDEO' | 'ARTICLE' | 'LIVE_SESSION';
  order: number;
  duration?: number; // in minutes
  sectionId: number;
  videoUrl?: string;
  articleContent?: string;
  isCompleted?: boolean;
  resources?: Resource[];
}

export interface Resource {
  id: number;
  title: string;
  type: 'PDF' | 'DOC' | 'PPT' | 'VIDEO' | 'OTHER';
  url: string;
  size?: number;
  lessonId: number;
}


// Enrollment Types
export interface Enrollment {
  id: number;
  courseId: number;
  course?: Course;
  studentId: number;
  student?: User;
  enrolledAt: string;
  progress: number;
  lastAccessedAt?: string;
  completedAt?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'DROPPED';
}

// Progress Types
export interface Progress {
  enrollmentId: number;
  courseId: number;
  courseName: string;
  completedLessons: number;
  totalLessons: number;
  progressPercentage: number;
  timeSpent?: number; // in minutes
  lastAccessedLesson?: Lesson;
}

// Certificate Types
export interface Certificate {
  id: number;
  studentId: number;
  courseId: number;
  courseName: string;
  instructorName: string;
  issuedAt: string;
  certificateUrl: string;
  verificationCode: string;
}

// Payment/Transaction Types
export interface Transaction {
  id: number;
  userId: number;
  courseId: number;
  courseName?: string;
  amount: number;
  currency: string;
  paymentMethod: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
  transactionId: string;
  createdAt: string;
  updatedAt?: string;
}

// Statistics/Analytics Types
export interface DashboardStats {
  totalCourses: number;
  totalStudents: number;
  totalInstructors: number;
  totalRevenue: number;
  activeCourses: number;
  newEnrollmentsThisMonth: number;
  averageCompletionRate: number;
  pendingApprovals?: number;
}

export interface RevenueData {
  date: string;
  revenue: number;
  enrollments: number;
}

export interface StudentStats {
  studentId: number;
  studentName: string;
  totalEnrollments: number;
  completedCourses: number;
  averageScore: number;
  completionRate: number;
  totalTimeSpent?: number;
  lastActive?: string;
}

// Chatbot Types
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

export interface ChatSession {
  sessionId: string;
  userId?: number;
  messages: ChatMessage[];
  createdAt: string;
}

// API Response Types
export interface ApiResponse<T = any> {
  data?: T;
  message?: string;
  success: boolean;
  error?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Form Types
export interface SearchFilters {
  keyword?: string;
  categoryId?: number;
  level?: string;
  minPrice?: number;
  maxPrice?: number;
  isFree?: boolean; // Filter for free courses (price = 0)
  isPaid?: boolean; // Filter for paid courses (price > 0)
  rating?: number; // Filter by exact rating (1-5 stars)
  hasNoRating?: boolean; // Filter courses with no ratings
  language?: string;
  sortBy?: 'popular' | 'rating' | 'newest' | 'price_low' | 'price_high';
  page?: number; // Current page number (0-indexed)
}

