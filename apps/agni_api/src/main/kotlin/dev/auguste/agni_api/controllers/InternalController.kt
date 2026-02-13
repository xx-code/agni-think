package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.InternalModelOutput
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
            InternalModelOutput("REGISTERED", "Enregisterer"),
            InternalModelOutput("UNREGISTERED", "Unregister")
        ))
    }

    @GetMapping("/management-account-type")
    fun getManagementAccountTypes(): ResponseEntity<List<InternalModelOutput>> {
        return ResponseEntity.ok(listOf(
            InternalModelOutput("MANAGED", "Manager"),
            InternalModelOutput("ROBOT", "Robot"),
            InternalModelOutput("SELF_DIRECTED", "Gerer")
        ))
    }
}