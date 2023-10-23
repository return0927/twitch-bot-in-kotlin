package kr.enak.luya.luyasupport.twitch.patch

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.netflix.hystrix.HystrixCommand

private val mapper = jsonMapper {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    addModule(kotlinModule {
        withReflectionCacheSize(512)
        configure(KotlinFeature.NullToEmptyCollection, false)
        configure(KotlinFeature.NullToEmptyMap, false)
        configure(KotlinFeature.NullIsSameAsDefault, false)
        configure(KotlinFeature.SingletonSupport, false)
        configure(KotlinFeature.StrictNullChecks, false)
        build()
    })
    addModule(JavaTimeModule())

    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

fun HystrixCommand<*>.toLogString(): String {
    val apiName = this.commandKey.name().split("(")[0].split("#")[1]
    return "[Twitch API] $apiName"
}

fun Any.toJson(): String = try {
    mapper.writeValueAsString(this)
} catch (e: Throwable) {
    e.toString()
}
