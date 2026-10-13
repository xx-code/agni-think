package dev.auguste.rest_api.controllers

import adapters.IAuthenticateAdapter
import configs.DataSeeder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/devtools")
@ConditionalOnProperty(
    name = ["agni.dev.tool"],
    havingValue = "true",
    matchIfMissing = false
)
class DevToolController(
    private val dataSeeder: DataSeeder,
    private val auth: IAuthenticateAdapter
) {
    @PostMapping("/reset-and-populate")
    suspend fun resetAndPopulate(): ResponseEntity<String> {
        val user = auth.getCurrentUser()
        dataSeeder.seed(user.id)
        return ResponseEntity.ok("Application reset and populated successfully !")
    }
}