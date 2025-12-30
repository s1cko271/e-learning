'use client';

import React from 'react';
import Link from 'next/link';
import { Search, Filter, Grid3x3, List, BookOpen, PlayCircle, Clock, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ROUTES, STORAGE_KEYS } from '@/lib/constants';
import apiClient from '@/lib/api';
import type { Course } from '@/types';
import { useAuthStore } from '@/stores/authStore';

export default function MyCoursesPage() {
  const { isAuthenticated } = useAuthStore();
  const [viewMode, setViewMode] = React.useState<'grid' | 'list'>('grid');
  const [searchQuery, setSearchQuery] = React.useState('');
  const [sortBy, setSortBy] = React.useState('recent');
  const [courses, setCourses] = React.useState<Course[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  
  React.useEffect(() => {
    const fetchCourses = async () => {
      // Check if user is authenticated and has token
      if (typeof window === 'undefined') {
        setIsLoading(false);
        return;
      }
      
      const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
      if (!token || !isAuthenticated) {
        setIsLoading(false);
        setCourses([]);
        return;
      }
      
      try {
        const response = await apiClient.get<Course[]>('/v1/courses/my-courses');
        
        // Ensure we have an array
        if (Array.isArray(response.data)) {
          setCourses(response.data);
        } else {
          setCourses([]);
        }
      } catch (error: any) {
        // Silently handle 403 errors (user not authenticated)
        if (error.response?.status === 403 || error.response?.status === 401) {
          setCourses([]);
        } else {
          // Only log non-auth errors
          console.error('Error fetching courses:', error);
        }
        setCourses([]);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchCourses();
  }, [isAuthenticated]);
  
  // Filter courses by status using enrollmentStatus
  const inProgressCourses = Array.isArray(courses) 
    ? courses.filter(c => !c.enrollmentStatus || c.enrollmentStatus === 'IN_PROGRESS' || (c.enrollmentProgress ?? 0) < 100)
    : [];
  const completedCourses = Array.isArray(courses)
    ? courses.filter(c => c.enrollmentStatus === 'COMPLETED' || (c.enrollmentProgress ?? 0) >= 100)
    : [];
  
  // Filter and sort courses
  const filterAndSortCourses = (courseList: Course[]) => {
    let filtered = courseList;
    
    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(c => 
        c.title.toLowerCase().includes(query) ||
        (typeof c.instructor === 'string' ? c.instructor.toLowerCase().includes(query) : c.instructor?.fullName?.toLowerCase().includes(query))
      );
    }
    
    // Apply sort
    switch (sortBy) {
      case 'progress':
        filtered = [...filtered].sort((a, b) => (b.enrollmentProgress ?? 0) - (a.enrollmentProgress ?? 0));
        break;
      case 'title':
        filtered = [...filtered].sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'enrolled':
        filtered = [...filtered].sort((a, b) => {
          const dateA = new Date(a.lastAccessed || 0).getTime();
          const dateB = new Date(b.lastAccessed || 0).getTime();
          return dateB - dateA;
        });
        break;
      case 'recent':
      default:
        filtered = [...filtered].sort((a, b) => {
          const dateA = new Date(a.lastAccessed || 0).getTime();
          const dateB = new Date(b.lastAccessed || 0).getTime();
          return dateB - dateA;
        });
        break;
    }
    
    return filtered;
  };
  
  // Render course card component
  const renderCourseCard = (course: Course, viewMode: 'grid' | 'list') => {
    const instructorName = typeof course.instructor === 'string' 
      ? course.instructor 
      : course.instructor?.fullName || 'Giảng viên';
    const isCompleted = course.enrollmentStatus === 'COMPLETED' || (course.enrollmentProgress ?? 0) >= 100;
    
    if (viewMode === 'grid') {
      return (
        <Card className="overflow-hidden hover:shadow-lg transition-shadow">
          <div className="aspect-video bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center relative overflow-hidden">
            {course.imageUrl ? (
              <img 
                src={course.imageUrl} 
                alt={course.title}
                className="w-full h-full object-cover"
              />
            ) : (
              <BookOpen className="h-16 w-16 text-primary/50" />
            )}
          </div>
          <CardHeader>
            <div className="flex items-start justify-between gap-2 mb-2">
              <Badge variant={isCompleted ? 'default' : 'secondary'}>
                {isCompleted ? 'Hoàn thành' : 'Đang học'}
              </Badge>
              {course.category && (
                <Badge variant="outline">{course.category.name}</Badge>
              )}
            </div>
            <CardTitle className="line-clamp-2">{course.title}</CardTitle>
            <CardDescription>{instructorName}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Tiến độ</span>
                <span className="font-medium">{Math.round(course.enrollmentProgress ?? 0)}%</span>
              </div>
              <Progress value={course.enrollmentProgress ?? 0} />
              <p className="text-xs text-muted-foreground">
                {isCompleted ? 'Đã hoàn thành' : 'Bắt đầu học ngay'}
              </p>
            </div>
            
            <div className="flex items-center gap-2">
              <Button asChild className="flex-1">
                <Link href={ROUTES.LEARN(course.id.toString())}>
                  <PlayCircle className="h-4 w-4 mr-2" />
                  {isCompleted ? 'Ôn tập' : 'Tiếp tục học'}
                </Link>
              </Button>
              <Button variant="outline" asChild>
                <Link href={ROUTES.COURSE_DETAIL(course.id.toString())}>
                  Chi tiết
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      );
    } else {
      return (
        <Card className="hover:shadow-lg transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center gap-6">
              <div className="w-40 aspect-video bg-gradient-to-br from-primary/20 to-secondary/20 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden">
                {course.imageUrl ? (
                  <img 
                    src={course.imageUrl} 
                    alt={course.title}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <BookOpen className="h-12 w-12 text-primary/50" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-4 mb-2">
                  <div>
                    <h3 className="font-semibold text-lg mb-1">{course.title}</h3>
                    <p className="text-sm text-muted-foreground">{instructorName}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={isCompleted ? 'default' : 'secondary'}>
                      {isCompleted ? 'Hoàn thành' : 'Đang học'}
                    </Badge>
                  </div>
                </div>
                
                <div className="space-y-2 mb-4">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Tiến độ</span>
                    <span className="font-medium">{Math.round(course.enrollmentProgress ?? 0)}%</span>
                  </div>
                  <Progress value={course.enrollmentProgress ?? 0} className="h-2" />
                  <p className="text-xs text-muted-foreground">
                    {isCompleted ? 'Đã hoàn thành khóa học' : 'Đang học tập'}
                  </p>
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="text-xs text-muted-foreground">
                    {course.lastAccessed ? `Truy cập: ${new Date(course.lastAccessed).toLocaleDateString('vi-VN')}` : ''}
                  </div>
                  <div className="flex items-center gap-2">
                    <Button asChild>
                      <Link href={ROUTES.LEARN(course.id.toString())}>
                        <PlayCircle className="h-4 w-4 mr-2" />
                        {isCompleted ? 'Ôn tập' : 'Tiếp tục học'}
                      </Link>
                    </Button>
                    <Button variant="outline" asChild>
                      <Link href={ROUTES.COURSE_DETAIL(course.id.toString())}>
                        Chi tiết
                      </Link>
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      );
    }
  };
  
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <div className="border-b bg-card">
        <div className="container mx-auto px-4 py-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold font-poppins mb-2">
                Khóa học của tôi
              </h1>
              <p className="text-muted-foreground">
                Quản lý và theo dõi tiến độ học tập
              </p>
            </div>
            
            <Button asChild>
              <Link href={ROUTES.COURSES}>
                <BookOpen className="h-4 w-4 mr-2" />
                Khám phá thêm
              </Link>
            </Button>
          </div>
          
          {/* Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Tìm kiếm khóa học..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            
            <Select value={sortBy} onValueChange={setSortBy}>
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="recent">Gần đây nhất</SelectItem>
                <SelectItem value="progress">Tiến độ</SelectItem>
                <SelectItem value="title">Tên A-Z</SelectItem>
                <SelectItem value="enrolled">Ngày đăng ký</SelectItem>
              </SelectContent>
            </Select>
            
            <div className="flex items-center gap-2">
              <Button
                variant={viewMode === 'grid' ? 'default' : 'outline'}
                size="icon"
                onClick={() => setViewMode('grid')}
              >
                <Grid3x3 className="h-4 w-4" />
              </Button>
              <Button
                variant={viewMode === 'list' ? 'default' : 'outline'}
                size="icon"
                onClick={() => setViewMode('list')}
              >
                <List className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
      
      {/* Content */}
      <div className="container mx-auto px-4 py-8">
        <Tabs defaultValue="all" className="space-y-6">
          <TabsList>
            <TabsTrigger value="all">
              Tất cả ({courses.length})
            </TabsTrigger>
            <TabsTrigger value="in-progress">
              Đang học ({inProgressCourses.length})
            </TabsTrigger>
            <TabsTrigger value="completed">
              Hoàn thành ({completedCourses.length})
            </TabsTrigger>
          </TabsList>
          
          <TabsContent value="all">
            {isLoading ? (
              <div className="text-center py-12 text-muted-foreground">Đang tải...</div>
            ) : !Array.isArray(courses) || courses.length === 0 ? (
              <div className="text-center py-12">
                <BookOpen className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                <h3 className="text-lg font-semibold mb-2">Bạn chưa mua khóa học nào</h3>
                <p className="text-muted-foreground mb-6">
                  Bắt đầu học một khóa học mới ngay hôm nay!
                </p>
                <Button asChild>
                  <Link href={ROUTES.COURSES}>Khám phá khóa học</Link>
                </Button>
              </div>
            ) : (() => {
              const filteredCourses = filterAndSortCourses(courses);
              return filteredCourses.length === 0 ? (
                <div className="text-center py-12">
                  <BookOpen className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                  <h3 className="text-lg font-semibold mb-2">Không tìm thấy khóa học</h3>
                  <p className="text-muted-foreground">
                    Thử tìm kiếm với từ khóa khác
                  </p>
                </div>
              ) : viewMode === 'grid' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredCourses.map((course) => (
                    <React.Fragment key={course.id}>
                      {renderCourseCard(course, 'grid')}
                    </React.Fragment>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {filteredCourses.map((course) => (
                    <React.Fragment key={course.id}>
                      {renderCourseCard(course, 'list')}
                    </React.Fragment>
                  ))}
                </div>
              );
            })()}
          </TabsContent>
          
          <TabsContent value="in-progress">
            {isLoading ? (
              <div className="text-center py-12 text-muted-foreground">Đang tải...</div>
            ) : inProgressCourses.length === 0 ? (
              <div className="text-center py-12">
                <BookOpen className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                <h3 className="text-lg font-semibold mb-2">Chưa có khóa học đang học</h3>
                <p className="text-muted-foreground mb-6">
                  Bắt đầu học một khóa học mới ngay hôm nay!
                </p>
                <Button asChild>
                  <Link href={ROUTES.COURSES}>Khám phá khóa học</Link>
                </Button>
              </div>
            ) : (() => {
              const filteredCourses = filterAndSortCourses(inProgressCourses);
              return filteredCourses.length === 0 ? (
                <div className="text-center py-12">
                  <BookOpen className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                  <h3 className="text-lg font-semibold mb-2">Không tìm thấy khóa học</h3>
                  <p className="text-muted-foreground">
                    Thử tìm kiếm với từ khóa khác
                  </p>
                </div>
              ) : viewMode === 'grid' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredCourses.map((course) => (
                    <React.Fragment key={course.id}>
                      {renderCourseCard(course, 'grid')}
                    </React.Fragment>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {filteredCourses.map((course) => (
                    <React.Fragment key={course.id}>
                      {renderCourseCard(course, 'list')}
                    </React.Fragment>
                  ))}
                </div>
              );
            })()}
          </TabsContent>
          
          <TabsContent value="completed">
            {isLoading ? (
              <div className="text-center py-12 text-muted-foreground">Đang tải...</div>
            ) : completedCourses.length === 0 ? (
              <div className="text-center py-12">
                <CheckCircle2 className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                <h3 className="text-lg font-semibold mb-2">Chưa hoàn thành khóa học nào</h3>
                <p className="text-muted-foreground">
                  Tiếp tục học tập để hoàn thành khóa học đầu tiên!
                </p>
              </div>
            ) : (() => {
              const filteredCourses = filterAndSortCourses(completedCourses);
              const instructorName = (course: Course) => typeof course.instructor === 'string' 
                ? course.instructor 
                : course.instructor?.fullName || 'Giảng viên';
              
              return filteredCourses.length === 0 ? (
                <div className="text-center py-12">
                  <CheckCircle2 className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                  <h3 className="text-lg font-semibold mb-2">Không tìm thấy khóa học</h3>
                  <p className="text-muted-foreground">
                    Thử tìm kiếm với từ khóa khác
                  </p>
                </div>
              ) : viewMode === 'grid' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredCourses.map((course) => (
                    <Card key={course.id} className="overflow-hidden hover:shadow-lg transition-shadow">
                      <div className="aspect-video bg-gradient-to-br from-accent/20 to-primary/20 flex items-center justify-center relative overflow-hidden">
                        {course.imageUrl ? (
                          <img 
                            src={course.imageUrl} 
                            alt={course.title}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <CheckCircle2 className="h-16 w-16 text-accent" />
                        )}
                        <Badge className="absolute top-4 right-4 bg-accent">
                          Hoàn thành
                        </Badge>
                      </div>
                      <CardHeader>
                        <CardTitle className="line-clamp-2">{course.title}</CardTitle>
                        <CardDescription>{instructorName(course)}</CardDescription>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-muted-foreground">100% hoàn thành</span>
                          <Badge variant="outline">Chứng chỉ</Badge>
                        </div>
                        
                        <div className="flex items-center gap-2">
                          <Button asChild className="flex-1" variant="outline">
                            <Link href={ROUTES.LEARN(course.id.toString())}>
                              Ôn tập
                            </Link>
                          </Button>
                          <Button asChild className="flex-1">
                            <Link href={`/student/certificates/${course.id}`}>
                              Xem chứng chỉ
                            </Link>
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {filteredCourses.map((course) => (
                    <Card key={course.id} className="hover:shadow-lg transition-shadow">
                      <CardContent className="p-6">
                        <div className="flex items-center gap-6">
                          <div className="w-40 aspect-video bg-gradient-to-br from-accent/20 to-primary/20 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden relative">
                            {course.imageUrl ? (
                              <img 
                                src={course.imageUrl} 
                                alt={course.title}
                                className="w-full h-full object-cover"
                              />
                            ) : (
                              <CheckCircle2 className="h-12 w-12 text-accent" />
                            )}
                            <Badge className="absolute top-2 right-2 bg-accent text-xs">
                              Hoàn thành
                            </Badge>
                          </div>
                          
                          <div className="flex-1 min-w-0">
                            <div className="flex items-start justify-between gap-4 mb-2">
                              <div>
                                <h3 className="font-semibold text-lg mb-1">{course.title}</h3>
                                <p className="text-sm text-muted-foreground">{instructorName(course)}</p>
                              </div>
                              <Badge variant="default">Hoàn thành</Badge>
                            </div>
                            
                            <div className="space-y-2 mb-4">
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Tiến độ</span>
                                <span className="font-medium">100%</span>
                              </div>
                              <Progress value={100} className="h-2" />
                              <p className="text-xs text-muted-foreground">Đã hoàn thành khóa học</p>
                            </div>
                            
                            <div className="flex items-center justify-between">
                              <Badge variant="outline">Chứng chỉ</Badge>
                              <div className="flex items-center gap-2">
                                <Button asChild variant="outline">
                                  <Link href={ROUTES.LEARN(course.id.toString())}>
                                    Ôn tập
                                  </Link>
                                </Button>
                                <Button asChild>
                                  <Link href={`/student/certificates/${course.id}`}>
                                    Xem chứng chỉ
                                  </Link>
                                </Button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              );
            })()}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

