package com.coptimize.openinventory.data.repository


interface CustomerRepository {
    /**
     * Adds a new customer to the database.
     * @return The unique ID of the newly created customer.
     */
    suspend fun addCustomer(name: String?, contact: String?, paymentMethod: String?): String

    suspend fun updateCustomer(customerName: String?, customerContact: String?, paymentMethod: String?, customerId: String)
}