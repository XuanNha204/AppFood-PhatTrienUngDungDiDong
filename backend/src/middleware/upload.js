const multer = require('multer');

// Một document Firestore tối đa 1 MiB nên giới hạn ảnh ở 900KB (app đã tự nén ảnh trước khi gửi)
const MAX_IMAGE_BYTES = 900 * 1024;

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_IMAGE_BYTES },
  fileFilter: (req, file, cb) => {
    if (file.mimetype && file.mimetype.startsWith('image/')) return cb(null, true);
    cb(new multer.MulterError('LIMIT_UNEXPECTED_FILE', 'Chỉ chấp nhận file hình ảnh'));
  },
});

module.exports = { upload };
