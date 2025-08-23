from typing import List, Optional
from pydantic import BaseModel

class GoalPlanningDto(BaseModel):
    id: str
    score: float
    suggest_amount: float
    reasoning: str


class AgentPlanningAdivsorOutput(BaseModel):
    goals: list[GoalPlanningDto]
    comment: str


class WishSpendPlanningDto(BaseModel):
    amount: float
    description: str

class GoalToPlanDto(BaseModel):
    uuid: str
    description: str
    target: float
    score: float
    current_balance: float
    desir_value: int
    importance: int
    wish_due_date: Optional[str] = None 

class WishGoalToTargetDto(BaseModel):
    goal_uuid: float
    amount: int

class AgentPlanningAdivsorInput(BaseModel):
    current_amount_in_investissment: float
    current_amount_in_saving: float
    percent_of_net_income_saving_and_investissment: float
    net_income: float
    amount_to_allocate: float
    comment: str
    goals: list[GoalToPlanDto]
    goals_i_want_to_target: list[WishGoalToTargetDto]
    wish_spends: list[WishSpendPlanningDto]