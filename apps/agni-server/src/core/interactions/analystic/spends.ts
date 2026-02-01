import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { mapperPeriod, Period, RecordType, TransactionType } from "@core/domains/constants";
import UnExpectedError from "@core/errors/unExpectedError";
import { MomentDateService } from "@core/domains/entities/libs";
import { Category } from "@core/domains/entities/category";
import { Tag } from "@core/domains/entities/tag";

export type RequestSpendAnalystic = {
    period: string
    periodTime: number
    showNumber: number
}

type SpendTag = {
    tagId: string
    spend: number
}

type SpendCategory = {
    categoryId: string
    spend: number
    spendBytag: SpendTag[]
}

export type SpendAnalysticResponse = {
    totalSpends: number[] 
    spendByCategories: SpendCategory[][] 
}

export class SpendAnalysticUseCase implements IUsecase<RequestSpendAnalystic, SpendAnalysticResponse> {
    private recordRepo: Repository<Record>
    private transactionRepo: Repository<Transaction>
    private categoryRepo: Repository<Category>
    private tagRepo: Repository<Tag>

    constructor(
        recordRepo: Repository<Record>,
        transactionRepo: Repository<Transaction>,
        categoryRepo: Repository<Category>,
        tagRepo: Repository<Tag>
    ) {
        this.recordRepo = recordRepo
        this.transactionRepo = transactionRepo
        this.categoryRepo = categoryRepo
        this.tagRepo = tagRepo
    }

    async execute(request: RequestSpendAnalystic): Promise<SpendAnalysticResponse> {
        const period = mapperPeriod(request.period)
        if (period === Period.DAY || (period === Period.WEEK && request.periodTime === 1))
            throw new UnExpectedError("BAD_PERIOD_SELECTION")

        if (request.showNumber <= 0)
            throw new UnExpectedError("SHOWNUMBER_MUST_BE_GREATER_THAN_)")

        const today = new Date(Date.now()) 
        const totals: number[] = []
        const spends: SpendCategory[][] = []

        const categories = await this.categoryRepo.getAll({offset: 0, limit: 0, queryAll: true})
        const tags = await this.tagRepo.getAll({offset: 0, limit: 0, queryAll: true})

        for(let i = request.showNumber; i > 0; i--) {
            const beginDate = MomentDateService.getUTCDateSubstraction(today, period, 
                (request.periodTime * (i - 1))) 

            const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
            const extendFilter = new TransactionFilter()
            extendFilter.startDate = startDate
            extendFilter.endDate = endDate
            extendFilter.types = [TransactionType.VARIABLECOST, TransactionType.FIXEDCOST]
            const transactions = await this.transactionRepo.getAll({limit: 0, offset: 0, queryAll: true}, extendFilter)

            const recordExtendFilter = new RecordFilter()
            recordExtendFilter.transactionIds = transactions.items.map(i => i.getId()) 
            let records = await this.recordRepo.getAll({offset: 0, limit: 0, queryAll: true}, recordExtendFilter)
            const totalAmount = records.items.filter(i => i.getType() === RecordType.DEBIT)
                .reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)

            // TODO: Heavy work
            let spendByCategories: SpendCategory[] = []
            categories.items.forEach(category => {
                const recordCats = records.items.filter(rec => rec.getCategoryRef() === category.getId())
                const totalAmountCat = recordCats.reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)
                
                const spendByTags: SpendTag[] = []
                tags.items.forEach(tag => {
                    const recordTags = recordCats.filter(rec => rec.getTags().includes(tag.getId()))
                    const totalAmountRecord = recordTags.reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0) 
                    spendByTags.push({
                        tagId: tag.getId(),
                        spend: totalAmountRecord
                    }) 
                })

                spendByCategories.push({
                    categoryId: category.getId(),
                    spend: totalAmountCat,
                    spendBytag: spendByTags 
                })
            })


            totals.push(totalAmount)
            spends.push(spendByCategories) 
        }

        return {
            totalSpends: totals,
            spendByCategories: spends
        }
    }
}