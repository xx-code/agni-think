import type { CreateFundRequest } from "../api/fund"

export type FundForm = Omit<CreateFundRequest, 'id'>