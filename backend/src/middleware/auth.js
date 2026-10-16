const jwt = require('jsonwebtoken');
const { getUser } = require('../data/users');

function signToken(userId) {
  return jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
  });
}

// Bắt buộc có header: Authorization: Bearer <token>
async function protect(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ message: 'Bạn chưa đăng nhập' });

  let payload;
  try {
    payload = jwt.verify(token, process.env.JWT_SECRET);
  } catch {
    return res.status(401).json({ message: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại' });
  }
  const user = await getUser(payload.id);
  if (!user) return res.status(401).json({ message: 'Tài khoản không tồn tại' });
  req.user = user;
  next();
}

module.exports = { protect, signToken };
