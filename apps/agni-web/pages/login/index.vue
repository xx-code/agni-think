<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui'
import type { LoginRequest } from '~/types/api/auth'

definePageMeta({
    layout: false
})

const { isLoading, start, stop } = useLoading()
const toast = useToast()
const env = getEnviromnent()
const { login } = useAuth()


const content = {
    PRODUCTION: {
        badge: null,
        title: 'Bon retour sur Agni',
        subtitle: 'Connectez-vous avec le compte de votre organisation.',
        button: 'Se connecter avec le SSO',
        icon: 'i-lucide-key-round',
        note: 'Vous serez redirigé vers votre fournisseur d’identité pour vous authentifier.'
    },
    DEMO: {
        badge: { label: 'Mode démo', color: 'warning' as const },
        title: 'Essayez Agni',
        subtitle: 'Créez un profil de test pour explorer l’application.',
        button: 'Commencer à tester',
        icon: 'i-lucide-flask-conical',
        note: 'Les données de la démo sont fictives et peuvent être réinitialisées à tout moment.'
    },
    DEVELOPMENT: {
        badge: { label: 'Développement', color: 'neutral' as const },
        title: 'Environnement de développement',
        subtitle: 'La connexion utilise automatiquement le profil de test.',
        button: 'Connexion',
        icon: 'i-lucide-terminal',
        note: null
    }
} as const

const current = computed(() => content[env as keyof typeof content])

const loginForm = reactive<LoginRequest>({
    externalId: '',
    username: '',
    email: ''
})

// Seul l'environnement DEMO demande une saisie
function validate(data: Partial<LoginRequest>): FormError[] {
    if (env !== 'DEMO') return []

    const errors: FormError[] = []

    if (!data.username?.trim())
        errors.push({ name: 'username', message: 'Entrez un nom d’utilisateur.' })

    if (!data.email?.trim())
        errors.push({ name: 'email', message: 'Entrez une adresse courriel.' })

    return errors
}

async function onSubmit(event: FormSubmitEvent<LoginRequest>) {
    start()
    const data = event.data
    try {
        switch (env) {
            case 'DEMO':
                data.externalId = crypto.randomUUID()
                break
            case 'DEVELOPMENT':
                data.externalId = 'dev'
                data.email = 'dev@dev.dev'
                data.username = 'Dev'
                break
        }
        await login(data)
    } catch (err: any) {
        toast.add({
            title: 'Connexion impossible',
            description: err?.message ?? 'Vérifiez vos informations et réessayez.',
            color: 'error',
            icon: 'i-lucide-circle-alert'
        })
    } finally {
        stop()
    }
}

// Aperçu décoratif (données fictives)
const previewAccounts = [
    { type: 'Compte courant', name: 'Principal', amount: 'CA$2,272.15', border: 'border-l-rose-500' },
    { type: 'Compte courant', name: 'Épargne', amount: 'CA$3,300.00', border: 'border-l-blue-500' },
    { type: 'Compte courant', name: 'Voyage', amount: 'CA$1,619.95', border: 'border-l-emerald-500' },
    { type: 'Carte de crédit', name: 'Visa', amount: '-CA$412.30', border: 'border-l-orange-500', negative: true }
]
</script>

<template>
    <main class="grid min-h-dvh w-full lg:grid-cols-2">
        <!-- Panneau gauche : identité + aperçu (desktop uniquement) -->
        <aside class="hidden flex-col bg-default p-12 lg:flex xl:p-16">
            <span class="text-2xl font-extrabold tracking-tight text-primary">Agni.</span>

            <div class="my-auto w-full max-w-xl">
                <h2 class="text-balance text-4xl font-bold leading-tight tracking-tight text-highlighted xl:text-5xl">
                    Toutes vos finances, en un coup d’œil.
                </h2>
                <p class="mt-4 max-w-md text-pretty text-lg text-muted">
                    Suivez vos comptes, vos budgets et vos factures à venir depuis un seul portefeuille.
                </p>

                <!-- Aperçu du tableau de bord -->
                <div
                    aria-hidden="true"
                    class="mt-12 select-none rounded-3xl bg-muted p-6 ring-1 ring-default"
                >
                    <div class="flex items-end justify-between">
                        <div>
                            <p class="text-sm font-semibold text-muted">Balance totale</p>
                            <p class="text-3xl font-bold tracking-tight text-highlighted">CA$690.90</p>
                        </div>
                        <span class="rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
                            Période en cours
                        </span>
                    </div>

                    <div class="mt-5 grid grid-cols-2 gap-3">
                        <div
                            v-for="(account, i) in previewAccounts"
                            :key="account.name"
                            class="rise rounded-2xl border-l-4 bg-default p-4 ring-1 ring-default"
                            :class="account.border"
                            :style="{ '--d': `${i * 90}ms` }"
                        >
                            <p class="text-xs text-muted">{{ account.type }}</p>
                            <p class="mt-0.5 font-semibold text-highlighted">{{ account.name }}</p>
                            <p
                                class="mt-2 text-lg font-semibold tabular-nums"
                                :class="account.negative ? 'text-error' : 'text-highlighted'"
                            >
                                {{ account.amount }}
                            </p>
                        </div>
                    </div>

                    <div class="mt-4 rounded-2xl bg-default p-4 ring-1 ring-default">
                        <div class="flex items-baseline justify-between text-sm">
                            <span class="font-semibold text-highlighted">Épicerie</span>
                            <span class="tabular-nums text-muted">CA$312 / CA$400</span>
                        </div>
                        <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-accented">
                            <div class="h-full w-[78%] rounded-full bg-primary" />
                        </div>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Panneau droit : connexion -->
        <section class="flex min-h-dvh items-center justify-center bg-muted px-6 py-12 sm:px-10 lg:min-h-0">
            <div class="w-full max-w-sm">
                <!-- Logo affiché seulement quand le panneau gauche est masqué -->
                <span class="mb-10 block text-2xl font-extrabold tracking-tight text-primary lg:hidden">
                    Agni.
                </span>

                <UBadge
                    v-if="current.badge"
                    :label="current.badge.label"
                    :color="current.badge.color"
                    variant="subtle"
                    class="mb-4"
                />

                <h1 class="text-balance text-2xl font-bold tracking-tight text-highlighted sm:text-3xl">
                    {{ current.title }}
                </h1>
                <p class="mt-2 text-pretty text-muted">
                    {{ current.subtitle }}
                </p>

                <UForm
                    :state="loginForm"
                    :validate="validate"
                    class="mt-8 space-y-5"
                    @submit="onSubmit"
                >
                    <!-- DEMO : saisie d'un profil de test -->
                    <template v-if="env === 'DEMO'">
                        <UFormField name="username" label="Nom d’utilisateur">
                            <UInput
                                v-model="loginForm.username"
                                class="w-full"
                                size="xl"
                                icon="i-lucide-user"
                                placeholder="Ex. : marie"
                                autocomplete="username"
                            />
                        </UFormField>

                        <UFormField name="email" label="Courriel">
                            <UInput
                                v-model="loginForm.email"
                                class="w-full"
                                size="xl"
                                type="email"
                                icon="i-lucide-mail"
                                placeholder="vous@exemple.com"
                                autocomplete="email"
                            />
                        </UFormField>
                    </template>

                    <!-- DEVELOPMENT : rappel du profil utilisé -->
                    <div
                        v-else-if="env === 'DEVELOPMENT'"
                        class="flex items-center gap-3 rounded-xl bg-default p-4 ring-1 ring-default"
                    >
                        <UIcon name="i-lucide-user-round-check" class="size-5 shrink-0 text-primary" />
                        <div class="min-w-0 text-sm">
                            <p class="font-medium text-highlighted">Dev</p>
                            <p class="truncate text-muted">dev@dev.dev</p>
                        </div>
                    </div>

                    <UButton
                        type="submit"
                        block
                        size="xl"
                        :loading="isLoading"
                        :disabled="isLoading"
                        :label="isLoading ? 'Connexion…' : current.button"
                        :leading-icon="isLoading ? undefined : current.icon"
                        :trailing-icon="isLoading ? undefined : 'i-lucide-arrow-right'"
                        class="group font-semibold"
                        :ui="{ trailingIcon: 'transition-transform duration-200 group-hover:translate-x-1' }"
                    />
                </UForm>

                <p v-if="current.note" class="mt-6 flex gap-2 text-sm text-muted">
                    <UIcon name="i-lucide-info" class="mt-0.5 size-4 shrink-0" />
                    <span>{{ current.note }}</span>
                </p>
            </div>
        </section>
    </main>
</template>

<style scoped>
/* Une seule entrée orchestrée : les cartes de l'aperçu apparaissent à la suite. */
@media (prefers-reduced-motion: no-preference) {
    .rise {
        animation: rise 0.6s cubic-bezier(0.2, 0.7, 0.2, 1) both;
        animation-delay: var(--d, 0ms);
    }

    @keyframes rise {
        from {
            opacity: 0;
            transform: translateY(12px);
        }
    }
}
</style>