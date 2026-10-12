package dev.auguste.rest_api

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Email
import domain.entities.Profile
import domain.entities.User
import domain.exceptions.UnAuthorizeException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtDecoder: ObjectProvider<JwtDecoder>
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/public/**", "/", "/health").permitAll()
                    .anyRequest().authenticated()
            }

        jwtDecoder.ifAvailable?.let { decoder ->
            http.oauth2ResourceServer { oauth ->
                oauth.jwt { it.decoder(decoder) }
            }
        }

        return http.build()
    }
}

/**
 * Implementation OAuth2/OIDC du port [IAuthenticateAdapter].
 *
 * Le token est porte par la requete courante : un bean singleton ne peut donc pas
 * recevoir le `@AuthenticationPrincipal` par injection de constructeur. On lit le
 * [Jwt] depuis le [SecurityContextHolder] au moment de l'appel.
 *
 * Le premier appel authentifie d'un utilisateur inconnu le provisionne (utilisateur +
 * profil par defaut) a partir des claims OIDC standards.
 */
@Component
class OAuth2Adapter(
    private val userRepo: IRepository<User>,
    private val profileRepo: IRepository<Profile>,
    private val unitOfWork: IUnitOfWork
) : IAuthenticateAdapter {

    override suspend fun isAuthenticate(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        return authentication.isAuthenticated && authentication.principal is Jwt
    }

    override suspend fun getCurrentUser(): User {
        val jwt = currentJwt() ?: throw UnAuthorizeException.UserUnAuthorizedException()

        val externalId = (jwt.subject ?: jwt.getClaimAsString("sub"))
            ?: throw UnAuthorizeException.UserUnAuthorizedException()

        val condition = QueryExtendBuilder<User>()
            .addCondition("externalUserId", QueryComparator.Equal, externalId)
        val existing = userRepo.getAll(QueryFilter.queryAll(), condition).items.firstOrNull()
        if (existing != null) return existing

        val username = jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("username")
            ?: jwt.getClaimAsString("nickname")
            ?: jwt.getClaimAsString("name")
            ?: "user"

        val email = jwt.getClaimAsString("email") ?: syntheticEmail(externalId)

        val newUser = User(
            externalUserId = externalId,
            username = username,
            email = Email(email),
        )

        unitOfWork.execute {
            userRepo.create(newUser)
            profileRepo.create(Profile(userId = newUser.id))
        }

        return newUser
    }

    private fun currentJwt(): Jwt? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        return when (val principal = authentication.principal) {
            is Jwt -> principal
            is JwtAuthenticationToken -> principal.token
            else -> null
        }
    }

    /**
     * Certains fournisseurs n'exposent pas de claim `email` : on fabrique une adresse
     * synthetique valide pour ne pas casser la contrainte du value object [Email].
     */
    private fun syntheticEmail(externalId: String): String {
        val local = externalId.filter { it.isLetterOrDigit() }
            .take(60)
            .ifBlank { UUID.randomUUID().toString().replace("-", "") }
        return "$local@oauth.local"
    }
}


@Configuration
@ConditionalOnProperty(
    name = ["agni.local.auth"],
    havingValue = "true",
    matchIfMissing = false
)
class LocalJwtConfig(
    @Value("\${agni.local.secret-key:my-default-very-secret-local-key-change-me}")
    private val secretKey: String
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).build()
    }
}