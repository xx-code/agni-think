package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.InternalModelOutput
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.entities.enums.ContributionAccountType
import dev.auguste.agni_api.core.entities.enums.FinancePolicyRiskLevelType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import dev.auguste.agni_api.core.entities.enums.ManagementAccountType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.entities.enums.PrincipleType
import dev.auguste.agni_api.core.entities.enums.PriorityRuleLevelType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v2/internal")
class InternalController {

    @GetMapping("/account-type")
    fun getAccountTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput("Checking", "Compte courant"),
            InternalModelOutput("Saving", "Epargne"),
            InternalModelOutput("Broking", "Investissement"),
            InternalModelOutput("Business", "Pro"),
            InternalModelOutput("CreditCard", "Carte Credit")
        ))
    }

    @GetMapping("/period-type")
    fun getPeriodTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput("Day", "Jour"),
            InternalModelOutput("Week", "Semaine"),
            InternalModelOutput("Month", "Mois"),
            InternalModelOutput("Year", "Année")
        ))
    }

    @GetMapping("/transaction-type")
    fun getTransactionTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput("FixedCost", "Depense Fix"),
            InternalModelOutput("Income", "Gains"),
            InternalModelOutput("Other", "Autre"),
            InternalModelOutput("VariableCost", "Depense Variable")
        ))
    }

    @GetMapping("/intensity-desir-type")
    fun getIntensityDesirTypes(): ResponseEntity<List<InternalModelOutput>> {
        // Note: Using toString() if your InternalModelOutput.id is a String
        return ResponseEntity.ok(listOf(
            InternalModelOutput("0", "Indifferent"),
            InternalModelOutput("1", "Plaisir"),
            InternalModelOutput("2", "Desire"),
            InternalModelOutput("3", "Obsession"),
            InternalModelOutput("4", "Fomo")
        ))
    }

    @GetMapping("/importance-type")
    fun getImportanceTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput("1", "Insignifiant"),
            InternalModelOutput("2", "Normal"),
            InternalModelOutput("3", "Important"),
            InternalModelOutput("4", "Urgent")
        ))
    }

    @GetMapping("/contribution-type")
    fun getContributionTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(ContributionAccountType.REGISTERED.value, "Enregisterer"),
            InternalModelOutput(ContributionAccountType.UNREGISTERED.value, "Unregister")
        ))
    }

    @GetMapping("/management-account-type")
    fun getManagementAccountTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(ManagementAccountType.MANAGED.value, "Manager"),
            InternalModelOutput(ManagementAccountType.ROBOT.value, "Robot"),
            InternalModelOutput(ManagementAccountType.SELF_DIRECTED.value, "Gerer")
        ))
    }

    @GetMapping("/priority-rule-level-type")
    fun getPriorityRulesLevelTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(PriorityRuleLevelType.SAVING_FIRST.value, PriorityRuleLevelType.SAVING_FIRST.value),
            InternalModelOutput(PriorityRuleLevelType.DEBT_ACCEPTABLE.value, PriorityRuleLevelType.DEBT_ACCEPTABLE.value),
            InternalModelOutput(PriorityRuleLevelType.LIFE_STYLE_OPTIMIZED.value, PriorityRuleLevelType.LIFE_STYLE_OPTIMIZED.value),
        ))
    }

    @GetMapping("/finance-policy-risk-type")
    fun getFinancePolicyTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(FinancePolicyRiskLevelType.LOW.ordinal.toString(),
                "Low"),
            InternalModelOutput(FinancePolicyRiskLevelType.MEDIUM.ordinal.toString(),
                "Medium"),
            InternalModelOutput(FinancePolicyRiskLevelType.HIGH.ordinal.toString(),
                "High")
        ))
    }

    @GetMapping("/income-source-frequency-type")
    fun getIncomeSourceFrequencyTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(IncomeSourceFrequencyType.WEEKLY.value, "Weekly"),
            InternalModelOutput(IncomeSourceFrequencyType.BIWEEKLY.value, "Biweekly"),
            InternalModelOutput(IncomeSourceFrequencyType.MONTHLY.value, "Monthly"),
            InternalModelOutput(IncomeSourceFrequencyType.YEARLY.value, "Monthly"),
            InternalModelOutput(IncomeSourceFrequencyType.IRRELEVANTLY.value, "Irrelevant"),
        ))
    }

    @GetMapping("/income-source-type")
    fun getIncomeSourceTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(IncomeSourceType.SALARY.value, "Salary"),
            InternalModelOutput(IncomeSourceType.SALARY.value, "Contract"),
            InternalModelOutput(IncomeSourceType.SALARY.value, "Gig"),
            InternalModelOutput(IncomeSourceType.SALARY.value, "Passive"),
        ))
    }

    @GetMapping("/principle-type")
    fun getPrincipleTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput(PrincipleType.EMERGENCY_FUND.value, "Epargne d'urgence"),
            InternalModelOutput(PrincipleType.INVESTMENT.value, "Investissement"),
            InternalModelOutput(PrincipleType.UPGRADE.value, "Upgrade"),
        ))
    }
}