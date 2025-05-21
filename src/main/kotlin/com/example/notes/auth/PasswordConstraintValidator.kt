package com.example.notes.auth

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.LengthRule
import org.passay.PasswordData
import org.passay.PasswordValidator
import org.passay.WhitespaceRule
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PasswordConstraintValidator::class])
@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD])
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPassword(
    val message: String = "Invalid Password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PasswordConstraintValidator : ConstraintValidator<ValidPassword, NewUser> {
    override fun isValid(
        newUser: NewUser,
        context: ConstraintValidatorContext,
    ): Boolean {
        val validator =
            PasswordValidator(
                LengthRule(8, 30),
                WhitespaceRule(),
                CharacterRule(EnglishCharacterData.UpperCase, 1),
                CharacterRule(EnglishCharacterData.LowerCase, 1),
                CharacterRule(EnglishCharacterData.Digit, 1),
                CharacterRule(EnglishCharacterData.Special, 1),
            )

        val result = validator.validate(PasswordData(newUser.password))
        if (result.isValid) {
            return true
        }
        context.disableDefaultConstraintViolation()
        context
            .buildConstraintViolationWithTemplate(
                validator.getMessages(result).joinToString(","),
            ).addConstraintViolation()
        return false
    }
}
