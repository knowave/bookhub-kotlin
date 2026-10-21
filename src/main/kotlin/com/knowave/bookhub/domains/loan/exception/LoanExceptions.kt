package com.knowave.bookhub.domains.loan.exception

import com.knowave.bookhub.common.exception.BusinessException
import org.springframework.http.HttpStatus
import java.util.UUID

class LoanNotFoundException(loanId: UUID) : BusinessException(
    code = "LOAN_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "대출을 찾을 수 없습니다: $loanId",
)

class LoanAlreadyClosedException(loanId: UUID) : BusinessException(
    code = "LOAN_ALREADY_CLOSED",
    status = HttpStatus.CONFLICT,
    rule = "L-12",
    message = "이미 종료된 대출입니다: $loanId",
)

class LoanLimitExceededException(userId: UUID) : BusinessException(
    code = "LOAN_LIMIT_EXCEEDED",
    status = HttpStatus.CONFLICT,
    rule = "L-2",
    message = "진행 중인 대출이 이미 한도에 도달했습니다: $userId",
)

class LoanOverdueExistsException(userId: UUID) : BusinessException(
    code = "LOAN_OVERDUE_EXISTS",
    status = HttpStatus.CONFLICT,
    rule = "L-3",
    message = "연체 중인 대출이 있어 새로 대출할 수 없습니다: $userId",
)

class LoanExtensionLimitExceededException(loanId: UUID) : BusinessException(
    code = "LOAN_EXTENSION_LIMIT_EXCEEDED",
    status = HttpStatus.CONFLICT,
    rule = "L-4",
    message = "연장 가능 횟수를 모두 사용했습니다: $loanId",
)

class LoanOverdueCannotExtendException(loanId: UUID) : BusinessException(
    code = "LOAN_OVERDUE_CANNOT_EXTEND",
    status = HttpStatus.CONFLICT,
    rule = "L-5",
    message = "연체된 대출은 연장할 수 없습니다: $loanId",
)

class LoanNotOwnedException(loanId: UUID) : BusinessException(
    code = "LOAN_NOT_OWNED",
    status = HttpStatus.FORBIDDEN,
    rule = "L-8",
    message = "본인의 대출이 아닙니다: $loanId",
)

class LoanAccessDeniedLibraryException(bookCopyId: UUID) : BusinessException(
    code = "ACCESS_DENIED_LIBRARY",
    status = HttpStatus.FORBIDDEN,
    rule = "P-8",
    message = "소속 지점의 복본이 아닙니다: $bookCopyId",
)

class LoanAccessDeniedRoleException : BusinessException(
    code = "ACCESS_DENIED_ROLE",
    status = HttpStatus.FORBIDDEN,
    rule = "P-1",
    message = "이 역할은 대출 업무를 수행하지 않습니다.",
)

class BookCopyNotAvailableException(bookCopyId: UUID) : BusinessException(
    code = "BOOK_COPY_NOT_AVAILABLE",
    status = HttpStatus.CONFLICT,
    rule = "L-10",
    message = "대출할 수 있는 상태의 복본이 아닙니다: $bookCopyId",
)
