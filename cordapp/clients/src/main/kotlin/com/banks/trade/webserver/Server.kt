package com.banks.trade.webserver

import net.corda.client.jackson.JacksonSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.util.*
import org.springframework.web.servlet.config.annotation.CorsRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * A Spring Boot application.
 */

@SpringBootApplication
open class Server {
    /**
     * Spring Bean that binds a Corda Jackson object-mapper to HTTP message types used in Spring.
     */
    @Bean
    open fun mappingJackson2HttpMessageConverter(@Autowired rpcConnection: NodeRPCConnection): MappingJackson2HttpMessageConverter {
        val mapper = JacksonSupport.createDefaultMapper(rpcConnection.proxy)
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }

//    @Bean
//    open fun corsFilter(): CorsFilter? {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//        config.allowCredentials = true
//        // Don't do this in production, use a proper list  of allowed origins
//        config.allowedOrigins = Collections.singletonList("*")
//        config.allowedHeaders = Arrays.asList("Origin", "Content-Type", "Accept")
//        config.allowedMethods = Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
//        source.registerCorsConfiguration("/**", config)
//        return CorsFilter(source)
//    }
}

//@Configuration
//@EnableWebMvc
//open class WebConfig : WebMvcConfigurer {
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/api/**")
//            .allowedOrigins("http://localhost:62893")
//            .allowedMethods("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
//            .allowedHeaders("Origin", "Content-Type", "Accept")
//            .allowCredentials(false).maxAge(3600)
//    }
//}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Server::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = WebApplicationType.SERVLET
    app.run(*args)
}