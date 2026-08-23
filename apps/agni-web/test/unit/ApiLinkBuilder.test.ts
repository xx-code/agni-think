import { describe, it, expect, vi, afterEach } from 'vitest'
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder'
import type { ApiRouteDefinition } from '~/types/shared/routes'

const GET_ROUTE: ApiRouteDefinition = {
    serverPath: '/api/accounts/:id',
    apiPath: '/accounts/:id',
    method: 'GET'
}

describe('ApiLinkBuilder', () => {
    afterEach(() => {
        vi.unstubAllGlobals()
        vi.restoreAllMocks()
    })

    describe('route()', () => {
        it('creates a builder from a route definition', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder).toBeInstanceOf(ApiLinkBuilder)
        })

        it('uses serverPath as the request url', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder.buildOptions().url).toBe('/api/accounts/:id')
        })

        it('keeps the route method', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder.buildOptions().method).toBe('GET')
        })
    })

    describe('params()', () => {
        it('replaces a single param placeholder', () => {
            const url = ApiLinkBuilder
                .route(GET_ROUTE)
                .params({ id: 42 })
                .buildOptions()
                .url

            expect(url).toBe('/api/accounts/42')
        })

        it('replaces multiple param placeholders', () => {
            const route: ApiRouteDefinition = {
                serverPath: '/api/patrimonies/:id/snapshots/:snapId',
                apiPath: '/patrimonies/:id/snapshots/:snapId',
                method: 'GET'
            }

            const url = ApiLinkBuilder
                .route(route)
                .params({ id: 7, snapId: 'abc' })
                .buildOptions()
                .url

            expect(url).toBe('/api/patrimonies/7/snapshots/abc')
        })

        it('leaves placeholders untouched when no matching param is given', () => {
            const url = ApiLinkBuilder
                .route(GET_ROUTE)
                .buildOptions()
                .url

            expect(url).toBe('/api/accounts/:id')
        })

        it('does not modify routes without placeholders', () => {
            const route: ApiRouteDefinition = {
                serverPath: '/api/accounts',
                apiPath: '/accounts',
                method: 'POST'
            }

            const url = ApiLinkBuilder
                .route(route)
                .params({ id: 42 })
                .buildOptions()
                .url

            expect(url).toBe('/api/accounts')
        })

        it('is chainable', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder.params({ id: 1 })).toBe(builder)
        })
    })

    describe('body()', () => {
        it('sets the request body', () => {
            const data = { title: 'Groceries', target: 500 }
            const options = ApiLinkBuilder
                .route({ serverPath: '/api/budgets', apiPath: '/budgets', method: 'POST' })
                .body(data)
                .buildOptions()

            expect(options.body).toEqual(data)
        })

        it('is chainable', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder.body({})).toBe(builder)
        })
    })

    describe('query()', () => {
        it('sets the query params', () => {
            const query = { page: 2, limit: 10 }
            const options = ApiLinkBuilder
                .route(GET_ROUTE)
                .query(query)
                .buildOptions()

            expect(options.query).toEqual(query)
        })

        it('is chainable', () => {
            const builder = ApiLinkBuilder.route(GET_ROUTE)
            expect(builder.query({})).toBe(builder)
        })
    })

    describe('buildOptions()', () => {
        it('returns undefined body and query when not set', () => {
            const options = ApiLinkBuilder.route(GET_ROUTE).buildOptions()

            expect(options.url).toBe('/api/accounts/:id')
            expect(options.method).toBe('GET')
            expect(options.body).toBeUndefined()
            expect(options.query).toBeUndefined()
        })
    })

    describe('execute()', () => {
        it('calls $fetch with built options', async () => {
            const fetchMock = vi.fn().mockResolvedValue({ id: 42 })
            vi.stubGlobal('$fetch', fetchMock)

            await ApiLinkBuilder
                .route(GET_ROUTE)
                .params({ id: 42 })
                .execute()

            expect(fetchMock).toHaveBeenCalledOnce()
            expect(fetchMock).toHaveBeenCalledWith('/api/accounts/42', {
                method: 'GET',
                body: undefined,
                query: undefined
            })
        })

        it('passes body for POST requests', async () => {
            const fetchMock = vi.fn().mockResolvedValue({ ok: true })
            vi.stubGlobal('$fetch', fetchMock)

            const data = { title: 'Rent' }
            await ApiLinkBuilder
                .route({ serverPath: '/api/budgets', apiPath: '/budgets', method: 'POST' })
                .body(data)
                .execute()

            expect(fetchMock).toHaveBeenCalledWith('/api/budgets', {
                method: 'POST',
                body: data,
                query: undefined
            })
        })

        it('returns the raw response when no mapper is provided', async () => {
            const response = { id: 1, name: 'Checking' }
            vi.stubGlobal('$fetch', vi.fn().mockResolvedValue(response))

            const result = await ApiLinkBuilder.route(GET_ROUTE).execute()

            expect(result).toEqual(response)
        })

        it('applies the mapper to the response', async () => {
            const response = { id: 1, balance: 1234.56 }
            vi.stubGlobal('$fetch', vi.fn().mockResolvedValue(response))

            const result = await ApiLinkBuilder
                .route<{ id: number, balance: number }>(GET_ROUTE)
                .mapper(data => `Balance: ${data.balance}`)
                .execute()

            expect(result).toBe('Balance: 1234.56')
        })

        it('propagates $fetch errors', async () => {
            vi.stubGlobal('$fetch', vi.fn().mockRejectedValue(new Error('Network error')))

            await expect(
                ApiLinkBuilder.route(GET_ROUTE).execute()
            ).rejects.toThrow('Network error')
        })
    })
})
