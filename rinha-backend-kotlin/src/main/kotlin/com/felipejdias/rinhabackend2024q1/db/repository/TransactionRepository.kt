package com.felipejdias.rinhabackend2024q1.db.repository

import com.felipejdias.rinhabackend2024q1.db.model.TransactionEntity
import com.felipejdias.rinhabackend2024q1.exchange.TransactionResponse
import org.springframework.stereotype.Repository
import javax.sql.DataSource

@Repository
class TransactionRepository(private val dataSource: DataSource) {

    fun registerTransaction(idCliente: Long, transactionEntity: TransactionEntity): TransactionResponse {
        return dataSource.connection.use { connection ->
            connection.prepareCall("{call public.register_transaction(?, ?, ?, ?)}").use { callableStatement ->
                callableStatement.setInt(1, idCliente.toInt())
                callableStatement.setString(2, transactionEntity.type)
                callableStatement.setLong(3, transactionEntity.amount)
                callableStatement.setString(4, transactionEntity.description)

                callableStatement.executeQuery().use { result ->
                    result.next()
                    TransactionResponse(result.getLong("limiteRetorno"), result.getLong("saldoRetorno"))
                }
            }
        }
    }
}
