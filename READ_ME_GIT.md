# 🚀 Hướng Dẫn Git Cơ Bản Cho Newbie (Từ Branch Đến Pull Request)

Tài liệu này hướng dẫn quy trình làm việc chuẩn (Git Flow) khi làm việc nhóm trong dự án. Hãy đọc kỹ và tuân thủ các bước dưới đây để tránh đè code hoặc làm hỏng nhánh chính (`main`/`develop`).

---

## 📌 Quy Trình 5 Bước Làm Việc Hàng Ngày

### Bước 1: Cập nhật code mới nhất về máy
Trước khi bắt đầu làm bất kỳ tính năng hay sửa lỗi nào, luôn đảm bảo local code của bạn là mới nhất.

1. Chuyển về branch chính (thường là `main` hoặc `develop` tùy quy định dự án):
   ```bash
   git checkout main
   ```
2. Tải code mới nhất từ GitHub/GitLab về:
   ```bash
   git pull origin main
   ```

---

### Bước 2: Tạo Branch (nhánh) mới để làm việc
> ⚠️ **Nguyên tắc vàng:** Không bao giờ code trực tiếp trên branch `main` hoặc `develop`.

Tạo và chuyển sang branch mới. Tên branch nên phản ánh tính năng hoặc lỗi bạn đang xử lý (ví dụ: `feature/login-page`, `fix/header-bug`):

```bash
# Tạo nhánh mới và chuyển sang nhánh đó ngay lập tức
git checkout -b feature/ten-tinh-nang-cua-ban
```

---

### Bước 3: Code và Lưu lại kết quả (Commit)
Sau khi bạn đã hoàn thành một phần việc hoặc sửa xong file:

1. **Kiểm tra** trạng thái các file đã thay đổi:
   ```bash
   git status
   ```
2. **Thêm** các file vào khu vực chuẩn bị commit (Staging Area):
   ```bash
   # Thêm tất cả file đã sửa/tạo mới
   git add .
   
   # Hoặc chỉ thêm file cụ thể
   git add path/to/file.ext
   ```
3. **Lưu vết (Commit)** kèm thông điệp (message) rõ ràng mô tả việc bạn vừa làm:
   ```bash
   git commit -m "feat: thêm giao diện trang đăng nhập"
   ```

---

### Bước 4: Đẩy Branch lên GitHub (Git Push)
Lần đầu tiên đẩy branch mới này lên remote repository:

```bash
git push -u origin feature/ten-tinh-nang-cua-ban
```
*(Từ các lần push tiếp theo trên cùng branch này, bạn chỉ cần gõ `git push`).*

---

### Bước 5: Tạo PR (Pull Request) để Review & Merge Code

1. Truy cập vào trang dự án trên **GitHub**.
2. Bạn sẽ thấy thông báo màu vàng gợi ý: **"Compare & pull request"** $\rightarrow$ Bấm vào nút đó.
3. **Điền thông tin PR:**
   * **Title:** Tóm tắt công việc đã làm (ví dụ: `[Feat] Thêm giao diện Login`).
   * **Description:** Mô tả chi tiết các thay đổi, kèm hình ảnh minh họa nếu có.
4. Gán **Reviewers**: Chọn leader hoặc đồng nghiệp trong dự án để họ kiểm tra code.
5. Nhấn **Create pull request**.

> 🎉 **Hoàn thành!** Công việc của bạn đã xong. Bây giờ chỉ cần chờ Reviewer kiểm tra, đóng góp ý kiến (nếu có) và Merge code vào nhánh chính.

---

## 🛠️ Tóm Tắt Nhanh Các Lệnh Git Thông Dụng

| Lệnh | Công dụng |
| :--- | :--- |
| `git status` | Xem trạng thái các file (file nào vừa sửa, chưa add,...) |
| `git branch` | Xem danh sách các branch ở máy local |
| `git checkout <tên-branch>` | Chuyển sang một branch đã có |
| `git checkout -b <tên-branch>` | Tạo branch mới và chuyển sang ngay |
| `git pull` | Cập nhật code mới nhất từ server về máy |
| `git push` | Đẩy commit ở máy local lên server |
| `git log` | Xem lịch sử các commit đã thực hiện |