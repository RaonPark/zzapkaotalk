package com.example.userservice.entity.keycloak

data class KeycloakUser(
    val email: String,
    val username: String,
    val enabled: Boolean = true,
    val credentials: List<KeycloakCredential>
)