# ⚡ Quick Start - Deploy Checklist

Hướng dẫn nhanh để deploy ứng dụng. Xem chi tiết tại [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)

## 🎯 Thứ tự thực hiện

### 1️⃣ Aiven (5 phút)

```bash
# 1. Tạo MySQL service trên Aiven
# 2. Copy connection string:
Host: mysql-xxxxx.a.aivencloud.com
Port: 25060
Database: defaultdb
Username: avnadmin
Password: [từ Aiven]
```

**Connection String:**
```
jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
```

---

### 2️⃣ Render Backend (10 phút)

**Settings:**
- Root Directory: `backend`
- Runtime: `Docker`

**Environment Variables:**

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DB]?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=[PASSWORD]

# Server
PORT=10000
SPRING_PROFILES_ACTIVE=production

# CORS (cập nhật sau khi có Vercel URL)
ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000

# VNPay
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=https://your-app.vercel.app/payment/vnpay-return
VNPAY_IPN_URL=https://your-backend.onrender.com/api/v1/vnpay/ipn
VNPAY_TMN_CODE=PISGV29M
VNPAY_HASH_SECRET=DRC0V9AAYA651P2SID7SVYRY46HND1H4

# JWT (tạo random string)
JWT_SECRET=[RANDOM_64_CHAR_STRING]

# Mail (Gmail App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=[GMAIL_APP_PASSWORD]

# Gemini
GEMINI_API_KEY=[YOUR_KEY]
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta
GEMINI_API_MODEL=gemini-2.5-flash

# File Storage
CERTIFICATE_STORAGE_PATH=/app/certificates
CERTIFICATE_BASE_URL=https://your-backend.onrender.com/certificates
AVATAR_STORAGE_PATH=/app/uploads/avatars
AVATAR_BASE_URL=https://your-backend.onrender.com/api/files/avatars
```

**Sau khi deploy xong, lấy URL:** `https://your-backend.onrender.com`

---

### 3️⃣ Vercel Frontend (5 phút)

**Settings:**
- Root Directory: `frontend`
- Framework: `Next.js`

**Environment Variables:**

```bash
NEXT_PUBLIC_API_URL=https://your-backend.onrender.com
```

**Sau khi deploy xong, lấy URL:** `https://your-app.vercel.app`

---

### 4️⃣ Cập nhật CORS & VNPay URLs

**Quay lại Render, cập nhật:**

```bash
ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000
VNPAY_RETURN_URL=https://your-app.vercel.app/payment/vnpay-return
```

**Manual Deploy lại backend.**

---

### 5️⃣ Cấu hình VNPay IPN

1. Đăng nhập: https://sandbox.vnpayment.vn/merchantv2/
2. Vào **Cấu hình** → **Cấu hình IPN**
3. Nhập: `https://your-backend.onrender.com/api/v1/vnpay/ipn`
4. Lưu

---

## ✅ Test

```bash
# Backend
curl https://your-backend.onrender.com/api/v1/courses

# Frontend
# Mở: https://your-app.vercel.app
```

---

## 🔗 Links hữu ích

- **Aiven Console**: https://console.aiven.io/
- **Render Dashboard**: https://dashboard.render.com/
- **Vercel Dashboard**: https://vercel.com/dashboard
- **VNPay Sandbox**: https://sandbox.vnpayment.vn/merchantv2/

---

## ⚠️ Lưu ý quan trọng

1. **Aiven SSL**: Phải có `useSSL=true&requireSSL=true` trong connection string
2. **Root Directory**: Render = `backend`, Vercel = `frontend`
3. **CORS**: Phải cập nhật sau khi có URL Vercel
4. **VNPay IPN**: Phải cấu hình trên VNPay Dashboard
5. **JWT Secret**: Phải là chuỗi random dài và bảo mật

