# E-Banking 3.0 - Modern Angular Frontend Platform

A premium, production-ready E-Banking frontend application built with Angular, featuring modern UI/UX design, dark/light themes, and scalable architecture prepared for GraphQL integration.

![Angular](https://img.shields.io/badge/Angular-19.2-red)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)
![Material](https://img.shields.io/badge/Material-19.2-purple)
![License](https://img.shields.io/badge/License-MIT-green)

## 🎯 Project Overview

E-Banking 3.0 is a sophisticated frontend-only banking application showcasing modern web development practices with a focus on FinTech aesthetics. The application demonstrates:

- ✨ **Premium UI/UX** - Glassmorphism, soft gradients, and elegant animations
- 🎨 **Theme System** - Complete dark/light mode support with smooth transitions
- 🏗️ **Scalable Architecture** - Clean folder structure ready for enterprise growth
- 🔒 **Authentication Flow** - Login with MFA (Multi-Factor Authentication)
- 📊 **Rich Dashboard** - Interactive widgets displaying financial data
- 🚀 **Performance Optimized** - Standalone components with lazy loading support
- 📱 **Fully Responsive** - Works seamlessly on desktop, tablet, and mobile

## 🛠️ Tech Stack

### Core Technologies
- **Angular 19.2** - Latest version with standalone components
- **TypeScript** - Strongly typed development
- **SCSS** - Advanced styling with custom theme system
- **Angular Material 19.2** - UI component library with custom theming

### Architecture Patterns
- Smart/Dumb component separation
- Service-based state management
- Route guards for authentication
- Reactive programming with RxJS
- Type-safe models and interfaces

## 📁 Project Structure

```
ebanking-app/
├── src/
│   ├── app/
│   │   ├── core/                      # Singleton services and guards
│   │   │   ├── guards/                # Route guards (auth, admin)
│   │   │   └── services/              # Core services (auth, theme, etc.)
│   │   │       ├── auth.service.ts
│   │   │       ├── theme.service.ts
│   │   │       ├── account.service.ts
│   │   │       ├── transaction.service.ts
│   │   │       ├── notification.service.ts
│   │   │       └── graphql.service.ts  # GraphQL placeholder
│   │   │
│   │   ├── shared/                    # Reusable components
│   │   │   └── components/
│   │   │       ├── sidebar/           # Collapsible navigation
│   │   │       ├── navbar/            # Top navigation bar
│   │   │       ├── page-header/       # Page title with breadcrumbs
│   │   │       ├── info-card/         # Stats & balance cards
│   │   │       ├── transaction-item/  # Transaction list item
│   │   │       ├── chart-widget/      # Chart placeholder
│   │   │       ├── custom-button/     # Styled button component
│   │   │       ├── loader/            # Loading spinner
│   │   │       └── notification-bell/ # Notifications dropdown
│   │   │
│   │   ├── layouts/                   # Layout components
│   │   │   ├── main-layout/           # Main app layout (sidebar + navbar)
│   │   │   └── auth-layout/           # Authentication layout
│   │   │
│   │   ├── pages/                     # Feature pages
│   │   │   ├── login/                 # Login page
│   │   │   ├── mfa/                   # Multi-factor authentication
│   │   │   ├── dashboard/             # Main dashboard
│   │   │   └── accounts/              # Accounts overview
│   │   │
│   │   ├── models/                    # TypeScript interfaces
│   │   │   ├── user.model.ts
│   │   │   ├── account.model.ts
│   │   │   ├── transaction.model.ts
│   │   │   └── notification.model.ts
│   │   │
│   │   ├── themes/                    # SCSS theme system
│   │   │   ├── _variables.scss        # Design tokens
│   │   │   ├── _mixins.scss           # Reusable mixins
│   │   │   ├── light-theme.scss       # Light mode colors
│   │   │   └── dark-theme.scss        # Dark mode colors
│   │   │
│   │   ├── app.routes.ts              # Application routing
│   │   └── app.component.ts           # Root component
│   │
│   ├── assets/                        # Static assets
│   └── styles.scss                    # Global styles
│
├── angular.json                       # Angular CLI configuration
├── tsconfig.json                      # TypeScript configuration
└── package.json                       # Dependencies
```

## 🚀 Getting Started

### Prerequisites

- **Node.js** (v18 or higher)
- **npm** (v9 or higher)
- **Angular CLI** (v19.2 or higher)

### Installation

1. **Navigate to the project directory:**
   ```bash
   cd c:\Users\Hp\Desktop\front\ebanking-app
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   ng serve
   ```

4. **Open your browser:**
   Navigate to `http://localhost:4200`

5. **Login credentials (mock):**
   - Any email/password combination will work
   - MFA code: Any 6-digit number (e.g., 123456)

## 🎨 Features

### Authentication
- **Login Page** - Clean, modern login form with validation
- **MFA (Two-Factor Authentication)** - 6-digit code verification
- **Route Guards** - Protected routes for authenticated users
- **Mock Authentication** - No real backend required

### Dashboard
- **Account Summary Cards** - Total balance, income, expenses, crypto assets
- **Quick Actions** - Transfer money, pay bills, view accounts
- **Accounts Overview** - Display of all bank accounts
- **Recent Transactions** - Latest financial activities
- **Crypto Portfolio** - Cryptocurrency holdings with 24h changes
- **Chart Widgets** - Placeholders ready for chart library integration

### Shared Components
- **Sidebar** - Role-based collapsible navigation
- **Navbar** - Profile menu, notifications, theme toggle
- **Info Cards** - Reusable cards with trends and icons
- **Transaction Items** - Formatted transaction display
- **Notification Bell** - Dropdown with unread count

### Theme System
- **Dark/Light Modes** - Complete theme switching
- **Glassmorphism Effects** - Modern translucent design
- **Soft Gradients** - Premium color schemes
- **Smooth Animations** - Polished transitions
- **CSS Custom Properties** - Dynamic theme variables

## 🎯 Design System

### Color Palette
- **Primary:** Indigo (#4F46E5 - #6366F1)
- **Secondary:** Green (#16A34A - #22C55E)
- **Accent:** Amber (#D97706 - #F59E0B)
- **Success:** #10B981
- **Warning:** #F59E0B
- **Error:** #EF4444

### Typography
- **Font Family:** Inter (sans-serif)
- **Font Weights:** 300, 400, 500, 600, 700, 800
- **Responsive sizing** with mobile-first approach

### Spacing Scale
- XS: 4px, SM: 8px, MD: 16px, LG: 24px, XL: 32px, 2XL: 48px

## 🔌 GraphQL Integration (Prepared)

The application includes a `GraphqlService` placeholder ready for Apollo Client integration:

```typescript
// Future implementation example:
const GET_ACCOUNTS = gql`
  query GetAccounts($userId: ID!) {
    accounts(userId: $userId) {
      id
      accountNumber
      balance
      currency
    }
  }
`;
```

### Integration Steps:
1. Install Apollo Angular: `npm install @apollo/client @apollo/angular graphql`
2. Configure Apollo Client in `app.config.ts`
3. Replace mock services with GraphQL queries
4. Implement error handling and caching strategies

## 📱 Responsive Design

The application is fully responsive with breakpoints:
- **Mobile:** 375px - 640px
- **Tablet:** 641px - 1024px
- **Desktop:** 1025px+

## 🧪 Mock Data

All data is mocked using in-memory services:
- **User accounts** - 3 different account types
- **Transactions** - Recent financial activities
- **Notifications** - System alerts and messages
- **Crypto assets** - Bitcoin, Ethereum, USDT

## 🔒 Security Features

- Route guards protecting authenticated pages
- Token-based authentication (mock implementation)
- Role-based access control ready
- Secure form handling

## 🚧 Future Enhancements

### Pages to Add:
- [ ] Transactions history with filters
- [ ] Payments & Transfers page
- [ ] Crypto Wallet detailed view
- [ ] Analytics dashboard with charts
- [ ] Notifications center
- [ ] User profile & security settings
- [ ] Admin dashboard
- [ ] Audit logs

### Technical Improvements:
- [ ] Integrate real GraphQL backend
- [ ] Add chart library (ApexCharts recommended)
- [ ] Implement form validation with reactive forms
- [ ] Add unit and e2e tests
- [ ] Implement state management (NgRx/Signal Store)
- [ ] Add PWA capabilities
- [ ] Implement real-time notifications

## 📝 Development Guidelines

### Component Creation
```bash
ng generate component pages/your-page --skip-tests
```

### Service Creation
```bash
ng generate service core/services/your-service
```

### Styling Best Practices
- Use SCSS variables from `themes/_variables.scss`
- Apply mixins for common patterns (glassmorphism, shadows)
- Maintain consistency with existing design tokens
- Use CSS custom properties for theme switching

### Code Style
- Follow Angular style guide
- Use TypeScript strict mode
- Prefer standalone components
- Use signals for reactive state
- Document complex logic

## 🤝 Contributing

This is a demonstration project showcasing modern Angular development practices. Feel free to:
- Fork and extend the functionality
- Use as a learning resource
- Adapt for your own projects
- Provide feedback and suggestions

## 📄 License

MIT License - feel free to use this project for learning or as a starting point for your own applications.

## 🙏 Acknowledgments

- Angular Team for the fantastic framework
- Material Design for the component library
- Inter font family for beautiful typography
- Community for inspiration and best practices

---

**Built with ❤️ using Angular 19.2 | January 2026**
