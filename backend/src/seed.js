// Tạo dữ liệu mẫu trên Firestore: npm run seed
// Cảnh báo: xoá toàn bộ danh mục, món ăn, giỏ hàng, đơn hàng và ảnh hiện có
require('dotenv').config({ quiet: true });
const { db, auth } = require('./config/firebase');
const { normalize } = require('./utils/text');

const DEMO = {
  name: 'Người dùng Demo',
  email: 'demo@foodapp.com',
  password: '123456',
  phone: '0901234567',
  address: '10 Huỳnh Văn Nghệ, Bửu Long, Biên Hoà, Đồng Nai',
};

const IMG = (id) => `https://www.themealdb.com/images/media/meals/${id}.jpg`;
const CAT_IMG = (name) => `https://www.themealdb.com/images/category/${name}.png`;

const CATEGORIES = [
  { name: 'Món bò', description: 'Các món từ thịt bò', imageUrl: CAT_IMG('beef'), order: 1 },
  { name: 'Món gà', description: 'Gà rán, gà nướng, cà ri gà', imageUrl: CAT_IMG('chicken'), order: 2 },
  { name: 'Hải sản', description: 'Tôm, cá, mực tươi ngon', imageUrl: CAT_IMG('seafood'), order: 3 },
  { name: 'Mì & Pasta', description: 'Mì Ý, lasagna, mac & cheese', imageUrl: CAT_IMG('pasta'), order: 4 },
  { name: 'Món chay', description: 'Thanh đạm, tốt cho sức khoẻ', imageUrl: CAT_IMG('vegetarian'), order: 5 },
  { name: 'Bữa sáng', description: 'Khởi đầu ngày mới đầy năng lượng', imageUrl: CAT_IMG('breakfast'), order: 6 },
  { name: 'Tráng miệng', description: 'Bánh ngọt và món tráng miệng', imageUrl: CAT_IMG('dessert'), order: 7 },
];

// [tên, mô tả, giá, ảnh, rating, prepTime, calories, phổ biến]
const FOODS = {
  'Món bò': [
    ['Burger Bò Úc', 'Burger bò Úc nướng than, phô mai cheddar, rau xà lách và sốt đặc biệt.', 79000, IMG('44bzep1761848278'), 4.8, 15, 650, true],
    ['Bò Xào Bông Cải', 'Thịt bò mềm xào bông cải xanh giòn với sốt dầu hào đậm đà.', 69000, IMG('m0p0j81765568742'), 4.6, 15, 420, false],
    ['Bò Nướng Asado', 'Bò nướng kiểu Argentina, ướp thảo mộc, nướng chậm trên than hồng.', 159000, IMG('kgfh3q1763075438'), 4.9, 30, 780, true],
    ['Bánh Pie Bò Mù Tạt', 'Bánh nướng nhân bò hầm mù tạt, vỏ bánh giòn bơ thơm lừng.', 89000, IMG('sytuqu1511553755'), 4.5, 25, 690, false],
    ['Thịt Viên Sốt Cà', 'Thịt bò viên sốt cà chua cay nhẹ kiểu Algeria.', 65000, IMG('8rfd4q1764112993'), 4.4, 20, 510, false],
  ],
  'Món gà': [
    ['Burger Gà Halloumi', 'Burger gà nướng kèm phô mai Halloumi áp chảo, sẵn sàng trong 15 phút.', 75000, IMG('vdwloy1713225718'), 4.7, 15, 600, true],
    ['Gà Nướng Ayam Percik', 'Gà nướng sốt nước cốt dừa cay kiểu Malaysia.', 89000, IMG('020z181619788503'), 4.8, 25, 540, true],
    ['Gà Hầm Kiểu Jamaica', 'Gà hầm nâu đậm vị với ớt, hành và gia vị Caribbean.', 85000, IMG('sypxpx1515365095'), 4.5, 30, 560, false],
    ['Sandwich Gà Giòn', 'Gà phi lê chiên giòn, dưa leo muối, bánh mì bơ nướng.', 59000, IMG('sbx7n71587673021'), 4.6, 10, 580, false],
    ['Cơm Gà Chorizo', 'Cơm nấu niêu cùng gà và xúc xích chorizo cay thơm.', 72000, IMG('fk80jp1763280767'), 4.4, 25, 640, false],
  ],
  'Hải sản': [
    ['Cá Hồi Nướng Thì Là', 'Cá hồi phi lê nướng cùng thì là tây và cà chua bi.', 149000, IMG('1548772327'), 4.9, 20, 480, true],
    ['Cơm Tôm Mực', 'Cơm chiên kiểu Tây Ban Nha với tôm và mực tươi.', 119000, IMG('jc6oub1763196663'), 4.7, 25, 620, false],
    ['Salad Tôm Bang Bang', 'Tôm chiên giòn trộn sốt bang bang cay ngọt và rau tươi.', 95000, IMG('4xcfai1763765676'), 4.6, 15, 390, false],
    ['Cà Ri Cá Amok', 'Cà ri cá hấp lá chuối kiểu Campuchia, thơm nước cốt dừa.', 99000, IMG('diuub11782687570'), 4.5, 30, 450, false],
    ['Lẩu Tôm Moqueca', 'Tôm hầm sữa dừa, ớt chuông và rau mùi kiểu Brazil.', 129000, IMG('e2kcut1782591669'), 4.6, 30, 520, false],
  ],
  'Mì & Pasta': [
    ['Mì Ý Tôm Cay', 'Linguine xào tôm, ớt, tỏi và dầu ô liu.', 99000, IMG('usywpp1511189717'), 4.7, 20, 610, true],
    ['Fettuccine Alfredo', 'Mì fettuccine sốt kem phô mai Parmesan béo ngậy.', 89000, IMG('0jv5gx1661040802'), 4.8, 20, 720, false],
    ['Lasagne Bò Bằm', 'Lasagne nhiều lớp với sốt bò bằm và phô mai mozzarella.', 109000, IMG('wtsvxx1511296896'), 4.9, 35, 780, true],
    ['Sandwich Mac & Cheese', 'Bánh mì nướng kẹp nui phô mai tan chảy.', 55000, IMG('xutquv1505330523'), 4.3, 10, 650, false],
  ],
  'Món chay': [
    ['Khoai Tây Bravas', 'Khoai tây chiên nồi chiên không dầu với sốt cà chua cay.', 45000, IMG('3m8yae1763257951'), 4.5, 20, 320, false],
    ['Cà Tím Nướng Hummus', 'Cà tím nướng kèm sốt hummus đậu gà béo bùi.', 59000, IMG('zub3s91764110535'), 4.4, 20, 300, false],
    ['Salad Couscous Cà Tím', 'Couscous trộn cà tím nướng, rau thơm và chanh.', 55000, IMG('02s6gc1763799560'), 4.3, 15, 350, false],
    ['Cà Tím Nướng Bharta', 'Cà tím nướng nghiền với gia vị Ấn Độ, ăn kèm bánh naan.', 62000, IMG('urtpqw1487341253'), 4.6, 25, 280, true],
  ],
  'Bữa sáng': [
    ['Bữa Sáng Kiểu Anh', 'Trứng ốp, xúc xích, thịt xông khói, đậu sốt cà và bánh mì nướng.', 99000, IMG('utxryw1511721587'), 4.8, 15, 850, true],
    ['Trứng Cuộn Bánh Mì', 'Bánh mì kẹp trứng chiên kiểu Ấn Độ, nhanh gọn đầy đủ.', 35000, IMG('hqaejl1695738653'), 4.4, 10, 380, false],
    ['Khoai Tây Bữa Sáng', 'Khoai tây áp chảo với hành, ớt chuông và thảo mộc.', 39000, IMG('1550441882'), 4.3, 15, 310, false],
    ['Bánh Poffertjes', 'Bánh pancake mini Hà Lan rắc đường bột và bơ.', 45000, IMG('oaqz9f1766593912'), 4.7, 15, 420, false],
  ],
  'Tráng miệng': [
    ['Crumble Táo Mâm Xôi', 'Táo và mâm xôi nướng phủ lớp vụn bơ giòn, ăn kèm kem.', 49000, IMG('xvsurr1511719182'), 4.8, 10, 410, true],
    ['Bánh Alfajores', 'Bánh quy kẹp dulce de leche, phủ dừa nạo.', 35000, IMG('a4kgf21763075288'), 4.6, 5, 280, false],
    ['Bánh Táo Nướng', 'Bánh táo truyền thống, vỏ giòn nhân táo quế thơm.', 45000, IMG('stnxzp1784835840'), 4.7, 10, 390, false],
    ['Bánh Apam Balik', 'Bánh kếp Malaysia nhân đậu phộng, đường và bắp.', 29000, IMG('adxcbq1619787919'), 4.5, 10, 330, false],
    ['Tart Táo Frangipane', 'Tart hạnh nhân frangipane với táo mỏng nướng vàng.', 55000, IMG('wxywrq1468235067'), 4.6, 10, 360, false],
  ],
};

async function clearCollection(name) {
  const snap = await db.collection(name).get();
  for (let i = 0; i < snap.docs.length; i += 400) {
    const batch = db.batch();
    snap.docs.slice(i, i + 400).forEach((d) => batch.delete(d.ref));
    await batch.commit();
  }
  return snap.size;
}

// Tạo (hoặc đặt lại) tài khoản demo trên Firebase Authentication + hồ sơ trên Firestore
async function seedDemoUser() {
  let record;
  try {
    record = await auth.getUserByEmail(DEMO.email);
    await auth.updateUser(record.uid, { password: DEMO.password, displayName: DEMO.name });
  } catch (err) {
    if (err.code !== 'auth/user-not-found') throw err;
    record = await auth.createUser({ email: DEMO.email, password: DEMO.password, displayName: DEMO.name });
  }
  const at = new Date().toISOString();
  await db.collection('users').doc(record.uid).set({
    name: DEMO.name,
    email: DEMO.email,
    phone: DEMO.phone,
    address: DEMO.address,
    avatarUrl: '',
    favorites: [],
    createdAt: at,
    updatedAt: at,
  });
}

(async () => {
  console.log('… Đang xoá dữ liệu cũ');
  for (const name of ['categories', 'foods', 'carts', 'orders', 'images']) await clearCollection(name);

  const batch = db.batch();
  const catId = new Map();
  const base = Date.now();
  CATEGORIES.forEach((c) => {
    const ref = db.collection('categories').doc();
    catId.set(c.name, ref.id);
    batch.set(ref, { ...c, createdAt: new Date(base).toISOString(), updatedAt: new Date(base).toISOString() });
  });

  let count = 0;
  for (const [catName, foods] of Object.entries(FOODS)) {
    for (const [name, description, price, imageUrl, rating, prepTime, calories, isPopular] of foods) {
      // Lệch thời gian tạo vài phút để sắp xếp "Mới nhất" có thứ tự rõ ràng
      const at = new Date(base - count * 60 * 1000).toISOString();
      batch.set(db.collection('foods').doc(), {
        name,
        description,
        price,
        imageUrl,
        category: catId.get(catName),
        rating,
        ratingCount: 20 + Math.floor(Math.random() * 300),
        soldCount: Math.floor(Math.random() * 500),
        prepTime,
        calories,
        isPopular,
        createdBy: null,
        searchText: normalize(`${name} ${description}`),
        createdAt: at,
        updatedAt: at,
      });
      count += 1;
    }
  }
  await batch.commit();
  await seedDemoUser();

  console.log(`✔ Đã tạo ${CATEGORIES.length} danh mục, ${count} món ăn trên Firestore`);
  console.log(`✔ Tài khoản demo: ${DEMO.email} / ${DEMO.password}`);
  process.exit(0);
})().catch((err) => {
  console.error('✖', err.message);
  process.exit(1);
});
