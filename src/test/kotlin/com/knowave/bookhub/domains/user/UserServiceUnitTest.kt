package com.knowave.bookhub.domains.user

import com.knowave.bookhub.common.exception.EmailDuplicatedException
import com.knowave.bookhub.domains.library.service.LibraryService
import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.repository.UserRepository
import com.knowave.bookhub.domains.user.service.UserServiceImpl
import com.knowave.bookhub.domains.user.service.dto.CreateLibrarianCommand
import com.knowave.bookhub.domains.user.service.dto.CreateMemberCommand
import com.knowave.bookhub.fixture.asPersisted
import com.knowave.bookhub.fixture.createLibrary
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class UserServiceUnitTest : DescribeSpec({

    lateinit var userRepository: UserRepository
    lateinit var libraryService: LibraryService
    lateinit var passwordEncoder: PasswordEncoder
    lateinit var sut: UserServiceImpl

    beforeEach {
        userRepository = mockk()
        libraryService = mockk()
        passwordEncoder = mockk()
        sut = UserServiceImpl(userRepository, libraryService, passwordEncoder)
    }

    describe("createMember") {
        val testMemberName = "테스트 독자"
        val testMemberEmail = "reader@bookhub.com"
        val testMemberPassword = "reader1234!!!"

        val createMemberCommand = CreateMemberCommand(
            email = testMemberEmail,
            encodedPassword = testMemberPassword,
            name = testMemberName
        )

        it ("일반 회원은 가입 즉시 ACTIVE가 된다") {

            every { userRepository.existsByEmail(testMemberEmail) } returns false
            every { userRepository.save(any()) } answers { firstArg<User>().asPersisted() }

            val result = sut.createMember(createMemberCommand)

            result.status shouldBe UserStatus.ACTIVE
        }

        it("일반 회원 생성 시 이미 존재하는 User가 있다면 EmailDuplicatedException이 발생한다") {

            every { userRepository.existsByEmail(testMemberEmail) } returns true

            val exception = shouldThrow<EmailDuplicatedException> {
                sut.createMember(createMemberCommand)
            }

            exception.code shouldBe "USER_EMAIL_DUPLICATED"
        }
    }

    describe("createLibrarian") {
        val libraryId = UUID.randomUUID()
        val testLibrarianEmail = "staff@example.com"
        val testLibrarianPassword = "plain1234!"
        val testLibrarianName = "박사서"

        val createLibrarianCommand = CreateLibrarianCommand(
            email = testLibrarianEmail,
            encodedPassword = testLibrarianPassword,
            name = testLibrarianName,
            libraryId = libraryId
        )

        it ("사서 생성 시 상태는 무조건 PENDING 이어야 한다") {

            every { userRepository.existsByEmail(testLibrarianEmail) } returns false
            every { libraryService.getLibraryEntity(libraryId) } returns createLibrary(id = libraryId)
            every { userRepository.save(any()) } answers { firstArg<User>().asPersisted() }

            val result = sut.createLibrarian(createLibrarianCommand)

            result.status shouldBe UserStatus.PENDING
        }
    }


})