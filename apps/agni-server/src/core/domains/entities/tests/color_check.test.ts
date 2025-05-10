import { isValidColor } from "@core/domains/helpers"

describe('Verify Color', () => {
    it('hex', () => {
        expect(isValidColor("#fff")).toBeTruthy()
        expect(isValidColor("#FFFFFF")).toBeTruthy()
        expect(isValidColor("#123456")).toBeTruthy()
        expect(isValidColor("#ff")).toBeFalsy()
        expect(isValidColor("#12345G")).toBeFalsy()
    })

    it('rgb', () => {
        expect(isValidColor("rgb(255, 0, 128)")).toBeTruthy()
        expect(isValidColor("rgb( 0 , 255 , 255 )")).toBeTruthy()
        expect(isValidColor("#123456")).toBeTruthy()
        expect(isValidColor("rgb(300, 0, 0)")).toBeFalsy()
        expect(isValidColor("rgb(255,0)")).toBeFalsy()
    })

    it('rgba', () => {
        expect(isValidColor("rgba(255, 0, 0, 0.5)")).toBeTruthy()
        expect(isValidColor("rgba(0, 255, 128, 1.0)")).toBeTruthy()
        expect(isValidColor("rgba(255,255,255,2)")).toBeFalsy()
        expect(isValidColor("rgba(255,255,255)")).toBeFalsy()
    })

    it('hsl', () => {
        expect(isValidColor("hsl(0, 100%, 50%)")).toBeTruthy()
        expect(isValidColor("hsl(360, 0%, 0%)")).toBeTruthy()
        expect(isValidColor("hsl(400, 50%, 50%)")).toBeFalsy()
        expect(isValidColor("hsl(120, 50, 50)")).toBeFalsy()
    })
})