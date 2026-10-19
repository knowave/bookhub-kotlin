package com.knowave.bookhub.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null) {
            try {
                val principal = jwtProvider.parseAccess(token)

                val authorities = listOf(SimpleGrantedAuthority(principal.role.name))
                val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: InvalidTokenException) {
                logger.debug("Token invalid: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        if (bearerToken?.startsWith(BEARER_PREFIX) == true) {
            return bearerToken.substring(BEARER_PREFIX.length)
        }

        return null
    }

    companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}