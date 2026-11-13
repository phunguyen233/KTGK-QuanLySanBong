# HƯỚNG DẪN: Ứng dụng Quản lý sân bóng (Java + SQLite)

## 1. Cấu trúc project

```
app/src/main/java/com/example/ktgk13_11_26/
├── model/
│   ├── SanBong.java            ← lớp dữ liệu 1 sân (mã, tên, địa chỉ, giá, trạng thái)
│   └── DonDatSan.java          ← lớp dữ liệu 1 đơn đặt
├── data/
│   └── DatabaseHelper.java     ← TOÀN BỘ thao tác SQLite (tạo bảng, thêm, xóa, truy vấn)
├── adapter/
│   ├── SanBongAdapter.java     ← biến List<SanBong> thành các dòng trong ListView
│   └── DonDatSanAdapter.java   ← biến List<DonDatSan> thành các dòng trong ListView
├── BaseActivity.java           ← Activity cha: Toolbar + nút quay lại
├── Utils.java                  ← định dạng tiền / giờ / ngày, xóa lỗi khi gõ
├── MainActivity.java           ← Menu chính (3 nút)
├── DanhSachSanActivity.java    ← CN1: danh sách sân + xóa sân
├── ThemSanActivity.java        ← CN1: form thêm sân
├── DatSanActivity.java         ← CN2: đặt sân
├── DanhSachDonActivity.java    ← CN3: danh sách đơn
└── ChiTietDonActivity.java     ← CN3: chi tiết + hủy đơn

app/src/main/res/layout/        ← giao diện XML tương ứng từng màn hình
```

Kiến trúc 3 lớp đơn giản:

```
 Giao diện (layout XML)  ⇄  Activity (xử lý sự kiện, kiểm tra dữ liệu)  ⇄  DatabaseHelper (SQLite)
                                       ⇅
                                 Model (SanBong, DonDatSan)
```

Activity **không bao giờ** viết SQL trực tiếp — chỉ gọi hàm của `DatabaseHelper`.

---

## 2. Sơ đồ điều hướng màn hình (Intent)

```
                    ┌──────────────────┐
                    │   MainActivity   │
                    │  (menu 3 nút)    │
                    └──┬──────┬──────┬─┘
                       │      │      │
          ┌────────────┘      │      └──────────────┐
          ▼                   ▼                     ▼
 ┌──────────────────┐  ┌──────────────┐   ┌──────────────────────┐
 │DanhSachSanActivity│ │DatSanActivity│◀──│ DanhSachDonActivity  │
 │ - nút + ──────────┼▶ThemSanActivity│ + │  bấm 1 đơn           │
 │ - bấm 1 sân ──────┼─▶ (extra mã sân)│   │      │ (extra id đơn) │
 │ - nút thùng rác   │ └──────────────┘   │      ▼               │
 └──────────────────┘                     │ ChiTietDonActivity   │
                                          │  - nút Hủy đơn       │
                                          └──────────────────────┘
```

Truyền dữ liệu giữa màn hình bằng `Intent.putExtra`:

```java
// Gửi (DanhSachSanActivity)
Intent intent = new Intent(this, DatSanActivity.class);
intent.putExtra(DatSanActivity.EXTRA_MA_SAN, san.getMaSan());
startActivity(intent);

// Nhận (DatSanActivity.onCreate)
String maSan = getIntent().getStringExtra(EXTRA_MA_SAN);
```

Tương tự `ChiTietDonActivity` nhận `EXTRA_DON_ID` (id đơn) rồi tự đọc đơn từ database.
→ Chỉ truyền **khóa** (mã/id), không truyền cả object — màn hình nhận tự lấy dữ liệu mới nhất từ DB.

---

## 3. Chức năng 4 – Lưu trữ (SQLite) — làm đầu tiên vì mọi thứ dựa vào nó

### 3.1 Hai bảng

```sql
CREATE TABLE san_bong (
    ma_san     TEXT PRIMARY KEY,     -- S01, S02...
    ten_san    TEXT NOT NULL,
    dia_chi    TEXT NOT NULL,
    gia_thue   INTEGER NOT NULL,     -- VNĐ / giờ
    trang_thai INTEGER NOT NULL DEFAULT 0   -- 0 = Còn trống, 1 = Đã được đặt
);

CREATE TABLE don_dat (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    ma_san        TEXT NOT NULL,     -- khóa ngoại → san_bong
    ho_ten        TEXT NOT NULL,
    so_dien_thoai TEXT NOT NULL,
    ngay_thue     TEXT NOT NULL,     -- 'yyyy-MM-dd'
    gio_bat_dau   INTEGER NOT NULL,  -- số phút từ 00:00 (18:30 → 1110)
    so_gio        INTEGER NOT NULL,
    tong_tien     INTEGER NOT NULL
);
```

**Vì sao lưu giờ dạng số phút?** Để so sánh trùng lịch chỉ cần phép `<`, `>` trên số nguyên.
**Vì sao ngày dạng `yyyy-MM-dd`?** Chuỗi kiểu này sắp xếp theo alphabet = sắp xếp theo thời gian. Khi hiển thị mới đổi sang `dd/MM/yyyy` (`Utils.formatNgay`).

### 3.2 Vòng đời `SQLiteOpenHelper`

- `onCreate(db)`: chạy **1 lần duy nhất** khi file `quanlysanbong.db` chưa tồn tại → tạo bảng + chèn 3 sân mẫu.
- Những lần mở app sau: file DB đã có → bỏ qua `onCreate`, dữ liệu cũ còn nguyên ⇒ **đáp ứng yêu cầu tắt app mở lại vẫn còn dữ liệu**.
- `onUpgrade`: chạy khi tăng `DB_VERSION` (sửa cấu trúc bảng).
- `getInstance()` (singleton): cả app dùng chung 1 đối tượng helper.

> Muốn reset dữ liệu khi test: gỡ app hoặc *Settings → Apps → Clear storage*.

---

## 4. Chức năng 1 – Quản lý danh sách sân

### Luồng hiển thị
```
DanhSachSanActivity.onResume()
   └─▶ db.layDanhSachSan()           (SELECT * FROM san_bong)
          └─▶ List<SanBong>
                 └─▶ adapter.setData(list) → notifyDataSetChanged()
                        └─▶ ListView gọi adapter.getView() cho từng dòng
                               → gán text + màu trạng thái (xanh: Còn trống / đỏ: Đã được đặt)
```

**Tại sao tải dữ liệu trong `onResume()` chứ không phải `onCreate()`?**
`onResume` chạy mỗi khi màn hình hiện lại. Sau khi thêm sân / đặt sân rồi bấm quay lại, danh sách tự cập nhật mà không cần code thêm.

### Luồng thêm sân (`ThemSanActivity`)
1. Bấm nút **+** → mở `ThemSanActivity`.
2. Bấm **Lưu** → `luuSan()`:
   - Mã sân rỗng → báo lỗi; mã đã tồn tại (`tonTaiMaSan`, không phân biệt hoa/thường) → báo lỗi.
   - Tên, địa chỉ rỗng → báo lỗi.
   - Giá rỗng / không phải số / ≤ 0 → báo lỗi.
3. Hợp lệ → `db.themSan()` (INSERT) → Toast → `finish()` → quay lại danh sách → `onResume` tải lại.

Lỗi hiển thị ngay dưới ô nhập bằng `TextInputLayout.setError(...)`, tự mất khi người dùng gõ lại (`Utils.xoaLoiKhiNhap`).

### Luồng xóa sân
Bấm icon thùng rác → `xacNhanXoa(san)`:
- Nếu sân **đang có đơn đặt** → không cho xóa, yêu cầu hủy đơn trước (tránh đơn đặt "mồ côi" trỏ tới sân không tồn tại).
- Nếu không → hộp thoại xác nhận → `db.xoaSan()` → tải lại danh sách.

---

## 5. Chức năng 2 – Đặt sân (`DatSanActivity`) — phần quan trọng nhất

### Bước 1: Đổ sân vào Spinner
```java
dsSan = db.layDanhSachSan();
ArrayAdapter<SanBong> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dsSan);
```
Spinner hiển thị `SanBong.toString()` → `"S02 - Sân mini số 2 (Còn trống)"`.
Nếu mở từ danh sách sân (có `EXTRA_MA_SAN`) → tự `setSelection` đúng sân đó.

### Bước 2: Chọn ngày, giờ
- `DatePickerDialog` + `setMinDate(hôm nay)` → không chọn được ngày quá khứ. Lưu `ngayChon = "2026-11-14"`.
- `TimePickerDialog` (24h) → lưu `gioChon = giờ*60 + phút`.

### Bước 3: Tính tiền tự động
```
Tổng tiền = Giá thuê theo giờ × Số giờ thuê
```
`tinhTongTien()` được gọi mỗi khi:
- đổi sân trong Spinner (`OnItemSelectedListener`),
- gõ số giờ (`TextWatcher.afterTextChanged`).

→ Người dùng thấy tổng tiền thay đổi ngay khi gõ, không cần bấm nút.

### Bước 4: Bấm "Xác nhận đặt sân" → `luuDon()` kiểm tra theo thứ tự

| # | Kiểm tra | Thông báo |
|---|----------|-----------|
| 1 | Chưa có sân nào | Chưa chọn sân |
| 2 | Họ tên rỗng | Vui lòng nhập họ tên |
| 3 | SĐT rỗng / không khớp `^0\d{9}$` | SĐT phải 10 số, bắt đầu bằng 0 |
| 4 | Số giờ không phải số nguyên > 0 | Số giờ thuê phải > 0 |
| 5 | Chưa chọn ngày / giờ | Vui lòng chọn ... |
| 6 | Giờ kết thúc > 24:00 | Vượt quá 24:00 |
| 7 | Đặt hôm nay mà giờ bắt đầu đã qua | Giờ bắt đầu đã qua |
| 8 | **Trùng lịch** | Hộp thoại "Trùng lịch" |

### Bước 5: Thuật toán kiểm tra trùng lịch (`DatabaseHelper.biTrungLich`)

Hai khoảng thời gian `[A_bắtđầu, A_kếtthúc)` và `[B_bắtđầu, B_kếtthúc)` **giao nhau** khi và chỉ khi:

```
A_bắtđầu < B_kếtthúc   VÀ   B_bắtđầu < A_kếtthúc
```

Ví dụ đã có đơn 11:00–13:00 trên sân S02 ngày 14/11:

| Đơn mới | Kết quả |
|---------|---------|
| 12:00–13:00 | 660<780 và 720<780 → **TRÙNG** |
| 10:00–11:30 | **TRÙNG** |
| 10:00–14:00 (bao trùm) | **TRÙNG** |
| 13:00–14:00 | 780<780 sai → **không trùng** (nối tiếp được) |

SQL tương ứng:
```sql
SELECT COUNT(*) FROM don_dat
WHERE ma_san = ? AND ngay_thue = ?
  AND gio_bat_dau < CAST(? AS INTEGER)                    -- đơn cũ bắt đầu trước khi đơn mới kết thúc
  AND (gio_bat_dau + so_gio*60) > CAST(? AS INTEGER)      -- đơn cũ kết thúc sau khi đơn mới bắt đầu
```

> ⚠️ **Bẫy hay gặp:** `rawQuery` truyền mọi tham số `?` dưới dạng **chuỗi**. Cột `gio_bat_dau` tự ép kiểu được,
> nhưng biểu thức `(gio_bat_dau + so_gio*60)` thì **không** → SQLite so sánh số với chuỗi và luôn ra sai,
> khiến đặt trùng vẫn lọt. Vì vậy phải `CAST(? AS INTEGER)`. (Lỗi này đã gặp thật khi test và đã sửa.)

### Bước 6: Lưu
```
db.themDon(don)
   ├─ INSERT INTO don_dat ...
   └─ capNhatTrangThaiSan(maSan)   → sân chuyển "Đã được đặt"
→ Toast "Đặt sân thành công!" → finish()
```

---

## 6. Chức năng 3 – Quản lý đơn đặt sân

### Danh sách đơn (`DanhSachDonActivity`)
`db.layDanhSachDon()` dùng **JOIN** để lấy tên sân:
```sql
SELECT d.*, s.ten_san, s.dia_chi, s.gia_thue
FROM don_dat d LEFT JOIN san_bong s ON d.ma_san = s.ma_san
ORDER BY d.ngay_thue DESC, d.gio_bat_dau ASC
```
Mỗi dòng hiển thị: tên người đặt, tên sân, ngày, giờ bắt đầu, số giờ, tổng tiền.
Bấm 1 đơn → `Intent` + `EXTRA_DON_ID` → `ChiTietDonActivity`.

### Chi tiết & hủy đơn (`ChiTietDonActivity`)
1. Đọc id từ Intent → `db.layDon(id)` → hiển thị đầy đủ (thêm SĐT, địa chỉ, khung giờ bắt đầu–kết thúc, giá/giờ).
2. Bấm **Hủy đơn** → hộp thoại xác nhận → `db.huyDon(id)`:
```
huyDon(id)
   ├─ layDon(id)                   ← lấy mã sân TRƯỚC khi xóa
   ├─ DELETE FROM don_dat WHERE id = ?
   └─ capNhatTrangThaiSan(maSan)   ← sân không còn đơn nào → "Còn trống"
```
3. `finish()` → quay về danh sách đơn → `onResume` tải lại.

### Quy tắc trạng thái sân
```java
trạng thái = (số đơn của sân > 0) ? "Đã được đặt" : "Còn trống"
```
Được tính lại **mỗi lần thêm hoặc hủy đơn**, nên luôn khớp với dữ liệu thực.

---

## 7. Kịch bản demo khi nộp bài

1. Mở app → **Danh sách sân bóng**: 3 sân mẫu đều "Còn trống".
2. Bấm **+** → bấm Lưu khi để trống → thấy các lỗi. Nhập mã `S01` → báo "Mã sân đã tồn tại". Nhập `S04` hợp lệ → lưu.
3. Bấm vào **Sân mini số 2** → màn đặt sân tự chọn S02. Nhập tên, SĐT, chọn ngày mai, 18:00, 2 giờ → tổng tiền **500.000 đ** tự hiện → Xác nhận.
4. Quay lại danh sách sân → S02 chuyển **"Đã được đặt"** (màu đỏ).
5. Đặt lại S02 cùng ngày lúc 19:00, 1 giờ → hộp thoại **"Trùng lịch"**. Đổi sang 20:00 → đặt được.
6. **Tắt hẳn app** (vuốt khỏi đa nhiệm) → mở lại → dữ liệu vẫn còn.
7. **Quản lý đơn đặt sân** → bấm 1 đơn → xem chi tiết → **Hủy đơn**. Hủy hết đơn của S02 → S02 về **"Còn trống"**.
8. Thử xóa sân đang có đơn → bị chặn.

---

## 8. Câu hỏi giảng viên hay hỏi

- **Vì sao chọn SQLite?** Có 2 loại dữ liệu liên quan nhau (sân – đơn), cần truy vấn có điều kiện (trùng lịch, JOIN lấy tên sân). SharedPreferences chỉ hợp lưu cài đặt nhỏ dạng key–value; File thì phải tự đọc/ghi/parse toàn bộ.
- **Adapter làm gì?** Là cầu nối giữa dữ liệu (List) và View (ListView): ListView hỏi adapter "có bao nhiêu dòng" (`getCount`) và "vẽ dòng thứ i thế nào" (`getView`). `convertView` được tái sử dụng để tiết kiệm bộ nhớ.
- **`finish()` khác `startActivity(MainActivity)` thế nào?** `finish()` đóng màn hình hiện tại và quay về màn hình trước trong back stack; `startActivity` sẽ tạo thêm màn hình mới chồng lên.
- **Vì sao dùng `?` trong SQL thay vì cộng chuỗi?** Tránh lỗi SQL injection và lỗi khi dữ liệu có dấu nháy `'`.
