package com.felipejdias.rinhabackend2024q1.service.impl

import com.felipejdias.rinhabackend2024q1.db.repository.StatementRepository
import com.felipejdias.rinhabackend2024q1.domain.ClientStatement
import com.felipejdias.rinhabackend2024q1.exception.ClientNotFoundException
import com.felipejdias.rinhabackend2024q1.service.StatementService

import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
class DefaultStatementService: StatementService {

    private var statementRepository = StatementRepository()

    override fun getClientStatement(clientId: Long): ClientStatement {
        return try {
            statementRepository.calculateClientStatement( clientId)
        } catch (ex: SQLException) {
            throw ClientNotFoundException()
        }
    }


}