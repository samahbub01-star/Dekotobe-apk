/**
 * Dekotube - Complete Single-File Video Streaming Platform
 * 
 * Features:
 * 1. User Signup & Login System with Token-based Authentication
 * 2. Admin Panel with User Stats, Video Stats, and Platform Rules Management
 * 3. Video and Short-Video (Shorts) Upload Section using Multer
 * 4. View Tracking & Creator Monetization Earnings Calculator
 * Modern YouTube-inspired Dark-Mode UI served as a single-page full-stack app.
 */

const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

// Setup upload directory
const UPLOAD_DIR = path.join(__dirname, 'uploads');
if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

// Multer storage configuration
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, UPLOAD_DIR);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const ext = path.extname(file.originalname).toLowerCase();
    cb(null, `${file.fieldname}-${uniqueSuffix}${ext}`);
  }
});

const upload = multer({
  storage: storage,
  limits: { fileSize: 150 * 1024 * 1024 }, // 150MB limit
  fileFilter: (req, file, cb) => {
    const allowedVideo = /\.(mp4|webm|ogg|mov|mkv)$/i;
    const allowedImage = /\.(jpg|jpeg|png|webp|gif)$/i;
    if (file.fieldname === 'video' && allowedVideo.test(path.extname(file.originalname))) {
      return cb(null, true);
    }
    if (file.fieldname === 'thumbnail' && allowedImage.test(path.extname(file.originalname))) {
      return cb(null, true);
    }
    cb(null, true); // Permissive for prototype demonstration
  }
});

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static(UPLOAD_DIR));

// ==========================================
// IN-MEMORY DATA STORE & INITIAL DEMO DATA
// ==========================================

const users = [
  {
    id: 'user-admin-1',
    username: 'admin',
    email: 'admin@dekotube.io',
    channelName: 'Dekotube Official',
    passwordHash: hashPassword('admin123'),
    role: 'admin',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    createdAt: new Date('2026-01-01').toISOString(),
    subscribers: 142000
  },
  {
    id: 'user-creator-1',
    username: 'alex_tech',
    email: 'alex@techreview.com',
    channelName: 'Alex Tech & Design',
    passwordHash: hashPassword('creator123'),
    role: 'creator',
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    createdAt: new Date('2026-02-15').toISOString(),
    subscribers: 58300
  },
  {
    id: 'user-creator-2',
    username: 'sarah_code',
    email: 'sarah@codelab.dev',
    channelName: 'Sarah Codes',
    passwordHash: hashPassword('creator123'),
    role: 'creator',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    createdAt: new Date('2026-03-01').toISOString(),
    subscribers: 89100
  }
];

const sessions = new Map(); // token -> userId

const platformRules = [
  {
    id: 'rule-1',
    title: 'Copyright & Intellectual Property',
    category: 'Copyright',
    description: 'Only upload videos that you created yourself or have authorization to use. Fair use must be respected.',
    severity: 'High',
    active: true,
    updatedAt: '2026-06-10'
  },
  {
    id: 'rule-2',
    title: 'Community Safety & Respect',
    category: 'Safety',
    description: 'Hate speech, harassment, cyberbullying, and dangerous activities are strictly prohibited across videos and shorts.',
    severity: 'Critical',
    active: true,
    updatedAt: '2026-05-18'
  },
  {
    id: 'rule-3',
    title: 'Shorts Content Guidelines',
    category: 'Format',
    description: 'Shorts must be vertical format (9:16 aspect ratio) with duration of 60 seconds or less. Re-uploaded low-effort clips may be down-ranked.',
    severity: 'Medium',
    active: true,
    updatedAt: '2026-07-22'
  },
  {
    id: 'rule-4',
    title: 'Monetization & Ad-Friendly Standards',
    category: 'Monetization',
    description: 'Creators must comply with advertiser-friendly content guidelines to qualify for AdSense revenue share. Inappropriate language in the first 15 seconds disqualifies monetization.',
    severity: 'High',
    active: true,
    updatedAt: '2026-08-01'
  },
  {
    id: 'rule-5',
    title: 'Spam, Scams & Deceptive Practices',
    category: 'Integrity',
    description: 'Misleading metadata, clickbait thumbnails, automated view bots, and affiliate scheme promotions without disclosure are banned.',
    severity: 'Critical',
    active: true,
    updatedAt: '2026-08-14'
  }
];

const videos = [
  {
    id: 'vid-1',
    title: 'Next-Gen Full Stack Development with Node.js & Express 2026',
    description: 'Complete masterclass on modern microservices, full-stack video streaming architecture, and scalable media handling.',
    uploaderId: 'user-creator-1',
    uploaderName: 'Alex Tech & Design',
    uploaderAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=80',
    duration: '14:28',
    views: 184520,
    likes: 12400,
    isShort: false,
    category: 'Technology',
    tags: ['coding', 'javascript', 'express', 'fullstack'],
    monetized: true,
    createdAt: new Date('2026-08-10').toISOString()
  },
  {
    id: 'vid-2',
    title: 'Designing High-Conversion Dark UI with Modern CSS',
    description: 'Deep dive into YouTube-inspired dark palettes, micro-interactions, responsive grids, and design tokens.',
    uploaderId: 'user-creator-2',
    uploaderName: 'Sarah Codes',
    uploaderAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=80',
    duration: '21:05',
    views: 342100,
    likes: 24800,
    isShort: false,
    category: 'Design',
    tags: ['ui', 'css', 'design', 'web'],
    monetized: true,
    createdAt: new Date('2026-08-18').toISOString()
  },
  {
    id: 'vid-3',
    title: 'Dekotube Platform Update: New Creator Monetization Engine',
    description: 'Welcome to Dekotube! Learn about our 55/45 revenue share model, shorts monetization pool, and community rules.',
    uploaderId: 'user-admin-1',
    uploaderName: 'Dekotube Official',
    uploaderAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80',
    duration: '08:42',
    views: 521900,
    likes: 41200,
    isShort: false,
    category: 'News',
    tags: ['dekotube', 'monetization', 'creators'],
    monetized: true,
    createdAt: new Date('2026-08-25').toISOString()
  },
  // Shorts
  {
    id: 'short-1',
    title: 'Top 3 VS Code Tips You Did Not Know! ⚡ #shorts #coding',
    description: 'Fast productivity boosters for programmers in 30 seconds.',
    uploaderId: 'user-creator-2',
    uploaderName: 'Sarah Codes',
    uploaderAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop&q=80',
    duration: '0:34',
    views: 892400,
    likes: 93200,
    isShort: true,
    category: 'Technology',
    tags: ['shorts', 'vscode', 'developer'],
    monetized: true,
    createdAt: new Date('2026-08-28').toISOString()
  },
  {
    id: 'short-2',
    title: 'How CSS Flexbox Actually Centers Items 🎯 #shorts',
    description: 'Simple visual explanation of justify-content and align-items.',
    uploaderId: 'user-creator-1',
    uploaderName: 'Alex Tech & Design',
    uploaderAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=500&auto=format&fit=crop&q=80',
    duration: '0:45',
    views: 1420900,
    likes: 154000,
    isShort: true,
    category: 'Design',
    tags: ['shorts', 'css', 'frontend'],
    monetized: true,
    createdAt: new Date('2026-08-30').toISOString()
  }
];

const comments = [
  {
    id: 'c-1',
    videoId: 'vid-1',
    author: 'Alex Tech & Design',
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    text: 'Thanks for watching everyone! Let me know in the comments what stack you are building with this year.',
    likes: 342,
    createdAt: '2 hours ago'
  },
  {
    id: 'c-2',
    videoId: 'vid-1',
    author: 'Sarah Codes',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    text: 'The explanation on HTTP 206 partial streaming range headers was super crisp!',
    likes: 89,
    createdAt: '1 hour ago'
  }
];

// Helper Functions
function hashPassword(password) {
  return crypto.createHash('sha256').update(password + 'dekotube-salt-2026').digest('hex');
}

function authenticate(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    req.user = null;
    return next();
  }
  const token = authHeader.replace('Bearer ', '').trim();
  const userId = sessions.get(token);
  if (userId) {
    req.user = users.find(u => u.id === userId) || null;
  } else {
    req.user = null;
  }
  next();
}

function requireAuth(req, res, next) {
  if (!req.user) {
    return res.status(401).json({ error: 'Authentication required. Please sign in.' });
  }
  next();
}

function requireAdmin(req, res, next) {
  if (!req.user || req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Access denied: Administrator privileges required.' });
  }
  next();
}

// ==========================================
// API ROUTES
// ==========================================

// 1. AUTHENTICATION ROUTES
app.post('/api/auth/signup', (req, res) => {
  const { username, email, password, channelName } = req.body;
  if (!username || !email || !password) {
    return res.status(400).json({ error: 'Username, email, and password are required' });
  }
  if (users.some(u => u.email.toLowerCase() === email.toLowerCase())) {
    return res.status(409).json({ error: 'An account with this email already exists' });
  }
  if (users.some(u => u.username.toLowerCase() === username.toLowerCase())) {
    return res.status(409).json({ error: 'Username is already taken' });
  }

  const newUser = {
    id: 'user-' + Date.now(),
    username: username.trim(),
    email: email.trim().toLowerCase(),
    channelName: channelName ? channelName.trim() : `${username}'s Channel`,
    passwordHash: hashPassword(password),
    role: 'creator',
    avatar: `https://api.dicebear.com/7.x/identicon/svg?seed=${username}`,
    createdAt: new Date().toISOString(),
    subscribers: 0
  };

  users.push(newUser);
  const token = crypto.randomBytes(32).toString('hex');
  sessions.set(token, newUser.id);

  res.status(201).json({
    message: 'User registered successfully',
    token,
    user: {
      id: newUser.id,
      username: newUser.username,
      email: newUser.email,
      channelName: newUser.channelName,
      role: newUser.role,
      avatar: newUser.avatar,
      subscribers: newUser.subscribers
    }
  });
});

app.post('/api/auth/login', (req, res) => {
  const { emailOrUsername, password } = req.body;
  if (!emailOrUsername || !password) {
    return res.status(400).json({ error: 'Email/username and password are required' });
  }

  const user = users.find(u =>
    u.email.toLowerCase() === emailOrUsername.toLowerCase() ||
    u.username.toLowerCase() === emailOrUsername.toLowerCase()
  );

  if (!user || user.passwordHash !== hashPassword(password)) {
    return res.status(401).json({ error: 'Invalid email/username or password' });
  }

  const token = crypto.randomBytes(32).toString('hex');
  sessions.set(token, user.id);

  res.json({
    message: 'Login successful',
    token,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      channelName: user.channelName,
      role: user.role,
      avatar: user.avatar,
      subscribers: user.subscribers
    }
  });
});

app.get('/api/auth/me', authenticate, (req, res) => {
  if (!req.user) {
    return res.status(401).json({ user: null });
  }
  res.json({
    user: {
      id: req.user.id,
      username: req.user.username,
      email: req.user.email,
      channelName: req.user.channelName,
      role: req.user.role,
      avatar: req.user.avatar,
      subscribers: req.user.subscribers
    }
  });
});

// 2. VIDEO STREAMING & VIEW TRACKING ROUTES
app.get('/api/videos', (req, res) => {
  const { type, category, search } = req.query;
  let filtered = [...videos];

  if (type === 'shorts') {
    filtered = filtered.filter(v => v.isShort === true);
  } else if (type === 'videos') {
    filtered = filtered.filter(v => v.isShort !== true);
  }

  if (category && category !== 'All') {
    filtered = filtered.filter(v => v.category && v.category.toLowerCase() === category.toLowerCase());
  }

  if (search) {
    const q = search.toLowerCase();
    filtered = filtered.filter(v =>
      v.title.toLowerCase().includes(q) ||
      v.description.toLowerCase().includes(q) ||
      (v.tags && v.tags.some(t => t.toLowerCase().includes(q)))
    );
  }

  res.json({ videos: filtered });
});

app.get('/api/videos/:id', (req, res) => {
  const video = videos.find(v => v.id === req.params.id);
  if (!video) return res.status(404).json({ error: 'Video not found' });
  const videoComments = comments.filter(c => c.videoId === video.id);
  res.json({ video, comments: videoComments });
});

// View tracking with real-time increment
app.post('/api/videos/:id/view', (req, res) => {
  const video = videos.find(v => v.id === req.params.id);
  if (!video) return res.status(404).json({ error: 'Video not found' });

  video.views = (video.views || 0) + 1;

  // Calculate estimated earnings for this video based on RPM
  const rpm = video.isShort ? 0.12 : 3.50; // $0.12 for shorts, $3.50 for standard
  const estimatedRevenue = ((video.views / 1000) * rpm * 0.55).toFixed(2);

  res.json({
    success: true,
    views: video.views,
    estimatedRevenue: `$${estimatedRevenue}`
  });
});

app.post('/api/videos/:id/like', authenticate, requireAuth, (req, res) => {
  const video = videos.find(v => v.id === req.params.id);
  if (!video) return res.status(404).json({ error: 'Video not found' });
  video.likes = (video.likes || 0) + 1;
  res.json({ likes: video.likes });
});

app.post('/api/videos/:id/comments', authenticate, requireAuth, (req, res) => {
  const video = videos.find(v => v.id === req.params.id);
  if (!video) return res.status(404).json({ error: 'Video not found' });
  const { text } = req.body;
  if (!text || !text.trim()) return res.status(400).json({ error: 'Comment text cannot be empty' });

  const newComment = {
    id: 'c-' + Date.now(),
    videoId: video.id,
    author: req.user.channelName || req.user.username,
    avatar: req.user.avatar,
    text: text.trim(),
    likes: 0,
    createdAt: 'Just now'
  };

  comments.unshift(newComment);
  res.status(201).json({ comment: newComment });
});

// HTTP 206 Partial Content Range Streaming for Uploaded Videos
app.get('/api/stream/:filename', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.filename);
  if (!fs.existsSync(filePath)) {
    return res.status(404).send('Media file not found');
  }

  const stat = fs.statSync(filePath);
  const fileSize = stat.size;
  const range = req.headers.range;

  if (range) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
    const chunksize = (end - start) + 1;
    const file = fs.createReadStream(filePath, { start, end });
    const head = {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': 'video/mp4',
    };
    res.writeHead(206, head);
    file.pipe(res);
  } else {
    const head = {
      'Content-Length': fileSize,
      'Content-Type': 'video/mp4',
    };
    res.writeHead(200, head);
    fs.createReadStream(filePath).pipe(res);
  }
});

// 3. VIDEO & SHORTS UPLOAD ROUTE WITH MULTER
const uploadFields = upload.fields([
  { name: 'video', maxCount: 1 },
  { name: 'thumbnail', maxCount: 1 }
]);

app.post('/api/upload', authenticate, requireAuth, uploadFields, (req, res) => {
  const { title, description, category, tags, isShort } = req.body;
  if (!title || !title.trim()) {
    return res.status(400).json({ error: 'Video title is required' });
  }

  let videoUrl = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';
  let thumbnailUrl = 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80';

  if (req.files && req.files.video && req.files.video[0]) {
    videoUrl = `/api/stream/${req.files.video[0].filename}`;
  }

  if (req.files && req.files.thumbnail && req.files.thumbnail[0]) {
    thumbnailUrl = `/uploads/${req.files.thumbnail[0].filename}`;
  } else if (isShort === 'true' || isShort === true) {
    thumbnailUrl = 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop&q=80';
  }

  const parsedTags = tags ? (Array.isArray(tags) ? tags : tags.split(',').map(t => t.trim()).filter(Boolean)) : ['dekotube'];
  const isShortBool = isShort === 'true' || isShort === true;

  const newVideo = {
    id: (isShortBool ? 'short-' : 'vid-') + Date.now(),
    title: title.trim(),
    description: description ? description.trim() : '',
    uploaderId: req.user.id,
    uploaderName: req.user.channelName || req.user.username,
    uploaderAvatar: req.user.avatar,
    videoUrl,
    thumbnailUrl,
    duration: isShortBool ? '0:42' : '10:15',
    views: 1,
    likes: 1,
    isShort: isShortBool,
    category: category || 'Entertainment',
    tags: parsedTags,
    monetized: true,
    createdAt: new Date().toISOString()
  };

  videos.unshift(newVideo);

  res.status(201).json({
    message: isShortBool ? 'Short uploaded successfully!' : 'Video uploaded successfully!',
    video: newVideo
  });
});

// 4. ADMIN PANEL ROUTES
app.get('/api/admin/overview', authenticate, requireAdmin, (req, res) => {
  const totalUsers = users.length;
  const totalVideos = videos.filter(v => !v.isShort).length;
  const totalShorts = videos.filter(v => v.isShort).length;
  const totalViews = videos.reduce((sum, v) => sum + (v.views || 0), 0);
  const totalLikes = videos.reduce((sum, v) => sum + (v.likes || 0), 0);

  // Monetization metrics (platform cut: 45% on long-form, 55% on shorts pool)
  const longFormViews = videos.filter(v => !v.isShort).reduce((s, v) => s + (v.views || 0), 0);
  const shortViews = videos.filter(v => v.isShort).reduce((s, v) => s + (v.views || 0), 0);
  const estimatedPlatformRevenue = (
    (longFormViews / 1000 * 3.50 * 0.45) +
    (shortViews / 1000 * 0.12 * 0.55)
  ).toFixed(2);

  res.json({
    stats: {
      totalUsers,
      totalVideos,
      totalShorts,
      totalViews,
      totalLikes,
      estimatedPlatformRevenue: `$${Number(estimatedPlatformRevenue).toLocaleString()}`,
      activeRulesCount: platformRules.filter(r => r.active).length
    },
    users: users.map(u => ({
      id: u.id,
      username: u.username,
      email: u.email,
      channelName: u.channelName,
      role: u.role,
      subscribers: u.subscribers,
      createdAt: u.createdAt
    })),
    rules: platformRules,
    recentVideos: videos.slice(0, 10)
  });
});

// Admin Platform Rules Management
app.post('/api/admin/rules', authenticate, requireAdmin, (req, res) => {
  const { title, category, description, severity } = req.body;
  if (!title || !description) {
    return res.status(400).json({ error: 'Title and description are required' });
  }

  const newRule = {
    id: 'rule-' + Date.now(),
    title: title.trim(),
    category: category || 'General',
    description: description.trim(),
    severity: severity || 'Medium',
    active: true,
    updatedAt: new Date().toISOString().split('T')[0]
  };

  platformRules.push(newRule);
  res.status(201).json({ message: 'Platform rule added successfully', rule: newRule });
});

app.patch('/api/admin/rules/:id/toggle', authenticate, requireAdmin, (req, res) => {
  const rule = platformRules.find(r => r.id === req.params.id);
  if (!rule) return res.status(404).json({ error: 'Rule not found' });
  rule.active = !rule.active;
  rule.updatedAt = new Date().toISOString().split('T')[0];
  res.json({ message: 'Rule status updated', rule });
});

app.delete('/api/admin/rules/:id', authenticate, requireAdmin, (req, res) => {
  const idx = platformRules.findIndex(r => r.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Rule not found' });
  platformRules.splice(idx, 1);
  res.json({ message: 'Rule deleted successfully' });
});

app.delete('/api/admin/videos/:id', authenticate, requireAdmin, (req, res) => {
  const idx = videos.findIndex(v => v.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Video not found' });
  videos.splice(idx, 1);
  res.json({ message: 'Video removed by administrator' });
});

// 5. MONETIZATION EARNINGS CALCULATOR API
app.post('/api/monetization/calculate', (req, res) => {
  const {
    monthlyViews = 100000,
    longFormRpm = 3.50,
    shortsViews = 500000,
    shortsRpm = 0.12,
    subscribers = 10000,
    membershipPercent = 1.0,
    membershipFee = 4.99
  } = req.body;

  // YouTube Partner Program breakdown
  const longFormGross = (monthlyViews / 1000) * Number(longFormRpm);
  const longFormCreatorRevenue = longFormGross * 0.55; // 55% creator share

  const shortsGross = (shortsViews / 1000) * Number(shortsRpm);
  const shortsCreatorRevenue = shortsGross * 0.45; // 45% creator pool share

  const payingMembers = Math.round((subscribers * (Number(membershipPercent) / 100)));
  const membershipGross = payingMembers * Number(membershipFee);
  const membershipCreatorRevenue = membershipGross * 0.70; // 70% creator share

  const totalMonthlyEarnings = longFormCreatorRevenue + shortsCreatorRevenue + membershipCreatorRevenue;
  const totalYearlyEarnings = totalMonthlyEarnings * 12;

  // Thresholds: $100 minimum payout threshold
  const payoutEligible = totalMonthlyEarnings >= 100;
  const progressToPayout = Math.min(100, (totalMonthlyEarnings / 100) * 100);

  res.json({
    summary: {
      totalMonthlyEarnings: Number(totalMonthlyEarnings.toFixed(2)),
      totalYearlyEarnings: Number(totalYearlyEarnings.toFixed(2)),
      payoutEligible,
      progressToPayout: Math.round(progressToPayout)
    },
    breakdown: {
      longForm: {
        views: monthlyViews,
        rpm: longFormRpm,
        creatorEarnings: Number(longFormCreatorRevenue.toFixed(2))
      },
      shorts: {
        views: shortsViews,
        rpm: shortsRpm,
        creatorEarnings: Number(shortsCreatorRevenue.toFixed(2))
      },
      memberships: {
        payingMembers,
        creatorEarnings: Number(membershipCreatorRevenue.toFixed(2))
      }
    }
  });
});

// ==========================================
// FULL-STACK CLIENT: EMBEDDED DARK YOUTUBE UI
// ==========================================

app.get('*', (req, res) => {
  res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>dekotube - Video Streaming Platform</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --yt-bg: #0f0f0f;
      --yt-surface: #1f1f1f;
      --yt-card: #212121;
      --yt-border: #303030;
      --yt-hover: #272727;
      --yt-red: #ff0000;
      --yt-red-hover: #cc0000;
      --yt-text: #f1f1f1;
      --yt-text-secondary: #aaaaaa;
      --yt-chip-bg: #272727;
      --yt-chip-active: #ffffff;
      --yt-chip-active-text: #0f0f0f;
      --yt-blue: #3ea6ff;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }
    body {
      background-color: var(--yt-bg);
      color: var(--yt-text);
      font-family: 'Roboto', -apple-system, BlinkMacSystemFont, sans-serif;
      overflow-x: hidden;
    }
    /* HEADER */
    .header {
      position: sticky;
      top: 0;
      z-index: 100;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 16px;
      height: 56px;
      background-color: var(--yt-bg);
      border-bottom: 1px solid var(--yt-border);
    }
    .brand-box {
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
    }
    .brand-logo {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 700;
      font-size: 20px;
      letter-spacing: -0.5px;
    }
    .brand-logo svg {
      color: var(--yt-red);
    }
    .search-box {
      display: flex;
      align-items: center;
      flex: 0 1 600px;
      margin: 0 20px;
    }
    .search-input {
      width: 100%;
      height: 40px;
      padding: 0 16px;
      background-color: #121212;
      border: 1px solid var(--yt-border);
      border-right: none;
      border-radius: 40px 0 0 40px;
      color: var(--yt-text);
      font-size: 15px;
      outline: none;
    }
    .search-input:focus {
      border-color: var(--yt-blue);
    }
    .search-btn {
      width: 64px;
      height: 40px;
      background-color: var(--yt-surface);
      border: 1px solid var(--yt-border);
      border-radius: 0 40px 40px 0;
      color: var(--yt-text);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .search-btn:hover {
      background-color: var(--yt-hover);
    }
    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: background-color 0.2s;
    }
    .btn-create {
      background-color: var(--yt-surface);
      color: var(--yt-text);
    }
    .btn-create:hover {
      background-color: var(--yt-hover);
    }
    .btn-primary {
      background-color: var(--yt-red);
      color: #fff;
    }
    .btn-primary:hover {
      background-color: var(--yt-red-hover);
    }
    .btn-outline {
      background: transparent;
      border: 1px solid var(--yt-blue);
      color: var(--yt-blue);
    }
    .btn-outline:hover {
      background: rgba(62, 166, 255, 0.1);
    }
    .avatar-btn {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: none;
      cursor: pointer;
      overflow: hidden;
      background-color: #333;
    }
    .avatar-btn img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    /* MAIN APP LAYOUT */
    .app-container {
      display: flex;
      min-height: calc(100vh - 56px);
    }
    /* SIDEBAR */
    .sidebar {
      width: 240px;
      flex-shrink: 0;
      background-color: var(--yt-bg);
      border-right: 1px solid var(--yt-border);
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 10px 14px;
      border-radius: 10px;
      color: var(--yt-text);
      text-decoration: none;
      font-size: 14px;
      font-weight: 400;
      cursor: pointer;
      transition: background 0.15s;
    }
    .nav-item:hover {
      background-color: var(--yt-hover);
    }
    .nav-item.active {
      background-color: var(--yt-surface);
      font-weight: 500;
    }
    .nav-divider {
      height: 1px;
      background-color: var(--yt-border);
      margin: 8px 0;
    }
    .nav-section-title {
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--yt-text-secondary);
      padding: 8px 14px 4px;
      font-weight: 500;
    }

    /* MAIN CONTENT VIEW */
    .main-view {
      flex: 1;
      padding: 16px 24px 60px;
      overflow-y: auto;
      max-width: 1600px;
    }

    /* CHIPS BAR */
    .chips-bar {
      display: flex;
      gap: 12px;
      overflow-x: auto;
      padding-bottom: 16px;
      scrollbar-width: none;
    }
    .chips-bar::-webkit-scrollbar { display: none; }
    .chip {
      padding: 6px 14px;
      background-color: var(--yt-chip-bg);
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      white-space: nowrap;
      border: none;
      color: var(--yt-text);
    }
    .chip.active {
      background-color: var(--yt-chip-active);
      color: var(--yt-chip-active-text);
    }

    /* VIDEOS GRID */
    .section-title {
      font-size: 20px;
      font-weight: 700;
      margin: 16px 0 16px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .video-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px 16px;
    }
    .video-card {
      cursor: pointer;
      display: flex;
      flex-direction: column;
      gap: 12px;
      transition: transform 0.15s;
    }
    .video-card:hover .video-thumbnail img {
      transform: scale(1.02);
    }
    .video-thumbnail {
      position: relative;
      width: 100%;
      padding-top: 56.25%; /* 16:9 */
      border-radius: 12px;
      overflow: hidden;
      background-color: #2a2a2a;
    }
    .video-thumbnail img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.2s;
    }
    .duration-pill {
      position: absolute;
      bottom: 8px;
      right: 8px;
      background-color: rgba(0, 0, 0, 0.85);
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 4px;
      font-weight: 500;
    }
    .video-info {
      display: flex;
      gap: 12px;
    }
    .channel-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      overflow: hidden;
      flex-shrink: 0;
      background-color: #333;
    }
    .channel-avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .video-details {
      display: flex;
      flex-direction: column;
      gap: 4px;
      overflow: hidden;
    }
    .video-title {
      font-size: 15px;
      font-weight: 600;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .channel-name, .video-meta {
      font-size: 13px;
      color: var(--yt-text-secondary);
    }

    /* SHORTS SECTION */
    .shorts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
      gap: 16px;
      margin-bottom: 32px;
    }
    .short-card {
      cursor: pointer;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .short-thumbnail {
      position: relative;
      width: 100%;
      padding-top: 177.7%; /* 9:16 */
      border-radius: 14px;
      overflow: hidden;
      background-color: #202020;
    }
    .short-thumbnail img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.2s;
    }
    .short-card:hover .short-thumbnail img {
      transform: scale(1.03);
    }
    .short-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      background: rgba(255, 0, 0, 0.85);
      font-size: 11px;
      padding: 3px 8px;
      border-radius: 6px;
      font-weight: 700;
      letter-spacing: 0.5px;
    }
    .short-title {
      font-size: 14px;
      font-weight: 600;
      line-height: 1.25;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .short-views {
      font-size: 12px;
      color: var(--yt-text-secondary);
    }

    /* WATCH VIEW MODAL */
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0,0,0,0.85);
      display: none;
      justify-content: center;
      align-items: center;
      z-index: 1000;
      padding: 20px;
      overflow-y: auto;
    }
    .modal-overlay.open {
      display: flex;
    }
    .watch-container {
      width: 100%;
      max-width: 1100px;
      background-color: #121212;
      border-radius: 16px;
      overflow: hidden;
      border: 1px solid var(--yt-border);
      display: flex;
      flex-direction: column;
    }
    .video-player-box {
      width: 100%;
      background: #000;
      position: relative;
    }
    .video-player-box video {
      width: 100%;
      max-height: 560px;
      display: block;
    }
    .watch-content {
      padding: 20px;
    }
    .watch-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 12px;
    }
    .watch-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--yt-border);
    }
    .watch-channel {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .watch-actions {
      display: flex;
      gap: 10px;
    }
    .watch-desc-box {
      background-color: var(--yt-surface);
      border-radius: 12px;
      padding: 14px;
      margin-top: 16px;
      font-size: 14px;
      line-height: 1.4;
    }
    .comments-section {
      margin-top: 24px;
    }
    .comment-input-box {
      display: flex;
      gap: 12px;
      margin: 16px 0;
    }
    .comment-input {
      flex: 1;
      background: transparent;
      border: none;
      border-bottom: 1px solid var(--yt-border);
      color: #fff;
      font-size: 14px;
      padding: 8px 0;
      outline: none;
    }
    .comment-input:focus {
      border-bottom-color: var(--yt-blue);
    }

    /* ADMIN PANEL VIEW */
    .admin-card-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .stat-card {
      background-color: var(--yt-surface);
      border: 1px solid var(--yt-border);
      border-radius: 12px;
      padding: 18px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .stat-title {
      font-size: 13px;
      color: var(--yt-text-secondary);
      text-transform: uppercase;
      font-weight: 600;
    }
    .stat-value {
      font-size: 26px;
      font-weight: 700;
      color: #fff;
    }
    .rules-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 12px;
      background-color: var(--yt-surface);
      border-radius: 12px;
      overflow: hidden;
    }
    .rules-table th, .rules-table td {
      padding: 12px 16px;
      text-align: left;
      border-bottom: 1px solid var(--yt-border);
      font-size: 14px;
    }
    .rules-table th {
      background-color: #1a1a1a;
      color: var(--yt-text-secondary);
      font-weight: 500;
    }
    .badge {
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
      display: inline-block;
    }
    .badge-critical { background: rgba(255, 0, 0, 0.2); color: #ff5555; }
    .badge-high { background: rgba(255, 140, 0, 0.2); color: #ffa726; }
    .badge-medium { background: rgba(62, 166, 255, 0.2); color: #3ea6ff; }
    .badge-active { background: rgba(76, 175, 80, 0.2); color: #66bb6a; }
    .badge-inactive { background: rgba(150, 150, 150, 0.2); color: #aaa; }

    /* MONETIZATION CALCULATOR */
    .calc-container {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
      background-color: var(--yt-surface);
      border-radius: 16px;
      border: 1px solid var(--yt-border);
      padding: 24px;
    }
    @media (max-width: 900px) {
      .calc-container { grid-template-columns: 1fr; }
    }
    .calc-controls {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .calc-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .calc-label {
      display: flex;
      justify-content: space-between;
      font-size: 14px;
      font-weight: 500;
    }
    .calc-slider {
      width: 100%;
      accent-color: var(--yt-red);
      height: 6px;
    }
    .calc-results {
      background-color: #151515;
      border-radius: 12px;
      padding: 24px;
      border: 1px solid var(--yt-border);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .earnings-big {
      font-size: 42px;
      font-weight: 700;
      color: #4caf50;
      margin: 8px 0;
    }
    .payout-bar {
      height: 10px;
      background-color: #333;
      border-radius: 5px;
      overflow: hidden;
      margin: 8px 0;
    }
    .payout-fill {
      height: 100%;
      background: linear-gradient(90deg, #3ea6ff, #4caf50);
      width: 100%;
      border-radius: 5px;
    }

    /* FORMS & DIALOGS */
    .form-card {
      background-color: var(--yt-surface);
      border-radius: 16px;
      padding: 24px;
      max-width: 600px;
      width: 100%;
      border: 1px solid var(--yt-border);
    }
    .form-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 18px;
    }
    .form-group {
      margin-bottom: 16px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .form-group label {
      font-size: 13px;
      font-weight: 500;
      color: var(--yt-text-secondary);
    }
    .form-input, .form-textarea, .form-select {
      background-color: #121212;
      border: 1px solid var(--yt-border);
      border-radius: 8px;
      padding: 10px 14px;
      color: #fff;
      font-size: 14px;
      outline: none;
    }
    .form-input:focus, .form-textarea:focus, .form-select:focus {
      border-color: var(--yt-blue);
    }
    .form-textarea {
      resize: vertical;
      min-height: 80px;
    }
    .form-row {
      display: flex;
      gap: 12px;
    }
    .form-row .form-group {
      flex: 1;
    }
    .upload-zone {
      border: 2px dashed var(--yt-border);
      border-radius: 12px;
      padding: 30px;
      text-align: center;
      cursor: pointer;
      background: #151515;
      transition: border-color 0.2s;
    }
    .upload-zone:hover {
      border-color: var(--yt-red);
    }
  </style>
</head>
<body>

  <!-- HEADER -->
  <header class="header">
    <div class="brand-box" onclick="navigate('home')">
      <div class="brand-logo">
        <svg width="30" height="22" viewBox="0 0 24 24" fill="currentColor">
          <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
        </svg>
        <span>dekotube</span>
      </div>
    </div>

    <div class="search-box">
      <input type="text" id="searchInput" class="search-input" placeholder="Search videos, shorts, tags..." onkeyup="if(event.key==='Enter') searchVideos()">
      <button class="search-btn" onclick="searchVideos()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
      </button>
    </div>

    <div class="header-actions">
      <button class="btn btn-create" onclick="openUploadModal()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>Create</span>
      </button>

      <div id="authActions">
        <button class="btn btn-primary" onclick="openAuthModal('login')">Sign In</button>
      </div>
    </div>
  </header>

  <!-- MAIN WRAPPER -->
  <div class="app-container">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <a class="nav-item active" id="nav-home" onclick="navigate('home')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
        </svg>
        <span>Home</span>
      </a>

      <a class="nav-item" id="nav-shorts" onclick="navigate('shorts')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M17.77 10.32l-1.2-.5L18 9.06a3.74 3.74 0 0 0-3.5-5.56 3.75 3.75 0 0 0-3.4 2.18l-3.3 7.62a3.75 3.75 0 0 0 3.4 5.32c.38 0 .76-.06 1.13-.18l1.2.5-1.43.76a3.74 3.74 0 0 0 3.5 5.56 3.75 3.75 0 0 0 3.4-2.18l3.3-7.62a3.75 3.75 0 0 0-4.03-5.34z"/>
        </svg>
        <span>Shorts</span>
      </a>

      <div class="nav-divider"></div>
      <div class="nav-section-title">Creator & Management</div>

      <a class="nav-item" id="nav-upload" onclick="openUploadModal()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="17 8 12 3 7 8"/>
          <line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
        <span>Upload Studio</span>
      </a>

      <a class="nav-item" id="nav-monetization" onclick="navigate('monetization')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
        </svg>
        <span>Monetization Calculator</span>
      </a>

      <a class="nav-item" id="nav-admin" onclick="navigate('admin')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
        </svg>
        <span>Admin Rules & Panel</span>
      </a>
    </aside>

    <!-- MAIN VIEW -->
    <main class="main-view" id="mainView">
      <!-- DYNAMIC CONTENT RENDERED HERE -->
    </main>
  </div>

  <!-- WATCH MODAL -->
  <div class="modal-overlay" id="watchModal">
    <div class="watch-container">
      <div class="video-player-box">
        <video id="activePlayer" controls autoplay></video>
        <button onclick="closeWatchModal()" style="position: absolute; top: 12px; right: 12px; background: rgba(0,0,0,0.7); border: none; color: #fff; width: 36px; height: 36px; border-radius: 50%; cursor: pointer; font-size: 18px;">✕</button>
      </div>
      <div class="watch-content">
        <div class="watch-title" id="watchTitle">Video Title</div>
        <div class="watch-bar">
          <div class="watch-channel">
            <div class="channel-avatar">
              <img id="watchAvatar" src="" alt="Avatar">
            </div>
            <div>
              <div style="font-weight: 600;" id="watchChannelName">Channel</div>
              <div style="font-size: 12px; color: var(--yt-text-secondary);" id="watchSubscribers">Subscriber Count</div>
            </div>
            <button class="btn btn-primary" style="margin-left: 12px;" onclick="alert('Subscribed to channel!')">Subscribe</button>
          </div>
          <div class="watch-actions">
            <button class="btn btn-create" id="likeBtn" onclick="likeCurrentVideo()">
              👍 <span id="watchLikes">0</span>
            </button>
            <button class="btn btn-create" onclick="alert('Shared link copied to clipboard!')">Share</button>
            <button class="btn btn-create" id="watchRevenueBadge" style="color: #4caf50; font-weight: 600;">💰 $0.00 Est.</button>
          </div>
        </div>

        <div class="watch-desc-box" id="watchDescription">
          Description
        </div>

        <!-- COMMENTS -->
        <div class="comments-section">
          <div style="font-weight: 700; font-size: 16px; margin-bottom: 12px;">Comments</div>
          <div class="comment-input-box">
            <input type="text" class="comment-input" id="newCommentInput" placeholder="Add a comment on Dekotube..." onkeyup="if(event.key==='Enter') submitComment()">
            <button class="btn btn-primary" onclick="submitComment()">Comment</button>
          </div>
          <div id="commentsList"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- UPLOAD MODAL -->
  <div class="modal-overlay" id="uploadModal">
    <div class="form-card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <div class="form-title" style="margin-bottom: 0;">Upload to Dekotube Studio</div>
        <button onclick="closeUploadModal()" style="background:none; border:none; color:#aaa; font-size:20px; cursor:pointer;">✕</button>
      </div>

      <form id="uploadForm" onsubmit="handleUploadSubmit(event)">
        <div class="form-group">
          <label>Video File (MP4, WebM)</label>
          <input type="file" name="video" class="form-input" accept="video/*">
        </div>

        <div class="form-group">
          <label>Custom Thumbnail (Optional)</label>
          <input type="file" name="thumbnail" class="form-input" accept="image/*">
        </div>

        <div class="form-group">
          <label>Title</label>
          <input type="text" name="title" class="form-input" placeholder="Enter an engaging title" required>
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea name="description" class="form-textarea" placeholder="Tell viewers about your video"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Category</label>
            <select name="category" class="form-select">
              <option value="Technology">Technology</option>
              <option value="Design">Design</option>
              <option value="Gaming">Gaming</option>
              <option value="Music">Music</option>
              <option value="Education">Education</option>
              <option value="News">News</option>
            </select>
          </div>
          <div class="form-group">
            <label>Format</label>
            <select name="isShort" class="form-select" id="uploadFormatSelect">
              <option value="false">Standard Video (16:9)</option>
              <option value="true">Dekotube Short (9:16)</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>Tags (comma separated)</label>
          <input type="text" name="tags" class="form-input" placeholder="tech, tutorial, coding">
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 12px; margin-top: 10px;">
          Upload & Publish
        </button>
      </form>
    </div>
  </div>

  <!-- AUTH MODAL -->
  <div class="modal-overlay" id="authModal">
    <div class="form-card" style="max-width: 440px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div class="form-title" id="authModalTitle" style="margin-bottom: 0;">Sign in to dekotube</div>
        <button onclick="closeAuthModal()" style="background:none; border:none; color:#aaa; font-size:20px; cursor:pointer;">✕</button>
      </div>

      <div id="authAlert" style="display: none; padding: 10px; background: rgba(255,0,0,0.2); border-radius: 8px; color: #ff5555; font-size: 13px; margin-bottom: 14px;"></div>

      <form id="authForm" onsubmit="handleAuthSubmit(event)">
        <div class="form-group" id="groupUsername" style="display: none;">
          <label>Username</label>
          <input type="text" id="authUsername" class="form-input" placeholder="Pick a username">
        </div>

        <div class="form-group" id="groupChannelName" style="display: none;">
          <label>Channel Name</label>
          <input type="text" id="authChannelName" class="form-input" placeholder="e.g. Code Masters">
        </div>

        <div class="form-group">
          <label id="authEmailLabel">Email or Username</label>
          <input type="text" id="authEmail" class="form-input" placeholder="Enter your credentials" required>
        </div>

        <div class="form-group">
          <label>Password</label>
          <input type="password" id="authPassword" class="form-input" placeholder="Enter password" required>
        </div>

        <button type="submit" class="btn btn-primary" id="authSubmitBtn" style="width: 100%; justify-content: center; padding: 12px; margin-top: 10px;">
          Sign In
        </button>

        <div style="margin-top: 16px; text-align: center; font-size: 13px; color: var(--yt-text-secondary);">
          <span id="authToggleText">Do not have an account?</span>
          <a href="javascript:void(0)" onclick="toggleAuthMode()" id="authToggleLink" style="color: var(--yt-blue); text-decoration: none; font-weight: 500; margin-left: 6px;">Sign up</a>
        </div>

        <div style="margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--yt-border); font-size: 12px; color: var(--yt-text-secondary); text-align: center;">
          Demo accounts: <b>admin</b> / <b>admin123</b> (Admin role) or <b>alex_tech</b> / <b>creator123</b>
        </div>
      </form>
    </div>
  </div>

  <!-- ADD RULE MODAL (ADMIN) -->
  <div class="modal-overlay" id="addRuleModal">
    <div class="form-card" style="max-width: 520px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div class="form-title" style="margin-bottom: 0;">Add Platform Rule</div>
        <button onclick="document.getElementById('addRuleModal').classList.remove('open')" style="background:none; border:none; color:#aaa; font-size:20px; cursor:pointer;">✕</button>
      </div>
      <form onsubmit="handleCreateRuleSubmit(event)">
        <div class="form-group">
          <label>Rule Title</label>
          <input type="text" id="ruleTitleInput" class="form-input" placeholder="e.g. AI-Generated Content Transparency" required>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Category</label>
            <select id="ruleCategorySelect" class="form-select">
              <option value="Copyright">Copyright</option>
              <option value="Safety">Safety</option>
              <option value="Monetization">Monetization</option>
              <option value="Format">Format</option>
              <option value="Integrity">Integrity</option>
            </select>
          </div>
          <div class="form-group">
            <label>Severity</label>
            <select id="ruleSeveritySelect" class="form-select">
              <option value="Critical">Critical</option>
              <option value="High">High</option>
              <option value="Medium">Medium</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>Rule Description & Guidelines</label>
          <textarea id="ruleDescInput" class="form-textarea" placeholder="Detail the enforcement and expectations for creators..." required></textarea>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 12px;">Create Rule</button>
      </form>
    </div>
  </div>

  <script>
    // App State
    let currentUser = null;
    let currentAuthMode = 'login';
    let activeTab = 'home';
    let currentCategory = 'All';
    let allVideos = [];
    let currentWatchVideo = null;

    // Initialize
    window.addEventListener('DOMContentLoaded', async () => {
      checkStoredAuth();
      loadVideos();
    });

    // NAVIGATION
    function navigate(tab) {
      activeTab = tab;
      document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
      const activeEl = document.getElementById('nav-' + tab);
      if (activeEl) activeEl.classList.add('active');

      if (tab === 'home') renderHomeView();
      else if (tab === 'shorts') renderShortsView();
      else if (tab === 'monetization') renderMonetizationView();
      else if (tab === 'admin') renderAdminView();
    }

    // AUTH SYSTEM
    function checkStoredAuth() {
      const token = localStorage.getItem('dekotube_token');
      if (token) {
        fetch('/api/auth/me', {
          headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(r => r.json())
        .then(data => {
          if (data.user) {
            currentUser = data.user;
            renderAuthHeader();
          } else {
            localStorage.removeItem('dekotube_token');
          }
        })
        .catch(() => localStorage.removeItem('dekotube_token'));
      }
    }

    function renderAuthHeader() {
      const container = document.getElementById('authActions');
      if (currentUser) {
        container.innerHTML = \`
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 13px; color: var(--yt-text-secondary);">\${currentUser.channelName}</span>
            <button class="avatar-btn" title="\${currentUser.username} (\${currentUser.role})" onclick="toggleUserDropdown()">
              <img src="\${currentUser.avatar}" alt="Avatar">
            </button>
            <button class="btn btn-outline" style="padding: 4px 10px; font-size: 12px;" onclick="logout()">Sign Out</button>
          </div>
        \`;
      } else {
        container.innerHTML = \`<button class="btn btn-primary" onclick="openAuthModal('login')">Sign In</button>\`;
      }
    }

    function openAuthModal(mode = 'login') {
      currentAuthMode = mode;
      document.getElementById('authAlert').style.display = 'none';
      const isSignup = mode === 'signup';
      document.getElementById('authModalTitle').innerText = isSignup ? 'Create your Dekotube Channel' : 'Sign in to dekotube';
      document.getElementById('groupUsername').style.display = isSignup ? 'flex' : 'none';
      document.getElementById('groupChannelName').style.display = isSignup ? 'flex' : 'none';
      document.getElementById('authEmailLabel').innerText = isSignup ? 'Email Address' : 'Email or Username';
      document.getElementById('authSubmitBtn').innerText = isSignup ? 'Sign Up' : 'Sign In';
      document.getElementById('authToggleText').innerText = isSignup ? 'Already have an account?' : 'Do not have an account?';
      document.getElementById('authToggleLink').innerText = isSignup ? 'Sign in' : 'Sign up';
      document.getElementById('authModal').classList.add('open');
    }

    function closeAuthModal() {
      document.getElementById('authModal').classList.remove('open');
    }

    function toggleAuthMode() {
      openAuthModal(currentAuthMode === 'login' ? 'signup' : 'login');
    }

    async function handleAuthSubmit(e) {
      e.preventDefault();
      const alertBox = document.getElementById('authAlert');
      alertBox.style.display = 'none';

      const emailVal = document.getElementById('authEmail').value;
      const passVal = document.getElementById('authPassword').value;

      try {
        let endpoint = currentAuthMode === 'signup' ? '/api/auth/signup' : '/api/auth/login';
        let payload = currentAuthMode === 'signup' ? {
          username: document.getElementById('authUsername').value,
          channelName: document.getElementById('authChannelName').value,
          email: emailVal,
          password: passVal
        } : {
          emailOrUsername: emailVal,
          password: passVal
        };

        const res = await fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (!res.ok) {
          alertBox.innerText = data.error || 'Authentication error';
          alertBox.style.display = 'block';
          return;
        }

        localStorage.setItem('dekotube_token', data.token);
        currentUser = data.user;
        renderAuthHeader();
        closeAuthModal();
        alert('Welcome, ' + (currentUser.channelName || currentUser.username) + '!');
      } catch (err) {
        alertBox.innerText = 'Network request failed';
        alertBox.style.display = 'block';
      }
    }

    function logout() {
      localStorage.removeItem('dekotube_token');
      currentUser = null;
      renderAuthHeader();
      navigate('home');
    }

    // VIDEOS FETCH & RENDER
    async function loadVideos() {
      try {
        const res = await fetch('/api/videos');
        const data = await res.json();
        allVideos = data.videos || [];
        if (activeTab === 'home') renderHomeView();
      } catch (e) {
        console.error('Error fetching videos', e);
      }
    }

    function filterCategory(cat) {
      currentCategory = cat;
      renderHomeView();
    }

    function searchVideos() {
      const q = document.getElementById('searchInput').value.trim();
      renderHomeView(q);
    }

    // 1. HOME VIEW
    function renderHomeView(searchQuery = '') {
      const main = document.getElementById('mainView');
      const categories = ['All', 'Technology', 'Design', 'Gaming', 'Music', 'Education', 'News'];

      let displayVideos = [...allVideos].filter(v => !v.isShort);
      let shortsPreview = [...allVideos].filter(v => v.isShort);

      if (currentCategory !== 'All') {
        displayVideos = displayVideos.filter(v => v.category === currentCategory);
      }
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        displayVideos = displayVideos.filter(v => v.title.toLowerCase().includes(q) || v.description.toLowerCase().includes(q));
      }

      main.innerHTML = \`
        <!-- CHIPS BAR -->
        <div class="chips-bar">
          \${categories.map(c => \`
            <button class="chip \${currentCategory === c ? 'active' : ''}" onclick="filterCategory('\${c}')">\${c}</button>
          \`).join('')}
        </div>

        <!-- SHORTS STRIP (IF ANY) -->
        \${shortsPreview.length > 0 && !searchQuery && currentCategory === 'All' ? \`
          <div class="section-title">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="var(--yt-red)">
              <path d="M17.77 10.32l-1.2-.5L18 9.06a3.74 3.74 0 0 0-3.5-5.56 3.75 3.75 0 0 0-3.4 2.18l-3.3 7.62a3.75 3.75 0 0 0 3.4 5.32c.38 0 .76-.06 1.13-.18l1.2.5-1.43.76a3.74 3.74 0 0 0 3.5 5.56 3.75 3.75 0 0 0 3.4-2.18l3.3-7.62a3.75 3.75 0 0 0-4.03-5.34z"/>
            </svg>
            <span>Shorts</span>
          </div>
          <div class="shorts-grid">
            \${shortsPreview.slice(0, 4).map(s => \`
              <div class="short-card" onclick="openWatchModal('\${s.id}')">
                <div class="short-thumbnail">
                  <img src="\${s.thumbnailUrl}" alt="\${s.title}">
                  <span class="short-badge">SHORTS</span>
                </div>
                <div class="short-title">\${s.title}</div>
                <div class="short-views">\${Number(s.views).toLocaleString()} views</div>
              </div>
            \`).join('')}
          </div>
        \` : ''}

        <!-- STANDARD VIDEOS GRID -->
        <div class="section-title">Recommended Videos</div>
        <div class="video-grid">
          \${displayVideos.map(v => \`
            <div class="video-card" onclick="openWatchModal('\${v.id}')">
              <div class="video-thumbnail">
                <img src="\${v.thumbnailUrl}" alt="\${v.title}">
                <span class="duration-pill">\${v.duration}</span>
              </div>
              <div class="video-info">
                <div class="channel-avatar">
                  <img src="\${v.uploaderAvatar}" alt="\${v.uploaderName}">
                </div>
                <div class="video-details">
                  <div class="video-title">\${v.title}</div>
                  <div class="channel-name">\${v.uploaderName}</div>
                  <div class="video-meta">\${Number(v.views).toLocaleString()} views • \${timeAgo(v.createdAt)}</div>
                </div>
              </div>
            </div>
          \`).join('')}
        </div>
      \`;
    }

    // 2. SHORTS VIEW
    function renderShortsView() {
      const main = document.getElementById('mainView');
      const shortsList = allVideos.filter(v => v.isShort);

      main.innerHTML = \`
        <div class="section-title">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="var(--yt-red)">
            <path d="M17.77 10.32l-1.2-.5L18 9.06a3.74 3.74 0 0 0-3.5-5.56 3.75 3.75 0 0 0-3.4 2.18l-3.3 7.62a3.75 3.75 0 0 0 3.4 5.32c.38 0 .76-.06 1.13-.18l1.2.5-1.43.76a3.74 3.74 0 0 0 3.5 5.56 3.75 3.75 0 0 0 3.4-2.18l3.3-7.62a3.75 3.75 0 0 0-4.03-5.34z"/>
          </svg>
          <span>Dekotube Shorts</span>
        </div>
        <p style="color: var(--yt-text-secondary); margin-bottom: 24px; font-size: 14px;">
          Vertical, fast-paced 60-second video feeds with instant view-tracking and shorts pool monetization.
        </p>

        <div class="shorts-grid" style="grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));">
          \${shortsList.map(s => \`
            <div class="short-card" onclick="openWatchModal('\${s.id}')">
              <div class="short-thumbnail">
                <img src="\${s.thumbnailUrl}" alt="\${s.title}">
                <span class="short-badge">SHORTS</span>
              </div>
              <div class="short-title" style="margin-top: 6px;">\${s.title}</div>
              <div class="channel-name">\${s.uploaderName}</div>
              <div class="short-views">\${Number(s.views).toLocaleString()} views</div>
            </div>
          \`).join('')}
        </div>
      \`;
    }

    // 3. MONETIZATION VIEW
    function renderMonetizationView() {
      const main = document.getElementById('mainView');
      main.innerHTML = \`
        <div class="section-title">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="#4caf50">
            <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
          </svg>
          <span>Creator Monetization & Earnings Calculator</span>
        </div>
        <p style="color: var(--yt-text-secondary); margin-bottom: 24px; font-size: 14px;">
          Calculate estimated monthly and annual revenue based on views, RPM, channel memberships, and Dekotube 55/45 revenue share split.
        </p>

        <div class="calc-container">
          <!-- CONTROLS -->
          <div class="calc-controls">
            <div class="calc-group">
              <div class="calc-label">
                <span>Monthly Long-form Views</span>
                <b id="lblLongViews">250,000</b>
              </div>
              <input type="range" class="calc-slider" id="rngLongViews" min="10000" max="5000000" step="10000" value="250000" oninput="recalcMonetization()">
            </div>

            <div class="calc-group">
              <div class="calc-label">
                <span>Long-form RPM (Revenue Per 1K Views)</span>
                <b id="lblLongRpm">$3.80</b>
              </div>
              <input type="range" class="calc-slider" id="rngLongRpm" min="1.0" max="15.0" step="0.1" value="3.8" oninput="recalcMonetization()">
            </div>

            <div class="calc-group">
              <div class="calc-label">
                <span>Monthly Shorts Views</span>
                <b id="lblShortsViews">1,500,000</b>
              </div>
              <input type="range" class="calc-slider" id="rngShortsViews" min="50000" max="25000000" step="50000" value="1500000" oninput="recalcMonetization()">
            </div>

            <div class="calc-group">
              <div class="calc-label">
                <span>Shorts RPM Pool Share</span>
                <b id="lblShortsRpm">$0.12</b>
              </div>
              <input type="range" class="calc-slider" id="rngShortsRpm" min="0.04" max="0.30" step="0.01" value="0.12" oninput="recalcMonetization()">
            </div>

            <div class="calc-group">
              <div class="calc-label">
                <span>Active Channel Subscribers</span>
                <b id="lblSubs">45,000</b>
              </div>
              <input type="range" class="calc-slider" id="rngSubs" min="1000" max="500000" step="1000" value="45000" oninput="recalcMonetization()">
            </div>
          </div>

          <!-- RESULTS -->
          <div class="calc-results">
            <div>
              <div style="font-size: 13px; text-transform: uppercase; color: var(--yt-text-secondary); font-weight: 600;">Estimated Creator Payout</div>
              <div class="earnings-big" id="resMonthly">$0.00</div>
              <div style="font-size: 14px; color: var(--yt-text-secondary);">Projected Monthly Net Income</div>

              <div style="margin: 20px 0 10px;">
                <div style="display: flex; justify-content: space-between; font-size: 13px;">
                  <span>Monthly Payout Threshold ($100 Min)</span>
                  <b id="resThreshold">100% Eligible</b>
                </div>
                <div class="payout-bar">
                  <div class="payout-fill" id="resProgress"></div>
                </div>
              </div>

              <div style="border-top: 1px solid var(--yt-border); padding-top: 16px; margin-top: 16px; display: flex; flex-direction: column; gap: 8px; font-size: 14px;">
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: var(--yt-text-secondary);">Long-Form Ads (55% Share):</span>
                  <b id="resLongForm">$0.00</b>
                </div>
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: var(--yt-text-secondary);">Shorts Pool (45% Share):</span>
                  <b id="resShorts">$0.00</b>
                </div>
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: var(--yt-text-secondary);">Memberships (70% Share):</span>
                  <b id="resMemberships">$0.00</b>
                </div>
                <div style="display: flex; justify-content: space-between; font-weight: 700; color: #4caf50; padding-top: 8px; border-top: 1px dashed var(--yt-border);">
                  <span>Annual Projected Earnings:</span>
                  <span id="resAnnual">$0.00</span>
                </div>
              </div>
            </div>

            <button class="btn btn-primary" style="margin-top: 20px; justify-content: center; width: 100%;" onclick="alert('Monetization settings saved to your creator profile!')">
              Apply to My Channel
            </button>
          </div>
        </div>
      \`;
      recalcMonetization();
    }

    async function recalcMonetization() {
      const longViews = parseInt(document.getElementById('rngLongViews').value);
      const longRpm = parseFloat(document.getElementById('rngLongRpm').value);
      const shortsViews = parseInt(document.getElementById('rngShortsViews').value);
      const shortsRpm = parseFloat(document.getElementById('rngShortsRpm').value);
      const subs = parseInt(document.getElementById('rngSubs').value);

      document.getElementById('lblLongViews').innerText = Number(longViews).toLocaleString();
      document.getElementById('lblLongRpm').innerText = '$' + longRpm.toFixed(2);
      document.getElementById('lblShortsViews').innerText = Number(shortsViews).toLocaleString();
      document.getElementById('lblShortsRpm').innerText = '$' + shortsRpm.toFixed(2);
      document.getElementById('lblSubs').innerText = Number(subs).toLocaleString();

      try {
        const res = await fetch('/api/monetization/calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            monthlyViews: longViews,
            longFormRpm: longRpm,
            shortsViews: shortsViews,
            shortsRpm: shortsRpm,
            subscribers: subs
          })
        });
        const data = await res.json();
        document.getElementById('resMonthly').innerText = '$' + Number(data.summary.totalMonthlyEarnings).toLocaleString();
        document.getElementById('resAnnual').innerText = '$' + Number(data.summary.totalYearlyEarnings).toLocaleString();
        document.getElementById('resLongForm').innerText = '$' + Number(data.breakdown.longForm.creatorEarnings).toLocaleString();
        document.getElementById('resShorts').innerText = '$' + Number(data.breakdown.shorts.creatorEarnings).toLocaleString();
        document.getElementById('resMemberships').innerText = '$' + Number(data.breakdown.memberships.creatorEarnings).toLocaleString();
        document.getElementById('resProgress').style.width = data.summary.progressToPayout + '%';
        document.getElementById('resThreshold').innerText = data.summary.payoutEligible ? 'Eligible for Direct Deposit' : 'Under $100 Threshold';
      } catch (err) {
        console.error('Calculation error', err);
      }
    }

    // 4. ADMIN PANEL VIEW
    async function renderAdminView() {
      const main = document.getElementById('mainView');

      if (!currentUser || currentUser.role !== 'admin') {
        main.innerHTML = \`
          <div style="background-color: var(--yt-surface); border: 1px solid var(--yt-border); border-radius: 16px; padding: 40px; text-align: center; max-width: 600px; margin: 40px auto;">
            <div style="font-size: 48px; margin-bottom: 16px;">🔒</div>
            <div style="font-size: 22px; font-weight: 700; margin-bottom: 8px;">Admin Access Required</div>
            <p style="color: var(--yt-text-secondary); margin-bottom: 24px; font-size: 14px;">
              You must be logged in as an administrator to manage platform rules, moderate videos, and view full system analytics.
            </p>
            <button class="btn btn-primary" onclick="openAuthModal('login')">Sign in as Administrator</button>
            <div style="font-size: 12px; color: var(--yt-text-secondary); margin-top: 14px;">
              Quick demo login: username: <b>admin</b> | password: <b>admin123</b>
            </div>
          </div>
        \`;
        return;
      }

      try {
        const token = localStorage.getItem('dekotube_token');
        const res = await fetch('/api/admin/overview', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        const data = await res.json();

        main.innerHTML = \`
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div class="section-title" style="margin: 0;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="var(--yt-red)">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
              </svg>
              <span>Dekotube Admin Console & Rules Management</span>
            </div>
            <button class="btn btn-primary" onclick="openAddRuleModal()">
              + Add Platform Rule
            </button>
          </div>

          <!-- METRICS -->
          <div class="admin-card-row">
            <div class="stat-card">
              <span class="stat-title">Total Users</span>
              <span class="stat-value">\${data.stats.totalUsers}</span>
            </div>
            <div class="stat-card">
              <span class="stat-title">Uploaded Videos</span>
              <span class="stat-value">\${data.stats.totalVideos}</span>
            </div>
            <div class="stat-card">
              <span class="stat-title">Uploaded Shorts</span>
              <span class="stat-value">\${data.stats.totalShorts}</span>
            </div>
            <div class="stat-card">
              <span class="stat-title">Platform Views</span>
              <span class="stat-value">\${Number(data.stats.totalViews).toLocaleString()}</span>
            </div>
            <div class="stat-card">
              <span class="stat-title">Est. Platform Rev</span>
              <span class="stat-value" style="color: #4caf50;">\${data.stats.estimatedPlatformRevenue}</span>
            </div>
          </div>

          <!-- PLATFORM RULES MANAGEMENT -->
          <div style="margin-top: 32px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
              <div style="font-weight: 700; font-size: 18px;">Enforced Platform Rules</div>
              <span style="font-size: 13px; color: var(--yt-text-secondary);">\${data.stats.activeRulesCount} active rules</span>
            </div>
            <table class="rules-table">
              <thead>
                <tr>
                  <th>Rule ID</th>
                  <th>Title & Description</th>
                  <th>Category</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                \${data.rules.map(r => \`
                  <tr>
                    <td style="font-family: monospace; font-size: 12px; color: var(--yt-text-secondary);">\${r.id}</td>
                    <td>
                      <div style="font-weight: 600; color: #fff;">\${r.title}</div>
                      <div style="font-size: 13px; color: var(--yt-text-secondary); margin-top: 2px;">\${r.description}</div>
                    </td>
                    <td>\${r.category}</td>
                    <td>
                      <span class="badge badge-\${r.severity.toLowerCase()}">\${r.severity}</span>
                    </td>
                    <td>
                      <span class="badge \${r.active ? 'badge-active' : 'badge-inactive'}">
                        \${r.active ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>
                      <button class="btn btn-create" style="padding: 4px 8px; font-size: 12px;" onclick="toggleRule('\${r.id}')">
                        \${r.active ? 'Disable' : 'Enable'}
                      </button>
                      <button class="btn btn-create" style="padding: 4px 8px; font-size: 12px; color: #ff5555; margin-left: 4px;" onclick="deleteRule('\${r.id}')">
                        Delete
                      </button>
                    </td>
                  </tr>
                \`).join('')}
              </tbody>
            </table>
          </div>

          <!-- RECENT VIDEOS & MODERATION -->
          <div style="margin-top: 36px;">
            <div style="font-weight: 700; font-size: 18px; margin-bottom: 12px;">Content Moderation (Recent Uploads)</div>
            <table class="rules-table">
              <thead>
                <tr>
                  <th>Video</th>
                  <th>Uploader</th>
                  <th>Type</th>
                  <th>Views</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                \${data.recentVideos.map(v => \`
                  <tr>
                    <td style="font-weight: 500;">\${v.title}</td>
                    <td style="color: var(--yt-text-secondary);">\${v.uploaderName}</td>
                    <td>\${v.isShort ? 'Short' : 'Standard'}</td>
                    <td>\${Number(v.views).toLocaleString()}</td>
                    <td>
                      <button class="btn btn-create" style="padding: 4px 8px; font-size: 12px; color: #ff5555;" onclick="deleteVideoAdmin('\${v.id}')">
                        Remove
                      </button>
                    </td>
                  </tr>
                \`).join('')}
              </tbody>
            </table>
          </div>
        \`;
      } catch (err) {
        main.innerHTML = \`<div style="color: #ff5555; padding: 20px;">Failed to load Admin console. Check permissions.</div>\`;
      }
    }

    function openAddRuleModal() {
      document.getElementById('addRuleModal').classList.add('open');
    }

    async function handleCreateRuleSubmit(e) {
      e.preventDefault();
      const token = localStorage.getItem('dekotube_token');
      const title = document.getElementById('ruleTitleInput').value;
      const category = document.getElementById('ruleCategorySelect').value;
      const severity = document.getElementById('ruleSeveritySelect').value;
      const description = document.getElementById('ruleDescInput').value;

      try {
        const res = await fetch('/api/admin/rules', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
          },
          body: JSON.stringify({ title, category, severity, description })
        });
        if (res.ok) {
          document.getElementById('addRuleModal').classList.remove('open');
          renderAdminView();
        } else {
          alert('Failed to create rule');
        }
      } catch (err) {
        alert('Network error');
      }
    }

    async function toggleRule(id) {
      const token = localStorage.getItem('dekotube_token');
      await fetch('/api/admin/rules/' + id + '/toggle', {
        method: 'PATCH',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      renderAdminView();
    }

    async function deleteRule(id) {
      if (!confirm('Are you sure you want to delete this rule?')) return;
      const token = localStorage.getItem('dekotube_token');
      await fetch('/api/admin/rules/' + id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      renderAdminView();
    }

    async function deleteVideoAdmin(id) {
      if (!confirm('Remove this video for policy violation?')) return;
      const token = localStorage.getItem('dekotube_token');
      await fetch('/api/admin/videos/' + id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      await loadVideos();
      renderAdminView();
    }

    // 5. WATCH MODAL & VIEW TRACKING
    async function openWatchModal(videoId) {
      const res = await fetch('/api/videos/' + videoId);
      const data = await res.json();
      currentWatchVideo = data.video;

      document.getElementById('watchTitle').innerText = currentWatchVideo.title;
      document.getElementById('watchChannelName').innerText = currentWatchVideo.uploaderName;
      document.getElementById('watchAvatar').src = currentWatchVideo.uploaderAvatar;
      document.getElementById('watchSubscribers').innerText = (currentWatchVideo.isShort ? 'Short Reel' : 'Standard HD') + ' • ' + Number(currentWatchVideo.views).toLocaleString() + ' views';
      document.getElementById('watchLikes').innerText = Number(currentWatchVideo.likes).toLocaleString();
      document.getElementById('watchDescription').innerText = currentWatchVideo.description || 'No description provided.';

      const player = document.getElementById('activePlayer');
      player.src = currentWatchVideo.videoUrl;
      player.play().catch(() => {});

      document.getElementById('watchModal').classList.add('open');
      renderComments(data.comments || []);

      // Increment View Count & Track Monetization
      try {
        const viewRes = await fetch('/api/videos/' + videoId + '/view', { method: 'POST' });
        const viewData = await viewRes.json();
        if (viewData.views) {
          currentWatchVideo.views = viewData.views;
          document.getElementById('watchSubscribers').innerText = (currentWatchVideo.isShort ? 'Short Reel' : 'Standard HD') + ' • ' + Number(viewData.views).toLocaleString() + ' views';
          document.getElementById('watchRevenueBadge').innerText = '💰 ' + viewData.estimatedRevenue + ' Est.';
        }
      } catch (err) {}
    }

    function closeWatchModal() {
      const player = document.getElementById('activePlayer');
      player.pause();
      player.src = '';
      document.getElementById('watchModal').classList.remove('open');
      currentWatchVideo = null;
    }

    async function likeCurrentVideo() {
      if (!currentUser) return openAuthModal('login');
      if (!currentWatchVideo) return;
      const token = localStorage.getItem('dekotube_token');
      const res = await fetch('/api/videos/' + currentWatchVideo.id + '/like', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const data = await res.json();
      if (data.likes) {
        document.getElementById('watchLikes').innerText = Number(data.likes).toLocaleString();
      }
    }

    function renderComments(comments) {
      const list = document.getElementById('commentsList');
      if (!comments.length) {
        list.innerHTML = '<div style="color: var(--yt-text-secondary); font-size: 13px;">No comments yet. Be the first to comment!</div>';
        return;
      }
      list.innerHTML = comments.map(c => \`
        <div style="display: flex; gap: 12px; margin-bottom: 14px;">
          <div class="channel-avatar" style="width: 32px; height: 32px;">
            <img src="\${c.avatar}" alt="\${c.author}">
          </div>
          <div>
            <div style="font-size: 13px; font-weight: 600; color: #fff;">\${c.author} <span style="font-size: 11px; color: var(--yt-text-secondary); font-weight: normal; margin-left: 6px;">\${c.createdAt}</span></div>
            <div style="font-size: 13px; color: #e1e1e1; margin-top: 3px;">\${c.text}</div>
          </div>
        </div>
      \`).join('');
    }

    async function submitComment() {
      if (!currentUser) return openAuthModal('login');
      const input = document.getElementById('newCommentInput');
      const text = input.value.trim();
      if (!text || !currentWatchVideo) return;

      const token = localStorage.getItem('dekotube_token');
      const res = await fetch('/api/videos/' + currentWatchVideo.id + '/comments', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify({ text })
      });
      const data = await res.json();
      if (data.comment) {
        input.value = '';
        const list = document.getElementById('commentsList');
        const newHtml = \`
          <div style="display: flex; gap: 12px; margin-bottom: 14px;">
            <div class="channel-avatar" style="width: 32px; height: 32px;">
              <img src="\${data.comment.avatar}" alt="\${data.comment.author}">
            </div>
            <div>
              <div style="font-size: 13px; font-weight: 600; color: #fff;">\${data.comment.author} <span style="font-size: 11px; color: var(--yt-text-secondary); font-weight: normal; margin-left: 6px;">Just now</span></div>
              <div style="font-size: 13px; color: #e1e1e1; margin-top: 3px;">\${data.comment.text}</div>
            </div>
          </div>
        \`;
        list.insertAdjacentHTML('afterbegin', newHtml);
      }
    }

    // 6. UPLOAD MODAL & MULTER FORM
    function openUploadModal() {
      if (!currentUser) {
        return openAuthModal('login');
      }
      document.getElementById('uploadModal').classList.add('open');
    }

    function closeUploadModal() {
      document.getElementById('uploadModal').classList.remove('open');
    }

    async function handleUploadSubmit(e) {
      e.preventDefault();
      const form = document.getElementById('uploadForm');
      const formData = new FormData(form);
      const token = localStorage.getItem('dekotube_token');

      try {
        const res = await fetch('/api/upload', {
          method: 'POST',
          headers: { 'Authorization': 'Bearer ' + token },
          body: formData
        });
        const data = await res.json();
        if (res.ok) {
          alert('Success: ' + data.message);
          closeUploadModal();
          form.reset();
          await loadVideos();
          if (data.video && data.video.isShort) navigate('shorts');
          else navigate('home');
        } else {
          alert('Upload failed: ' + (data.error || 'Check fields'));
        }
      } catch (err) {
        alert('Network upload error');
      }
    }

    // Helpers
    function timeAgo(dateString) {
      const date = new Date(dateString);
      const seconds = Math.floor((new Date() - date) / 1000);
      if (seconds < 60) return 'Just now';
      const minutes = Math.floor(seconds / 60);
      if (minutes < 60) return minutes + ' mins ago';
      const hours = Math.floor(minutes / 60);
      if (hours < 24) return hours + ' hours ago';
      const days = Math.floor(hours / 24);
      return days + ' days ago';
    }
  </script>
</body>
</html>
`);
});

// START SERVER
app.listen(PORT, () => {
  console.log('====================================================');
  console.log(` Dekotube Streaming Platform is running on port ${PORT}`);
  console.log(` Web App URL: http://localhost:${PORT}`);
  console.log(` Uploads Folder: ${UPLOAD_DIR}`);
  console.log('====================================================');
});
