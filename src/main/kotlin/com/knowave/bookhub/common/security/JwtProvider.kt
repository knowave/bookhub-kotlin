package com.knowave.bookhub.common.security

interface JwtProvider {
    fun createAccessToken(principal: UserPrincipal): String
    fun createRefreshToken(principal: UserPrincipal): String
    fun parseAccess(token: String): UserPrincipal
    fun parseRefresh(token: String): UserPrincipal
}