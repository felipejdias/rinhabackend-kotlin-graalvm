package com.felipejdias.rinhabackend2024q1.db.model

data class Client(
    val id: Long,
    val name: String,
    var limit: Long,
    var balance: Long,
    var version: Long){
    override fun toString(): String {
        return "Client(id=$id, name='$name', limit=$limit, balance=$balance)"
    }
}
