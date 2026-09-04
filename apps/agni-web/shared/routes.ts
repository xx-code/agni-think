import type { ApiRouteDefinition } from "~/types/shared/routes";

export const API_ROUTES = {
    ACCOUNTS: {
        CREATE_ACCOUNT: {
            serverPath: '/api/accounts',
            apiPath: '/accounts',
            method: 'POST'
        },
        GET_ACCOUNTS: {
            serverPath: '/api/accounts',
            apiPath: '/accounts',
            method: 'GET'
        },
        GET_ACCOUNT: {
            serverPath: '/api/accounts/:id',
            apiPath: '/accounts/:id',
            method: 'GET'
        },
        UPDATE_ACCOUNT: {
            serverPath: '/api/accounts/:id',
            apiPath: '/accounts/:id',
            method: 'PUT'
        },
        DELETE_ACCOUNT: {
            serverPath: '/api/accounts/:id',
            apiPath: '/accounts/:id',
            method: 'DELETE'
        }
    },

    AGENTS: {
        CHAT: {
            serverPath: '/api/agents/chat',
            apiPath: '/chat',
            method: 'POST',
            backend: 'agent'
        },
        PLANNING_ADVISOR: {
            serverPath: '/api/agents/planningAdvisor',
            apiPath: '/analytics/save-goal-planning',
            method: 'GET'
        },
        GET_SUGGESTIONS: {
            serverPath: '/api/agents/suggestions',
            apiPath: '/agent-suggestions',
            method: 'GET',
            backend: 'agent'
        },
        CONFIRM_SUGGESTION: {
            serverPath: '/api/agents/suggestions/:id/confirm',
            apiPath: '/agent-suggestions/:id/confirm',
            method: 'POST',
            backend: 'agent'
        },
        TREAT_INVOICE: {
            serverPath: '/api/agents/treat-invoice',
            apiPath: '/treat-unformat-transaction',
            method: 'POST',
            backend: 'agent'
        }
    },

    ANALYTICS: {
        ANNUAL_OUTLOOK: {
            serverPath: '/api/analytics/annual-outlook',
            apiPath: '/analytics/annual-outlook',
            method: 'GET'
        },
        BUDGETING_RULE: {
            serverPath: '/api/analytics/budgeting-rule',
            apiPath: '/analytics/budgeting-rule',
            method: 'GET'
        },
        BUDGET_TOTAL_SUMMARY: {
            serverPath: '/api/analytics/budget-total-summary',
            apiPath: '/analytics/budget-total-summary',
            method: 'GET'
        },
        CASHFLOW: {
            serverPath: '/api/analytics/cashflow',
            apiPath: '/analytics/cashflow',
            method: 'GET'
        },
        ESTIMATION_LEFT_AMOUNT: {
            serverPath: '/api/analytics/estimation-left-amount',
            apiPath: '/analytics/estimation-left-amount',
            method: 'GET'
        },
        FUND_TOTAL_SUMMARY: {
            serverPath: '/api/analytics/fund-total-summary',
            apiPath: '/analytics/fund-total-summary',
            method: 'GET'
        },
        INCOMES: {
            serverPath: '/api/analytics/incomes',
            apiPath: '/analytics/incomes',
            method: 'GET'
        },
        PATRIMONY_EVOLUTION: {
            serverPath: '/api/analytics/patrimony-evolution',
            apiPath: '/analytics/patrimony-evolution',
            method: 'GET'
        },
        PATRIMONY_SUMMARY: {
            serverPath: '/api/analytics/patrimony-summary',
            apiPath: '/analytics/patrimony-summary',
            method: 'GET'
        },
        SAVINGS: {
            serverPath: '/api/analytics/savings',
            apiPath: '/analytics/savings',
            method: 'GET'
        },
        SPEND_CATEGORIES: {
            serverPath: '/api/analytics/spend-categories',
            apiPath: '/analytics/spend-categories',
            method: 'GET'
        },
        SPEND_TAGS: {
            serverPath: '/api/analytics/spend-tags',
            apiPath: '/analytics/spend-tags',
            method: 'GET'
        },
        PROVISION_SUMMARY: {
            serverPath: '/api/analytics/provision-summary',
            apiPath: '/analytics/provision-summary',
            method: 'GET'
        },
        SCHEDULE_INVOICE: {
            serverPath: "/api/analytics/schedule-invoice-summary",
            apiPath: "/analytics/schedule-invoice-summary",
            method: "GET"
        },
        FORCAST_SPENDING: {
            serverPath: '/api/analytics/forcast-spending', 
            apiPath: '/analytics/forcast-spending',
            method: 'POST'
        }
    },

    BANK: {
        GET_ACCOUNTS: {
            serverPath: '/api/bank/accounts',
            apiPath: '/bank_accounts',
            method: 'GET',
            backend: 'agent'
        },
        EXCHANGE_TOKEN: {
            serverPath: '/api/bank/exchange-token',
            apiPath: '/exchange-public-token',
            method: 'POST',
            backend: 'agent'
        },
        INIT_TRANSACTION: {
            serverPath: '/api/bank/init-transaction',
            apiPath: '/init-external-transactions',
            method: 'GET',
            backend: 'agent'
        },
        SYNC_TRANSACTION: {
            serverPath: '/api/bank/sync-transaction',
            apiPath: '/force-sync-transaction',
            method: 'GET',
            backend: 'agent'
        },
        CREATE_TOKEN: {
            serverPath: '/api/bank/token',
            apiPath: '/create-bank-link',
            method: 'POST',
            backend: 'agent'
        },
        GET_TRANSACTIONS: {
            serverPath: '/api/bank/transactions',
            apiPath: '/bank-transaction',
            method: 'GET',
            backend: 'agent'
        }
    },

    BANK_REGISTERS: {
        CREATE_BANK_REGISTER: {
            serverPath: '/api/bank-registers',
            apiPath: '/bank-registers',
            method: 'POST'
        },
        GET_BANK_REGISTERS: {
            serverPath: '/api/bank-registers',
            apiPath: '/bank-registers',
            method: 'GET'
        },
        UPDATE_BANK_REGISTER: {
            serverPath: '/api/bank-registers/:id',
            apiPath: '/bank-registers/:id',
            method: 'PUT'
        }
    },

    BUDGETS: {
        CREATE_BUDGET: {
            serverPath: '/api/budgets',
            apiPath: '/budgets',
            method: 'POST'
        },
        GET_BUDGETS: {
            serverPath: '/api/budgets',
            apiPath: '/budgets',
            method: 'GET'
        },
        GET_BUDGET: {
            serverPath: '/api/budgets/:id',
            apiPath: '/budgets/:id',
            method: 'GET'
        },
        UPDATE_BUDGET: {
            serverPath: '/api/budgets/:id',
            apiPath: '/budgets/:id',
            method: 'PUT'
        },
        DELETE_BUDGET: {
            serverPath: '/api/budgets/:id',
            apiPath: '/budgets/:id',
            method: 'DELETE'
        }
    },

    CATEGORIES: {
        CREATE_CATEGORY: {
            serverPath: '/api/categories',
            apiPath: '/categories',
            method: 'POST'
        },
        GET_CATEGORIES: {
            serverPath: '/api/categories',
            apiPath: '/categories',
            method: 'GET'
        },
        GET_CATEGORY: {
            serverPath: '/api/categories/:id',
            apiPath: '/categories/:id',
            method: 'GET'
        },
        UPDATE_CATEGORY: {
            serverPath: '/api/categories/:id',
            apiPath: '/categories/:id',
            method: 'PUT'
        },
        ARCHIVE_CATEGORY: {
            serverPath: '/api/categories/:id/archive',
            apiPath: '/categories/:id/archive',
            method: 'PUT'
        },
        DELETE_CATEGORY: {
            serverPath: '/api/categories/:id',
            apiPath: '/categories/:id',
            method: 'DELETE'
        }
    },

    CURRENCIES: {
        CREATE_CURRENCY: {
            serverPath: '/api/currencies',
            apiPath: '/currencies',
            method: 'POST'
        },
        GET_CURRENCIES: {
            serverPath: '/api/currencies',
            apiPath: '/currencies',
            method: 'GET'
        },
        GET_CURRENCY: {
            serverPath: '/api/currencies/:id',
            apiPath: '/currencies/:id',
            method: 'GET'
        },
        UPDATE_CURRENCY: {
            serverPath: '/api/currencies/:id',
            apiPath: '/currencies/:id',
            method: 'PUT'
        },
        DELETE_CURRENCY: {
            serverPath: '/api/currencies/:id',
            apiPath: '/currencies/:id',
            method: 'DELETE'
        }
    },

    DEDUCTIONS: {
        CREATE_DEDUCTION: {
            serverPath: '/api/deductions',
            apiPath: '/deductions',
            method: 'POST'
        },
        GET_DEDUCTIONS: {
            serverPath: '/api/deductions',
            apiPath: '/deductions',
            method: 'GET'
        },
        GET_DEDUCTION: {
            serverPath: '/api/deductions/:id',
            apiPath: '/deductions/:id',
            method: 'GET'
        },
        UPDATE_DEDUCTION: {
            serverPath: '/api/deductions/:id',
            apiPath: '/deductions/:id',
            method: 'PUT'
        },
        DELETE_DEDUCTION: {
            serverPath: '/api/deductions/:id',
            apiPath: '/deductions/:id',
            method: 'DELETE'
        }
    },

    FINANCE_PRINCIPLES: {
        CREATE_FINANCE_PRINCIPLE: {
            serverPath: '/api/finance-principles',
            apiPath: '/finance-principles',
            method: 'POST'
        },
        GET_FINANCE_PRINCIPLES: {
            serverPath: '/api/finance-principles',
            apiPath: '/finance-principles',
            method: 'GET'
        },
        GET_FINANCE_PRINCIPLE: {
            serverPath: '/api/finance-principles/:id',
            apiPath: '/finance-principles/:id',
            method: 'GET'
        },
        UPDATE_FINANCE_PRINCIPLE: {
            serverPath: '/api/finance-principles/:id',
            apiPath: '/finance-principles/:id',
            method: 'PUT'
        },
        DELETE_FINANCE_PRINCIPLE: {
            serverPath: '/api/finance-principles/:id',
            apiPath: '/finance-principles/:id',
            method: 'DELETE'
        }
    },

    FINANCE_REPORTS: {
        GET_FINANCE_REPORTS: {
            serverPath: '/api/finance-reports',
            apiPath: '/finance-reports',
            method: 'GET'
        },
        GET_FINANCE_REPORT: {
            serverPath: '/api/finance-reports/:id',
            apiPath: '/finance-reports/:id',
            method: 'GET'
        },
        DELETE_FINANCE_REPORT: {
            serverPath: '/api/finance-reports/:id',
            apiPath: '/finance-reports/:id',
            method: 'DELETE'
        }
    },

    FUNDS: {
        CREATE_FUND: {
            serverPath: '/api/funds',
            apiPath: '/funds',
            method: 'POST'
        },
        GET_FUNDS: {
            serverPath: '/api/funds',
            apiPath: '/funds',
            method: 'GET'
        },
        GET_FUND: {
            serverPath: '/api/funds/:id',
            apiPath: '/funds/:id',
            method: 'GET'
        },
        UPDATE_FUND: {
            serverPath: '/api/funds/:id',
            apiPath: '/funds/:id',
            method: 'PUT'
        },
        DECREASE_FUND: {
            serverPath: '/api/funds/:id/decrease',
            apiPath: '/funds/:id/decrease',
            method: 'PUT'
        },
        INCREASE_FUND: {
            serverPath: '/api/funds/:id/increase',
            apiPath: '/funds/:id/increase',
            method: 'PUT'
        },
        REMOVE_FUND: {
            serverPath: '/api/funds/:id/remove',
            apiPath: '/funds/:id/remove',
            method: 'PUT'
        }
    },

    GOALS: {
        CREATE_GOAL: {
            serverPath: '/api/goals',
            apiPath: '/goals',
            method: 'POST'
        },
        GET_GOALS: {
            serverPath: '/api/goals',
            apiPath: '/goals',
            method: 'GET'
        },
        GET_GOAL: {
            serverPath: '/api/goals/:id',
            apiPath: '/goals/:id',
            method: 'GET'
        },
        UPDATE_GOAL: {
            serverPath: '/api/goals/:id',
            apiPath: '/goals/:id',
            method: 'PUT'
        },
        DELETE_GOAL: {
            serverPath: '/api/goals/:id',
            apiPath: '/goals/:id',
            method: 'DELETE'
        }
    },

    INCOME_SOURCES: {
        CREATE_INCOME_SOURCE: {
            serverPath: '/api/income-sources',
            apiPath: '/income-sources',
            method: 'POST'
        },
        GET_INCOME_SOURCES: {
            serverPath: '/api/income-sources',
            apiPath: '/income-sources',
            method: 'GET'
        },
        GET_INCOME_SOURCE: {
            serverPath: '/api/income-sources/:id',
            apiPath: '/income-sources/:id',
            method: 'GET'
        },
        UPDATE_INCOME_SOURCE: {
            serverPath: '/api/income-sources/:id',
            apiPath: '/income-sources/:id',
            method: 'PUT'
        },
        DELETE_INCOME_SOURCE: {
            serverPath: '/api/income-sources/:id',
            apiPath: '/income-sources/:id',
            method: 'DELETE'
        }
    },

    INTERNAL_LOANS: {
        CREATE_INTERNAL_LOAN: {
            serverPath: '/api/internal-loans',
            apiPath: '/internal-loans',
            method: 'POST'
        },
        GET_INTERNAL_LOANS: {
            serverPath: '/api/internal-loans',
            apiPath: '/internal-loans',
            method: 'GET'
        },
        GET_INTERNAL_LOAN: {
            serverPath: '/api/internal-loans/:id',
            apiPath: '/internal-loans/:id',
            method: 'GET'
        },
        UPDATE_INTERNAL_LOAN: {
            serverPath: '/api/internal-loans/:id',
            apiPath: '/internal-loans/:id',
            method: 'PUT'
        },
        DELETE_INTERNAL_LOAN: {
            serverPath: '/api/internal-loans/:id',
            apiPath: '/internal-loans/:id',
            method: 'DELETE'
        },
        ADD_FUND: {
            serverPath: '/api/internal-loans/:id/add-fund',
            apiPath: '/internal-loans/:id/add-fund',
            method: 'PUT'
        },
        REMOVE_FUND: {
            serverPath: '/api/internal-loans/:id/remove-fund',
            apiPath: '/internal-loans/:id/remove-fund',
            method: 'PUT'
        }
    },

    INTERNALS: {
        ACCOUNT_TYPE: {
            serverPath: '/api/internals/account-type',
            apiPath: '/internals/account-type',
            method: 'GET'
        },
        CONTRIBUTION_TYPE: {
            serverPath: '/api/internals/contribution-type',
            apiPath: '/internals/contribution-type',
            method: 'GET'
        },
        FINANCE_POLICY_RISK_TYPE: {
            serverPath: '/api/internals/finance-policy-risk-type',
            apiPath: '/internals/finance-policy-risk-type',
            method: 'GET'
        },
        IMPORTANCE_TYPE: {
            serverPath: '/api/internals/importance-type',
            apiPath: '/internals/importance-type',
            method: 'GET'
        },
        INCOME_SOURCE_FREQUENCY_TYPE: {
            serverPath: '/api/internals/income-source-frequency-type',
            apiPath: '/internals/income-source-frequency-type',
            method: 'GET'
        },
        INCOME_SOURCE_TYPE: {
            serverPath: '/api/internals/income-source-type',
            apiPath: '/internals/income-source-type',
            method: 'GET'
        },
        INTENSITY_DESIR_TYPE: {
            serverPath: '/api/internals/intensity-desir-type',
            apiPath: '/internals/intensity-desir-type',
            method: 'GET'
        },
        MANAGEMENT_ACCOUNT_TYPE: {
            serverPath: '/api/internals/management-account-type',
            apiPath: '/internals/management-account-type',
            method: 'GET'
        },
        PERIOD_TYPE: {
            serverPath: '/api/internals/period-type',
            apiPath: '/internals/period-type',
            method: 'GET'
        },
        PRINCIPLE_TYPE: {
            serverPath: '/api/internals/principle-type',
            apiPath: '/internals/principle-type',
            method: 'GET'
        },
        PRIORITY_RULE_LEVEL_TYPE: {
            serverPath: '/api/internals/priority-rule-level-type',
            apiPath: '/internals/priority-rule-level-type',
            method: 'GET'
        },
        TRANSACTION_TYPE: {
            serverPath: '/api/internals/transaction-type',
            apiPath: '/internals/transaction-type',
            method: 'GET'
        }
    },

    INVOICES: {
        GET_INVOICES: {
            serverPath: '/api/invoices',
            apiPath: '/invoices',
            method: 'GET'
        },
        CREATE_INVOICE: {
            serverPath: '/api/invoices',
            apiPath: '/invoices',
            method: 'POST'
        },
        GET_INVOICE: {
            serverPath: '/api/invoices/:id',
            apiPath: '/invoices/:id',
            method: 'GET'
        },
        UPDATE_INVOICE: {
            serverPath: '/api/invoices/:id',
            apiPath: '/invoices/:id',
            method: 'PUT'
        },
        DELETE_INVOICE: {
            serverPath: '/api/invoices/:id',
            apiPath: '/invoices/:id',
            method: 'DELETE'
        },
        COMPLETE_INVOICE: {
            serverPath: '/api/invoices/:id/completed',
            apiPath: '/invoices/:id/completed',
            method: 'PUT'
        },
        GET_BALANCES: {
            serverPath: '/api/invoices/balances',
            apiPath: '/invoices/balances',
            method: 'GET'
        },
        GET_BALANCES_BY_PERIOD: {
            serverPath: '/api/invoices/balances-by-period',
            apiPath: '/invoices/balances-by-period',
            method: 'GET'
        },
        CREATE_FREEZE: {
            serverPath: '/api/invoices/create-freeze',
            apiPath: '/invoices/create-freeze',
            method: 'POST'
        },
        TRANSFER: {
            serverPath: '/api/invoices/transfer',
            apiPath: '/invoices/transfer',
            method: 'POST'
        }
    },

    LLM: {
        GET_MODELS: {
            serverPath: '/api/llm/models',
            apiPath: '/models',
            method: 'GET',
            backend: 'agent'
        }
    },

    NOTIFICATIONS: {
        GET_NOTIFICATIONS: {
            serverPath: '/api/notifications',
            apiPath: '/notifications',
            method: 'GET'
        },
        DELETE_NOTIFICATION: {
            serverPath: '/api/notifications/:id',
            apiPath: '/notifications/:id',
            method: 'DELETE'
        },
        TOGGLE_READ: {
            serverPath: '/api/notifications/:id/toggle-read',
            apiPath: '/notifications/:id/toggle-read',
            method: 'PUT'
        }
    },

    PATRIMONIES: {
        CREATE_PATRIMONY: {
            serverPath: '/api/patrimonies',
            apiPath: '/patrimonies',
            method: 'POST'
        },
        GET_PATRIMONIES: {
            serverPath: '/api/patrimonies',
            apiPath: '/patrimonies',
            method: 'GET'
        },
        GET_PATRIMONY: {
            serverPath: '/api/patrimonies/:id',
            apiPath: '/patrimonies/:id',
            method: 'GET'
        },
        UPDATE_PATRIMONY: {
            serverPath: '/api/patrimonies/:id',
            apiPath: '/patrimonies/:id',
            method: 'PUT'
        },
        DELETE_PATRIMONY: {
            serverPath: '/api/patrimonies/:id',
            apiPath: '/patrimonies/:id',
            method: 'DELETE'
        },
        ADD_SNAPSHOT: {
            serverPath: '/api/patrimonies/:id/add-snapshot',
            apiPath: '/patrimonies/:id/add-snapshot',
            method: 'POST'
        },
        GET_SNAPSHOTS: {
            serverPath: '/api/patrimonies/:id/snapshots',
            apiPath: '/patrimonies/:id/snapshots',
            method: 'GET'
        },
        REMOVE_SNAPSHOT: {
            serverPath: '/api/patrimonies/remove-snapshot/:id',
            apiPath: '/patrimonies/remove-snapshot/:id',
            method: 'PUT'
        },
        TOTAL_FUND: {
            serverPath: '/api/patrimonies/total-fund',
            apiPath: '/patrimonies/total-fund',
            method: 'GET'
        },
        UPDATE_SNAPSHOT: {
            serverPath: '/api/patrimonies/update-snapshot/:id',
            apiPath: '/patrimonies/update-snapshot/:id',
            method: 'PUT'
        }
    },

    PROVISIONS: {
        CREATE_PROVISION: {
            serverPath: '/api/provisions',
            apiPath: '/provisions',
            method: 'POST'
        },
        GET_PROVISIONS: {
            serverPath: '/api/provisions',
            apiPath: '/provisions',
            method: 'GET'
        },
        GET_PROVISION: {
            serverPath: '/api/provisions/:id',
            apiPath: '/provisions/:id',
            method: 'GET'
        },
        UPDATE_PROVISION: {
            serverPath: '/api/provisions/:id',
            apiPath: '/provisions/:id',
            method: 'PUT'
        },
        DELETE_PROVISION: {
            serverPath: '/api/provisions/:id',
            apiPath: '/provisions/:id',
            method: 'DELETE'
        }
    },

    SCHEDULE_INVOICES: {
        CREATE_SCHEDULE_INVOICE: {
            serverPath: '/api/schedule-invoices',
            apiPath: '/schedule-invoices',
            method: 'POST'
        },
        GET_SCHEDULE_INVOICES: {
            serverPath: '/api/schedule-invoices',
            apiPath: '/schedule-invoices',
            method: 'GET'
        },
        GET_SCHEDULE_INVOICE: {
            serverPath: '/api/schedule-invoices/:id',
            apiPath: '/schedule-invoices/:id',
            method: 'GET'
        },
        UPDATE_SCHEDULE_INVOICE: {
            serverPath: '/api/schedule-invoices/:id',
            apiPath: '/schedule-invoices/:id',
            method: 'PUT'
        },
        DELETE_SCHEDULE_INVOICE: {
            serverPath: '/api/schedule-invoices/:id',
            apiPath: '/schedule-invoices/:id',
            method: 'DELETE'
        }
    },

    TAGS: {
        CREATE_TAG: {
            serverPath: '/api/tags',
            apiPath: '/tags',
            method: 'POST'
        },
        GET_TAGS: {
            serverPath: '/api/tags',
            apiPath: '/tags',
            method: 'GET'
        },
        GET_TAG: {
            serverPath: '/api/tags/:id',
            apiPath: '/tags/:id',
            method: 'GET'
        },
        UPDATE_TAG: {
            serverPath: '/api/tags/:id',
            apiPath: '/tags/:id',
            method: 'PUT'
        },
        DELETE_TAG: {
            serverPath: '/api/tags/:id',
            apiPath: '/tags/:id',
            method: 'DELETE'
        }
    }
} satisfies Record<string, Record<string, ApiRouteDefinition>>
