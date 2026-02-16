from pydantic import BaseModel, Field
from uuid import UUID
from datetime import datetime, date
from typing import List, Optional

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
    accountInfos: List[AccountInfo]
    principles: List[PrincipleToFollow]
    comingSpending: List[ComingTransaction]
    comingRevenue: List[ComingTransaction]
    incomeSources: List[IncomeSource]

    def to_str(self) -> str:
        return self.model_dump_json(indent=4)