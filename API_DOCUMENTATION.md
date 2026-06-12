# 🔌 API Documentation

## Base URL
```
https://api.creativity.dev/v1
```

## Authentication

All requests require JWT token in header:
```
Authorization: Bearer <token>
```

---

## 🔐 AUTH ENDPOINTS

### Register
```
POST /auth/register

Request:
{
  "email": "user@example.com",
  "password": "secure-password",
  "displayName": "John Doe"
}

Response:
{
  "success": true,
  "data": {
    "user": {...},
    "token": "jwt-token"
  }
}
```

### Login
```
POST /auth/login

Request:
{
  "email": "user@example.com",
  "password": "secure-password"
}

Response:
{
  "success": true,
  "data": {
    "user": {...},
    "token": "jwt-token"
  }
}
```

### Verify Email
```
POST /auth/verify-email

Request:
{
  "token": "verification-token"
}
```

### Get Current User
```
GET /auth/me

Response:
{
  "success": true,
  "data": {...user}
}
```

---

## 📁 PROJECT ENDPOINTS

### List Projects
```
GET /projects?page=1&pageSize=20

Response:
{
  "success": true,
  "data": [...projects],
  "total": 100,
  "hasMore": true
}
```

### Get Project
```
GET /projects/:id

Response:
{
  "success": true,
  "data": {...project}
}
```

### Create Project
```
POST /projects

Request:
{
  "title": "My App",
  "description": "...",
  "type": "app",
  "data": {...}
}

Response:
{
  "success": true,
  "data": {...project}
}
```

### Update Project
```
PUT /projects/:id

Request:
{
  "title": "Updated Title",
  "data": {...}
}
```

### Delete Project
```
DELETE /projects/:id
```

### Duplicate Project
```
POST /projects/:id/duplicate
```

---

## 🎨 ASSET ENDPOINTS

### List Assets
```
GET /assets?category=images&page=1
```

### Upload Asset
```
POST /assets/upload

Form Data:
- file: File
- category: string

Response:
{
  "success": true,
  "data": {...asset}
}
```

### Delete Asset
```
DELETE /assets/:id
```

---

## 🛍️ MARKETPLACE ENDPOINTS

### List Marketplace Items
```
GET /marketplace?category=templates&page=1
```

### Search Marketplace
```
GET /marketplace/search?q=query
```

### Purchase Item
```
POST /marketplace/:id/purchase

Response:
{
  "success": true,
  "data": {...purchase}
}
```

---

## 🤖 AI ENDPOINTS

### Generate Code
```
POST /ai/generate-code

Request:
{
  "prompt": "Create a login form"
}

Response:
{
  "success": true,
  "data": {
    "code": "...",
    "language": "javascript"
  }
}
```

### Generate UI
```
POST /ai/generate-ui

Request:
{
  "prompt": "Dashboard layout"
}
```

### Generate Design
```
POST /ai/generate-design

Request:
{
  "prompt": "Modern website header"
}
```

### Generate Image
```
POST /ai/generate-image

Request:
{
  "prompt": "Cat sitting on desk"
}
```

---

## 📤 EXPORT ENDPOINTS

### Export Project
```
POST /projects/:id/export

Request:
{
  "format": "html" | "json" | "zip"
}

Response: File blob
```

### Export as APK
```
POST /projects/:id/export-apk

Response: APK file
```

### Export as HTML
```
POST /projects/:id/export-html

Response: HTML file
```

---

## Error Responses

```javascript
// 400 Bad Request
{
  "success": false,
  "error": "Invalid request",
  "details": {...}
}

// 401 Unauthorized
{
  "success": false,
  "error": "Authentication required"
}

// 403 Forbidden
{
  "success": false,
  "error": "Access denied"
}

// 404 Not Found
{
  "success": false,
  "error": "Resource not found"
}

// 500 Server Error
{
  "success": false,
  "error": "Internal server error"
}
```
