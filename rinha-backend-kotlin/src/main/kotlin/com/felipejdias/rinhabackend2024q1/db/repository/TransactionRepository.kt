package com.felipejdias.rinhabackend2024q1.db.repository

import com.felipejdias.rinhabackend2024q1.db.connection.DatabaseConnection
import com.felipejdias.rinhabackend2024q1.db.model.TransactionEntity
import com.felipejdias.rinhabackend2024q1.exchange.TransactionResponse
import java.sql.CallableStatement

class TransactionRepository {

    fun registerTransaction(idCliente: Long, transactionEntity: TransactionEntity): TransactionResponse {
        var transacaoResponse: TransactionResponse? = null
        return DatabaseConnection.getDataSource().connection.use { connection ->
                val procedureCall = "{call public.register_transaction(?, ?, ?, ?)}"
                val callableStatement: CallableStatement = connection.prepareCall(procedureCall)

                callableStatement.setInt(1, idCliente.toInt())
                callableStatement.setString(2, transactionEntity.type)
                callableStatement.setLong(3, transactionEntity.amount)
                callableStatement.setString(4, transactionEntity.description)

                callableStatement.executeQuery().use { result ->
                    if (result.next()) {
                        transacaoResponse = TransactionResponse(result.getLong("limiteRetorno"),
                            result.getLong("saldoRetorno"))
                    }
                }

              transacaoResponse!!
            }
    }


}