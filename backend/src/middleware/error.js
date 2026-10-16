const multer = require('multer');

function notFound(req, res) {
  res.status(404).json({ message: `Không tìm thấy: ${req.method} ${req.originalUrl}` });
}

// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  if (err instanceof multer.MulterError) {
    const message = err.code === 'LIMIT_FILE_SIZE' ? 'Ảnh quá lớn (tối đa 900KB)' : err.field || err.message;
    return res.status(400).json({ message });
  }
  if (err.type === 'entity.parse.failed') {
    return res.status(400).json({ message: 'Dữ liệu gửi lên không đúng định dạng JSON' });
  }
  if (err.status && err.status < 500) return res.status(err.status).json({ message: err.message });
  console.error(err);
  res.status(err.status || 500).json({ message: err.message || 'Lỗi máy chủ' });
}

module.exports = { notFound, errorHandler };
