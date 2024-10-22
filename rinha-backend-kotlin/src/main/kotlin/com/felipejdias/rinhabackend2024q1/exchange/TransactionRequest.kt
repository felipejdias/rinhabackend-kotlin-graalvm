package com.felipejdias.rinhabackend2024q1.exchange

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionRequest(
    @JsonProperty("valor")
    val amount: Double,
    @JsonProperty("tipo")
    val type: String,
    @JsonProperty("descricao")
    val description: String)