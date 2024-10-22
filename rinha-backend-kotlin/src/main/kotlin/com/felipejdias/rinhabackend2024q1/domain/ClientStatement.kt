package com.felipejdias.rinhabackend2024q1.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientStatement(
    @JsonProperty("saldo")
    var balance: Balance = Balance(),

    @JsonProperty("ultimas_transacoes")
    var recentTransactions: List<TransactionResponse> = ArrayList<TransactionResponse>()
)

fun ClientStatement.isClientInvalid () = this.balance.limit == 0L && this.balance.total == 0L && this.recentTransactions.isEmpty()

// Classes Balance e TransactionResponse para contexto
data class Balance(
    @JsonProperty("total")
    var total: Long = 0,

    @JsonProperty("data_extrato")
    var statementDate: String = "",

    @JsonProperty("limite")
    var limit: Long = 0
)

data class TransactionResponse(
    @JsonProperty("valor")
    val amount: Long = 0,

    @JsonProperty("tipo")
    val type: String = "",

    @JsonProperty("descricao")
    val description: String = "",

    @JsonProperty("realizado_em")
    val createdAt: String = ""
)
