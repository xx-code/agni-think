from enum import Enum

from pydantic import BaseModel, Field
from uuid import UUID
from datetime import datetime, date
from typing import List, Optional

class CreatedResponse(BaseModel):
    newId: UUID

class BudgetResponse(BaseModel):
    id: UUID
    title: str
    target: float
    currentBalance: float
    dueDate: datetime

class SavingGoalResponse(BaseModel):
    id: UUID
    title: str
    description: str
    target: float
    balance: float
    desirValue: int
    importance: int
    wishDueDate: Optional[date]

class NotificationRequest(BaseModel):
    title: str
    content: str

# Nested Helper Models for FinanceProfile
class AccountInfo(BaseModel):
    accountName: str
    accountType: str
    balance: float
    accountDetailRule: str
    isLiquidity: bool = Field(..., alias="liquidity")

class PrincipleToFollow(BaseModel):
    title: str
    ruleToFollow: str

class ComingTransaction(BaseModel):
    title: str
    amount: float
    dueDate: datetime

class IncomeSource(BaseModel):
    title: str
    type: str
    grossAmount: Optional[float]
    estimateNextNetAmount: float
    estimatedFutureOccurrences: int
    confidence: int

class FinanceProfileResponse(BaseModel):
    currentBalanceTotalWithFreeze: float
    currentCreditUtilization: str
    accountInfos: List[AccountInfo]
    principles: List[PrincipleToFollow]
    comingSpending: List[ComingTransaction]
    comingRevenue: List[ComingTransaction]
    incomeSources: List[IncomeSource]

    def to_str(self) -> str:
        return self.model_dump_json(indent=4)
    
class BankRegisterAccountLinkResponse(BaseModel):
    bankRegisterId: str
    accountId: UUID

class BankRegisterResponse(BaseModel):
    id: UUID
    title: str
    accessCode: str
    cursor: str
    active: bool
    accounts: List[BankRegisterAccountLinkResponse]

class ExternalTransactionRequest(BaseModel):
    accountId: str
    transactionId: str
    amount: float = 0
    dateTransaction: datetime
    merchantName: Optional[str] = "" 
    categoryPrimary: Optional[str] = ""
    categoryDetail: Optional[str] = ""
    isTreated: bool

class ExternalTransactionResponse(BaseModel): 
    id: UUID
    accountId: str
    amount: float
    dateTransaction: datetime
    merchantName: str
    categoryPrimary: str
    categoryDetail: str
    isTreated: bool = Field(..., alias="treated")

class DeductionResponse(BaseModel):
    id: UUID
    title: str
    description: str
    base: str
    mode: str

class TransactionRequest(BaseModel):
    amount: float
    categoryId: UUID
    description: str
    tagIds: List[str]
    budgetIds: List[str]

class TransactionDeductionRequest(BaseModel):
    deductionId: str
    amount: float

class InvoiceTypeRequestType(str, Enum):
    Income = "Income"
    FixedCost = "FixedCost"
    VariableCost = "VariableCost"
    Other = "Other"

class InvoiceStatusRequestType(str, Enum):
    Pending = "Pending"
    Complete = "Complete"

class InvoiceMouvmentRequestType(str, Enum):
    Credit = "Credit"
    Debit = "Debit"

class CreateInvoiceRequest(BaseModel):
    accountId: UUID
    type: InvoiceTypeRequestType 
    status: InvoiceMouvmentRequestType
    date: datetime
    mouvement: InvoiceMouvmentRequestType
    transactions: List[TransactionRequest]
    deductions: List[TransactionDeductionRequest]

class CategoryResponse(BaseModel):
    id: UUID
    title: str
    icon: str
    color: str
    isSystem: Optional[bool] = Field(..., alias="system") 

class TagResponse(BaseModel):
    id: str
    value: str
    color: Optional[str] 
    isSystem: bool = Field(..., alias="system")


class SpendByCategoryResponse(BaseModel):
    categoryId: UUID
    spend: float

class AnnualOutlookResponse(BaseModel):
    incomeOutlook: float
    spendOutlook: float
    budgetOutlook: float
    savingMargin: float
    currentIncomeOutlook: float
    currentSpendOutlook: float
    currentBudgetOutlook: float
    currentSaving: float
    spendByCategoriesOutlook: List[SpendByCategoryResponse]
    currentSpendByCategoryOutlook: List[SpendByCategoryResponse]

class BrokingDetailResponse(BaseModel):
    managementType: str
    contributionType: str

class CreditCardAccountResponse(BaseModel):
    creditCardLimit: float
    creditUtilisation: float
    nextInvoicePayment: date

class CheckingDetailResponse(BaseModel):
    buffer: float

class AccountDetailResponse(BaseModel):
    detailForCreditCard: Optional[CreditCardAccountResponse] 
    detailForBroking: Optional[BrokingDetailResponse] 
    detailForChecking: Optional[CheckingDetailResponse] 

class AccountResponse(BaseModel):
    id: str
    title: str
    balance: float
    type: str
    lockedBalance: float
    freezeBalance: float
    detail: AccountDetailResponse

class InternalLoanResponse(BaseModel):
    id: str
    creditTargetId: str
    invoiceId:str
    fundSourceId: str
    dueDate: date