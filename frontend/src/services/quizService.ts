/**
 * Quiz/Test Service
 * API Base:
 * - Student Access: /api/tests
 * - Management: /api/manage/tests
 * 
 * Backend uses Test/Test_Question/Test_Result models
 */
import apiClient from '@/lib/api';

// =====================
// TYPES - Matching Backend Models
// =====================

export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'ESSAY';
export type TestType = 'QUIZ' | 'ASSIGNMENT' | 'EXAM' | 'PRACTICE';

export interface AnswerOption {
  id: number;
  questionId: number;
  optionText: string;
  isCorrect: boolean;
  orderIndex: number;
}

export interface Question {
  id: number;
  testId: number;
  questionText: string;
  questionType: QuestionType;
  points: number;
  orderIndex: number;
  options?: AnswerOption[];
  correctAnswer?: string; // For SHORT_ANSWER type
  explanation?: string;
}

export interface Test {
  id: number;
  lessonId: number;
  title: string;
  description?: string;
  testType: TestType;
  timeLimit?: number; // in minutes
  passingScore: number; // percentage 0-100
  maxAttempts?: number;
  shuffleQuestions: boolean;
  shuffleAnswers: boolean;
  showResults: boolean;
  questions?: Question[];
  createdAt?: string;
  updatedAt?: string;
}

export interface TestResult {
  id: number;
  testId: number;
  userId: number;
  score: number;
  totalPoints: number;
  percentage: number;
  isPassed: boolean;
  attemptNumber: number;
  startedAt: string;
  submittedAt?: string;
  timeSpent?: number; // in seconds
  answers?: TestResultAnswer[];
}

export interface TestResultAnswer {
  id: number;
  resultId: number;
  questionId: number;
  answerText: string;
  isCorrect?: boolean;
  points?: number;
  feedback?: string;
}

export interface TestStatistics {
  testId: number;
  totalAttempts: number;
  passedAttempts: number;
  failedAttempts: number;
  averageScore: number;
  highestScore: number;
  lowestScore: number;
  averageTimeSpent: number;
}

// =====================
// REQUEST/RESPONSE TYPES
// =====================

export interface TestRequest {
  title: string;
  description?: string;
  testType: TestType;
  timeLimit?: number;
  passingScore: number;
  maxAttempts?: number;
  shuffleQuestions?: boolean;
  shuffleAnswers?: boolean;
  showResults?: boolean;
  questions?: QuestionRequest[];
}

export interface QuestionRequest {
  questionText: string;
  questionType: QuestionType;
  points: number;
  orderIndex: number;
  correctAnswer?: string;
  explanation?: string;
  options?: {
    optionText: string;
    isCorrect: boolean;
    orderIndex: number;
  }[];
}

export interface TestSubmissionRequest {
  answers: {
    questionId: number;
    answerText: string;
  }[];
}

export interface ManualGradeRequest {
  resultId: number;
  answerId: number;
  score: number;
  feedback?: string;
}

// =====================
// STUDENT ACCESS API (/api/tests)
// =====================

/**
 * Get test for student to take
 * Note: Correct answers are hidden
 */
export const getTestForStudent = async (testId: number): Promise<Test> => {
  const response = await apiClient.get<Test>(`/tests/${testId}`);
  return response.data;
};

/**
 * Submit test answers
 */
export const submitTest = async (
  testId: number,
  data: TestSubmissionRequest
): Promise<TestResult> => {
  const response = await apiClient.post<TestResult>(`/tests/${testId}/submit`, data);
  return response.data;
};

/**
 * Get student's test result
 */
export const getTestResult = async (testId: number): Promise<TestResult> => {
  const response = await apiClient.get<TestResult>(`/tests/${testId}/result`);
  return response.data;
};

// =====================
// MANAGEMENT API (/api/manage/tests)
// For Instructors and Admins
// =====================

/**
 * Create a new test for a lesson
 */
export const createTest = async (lessonId: number, data: TestRequest): Promise<Test> => {
  const response = await apiClient.post<Test>(`/manage/tests/lessons/${lessonId}`, data);
  return response.data;
};

/**
 * Get test details for management (includes correct answers)
 */
export const getTestForManagement = async (testId: number): Promise<Test> => {
  const response = await apiClient.get<Test>(`/manage/tests/${testId}`);
  return response.data;
};

/**
 * Get all submissions for a test
 */
export const getTestSubmissions = async (testId: number): Promise<TestResult[]> => {
  const response = await apiClient.get<TestResult[]>(`/manage/tests/${testId}/submissions`);
  return response.data;
};

/**
 * Grade essay question manually
 */
export const gradeEssayQuestion = async (data: ManualGradeRequest): Promise<TestResult> => {
  const response = await apiClient.post<TestResult>('/manage/tests/grade-essay', data);
  return response.data;
};

/**
 * Get test statistics
 */
export const getTestStatistics = async (testId: number): Promise<TestStatistics> => {
  const response = await apiClient.get<TestStatistics>(`/manage/tests/${testId}/statistics`);
  return response.data;
};

// =====================
// HELPER FUNCTIONS
// =====================

/**
 * Calculate time remaining in seconds
 */
export const calculateTimeRemaining = (
  startedAt: string,
  timeLimitMinutes: number
): number => {
  const start = new Date(startedAt).getTime();
  const now = Date.now();
  const elapsed = Math.floor((now - start) / 1000);
  const remaining = timeLimitMinutes * 60 - elapsed;
  return Math.max(0, remaining);
};

/**
 * Format time as MM:SS
 */
export const formatTime = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * Check if answer is correct (for auto-grading types)
 */
export const isAnswerCorrect = (
  question: Question,
  answer: string
): boolean => {
  if (question.questionType === 'SHORT_ANSWER') {
    return question.correctAnswer?.toLowerCase().trim() === answer.toLowerCase().trim();
  }
  if (question.questionType === 'TRUE_FALSE') {
    return question.correctAnswer === answer;
  }
  if (question.options) {
    const correctOption = question.options.find((opt) => opt.isCorrect);
    return correctOption?.optionText === answer;
  }
  return false;
};

/**
 * Calculate test score from answers
 */
export const calculateScore = (
  questions: Question[],
  answers: { questionId: number; answerText: string }[]
): { score: number; total: number; percentage: number } => {
  let score = 0;
  let total = 0;

  questions.forEach((question) => {
    // Skip essay questions - need manual grading
    if (question.questionType === 'ESSAY') return;

    total += question.points;
    const answer = answers.find((a) => a.questionId === question.id);
    if (answer && isAnswerCorrect(question, answer.answerText)) {
      score += question.points;
    }
  });

  return {
    score,
    total,
    percentage: total > 0 ? Math.round((score / total) * 100) : 0,
  };
};

// =====================
// EXPORTS
// =====================

export default {
  // Student Access
  getTestForStudent,
  submitTest,
  getTestResult,

  // Management
  createTest,
  getTestForManagement,
  getTestSubmissions,
  gradeEssayQuestion,
  getTestStatistics,

  // Helpers
  calculateTimeRemaining,
  formatTime,
  isAnswerCorrect,
  calculateScore,
};
