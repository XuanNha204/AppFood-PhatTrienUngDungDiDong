// Gọi model AI qua API chuẩn OpenAI (OmniRoute, OpenRouter, OpenAI... đều dùng được)
const BASE_URL = () => (process.env.AI_BASE_URL || 'http://localhost:20128/v1').replace(/\/+$/, '');
const TIMEOUT_MS = 60 * 1000;

function aiError(message, status = 503) {
  const err = new Error(message);
  err.status = status;
  return err;
}

async function chatCompletion(messages) {
  const headers = { 'Content-Type': 'application/json' };
  if (process.env.AI_API_KEY) headers.Authorization = `Bearer ${process.env.AI_API_KEY}`;

  let res;
  try {
    res = await fetch(`${BASE_URL()}/chat/completions`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        model: process.env.AI_MODEL || 'antigravity/gemini-3.5-flash-lite',
        messages,
        temperature: 0.7,
      }),
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });
  } catch (err) {
    console.error('✖ AI request failed:', err.message);
    if (err.name === 'TimeoutError') throw aiError('Trợ lý AI phản hồi quá lâu, vui lòng thử lại');
    throw aiError('Không kết nối được tới dịch vụ AI, kiểm tra AI_BASE_URL và OmniRoute đã chạy chưa');
  }

  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    const detail = (body.error && body.error.message) || res.statusText;
    console.error(`✖ AI error ${res.status}:`, detail);
    if (res.status === 401 || res.status === 403) throw aiError('AI_API_KEY trong file .env không đúng hoặc chưa điền');
    if (res.status === 429) throw aiError('Trợ lý AI đang quá tải, vui lòng thử lại sau ít phút');
    throw aiError('Trợ lý AI đang gặp sự cố, vui lòng thử lại sau');
  }

  const content = body.choices && body.choices[0] && body.choices[0].message && body.choices[0].message.content;
  if (!content || !content.trim()) throw aiError('Trợ lý AI không trả lời, vui lòng hỏi lại');
  return content.trim();
}

module.exports = { chatCompletion };
