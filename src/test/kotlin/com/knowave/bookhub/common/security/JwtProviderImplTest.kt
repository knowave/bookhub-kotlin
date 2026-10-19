package com.knowave.bookhub.common.security

import com.knowave.bookhub.domains.user.entity.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import java.util.*

class JwtProviderImplTest : DescribeSpec({

    val secret = "test-secret-key-for-unit-test-only-32bytes+"
    val otherSecret = "another-secret-key-for-unit-test-only-32b+"
    val sut = JwtProviderImpl(secret, 3600L, 7200L)

    describe("토큰 생성과 파싱") {
        it("access 토큰을 parse하면 id, role, libraryId를 그대로 돌려준다") {
            val principal = UserPrincipal(UUID.randomUUID(), UserRole.LIBRARIAN, UUID.randomUUID())

            sut.parseAccess(sut.createAccessToken(principal)) shouldBe principal
        }

        it("libraryId가 null이면 parse 결과의 libraryId도 null이다") {
            val principal = UserPrincipal(UUID.randomUUID(), UserRole.MEMBER, null)

            sut.parseAccess(sut.createAccessToken(principal)).libraryId shouldBe null
        }
    }

    describe("토큰 타입 구분") {
        val principal = UserPrincipal(UUID.randomUUID(), UserRole.MEMBER, null)

        it("refresh 토큰을 parse에 넣으면 InvalidTokenException이다") {
            val refresh = sut.createRefreshToken(principal)

            shouldThrow<InvalidTokenException> { sut.parseAccess(refresh) }
        }

        it("access 토큰을 parseRefresh에 넣으면 InvalidTokenException이다") {
            val access = sut.createAccessToken(principal)

            shouldThrow<InvalidTokenException> { sut.parseRefresh(access) }
        }
    }

    describe("시크릿 검증") {
        it("32바이트 미만 시크릿이면 생성 단계에서 실패하고 메시지에 시크릿이 없다") {
            val shortSecret = "short-secret-value"

            val e = shouldThrow<IllegalArgumentException> { JwtProviderImpl(shortSecret, 3600L, 7200L) }

            (e.message ?: "") shouldNotContain shortSecret
        }
    }

    describe("서명과 만료 거부") {
        val principal = UserPrincipal(UUID.randomUUID(), UserRole.ADMIN, null)

        it("다른 시크릿으로 서명된 토큰은 InvalidTokenException이다") {
            val forged = JwtProviderImpl(otherSecret, 3600L, 7200L).createAccessToken(principal)

            shouldThrow<InvalidTokenException> { sut.parseAccess(forged) }
        }

        it("만료된 토큰은 InvalidTokenException이다") {
            val expired = JwtProviderImpl(secret, -10L, -10L).createAccessToken(principal)

            shouldThrow<InvalidTokenException> { sut.parseAccess(expired) }
        }
    }
})
