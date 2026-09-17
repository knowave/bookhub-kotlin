package com.knowave.bookhub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookhubApplication

fun main(args: Array<String>) {
	runApplication<BookhubApplication>(*args)
}
