package com.example.notes.auth

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey.Builder
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey,
)

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(RsaKeyProperties::class)
class SecurityConfig(
    private val rsaKeys: RsaKeyProperties,
    private val jpaUserDetailsService: JpaUserDetailsService,
    private val h2ConsoleProperties: H2ConsoleProperties?,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val h2ConsoleEnabled = h2ConsoleProperties?.enabled ?: false

        http {
            csrf { disable() }
            if (h2ConsoleEnabled) {
                headers {
                    frameOptions {
                        sameOrigin = true
                    }
                }
            }
            authorizeHttpRequests {
                authorize("/register", permitAll)
                if (h2ConsoleEnabled) {
                    authorize(PathRequest.toH2Console(), permitAll)
                }
                authorize(AntPathRequestMatcher("/v3/api-docs/**"), permitAll)
                authorize(AntPathRequestMatcher("/swagger-ui/**"), permitAll)
                authorize(anyRequest, authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            oauth2ResourceServer {
                jwt { }
            }
            httpBasic { }
        }
        http.userDetailsService(jpaUserDetailsService)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder
            .withPublicKey(rsaKeys.publicKey)
            .build()

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val jwk =
            Builder(rsaKeys.publicKey)
                .privateKey(rsaKeys.privateKey)
                .build()

        val jwkSource = ImmutableJWKSet<SecurityContext?>(JWKSet(jwk))

        return NimbusJwtEncoder(jwkSource)
    }
}
