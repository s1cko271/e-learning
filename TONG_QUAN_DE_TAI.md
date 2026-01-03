# TỔNG QUAN ĐỀ TÀI

## 1.1 Giới thiệu đề tài

### 1.1.1 Lý do chọn đề tài

Trong bối cảnh kỷ nguyên số và sự bùng nổ của cuộc cách mạng công nghiệp 4.0, giáo dục trực tuyến (E-learning) đã không còn là một lựa chọn phụ mà đã trở thành một xu thế tất yếu toàn cầu. Việc xây dựng một hệ thống quản lý học tập hiện đại không chỉ giúp tối ưu hóa quy trình đào tạo mà còn mở ra cơ hội tiếp cận tri thức không giới hạn về không gian và thời gian. Xuất phát từ thực tế đó, em quyết định thực hiện đề tài "Xây dựng hệ thống học trực tuyến E-learning" vì những lý do sau:

**Tính cấp thiết và thực tiễn của giáo dục trực tuyến:** Sự thay đổi trong thói quen học tập sau đại dịch COVID-19 đã thúc đẩy nhu cầu về các nền tảng học tập từ xa. Người học cần một hệ thống linh hoạt, cho phép họ chủ động theo dõi tiến độ, truy cập tài liệu đa phương tiện (video, slide, tài liệu) và nhận phản hồi tức thì.

**Giải quyết các bài toán quản lý và vận hành đào tạo:** Hệ thống giúp tự động hóa nhiều quy trình mà phương thức truyền thống khó thực hiện hiệu quả như đánh giá tự động, chứng nhận chuyên nghiệp và quản trị toàn diện. Hệ thống được xây dựng hỗ trợ đầy đủ quy trình từ đăng ký, học tập, thi cử đến cấp chứng chỉ tự động.

**Ứng dụng công nghệ hiện đại và bảo mật thông tin:** Việc phát triển hệ thống này là cơ hội để em nghiên cứu và áp dụng các công nghệ lập trình tiên tiến nhằm giải quyết các vấn đề thực tế như bảo mật, tính hợp thanh toán và trí tuệ nhân tạo. Hệ thống tích hợp WebSocket cho chat real-time, AI Chatbot với Google Gemini để hỗ trợ học viên 24/7, và cổng thanh toán VNPay phù hợp với thị trường Việt Nam.

### 1.1.2 Khảo sát các hệ thống tương tự

Hiện nay, các nền tảng như Udemy hay Coursera đã đặt ra những tiêu chuẩn cao cho giáo dục trực tuyến. Tuy nhiên, qua khảo sát, các hệ thống này vẫn tồn tại những hạn chế khi áp dụng tại Việt Nam như: rào cản về ngôn ngữ, phương thức thanh toán quốc tế phức tạp và thiếu sự hỗ trợ cá nhân hóa theo lộ trình học tập nội địa. 

Hệ thống được đề xuất trong đồ án sẽ kế thừa các ưu điểm về quản lý nội dung đồng thời tối ưu hóa tính năng thanh toán nội địa (VNPay), hỗ trợ tự động hóa thông qua chatbot AI với khả năng hiểu ngữ cảnh tiếng Việt, và tích hợp hệ thống chat real-time giữa học viên và giảng viên để tăng tính tương tác.

### 1.1.3 Cấu trúc đồ án

Nội dung đồ án được tổ chức thành 5 chương chính và phần tài liệu tham khảo, cụ thể như sau:

- **Chương 1:** Giới thiệu đề tài và công nghệ sử dụng
- **Chương 2:** Đặc tả và phân tích hệ thống
- **Chương 3:** Thiết kế hệ thống
- **Chương 4:** Cài đặt và thử nghiệm hệ thống
- **Chương 5:** Kết luận

## 1.2. Công nghệ sử dụng

### 1.2.1. Front-end

#### a. Framework: Next.js 16

Next.js là một framework React mạnh mẽ cho phép xây dựng các ứng dụng web với khả năng render phía máy chủ và tạo trang tĩnh. Trong dự án này, phiên bản Next.js 16.0.10 được sử dụng với cơ chế App Router, giúp tổ chức thư mục theo hướng route-based, hỗ trợ tối ưu hóa cấu trúc dự án ngay từ đầu.

**Ưu điểm:**
- **Hiệu năng và SEO:** Hỗ trợ Server Components giúp giảm tải JavaScript gửi xuống client, tăng tốc độ tải trang và tối ưu hóa SEO tốt hơn so với React thuần.
- **Routing mạnh mẽ:** Hệ thống file-system routing của App Router giúp việc phân chia layout trở nên trực quan và dễ quản lý. Dự án sử dụng route groups `(auth)`, `(dashboard)` để tổ chức các trang một cách logic.
- **Tích hợp sẵn:** Hỗ trợ tốt việc quản lý hình ảnh, font chữ và các cấu hình biên dịch mà không cần cài đặt phức tạp.

**Nhược điểm:**
- **Đường cong học tập:** App Router là mô hình mới, đòi hỏi người phát triển phải hiểu rõ sự khác biệt giữa Server Component và Client Component.
- **Build time:** Thời gian build dự án có thể lâu hơn so với ứng dụng React truyền thống do quá trình tiền xử lý phía server.

#### b. Ngôn ngữ: TypeScript 5

Toàn bộ mã nguồn Frontend được viết bằng TypeScript 5. Đây là một siêu tập hợp của JavaScript, bổ sung tính năng định kiểu tĩnh, giúp kiểm soát chặt chẽ dữ liệu đầu vào và đầu ra trong các component và service.

**Ưu điểm:**
- **An toàn kiểu dữ liệu:** Giảm thiểu tối đa các lỗi runtime phổ biến nhờ việc phát hiện lỗi ngay trong quá trình viết code.
- **Dễ bảo trì và mở rộng:** Code tường minh, dễ hiểu nhờ các interface/type rõ ràng, hỗ trợ tốt cho việc làm việc nhóm và refactor code sau này.
- **Hỗ trợ IDE tốt:** Các tính năng gợi ý code hoạt động hiệu quả, giúp tăng tốc độ phát triển.

**Nhược điểm:**
- **Cấu hình ban đầu:** Cần thời gian thiết lập môi trường và định nghĩa các kiểu dữ liệu cho các API response và Props.
- **Khối lượng code tăng:** Số lượng dòng code sẽ nhiều hơn so với JavaScript thuần do phải khai báo kiểu.

#### c. UI & Design System: Tailwind CSS 4 và shadcn/ui

Hệ thống giao diện được xây dựng dựa trên Tailwind CSS 4 kết hợp với shadcn/ui (bộ sưu tập các component tái sử dụng dựa trên Radix UI). Các thành phần UI như Button, Card, Dialog, Form Control được đồng bộ hóa style và đảm bảo tính Responsive 100% trên mọi thiết bị.

**Ưu điểm:**
- **Tốc độ phát triển nhanh:** Không cần viết CSS thủ công hoặc đặt tên class phức tạp, chỉ cần sử dụng các class utility có sẵn của Tailwind.
- **Tính nhất quán:** shadcn/ui cung cấp các component được thiết kế chuẩn mực, giúp giao diện đồng bộ từ trang Dashboard đến trang khóa học.
- **Tùy biến cao:** Dễ dàng chỉnh sửa theme, màu sắc, spacing thông qua file cấu hình Tailwind.

**Nhược điểm:**
- **HTML trở nên dài dòng:** Việc nhúng quá nhiều class utility vào HTML có thể làm giảm tính đọc được của code, tuy nhiên điều này được giảm thiểu nhờ việc sử dụng component-based architecture.

#### d. Data Fetching & State Management: TanStack Query (React Query) và Zustand

Việc quản lý trạng thái ứng dụng được thực hiện thông qua TanStack Query (React Query) cho server state và Zustand cho client state. TanStack Query cung cấp caching, background updates, và optimistic updates tự động, trong khi Zustand quản lý các state cục bộ như giỏ hàng, authentication state.

**Ưu điểm:**
- **Tự động caching và refetching:** TanStack Query tự động cache dữ liệu từ API, giảm số lượng request không cần thiết và cải thiện hiệu năng.
- **Đơn giản và hiệu quả:** Zustand là state management library nhẹ, không cần cài đặt các thư viện quá phức tạp như Redux cho các nhu cầu cơ bản.
- **Tách biệt logic:** Giúp UI component gọn nhẹ, chỉ tập trung vào hiển thị, còn logic xử lý dữ liệu nằm ở Custom Hooks và Stores.

**Nhược điểm:**
- **Phạm vi quản lý:** Nếu ứng dụng mở rộng quá lớn với nhiều trạng thái phức tạp chằng chịt, việc chỉ dùng Hooks đơn thuần có thể dẫn đến khó kiểm soát luồng dữ liệu.

#### e. Real-time Communication: WebSocket (STOMP)

Hệ thống tích hợp WebSocket thông qua STOMP protocol để hỗ trợ chat real-time giữa học viên và giảng viên. Sử dụng thư viện `@stomp/stompjs` và `sockjs-client` để kết nối với Spring Boot WebSocket backend.

**Ưu điểm:**
- **Real-time:** Cho phép gửi và nhận tin nhắn tức thì mà không cần polling, cải thiện trải nghiệm người dùng.
- **Hiệu quả:** Giảm tải server so với việc polling liên tục.

**Nhược điểm:**
- **Phức tạp hơn HTTP:** Cần xử lý kết nối, reconnect, và error handling cẩn thận.

#### f. Form Handling: React Hook Form và Zod

Tất cả các form trong hệ thống được xử lý bằng React Hook Form kết hợp với Zod để validation. Điều này đảm bảo dữ liệu đầu vào được kiểm tra chặt chẽ cả phía client và có thể validate lại phía server.

**Ưu điểm:**
- **Hiệu năng cao:** React Hook Form sử dụng uncontrolled components, giảm số lần re-render không cần thiết.
- **Type-safe validation:** Zod cung cấp schema validation với TypeScript, đảm bảo type safety từ đầu đến cuối.

**Nhược điểm:**
- **Học tập ban đầu:** Cần thời gian để làm quen với cách tích hợp Zod schema với React Hook Form.

#### g. Testing: Vitest và React Testing Library

Để đảm bảo chất lượng mã nguồn, dự án sử dụng Vitest (framework test unit tốc độ cao) kết hợp với React Testing Library để kiểm thử các component quan trọng như CourseCard và các service (authService).

**Ưu điểm:**
- **Tốc độ thực thi nhanh:** Vitest được tối ưu hóa cho Vite/Next.js, cho phản hồi kết quả test gần như tức thì.
- **Kiểm thử hành vi:** React Testing Library khuyến khích viết test dựa trên hành vi người dùng (User Behavior) thay vì chi tiết triển khai, giúp test bền vững hơn khi refactor code.

**Nhược điểm:**
- **Chi phí thời gian:** Việc viết test case đầy đủ đòi hỏi thời gian và công sức đáng kể trong giai đoạn phát triển ban đầu.

### 1.2.2. Back-end

#### a. Ngôn ngữ & Framework: Java 21 + Spring Boot 3.5.6

Hệ thống được xây dựng trên nền tảng Java 21 - phiên bản Long Term Support mới nhất với nhiều cải tiến về hiệu năng, kết hợp với Spring Boot 3.5.6. Đây là "xương sống" của hệ thống, cung cấp môi trường chạy ổn định, quản lý các dependency và cấu hình tự động cho toàn bộ ứng dụng.

**Ưu điểm:**
- **Hiệu năng cao & Ổn định:** Java 21 giúp xử lý hàng ngàn request đồng thời với tài nguyên thấp, tối ưu hóa cho các tác vụ I/O như gọi Database hay API thanh toán.
- **Hệ sinh thái khổng lồ:** Spring Boot cung cấp sẵn các module cho mọi nhu cầu: Web, Security, Data JPA, Mail, WebSocket, giúp giảm thiểu code thừa.
- **Kiểu dữ liệu chặt chẽ:** Java giúp phát hiện lỗi sai kiểu dữ liệu ngay lúc biên dịch, giảm thiểu lỗi runtime nguy hiểm trong các giao dịch tài chính.

**Nhược điểm:**
- **Khởi động chậm:** JVM và Spring Boot context cần thời gian khởi động lâu hơn so với NodeJS hay Go.
- **Tiêu tốn bộ nhớ:** Ứng dụng Java thường yêu cầu lượng RAM cơ sở cao hơn để vận hành JVM.

#### b. Cơ sở dữ liệu & ORM: MySQL 8.0 + Spring Data JPA

Dữ liệu hệ thống được lưu trữ trong MySQL 8.0 - hệ quản trị cơ sở dữ liệu quan hệ phổ biến nhất. Việc truy xuất dữ liệu được thực hiện thông qua Spring Data JPA, cho phép thao tác với DB thông qua các Java Object thay vì viết câu lệnh SQL thủ công.

**Ưu điểm:**
- **Tính toàn vẹn dữ liệu:** MySQL đảm bảo tính nhất quán tuyệt đối cho các giao dịch quan trọng như thanh toán và enrollment.
- **Năng suất phát triển:** JPA Repository tự động sinh các câu lệnh SQL cho các thao tác CRUD cơ bản, giúp developer tập trung vào logic nghiệp vụ.
- **Dễ dàng bảo trì:** Cấu trúc bảng được định nghĩa ngay trong code Java thông qua JPA Entities, giúp dễ dàng theo dõi sự thay đổi của Schema.

**Nhược điểm:**
- **Vấn đề hiệu năng:** Nếu không cấu hình FetchType cẩn thận, Hibernate có thể sinh ra hàng loạt câu lệnh SQL dư thừa (N+1 problem), làm chậm hệ thống. Dự án đã giải quyết vấn đề này bằng cách sử dụng `JOIN FETCH` trong các query phức tạp.
- **Phức tạp với truy vấn khó:** Với các báo cáo thống kê phức tạp, việc dùng JPQL/HQL đôi khi khó tối ưu bằng SQL thuần.

#### c. Bảo mật: Spring Security & JWT

Hệ thống sử dụng cơ chế xác thực Stateless. Khi người dùng đăng nhập, server cấp phát một JWT (JSON Web Token). Client đính kèm token này vào header của mỗi request để truy cập tài nguyên.

**Ưu điểm:**
- **Khả năng mở rộng:** Do server không cần lưu session state, hệ thống dễ dàng mở rộng ngang mà không lo vấn đề đồng bộ session.
- **Phân quyền linh hoạt:** Spring Security cho phép định nghĩa quyền truy cập chi tiết tới từng API endpoint dựa trên Role thông qua annotation `@PreAuthorize`, ví dụ: `@PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #id)")`.

**Nhược điểm:**
- **Thu hồi Token:** Vì JWT là stateless, việc thu hồi token ngay lập tức khó khăn hơn so với Session truyền thống. Tuy nhiên, điều này được giảm thiểu bằng cách đặt thời gian hết hạn token ngắn và yêu cầu refresh token.

#### d. Real-time Communication: Spring WebSocket (STOMP)

Hệ thống tích hợp Spring WebSocket với giao thức STOMP để hỗ trợ chat real-time giữa học viên và giảng viên. WebSocket cho phép gửi và nhận tin nhắn tức thì mà không cần polling.

**Ưu điểm:**
- **Hiệu quả:** Giảm tải server so với việc polling liên tục.
- **Real-time:** Cho phép gửi và nhận tin nhắn tức thì, cải thiện trải nghiệm người dùng.

**Nhược điểm:**
- **Quản lý kết nối:** Cần xử lý kết nối, ngắt kết nối, và reconnect một cách cẩn thận.

#### e. Tích hợp thanh toán: VNPay Integration

Module thanh toán được tích hợp với cổng VNPay, sử dụng cơ chế tạo URL thanh toán bảo mật với thuật toán mã hóa SHA512 để đảm bảo toàn vẹn dữ liệu giao dịch. Hệ thống hỗ trợ thanh toán đơn lẻ và thanh toán từ giỏ hàng.

**Ưu điểm:**
- **An toàn:** Mọi thông tin đơn hàng được "ký" bằng chuỗi bí mật (hash secret), ngăn chặn việc hacker sửa đổi số tiền thanh toán trên đường truyền.
- **Trải nghiệm người dùng:** Chuyển hướng mượt mà sang cổng ngân hàng nội địa, phù hợp với thói quen người dùng Việt Nam.
- **Tự động hóa:** Sau khi thanh toán thành công, hệ thống tự động tạo enrollment và xóa các khóa học khỏi giỏ hàng.

**Nhược điểm:**
- **Phụ thuộc bên thứ ba:** Quá trình kiểm thử phụ thuộc vào môi trường Sandbox của VNPay, đôi khi không ổn định.
- **Callback handling:** Cần xử lý cẩn thận callback từ VNPay để đảm bảo tính nhất quán dữ liệu.

#### f. Xử lý dữ liệu: Apache POI & OpenHTMLToPDF

Hệ thống sử dụng Apache POI để xử lý Import/Export dữ liệu bài học từ file Excel và OpenHTMLToPDF để tự động sinh chứng chỉ PDF cho học viên hoàn thành khóa học. Chứng chỉ được tạo với hỗ trợ đầy đủ UTF-8 và font tiếng Việt.

**Ưu điểm:**
- **Tự động hóa:** Giúp giảng viên tiết kiệm thời gian nhập liệu thủ công và tạo chứng chỉ chuyên nghiệp tức thì.
- **Chuẩn hóa dữ liệu:** Kiểm soát được định dạng dữ liệu đầu vào từ file Excel trước khi lưu vào Database.
- **Hỗ trợ Unicode:** OpenHTMLToPDF với font embedding (DejaVu Sans, Arial Unicode MS) đảm bảo chứng chỉ hiển thị đúng tiếng Việt.

**Nhược điểm:**
- **Tốn tài nguyên:** Việc xử lý các file Excel/PDF lớn có thể tiêu tốn nhiều RAM và CPU, cần xử lý cẩn thận để tránh lỗi OutOfMemoryError.
- **Cấu hình font:** Cần cấu hình cẩn thận để embed font Unicode vào PDF, đặc biệt là với tiếng Việt.

#### g. Email Service: Spring Mail

Hệ thống tích hợp Spring Mail để gửi email xác thực, đặt lại mật khẩu, và thông báo. Sử dụng SMTP server để gửi email tự động.

**Ưu điểm:**
- **Tự động hóa:** Gửi email tự động cho các sự kiện quan trọng như đăng ký, quên mật khẩu.
- **Tích hợp dễ dàng:** Spring Mail cung cấp abstraction layer, dễ dàng thay đổi SMTP provider.

**Nhược điểm:**
- **Phụ thuộc SMTP:** Cần cấu hình SMTP server, có thể phụ thuộc vào dịch vụ bên thứ ba như Gmail, SendGrid.

### 1.2.3. Trí tuệ nhân tạo (AI)

#### a. Framework & Ngôn ngữ: Python 3.8+ & FastAPI

Module Chatbot được phát triển bằng ngôn ngữ Python 3.8+, tận dụng hệ sinh thái thư viện AI phong phú. FastAPI được chọn làm framework chính để xây dựng các RESTful API, chạy trên giao diện ASGI, đảm nhiệm việc xử lý các request chat, xác thực JWT và giao tiếp với Java Backend.

**Ưu điểm:**
- **Hiệu năng cao:** FastAPI là một trong những framework Python nhanh nhất hiện nay nhờ cơ chế bất đồng bộ (async/await), giúp xử lý đồng thời nhiều request chat mà không bị chặn.
- **Tích hợp AI dễ dàng:** Python là ngôn ngữ bản địa của AI/ML, giúp việc tích hợp các thư viện như `google-generativeai`, `chromadb`, `sentence-transformers` trở nên mượt mà.
- **Tự động hóa tài liệu:** FastAPI tự động sinh document API (Swagger/OpenAPI), giúp đội Frontend dễ dàng tích hợp và kiểm thử.

**Nhược điểm:**
- **Global Interpreter Lock:** Python có cơ chế GIL giới hạn việc thực thi đa luồng trên CPU, tuy nhiên điều này được giảm thiểu nhờ cơ chế I/O bất đồng bộ của FastAPI khi gọi tới các dịch vụ bên ngoài (Gemini API).

#### b. Large Language Model (LLM): Google Gemini

Trái tim của Chatbot là mô hình ngôn ngữ lớn Google Gemini. Mô hình này chịu trách nhiệm hiểu ngôn ngữ tự nhiên tiếng Việt, tổng hợp thông tin từ ngữ cảnh được cung cấp và sinh ra câu trả lời thân thiện cho học viên.

**Ưu điểm:**
- **Tốc độ và Chi phí:** Phiên bản Flash có tốc độ phản hồi cực nhanh và chính sách free-tier hào phóng, rất phù hợp cho các dự án sinh viên hoặc startup giai đoạn đầu.
- **Khả năng hiểu ngữ cảnh:** Gemini xử lý tốt context window lớn, cho phép nạp nhiều thông tin về hồ sơ học viên và dữ liệu khóa học vào prompt để cá nhân hóa câu trả lời.
- **Hỗ trợ đa ngôn ngữ:** Khả năng xử lý tiếng Việt tự nhiên, không bị cứng nhắc như các mô hình đời cũ.

**Nhược điểm:**
- **Phụ thuộc bên thứ 3:** Hệ thống phụ thuộc vào API của Google. Nếu dịch vụ của Google bị gián đoạn hoặc thay đổi chính sách giá, hệ thống sẽ bị ảnh hưởng.
- **Rate Limiting:** Gói miễn phí có giới hạn số lượng request/phút, do đó hệ thống cần phải cài đặt cơ chế retry và hàng đợi để tránh lỗi 429.

#### c. RAG System: ChromaDB & Sentence Transformers

Để khắc phục điểm yếu "ảo giác" (hallucination) của LLM, hệ thống sử dụng kỹ thuật RAG (Retrieval-Augmented Generation). 
- **Vector Database:** Sử dụng ChromaDB để lưu trữ các vector kiến thức từ nội dung khóa học.
- **Embedding Model:** Sử dụng `paraphrase-multilingual-MiniLM-L12-v2` chạy local để chuyển đổi văn bản thành vector.

**Ưu điểm:**
- **Độ chính xác cao:** Chatbot trả lời dựa trên dữ liệu thực tế của hệ thống, không bịa đặt thông tin.
- **Hoạt động Offline/Local:** Việc tạo embedding chạy ngay trên server bằng Sentence Transformers, giúp tiết kiệm chi phí API và bảo mật dữ liệu nội bộ tốt hơn so với việc dùng OpenAI Embeddings.
- **Lưu trữ đơn giản:** ChromaDB hoạt động dạng embedded, không cần cài đặt server database phức tạp riêng biệt.

**Nhược điểm:**
- **Tài nguyên Server:** Việc chạy mô hình Embedding local sẽ tiêu tốn CPU/RAM của server hosting.
- **Đồng bộ dữ liệu:** Cần có cơ chế cập nhật lại ChromaDB mỗi khi nội dung khóa học trên Java Backend thay đổi để đảm bảo Chatbot luôn có thông tin mới nhất.

#### d. Cơ sở dữ liệu & Session: SQLite & SQLAlchemy

Để quản lý phiên làm việc và lịch sử chat, hệ thống sử dụng SQLite kết hợp với ORM SQLAlchemy. Bảng `chat_sessions` và `chat_messages` giúp lưu trữ đoạn hội thoại để làm ngữ cảnh cho các câu hỏi tiếp theo.

**Ưu điểm:**
- **Gọn nhẹ:** SQLite là file-based database, cực kỳ dễ triển khai và backup, phù hợp cho module microservice độc lập như Chatbot.
- **ORM mạnh mẽ:** SQLAlchemy giúp thao tác dữ liệu bằng đối tượng Python, tránh các lỗi SQL Injection và dễ dàng bảo trì code.

**Nhược điểm:**
- **Khả năng mở rộng:** SQLite chỉ phù hợp với quy mô vừa và nhỏ. Khi lượng chat concurrent tăng quá cao, SQLite có thể gặp vấn đề về khóa ghi.

#### e. Kỹ thuật Prompt Engineering & Cá nhân hóa

Hệ thống áp dụng kỹ thuật Prompt Engineering có cấu trúc, kết hợp dữ liệu từ Java Backend (thông tin học viên, tiến độ học tập, danh sách khóa học) để tạo ra ngữ cảnh trong prompt gửi cho Gemini.

**Ưu điểm:**
- **Trải nghiệm cá nhân hóa:** Chatbot không chỉ trả lời chung chung mà còn biết rõ người dùng là ai, đã học đến đâu để đưa ra lời khuyên phù hợp.
- **Kiểm soát đầu ra:** Các chỉ dẫn nghiêm ngặt trong prompt giúp định hướng giọng văn và giới hạn phạm vi trả lời, đảm bảo chatbot chỉ trả lời về nội dung liên quan đến khóa học.

**Nhược điểm:**
- **Giới hạn Context Window:** Cần khéo léo lựa chọn thông tin để không vượt quá giới hạn token của LLM, gây mất thông tin hoặc tăng độ trễ.

## 1.3. Kết luận chương

Thông qua những nghiên cứu về lý thuyết và khảo sát các hệ thống tương tự, chương 1 đã làm rõ tầm quan trọng của việc xây dựng một hệ thống học trực tuyến hiện đại. Dựa trên những kiến thức đã tìm hiểu, em quyết định xây dựng đồ án nhằm giải quyết các bài toán cốt lõi sau:

**Xây dựng nền tảng quản lý khóa học toàn diện:** Phục vụ nhu cầu giảng dạy và học tập trực tuyến, hỗ trợ đầy đủ quy trình từ đăng ký, học tập đến thi cử và cấp chứng chỉ. Hệ thống hỗ trợ nhiều loại nội dung: video, bài viết, quiz, assignment.

**Tích hợp hệ thống chat real-time:** Xây dựng hệ thống chat giữa học viên và giảng viên sử dụng WebSocket (STOMP), cho phép trao đổi tức thì và quản lý cuộc trò chuyện hiệu quả.

**Tích hợp AI Chatbot thông minh:** Xây dựng chatbot AI sử dụng Google Gemini với kỹ thuật RAG, giúp học viên nhận được hỗ trợ 24/7 với câu trả lời chính xác dựa trên nội dung khóa học thực tế.

**Mô hình kiến trúc hệ thống:** Hệ thống được xây dựng theo mô hình Client-Server với microservice architecture. Phía Client (Next.js) gửi yêu cầu và nhận phản hồi định dạng JSON từ Backend Server (Spring Boot) thông qua các RESTful API. Chatbot service (Python FastAPI) hoạt động độc lập và giao tiếp với Backend thông qua HTTP.

**Các tác nhân tham gia hệ thống:** Hệ thống phân quyền rõ ràng cho 03 tác nhân chính bao gồm:
- **Quản trị viên (Admin):** Quản lý toàn bộ hệ thống, phê duyệt khóa học, quản lý người dùng, xem thống kê và báo cáo.
- **Giảng viên (Instructor):** Tạo và quản lý khóa học, xem thống kê doanh thu và học viên, chat với học viên.
- **Học viên (Student):** Đăng ký và học khóa học, theo dõi tiến độ, nhận chứng chỉ, chat với giảng viên và sử dụng AI chatbot.

**Các công nghệ chủ đạo được lựa chọn:**
- **Phía Frontend:** Next.js 16 với React 19 và TypeScript, Tailwind CSS 4, shadcn/ui, TanStack Query, Zustand, WebSocket (STOMP).
- **Phía Backend:** Ngôn ngữ lập trình Java 21 kết hợp Framework Spring Boot 3.5.6 giúp xây dựng hệ thống ổn định và bảo mật.
- **Cơ sở dữ liệu:** Hệ quản trị cơ sở dữ liệu MySQL 8.0 đảm bảo tính toàn vẹn và hiệu suất truy xuất dữ liệu cao.
- **Bảo mật:** Sử dụng chuẩn xác thực JWT để quản lý phiên làm việc của người dùng, kết hợp Spring Security cho phân quyền chi tiết.
- **Dịch vụ bổ trợ:** 
  - Tích hợp thanh toán trực tuyến qua cổng VNPay với mã hóa SHA512.
  - Xuất file báo cáo bằng thư viện Apache POI.
  - Tạo chứng chỉ PDF tự động bằng OpenHTMLToPDF với hỗ trợ Unicode đầy đủ.
- **Trí tuệ nhân tạo:** Python FastAPI với Google Gemini AI, ChromaDB cho RAG system, Sentence Transformers cho embedding model.

Hệ thống được thiết kế với khả năng mở rộng cao, bảo mật tốt, và trải nghiệm người dùng tối ưu, sẵn sàng cho việc triển khai trong môi trường production.

