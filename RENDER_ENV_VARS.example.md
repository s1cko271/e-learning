# 🔧 Render Environment Variables Configuration Template

Copy và paste các biến môi trường sau vào Render Dashboard.

## 📋 Database Configuration (Aiven)

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=[YOUR_PASSWORD]
```

## 🖥️ Server Configuration

```bash
PORT=10000
SPRING_PROFILES_ACTIVE=production
```

## 🌐 CORS Configuration

**⚠️ Cập nhật sau khi có URL Vercel:**

```bash
ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000
```

## 💳 VNPay Configuration

```bash
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=https://your-app.vercel.app/payment/vnpay-return
VNPAY_IPN_URL=https://your-backend.onrender.com/api/v1/vnpay/ipn
VNPAY_TMN_CODE=[YOUR_TMN_CODE]
VNPAY_HASH_SECRET=[YOUR_HASH_SECRET]
```

## 🔐 JWT Configuration

**⚠️ Tạo random string (64 ký tự):**

```bash
JWT_SECRET=[RANDOM_64_CHAR_STRING]
```

## 📧 Mail Configuration (Gmail)

```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=[GMAIL_APP_PASSWORD]
```

## 🤖 Gemini API Configuration

```bash
GEMINI_API_KEY=[YOUR_GEMINI_API_KEY]
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta
GEMINI_API_MODEL=gemini-2.5-flash
```

## 📁 File Storage Configuration

**⚠️ Cập nhật sau khi có URL Render backend:**

```bash
CERTIFICATE_STORAGE_PATH=/app/certificates
CERTIFICATE_BASE_URL=https://your-backend.onrender.com/certificates
AVATAR_STORAGE_PATH=/app/uploads/avatars
AVATAR_BASE_URL=https://your-backend.onrender.com/api/files/avatars
```

---

## ⚠️ Lưu ý

1. Tạo file `RENDER_ENV_VARS.md` (không commit) với thông tin thực tế
2. File này chỉ là template
3. Không commit file chứa password/secret thực tế

