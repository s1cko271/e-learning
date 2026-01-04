# 🔧 Sửa lỗi Upload Video và Dropdown Loại Nội Dung

## ❌ Vấn đề

1. **Không upload được video bài học**: Lỗi 400 Bad Request khi upload
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart Config (Backend)

**File:** `backend/src/main/resources/application.properties.example`

Đã tăng từ 10MB lên 500MB:
```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### 2. Cập nhật FileStorageService

**File:** `backend/src/main/java/com/coursemgmt/service/FileStorageService.java`

Đã thêm đọc `LESSON_VIDEO_BASE_URL` từ biến môi trường:
```java
@Value("${LESSON_VIDEO_BASE_URL:${lesson.video.base-url:http://localhost:8080/api/files/lessons/videos}}")
```

### 3. Sửa Select Component (Frontend)

**File:** `frontend/src/app/(dashboard)/instructor/courses/[id]/content/page.tsx`

Đã thêm `id="lesson-type"` cho SelectTrigger để fix console warning.

## 📝 Cần làm tiếp

### Bước 1: Cập nhật Multipart Config trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa biến:**
```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

**Hoặc nếu Render không hỗ trợ, thêm vào application.properties:**
```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### Bước 2: Cập nhật LESSON_VIDEO_BASE_URL trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa biến:**
```
LESSON_VIDEO_BASE_URL=https://e-learning-backend-hchr.onrender.com/api/files/lessons/videos
```

**Lưu ý:** Thay bằng URL backend thực tế của bạn trên Render.

### Bước 3: Restart Backend

Sau khi cập nhật biến môi trường:
1. Click **Save Changes** trên Render
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

### Bước 4: Kiểm tra Storage Path

Đảm bảo storage path đã được cấu hình:
```
LESSON_VIDEO_STORAGE_PATH=/app/uploads/lessons/videos
```

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã cập nhật `LESSON_VIDEO_BASE_URL` trên Render
- [ ] Đã kiểm tra `LESSON_VIDEO_STORAGE_PATH=/app/uploads/lessons/videos`
- [ ] Đã restart backend trên Render
- [ ] Đã test upload video (file < 500MB)
- [ ] Đã test chọn loại nội dung

## 🔍 Debug

### Kiểm tra lỗi upload video:

1. **Lỗi 400 Bad Request:**
   - Kiểm tra multipart config đã đúng chưa (500MB)
   - Kiểm tra file size có vượt quá 500MB không
   - Kiểm tra logs trên Render để xem lỗi chi tiết

2. **Lỗi Network Error:**
   - Kiểm tra backend có đang chạy không
   - Kiểm tra CORS config
   - Kiểm tra timeout (đã set 10 phút cho video upload)

3. **Dropdown không hoạt động:**
   - Kiểm tra console có lỗi JavaScript không
   - Kiểm tra Select component có được render đúng không
   - Thử refresh page

## ⚠️ Lưu ý

1. **File size:**
   - Video: tối đa 500MB
   - Document: tối đa 50MB
   - Image: tối đa 10MB

2. **Timeout:**
   - Frontend timeout: 10 phút (600000ms) cho video upload
   - Backend có thể cần timeout lớn hơn nếu file lớn

3. **Storage Path:**
   - Trên Render, storage path phải là absolute path: `/app/uploads/...`
   - Không dùng relative path như `./uploads/...`

## 🎉 Hoàn thành!

Sau khi cập nhật các biến môi trường trên Render và restart backend, upload video và chọn loại nội dung sẽ hoạt động bình thường.
