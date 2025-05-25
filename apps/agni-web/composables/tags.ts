export type TagType = {
    id: string,
    value: string,
    color: string
}


export const useFetchTags = (): Ref<TagType[]> => {
    const tag = ref([
        {
            id: 'tag-1',
            value: 'tag-test',
            color: '#f5b078'
        },
        {
            id: 'tag-2',
            value: 'fastfood',
            color: '#f5ea78'
        }
    ]);
    return tag;
}