package com.felipejdias.rinhabackend2024q1.db.model

import com.felipejdias.rinhabackend2024q1.domain.TransactionResponse
import java.time.Instant
import java.util.*


data class TransactionEntity(
    val id: UUID = UUID.randomUUID(),
    var type: String,
    var amount: Long,
    var description: String,
    var clientId: Long,
    var createdAt: String
){
    class Builder {
        private var id: UUID = UUID.randomUUID()
        private lateinit var type: String
        private var amount: Long = 0
        private lateinit var description: String
        private var clientId: Long = 0
        private var createdAt: String = Instant.now().toString()

        fun id(id: UUID) = apply { this.id = id }
        fun type(type: String) = apply { this.type = type }
        fun amount(amount: Long) = apply { this.amount = amount }
        fun description(description: String) = apply { this.description = description }
        fun clientId(clientId: Long) = apply { this.clientId = clientId }
        fun createdAt(createdAt: String) = apply { this.createdAt = createdAt }

        fun build() = TransactionEntity(
            id = id,
            type = type,
            amount = amount,
            description = description,
            clientId = clientId,
            createdAt = createdAt
        )
    }
}



fun TransactionEntity.toTransactionResponse(): TransactionResponse {
    return TransactionResponse(
        amount = amount,
        type = type,
        description = description,
        createdAt = createdAt.toString(),
    )
}

fun List<TransactionEntity>.toTransactionsStatement(): List<TransactionResponse> {
    return this.map { it.toTransactionResponse() }
}