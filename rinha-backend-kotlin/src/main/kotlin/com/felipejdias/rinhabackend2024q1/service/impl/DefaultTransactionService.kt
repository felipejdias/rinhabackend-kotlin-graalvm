package com.felipejdias.rinhabackend2024q1.service.impl

import com.felipejdias.rinhabackend2024q1.context.Context
import com.felipejdias.rinhabackend2024q1.context.requestToEntity
import com.felipejdias.rinhabackend2024q1.db.repository.TransactionRepository
import com.felipejdias.rinhabackend2024q1.exception.ClientLimitExceededException
import com.felipejdias.rinhabackend2024q1.exchange.TransactionResponse
import com.felipejdias.rinhabackend2024q1.service.TransactionService
import org.postgresql.util.PSQLException
import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
class DefaultTransactionService: TransactionService {

    override fun create(context: Context): TransactionResponse {
        var transactionRepository = TransactionRepository()
        val transaction = context.requestToEntity(context.clientId)
        return try {
            transactionRepository.registerTransaction(context.clientId, transaction)
        } catch (ex: SQLException) {
            throw ClientLimitExceededException()
        } catch (ex: PSQLException){
            throw ex
        }
    }
}