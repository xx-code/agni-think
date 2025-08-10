import { ListDto } from "@core/dto/base";
import { AccountRepository } from "../../repositories/accountRepository";
import { IUsecase } from "../interfaces";
import { mapperPeriod, Period, RecordType, TransactionStatus } from "@core/domains/constants";
import { TransactionFilter, TransactionRepository } from "@core/repositories/transactionRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { MomentDateService } from "@core/domains/entities/libs";


export type GetAllAccountWithPastBalanceDto = {
    accountId: string
    title: string
    balance: number
    pastBalance: number
    type: string
}

export type RequestGetAllAccountPastBalanceUseCase = {
    period: string 
    periodTime: number
}


export class GetAllAccountWithPastBalanceUseCase implements IUsecase<RequestGetAllAccountPastBalanceUseCase, ListDto<GetAllAccountWithPastBalanceDto>>{
    private repository: AccountRepository;
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository

    constructor(
        repo: AccountRepository, 
        transactionRepo: TransactionRepository,
        recordRepository: RecordRepository
    ) {
        this.repository = repo;
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepository;
    }

    async execute(request: RequestGetAllAccountPastBalanceUseCase): Promise<ListDto<GetAllAccountWithPastBalanceDto>> {
        let accounts = await this.repository.getAll();

        const { startDate, endDate } = MomentDateService.getDateByPeriod(mapperPeriod(request.period), request.periodTime)
        
        let accountsDisplay: GetAllAccountWithPastBalanceDto[] = [];
        for (let account of accounts) {

            const filter: TransactionFilter = {
                accounts: [account.getId()],
                startDate: startDate.toISOString(),
                endDate: endDate.toISOString(),
                categories: [],
                tags: [],
                budgets: [],
                types: []
            }

            let transactions = await this.transactionRepository.getTransactions(filter);

            let records = await this.recordRepository
                .getManyById(transactions.filter(i => i.getStatus() == TransactionStatus.COMPLETE)
                .map(transaction => transaction.getRecordRef()))

            let balance = 0

            for (let record of records) {
                if (record.getType() === RecordType.CREDIT)
                    balance += record.getMoney().getAmount()
                else 
                    balance -= record.getMoney().getAmount()
            }

            const pastbalance = Number(balance.toFixed(2))

            accountsDisplay.push({
                accountId: account.getId(), 
                title: account.getTitle(), 
                balance: account.getBalance(), 
                pastBalance: pastbalance,
                type: account.getType()
            });
        }

        return { items: accountsDisplay, totals: accountsDisplay.length}
    }
}