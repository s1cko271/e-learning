# 🔐 Aiven Database Configuration

## 📝 Template - Thay thế với thông tin thực tế của bạn

### Connection Details
- **Host**: `[YOUR_HOST].aivencloud.com`
- **Port**: `[PORT]`
- **Database**: `defaultdb`
- **Username**: `avnadmin`
- **Password**: `[YOUR_PASSWORD]`
- **SSL Mode**: `REQUIRED`

### Service URI
```
mysql://avnadmin:[PASSWORD]@[HOST]:[PORT]/defaultdb?ssl-mode=REQUIRED
```

---

## 🔧 Connection String cho Spring Boot

### JDBC URL
```
jdbc:mysql://[HOST]:[PORT]/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
```

### Environment Variables cho Render

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=[YOUR_PASSWORD]
```

---

## ⚠️ Lưu ý

1. Tạo file `AIVEN_CONFIG.md` (không commit) với thông tin thực tế
2. File này chỉ là template
3. Không commit file chứa password thực tế

