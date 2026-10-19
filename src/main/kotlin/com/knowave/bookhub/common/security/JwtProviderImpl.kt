package com.knowave.bookhub.common.security

import com.knowave.bookhub.domains.user.entity.UserRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProviderImpl(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-validity-seconds}") private val accessTokenValiditySeconds: Long,
    @Value("\${jwt.refresh-token-validity-seconds}") private val refreshTokenValiditySeconds: Long,
) : JwtProvider {

    private val key: SecretKey = secret.toByteArray(Charsets.UTF_8).let { bytes ->
        require(bytes.size >= 32) {
            "jwt.secret은 HS256에 필요한 32바이트 이상이어야 합니다. 현재 ${bytes.size}바이트"
        }
        Keys.hmacShaKeyFor(bytes)
    }

    override fun createAccessToken(principal: UserPrincipal): String =
        create(principal, TYPE_ACCESS, accessTokenValiditySeconds)

    override fun createRefreshToken(principal: UserPrincipal): String =
        create(principal, TYPE_REFRESH, refreshTokenValiditySeconds)

    override fun parseAccess(token: String): UserPrincipal = toPrincipal(token, TYPE_ACCESS)

    override fun parseRefresh(token: String): UserPrincipal = toPrincipal(token, TYPE_REFRESH)

    private fun create(principal: UserPrincipal, type: String, validitySeconds: Long): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(principal.id.toString())
            .claim(CLAIM_ROLE, principal.role.name)
            .claim(CLAIM_LIBRARY_ID, principal.libraryId?.toString())
            .claim(CLAIM_TYPE, type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(validitySeconds)))
            .signWith(key)
            .compact()
    }

    private fun parseClaims(token: String) =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    private fun toPrincipal(token: String, expectedType: String): UserPrincipal =
        try {
            val claims = parseClaims(token)
            if (claims[CLAIM_TYPE, String::class.java] != expectedType) throw InvalidTokenException()
            UserPrincipal(
                id = UUID.fromString(claims.subject),
                role = UserRole.valueOf(claims[CLAIM_ROLE, String::class.java]),
                libraryId = claims[CLAIM_LIBRARY_ID, String::class.java]?.let(UUID::fromString),
            )
        } catch (e: InvalidTokenException) {
            throw e
        } catch (e: Exception) {
            throw InvalidTokenException()
        }

    private companion object {
        const val CLAIM_ROLE = "role"
        const val CLAIM_LIBRARY_ID = "libraryId"
        const val CLAIM_TYPE = "type"
        const val TYPE_ACCESS = "access"
        const val TYPE_REFRESH = "refresh"
    }
}
