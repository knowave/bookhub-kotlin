package com.knowave.bookhub

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@ApplyExtension(SpringExtension::class)
class BookhubApplicationTests : DescribeSpec ({
	describe("어플리케이션 컨텍스트") {
		it("정상적으로 로드된다") {}
	}
})
