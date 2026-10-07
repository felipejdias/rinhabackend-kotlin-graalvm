package com.felipejdias.rinhabackend2024q1.service.impl

import com.felipejdias.rinhabackend2024q1.context.Context
import com.felipejdias.rinhabackend2024q1.context.requestToEntity
import com.felipejdias.rinhabackend2024q1.db.repository.TransactionRepository
import com.felipejdias.rinhabackend2024q1.exception.ClientLimitExceededException
import com.felipejdias.rinhabackend2024q1.exception.ClientNotFoundException
import com.felipejdias.rinhabackend2024q1.exchange.TransactionResponse
import com.felipejdias.rinhabackend2024q1.service.TransactionService
import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
class DefaultTransactionService(private val transactionRepository: TransactionRepository) : TransactionService {

    companion object {
        // SQLSTATEs levantados pela procedure register_transaction (scripts/script.sql)
        private const val CLIENT_NOT_FOUND = "P0002"
        private const val CLIENT_LIMIT_EXCEEDED = "P0001"
    }

    override fun create(context: Context): TransactionResponse {
        val transaction = context.requestToEntity(context.clientId)
        return try {
            transactionRepository.registerTransaction(context.clientId, transaction)
        } catch (ex: SQLException) {
            when (ex.sqlState) {
                CLIENT_NOT_FOUND -> throw ClientNotFoundException()
                CLIENT_LIMIT_EXCEEDED -> throw ClientLimitExceededException()
                else -> throw ex
            }
        }
    }
}
