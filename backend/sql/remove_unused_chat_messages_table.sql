-- Script để xóa bảng chat_messages không sử dụng trong MySQL
-- Bảng này ban đầu được thiết kế cho chatbot nhưng đã chuyển sang SQLite riêng
-- Chạy script này để dọn dẹp database

-- Xóa foreign key constraint trước
ALTER TABLE `chat_messages` DROP FOREIGN KEY IF EXISTS `FK6f0y4l43ihmgfswkgy9yrtjkh`;

-- Xóa bảng
DROP TABLE IF EXISTS `chat_messages`;

-- Xác nhận đã xóa
SELECT 'Bảng chat_messages đã được xóa thành công!' AS message;

