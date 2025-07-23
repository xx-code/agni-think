import { Router } from "express"

export interface ApiController {
    setupRoutes: () => void
    getRoute: () => Router
}