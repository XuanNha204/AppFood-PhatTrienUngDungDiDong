# Food App — Android Java + Node.js + Firebase

Ứng dụng đặt món ăn gồm 2 phần:

| Thư mục | Công nghệ |
|---|---|
| `backend/` | Node.js, Express, Firebase (Firestore + Authentication), JWT, Multer (upload ảnh), Nodemailer (OTP) |
| `android/` | Android **Java**, Retrofit, Glide, Material 3, ViewBinding, Shimmer |

```
Android App (Java) ──HTTP/JSON + JWT──► Express API ──► Firebase Authentication (email + mật khẩu)
                                              └──► Cloud Firestore (dữ liệu + ảnh upload)
```

App Android không kết nối thẳng tới Firebase mà đi qua backend: file khoá `serviceAccountKey.json`
chỉ nằm trên máy chủ, người dùng không lấy được, và giữ đúng yêu cầu "xác thực bằng JWT API".

### Dữ liệu trên Firestore
| Collection | Nội dung |
|---|---|
| `users/{uid}` | Hồ sơ (họ tên, SĐT, địa chỉ, avatar, món yêu thích). `uid` trùng với tài khoản trong Authentication |
| `categories` | Danh mục món ăn |
| `foods` | Món ăn (`category` = id danh mục, `createdBy` = uid người đăng) |
| `carts/{uid}` | Giỏ hàng của từng người |
| `orders` | Đơn hàng |
| `images` | Ảnh upload (gói Spark miễn phí không có Cloud Storage nên ảnh được lưu thẳng vào Firestore, tối đa ~900KB/ảnh, app tự nén trước khi gửi) |

## 1. Chạy backend

Yêu cầu: Node.js 18+ và một project Firebase đã bật **Firestore Database** và **Authentication → Email/Password**.

1. Firebase Console → Project settings → Service accounts → **Generate new private key**,
   lưu file thành `backend/serviceAccountKey.json` (không chia sẻ file này).
2. Tạo file `.env` từ `.env.example`, điền `FIREBASE_API_KEY` (Project settings → General → Web API key)
   và `JWT_SECRET`.

```bash
cd backend
npm install
npm run seed              # tạo 7 danh mục, 32 món ăn và tài khoản demo trên Firebase (xoá dữ liệu cũ)
npm start                 # hoặc: npm run dev (tự restart khi sửa code)
```

> Firestore ở "test mode" sẽ khoá truy cập sau 30 ngày, nhưng backend dùng Admin SDK nên không bị ảnh hưởng.

Tài khoản demo: **demo@foodapp.com / 123456**

Khi chạy, server in ra địa chỉ cho emulator (`http://10.0.2.2:3000/`) và cho điện thoại thật (IP LAN).

### Gửi OTP quên mật khẩu qua email (không bắt buộc)
Điền `SMTP_USER` và `SMTP_PASS` (Gmail App Password) trong `.env`.
Nếu để trống thì backend chạy **chế độ dev**: OTP được in ra console và app tự hiển thị mã OTP để demo.

## 2. Chạy app Android

1. Mở thư mục `android/` bằng Android Studio.
2. Sửa địa chỉ API trong `android/gradle.properties` nếu cần:
   - Emulator: `API_BASE_URL=http://10.0.2.2:3000/`
   - Điện thoại thật (cùng Wi-Fi với máy tính): `API_BASE_URL=http://192.168.x.x:3000/`
3. Bấm Run. Có thể build bằng dòng lệnh: `./gradlew assembleDebug`.

> Nếu dùng điện thoại thật mà không kết nối được, hãy cho phép cổng 3000 qua Windows Firewall.

## 3. Chức năng

**Xác thực (Firebase Authentication + JWT):** đăng ký, đăng nhập, đăng xuất, quên mật khẩu (OTP 6 số qua email, đếm ngược gửi lại), đổi mật khẩu, tự đăng xuất khi token hết hạn.

**Màn hình:**
- Trang chủ: lời chào, ô tìm kiếm, banner khuyến mãi tự chạy, danh mục, món phổ biến, gợi ý
- Danh mục food (lưới, kèm số lượng món)
- Food list: tìm kiếm không dấu ("pho mai" tìm được "phô mai"), sắp xếp (bán chạy, đánh giá, giá, mới nhất)
- Food detail: ảnh thu gọn khi cuộn, chọn số lượng, thêm vào giỏ, yêu thích, sửa/xoá (với món do mình đăng)
- Thêm/sửa món: **upload ảnh food** (chụp camera, chọn từ thư viện hoặc dán link ảnh trên Internet; tự nén và xoay ảnh đúng chiều)
- Giỏ hàng: tăng/giảm số lượng, vuốt để xoá (có hoàn tác), thanh tiến trình miễn phí giao hàng
- Đặt hàng: địa chỉ, SĐT, ghi chú, phương thức thanh toán, hộp thoại đặt hàng thành công
- Lịch sử đơn hàng, chi tiết đơn, huỷ đơn khi đang chờ xác nhận
- User profile: **upload avatar** (hoặc dán link ảnh), sửa thông tin, món ăn đã đăng, chế độ tối (Dark mode)
- Món yêu thích

**Hiệu ứng UI/UX:** splash animation, shared element transition (ảnh món "bay" từ danh sách sang chi tiết), shimmer loading, animation cho danh sách, badge số lượng trên icon giỏ hàng, nút "nảy" khi bấm, kéo để làm mới, trạng thái rỗng/lỗi có nút thử lại, chuyển màn hình bằng hiệu ứng trượt.

## 4. API

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/register` | Đăng ký |
| POST | `/api/auth/login` | Đăng nhập → `{ token, user }` |
| POST | `/api/auth/forgot-password` | Gửi OTP |
| POST | `/api/auth/reset-password` | Đặt lại mật khẩu bằng OTP |
| POST | `/api/auth/change-password` 🔒 | Đổi mật khẩu |
| GET/PUT | `/api/users/me` 🔒 | Xem / sửa hồ sơ |
| POST | `/api/users/me/avatar` 🔒 | Upload avatar (multipart `image`) hoặc JSON `{ imageUrl }` |
| GET | `/api/users/me/favorites` 🔒 | Món yêu thích |
| POST | `/api/users/me/favorites/:foodId` 🔒 | Bật/tắt yêu thích |
| GET | `/api/banners` | Banner trang chủ |
| GET | `/api/categories` | Danh mục (kèm `foodCount`) |
| GET | `/api/foods?category=&search=&sort=&popular=` | Danh sách món |
| GET | `/api/foods/mine` 🔒 | Món do tôi đăng |
| GET | `/api/foods/:id` | Chi tiết món |
| POST/PUT/DELETE | `/api/foods[/:id]` 🔒 | Thêm / sửa (multipart, ảnh qua `image` hoặc `imageUrl`) / xoá món |
| GET | `/images/:id` | Ảnh đã upload (lưu trong Firestore) |
| GET/DELETE | `/api/cart` 🔒 | Xem / xoá toàn bộ giỏ hàng |
| POST | `/api/cart/items` 🔒 | Thêm món `{ foodId, quantity }` |
| PUT/DELETE | `/api/cart/items/:foodId` 🔒 | Sửa số lượng / xoá món |
| POST/GET | `/api/orders` 🔒 | Đặt hàng / lịch sử |
| GET | `/api/orders/:id` 🔒 | Chi tiết đơn |
| PUT | `/api/orders/:id/cancel` 🔒 | Huỷ đơn |

🔒 = cần header `Authorization: Bearer <token>`

## Chatbot AI (Trợ lý Food AI)

Mở bằng nút **"Trợ lý AI"** ở trang chủ. Trợ lý biết toàn bộ thực đơn, giỏ hàng hiện tại và mốc miễn phí giao hàng,
trả lời bằng tiếng Việt; món được gợi ý hiện thành thẻ (bấm để xem chi tiết, nút + để thêm vào giỏ).
Lịch sử chat lưu trên Firestore (`chats/{uid}`), có nút xoá cuộc trò chuyện.

App không gọi AI trực tiếp mà qua backend (`POST /api/chat`), nên API key chỉ nằm trên máy chủ.
Cấu hình trong `backend/.env` (dùng được mọi dịch vụ có API chuẩn OpenAI):

```
AI_BASE_URL=http://localhost:20128/v1
AI_MODEL=antigravity/gemini-3.5-flash-lite
AI_API_KEY=            # dán key OmniRoute vào đây rồi khởi động lại server
```

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/chat` 🔒 | Lịch sử chat |
| POST | `/api/chat` 🔒 | Gửi `{ message }` → `{ role, content, foods: [Food] }` |
| DELETE | `/api/chat` 🔒 | Xoá lịch sử |

## 5. Cấu trúc code Android

```
com.example.foodapp
├── api/        ApiService (Retrofit), ApiClient, AuthInterceptor (gắn JWT), ApiCallback
├── data/       SessionManager (lưu token), AppState (badge giỏ hàng, yêu thích), FoodActions
├── model/      User, Food, Category, Cart, Order, ...
├── util/       Format (tiền, ngày), ImageUtils (Glide), ImageUploadHelper (nén ảnh), Validator
└── ui/
    ├── SplashActivity
    ├── auth/      Login, Register, ForgotPassword
    ├── main/      MainActivity (BottomNavigation)
    ├── home/      HomeFragment, BannerAdapter
    ├── category/  CategoriesActivity
    ├── food/      FoodList, FoodDetail, AddEditFood
    ├── favorite/  FavoritesFragment
    ├── cart/      CartFragment, CheckoutActivity
    ├── order/     Orders, OrderDetail
    ├── profile/   ProfileFragment, EditProfile, ChangePassword
    ├── chat/      ChatActivity, ChatAdapter (trợ lý AI)
    └── common/    BaseActivity, FoodAdapter, CategoryAdapter, ImagePickerHelper, EmptyState
```
