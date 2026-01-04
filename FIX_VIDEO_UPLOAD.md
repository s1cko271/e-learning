# 🔧 Sửa lỗi Upload Video và Dropdown Loại Nội Dung

## ❌ Vấn đề

1. **Không upload được video bài học**: Lỗi 400 Bad Request
2. **Không chọn được loại nội dung**: Dropdown không hoạt động

## 🔍 Nguyên nhân

### 1. Multipart Config Quá Nhỏ

- Config hiện tại: `max-file-size=10MB`
- Video cần: `500MB`
- → Spring Boot reject file trước khi đến controller

### 2. Dropdown Loại Nội Dung

- Có thể do state không được set đúng
- Hoặc Select component có vấn đề

## ✅ Giải pháp

### 1. Cập nhật Multipart Config trên Render

Vào Render Dashboard → Web Service `e-learning-backend` → Environment:

**Thêm/Sửa các biến:**

```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB
SPRING_SERVLET_MULTIPART_ENABLED=true
```

**Lưu ý:** 
- Render sẽ tự động map `SPRING_SERVLET_MULTIPART_*` thành `spring.servlet.multipart.*`
- Sau khi cập nhật, restart service

### 2. Kiểm tra Code

#### Backend:
- ✅ Đã cập nhật `application.properties.example` với 500MB
- ✅ `FileStorageService.storeLessonVideo()` đã validate 500MB
- ✅ Exception handler đã handle `MaxUploadSizeExceededException`

#### Frontend:
- ✅ Select component có vẻ OK
- ✅ State management có vẻ OK

### 3. Debug Steps

#### Kiểm tra Multipart Config:

1. **Xem logs trên Render:**
   - Tìm log: `MaxUploadSizeExceededException`
   - Hoặc: `MultipartException`

2. **Test endpoint:**
   ```bash
   # Test với file nhỏ trước (dưới 10MB)
   curl -X POST https://e-learning-backend-hchr.onrender.com/api/v1/courses/15/chapters/1/lessons/1/upload-video \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -F "file=@small-video.mp4"
   ```

3. **Kiểm tra file size:**
   - File video: 92.69 MB (trong hình)
   - Config cũ: 10MB → **Lỗi!**
   - Config mới: 500MB → **OK**

#### Kiểm tra Dropdown:

1. **Mở DevTools Console:**
   - Xem có lỗi JavaScript không
   - Kiểm tra state `contentType` có được set không

2. **Test thủ công:**
   - Click vào dropdown
   - Chọn option khác
   - Xem state có thay đổi không

## 📝 Checklist

- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB` trên Render
- [ ] Đã cập nhật `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB` trên Render
- [ ] Đã restart backend trên Render
- [ ] Đã test upload video với file < 500MB
- [ ] Đã kiểm tra dropdown loại nội dung hoạt động
- [ ] Đã xem logs trên Render không có lỗi multipart

## ⚠️ Lưu ý

1. **File Size Limits:**
   - Video: 500MB
   - Document: 50MB
   - Image: 10MB

2. **Timeout:**
   - Frontend đã set timeout 10 phút cho video upload
   - Backend cần đủ thời gian để xử lý file lớn

3. **Storage:**
   - Đảm bảo Render có đủ disk space
   - Video files sẽ được lưu trong `/app/uploads/lessons/videos/`

## 🎯 Sau khi sửa

1. **Restart Backend:**
   - Render sẽ tự động restart sau khi cập nhật env vars
   - Hoặc click "Manual Deploy" → "Deploy latest commit"

2. **Test:**
   - Upload video < 500MB
   - Kiểm tra dropdown loại nội dung
   - Xem logs nếu vẫn lỗi

