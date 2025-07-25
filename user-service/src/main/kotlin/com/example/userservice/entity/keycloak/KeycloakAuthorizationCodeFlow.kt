package com.example.userservice.entity.keycloak

data class KeycloakAuthorizationCodeFlow(
    val clientId: String,
    val redirectionUri: String,
    val responseType: String,
    val scope: String,
    val state: String,
)