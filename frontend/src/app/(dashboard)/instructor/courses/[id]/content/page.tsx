'use client';

import React from 'react';
import { useParams, useRouter } from 'next/navigation';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { ArrowLeft, Plus, Edit, Trash2, GripVertical, Video, FileText, BookOpen, Upload, X, Eye } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/lib/api';
import { useUIStore } from '@/stores/uiStore';
import { isYouTubeUrl, getYouTubeEmbedUrl } from '@/lib/utils';
import { ROUTES } from '@/lib/constants';
import type { ChapterResponse, LessonResponse, ChapterRequest, LessonRequest } from '@/services/contentService';
import { createChapter, updateChapter, deleteChapter, createLesson, updateLesson, deleteLesson, uploadLessonVideo, uploadLessonDocument, uploadLessonSlide, reorderChapters, reorderLessons, previewLesson as previewLessonApi } from '@/services/contentService';
import { useCourse } from '@/hooks/useCourses';

// Dialog components for creating/editing chapters and lessons
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export default function CourseContentPage() {
  const params = useParams();
  const router = useRouter();
  const courseId = params.id as string;
  const { course, isLoading: isLoadingCourse } = useCourse(courseId);
  const { addToast } = useUIStore();
  const queryClient = useQueryClient();

  // State for dialogs
  const [chapterDialogOpen, setChapterDialogOpen] = React.useState(false);
  const [lessonDialogOpen, setLessonDialogOpen] = React.useState(false);
  const [previewDialogOpen, setPreviewDialogOpen] = React.useState(false);
  const [previewLesson, setPreviewLesson] = React.useState<LessonResponse | null>(null);
  const [editingChapter, setEditingChapter] = React.useState<ChapterResponse | null>(null);
  const [editingLesson, setEditingLesson] = React.useState<{ chapterId: number; lesson: LessonResponse } | null>(null);
  const [selectedChapterId, setSelectedChapterId] = React.useState<number | null>(null);

  // Fetch chapters
  const { data: chapters = [], isLoading, refetch } = useQuery<ChapterResponse[]>({
    queryKey: ['course-chapters', courseId],
    queryFn: async () => {
      const response = await apiClient.get<ChapterResponse[]>(`/v1/courses/${courseId}/chapters`);
      return response.data;
    },
  });

  // Chapter mutations
  const createChapterMutation = useMutation({
    mutationFn: (data: ChapterRequest) => createChapter(Number(courseId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      setChapterDialogOpen(false);
      addToast({ type: 'success', description: 'Chương đã được tạo thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi tạo chương' });
    },
  });

  const updateChapterMutation = useMutation({
    mutationFn: ({ chapterId, data }: { chapterId: number; data: ChapterRequest }) =>
      updateChapter(Number(courseId), chapterId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      setChapterDialogOpen(false);
      setEditingChapter(null);
      addToast({ type: 'success', description: 'Chương đã được cập nhật thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi cập nhật chương' });
    },
  });

  const deleteChapterMutation = useMutation({
    mutationFn: (chapterId: number) => deleteChapter(Number(courseId), chapterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      addToast({ type: 'success', description: 'Chương đã được xóa thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi xóa chương' });
    },
  });

  // Lesson mutations
  const createLessonMutation = useMutation({
    mutationFn: async ({ chapterId, data, videoFile, documentFile, slideFile }: { 
      chapterId: number; 
      data: LessonRequest; 
      videoFile?: File;
      documentFile?: File;
      slideFile?: File;
    }) => {
      // Step 1: Create the lesson
      const createdLesson = await createLesson(Number(courseId), chapterId, data);
      
      // Step 2: Upload file if provided
      if (videoFile && createdLesson.id) {
        try {
          const videoUrl = await uploadLessonVideo(Number(courseId), chapterId, createdLesson.id, videoFile);
          // Step 3: Update lesson with the video URL
          await updateLesson(Number(courseId), chapterId, createdLesson.id, {
            ...data,
            videoUrl,
          });
        } catch (uploadError: any) {
          console.error('Video upload failed:', uploadError);
          throw new Error(`Bài học đã tạo nhưng upload video thất bại: ${uploadError.message || 'Lỗi không xác định'}`);
        }
      }
      
      if (documentFile && createdLesson.id) {
        try {
          const documentUrl = await uploadLessonDocument(Number(courseId), chapterId, createdLesson.id, documentFile);
          // Step 3: Update lesson with the document URL
          await updateLesson(Number(courseId), chapterId, createdLesson.id, {
            ...data,
            documentUrl,
          });
        } catch (uploadError: any) {
          console.error('Document upload failed:', uploadError);
          throw new Error(`Bài học đã tạo nhưng upload tài liệu thất bại: ${uploadError.message || 'Lỗi không xác định'}`);
        }
      }

      if (slideFile && createdLesson.id) {
        try {
          const slideUrl = await uploadLessonSlide(Number(courseId), chapterId, createdLesson.id, slideFile);
          // Step 3: Update lesson with the slide URL
          await updateLesson(Number(courseId), chapterId, createdLesson.id, {
            ...data,
            slideUrl,
          });
        } catch (uploadError: any) {
          console.error('Slide upload failed:', uploadError);
          throw new Error(`Bài học đã tạo nhưng upload slide thất bại: ${uploadError.message || 'Lỗi không xác định'}`);
        }
      }
      
      return createdLesson;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      setLessonDialogOpen(false);
      setSelectedChapterId(null);
      addToast({ type: 'success', description: 'Bài học đã được tạo thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.message || error.response?.data?.message || 'Lỗi khi tạo bài học' });
    },
  });

  const updateLessonMutation = useMutation({
    mutationFn: ({ chapterId, lessonId, data }: { chapterId: number; lessonId: number; data: LessonRequest }) =>
      updateLesson(Number(courseId), chapterId, lessonId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      setLessonDialogOpen(false);
      setEditingLesson(null);
      addToast({ type: 'success', description: 'Bài học đã được cập nhật thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi cập nhật bài học' });
    },
  });

  const deleteLessonMutation = useMutation({
    mutationFn: ({ chapterId, lessonId }: { chapterId: number; lessonId: number }) =>
      deleteLesson(Number(courseId), chapterId, lessonId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      addToast({ type: 'success', description: 'Bài học đã được xóa thành công!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi xóa bài học' });
    },
  });

  // Reorder mutations
  const reorderChaptersMutation = useMutation({
    mutationFn: (chapterPositions: Record<number, number>) =>
      reorderChapters(Number(courseId), chapterPositions),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      addToast({ type: 'success', description: 'Thứ tự chương đã được cập nhật!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi cập nhật thứ tự chương' });
    },
  });

  const reorderLessonsMutation = useMutation({
    mutationFn: ({ chapterId, lessonPositions }: { chapterId: number; lessonPositions: Record<number, number> }) =>
      reorderLessons(Number(courseId), chapterId, lessonPositions),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['course-chapters', courseId] });
      addToast({ type: 'success', description: 'Thứ tự bài học đã được cập nhật!' });
    },
    onError: (error: any) => {
      addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi cập nhật thứ tự bài học' });
    },
  });

  const handleCreateChapter = () => {
    setEditingChapter(null);
    setChapterDialogOpen(true);
  };

  const handleEditChapter = (chapter: ChapterResponse) => {
    setEditingChapter(chapter);
    setChapterDialogOpen(true);
  };

  const handleDeleteChapter = (chapterId: number, chapterTitle: string) => {
    if (!confirm(`Bạn có chắc chắn muốn xóa chương "${chapterTitle}"? Tất cả bài học trong chương này cũng sẽ bị xóa.`)) {
      return;
    }
    deleteChapterMutation.mutate(chapterId);
  };

  const handleCreateLesson = (chapterId: number) => {
    setSelectedChapterId(chapterId);
    setEditingLesson(null);
    setLessonDialogOpen(true);
  };

  const handleEditLesson = (chapterId: number, lesson: LessonResponse) => {
    setSelectedChapterId(chapterId);
    setEditingLesson({ chapterId, lesson });
    setLessonDialogOpen(true);
  };

  const handleDeleteLesson = (chapterId: number, lessonId: number, lessonTitle: string) => {
    if (!confirm(`Bạn có chắc chắn muốn xóa bài học "${lessonTitle}"?`)) {
      return;
    }
    deleteLessonMutation.mutate({ chapterId, lessonId });
  };

  const handlePreviewLesson = async (lessonId: number) => {
    try {
      const lesson = await previewLessonApi(lessonId);
      setPreviewLesson(lesson);
      setPreviewDialogOpen(true);
    } catch (error: any) {
      console.error('Preview lesson error:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Lỗi khi tải bài học để preview';
      addToast({ type: 'error', description: errorMessage });
    }
  };


  if (isLoadingCourse) {
    return (
      <div className="p-6 space-y-6">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => router.push(ROUTES.INSTRUCTOR.COURSES)}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Quay lại
          </Button>
          <div>
            <h1 className="text-3xl font-bold font-poppins">Quản lý nội dung khóa học</h1>
            <p className="text-muted-foreground mt-1">
              {course?.title || 'Đang tải...'}
            </p>
          </div>
        </div>
        <Button onClick={handleCreateChapter}>
          <Plus className="h-4 w-4 mr-2" />
          Thêm chương mới
        </Button>
      </div>

      {/* Chapters List */}
      {isLoading ? (
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <Skeleton key={i} className="h-48 w-full" />
          ))}
        </div>
      ) : chapters.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <BookOpen className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Chưa có nội dung</h3>
            <p className="text-muted-foreground mb-4">
              Bắt đầu bằng cách thêm chương đầu tiên cho khóa học của bạn.
            </p>
            <Button onClick={handleCreateChapter}>
              <Plus className="h-4 w-4 mr-2" />
              Thêm chương đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <DragDropContext
          onDragEnd={(result: DropResult) => {
            if (!result.destination) return;

            // Handle chapter reordering
            if (result.type === 'chapter') {
              const items = Array.from(chapters);
              const [reorderedItem] = items.splice(result.source.index, 1);
              items.splice(result.destination.index, 0, reorderedItem);

              // Update positions
              const chapterPositions: Record<number, number> = {};
              items.forEach((chapter, index) => {
                chapterPositions[chapter.id] = index + 1;
              });

              reorderChaptersMutation.mutate(chapterPositions);
            }
          }}
        >
          <Droppable droppableId="chapters" type="chapter">
            {(provided) => (
              <div {...provided.droppableProps} ref={provided.innerRef} className="space-y-4">
                {chapters.map((chapter, index) => (
                  <Draggable key={chapter.id} draggableId={`chapter-${chapter.id}`} index={index}>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.draggableProps}
                        className={snapshot.isDragging ? 'opacity-50' : ''}
                      >
                        <ChapterCard
                          chapter={chapter}
                          dragHandleProps={provided.dragHandleProps}
                          onEdit={() => handleEditChapter(chapter)}
                          onDelete={() => handleDeleteChapter(chapter.id, chapter.title)}
                          onAddLesson={() => handleCreateLesson(chapter.id)}
                          onEditLesson={(lesson) => handleEditLesson(chapter.id, lesson)}
                          onDeleteLesson={(lessonId, lessonTitle) => handleDeleteLesson(chapter.id, lessonId, lessonTitle)}
                          onPreviewLesson={(lessonId) => handlePreviewLesson(lessonId)}
                          onReorderLessons={(lessonPositions) => {
                            reorderLessonsMutation.mutate({ chapterId: chapter.id, lessonPositions });
                          }}
                        />
                      </div>
                    )}
                  </Draggable>
                ))}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </DragDropContext>
      )}

      {/* Chapter Dialog */}
      <ChapterDialog
        open={chapterDialogOpen}
        onOpenChange={setChapterDialogOpen}
        chapter={editingChapter}
        chapters={chapters}
        onSubmit={(data) => {
          if (editingChapter) {
            updateChapterMutation.mutate({ chapterId: editingChapter.id, data });
          } else {
            createChapterMutation.mutate(data);
          }
        }}
        isLoading={createChapterMutation.isPending || updateChapterMutation.isPending}
      />

      {/* Lesson Dialog */}
      <LessonDialog
        open={lessonDialogOpen}
        onOpenChange={setLessonDialogOpen}
        lesson={editingLesson?.lesson || null}
        chapterId={selectedChapterId || editingLesson?.chapterId || null}
        chapters={chapters}
        onSubmit={(data, chapterId, videoFile, documentFile, slideFile) => {
          const finalChapterId = chapterId || selectedChapterId || editingLesson?.chapterId;
          if (!finalChapterId) return;
          
          if (editingLesson) {
            updateLessonMutation.mutate({ chapterId: finalChapterId, lessonId: editingLesson.lesson.id, data });
          } else {
            createLessonMutation.mutate({ chapterId: finalChapterId, data, videoFile, documentFile, slideFile });
          }
        }}
        isLoading={createLessonMutation.isPending || updateLessonMutation.isPending}
      />

      {/* Preview Lesson Dialog */}
      <PreviewLessonDialog
        open={previewDialogOpen}
        onOpenChange={setPreviewDialogOpen}
        lesson={previewLesson}
      />

    </div>
  );
}

// Preview Lesson Dialog Component
function PreviewLessonDialog({
  open,
  onOpenChange,
  lesson,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  lesson: LessonResponse | null;
}) {
  if (!lesson) return null;

  const renderContent = () => {
    switch (lesson.contentType) {
      case 'VIDEO':
        if (!lesson.videoUrl) return <p className="text-muted-foreground">Chưa có video</p>;
        if (isYouTubeUrl(lesson.videoUrl)) {
          return (
            <div className="aspect-video w-full">
              <iframe
                src={getYouTubeEmbedUrl(lesson.videoUrl)}
                className="w-full h-full rounded-lg"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            </div>
          );
        }
        return (
          <video
            src={lesson.videoUrl}
            controls
            className="w-full rounded-lg"
            style={{ maxHeight: '70vh' }}
          />
        );
      case 'TEXT':
        return (
          <div className="p-8 prose max-w-none">
            <div
              className="whitespace-pre-wrap"
              dangerouslySetInnerHTML={{ __html: lesson.content || 'Chưa có nội dung' }}
            />
          </div>
        );
      case 'DOCUMENT':
        if (!lesson.documentUrl) return <p className="text-muted-foreground">Chưa có tài liệu</p>;
        const getViewUrl = (url: string) => {
          if (url.includes('/api/files/lessons/documents/')) {
            return url.replace('/api/files/lessons/documents/', '/api/files/view/documents/');
          }
          const file = url.split('/').pop();
          return `http://localhost:8080/api/files/view/documents/${file}`;
        };
        const viewUrl = getViewUrl(lesson.documentUrl);
        return (
          <div className="w-full" style={{ height: '70vh' }}>
            <iframe src={viewUrl} className="w-full h-full rounded-lg" />
          </div>
        );
      case 'SLIDE':
        if (!lesson.slideUrl) return <p className="text-muted-foreground">Chưa có slide</p>;
        const getSlideViewUrl = (url: string) => {
          if (url.includes('/api/files/lessons/slides/')) {
            return url.replace('/api/files/lessons/slides/', '/api/files/view/slides/');
          }
          const file = url.split('/').pop();
          return `http://localhost:8080/api/files/view/slides/${file}`;
        };
        const slideViewUrl = getSlideViewUrl(lesson.slideUrl);
        return (
          <div className="w-full" style={{ height: '70vh' }}>
            <iframe src={slideViewUrl} className="w-full h-full rounded-lg" />
          </div>
        );
      default:
        return <p className="text-muted-foreground">Loại nội dung không được hỗ trợ</p>;
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-5xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Xem trước: {lesson.title}</DialogTitle>
          <DialogDescription>
            Đây là cách bài học sẽ hiển thị cho học viên
          </DialogDescription>
        </DialogHeader>
        <div className="mt-4">
          {renderContent()}
        </div>
        <DialogFooter>
          <Button type="button" onClick={() => onOpenChange(false)}>
            Đóng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// Chapter Card Component
function ChapterCard({
  chapter,
  dragHandleProps,
  onEdit,
  onDelete,
  onAddLesson,
  onEditLesson,
  onDeleteLesson,
  onPreviewLesson,
  onReorderLessons,
}: {
  chapter: ChapterResponse;
  dragHandleProps?: any;
  onEdit: () => void;
  onDelete: () => void;
  onAddLesson: () => void;
  onEditLesson: (lesson: LessonResponse) => void;
  onDeleteLesson: (lessonId: number, lessonTitle: string) => void;
  onPreviewLesson: (lessonId: number) => void;
  onReorderLessons: (lessonPositions: Record<number, number>) => void;
}) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div {...dragHandleProps}>
              <GripVertical className="h-5 w-5 text-muted-foreground cursor-move" />
            </div>
            <CardTitle className="text-xl">{chapter.title}</CardTitle>
            <Badge variant="secondary">Chương {chapter.position}</Badge>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm" onClick={onAddLesson}>
              <Plus className="h-4 w-4 mr-2" />
              Thêm bài học
            </Button>
            <Button variant="ghost" size="sm" onClick={onEdit}>
              <Edit className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="sm" onClick={onDelete}>
              <Trash2 className="h-4 w-4 text-destructive" />
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {chapter.lessons.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <p>Chưa có bài học nào trong chương này.</p>
            <Button variant="outline" size="sm" className="mt-4" onClick={onAddLesson}>
              <Plus className="h-4 w-4 mr-2" />
              Thêm bài học đầu tiên
            </Button>
          </div>
        ) : (
          <DragDropContext
            onDragEnd={(result: DropResult) => {
              if (!result.destination) return;

              if (result.type === 'lesson') {
                const items = Array.from(chapter.lessons);
                const [reorderedItem] = items.splice(result.source.index, 1);
                items.splice(result.destination.index, 0, reorderedItem);

                // Update positions
                const lessonPositions: Record<number, number> = {};
                items.forEach((lesson, index) => {
                  lessonPositions[lesson.id] = index + 1;
                });

                onReorderLessons(lessonPositions);
              }
            }}
          >
            <Droppable droppableId={`lessons-${chapter.id}`} type="lesson">
              {(provided) => (
                <div {...provided.droppableProps} ref={provided.innerRef} className="space-y-2">
                  {chapter.lessons.map((lesson, index) => (
                    <Draggable key={lesson.id} draggableId={`lesson-${lesson.id}`} index={index}>
                      {(provided, snapshot) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          className={snapshot.isDragging ? 'opacity-50' : ''}
                        >
                          <LessonItem
                            lesson={lesson}
                            dragHandleProps={provided.dragHandleProps}
                            onEdit={() => onEditLesson(lesson)}
                            onDelete={() => onDeleteLesson(lesson.id, lesson.title)}
                            onPreview={() => onPreviewLesson(lesson.id)}
                          />
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        )}
      </CardContent>
    </Card>
  );
}

// Lesson Item Component
function LessonItem({
  lesson,
  dragHandleProps,
  onEdit,
  onDelete,
  onPreview,
}: {
  lesson: LessonResponse;
  dragHandleProps?: any;
  onEdit: () => void;
  onDelete: () => void;
  onPreview: () => void;
}) {
  const getContentTypeIcon = () => {
    switch (lesson.contentType) {
      case 'VIDEO':
        return <Video className="h-4 w-4" />;
      case 'TEXT':
        return <FileText className="h-4 w-4" />;
      case 'DOCUMENT':
        return <FileText className="h-4 w-4" />;
      case 'SLIDE':
        return <FileText className="h-4 w-4" />;
      default:
        return <BookOpen className="h-4 w-4" />;
    }
  };

  const getContentTypeLabel = () => {
    switch (lesson.contentType) {
      case 'VIDEO':
        return 'Video';
      case 'TEXT':
        return 'Bài đọc';
      case 'DOCUMENT':
        return 'Tài liệu';
      case 'SLIDE':
        return 'Slide';
      default:
        return lesson.contentType;
    }
  };

  return (
    <div className="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50">
      <div className="flex items-center gap-3 flex-1">
        <div {...dragHandleProps}>
          <GripVertical className="h-4 w-4 text-muted-foreground cursor-move" />
        </div>
        {getContentTypeIcon()}
        <div className="flex-1">
          <p className="font-medium">{lesson.title}</p>
          <div className="flex items-center gap-2 mt-1">
            <Badge variant="outline" className="text-xs">
              {getContentTypeLabel()}
            </Badge>
            {lesson.durationInMinutes && (
              <span className="text-xs text-muted-foreground">
                {lesson.durationInMinutes} phút
              </span>
            )}
          </div>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="sm" onClick={onPreview} title="Xem trước">
          <Eye className="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="sm" onClick={onEdit}>
          <Edit className="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="sm" onClick={onDelete}>
          <Trash2 className="h-4 w-4 text-destructive" />
        </Button>
      </div>
    </div>
  );
}

// Chapter Dialog Component
function ChapterDialog({
  open,
  onOpenChange,
  chapter,
  chapters,
  onSubmit,
  isLoading,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  chapter: ChapterResponse | null;
  chapters: ChapterResponse[];
  onSubmit: (data: ChapterRequest) => void;
  isLoading: boolean;
}) {
  const [title, setTitle] = React.useState('');
  const [position, setPosition] = React.useState(1);

  React.useEffect(() => {
    if (chapter) {
      setTitle(chapter.title);
      setPosition(chapter.position);
    } else {
      setTitle('');
      setPosition(chapters.length + 1);
    }
  }, [chapter, chapters, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      return;
    }
    onSubmit({ title: title.trim(), position });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{chapter ? 'Chỉnh sửa chương' : 'Thêm chương mới'}</DialogTitle>
          <DialogDescription>
            {chapter ? 'Cập nhật thông tin chương' : 'Thêm một chương mới vào khóa học'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="chapter-title">Tiêu đề chương *</Label>
              <Input
                id="chapter-title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Ví dụ: Chương 1 - Giới thiệu"
                className="mt-2"
                required
              />
            </div>
            <div>
              <Label htmlFor="chapter-position">Thứ tự *</Label>
              <Input
                id="chapter-position"
                type="number"
                min="1"
                value={position}
                onChange={(e) => setPosition(Number(e.target.value))}
                className="mt-2"
                required
              />
              <p className="text-xs text-muted-foreground mt-1">
                Thứ tự hiển thị của chương trong khóa học
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Hủy
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? 'Đang lưu...' : chapter ? 'Cập nhật' : 'Tạo chương'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// Lesson Dialog Component
function LessonDialog({
  open,
  onOpenChange,
  lesson,
  chapterId,
  chapters,
  onSubmit,
  isLoading,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  lesson: LessonResponse | null;
  chapterId: number | null;
  chapters: ChapterResponse[];
  onSubmit: (data: LessonRequest, chapterId: number, videoFile?: File, documentFile?: File, slideFile?: File) => void;
  isLoading: boolean;
}) {
  const [title, setTitle] = React.useState('');
  const [contentType, setContentType] = React.useState<'VIDEO' | 'TEXT' | 'DOCUMENT' | 'SLIDE'>('VIDEO');
  const [videoUrl, setVideoUrl] = React.useState('');
  const [documentUrl, setDocumentUrl] = React.useState('');
  const [slideUrl, setSlideUrl] = React.useState('');
  const [content, setContent] = React.useState('');
  const [durationInMinutes, setDurationInMinutes] = React.useState(0);
  const [durationTouched, setDurationTouched] = React.useState(false);
  const [position, setPosition] = React.useState(1);
  const [selectedChapterId, setSelectedChapterId] = React.useState<number | null>(null);
  const [uploadingVideo, setUploadingVideo] = React.useState(false);
  const [uploadingDocument, setUploadingDocument] = React.useState(false);
  const [uploadingSlide, setUploadingSlide] = React.useState(false);
  // Store selected files for new lesson creation
  const [pendingVideoFile, setPendingVideoFile] = React.useState<File | null>(null);
  const [pendingDocumentFile, setPendingDocumentFile] = React.useState<File | null>(null);
  const [pendingSlideFile, setPendingSlideFile] = React.useState<File | null>(null);
  const params = useParams();
  const courseId = params.id as string;
  const { addToast } = useUIStore();

  React.useEffect(() => {
    if (lesson) {
      setTitle(lesson.title);
      setContentType(lesson.contentType);
      setVideoUrl(lesson.videoUrl || '');
      setDocumentUrl(lesson.documentUrl || '');
      setSlideUrl(lesson.slideUrl || '');
      setContent(lesson.content || '');
      setDurationInMinutes(lesson.durationInMinutes || 0);
      setDurationTouched(false);
      setPosition(lesson.position);
      setSelectedChapterId(chapterId);
      setPendingVideoFile(null);
      setPendingDocumentFile(null);
    } else {
      setTitle('');
      setContentType('VIDEO');
      setVideoUrl('');
      setDocumentUrl('');
      setSlideUrl('');
      setContent('');
      setDurationInMinutes(0);
      setDurationTouched(false);
      setSelectedChapterId(chapterId);
      setPendingVideoFile(null);
      setPendingDocumentFile(null);
      setPendingSlideFile(null);
      if (chapterId && chapters.length > 0) {
        const chapter = chapters.find(c => c.id === chapterId);
        if (chapter) {
          setPosition(chapter.lessons.length + 1);
        }
      }
    }
  }, [lesson, chapterId, chapters, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !selectedChapterId) {
      return;
    }

    // Validate duration
    if (!durationInMinutes || durationInMinutes <= 0) {
      addToast({ type: 'error', description: 'Vui lòng nhập thời lượng bài học (phải lớn hơn 0)' });
      return;
    }

    const data: LessonRequest = {
      title: title.trim(),
      contentType,
      position,
      durationInMinutes,
    };

    if (contentType === 'VIDEO') {
      data.videoUrl = videoUrl;
    } else if (contentType === 'DOCUMENT') {
      data.documentUrl = documentUrl;
    } else if (contentType === 'SLIDE') {
      data.slideUrl = slideUrl;
    } else if (contentType === 'TEXT') {
      data.content = content;
    }

    // Pass pending files along with the data
    onSubmit(
      data, 
      selectedChapterId,
      contentType === 'VIDEO' ? pendingVideoFile || undefined : undefined,
      contentType === 'DOCUMENT' ? pendingDocumentFile || undefined : undefined,
      contentType === 'SLIDE' ? pendingSlideFile || undefined : undefined
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{lesson ? 'Chỉnh sửa bài học' : 'Thêm bài học mới'}</DialogTitle>
          <DialogDescription>
            {lesson ? 'Cập nhật thông tin bài học' : 'Thêm một bài học mới vào chương'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            {!lesson && (
              <div>
                <Label htmlFor="lesson-chapter">Chương *</Label>
                <Select
                  value={selectedChapterId?.toString() || ''}
                  onValueChange={(value) => setSelectedChapterId(Number(value))}
                  required
                >
                  <SelectTrigger className="mt-2">
                    <SelectValue placeholder="Chọn chương" />
                  </SelectTrigger>
                  <SelectContent>
                    {chapters.map((chapter) => (
                      <SelectItem key={chapter.id} value={chapter.id.toString()}>
                        {chapter.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}
            <div>
              <Label htmlFor="lesson-title">Tiêu đề bài học *</Label>
              <Input
                id="lesson-title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Ví dụ: Bài 1 - Giới thiệu về..."
                className="mt-2"
                required
              />
            </div>
            <div>
              <Label htmlFor="lesson-type">Loại nội dung *</Label>
              <Select value={contentType} onValueChange={(value: 'VIDEO' | 'TEXT' | 'DOCUMENT' | 'SLIDE') => setContentType(value)}>
                <SelectTrigger className="mt-2">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="VIDEO">Video bài giảng</SelectItem>
                  <SelectItem value="TEXT">Bài đọc</SelectItem>
                  <SelectItem value="DOCUMENT">Tài liệu PDF</SelectItem>
                  <SelectItem value="SLIDE">Slide bài giảng</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {contentType === 'VIDEO' && (
              <div className="space-y-2">
                <Label>Video {!pendingVideoFile && !videoUrl && '*'}</Label>
                <div className="flex gap-2">
                  <Input
                    value={pendingVideoFile ? pendingVideoFile.name : videoUrl}
                    onChange={(e) => {
                      setVideoUrl(e.target.value);
                      setPendingVideoFile(null);
                    }}
                    placeholder="https://youtube.com/watch?v=... hoặc URL video khác"
                    className="flex-1"
                    disabled={!!pendingVideoFile}
                    required={!pendingVideoFile && !videoUrl}
                  />
                  <div className="relative">
                    <input
                      type="file"
                      accept="video/*"
                      className="hidden"
                      id="video-upload"
                      onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file || !selectedChapterId) return;
                        
                        // Validate file size (500MB)
                        if (file.size > 500 * 1024 * 1024) {
                          addToast({ type: 'error', description: 'File video không được vượt quá 500MB' });
                          e.target.value = '';
                          return;
                        }
                        
                        if (lesson) {
                          // Editing existing lesson: upload immediately
                          setUploadingVideo(true);
                          try {
                            const uploadedUrl = await uploadLessonVideo(Number(courseId), selectedChapterId, lesson.id, file);
                            setVideoUrl(uploadedUrl);
                            addToast({ type: 'success', description: 'Video đã được upload thành công!' });
                          } catch (error: any) {
                            addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi upload video' });
                          } finally {
                            setUploadingVideo(false);
                            e.target.value = '';
                          }
                        } else {
                          // Creating new lesson: store file for later upload
                          setPendingVideoFile(file);
                          setVideoUrl(''); // Clear URL since we're using file
                          addToast({ type: 'info', description: `Đã chọn video: ${file.name}. Video sẽ được upload khi tạo bài học.` });
                          e.target.value = '';
                        }
                      }}
                      disabled={uploadingVideo}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => document.getElementById('video-upload')?.click()}
                      disabled={uploadingVideo}
                    >
                      {uploadingVideo ? (
                        <>Đang upload...</>
                      ) : (
                        <>
                          <Upload className="h-4 w-4 mr-2" />
                          Upload
                        </>
                      )}
                    </Button>
                    {pendingVideoFile && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="ml-1"
                        onClick={() => setPendingVideoFile(null)}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
                <p className="text-xs text-muted-foreground">
                  {pendingVideoFile 
                    ? `File đã chọn: ${pendingVideoFile.name} (${(pendingVideoFile.size / 1024 / 1024).toFixed(2)} MB) - Sẽ upload khi tạo bài học`
                    : 'Bạn có thể upload file video hoặc nhập URL. File tối đa 500MB.'}
                </p>
              </div>
            )}
            {contentType === 'DOCUMENT' && (
              <div className="space-y-2">
                <Label>Tài liệu {!pendingDocumentFile && !documentUrl && '*'}</Label>
                <div className="flex gap-2">
                  <Input
                    value={pendingDocumentFile ? pendingDocumentFile.name : documentUrl}
                    onChange={(e) => {
                      setDocumentUrl(e.target.value);
                      setPendingDocumentFile(null);
                    }}
                    placeholder="https://example.com/document.pdf"
                    className="flex-1"
                    disabled={!!pendingDocumentFile}
                    required={!pendingDocumentFile && !documentUrl}
                  />
                  <div className="relative">
                    <input
                      type="file"
                      accept=".pdf"
                      className="hidden"
                      id="document-upload"
                      onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file || !selectedChapterId) return;
                        
                        // Validate file size (50MB)
                        if (file.size > 50 * 1024 * 1024) {
                          addToast({ type: 'error', description: 'File tài liệu không được vượt quá 50MB' });
                          e.target.value = '';
                          return;
                        }
                        
                        if (lesson) {
                          // Editing existing lesson: upload immediately
                          setUploadingDocument(true);
                          try {
                            const uploadedUrl = await uploadLessonDocument(Number(courseId), selectedChapterId, lesson.id, file);
                            setDocumentUrl(uploadedUrl);
                            addToast({ type: 'success', description: 'Tài liệu đã được upload thành công!' });
                          } catch (error: any) {
                            addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi upload tài liệu' });
                          } finally {
                            setUploadingDocument(false);
                            e.target.value = '';
                          }
                        } else {
                          // Creating new lesson: store file for later upload
                          setPendingDocumentFile(file);
                          setDocumentUrl(''); // Clear URL since we're using file
                          addToast({ type: 'info', description: `Đã chọn tài liệu: ${file.name}. Tài liệu sẽ được upload khi tạo bài học.` });
                          e.target.value = '';
                        }
                      }}
                      disabled={uploadingDocument}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => document.getElementById('document-upload')?.click()}
                      disabled={uploadingDocument}
                    >
                      {uploadingDocument ? (
                        <>Đang upload...</>
                      ) : (
                        <>
                          <Upload className="h-4 w-4 mr-2" />
                          Upload
                        </>
                      )}
                    </Button>
                    {pendingDocumentFile && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="ml-1"
                        onClick={() => setPendingDocumentFile(null)}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
                <p className="text-xs text-muted-foreground">
                  {pendingDocumentFile 
                    ? `File đã chọn: ${pendingDocumentFile.name} (${(pendingDocumentFile.size / 1024 / 1024).toFixed(2)} MB) - Sẽ upload khi tạo bài học`
                    : 'Bạn có thể upload file PDF/DOC hoặc nhập URL. File tối đa 50MB.'}
                </p>
              </div>
            )}
            {contentType === 'SLIDE' && (
              <div className="space-y-2">
                <Label>Slide bài giảng (PPT, PPTX, ODP, PDF) {!pendingSlideFile && !slideUrl && '*'}</Label>
                <div className="flex gap-2">
                  <Input
                    value={pendingSlideFile ? pendingSlideFile.name : slideUrl}
                    onChange={(e) => {
                      setSlideUrl(e.target.value);
                      setPendingSlideFile(null);
                    }}
                    placeholder="https://example.com/slide.pptx"
                    className="flex-1"
                    disabled={!!pendingSlideFile}
                    required={!pendingSlideFile && !slideUrl}
                  />
                  <div className="relative">
                    <input
                      type="file"
                      accept=".ppt,.pptx,.odp,.pdf"
                      className="hidden"
                      id="slide-upload"
                      onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file) return;
                        
                        if (lesson && selectedChapterId) {
                          // Editing existing lesson: upload immediately
                          setUploadingSlide(true);
                          try {
                            const uploadedUrl = await uploadLessonSlide(Number(courseId), selectedChapterId, lesson.id, file);
                            setSlideUrl(uploadedUrl);
                            addToast({ type: 'success', description: 'Slide đã được upload thành công!' });
                          } catch (error: any) {
                            addToast({ type: 'error', description: error.response?.data?.message || 'Lỗi khi upload slide' });
                          } finally {
                            setUploadingSlide(false);
                            e.target.value = '';
                          }
                        } else {
                          // Creating new lesson: store file for later upload
                          setPendingSlideFile(file);
                          setSlideUrl('');
                          addToast({ type: 'info', description: `Đã chọn slide: ${file.name}. Slide sẽ được upload khi tạo bài học.` });
                          e.target.value = '';
                        }
                      }}
                      disabled={uploadingSlide}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => document.getElementById('slide-upload')?.click()}
                      disabled={uploadingSlide}
                    >
                      {uploadingSlide ? (
                        <>Đang upload...</>
                      ) : (
                        <>
                          <Upload className="h-4 w-4 mr-2" />
                          Upload
                        </>
                      )}
                    </Button>
                    {pendingSlideFile && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="ml-1"
                        onClick={() => setPendingSlideFile(null)}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
                <p className="text-xs text-muted-foreground">
                  {pendingSlideFile 
                    ? `File đã chọn: ${pendingSlideFile.name} (${(pendingSlideFile.size / 1024 / 1024).toFixed(2)} MB) - Sẽ upload khi tạo bài học`
                    : 'Hỗ trợ: PDF, PPT, PPTX, ODP. File PPT/PPTX sẽ được tự động chuyển đổi sang PDF để xem trực tiếp trên trang. Tối đa 100MB.'}
                </p>
              </div>
            )}
            {contentType === 'TEXT' && (
              <div>
                <Label htmlFor="lesson-content">Nội dung bài đọc *</Label>
                <Textarea
                  id="lesson-content"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="Nhập nội dung bài đọc..."
                  className="mt-2 min-h-[200px]"
                  required
                />
              </div>
            )}
            <div>
              <Label htmlFor="lesson-duration">Thời lượng (phút) *</Label>
              <Input
                id="lesson-duration"
                type="number"
                min="1"
                value={durationInMinutes || ''}
                onChange={(e) => {
                  const value = e.target.value;
                  setDurationInMinutes(value === '' ? 0 : Number(value));
                  if (value !== '') {
                    setDurationTouched(true);
                  }
                }}
                onBlur={() => setDurationTouched(true)}
                className="mt-2"
                placeholder="Nhập thời lượng bài học"
                required
              />
              {durationTouched && durationInMinutes === 0 && (
                <p className="text-xs text-destructive mt-1">Vui lòng nhập thời lượng lớn hơn 0 phút</p>
              )}
            </div>
            <div>
              <Label htmlFor="lesson-position">Thứ tự *</Label>
              <Input
                id="lesson-position"
                type="number"
                min="1"
                value={position}
                onChange={(e) => setPosition(Number(e.target.value))}
                className="mt-2"
                required
              />
              <p className="text-xs text-muted-foreground mt-1">
                Thứ tự hiển thị của bài học trong chương
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Hủy
            </Button>
            <Button type="submit" disabled={isLoading || !selectedChapterId}>
              {isLoading ? 'Đang lưu...' : lesson ? 'Cập nhật' : 'Tạo bài học'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}


