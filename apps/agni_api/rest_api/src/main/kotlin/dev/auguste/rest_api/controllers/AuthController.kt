package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import dev.auguste.rest_api.controllers.models.ApiAuthLoginModel
import dev.auguste.rest_api.controllers.models.ApiAuthTokenModel
import domain.entities.Email
import domain.entities.Profile
import domain.entities.User
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@RestController
@RequestMapping("/public/auth")
class AuthController(
    private val userRepo: IRepository<User>,
    private val profileRepo: IRepository<Profile>,
    private val unitOfWork: IUnitOfWork,
    @Value("\${agni.local.secret-key:my-default-very-secret-local-key-change-me}")
    private val secretKey: String
) {
    @PostMapping("login")
    suspend fun login(@RequestBody request: ApiAuthLoginModel): ResponseEntity<ApiAuthTokenModel> {
        val condition = QueryExtendBuilder<User>()
            .addCondition("externalUserId", QueryComparator.Equal, request.externalId)

        val existing = userRepo.getAll(QueryFilter.queryAll(), condition).items.firstOrNull()

        // If user doesn't exist yet, provision them locally for testing/demo
        if (existing == null) {
            val newUser = User(
                externalUserId = request.externalId,
                username = request.username,
                email = Email(request.email),
            )

            unitOfWork.execute {
                userRepo.create(newUser)
                profileRepo.create(Profile(userId = newUser.id))
            }
        }

        val key = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        val now = Instant.now()
        val expiry = now.plusSeconds(86400) // 1 day expiry for local testing

        val tokenValue = Jwts.builder()
            .subject(request.externalId)
            .claim("email", request.email)
            .claim("preferred_username", request.username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()

        return ResponseEntity.ok(
            ApiAuthTokenModel(
                token = tokenValue,
                tokenType = "Bearer"
            )
        )
    }
}