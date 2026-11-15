package org.b3.wataju.order.application

import org.b3.wataju.order.domain.*

/** The server owns customer identity, snapshots, prices and final order acceptance. */
interface OrderGateway {
    suspend fun account(): Account
    suspend fun saveDestination(destination: Destination): Account
    suspend fun quote(request: QuoteRequest): Quote
    suspend fun place(quoteId: String, key: String): Order
    suspend fun history(): List<Order>
}
