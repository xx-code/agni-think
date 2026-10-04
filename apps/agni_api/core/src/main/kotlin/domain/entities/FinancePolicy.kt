package domain.entities

import domain.enums.FinancePolicyRiskLevelType
import domain.enums.PriorityRuleLevelType
import java.util.UUID


class FinancePolicy (
    id: UUID,
    private val riskTolerance: FinancePolicyRiskLevelType,
    private val priorityRule: PriorityRuleLevelType,
    private val impulseDelayDays: Int,
    private val automationLevel: Double, // 0.0 (manuel) to 1 Auto
    private val intent: String
) : Entity(id)