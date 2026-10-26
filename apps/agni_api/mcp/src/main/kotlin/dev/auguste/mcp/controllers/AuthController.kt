package dev.auguste.mcp.controllers

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import dev.auguste.mcp.controllers.models.ApiAuthLoginModel
import dev.auguste.mcp.controllers.models.ApiAuthTokenModel
import domain.entities.User
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
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
    @Value("\${agni.local.secret-key:my-default-very-secret-local-key-change-me}")
    private val secretKey: String,
) {
    @PostMapping("login")
    suspend fun login(@RequestBody request: ApiAuthLoginModel): ResponseEntity<ApiAuthTokenModel> {
        val condition = QueryExtendBuilder<User>()
            .addCondition("apiToken", QueryComparator.Equal, request.apiToken)

        val user = userRepo.getAll(QueryFilter.queryAll(), condition).items.firstOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val key = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        val now = Instant.now()
        val expiry = now.plusSeconds(86400)

        val tokenValue = Jwts.builder()
            .subject(user.apiToken)
            .claim("api_key", user.apiToken)
            .claim("email", user.email.toString())
            .claim("preferred_username", user.username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()

        return ResponseEntity.ok(
            ApiAuthTokenModel(
                token = tokenValue,
                tokenType = "Bearer",
            )
        )
    }
}
