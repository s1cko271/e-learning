# 🔧 Sửa lỗi CORS - Cập nhật ALLOWED_ORIGINS trên Render

## ❌ Lỗi hiện tại

Frontend trên Vercel không thể kết nối với backend trên Render do lỗi CORS:

```
Access to XMLHttpRequest at 'https://e-learning-backend-hchr.onrender.com/api/auth/register' 
from origin 'https://e-learning-git-main-s1cko271s-projects.vercel.app' 
has been blocked by CORS policy: 
Response to preflight request doesn't pass access control check: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

## ✅ Giải pháp

Cập nhật biến môi trường `ALLOWED_ORIGINS` trên Render để thêm URL Vercel mới.

---

## Bước 1: Vào Render Dashboard

1. Truy cập: https://dashboard.render.com/
2. Vào Web Service `e-learning-backend`
3. Vào tab **Environment**

---

## Bước 2: Tìm và sửa biến `ALLOWED_ORIGINS`

1. Tìm biến môi trường có tên: `ALLOWED_ORIGINS`
2. Click vào biến đó để sửa

### Giá trị cần cập nhật:

```
https://e-learning-git-main-s1cko271s-projects.vercel.app,https://e-learning-puce-two.vercel.app,https://e-learning-3yk718cx4-s1cko271s-projects.vercel.app,http://localhost:3000
```

**Lưu ý:**
- Thêm URL mới: `https://e-learning-git-main-s1cko271s-projects.vercel.app`
- Giữ nguyên các URL cũ
- Phân cách bằng dấu phẩy (`,`)
- **KHÔNG** có khoảng trắng sau dấu phẩy
- **KHÔNG** có trailing slash (`/`) ở cuối URL

---

## Bước 3: Lưu và chờ restart

1. Click **Save Changes** ở cuối trang
2. Render sẽ tự động restart service
3. Đợi 1-2 phút để service restart xong

---

## Bước 4: Kiểm tra

### Kiểm tra trong Render Logs:
1. Vào tab **Logs** trên Render
2. Tìm log: `Started CourseManagementSystemApplication`
3. Kiểm tra không có lỗi CORS

### Kiểm tra trên Frontend:
1. Mở: https://e-learning-git-main-s1cko271s-projects.vercel.app/register
2. Thử đăng ký tài khoản
3. Kiểm tra console không còn lỗi CORS

---

## 📝 Lưu ý

- Nếu bạn có nhiều URL Vercel (preview deployments), thêm tất cả vào `ALLOWED_ORIGINS`
- Format: `url1,url2,url3` (không có khoảng trắng)
- Sau khi cập nhật, backend sẽ tự động restart và áp dụng cấu hình mới

