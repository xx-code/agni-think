import { useRoute } from 'nuxt/app'

interface RouteInfo {
  title: string
  subtitle: string
}

const routeMap: Record<string, RouteInfo> = {
  '/': {
    title: 'Dashboard',
    subtitle: 'Overview of your finances'
  },
  '/transactions': {
    title: 'Transactions',
    subtitle: 'Manage your income and expenses'
  },
  '/wallets': {
    title: 'Wallets',
    subtitle: 'View and manage your wallets'
  },
  '/scheduletransactions': {
    title: 'Scheduled Transactions',
    subtitle: 'Manage recurring payments'
  },
  '/budgets': {
    title: 'Budgets',
    subtitle: 'Track your spending limits'
  },
  '/goals': {
    title: 'Goals',
    subtitle: 'Monitor your savings goals'
  },
  '/analytics': {
    title: 'Analytics',
    subtitle: 'Insights into your financial data'
  },
  '/chat-agent': {
    title: 'Personnal finance advisor',
    subtitle: 'Chat with you money guy'
  },
  '/provisions': {
    title: 'Provision',
    subtitle: 'Track provision stuff I want to upgrade'
  },
  '/patrimonies': {
    title: 'Patrimoine',
    subtitle: 'Track your net worth'
  },
  '/settings': {
    title: 'Settings',
    subtitle: 'Customize your experience'
  }
}

export const useCurrentRouteInfo = () => {
  const route = useRoute()
  
  return computed(() => {
    const path = route.path
    return routeMap[path] || {
      title: 'Page',
      subtitle: 'Welcome'
    }
  })
}