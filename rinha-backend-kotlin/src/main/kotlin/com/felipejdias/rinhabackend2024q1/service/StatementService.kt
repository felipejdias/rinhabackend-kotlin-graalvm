package com.felipejdias.rinhabackend2024q1.service

import com.felipejdias.rinhabackend2024q1.domain.ClientStatement

interface StatementService {

    fun getClientStatement(clientId: Long): ClientStatement
}