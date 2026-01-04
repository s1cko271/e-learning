# 🔧 Sửa lỗi Upload Video Bài Học

## ❌ Vấn đề

1. **Không upload được video bài học** - Lỗi 400 Bad Request hoặc Network Error
2. **Không chọn được loại nội dung** - Select dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart File Size Limit

**File:** `backend/src/main/resources/application.properties.example`

```properties
# File Upload
# Video files can be up to 500MB
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### 2. Thêm LESSON_VIDEO_BASE_URL Support

**File:** `backend/src/main/java/com/coursemgmt/service/FileStorageService.java`

Đã thêm hỗ trợ biến môi trường `LESSON_VIDEO_BASE_URL`:
```java
@Value("${LESSON_VIDEO_BASE_URL:${lesson.video.base-url:http://localhost:8080/api/files/lessons/videos}}")
```

### 3. Thêm Lesson Video Config vào Example

**File:** `backend/src/main/resources/application.properties.example`

```properties
# Lesson Video Storage
lesson.video.storage.path=./uploads/lessons/videos
lesson.video.base-url=http://localhost:8080/api/files/lessons/videos
```

### 4. Sửa Security Check

**File:** `backend/src/main/java/com/coursemgmt/controller/ChapterController.java`

Đổi từ `isInstructorOfLesson` sang `isInstructorOfChapter` để tránh lỗi khi lesson vừa tạo:
```java
@PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
```

### 5. Thêm ID cho Select Component

**File:** `frontend/src/app/(dashboard)/instructor/courses/[id]/content/page.tsx`

Đã thêm `id="lesson-type"` cho Select component để fix lỗi form field.

## 📝 Cần làm trên Render

### Bước 1: Cập nhật Environment Variables

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa các biến sau:**

```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
LESSON_VIDEO_BASE_URL=https://e-learning-backend-hchr.onrender.com/api/files/lessons/videos
LESSON_VIDEO_STORAGE_PATH=/app/uploads/lessons/videos
```

**Lưu ý:** Thay `e-learning-backend-hchr.onrender.com` bằng URL backend thực tế của bạn.

### Bước 2: Restart Backend

Sau khi cập nhật biến môi trường:
1. Click **Save Changes**
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

### Bước 3: Test

1. **Test Upload Video:**
   - Vào trang quản lý khóa học
   - Tạo bài học mới với loại "Video bài giảng"
   - Upload video file (tối đa 500MB)
   - Kiểm tra xem upload có thành công không

2. **Test Select Content Type:**
   - Mở form tạo bài học
   - Click vào dropdown "Loại nội dung"
   - Kiểm tra xem có thể chọn được không

## 🔍 Debug nếu vẫn lỗi

### Kiểm tra Logs trên Render

1. Vào Render Dashboard → Logs
2. Tìm các log liên quan đến upload:
   ```
   Upload Lesson Video Request
   File size: ...
   Content type: ...
   ```

### Kiểm tra File Size

- Video file phải ≤ 500MB
- Nếu lớn hơn, cần tăng limit hoặc compress video

### Kiểm tra Network

Mở DevTools → Network tab:
- Kiểm tra request `/api/v1/courses/{id}/chapters/{id}/lessons/{id}/upload-video`
- Xem status code và response

### Kiểm tra CORS

Nếu có lỗi CORS:
- Đảm bảo `ALLOWED_ORIGINS` trên Render đã bao gồm URL Vercel

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã cập nhật `LESSON_VIDEO_BASE_URL` trên Render
- [ ] Đã cập nhật `LESSON_VIDEO_STORAGE_PATH` trên Render
- [ ] Đã restart backend trên Render
- [ ] Đã test upload video thành công
- [ ] Đã test chọn loại nội dung thành công

## 🎯 Kết quả mong đợi

Sau khi hoàn thành:
- ✅ Upload video bài học hoạt động (file ≤ 500MB)
- ✅ Chọn loại nội dung hoạt động
- ✅ Video được lưu đúng path và có thể access qua URL


## ❌ Lỗi hiện tại

1. **Không upload được video bài học**: Lỗi 400 Bad Request khi upload video
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart File Size Limit

**Vấn đề:** Multipart config chỉ cho phép 10MB, nhưng video có thể lên đến 500MB.

**Đã sửa:**
- Cập nhật `application.properties.example`: `max-file-size=500MB`, `max-request-size=500MB`

### 2. Cải thiện Select Component

**Đã sửa:**
- Thêm `id="lesson-type"` cho SelectTrigger (đã có sẵn)
- Thêm placeholder cho SelectValue
- Clear pending files khi thay đổi content type

### 3. Cải thiện Error Handling

**Đã sửa:**
- Thêm validation cho lesson creation
- Thêm logging chi tiết cho upload video
- Cải thiện error messages

## 📝 Cần làm tiếp

### Bước 1: Cập nhật Multipart Config trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa các biến môi trường:**

```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

**Hoặc nếu dùng application.properties, thêm vào file:**

```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### Bước 2: Kiểm tra Lesson Video Storage Path

Đảm bảo có biến môi trường:

```
LESSON_VIDEO_STORAGE_PATH=/app/uploads/lessons/videos
LESSON_VIDEO_BASE_URL=https://e-learning-backend-hchr.onrender.com/api/files/lessons/videos
```

**Lưu ý:** Thay URL bằng URL backend thực tế của bạn.

### Bước 3: Restart Backend

Sau khi cập nhật:
1. Click **Save Changes** trên Render
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

### Bước 4: Test Upload Video

1. Vào trang quản lý nội dung khóa học
2. Tạo bài học mới
3. Chọn loại nội dung: "Video bài giảng"
4. Upload video file
5. Kiểm tra xem có upload thành công không

## 🔍 Debug

### Kiểm tra Logs trên Render

Sau khi upload video, kiểm tra logs:

```
Upload Lesson Video Request
Course ID: ...
Chapter ID: ...
Lesson ID: ...
File name: ...
File size: ... bytes
Content type: ...
```

**Nếu không thấy log này:**
- Request không đến được backend
- Kiểm tra CORS và network

**Nếu thấy lỗi:**
- `File size exceeds...` → Multipart config chưa đúng
- `Invalid file type...` → File không phải video
- `Lesson not found` → Lesson chưa được tạo thành công

### Kiểm tra Console trên Frontend

Mở DevTools → Console, tìm:
- `Uploading video: {...}` - Request đã được gửi
- `Video uploaded successfully: ...` - Upload thành công
- `Video upload failed: ...` - Upload thất bại (xem error message)

## ⚠️ Lưu ý

1. **File Size:**
   - Video tối đa: 500MB
   - Nếu file lớn hơn, sẽ bị reject

2. **File Type:**
   - Chỉ chấp nhận video files (video/*)
   - Backend validate: `contentType.startsWith("video/")`

3. **Lesson Creation:**
   - Lesson phải được tạo thành công trước khi upload video
   - Nếu lesson creation fail, upload sẽ không thể thực hiện

4. **Timeout:**
   - Frontend timeout: 10 phút (600000ms) cho video lớn
   - Nếu upload quá lâu, có thể timeout

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã kiểm tra `LESSON_VIDEO_STORAGE_PATH` và `LESSON_VIDEO_BASE_URL`
- [ ] Đã restart backend trên Render
- [ ] Đã test upload video
- [ ] Đã test chọn loại nội dung

## 🎯 Kết quả mong đợi

Sau khi hoàn thành:
- ✅ Có thể chọn loại nội dung (VIDEO, TEXT, DOCUMENT, SLIDE)
- ✅ Có thể upload video file (tối đa 500MB)
- ✅ Video được lưu và hiển thị đúng URL
- ✅ Không còn lỗi 400 Bad Request


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


## ❌ Lỗi hiện tại

1. **Không upload được video bài học**: Lỗi 400 Bad Request khi upload video
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## ✅ Các thay đổi đã thực hiện

### 1. Tăng Multipart Config

**File:** `backend/src/main/resources/application.properties.example`

```properties
# File Upload
# Video files can be up to 500MB, so we need larger limits
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### 2. Thêm `consumes` cho Upload Endpoint

**File:** `backend/src/main/java/com/coursemgmt/controller/ChapterController.java`

```java
@PostMapping(value = "/{chapterId}/lessons/{lessonId}/upload-video", consumes = {"multipart/form-data"})
```

### 3. Sửa Select Component

**File:** `frontend/src/app/(dashboard)/instructor/courses/[id]/content/page.tsx`

- Thêm `id="lesson-type"` cho SelectTrigger
- Thêm `placeholder` cho SelectValue

### 4. Thêm Logging

- Backend: Log thông tin file khi upload
- Frontend: Log khi tạo lesson và upload video

## 📝 Cần làm tiếp

### Bước 1: Cập nhật Multipart Config trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa các biến:**

```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
```

**Hoặc** nếu Render không hỗ trợ biến môi trường này, cần tạo file `application-production.properties` với:

```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### Bước 2: Restart Backend

Sau khi cập nhật:
1. Click **Save Changes** trên Render
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

### Bước 3: Test

1. **Test chọn loại nội dung:**
   - Mở form thêm bài học
   - Click dropdown "Loại nội dung"
   - Chọn một loại (Video, Bài đọc, Tài liệu PDF, Slide)
   - Kiểm tra xem có chọn được không

2. **Test upload video:**
   - Chọn loại "Video bài giảng"
   - Chọn file video (nhỏ hơn 500MB)
   - Click "Tạo bài học"
   - Kiểm tra xem video có được upload không

## 🔍 Debug

### Nếu vẫn lỗi upload video:

1. **Kiểm tra logs trên Render:**
   - Vào Render Dashboard → Logs
   - Tìm log: "Upload Lesson Video Request"
   - Xem có lỗi gì không

2. **Kiểm tra file size:**
   - File phải nhỏ hơn 500MB
   - Nếu lớn hơn, cần tăng multipart config

3. **Kiểm tra lesson có được tạo không:**
   - Xem console log: "Lesson created:"
   - Kiểm tra `createdLesson.id` có giá trị không

### Nếu vẫn không chọn được loại nội dung:

1. **Kiểm tra console:**
   - Mở DevTools → Console
   - Xem có lỗi JavaScript không

2. **Kiểm tra Select component:**
   - Đảm bảo `id="lesson-type"` đã được thêm
   - Đảm bảo `value={contentType}` đúng

## ⚠️ Lưu ý

1. **Multipart Config:**
   - Phải được set trên Render (không chỉ trong code)
   - Nếu không set, mặc định là 1MB (quá nhỏ)

2. **File Size:**
   - Video: Tối đa 500MB
   - Document: Tối đa 50MB
   - Avatar: Tối đa 10MB

3. **Content-Type:**
   - Browser sẽ tự động set `Content-Type: multipart/form-data; boundary=...`
   - Không nên set thủ công trong frontend

## ✅ Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã restart backend trên Render
- [ ] Đã test chọn loại nội dung
- [ ] Đã test upload video (file nhỏ hơn 500MB)
- [ ] Đã kiểm tra logs trên Render

