# 🔧 Sửa lỗi Upload Video và Chọn Loại Nội Dung

## ❌ Vấn đề

1. **Không upload được video bài học**: Lỗi 400 Bad Request khi upload video
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart File Size Limit

**File:** `backend/src/main/resources/application.properties.example`

```properties
# File Upload
# Video files can be up to 500MB
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

**Lưu ý:** Cần cập nhật biến môi trường trên Render:
```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

### 2. Sửa Select Component (Loại nội dung)

**File:** `frontend/src/app/(dashboard)/instructor/courses/[id]/content/page.tsx`

- Thêm `id="lesson-type"` cho SelectTrigger
- Thêm `placeholder` cho SelectValue

### 3. Thêm Logging và Error Handling

**Backend:**
- Thêm logging chi tiết khi upload video
- Log file name, size, content type
- Log error details khi upload fail

**Frontend:**
- Thêm logging khi tạo lesson và upload video
- Validate lesson được tạo thành công trước khi upload
- Hiển thị error message chi tiết hơn

## 📝 Cần làm tiếp

### Bước 1: Cập nhật biến môi trường trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa các biến:**
```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

**Lưu ý:** 
- Nếu không có biến này, Spring Boot sẽ dùng default (1MB)
- Cần restart backend sau khi cập nhật

### Bước 2: Kiểm tra Storage Path

Đảm bảo các biến storage path đã được cấu hình:

```
LESSON_VIDEO_STORAGE_PATH=/app/uploads/lessons/videos
LESSON_VIDEO_BASE_URL=https://e-learning-backend-hchr.onrender.com/api/files/lessons/videos
```

### Bước 3: Test

1. **Test tạo bài học:**
   - Tạo bài học mới
   - Chọn loại nội dung (VIDEO, TEXT, DOCUMENT, SLIDE)
   - Kiểm tra xem dropdown có hoạt động không

2. **Test upload video:**
   - Chọn file video (dưới 500MB)
   - Upload và kiểm tra xem có thành công không
   - Xem logs trên Render để debug nếu có lỗi

## 🔍 Debug

### Nếu vẫn lỗi upload video:

1. **Kiểm tra logs trên Render:**
   - Tìm log: "Upload Video Request Received"
   - Kiểm tra file size, content type
   - Xem error message chi tiết

2. **Kiểm tra file size:**
   - File phải < 500MB
   - Kiểm tra biến môi trường `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE`

3. **Kiểm tra lesson ID:**
   - Lesson phải được tạo thành công trước khi upload
   - Kiểm tra console log: "Lesson created"

4. **Kiểm tra Content-Type:**
   - File phải là video (contentType starts with "video/")
   - Kiểm tra file extension (.mp4, .webm, etc.)

### Nếu dropdown không hoạt động:

1. **Kiểm tra console:**
   - Xem có lỗi JavaScript không
   - Kiểm tra state `contentType` có được set đúng không

2. **Kiểm tra Select component:**
   - Đảm bảo có `id="lesson-type"`
   - Đảm bảo `value` và `onValueChange` hoạt động

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` trên Render
- [ ] Đã kiểm tra `LESSON_VIDEO_STORAGE_PATH` và `LESSON_VIDEO_BASE_URL`
- [ ] Đã restart backend trên Render
- [ ] Đã test tạo bài học với các loại nội dung khác nhau
- [ ] Đã test upload video (file < 500MB)
- [ ] Đã kiểm tra logs nếu có lỗi

## ⚠️ Lưu ý

1. **File size limit:**
   - Video: Tối đa 500MB
   - Document: Tối đa 50MB
   - Avatar: Tối đa 10MB

2. **Multipart config:**
   - Phải được set trên Render environment variables
   - Không thể set trong code (phải qua biến môi trường)

3. **Storage path:**
   - Trên Render, phải dùng absolute path: `/app/uploads/...`
   - Không dùng relative path: `./uploads/...`
