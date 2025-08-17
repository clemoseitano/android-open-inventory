package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.auth.AuthDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthCustomerRepositoryImpl @Inject constructor(
    private val db: AuthDb
) : CustomerRepository {
    override suspend fun addCustomer(name: String?, contact: String?, paymentMethod: String?): String {
        return withContext(Dispatchers.IO) {
            db.customerQueries.insert(
                name = name,
                contact = contact,
                payment_method = paymentMethod
            )
            val newCustomerId = db.customerQueries.getLastCreatedId().executeAsOneOrNull()?:""
            newCustomerId
        }
    }

    override suspend fun updateCustomer(
        customerName: String?,
        customerContact: String?,
        paymentMethod: String?,
        customerId: String
    ) {
        return withContext(Dispatchers.IO) {
            db.customerQueries.update(
                name = customerName,
                contact = customerContact,
                payment_method = paymentMethod,
                id=customerId
            )
        }
    }
}