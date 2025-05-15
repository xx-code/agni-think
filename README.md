# 📦 Agni

> A personal finance tracking app to visualize your spending habits and raise awareness about your financial behavior.

This project is an application designed to help you track your expenses, analyze your financial behavior through graphs, and develop better money habits. It offers semi-automated features, and future versions will include AI and workflow automation.

To get the most out of this tool, treat it like a financial notebook — consistent usage is key to long-term insight and improvement.

## 🔥 Why the Name "Agni"?

In my culture, the Agni ethnic group is often stereotyped as stingy. However, I believe that being "stingy" isn't necessarily a bad trait — as long as it doesn't lead to a miserable lifestyle. Instead, it can be a mindset focused on financial health and responsibility. I see it as being economical and intentional with money.

## 📐 Stack

**Frontend**  
- Vue.js with Nuxt (migrating from React)

**Backend**  
- Node.js with Express  
- Database handled using Knex

**Mobile**  
- Swift with SwiftUI (coming soon)

**Monorepo**  
- Turborepo (not yet implemented)

## 🏗️ Architecture

The backend (Node.js + Express) follows **Clean Architecture** principles combined with a **Domain-Driven Design (DDD)** approach for modeling business logic.

### 📂 Folder Structure
```
apps/
    agni-server/ # Backend server
    config/             # Configuration files and dependency injection container # Includes environment setup, constants, and app-wide settings
    controllers/        # HTTP controllers or interfaces to execute use cases # Responsible for parsing requests, calling use cases, and formatting responses
    core/               # The heart of the application (domain and use cases)
      adapters/         # Interface adapters to connect the domain with external systems or frameworks# Example: data mappers, serializers, or gateways
      domains/          # Pure domain logic — no framework or DB logic here # Includes: # - Entities (business objects with identity) # - Value Objects (immutable, behavior-driven types) # - Domain services (pure logic with no side-effects)
      interactions/     # Application-level use cases # Defines how the outside world interacts with domain logic # Example: "CreateTransaction", "GenerateSpendingReport"
      repositories/     # Interfaces that define how to interact with data sources # These are implemented later in the infrastructure layer
    infra/              # Infrastructure implementations for external services
                        # Includes:
                        # - Database adapters (e.g., PostgreSQL, SQLite)
                        # - External APIs
                        # - Repository implementations that fulfill `core/repositories` interfaces
    agni-web/ # Frontend web application (Nuxt.js), Handles all user interface and client-side logic
    
    agni-mobile/ # Mobile app (Swift + SwiftUI, coming soon); # Will share logic where possible with agni-web
```

## ✨ Features
<!--TODO list feature, future features-->

## 🚀 Installation

```bash
# Clone the repository
git clone https://github.com/xx-code/agin-think.git

# Navigate into the main directory
cd agni-think/

# Install dependencies
npm install

# server
# Navigate into the server directory
cd apps/agni-server

# Start the development server
npm run dev

# Navigate into the web directory
cd apps/agni-web

npm run dev

```

