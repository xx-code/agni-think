# 📦 Agni

> A personal finance tracking app to visualize your spending habits and raise awareness about your financial behavior.

This project is an application designed to help you track your expenses, analyze your financial behavior through graphs, and develop better money habits. It offers semi-automated features, and future versions will include AI and workflow automation.

To get the most out of this tool, treat it like a financial notebook — consistent usage is key to long-term insight and improvement.

`why agni name? In my culture an Agni ethnic with a majority of her people are stingy, but I think be stingy a not a bad treat when you don't put yourself in miserable situation
just to keep money but a way to think for his financial health (be econome)`

## 📐 Stacks

front:
- Vuejs with nuxt (migration from react)

backend:
- Nodejs with express
- Database with knex 

mobile:
- swift and switf ui (soon)

monorepo:
- turbo-repo (no implemented)

## 📐 Architecture

> The backend server in node and express is implemented with a structure in clean architecture and a modelisation philosophie in DDD (domain, driven, design) 

struct
apps/
    agni-server/
        config/ here we but all configuration settings, di_contenaire
        controllers/ a interface to execute and inject our usecases
        core/ 
            adapters/ 
            domains/ our domain all core logic of the system that don't need automation, entities, value object etc..
            interactions/ the usecase to allow and add behavoir for our domains
            repositories/ interface for our repositories of data
        infra/ adpater implementation of external api, service to inject in core
    agni-web/
    agni-mobile/


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

