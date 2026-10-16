// Gói Firebase miễn phí (Spark) không có Cloud Storage, nên ảnh upload được lưu
// thành document trong collection "images" và phục vụ qua GET /images/:id
const { db } = require('../config/firebase');
const { now } = require('./firestore');

const PREFIX = '/images/';

async function saveImage(file, ownerId) {
  const ref = await db.collection('images').add({
    data: file.buffer,
    contentType: file.mimetype,
    size: file.size,
    owner: ownerId,
    createdAt: now(),
  });
  return PREFIX + ref.id;
}

// Chỉ xoá ảnh do server lưu, bỏ qua link ảnh bên ngoài
async function deleteImage(url) {
  if (!url || !url.startsWith(PREFIX)) return;
  await db.collection('images').doc(url.slice(PREFIX.length)).delete().catch(() => {});
}

// Ảnh mới cho món ăn / avatar: ưu tiên file upload, nếu không thì dùng link ảnh (imageUrl)
// Trả về undefined nếu không có ảnh mới, ném lỗi 400 nếu link không hợp lệ
async function resolveImage(req) {
  if (req.file) return saveImage(req.file, req.user.id);
  const link = typeof req.body.imageUrl === 'string' ? req.body.imageUrl.trim() : '';
  if (!link) return undefined;
  if (!/^https?:\/\/\S+$/i.test(link) || link.length > 2000) {
    const err = new Error('Link ảnh không hợp lệ (phải bắt đầu bằng http:// hoặc https://)');
    err.status = 400;
    throw err;
  }
  return link;
}

module.exports = { saveImage, deleteImage, resolveImage };
