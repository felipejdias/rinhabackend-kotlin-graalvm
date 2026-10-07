package com.felipejdias.rinhabackend2024q1.db.repository

import com.felipejdias.rinhabackend2024q1.db.model.TransactionEntity
import com.felipejdias.rinhabackend2024q1.db.model.toTransactionsStatement
import com.felipejdias.rinhabackend2024q1.domain.ClientStatement
import com.felipejdias.rinhabackend2024q1.exception.ClientNotFoundException
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

@Repository
class StatementRepository(private val dataSource: DataSource) {

    companion object {
        private const val QUERY = """SELECT c.balance, c.credit_limit, t.amount, t.type, t.created_at, t.description
                                     FROM client c
                                     LEFT JOIN transactions t ON c.client_id = t.client_id
                                     WHERE c.client_id = ? ORDER BY t.created_at DESC LIMIT 10"""
    }

    fun calculateClientStatement(clientId: Long): ClientStatement {
        return dataSource.connection.use { conn ->
            conn.prepareStatement(QUERY).use { preparedStatement ->
                preparedStatement.setLong(1, clientId)

                preparedStatement.executeQuery().use { resultSet ->
                    extractClientStatement(resultSet, clientId) ?: throw ClientNotFoundException()
                }
            }
        }
    }

    // null quando o cliente nao existe (nenhuma linha retornada)
    private fun extractClientStatement(resultSet: ResultSet, clientId: Long): ClientStatement? {
        val clientStatement = ClientStatement()
        val transactionsList = mutableListOf<TransactionEntity>()
        var clientFound = false

        while (resultSet.next()) {
            clientFound = true
            clientStatement.balance.total = resultSet.getLong("balance")
            clientStatement.balance.limit = resultSet.getLong("credit_limit")
            clientStatement.balance.statementDate = Instant.now().toString()

            // LEFT JOIN: cliente sem transacoes volta uma unica linha com as colunas da transacao nulas
            val type = resultSet.getString("type") ?: continue

            val transaction = TransactionEntity.Builder()
                .type(type)
                .amount(resultSet.getLong("amount"))
                .description(resultSet.getString("description"))
                .clientId(clientId)
                .createdAt(resultSet.getString("created_at"))
                .build()
            transactionsList.add(transaction)
        }

        if (!clientFound) return null

        clientStatement.recentTransactions = transactionsList.toTransactionsStatement()
        return clientStatement
    }
}
