package com.example.goalmate.data

enum class RegistrationStep {
    EMAIL_PASSWORD,
    PERSONAL_INFO,
    BIRTH_DATE;

    fun next(): RegistrationStep {
        return when (this) {
            EMAIL_PASSWORD -> PERSONAL_INFO
            PERSONAL_INFO -> BIRTH_DATE
            BIRTH_DATE -> BIRTH_DATE
        }
    }

    fun previous(): RegistrationStep {
        return when (this) {
            EMAIL_PASSWORD -> EMAIL_PASSWORD
            PERSONAL_INFO -> EMAIL_PASSWORD
            BIRTH_DATE -> PERSONAL_INFO
        }
    }
}