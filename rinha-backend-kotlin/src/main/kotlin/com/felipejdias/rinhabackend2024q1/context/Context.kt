package com.felipejdias.rinhabackend2024q1.context

import com.felipejdias.rinhabackend2024q1.db.model.TransactionEntity
import com.felipejdias.rinhabackend2024q1.exchange.TransactionRequest
import java.time.Instant

class Context(
    var request: TransactionRequest,
    var clientId: Long = 0
                )

    fun Context.requestToEntity(clientId: Long): TransactionEntity =
        TransactionEntity(
             type = this.request.type,
             amount = this.request.amount.toLong(),
             description = this.request.description,
             clientId = clientId,
             createdAt = Instant.now().toString())