/**
 * Content Management Service
 * API Base: 
 * - Access: /api/content
 * - Management: /api/manage/content
 * 
 * Backend uses Chapter/Lesson hierarchical model
 */
import apiClient from '@/lib/api';

// =====================
// TYPES - Matching Backend Models
// =====================

export interface Lesson {
  id: number;
  chapterId: number;
  title: string;
  description?: string;
  contentType: 'VIDEO' | 'TEXT' | 'DOCUMENT' | 'QUIZ';
  contentUrl?: string;
  duration?: number; // in minutes
  orderIndex: number;
  isFree: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Chapter {
  id: number;
  courseId: number;
  title: string;
  description?: string;
  orderIndex: number;
  lessons?: Lesson[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ChapterResponse {
  id: number;
  title: string;
  position: number;
  lessons: LessonResponse[];
}

export interface LessonResponse {
  id: number;
  title: string;
  contentType: 'VIDEO' | 'YOUTUBE' | 'TEXT' | 'DOCUMENT' | 'SLIDE';
  videoUrl?: string;
  documentUrl?: string;
  slideUrl?: string;
  content?: string;
  durationInMinutes?: number;
  position: number;
  isPreview?: boolean; // Cho phép giảng viên preview bài học trước khi publish
  isCompleted?: boolean;
}

export interface ChapterRequest {
  title: string;
  position: number;
}

export interface LessonRequest {
  title: string;
  contentType: 'VIDEO' | 'YOUTUBE' | 'TEXT' | 'DOCUMENT' | 'SLIDE';
  videoUrl?: string;
  documentUrl?: string;
  slideUrl?: string;
  content?: string; // For TEXT content type
  durationInMinutes?: number; // Optional vì YouTube không cần duration
  position: number;
  isPreview?: boolean; // Cho phép giảng viên preview bài học trước khi publish
}

export interface UserProgress {
  lessonId: number;
  completed: boolean;
  completedAt?: string;
}

// =====================
// CONTENT ACCESS API (/api/content)
// For Students and Instructors to view content
// =====================

/**
 * Get full course content (chapters and lessons)
 * Requires authentication - user must be enrolled or instructor
 */
export const getCourseContent = async (courseId: number): Promise<ChapterResponse[]> => {
  // Use /api/content endpoint for students (allows enrolled users or instructors)
  const response = await apiClient.get<ChapterResponse[]>(`/content/courses/${courseId}`);
  return response.data;
};

/**
 * Get preview lesson (first lesson) of a paid course
 * Public API - no authentication required
 * Returns null if course is free or has no preview lesson
 */
export const getPreviewLesson = async (courseId: number): Promise<LessonResponse | null> => {
  try {
    const response = await apiClient.get<LessonResponse>(`/content/courses/${courseId}/preview`);
    return response.data;
  } catch (error: any) {
    // Return null if not found (404) or any error
    if (error.response?.status === 404) {
      return null;
    }
    console.error('Error fetching preview lesson:', error);
    return null;
  }
};

/**
 * Mark lesson as completed (Student only)
 */
export const markLessonAsCompleted = async (lessonId: number): Promise<{ message: string }> => {
  const response = await apiClient.post(`/content/lessons/${lessonId}/complete`);
  return response.data;
};

/**
 * Update lesson watch time/progress (for video lessons)
 */
export const updateLessonProgress = async (
  lessonId: number,
  watchedTime: number, // seconds
  totalDuration: number // seconds
): Promise<{ message: string }> => {
  const response = await apiClient.post(`/content/lessons/${lessonId}/progress`, {
    watchedTime: Math.round(watchedTime),
    totalDuration: Math.round(totalDuration),
  });
  return response.data;
};

// =====================
// CONTENT MANAGEMENT API (/api/manage/content)
// For Instructors and Admins to manage content
// =====================

// --- CHAPTER MANAGEMENT ---

/**
 * Create a new chapter in a course
 */
export const createChapter = async (courseId: number, data: ChapterRequest): Promise<ChapterResponse> => {
  const response = await apiClient.post<{ chapter: ChapterResponse }>(`/v1/courses/${courseId}/chapters`, data);
  return response.data.chapter;
};

/**
 * Update a chapter
 */
export const updateChapter = async (courseId: number, chapterId: number, data: ChapterRequest): Promise<ChapterResponse> => {
  const response = await apiClient.put<{ chapter: ChapterResponse }>(`/v1/courses/${courseId}/chapters/${chapterId}`, data);
  return response.data.chapter;
};

/**
 * Delete a chapter
 */
export const deleteChapter = async (courseId: number, chapterId: number): Promise<{ message: string }> => {
  const response = await apiClient.delete(`/v1/courses/${courseId}/chapters/${chapterId}`);
  return response.data;
};

// --- LESSON MANAGEMENT ---

/**
 * Create a new lesson in a chapter
 */
export const createLesson = async (courseId: number, chapterId: number, data: LessonRequest): Promise<LessonResponse> => {
  const response = await apiClient.post<{ lesson: LessonResponse }>(`/v1/courses/${courseId}/chapters/${chapterId}/lessons`, data);
  return response.data.lesson;
};

/**
 * Update a lesson
 */
export const updateLesson = async (courseId: number, chapterId: number, lessonId: number, data: LessonRequest): Promise<LessonResponse> => {
  const response = await apiClient.put<{ lesson: LessonResponse }>(`/v1/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}`, data);
  return response.data.lesson;
};

/**
 * Delete a lesson
 */
export const deleteLesson = async (courseId: number, chapterId: number, lessonId: number): Promise<{ message: string }> => {
  const response = await apiClient.delete(`/v1/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}`);
  return response.data;
};

/**
 * Preview a lesson (for instructors only)
 */
export const previewLesson = async (lessonId: number): Promise<LessonResponse> => {
  const response = await apiClient.get<LessonResponse>(`/manage/content/lessons/${lessonId}/preview`);
  return response.data;
};

/**
 * Upload video file for a lesson
 * Note: Longer timeout for large video files
 * @param durationInSeconds Optional duration in seconds (will be extracted from video if not provided)
 */
export const uploadLessonVideo = async (
  courseId: number, 
  chapterId: number, 
  lessonId: number, 
  file: File,
  durationInSeconds?: number
): Promise<string> => {
  try {
    console.log('Uploading video to backend:', {
      courseId,
      chapterId,
      lessonId,
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type,
      durationInSeconds
    });
    
    const formData = new FormData();
    formData.append('file', file);
    if (durationInSeconds !== undefined && durationInSeconds > 0) {
      formData.append('durationInSeconds', durationInSeconds.toString());
    }
    
    const response = await apiClient.post<{ videoUrl: string }>(
      `/v1/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}/upload-video`, 
      formData,
      { timeout: 600000 } // 10 minutes timeout for large videos
    );
    
    console.log('Video upload response:', {
      videoUrl: response.data.videoUrl,
      status: response.status
    });
    
    return response.data.videoUrl;
  } catch (error: any) {
    console.error('Video upload error details:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      url: error.config?.url
    });
    throw error;
  }
};

/**
 * Extract duration from video file
 */
export const extractVideoDuration = (file: File): Promise<number> => {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video');
    video.preload = 'metadata';
    
    video.onloadedmetadata = () => {
      window.URL.revokeObjectURL(video.src);
      resolve(Math.floor(video.duration));
    };
    
    video.onerror = () => {
      window.URL.revokeObjectURL(video.src);
      reject(new Error('Failed to load video metadata'));
    };
    
    video.src = URL.createObjectURL(file);
  });
};

/**
 * Round duration in seconds to minutes
 * Rules: >= 30 seconds round up, < 30 seconds round down
 * Example: 378s (6:18) → 6 minutes, 349s (5:49) → 6 minutes
 */
export const roundDurationToMinutes = (durationInSeconds: number): number => {
  if (durationInSeconds <= 0) return 0;
  const minutes = durationInSeconds / 60;
  return Math.round(minutes);
};

/**
 * Extract duration from YouTube URL using backend API
 * Backend will use YouTube Data API v3 if API key is available
 */
/**
 * Extract duration từ YouTube URL (gọi backend API)
 * @param courseId ID của course (cần cho endpoint)
 * @param youtubeUrl URL của YouTube video
 * @returns Số phút (đã làm tròn), hoặc null nếu không thể extract
 */
export const extractYouTubeDuration = async (courseId: number, youtubeUrl: string): Promise<number | null> => {
  try {
    // baseURL đã có /api rồi, nên không cần thêm /api ở đây
    const response = await apiClient.get(`/v1/courses/${courseId}/chapters/extract-youtube-duration`, {
      params: { url: youtubeUrl }
    });
    return response.data.durationInMinutes || null;
  } catch (error: any) {
    console.warn('Failed to extract YouTube duration:', error);
    return null;
  }
};

/**
 * Upload document file for a lesson
 */
export const uploadLessonDocument = async (courseId: number, chapterId: number, lessonId: number, file: File): Promise<string> => {
  try {
    console.log('Uploading document to backend:', {
      courseId,
      chapterId,
      lessonId,
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type
    });
    
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<{ documentUrl: string }>(
      `/v1/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}/upload-document`, 
      formData,
      { timeout: 120000 } // 2 minutes timeout for documents
    );
    
    console.log('Document upload response:', {
      documentUrl: response.data.documentUrl,
      status: response.status
    });
    
    return response.data.documentUrl;
  } catch (error: any) {
    console.error('Document upload error details:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      url: error.config?.url
    });
    throw error;
  }
};

/**
 * Upload slide file for a lesson
 * Note: Longer timeout because PPT/PPTX files are converted to PDF on server
 */
export const uploadLessonSlide = async (courseId: number, chapterId: number, lessonId: number, file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await apiClient.post<{ slideUrl: string }>(
    `/v1/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}/upload-slide`, 
    formData,
    { timeout: 300000 } // 5 minutes timeout for slide conversion
  );
  return response.data.slideUrl;
};

/**
 * Reorder chapters
 */
export const reorderChapters = async (courseId: number, chapterPositions: Record<number, number>): Promise<{ message: string }> => {
  const response = await apiClient.patch(`/v1/courses/${courseId}/chapters/reorder`, chapterPositions);
  return response.data;
};

/**
 * Reorder lessons in a chapter
 */
export const reorderLessons = async (courseId: number, chapterId: number, lessonPositions: Record<number, number>): Promise<{ message: string }> => {
  const response = await apiClient.patch(`/v1/courses/${courseId}/chapters/${chapterId}/lessons/reorder`, lessonPositions);
  return response.data;
};

// --- IMPORT/EXPORT ---

/**
 * Export course content to Excel
 */
export const exportCourseContent = async (courseId: number): Promise<Blob> => {
  const response = await apiClient.get(`/manage/content/courses/${courseId}/export`, {
    responseType: 'blob',
  });
  return response.data;
};

/**
 * Import course content from Excel
 */
export const importCourseContent = async (
  courseId: number,
  file: File,
  onProgress?: (progress: number) => void
): Promise<{ message: string }> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post(`/manage/content/courses/${courseId}/import`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(progress);
      }
    },
  });

  return response.data;
};

// =====================
// HELPER FUNCTIONS
// =====================

/**
 * Download export file
 */
export const downloadExportFile = (blob: Blob, courseId: number) => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `NoiDungKhoaHoc_${courseId}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

/**
 * Calculate total course duration from chapters
 */
export const calculateCourseDuration = (chapters: ChapterResponse[]): number => {
  return chapters.reduce((total, chapter) => {
    const chapterDuration = chapter.lessons.reduce((sum, lesson) => {
      return sum + ((lesson as any).duration || 0);
    }, 0);
    return total + chapterDuration;
  }, 0);
};

/**
 * Count total lessons in course
 */
export const countTotalLessons = (chapters: ChapterResponse[]): number => {
  return chapters.reduce((total, chapter) => total + chapter.lessons.length, 0);
};

/**
 * Calculate completion percentage
 */
export const calculateCompletionPercentage = (chapters: ChapterResponse[]): number => {
  const totalLessons = countTotalLessons(chapters);
  if (totalLessons === 0) return 0;

  const completedLessons = chapters.reduce((total, chapter) => {
    return total + chapter.lessons.filter((lesson) => lesson.isCompleted).length;
  }, 0);

  return Math.round((completedLessons / totalLessons) * 100);
};

// =====================
// LEGACY FUNCTIONS (kept for backward compatibility)
// These map old API calls to new structure
// =====================

// =====================
// EXPORTS
// =====================

export default {
  // Content Access
  getCourseContent,
  markLessonAsCompleted,

  // Chapter Management
  createChapter,
  updateChapter,
  deleteChapter,

  // Lesson Management
  createLesson,
  updateLesson,
  deleteLesson,

  // Import/Export
  exportCourseContent,
  importCourseContent,

  // Helpers
  downloadExportFile,
  calculateCourseDuration,
  countTotalLessons,
  calculateCompletionPercentage,
};
