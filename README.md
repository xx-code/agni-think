# 📦 Agni

> A personal finance tracking app to visualize your spending habits and raise awareness about your financial behavior.

This project is an application designed to help you track your expenses, analyze your financial behavior through graphs, and develop better money habits. It offers semi-automated features, and future versions will include AI and workflow automation.

To get the most out of this tool, treat it like a financial notebook consistent usage is key to long-term insight and improvement.

## 🔥 Why the Name "Agni"?

In my culture, the Agni ethnic group is often stereotyped as stingy. However, I believe that being "stingy" isn't necessarily a bad trait as long as it doesn't lead to a miserable lifestyle. Instead, it can be a mindset focused on financial health and responsibility. I see it as being economical and intentional with money.

## 📐 Stack

**Frontend**  
- Vue.js with Nuxt (migrating from React)

**Backend**  
- Koltin with springboot

**Mobile**  
- Swift with SwiftUI (coming soon)
- Kotlin for android (coming soon)


## 🏗️ Architecture

The backend (Kotlin with springboot) follows **Clean Architecture** principles combined with a **Domain-Driven Design (DDD)** approach for modeling business logic.

## ✨ Features
<!--TODO list feature, future features-->

## 🚀 Installation (Quick Star)

```bash
# Clone the repository
git clone https://github.com/xx-code/agin-think.git

# Navigate into le dossier principal
cd agni-think/

# Installer les dépendances
npm install

# Backend
cd apps/agni-server
npm run dev # pour le développement

# Frontend
cd ../agni-web
npm run dev # pour le développement
```

## 📦 Deployment

### Backend (agni-server)
1. Set your environment variables in a `.env` file (see `.env.example`).
2. For production deployment, use:
  ```bash
  npm run build
  npm run start
  ```
3. You can also use Docker:
  ```bash
  docker-compose up -d --build
  ```

### Frontend (agni-web)
1. Set your environment variables in `.env` or `.env.production`.
2. To build for production:
  ```bash
  npm run build
  npm run start
  ```
3. For Docker:
  ```bash
  docker-compose up -d --build
  ```
