'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import { 
  Clock, 
  Users, 
  Star, 
  BookOpen, 
  Award, 
  Globe, 
  BarChart3, 
  CheckCircle2, 
  PlayCircle,
  FileText,
  Download,
  Lock,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion';
import { Navbar } from '@/components/layout/Navbar';
import { Footer } from '@/components/layout/Footer';
import { useCourse } from '@/hooks/useCourses';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES, COURSE_LEVEL_LABELS } from '@/lib/constants';
import { Skeleton } from '@/components/ui/skeleton';
import { ReviewSection } from '@/components/course/ReviewSection';
import { getCourseContent, type ChapterResponse, type LessonResponse } from '@/services/contentService';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/lib/api';
import { useCartStore } from '@/stores/cartStore';
import { useUIStore } from '@/stores/uiStore';
import { enrollCourse } from '@/services/enrollmentService';

export default function CourseDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { course, isLoading } = useCourse(params.id as string);
  const { isAuthenticated, user } = useAuthStore();
  const { addToCart, isLoading: isAddingToCart } = useCartStore();
  const { addToast } = useUIStore();
  const queryClient = useQueryClient();
  const [isEnrolling, setIsEnrolling] = React.useState(false);
  
  // Fetch curriculum data (chapters and lessons) - Only for enrolled users
  const { data: curriculum, isLoading: isLoadingCurriculum } = useQuery<ChapterResponse[]>({
    queryKey: ['course-content', params.id, course?.isEnrolled],
    queryFn: async () => {
      // Only fetch content if user is authenticated AND enrolled
        if (isAuthenticated && course?.isEnrolled === true) {
            return await getCourseContent(Number(params.id));
      }
      return [];
    },
    enabled: !!params.id && !!course && isAuthenticated && course?.isEnrolled === true,
  });
  
  if (isLoading) {
    return (
      <div className="min-h-screen flex flex-col">
        <Navbar />
        <div className="container mx-auto px-4 py-8">
          <Skeleton className="h-64 w-full mb-8" />
          <div className="grid lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
              <Skeleton className="h-8 w-3/4" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
            </div>
            <Skeleton className="h-96" />
          </div>
        </div>
        <Footer />
      </div>
    );
  }
  
  if (!course) {
    return (
      <div className="min-h-screen flex flex-col">
        <Navbar />
        <div className="container mx-auto px-4 py-16 text-center">
          <h1 className="text-2xl font-bold mb-4">Không tìm thấy khóa học</h1>
          <Button onClick={() => router.push(ROUTES.COURSES)}>
            Quay lại danh sách khóa học
          </Button>
        </div>
        <Footer />
      </div>
    );
  }
  
  const handleEnroll = async () => {
    if (!isAuthenticated) {
      router.push(ROUTES.LOGIN);
      return;
    }
    
    // If already enrolled, redirect to learn page
    if (course.isEnrolled) {
      router.push(ROUTES.LEARN(course.id.toString()));
      return;
    }

    // Check if course is free (price === 0)
    if (course.price === 0 || course.price === null) {
      // Free course - enroll directly without payment
      try {
        setIsEnrolling(true);
        if (!user?.id) {
          addToast({
            type: 'error',
            description: 'Không thể xác định người dùng',
          });
          return;
        }
        
        await enrollCourse(course.id, user.id);
        
        // Invalidate queries to refresh course data
        queryClient.invalidateQueries({ queryKey: ['course', params.id] });
        queryClient.invalidateQueries({ queryKey: ['my-courses'] });
        queryClient.invalidateQueries({ queryKey: ['featured-courses'] });
        queryClient.invalidateQueries({ queryKey: ['courses'] });
        
        addToast({
          type: 'success',
          description: 'Đã đăng ký khóa học thành công!',
        });
        
        // Redirect to learn page
        router.push(ROUTES.LEARN(course.id.toString()));
      } catch (error: any) {
        console.error('Error enrolling in course:', error);
        addToast({
          type: 'error',
          description: error.response?.data?.message || 'Không thể đăng ký khóa học',
        });
      } finally {
        setIsEnrolling(false);
      }
    } else {
      // Paid course - add to cart
      await addToCart(course.id);
    }
  };
  
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      
      <div className="flex-1">
        {/* Hero Section */}
        <div className="bg-gradient-to-br from-primary/10 via-secondary/10 to-accent/10 border-b">
          <div className="container mx-auto px-4 py-12">
            <div className="grid lg:grid-cols-3 gap-8">
              {/* Course Info */}
              <div className="lg:col-span-2 space-y-6">
                {/* Breadcrumb */}
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <a href={ROUTES.COURSES} className="hover:text-primary">Khóa học</a>
                  <span>/</span>
                  {course.category && (
                    <>
                      <span>{course.category.name}</span>
                      <span>/</span>
                    </>
                  )}
                  <span className="text-foreground">{course.title}</span>
                </div>
                
                {/* Title & Badges */}
                <div>
                  <div className="flex flex-wrap items-center gap-2 mb-3">
                    {course.isFeatured && (
                      <Badge className="bg-accent">Nổi bật</Badge>
                    )}
                    <Badge variant="secondary">
                      {COURSE_LEVEL_LABELS[course.level]}
                    </Badge>
                  </div>
                  <h1 className="text-3xl md:text-4xl font-bold font-poppins mb-3">
                    {course.title}
                  </h1>
                  <p className="text-lg text-muted-foreground">
                    {course.shortDescription || course.description}
                  </p>
                </div>
                
                {/* Meta Info */}
                <div className="flex flex-wrap items-center gap-6">
                  {/* Rating */}
                  {course.rating && (
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-1">
                        <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                        <span className="font-bold">{course.rating.toFixed(1)}</span>
                      </div>
                      <span className="text-sm text-muted-foreground">
                        ({course.reviewCount?.toLocaleString()} đánh giá)
                      </span>
                    </div>
                  )}
                  
                  {/* Students */}
                  <div className="flex items-center gap-2">
                    <Users className="h-5 w-5 text-muted-foreground" />
                    <span>{(course.enrollmentCount ?? 0).toLocaleString()} học viên</span>
                  </div>
                  
                  {/* Duration */}
                  {course.duration && (
                    <div className="flex items-center gap-2">
                      <Clock className="h-5 w-5 text-muted-foreground" />
                      <span>{course.duration}</span>
                    </div>
                  )}
                  
                  {/* Language */}
                  <div className="flex items-center gap-2">
                    <Globe className="h-5 w-5 text-muted-foreground" />
                    <span>{course.language}</span>
                  </div>
                </div>
                
                {/* Instructor */}
                <div className="flex items-center gap-3">
                  <Avatar className="h-12 w-12">
                    <AvatarImage src={course.instructor.avatar} />
                    <AvatarFallback>
                      {course.instructor.fullName.charAt(0)}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm text-muted-foreground">Giảng viên</p>
                    <p className="font-semibold">{course.instructor.fullName}</p>
                  </div>
                </div>
              </div>
              
              {/* Hero Image & Enrollment Card - Desktop */}
              <div className="hidden lg:block space-y-4">
                {/* Hero Image (Static - No Video Interaction) */}
                <div className="relative aspect-video w-full rounded-lg overflow-hidden bg-muted">
                  {course.imageUrl || course.thumbnail ? (
                    <Image
                      src={course.imageUrl || course.thumbnail || ''}
                      alt={course.title}
                      fill
                      className="object-cover"
                      sizes="(max-width: 768px) 100vw, 33vw"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center bg-gradient-to-br from-primary/20 to-secondary/20">
                      <BookOpen className="h-24 w-24 text-muted-foreground" />
                    </div>
                  )}
                </div>
                
                {/* Enrollment Card */}
                <Card className="sticky top-4">
                  <CardContent className="p-6 space-y-4">
                    {/* Price - Only show if not enrolled */}
                    {!course.isEnrolled && (
                      <div className="flex items-baseline gap-3">
                        {course.discountPrice ? (
                          <>
                            <span className="text-3xl font-bold text-primary">
                              {course.discountPrice.toLocaleString('vi-VN')}đ
                            </span>
                            <span className="text-lg text-muted-foreground line-through">
                              {course.price.toLocaleString('vi-VN')}đ
                            </span>
                          </>
                        ) : course.price > 0 ? (
                          <span className="text-3xl font-bold text-primary">
                            {course.price.toLocaleString('vi-VN')}đ
                          </span>
                        ) : (
                          <span className="text-3xl font-bold text-accent">
                            Miễn phí
                          </span>
                        )}
                      </div>
                    )}
                    
                    {/* CTA Buttons */}
                    <div className="space-y-2">
                      {course.isEnrolled ? (
                        // User is enrolled - Show "Tiếp tục học" or "Ôn tập lại" button
                        <Button 
                          className="w-full bg-green-600 hover:bg-green-700 text-white" 
                          size="lg"
                          onClick={() => router.push(ROUTES.LEARN(course.id.toString()))}
                        >
                          <PlayCircle className="h-4 w-4 mr-2" />
                          {course.enrollmentStatus === 'COMPLETED' || (course.enrollmentProgress ?? 0) >= 100
                            ? 'Ôn tập lại'
                            : 'Tiếp tục học'}
                        </Button>
                      ) : (
                        // User not enrolled - Show appropriate button based on price
                        <Button 
                          className="w-full" 
                          size="lg"
                          onClick={handleEnroll}
                          disabled={isAddingToCart || isEnrolling}
                        >
                          {isEnrolling 
                            ? 'Đang đăng ký...' 
                            : isAddingToCart
                            ? 'Đang thêm...'
                            : (course.price === 0 || course.price === null)
                            ? 'Vào học ngay'
                            : 'Thêm vào giỏ hàng'}
                        </Button>
                      )}
                    </div>
                    
                    <Separator />
                    
                    {/* Course Includes */}
                    <div className="space-y-3">
                      <h3 className="font-semibold">Khóa học bao gồm:</h3>
                      <ul className="space-y-2 text-sm">
                        <li className="flex items-center gap-2">
                          <PlayCircle className="h-4 w-4 text-primary" />
                          <span>Truy cập trọn đời</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <FileText className="h-4 w-4 text-primary" />
                          <span>Tài liệu học tập</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <Download className="h-4 w-4 text-primary" />
                          <span>Tài nguyên có thể tải</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <Award className="h-4 w-4 text-primary" />
                          <span>Chứng chỉ hoàn thành</span>
                        </li>
                      </ul>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>
        </div>
        
        {/* Main Content */}
        <div className="container mx-auto px-4 py-12">
          <div className="grid lg:grid-cols-3 gap-8">
            {/* Content */}
            <div className="lg:col-span-2">
              <Tabs defaultValue="overview" className="w-full">
                <TabsList className="grid w-full grid-cols-4">
                  <TabsTrigger value="overview">Tổng quan</TabsTrigger>
                  <TabsTrigger value="curriculum">Nội dung</TabsTrigger>
                  <TabsTrigger value="instructor">Giảng viên</TabsTrigger>
                  <TabsTrigger value="reviews">Đánh giá</TabsTrigger>
                </TabsList>
                
                {/* Overview Tab */}
                <TabsContent value="overview" className="space-y-6 mt-6">
                  {/* What you'll learn */}
                  {course.whatYouLearn && course.whatYouLearn.length > 0 && (
                    <Card>
                      <CardHeader>
                        <CardTitle>Bạn sẽ học được gì</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ul className="grid md:grid-cols-2 gap-3">
                          {course.whatYouLearn.map((item, index) => (
                            <li key={index} className="flex items-start gap-2">
                              <CheckCircle2 className="h-5 w-5 text-accent shrink-0 mt-0.5" />
                              <span>{item}</span>
                            </li>
                          ))}
                        </ul>
                      </CardContent>
                    </Card>
                  )}
                  
                  {/* Requirements */}
                  {course.requirements && course.requirements.length > 0 && (
                    <Card>
                      <CardHeader>
                        <CardTitle>Yêu cầu</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ul className="space-y-2">
                          {course.requirements.map((req, index) => (
                            <li key={index} className="flex items-start gap-2">
                              <span className="text-muted-foreground">•</span>
                              <span>{req}</span>
                            </li>
                          ))}
                        </ul>
                      </CardContent>
                    </Card>
                  )}
                  
                  {/* Description */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Mô tả khóa học</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div 
                        className="prose max-w-none"
                        dangerouslySetInnerHTML={{ __html: course.description }}
                      />
                    </CardContent>
                  </Card>
                </TabsContent>
                
                {/* Curriculum Tab */}
                <TabsContent value="curriculum" className="mt-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Nội dung khóa học</CardTitle>
                      {curriculum && curriculum.length > 0 ? (
                        <p className="text-sm text-muted-foreground">
                          {curriculum.length} chương • {curriculum.reduce((total, ch) => total + (ch.lessons?.length || 0), 0)} bài học
                        </p>
                      ) : (
                        <p className="text-sm text-muted-foreground">
                          Đang tải nội dung...
                        </p>
                      )}
                    </CardHeader>
                    <CardContent>
                      {isLoadingCurriculum ? (
                        <div className="space-y-4">
                          <Skeleton className="h-12 w-full" />
                          <Skeleton className="h-12 w-full" />
                          <Skeleton className="h-12 w-full" />
                        </div>
                      ) : curriculum && curriculum.length > 0 ? (
                        <Accordion type="single" collapsible className="w-full">
                          {curriculum.map((chapter) => (
                            <AccordionItem key={chapter.id} value={`chapter-${chapter.id}`}>
                              <AccordionTrigger className="hover:no-underline">
                                <div className="flex items-center justify-between w-full pr-4">
                                  <span className="font-semibold">
                                    {chapter.title}
                                  </span>
                                  <span className="text-sm text-muted-foreground">
                                    {chapter.lessons?.length || 0} bài học
                                  </span>
                                </div>
                              </AccordionTrigger>
                              <AccordionContent>
                                <ul className="space-y-2">
                                  {chapter.lessons?.map((lesson) => (
                                    <li 
                                      key={lesson.id}
                                      className="flex items-center justify-between p-3 hover:bg-muted rounded-lg transition-colors"
                                    >
                                      <div className="flex items-center gap-3 flex-1">
                                        <PlayCircle className="h-5 w-5 text-muted-foreground shrink-0" />
                                        <span className="flex-1">{lesson.title}</span>
                                      </div>
                                      <div className="flex items-center gap-2">
                                        {lesson.durationInMinutes && (
                                          <span className="text-sm text-muted-foreground">
                                            {Math.floor(lesson.durationInMinutes / 60)}:{(lesson.durationInMinutes % 60).toString().padStart(2, '0')}
                                          </span>
                                        )}
                                        {/* Show lock icon for non-enrolled users */}
                                        {!isAuthenticated ? (
                                          <div className="flex items-center gap-1 ml-2">
                                            <Lock className="h-4 w-4 text-muted-foreground" />
                                            <Button
                                              variant="ghost"
                                              size="sm"
                                              onClick={() => router.push(ROUTES.LOGIN)}
                                              className="h-auto p-1 text-xs text-muted-foreground hover:text-foreground"
                                            >
                                              Đăng nhập để xem
                                            </Button>
                                          </div>
                                        ) : (
                                          <Lock className="h-4 w-4 text-muted-foreground ml-2" />
                                        )}
                                      </div>
                                    </li>
                                  ))}
                                </ul>
                              </AccordionContent>
                            </AccordionItem>
                          ))}
                        </Accordion>
                      ) : (
                        <div className="text-center py-8">
                          <p className="text-muted-foreground">
                            Chưa có nội dung khóa học
                          </p>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </TabsContent>
                
                {/* Instructor Tab */}
                <TabsContent value="instructor" className="mt-6">
                  <Card>
                    <CardContent className="p-6">
                      <div className="flex items-start gap-4 mb-6">
                        <Avatar className="h-20 w-20">
                          <AvatarImage src={course.instructor.avatar} />
                          <AvatarFallback className="text-2xl">
                            {course.instructor.fullName.charAt(0)}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex-1">
                          <h3 className="text-2xl font-bold mb-1">
                            {course.instructor.fullName}
                          </h3>
                          <p className="text-muted-foreground mb-3">
                            {course.instructor.bio || 'Giảng viên chuyên nghiệp'}
                          </p>
                          <div className="flex flex-wrap gap-4 text-sm">
                            <div className="flex items-center gap-1">
                              <BookOpen className="h-4 w-4" />
                              <span>{course.instructor.totalCourses || 10} khóa học</span>
                            </div>
                            <div className="flex items-center gap-1">
                              <Users className="h-4 w-4" />
                              <span>{course.instructor.totalStudents?.toLocaleString() || '50,000'} học viên</span>
                            </div>
                            <div className="flex items-center gap-1">
                              <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                              <span>{course.instructor.averageRating || 4.8} đánh giá</span>
                            </div>
                          </div>
                        </div>
                      </div>
                      
                      <Separator className="my-6" />
                      
                      <div className="prose max-w-none">
                        <p>
                          Thông tin chi tiết về giảng viên sẽ được hiển thị ở đây...
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
                
                {/* Reviews Tab */}
                <TabsContent value="reviews" className="mt-6">
                  <ReviewSection courseId={course.id} />
                </TabsContent>
              </Tabs>
            </div>
            
            {/* Sidebar - Mobile sticky enrollment card */}
            <div className="lg:hidden fixed bottom-0 left-0 right-0 p-4 bg-background border-t shadow-lg z-10">
              <div className="flex items-center justify-between">
                {course.isEnrolled ? (
                  // User is enrolled - Show "Tiếp tục học" or "Ôn tập lại" button
                  <Button 
                    size="lg" 
                    className="w-full bg-green-600 hover:bg-green-700 text-white"
                    onClick={() => router.push(ROUTES.LEARN(course.id.toString()))}
                  >
                    <PlayCircle className="h-4 w-4 mr-2" />
                    {course.enrollmentStatus === 'COMPLETED' || (course.enrollmentProgress ?? 0) >= 100
                      ? 'Ôn tập lại'
                      : 'Tiếp tục học'}
                  </Button>
                ) : (
                  <>
                    <div>
                      {course.discountPrice ? (
                        <>
                          <div className="text-2xl font-bold text-primary">
                            {course.discountPrice.toLocaleString('vi-VN')}đ
                          </div>
                          <div className="text-sm text-muted-foreground line-through">
                            {course.price.toLocaleString('vi-VN')}đ
                          </div>
                        </>
                      ) : (
                        <div className="text-2xl font-bold text-primary">
                          {course.price > 0 ? `${course.price.toLocaleString('vi-VN')}đ` : 'Miễn phí'}
                        </div>
                      )}
                    </div>
                    <Button 
                      size="lg" 
                      onClick={handleEnroll}
                      disabled={isAddingToCart || isEnrolling}
                    >
                      {isEnrolling 
                        ? 'Đang đăng ký...' 
                        : isAddingToCart
                        ? 'Đang thêm...'
                        : (course.price === 0 || course.price === null)
                        ? 'Vào học ngay'
                        : 'Thêm vào giỏ hàng'}
                    </Button>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <Footer />
      
    </div>
  );
}

