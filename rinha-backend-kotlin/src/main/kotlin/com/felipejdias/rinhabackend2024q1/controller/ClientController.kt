package com.felipejdias.rinhabackend2024q1.controller

import com.felipejdias.rinhabackend2024q1.context.Context
import com.felipejdias.rinhabackend2024q1.domain.ClientStatement
import com.felipejdias.rinhabackend2024q1.exception.RequestValidationExceptions
import com.felipejdias.rinhabackend2024q1.exchange.TransactionRequest
import com.felipejdias.rinhabackend2024q1.exchange.TransactionResponse
import com.felipejdias.rinhabackend2024q1.service.StatementService
import com.felipejdias.rinhabackend2024q1.service.TransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clientes")
class ClientController {

    @Autowired
    private lateinit var statementService: StatementService

    @Autowired
    private lateinit var transactionService: TransactionService


    @GetMapping("/{id}/extrato")
    fun getClientStatements(@PathVariable id: Long): ClientStatement {
        return statementService.getClientStatement(clientId = id)

    }

    @PostMapping("/{clientId}/transacoes")
    fun createTransaction(
        @PathVariable("clientId") clientId: Long,
        @RequestBody transaction: TransactionRequest
    ): TransactionResponse {
        validateTransaction(transaction)
        return transactionService.create(Context(clientId = clientId, request = transaction))

    }

    private fun validateTransaction(transaction: TransactionRequest) {
        if (transaction.amount == null) { throw RequestValidationExceptions("amount must not be null", field="amount") }
        if (transaction.description == null) { throw RequestValidationExceptions("description must not be null", field="description") }
        if (transaction.type == null) { throw RequestValidationExceptions("type must not be null",  field="type") }
        if (transaction.amount <= 0)  { throw RequestValidationExceptions("amount must be greater than zero", field="amount") }
        if (transaction.description.isEmpty() || transaction.description.length > 10 ) { throw RequestValidationExceptions("description must be between 1 and 10 characters", field = "description") }
        if (!transaction.type.matches(Regex("^[cd]$"))) { throw  RequestValidationExceptions("type must be 'c' or 'd'", field="type") }
        if ((transaction.amount % 1) != 0.0) { throw RequestValidationExceptions("amount must be an integer", field="amount") }
    }
}