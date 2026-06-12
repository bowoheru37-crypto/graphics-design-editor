# 📊 Database Schema Documentation

## Collections Overview

### 1. Users Collection
```javascript
{
  _id: ObjectId,
  email: String (unique, indexed),
  password: String (hashed),
  displayName: String,
  avatar: String (URL),
  bio: String,
  subscription: Enum['free', 'pro', 'enterprise'],
  verificationStatus: Enum['pending', 'verified'],
  createdAt: Date,
  updatedAt: Date,
  lastLogin: Date,
  profile: {
    website: String,
    location: String,
    socialLinks: {
      twitter: String,
      github: String,
      linkedin: String
    }
  },
  settings: {
    theme: Enum['light', 'dark', 'auto'],
    language: String,
    emailNotifications: Boolean,
    twoFactorEnabled: Boolean
  }
}
```

### 2. Projects Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId (indexed),
  title: String (required),
  description: String,
  type: Enum['app', 'game', 'website', 'design'],
  thumbnail: String (URL),
  data: {
    canvas: {...},
    layers: [...],
    assets: [...],
    settings: {...}
  },
  collaborators: [{
    userId: ObjectId,
    role: Enum['editor', 'viewer']
  }],
  isPublic: Boolean,
  tags: [String],
  status: Enum['draft', 'published', 'archived'],
  viewCount: Number,
  likes: [ObjectId],
  comments: [{
    userId: ObjectId,
    text: String,
    createdAt: Date
  }],
  createdAt: Date,
  updatedAt: Date
}
```

### 3. Assets Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId,
  name: String,
  type: Enum['image', 'icon', 'vector', 'font', 'audio', 'video', ...],
  category: String,
  url: String,
  thumbnail: String,
  size: Number,
  tags: [String],
  isPublic: Boolean,
  metadata: {
    width: Number,
    height: Number,
    duration: Number,
    ...
  },
  createdAt: Date,
  updatedAt: Date
}
```

### 4. Marketplace Collection
```javascript
{
  _id: ObjectId,
  title: String,
  description: String,
  thumbnail: String,
  category: String,
  type: Enum['template', 'asset', 'plugin', 'theme'],
  price: Number,
  currency: String,
  creator: {
    userId: ObjectId,
    name: String,
    avatar: String,
    verified: Boolean
  },
  rating: {
    average: Number,
    count: Number
  },
  downloads: Number,
  purchases: Number,
  data: Schema.Types.Mixed,
  createdAt: Date,
  updatedAt: Date
}
```

### 5. Workspaces Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId,
  name: String,
  description: String,
  projects: [ObjectId],
  collaborators: [{
    userId: ObjectId,
    role: Enum['owner', 'admin', 'editor', 'viewer'],
    joinedAt: Date
  }],
  settings: {
    theme: Enum['light', 'dark', 'auto'],
    language: String,
    autoSave: Boolean,
    autoSaveInterval: Number
  },
  createdAt: Date,
  updatedAt: Date
}
```

### 6. Notifications Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId,
  type: Enum['comment', 'share', 'mention', 'system', 'update'],
  title: String,
  message: String,
  actionUrl: String,
  read: Boolean,
  createdAt: Date
}
```

### 7. Comments Collection
```javascript
{
  _id: ObjectId,
  projectId: ObjectId,
  userId: ObjectId,
  text: String,
  replies: [ObjectId],
  likes: [ObjectId],
  createdAt: Date,
  updatedAt: Date
}
```

## Indexes

```javascript
// Users
db.users.createIndex({ email: 1 }, { unique: true })
db.users.createIndex({ createdAt: -1 })

// Projects
db.projects.createIndex({ userId: 1 })
db.projects.createIndex({ type: 1 })
db.projects.createIndex({ isPublic: 1 })
db.projects.createIndex({ createdAt: -1 })

// Assets
db.assets.createIndex({ userId: 1 })
db.assets.createIndex({ category: 1 })
db.assets.createIndex({ type: 1 })

// Marketplace
db.marketplace.createIndex({ category: 1 })
db.marketplace.createIndex({ creator.userId: 1 })
db.marketplace.createIndex({ rating.average: -1 })
```

## Relationships

```
User
├── Projects (1:N)
├── Assets (1:N)
├── Workspaces (1:N)
├── Comments (1:N)
└── Notifications (1:N)

Project
├── User (N:1)
├── Layers (1:N)
├── Assets (1:N)
└── Comments (1:N)

Workspace
├── User (N:1)
├── Projects (1:N)
└── Collaborators (1:N)

Marketplace
├── Creator/User (N:1)
└── Purchases (1:N)
```

## Queries

```javascript
// Find user projects
db.projects.find({ userId: ObjectId("..."), isPublic: false })

// Get public projects with sorting
db.projects.find({ isPublic: true }).sort({ viewCount: -1 }).limit(20)

// Search marketplace by category
db.marketplace.find({ category: "templates" }).sort({ rating: -1 })

// Get user assets by type
db.assets.find({ userId: ObjectId("..."), type: "image" })
```
