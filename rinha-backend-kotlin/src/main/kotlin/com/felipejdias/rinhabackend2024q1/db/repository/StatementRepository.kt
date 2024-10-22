package com.felipejdias.rinhabackend2024q1.db.repository

import com.felipejdias.rinhabackend2024q1.db.connection.DatabaseConnection
import com.felipejdias.rinhabackend2024q1.db.model.TransactionEntity
import com.felipejdias.rinhabackend2024q1.db.model.toTransactionsStatement
import com.felipejdias.rinhabackend2024q1.domain.ClientStatement
import com.felipejdias.rinhabackend2024q1.domain.isClientInvalid
import com.felipejdias.rinhabackend2024q1.exception.ClientNotFoundException
import java.sql.ResultSet
import java.time.LocalDateTime

class StatementRepository {

    companion object {
        private const val QUERY = """SELECT c.balance, c.credit_limit, t.amount, t.type, t.created_at, t.description
                                     FROM client c 
                                     LEFT JOIN transactions t ON c.client_id = t.client_id 
                                     WHERE c.client_id = ? ORDER BY t.created_at DESC LIMIT 10"""
    }

    fun calculateClientStatement(clientId: Long): ClientStatement {
        return DatabaseConnection.getDataSource().connection.use { conn ->
            conn.prepareStatement(QUERY).use { preparedStatement ->
                preparedStatement.setLong(1, clientId)

                val resultSet = preparedStatement.executeQuery()
                val clientStatement = extractClientStatement(resultSet, clientId)

                if (clientStatement.isClientInvalid()) throw ClientNotFoundException()

                clientStatement
            }
        }
    }

    private fun extractClientStatement(resultSet: ResultSet, clientId: Long): ClientStatement {
        val clientStatement = ClientStatement()
        val transactionsList = mutableListOf<TransactionEntity>()

        while (resultSet.next()) {
            clientStatement.balance.total = resultSet.getLong("balance")
            clientStatement.balance.limit = resultSet.getLong("credit_limit")
            clientStatement.balance.statementDate = LocalDateTime.now().toString()

            val transaction = TransactionEntity.Builder()
                .type(resultSet.getString("type").orEmpty())
                .amount(resultSet.getLong("amount").or(0))
                .description(resultSet.getString("description").orEmpty())
                .clientId(clientId)
                .createdAt(resultSet.getString("created_at").orEmpty())
                .build()
            transactionsList.add(transaction)
        }

        clientStatement.recentTransactions = transactionsList.toTransactionsStatement()
        return clientStatement
    }
}
