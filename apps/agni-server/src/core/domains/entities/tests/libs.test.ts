import { searchInArray } from "../libs";

describe('Test search in array', () => {
    const values = [
        "title",
        "titan",
        "titlente",
        "titin",
        "broyer",
        "voyer",
        "Voeau",
        "String",
        "string_file"
    ];

    it('search tit', () => {
        let match_value = searchInArray('tit', values);

        let true_value = [
            "title",
            "titan",
            "titlente",
            "titin",
        ];

        expect(match_value).toStrictEqual(true_value);
    });

    it('search titl', () => {
        let match_value = searchInArray('titl', values);

        let true_value = [
            "title",
            "titlente"
        ];
        
        expect(match_value).toStrictEqual(true_value);
    });

    it('search title', () => {
        let match_value = searchInArray('title', values);

        let true_value = [
            "title",
            "titlente"
        ];
        
        expect(match_value).toStrictEqual(true_value);
    })

    it('search oyer', () => {
        let match_value = searchInArray('oyer', values);

        let true_value = [
            "broyer",
            "voyer"
        ];
        
        expect(match_value).toStrictEqual(true_value);
    })

    it('search nothing', () => {
        let match_value = searchInArray('', values);
        
        expect(match_value.length).toBe(9);

        match_value = searchInArray(' ', values);
        
        expect(match_value.length).toBe(0);
    })
})