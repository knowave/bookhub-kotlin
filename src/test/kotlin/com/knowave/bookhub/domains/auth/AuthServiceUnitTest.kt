package com.knowave.bookhub.domains.auth

import com.knowave.bookhub.domains.auth.service.AuthServiceImpl
import com.knowave.bookhub.domains.auth.service.dto.SignUpLibrarianCommand
import com.knowave.bookhub.domains.auth.service.dto.SignUpMemberCommand
import com.knowave.bookhub.domains.user.service.UserService
import com.knowave.bookhub.domains.user.service.dto.CreateLibrarianCommand
import com.knowave.bookhub.domains.user.service.dto.CreateMemberCommand
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceUnitTest : DescribeSpec({

    lateinit var userService: UserService
    lateinit var passwordEncoder: PasswordEncoder
    lateinit var sut: AuthServiceImpl

    beforeEach {
        userService = mockk()
        passwordEncoder = mockk()
        sut = AuthServiceImpl(userService, passwordEncoder)
    }

    describe("signUpMember") {
        val testName = "테스트 독자"
        val testEmail = "reader@bookhub.com"
        val testPassword = "reader1234!!!"

        it("평문 비밀번호를 암호화해 UserService에 전달한다") {

            val command = SignUpMemberCommand(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            val captured = slot<CreateMemberCommand>()

            every { passwordEncoder.encode(testPassword) } returns "encoded-hash"
            every { userService.createMember(capture(captured)) } returns mockk()

            sut.signUpMember(command)

            captured.captured.encodedPassword shouldBe "encoded-hash"
            captured.captured.encodedPassword shouldNotBe testPassword
        }

        it("이메일과 이름을 그대로 전달한다") {
            val command = SignUpMemberCommand(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            val captured = slot<CreateMemberCommand>()

            every { passwordEncoder.encode(testPassword) } returns "encoded-hash"
            every { userService.createMember(capture(captured)) } returns mockk()

            sut.signUpMember(command)

            captured.captured.email shouldBe testEmail
            captured.captured.name shouldBe testName
        }
    }

    describe("signUpLibrarian") {

        it("소속 지점 ID를 그대로 전달한다") {
            val libraryId = UUID.randomUUID()
            val command = SignUpLibrarianCommand(
                email = "staff@example.com",
                password = "plain1234!",
                name = "박사서",
                libraryId = libraryId,
            )
            val captured = slot<CreateLibrarianCommand>()

            every { passwordEncoder.encode(any()) } returns "encoded-hash"
            every { userService.createLibrarian(capture(captured)) } returns mockk()

            sut.signUpLibrarian(command)

            captured.captured.libraryId shouldBe libraryId
            captured.captured.encodedPassword shouldBe "encoded-hash"
        }
    }
})