# ✅ Deployment Checklist

Sử dụng checklist này để đảm bảo không bỏ sót bước nào khi deploy.

## 📋 Pre-Deployment

- [ ] Code đã được push lên GitHub
- [ ] Đã test local và mọi thứ hoạt động tốt
- [ ] Đã có tài khoản Aiven, Render, Vercel
- [ ] Đã chuẩn bị các API keys (Gmail App Password, Gemini API)

---

## 🗄️ Bước 1: Aiven MySQL

- [ ] Đã đăng ký/đăng nhập Aiven
- [ ] Đã tạo Project mới
- [ ] Đã tạo MySQL service
- [ ] Đã chọn plan phù hợp (Startup-1 free hoặc Business-4)
- [ ] Đã lưu thông tin kết nối:
  - [ ] Host
  - [ ] Port
  - [ ] Database name
  - [ ] Username
  - [ ] Password
- [ ] Đã tạo connection string với SSL
- [ ] Đã test kết nối (nếu có thể)

---

## 🔧 Bước 2: Render Backend

### Setup Service
- [ ] Đã kết nối GitHub repository
- [ ] Đã chọn branch (main)
- [ ] Đã set Root Directory: `backend`
- [ ] Đã chọn Runtime: `Docker`

### Environment Variables
- [ ] `SPRING_DATASOURCE_URL` (với SSL)
- [ ] `SPRING_DATASOURCE_USERNAME`
- [ ] `SPRING_DATASOURCE_PASSWORD`
- [ ] `PORT=10000`
- [ ] `SPRING_PROFILES_ACTIVE=production`
- [ ] `ALLOWED_ORIGINS` (tạm thời, sẽ cập nhật sau)
- [ ] `VNPAY_URL`
- [ ] `VNPAY_RETURN_URL` (tạm thời, sẽ cập nhật sau)
- [ ] `VNPAY_IPN_URL` (với URL Render)
- [ ] `VNPAY_TMN_CODE`
- [ ] `VNPAY_HASH_SECRET`
- [ ] `JWT_SECRET` (random string)
- [ ] `MAIL_HOST`
- [ ] `MAIL_PORT`
- [ ] `MAIL_USERNAME`
- [ ] `MAIL_PASSWORD` (Gmail App Password)
- [ ] `GEMINI_API_KEY`
- [ ] `GEMINI_API_URL`
- [ ] `GEMINI_API_MODEL`
- [ ] `CERTIFICATE_STORAGE_PATH`
- [ ] `CERTIFICATE_BASE_URL`
- [ ] `AVATAR_STORAGE_PATH`
- [ ] `AVATAR_BASE_URL`

### Deploy
- [ ] Đã click "Create Web Service"
- [ ] Build đã thành công (không có lỗi)
- [ ] Service đã running
- [ ] Đã lưu URL backend: `https://your-backend.onrender.com`
- [ ] Đã test API endpoint (ví dụ: `/api/v1/courses`)

---

## 🎨 Bước 3: Vercel Frontend

### Setup Project
- [ ] Đã kết nối GitHub repository
- [ ] Đã chọn branch (main)
- [ ] Đã set Root Directory: `frontend`
- [ ] Framework đã được detect: `Next.js`

### Environment Variables
- [ ] `NEXT_PUBLIC_API_URL` (với URL Render backend)

### Deploy
- [ ] Đã click "Deploy"
- [ ] Build đã thành công
- [ ] Đã lưu URL frontend: `https://your-app.vercel.app`
- [ ] Đã test mở trang web

---

## 🔄 Bước 4: Cập nhật URLs

### Render Backend
- [ ] Đã cập nhật `ALLOWED_ORIGINS` với URL Vercel
- [ ] Đã cập nhật `VNPAY_RETURN_URL` với URL Vercel
- [ ] Đã cập nhật `CERTIFICATE_BASE_URL` với URL Render
- [ ] Đã cập nhật `AVATAR_BASE_URL` với URL Render
- [ ] Đã manual deploy lại backend

### VNPay Dashboard
- [ ] Đã đăng nhập VNPay Sandbox
- [ ] Đã vào Cấu hình → Cấu hình IPN
- [ ] Đã nhập IPN URL: `https://your-backend.onrender.com/api/v1/vnpay/ipn`
- [ ] Đã lưu cấu hình
- [ ] Đã test IPN callback (nếu có)

---

## 🧪 Bước 5: Testing

### Backend API
- [ ] Health check endpoint hoạt động
- [ ] Courses endpoint hoạt động
- [ ] Authentication endpoint hoạt động
- [ ] Database connection thành công (check logs)

### Frontend
- [ ] Trang chủ load được
- [ ] Đăng nhập/Đăng ký hoạt động
- [ ] Danh sách khóa học hiển thị
- [ ] API calls đến backend thành công (check Network tab)

### VNPay Payment
- [ ] Có thể tạo payment URL
- [ ] Redirect đến VNPay gateway thành công
- [ ] Thanh toán test thành công
- [ ] Return về frontend thành công
- [ ] IPN callback được gọi (check Render logs)
- [ ] Transaction được cập nhật trong database

### File Upload
- [ ] Upload avatar thành công
- [ ] Upload certificate thành công
- [ ] File được lưu và truy cập được

---

## 🔒 Security Checklist

- [ ] JWT Secret là random string dài và bảo mật
- [ ] Database password không được commit vào code
- [ ] API keys không được commit vào code
- [ ] CORS chỉ cho phép domain Vercel
- [ ] SSL được enable cho database connection
- [ ] Environment variables được set đúng trên Render/Vercel

---

## 📝 Documentation

- [ ] Đã đọc [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)
- [ ] Đã đọc [DEPLOY_QUICK_START.md](./DEPLOY_QUICK_START.md)
- [ ] Đã lưu tất cả URLs và credentials ở nơi an toàn

---

## 🎉 Hoàn thành!

Sau khi check tất cả các mục trên, ứng dụng của bạn đã sẵn sàng production!

**URLs:**
- Frontend: `https://your-app.vercel.app`
- Backend: `https://your-backend.onrender.com`
- Database: Aiven MySQL

---

## 🆘 Nếu có lỗi

Xem phần **Troubleshooting** trong [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)

