package com.recruiter.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class GatewayApplication

fun main(args: Array<String>) {
	runApplication<GatewayApplication>(*args)
}