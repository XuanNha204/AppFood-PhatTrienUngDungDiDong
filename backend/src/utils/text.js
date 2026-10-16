// Bỏ dấu tiếng Việt để tìm kiếm không phân biệt dấu: "Phở Bò" -> "pho bo"
function normalize(str = '') {
  return str
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[đĐ]/g, 'd')
    .toLowerCase()
    .trim();
}

function escapeRegex(str = '') {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

module.exports = { normalize, escapeRegex };
