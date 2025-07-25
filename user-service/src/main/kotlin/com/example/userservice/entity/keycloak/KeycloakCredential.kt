package com.example.userservice.entity.keycloak

data class KeycloakCredential(
    val type: String,
    val value: String,
    val temporary: Boolean = false,
)