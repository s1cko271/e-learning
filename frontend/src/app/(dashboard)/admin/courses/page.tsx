'use client';

import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminService, type AdminCourse, type AdminUser } from '@/services/adminService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search, Loader2, ExternalLink } from 'lucide-react';
import { useUIStore } from '@/stores/uiStore';
import { formatDate, formatCurrency } from '@/lib/utils';
import { ROUTES } from '@/lib/constants';
import Link from 'next/link';
import Image from 'next/image';

export default function AdminCoursesPage() {
  const queryClient = useQueryClient();
  const { addToast } = useUIStore();
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const [statusFilter, setStatusFilter] = React.useState<string>('all');
  const [debouncedSearch, setDebouncedSearch] = React.useState('');

  // Debounce search
  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 500);
    return () => clearTimeout(timer);
  }, [search]);

  // Fetch courses
  const { data: coursesData, isLoading } = useQuery({
    queryKey: ['admin-courses', page, debouncedSearch, statusFilter],
    queryFn: () => adminService.getCourses({
      page,
      size: 10,
      sortBy: 'createdAt',
      sortDir: 'DESC',
      search: debouncedSearch || undefined,
      status: statusFilter !== 'all' ? statusFilter : undefined,
    }),
  });


  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }> = {
      DRAFT: { label: 'Bản nháp', variant: 'secondary' },
      PENDING_APPROVAL: { label: 'Chờ duyệt', variant: 'outline' },
      PUBLISHED: { label: 'Đã xuất bản', variant: 'default' },
      ARCHIVED: { label: 'Đã lưu trữ', variant: 'secondary' },
    };
    const statusInfo = statusMap[status] || { label: status, variant: 'secondary' };
    return <Badge variant={statusInfo.variant}>{statusInfo.label}</Badge>;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Quản lý Khóa học</h1>
        <p className="text-muted-foreground">Xem và quản lý tất cả khóa học trên hệ thống</p>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm kiếm khóa học..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="DRAFT">Bản nháp</SelectItem>
            <SelectItem value="PENDING_APPROVAL">Chờ duyệt</SelectItem>
            <SelectItem value="PUBLISHED">Đã xuất bản</SelectItem>
            <SelectItem value="ARCHIVED">Đã lưu trữ</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Table */}
      <div className="border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Khóa học</TableHead>
              <TableHead>Giảng viên</TableHead>
              <TableHead>Giá</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                </TableCell>
              </TableRow>
            ) : coursesData?.content.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                  Không tìm thấy khóa học nào
                </TableCell>
              </TableRow>
            ) : (
              coursesData?.content.map((course) => (
                <TableRow key={course.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      {course.imageUrl && (
                        <div className="relative w-16 h-16 rounded-lg overflow-hidden">
                          <Image
                            src={course.imageUrl}
                            alt={course.title}
                            fill
                            className="object-cover"
                          />
                        </div>
                      )}
                      <div>
                        <p className="font-medium">{course.title}</p>
                        <p className="text-sm text-muted-foreground line-clamp-1">
                          {course.description || 'Không có mô tả'}
                        </p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{course.instructor?.fullName || course.instructorName || 'N/A'}</TableCell>
                  <TableCell>{formatCurrency(course.price)}</TableCell>
                  <TableCell>{getStatusBadge(course.status)}</TableCell>
                  <TableCell>{formatDate(course.createdAt)}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        asChild
                      >
                        <Link href={ROUTES.COURSE_DETAIL(course.id.toString())} target="_blank">
                          <ExternalLink className="h-4 w-4 mr-2" />
                          Xem
                        </Link>
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {/* Pagination */}
        {coursesData && coursesData.totalPages > 1 && (
          <div className="flex items-center justify-between p-4 border-t">
            <p className="text-sm text-muted-foreground">
              Trang {page + 1} / {coursesData.totalPages} ({coursesData.totalElements} khóa học)
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Trước
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.min(coursesData.totalPages - 1, p + 1))}
                disabled={page >= coursesData.totalPages - 1}
              >
                Sau
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

