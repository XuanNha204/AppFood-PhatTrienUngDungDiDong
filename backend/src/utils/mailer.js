const nodemailer = require('nodemailer');

const isConfigured = () => Boolean(process.env.SMTP_USER && process.env.SMTP_PASS);

let transporter;
function getTransporter() {
  if (!transporter) {
    transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || 'smtp.gmail.com',
      port: Number(process.env.SMTP_PORT || 465),
      secure: Number(process.env.SMTP_PORT || 465) === 465,
      auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS },
    });
  }
  return transporter;
}

const escapeHtml = (str = '') =>
  String(str).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

async function sendOtpMail(to, name, otp) {
  if (!isConfigured()) {
    console.log(`[MAIL-DEV] OTP đặt lại mật khẩu cho ${to}: ${otp}`);
    return false;
  }
  const mail = {
    from: process.env.MAIL_FROM || `Food App <${process.env.SMTP_USER}>`,
    to,
    subject: 'Mã xác nhận đặt lại mật khẩu - Food App',
    html: `
      <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto">
        <h2 style="color:#FF6B35">Food App</h2>
        <p>Xin chào <b>${escapeHtml(name)}</b>,</p>
        <p>Mã xác nhận đặt lại mật khẩu của bạn là:</p>
        <p style="font-size:32px;font-weight:bold;letter-spacing:8px;color:#FF6B35">${otp}</p>
        <p>Mã có hiệu lực trong <b>10 phút</b>. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
      </div>`,
  };
  try {
    await getTransporter().sendMail(mail);
  } catch (err) {
    console.error('✖ Gửi email thất bại:', err.message);
    const error = new Error('Không gửi được email, vui lòng thử lại sau');
    error.status = 503;
    throw error;
  }
  return true;
}

module.exports = { sendOtpMail, isConfigured };
