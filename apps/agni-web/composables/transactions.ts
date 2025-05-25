export type RowTransactionType = {
    id: string
    title: string 
    description: string
    date: string
    icon: string
    amount: number
    tags: {color: string, title: string}[]
}

export type PrincipalCategory = {
    id: string
    label: string
}

export const useListTransactions = (page: number, size: number): Ref<RowTransactionType[]> => {
    const offset = page - 1
    const fetchTransactions: RowTransactionType[] = [
        {
            id: "tra-1",
            title: "Transaction mock1",
            description: "description mock1",
            date: "12/03/2006",
            icon: 'fa-solid fa-cart-shopping',
            amount: 152,
            tags: []
        },
        {
            id: "tra-2",
            title: "Transaction mock2",
            description: "description mock1",
            date: "12/03/2006",
            icon: 'fa-solid fa-cart-shopping',
            amount: 15,
            tags: []
        },
        {
            id: "tra-3",
            title: "Transaction mock3",
            description: "description mock3",
            date: "12/03/2006",
            icon: 'fa-solid fa-cart-shopping',
            amount: -102,
            tags: [{title: 'tag1', color: '#FFCBDD'}, 
                {title: 'long long tag', color: '#A755C2'}]
        },
        {
            id: "tra-4",
            title: "Transaction mock1",
            description: "description mock1",
            date: "12/03/2006",
            icon: 'fa-solid fa-cart-shopping',
            amount: 36,
            tags: []
        },
        {
            id: "tra-5",
            title: "Transaction mock5",
            description: "description mock5",
            icon: 'fa-solid fa-cart-shopping',
            date: "12/03/2006",
            amount: -41,
            tags: []
        },
        {
            id: "tra-6",
            title: "Transaction va mock6",
            description: "description mock6",
            icon: 'fa-solid fa-cart-shopping',
            date: "12/03/2006",
            amount: 88,
            tags: []
        },
        {
            id: "tra-6",
            title: "Transaction mock1",
            description: "description mock1",
            date: "12/03/2006",
            icon: 'fa-solid fa-cart-shopping',
            amount: -16.5,
            tags: []
        },
    ]
    
    const trans = ref(fetchTransactions.slice(0, size))

    return trans
} 

export const useFetchMainCategories = (): Ref<PrincipalCategory[]> => {
    const categories = ref([
        {
            id: 'cat-diver',
            label: 'Cout divers'
        },
        {
            id: 'cat-fix',
            label: 'Cout fix'
        },
        {
            id: 'cat-variable',
            label: 'Cout variable'
        },
    ])

    return categories
}