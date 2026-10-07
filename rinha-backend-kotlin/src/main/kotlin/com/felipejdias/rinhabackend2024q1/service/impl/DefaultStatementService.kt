package com.felipejdias.rinhabackend2024q1.service.impl

import com.felipejdias.rinhabackend2024q1.db.repository.StatementRepository
import com.felipejdias.rinhabackend2024q1.domain.ClientStatement
import com.felipejdias.rinhabackend2024q1.service.StatementService

import org.springframework.stereotype.Service

@Service
class DefaultStatementService(private val statementRepository: StatementRepository) : StatementService {

    override fun getClientStatement(clientId: Long): ClientStatement {
        return statementRepository.calculateClientStatement(clientId)
    }

}
