# 🔧 Sửa lỗi Upload Video và Chọn Loại Nội Dung

## ❌ Lỗi hiện tại

1. **Không upload được video bài học**: Lỗi 400 Bad Request khi upload
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart Config

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

### 2. Thêm LESSON_VIDEO_BASE_URL

**File:** `backend/src/main/java/com/coursemgmt/service/FileStorageService.java`

Đã sửa để đọc từ biến môi trường:
```java
@Value("${LESSON_VIDEO_BASE_URL:${lesson.video.base-url:http://localhost:8080/api/files/lessons/videos}}")
```

### 3. Select Component

Select component đã có `id="lesson-type"` và `placeholder`.

## 📝 Cần làm tiếp

### Bước 1: Cập nhật biến môi trường trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**1. Thêm/Sửa multipart config:**
```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

**2. Thêm/Sửa LESSON_VIDEO_BASE_URL:**
```
LESSON_VIDEO_BASE_URL=https://e-learning-backend-hchr.onrender.com/api/files/lessons/videos
```

**Lưu ý:** Thay `e-learning-backend-hchr.onrender.com` bằng URL backend thực tế của bạn.

### Bước 2: Restart Backend

Sau khi cập nhật biến môi trường:
1. Click **Save Changes** trên Render
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

### Bước 3: Test lại

1. **Test chọn loại nội dung:**
   - Mở form thêm bài học
   - Click dropdown "Loại nội dung"
   - Chọn một loại (VIDEO, TEXT, DOCUMENT, SLIDE)
   - Kiểm tra xem có chọn được không

2. **Test upload video:**
   - Chọn loại "Video bài giảng"
   - Chọn file video (nhỏ hơn 500MB)
   - Click "Tạo bài học"
   - Kiểm tra xem video có được upload không

## 🔍 Debug

### Nếu vẫn lỗi upload:

1. **Kiểm tra logs trên Render:**
   - Vào Render Dashboard → Logs
   - Tìm lỗi liên quan đến multipart hoặc file upload
   - Kiểm tra xem có "MaxUploadSizeExceededException" không

2. **Kiểm tra file size:**
   - Đảm bảo file video < 500MB
   - Nếu file quá lớn, cần tăng config hoặc compress video

3. **Kiểm tra endpoint:**
   ```bash
   # Test endpoint (cần token)
   curl -X POST https://your-backend.onrender.com/api/v1/courses/15/chapters/1/lessons/1/upload-video \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -F "file=@video.mp4"
   ```

### Nếu vẫn không chọn được loại nội dung:

1. **Kiểm tra console:**
   - Mở DevTools → Console
   - Tìm lỗi JavaScript
   - Kiểm tra xem có lỗi về Select component không

2. **Kiểm tra state:**
   - Đảm bảo `contentType` state được set đúng
   - Kiểm tra `onValueChange` handler có được gọi không

## ⚠️ Lưu ý

1. **Multipart config:**
   - Phải >= 500MB để upload video lớn
   - Cả `max-file-size` và `max-request-size` đều phải >= 500MB

2. **File size:**
   - Video tối đa 500MB
   - Nếu cần upload video lớn hơn, cần tăng config hoặc dùng streaming

3. **Content-Type:**
   - Frontend đã xử lý đúng (xóa Content-Type header cho FormData)
   - Browser sẽ tự động set boundary

4. **Lesson creation:**
   - Lesson phải được tạo thành công trước khi upload video
   - Code đã handle việc này (tạo lesson → upload video → update lesson)

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã cập nhật `LESSON_VIDEO_BASE_URL` trên Render
- [ ] Đã restart backend trên Render
- [ ] Đã test chọn loại nội dung
- [ ] Đã test upload video
