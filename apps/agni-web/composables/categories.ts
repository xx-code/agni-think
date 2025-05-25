export type CategoryType = {
    id: string,
    title: string,
    icon: string,
    color: string
}


export const useFetchCategories = (): Ref<CategoryType[]> => {
    const categories = ref([
        {
            id: 'cat-1',
            title: 'Epiceries',
            icon: 'i-lucide-shopping-cart',
            color: '#7cf54c'
        },
        {
            id: 'cat-2',
            title: 'Nourriture',
            icon: 'i-lucide-utensils-crossed',
            color: '#7b9aa6'
        },
        {
            id: 'cat-3',
            title: 'Salaire',
            icon: 'i-lucide-hand-coins',
            color: '#c781c2'
        },
    ])

    return categories
}
