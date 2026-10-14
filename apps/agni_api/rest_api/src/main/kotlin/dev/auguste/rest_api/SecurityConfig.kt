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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

class JwtAudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): org.springframework.security.oauth2.core.OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        if (audiences.contains(audience)) {
            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
        }
        return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
            org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "The required audience is missing", null)
        )
    }
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(
    name = ["agni.local.auth"],
    havingValue = "false",
    matchIfMissing = true
)
class OidcSecurityConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val issuerUri: String,
    @Value("\${agni.auth0.audience}")
    private val audience: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtDecoder: ObjectProvider<JwtDecoder>): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/v2/public/**",  "/", "/health").permitAll()
                    .anyRequest().authenticated()
            }

        jwtDecoder.ifAvailable?.let { decoder ->
            http.oauth2ResourceServer { oauth ->
                oauth.jwt { it.decoder(decoder) }
            }
        }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder

        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience: OAuth2TokenValidator<Jwt> = JwtAudienceValidator(audience)

        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(withIssuer, withAudience))

        return jwtDecoder
    }
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(
    name = ["agni.local.auth"],
    havingValue = "true"
)
class LocalSecurityConfig(
    @Value("\${agni.local.secret-key:my-default-very-secret-local-key-change-me}")
    private val secretKey: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtDecoder: ObjectProvider<JwtDecoder>): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/v2/public/**", "/", "/health").permitAll()
                    .anyRequest().authenticated()
            }

        jwtDecoder.ifAvailable?.let { decoder ->

            http.oauth2ResourceServer { oauth ->

                oauth.jwt { it.decoder(decoder) }

            }

        }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {

        val key = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")

        return NimbusJwtDecoder.withSecretKey(key).build()

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
            ?: jwt.getClaimAsString("agni-api/name")
            ?: "user"

        val email = jwt.getClaimAsString("agni-api/email") ?: syntheticEmail(externalId)

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
