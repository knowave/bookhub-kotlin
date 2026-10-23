package com.knowave.bookhub.domains.loan

import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.book.entity.Book
import com.knowave.bookhub.domains.book.entity.Category
import com.knowave.bookhub.domains.book.repository.BookRepository
import com.knowave.bookhub.domains.book.repository.CategoryRepository
import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.library.entity.Library
import com.knowave.bookhub.domains.library.repository.BookCopyRepository
import com.knowave.bookhub.domains.library.repository.LibraryRepository
import com.knowave.bookhub.domains.loan.repository.LoanRepository
import com.knowave.bookhub.domains.loan.service.LoanService
import com.knowave.bookhub.domains.loan.service.dto.CreateLoanCommand
import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Testcontainers
class LoanConcurrencyTest {

    lateinit var caller: UserPrincipal
    lateinit var members: List<User>
    lateinit var library: Library
    lateinit var bookCopy: BookCopy

    @Autowired lateinit var jdbcTemplate: JdbcTemplate

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var libraryRepository: LibraryRepository
    @Autowired lateinit var categoryRepository: CategoryRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var bookCopyRepository: BookCopyRepository
    @Autowired lateinit var loanRepository: LoanRepository

    @Autowired lateinit var loanService: LoanService


    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16")
    }

    @BeforeEach
    fun setup() {
        members = userRepository.saveAll(
            (1..1000).map {
                User.member(
                    email = "test${it}@bookhub.com",
                    password = "test123!",
                    name = "tester $it"
                )
            }
        )

        library = libraryRepository.save(
            Library(
                name = "test_library",
                address= "test 10101010",
                phone = "01012345678",
            )
        )

        caller = UserPrincipal(
            id = UUID.randomUUID(),
            role = UserRole.LIBRARIAN,
            libraryId = library.id!!,
        )

        val category = categoryRepository.save(Category(name = "소설"))
        val book = bookRepository.save(
            Book(
                isbn = "1234567890",
                title = "test_book",
                author = "test_book_author",
                publisher = "test_book_publisher",
                publishedAt = LocalDate.now(),
                category = category,
            )
        )

        bookCopy = bookCopyRepository.save(
            BookCopy(
                callNumber = "0987654321",
                book = book,
                library = library,
            )
        )
    }

    @Test
    fun `동시에 같은 복본을 대출하면 한 건만 성공한다`() {
        val threadCount = 1000
        // 스레드 풀 10개 준비
        val executor = Executors.newFixedThreadPool(threadCount)
        // 스레드 빗장 10개 준비
        val latch = CountDownLatch(threadCount)

        // 여러 스레드가 동시에 증가시켜도 안전한 정수.
        // 그냥 Int로 하면 여러 스레드가 읽기, 쓰기, 수정을 하기 때문에 카운트가 꼬일 우려가 있음
        val successCount = AtomicInteger()

        val start = System.currentTimeMillis()

        // 스레드 풀만큼 반복
        repeat(threadCount) { index ->
            // 다음 작업을 기다리지 않고, 실행 후 넘어가기 submit
            executor.submit {
                try {
                    loanService.createLoan(
                        CreateLoanCommand(
                            memberId = members[index].id!!,
                            bookCopyId = bookCopy.id!!,
                            caller = caller,
                        )
                    )

                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 실패는 정상.
                } finally {

                    // 각 스레드가 끝나면 카운트 감소 (10...9...8)
                    latch.countDown()
                }
            }
        }

        // 메인 스레드가 0이 될 때까지 대기
        latch.await()

        // 스레드 풀 정리
        executor.shutdown()

        println("소요 시간 ${System.currentTimeMillis()-start} ms")

        assertEquals(1, successCount.get())
        assertEquals(1, loanRepository.count())
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.execute(
            "truncate table loans, book_copies, books, categories, libraries, users cascade"
        )
    }
}