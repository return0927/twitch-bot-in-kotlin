package kr.enak.luya.luyasupport

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class LuyaSupportApplication

fun main(args: Array<String>) {
    runApplication<LuyaSupportApplication>(*args)
}
