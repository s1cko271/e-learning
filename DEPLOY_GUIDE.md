# 🚀 Hướng Dẫn Deploy E-Learning Platform

Hướng dẫn chi tiết deploy ứng dụng E-Learning lên:
- **Aiven** - MySQL Database
- **Render** - Spring Boot Backend
- **Vercel** - Next.js Frontend

---

## 📋 Mục Lục

1. [Bước 1: Setup Aiven MySQL Database](#bước-1-setup-aiven-mysql-database)
2. [Bước 2: Deploy Backend lên Render](#bước-2-deploy-backend-lên-render)
3. [Bước 3: Deploy Frontend lên Vercel](#bước-3-deploy-frontend-lên-vercel)
4. [Bước 4: Cấu hình VNPay IPN URL](#bước-4-cấu-hình-vnpay-ipn-url)
5. [Bước 5: Kiểm tra và Test](#bước-5-kiểm-tra-và-test)

---

## Bước 1: Setup Aiven MySQL Database

### ✅ ĐÃ HOÀN THÀNH

Database Aiven đã được cấu hình với thông tin sau:

**Thông tin Database:**
- **Host**: `e-learning-db-baophuc2712003-afff.l.aivencloud.com`
- **Port**: `23011`
- **Database**: `defaultdb`
- **Username**: `avnadmin`
- **Password**: `[PASSWORD_FROM_AIVEN]` ⚠️ **Không commit password thực tế**
- **SSL Mode**: `REQUIRED`

**Connection String cho Spring Boot:**
```
jdbc:mysql://e-learning-db-baophuc2712003-afff.l.aivencloud.com:23011/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
```

**⚠️ Lưu ý:** Password thực tế được lưu trong file `AIVEN_CONFIG.md` (không commit).  
📝 **Xem template tại:** [AIVEN_CONFIG.example.md](./AIVEN_CONFIG.example.md)

---

### 📚 Hướng dẫn cho người khác (nếu cần setup mới)

<details>
<summary>Click để xem hướng dẫn setup Aiven từ đầu</summary>

### 1.1. Đăng ký/Đăng nhập Aiven

1. Truy cập: https://console.aiven.io/
2. Đăng ký tài khoản mới hoặc đăng nhập
3. Tạo Project mới (ví dụ: `e-learning-platform`)

### 1.2. Tạo MySQL Service

1. Trong Dashboard, click **"Create service"**
2. Chọn:
   - **Service type**: `MySQL`
   - **Cloud provider**: Chọn gần nhất (AWS, GCP, Azure)
   - **Region**: Chọn gần nhất
   - **Plan**: `Startup-1` (free tier) hoặc `Business-4` (production)
   - **Service name**: `mysql-elearning` (hoặc tên bạn muốn)

3. Click **"Create service"** và đợi 2-3 phút để service được tạo

### 1.3. Lấy Connection String

1. Sau khi service được tạo, vào tab **"Overview"**
2. Tìm section **"Connection information"**
3. Copy các thông tin sau:
   - **Host**
   - **Port**
   - **Database name** (mặc định: `defaultdb`)
   - **Username** (mặc định: `avnadmin`)
   - **Password** (click "Show" để xem)

**Lưu ý quan trọng:**
- Aiven sử dụng SSL, cần thêm `useSSL=true&requireSSL=true` trong JDBC URL
- Port có thể khác 3306

</details>

---

## Bước 2: Deploy Backend lên Render

### 2.1. Chuẩn bị Repository

1. Đảm bảo code đã được push lên GitHub
2. Repository phải có:
   - `backend/Dockerfile`
   - `backend/pom.xml`
   - `backend/src/main/resources/application.properties.production`

### 2.2. Tạo Web Service trên Render

1. Truy cập: https://dashboard.render.com/
2. Đăng ký/Đăng nhập
3. Click **"New +"** → **"Web Service"**
4. Kết nối GitHub repository của bạn
5. Chọn repository và branch (thường là `main`)

### 2.3. Cấu hình Build & Deploy

**Basic Settings:**
- **Name**: `e-learning-backend` (hoặc tên bạn muốn)
- **Region**: Chọn gần nhất
- **Branch**: `main` (hoặc branch bạn muốn deploy)
- **Root Directory**: `backend` ⚠️ **QUAN TRỌNG**
- **Runtime**: `Docker`
- **Dockerfile Path**: `backend/Dockerfile` (hoặc chỉ `Dockerfile` nếu Root Directory đã là `backend`)

**Build Command**: (Để trống nếu dùng Dockerfile)

**Start Command**: (Để trống nếu dùng Dockerfile)

### 2.4. Cấu hình Environment Variables

Thêm các biến môi trường sau trong **"Environment"** tab:

#### Database Configuration (từ Aiven)
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://e-learning-db-baophuc2712003-afff.l.aivencloud.com:23011/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=[YOUR_PASSWORD_FROM_AIVEN]
```

📝 **Copy toàn bộ danh sách Environment Variables tại:** [RENDER_ENV_VARS.md](./RENDER_ENV_VARS.md)

#### Server Configuration
```bash
PORT=10000
SPRING_PROFILES_ACTIVE=production
```

#### CORS Configuration (sẽ cập nhật sau khi có Vercel URL)
```bash
ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000
```

#### VNPay Configuration
```bash
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=https://your-app.vercel.app/payment/vnpay-return
VNPAY_IPN_URL=https://your-backend.onrender.com/api/v1/vnpay/ipn
VNPAY_TMN_CODE=PISGV29M
VNPAY_HASH_SECRET=DRC0V9AAYA651P2SID7SVYRY46HND1H4
```

**Lưu ý:** 
- `VNPAY_IPN_URL` sẽ là URL Render của bạn (ví dụ: `https://e-learning-backend.onrender.com/api/v1/vnpay/ipn`)
- `VNPAY_RETURN_URL` sẽ là URL Vercel của bạn (cập nhật sau)

#### JWT Configuration
```bash
JWT_SECRET=[TẠO_MỘT_CHUỖI_BÍ_MẬT_DÀI_VÀ_NGẪU_NHIÊN]
```

**Tạo JWT Secret:**
```bash
# Trên Linux/Mac:
openssl rand -base64 64

# Hoặc dùng online tool: https://www.random.org/strings/
```

#### Mail Configuration (Gmail)
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=[GMAIL_APP_PASSWORD]
```

**Lưu ý:** Cần tạo App Password từ Gmail:
1. Vào Google Account → Security
2. Enable 2-Step Verification
3. Tạo App Password cho "Mail"

#### Gemini API Configuration
```bash
GEMINI_API_KEY=your-gemini-api-key
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta
GEMINI_API_MODEL=gemini-2.5-flash
```

#### File Storage Configuration
```bash
CERTIFICATE_STORAGE_PATH=/app/certificates
CERTIFICATE_BASE_URL=https://your-backend.onrender.com/certificates
AVATAR_STORAGE_PATH=/app/uploads/avatars
AVATAR_BASE_URL=https://your-backend.onrender.com/api/files/avatars
```

### 2.5. Deploy

1. Click **"Create Web Service"**
2. Render sẽ tự động build và deploy
3. Đợi 5-10 phút để build hoàn tất
4. Sau khi deploy xong, bạn sẽ có URL: `https://your-backend.onrender.com`

### 2.6. Kiểm tra Logs

1. Vào tab **"Logs"** để xem quá trình build và deploy
2. Kiểm tra xem có lỗi không
3. Nếu có lỗi kết nối database, kiểm tra lại:
   - Connection string từ Aiven
   - SSL settings (`useSSL=true&requireSSL=true`)
   - Firewall rules trên Aiven (nếu có)

### 2.7. Test Backend

Sau khi deploy xong, test API:
```bash
curl https://your-backend.onrender.com/api/v1/health
# hoặc
curl https://your-backend.onrender.com/api/v1/courses
```

---

## Bước 3: Deploy Frontend lên Vercel

### 3.1. Chuẩn bị Repository

1. Đảm bảo code đã được push lên GitHub
2. Repository phải có:
   - `frontend/package.json`
   - `frontend/next.config.ts`
   - `frontend/src/` directory

### 3.2. Tạo Project trên Vercel

1. Truy cập: https://vercel.com/
2. Đăng ký/Đăng nhập (có thể dùng GitHub account)
3. Click **"Add New..."** → **"Project"**
4. Import GitHub repository của bạn

### 3.3. Cấu hình Project

**Project Settings:**
- **Framework Preset**: `Next.js` (tự động detect)
- **Root Directory**: `frontend` ⚠️ **QUAN TRỌNG**
- **Build Command**: `npm run build` (hoặc `cd frontend && npm run build` nếu không set Root Directory)
- **Output Directory**: `.next` (mặc định)
- **Install Command**: `npm install`

### 3.4. Cấu hình Environment Variables

Thêm các biến môi trường trong **"Environment Variables"**:

#### API Configuration
```bash
NEXT_PUBLIC_API_URL=https://your-backend.onrender.com
```

**Lưu ý:** 
- `NEXT_PUBLIC_` prefix là bắt buộc để Next.js expose biến này ra client-side
- Thay `your-backend.onrender.com` bằng URL Render thực tế của bạn

### 3.5. Deploy

1. Click **"Deploy"**
2. Vercel sẽ tự động build và deploy
3. Đợi 2-5 phút
4. Sau khi deploy xong, bạn sẽ có URL: `https://your-app.vercel.app`

### 3.6. Cập nhật Backend CORS

Sau khi có URL Vercel, quay lại Render và cập nhật:

```bash
ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000
```

Sau đó **"Manual Deploy"** lại backend để áp dụng thay đổi.

### 3.7. Cập nhật VNPay Return URL

Cập nhật trên Render:

```bash
VNPAY_RETURN_URL=https://your-app.vercel.app/payment/vnpay-return
```

---

## Bước 4: Cấu hình VNPay IPN URL

### 4.1. Lấy IPN URL từ Render

IPN URL sẽ là:
```
https://your-backend.onrender.com/api/v1/vnpay/ipn
```

### 4.2. Cấu hình trên VNPay Dashboard

1. Đăng nhập VNPay Sandbox: https://sandbox.vnpayment.vn/merchantv2/
2. Vào **"Cấu hình"** → **"Cấu hình IPN"**
3. Nhập IPN URL: `https://your-backend.onrender.com/api/v1/vnpay/ipn`
4. Click **"Lưu"**

### 4.3. Test IPN Callback

1. Vào **"Kiểm tra (test case)"**: https://sandbox.vnpayment.vn/vnpaygw-sit-testing/user/login
2. Test IPN callback để đảm bảo Render nhận được request

---

## Bước 5: Kiểm tra và Test

### 5.1. Kiểm tra Database Connection

1. Vào Render Logs
2. Kiểm tra xem có log kết nối database thành công không
3. Nếu có lỗi, kiểm tra lại connection string

### 5.2. Test API Endpoints

```bash
# Test health check
curl https://your-backend.onrender.com/api/v1/health

# Test courses
curl https://your-backend.onrender.com/api/v1/courses

# Test authentication
curl -X POST https://your-backend.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### 5.3. Test Frontend

1. Truy cập: `https://your-app.vercel.app`
2. Test các chức năng:
   - Đăng nhập/Đăng ký
   - Xem danh sách khóa học
   - Thanh toán VNPay
   - Upload file
   - Chat

### 5.4. Test VNPay Payment Flow

1. Chọn một khóa học và thanh toán
2. Kiểm tra:
   - Redirect đến VNPay gateway
   - Thanh toán thành công
   - Redirect về frontend
   - IPN callback được gọi (check Render logs)

---

## 🔧 Troubleshooting

### Lỗi kết nối Database

**Lỗi:** `Communications link failure`

**Giải pháp:**
1. Kiểm tra connection string có đúng không
2. Đảm bảo có `useSSL=true&requireSSL=true`
3. Kiểm tra firewall rules trên Aiven
4. Kiểm tra port (có thể không phải 3306)

### Lỗi CORS

**Lỗi:** `Access to fetch at '...' from origin '...' has been blocked by CORS policy`

**Giải pháp:**
1. Kiểm tra `ALLOWED_ORIGINS` trên Render có đúng URL Vercel không
2. Đảm bảo không có trailing slash
3. Restart service trên Render

### Lỗi Build trên Render

**Lỗi:** `Build failed`

**Giải pháp:**
1. Kiểm tra Dockerfile có đúng không
2. Kiểm tra `pom.xml` có dependency nào thiếu không
3. Xem logs chi tiết trên Render

### Lỗi Build trên Vercel

**Lỗi:** `Build Error`

**Giải pháp:**
1. Kiểm tra `next.config.ts`
2. Kiểm tra `package.json` có script `build` không
3. Kiểm tra Root Directory có đúng `frontend` không

### Backend không nhận được IPN Callback

**Giải pháp:**
1. Kiểm tra IPN URL trên VNPay Dashboard
2. Kiểm tra Render logs để xem có request đến không
3. Đảm bảo endpoint `/api/v1/vnpay/ipn` là public (không cần auth)

---

## 📝 Checklist Deploy

- [ ] Aiven MySQL service đã được tạo và running
- [ ] Đã lưu connection string từ Aiven
- [ ] Render Web Service đã được tạo
- [ ] Tất cả Environment Variables đã được set trên Render
- [ ] Backend đã deploy thành công và có thể truy cập
- [ ] Vercel project đã được tạo
- [ ] `NEXT_PUBLIC_API_URL` đã được set trên Vercel
- [ ] Frontend đã deploy thành công
- [ ] CORS đã được cập nhật trên Render với URL Vercel
- [ ] VNPay IPN URL đã được cấu hình trên VNPay Dashboard
- [ ] VNPay Return URL đã được cập nhật trên Render
- [ ] Đã test tất cả các chức năng chính

---

## 🎉 Hoàn thành!

Sau khi hoàn thành tất cả các bước, ứng dụng của bạn sẽ chạy trên:
- **Database**: Aiven MySQL
- **Backend**: Render (https://your-backend.onrender.com)
- **Frontend**: Vercel (https://your-app.vercel.app)

Chúc bạn deploy thành công! 🚀

