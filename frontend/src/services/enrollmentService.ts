/**
 * Enrollment Service - Connect to Spring Boot Backend
 * API Base: /api/v1/enrollments
 */
import apiClient from '@/lib/api';
import type { Course } from '@/types';

// API prefix for enrollments
const API_PREFIX = '/v1/enrollments';

export interface EnrollmentResponse {
  id: number;
  courseId: number;
  studentId?: number;
  studentName?: string;
  studentEmail?: string;
  courseTitle?: string;
  instructorName?: string;
  enrolledAt: string;
  progress: number; // 0-100%
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'DROPPED' | 'SUSPENDED';
  currentScore?: number;
  completedAt?: string;
  lastAccessedAt?: string;
  completedLessons?: number;
  totalLessons?: number;
  isPaid?: boolean;
  paidAmount?: number;
  course?: Course;
}

export interface EnrollmentCreateRequest {
  courseId: number;
  studentId: number;
}

export interface EnrollmentUpdateRequest {
  status?: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'DROPPED';
  progress?: number;
}

export interface StudentLearningHistory {
  totalEnrollments: number;
  completedCourses: number;
  inProgressCourses: number;
  droppedCourses: number;
  averageProgress: number;
  enrollments: EnrollmentResponse[];
}

export interface MonthlyStudentStats {
  year: number;
  monthlyData: Array<{
    month: number;
    newEnrollments: number;
    completions: number;
  }>;
}

/**
 * Enroll in a course
 */
export const enrollCourse = async (courseId: number, studentId: number): Promise<EnrollmentResponse> => {
  const response = await apiClient.post<EnrollmentResponse>(API_PREFIX, {
    courseId,
    studentId,
  });
  return response.data;
};

/**
 * Get enrollments by course ID
 */
export const getEnrollmentsByCourse = async (
  courseId: number,
  page: number = 0,
  size: number = 20
): Promise<{ content: EnrollmentResponse[]; totalElements: number; totalPages: number }> => {
  const response = await apiClient.get(`${API_PREFIX}/course/${courseId}`, {
    params: { page, size },
  });
  return response.data;
};

/**
 * Get enrollments by student ID
 */
export const getEnrollmentsByStudent = async (
  studentId: number,
  page: number = 0,
  size: number = 20
): Promise<{ content: EnrollmentResponse[]; totalElements: number; totalPages: number }> => {
  const response = await apiClient.get(`${API_PREFIX}/student/${studentId}`, {
    params: { page, size },
  });
  return response.data;
};

/**
 * Get enrollment by ID
 */
export const getEnrollmentById = async (enrollmentId: number): Promise<EnrollmentResponse> => {
  const response = await apiClient.get<EnrollmentResponse>(`${API_PREFIX}/${enrollmentId}`);
  return response.data;
};

/**
 * Update enrollment status/progress
 */
export const updateEnrollment = async (
  enrollmentId: number,
  data: EnrollmentUpdateRequest
): Promise<EnrollmentResponse> => {
  const response = await apiClient.patch<EnrollmentResponse>(`${API_PREFIX}/${enrollmentId}`, data);
  return response.data;
};

/**
 * Remove enrollment (Admin only)
 */
export const removeEnrollment = async (enrollmentId: number): Promise<void> => {
  await apiClient.delete(`${API_PREFIX}/${enrollmentId}`);
};

/**
 * Get student learning history
 */
export const getStudentLearningHistory = async (studentId: number): Promise<StudentLearningHistory> => {
  const response = await apiClient.get<StudentLearningHistory>(`${API_PREFIX}/student/${studentId}/history`);
  return response.data;
};

/**
 * Get monthly student statistics
 */
export const getMonthlyStudentStats = async (year: number = 2025): Promise<MonthlyStudentStats> => {
  const response = await apiClient.get<MonthlyStudentStats>(`${API_PREFIX}/stats/monthly`, {
    params: { year },
  });
  return response.data;
};

// =====================
// LEGACY FUNCTIONS (kept for backward compatibility)
// =====================

/**
 * Complete lesson
 * Note: Use content API POST /api/content/lessons/{lessonId}/complete instead
 */
export const completeLesson = async (lessonId: number) => {
  const response = await apiClient.post(`/content/lessons/${lessonId}/complete`);
  return response.data;
};

/**
 * Get course progress
 * Note: Progress is included in enrollment data
 */
export const getCourseProgress = async (courseId: number) => {
  try {
    const response = await getEnrollmentsByCourse(courseId, 0, 1);
    const enrollment = response.content.length > 0 ? response.content[0] : null;
    return enrollment ? { progress: enrollment.progress } : { progress: 0 };
  } catch (error) {
    return { progress: 0 };
  }
};
