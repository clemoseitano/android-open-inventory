package com.coptimize.openinventory.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.SaleWithItems
import com.coptimize.openinventory.data.repository.SaleRepository
import com.coptimize.openinventory.ui.formatAsDateForDatabaseQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class SalesReportUiState(
    val startDate: Date = Date(), // Hold Date objects directly
    val endDate: Date = Date(),   // Hold Date objects directly
    val salesWithItems: List<SaleWithItems> = emptyList(),
    val totalSalesValue: Double = 0.0,
    val totalTransactions: Int = 0
)

@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    // Helper to get the start of the day for consistent queries
    private fun getStartOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Helper to get the end of the day
    private fun getEndOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    private val _dateRange = MutableStateFlow(
        getStartOfDay(Date()) to getEndOfDay(Date())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SalesReportUiState> = _dateRange.flatMapLatest { (startDate, endDate) ->
        // Directly call the repository with the Date objects
        saleRepository.getSalesWithItemsInRange(startDate.time.formatAsDateForDatabaseQuery(),
            endDate.time.formatAsDateForDatabaseQuery())
            .map { salesList ->
                SalesReportUiState(
                    startDate = startDate,
                    endDate = endDate,
                    salesWithItems = salesList,
                    totalSalesValue = salesList.sumOf { it.sale.total },
                    totalTransactions = salesList.size
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = SalesReportUiState()
    )

    // The UI provides Long milliseconds from the date picker
    fun onDateRangeSelected(startDateMillis: Long, endDateMillis: Long) {
        val newStartDate = getStartOfDay(Date(startDateMillis))
        val newEndDate = getEndOfDay(Date(endDateMillis))

        // Ensure start is not after end
        _dateRange.value = if (newStartDate.after(newEndDate)) {
            newStartDate to newStartDate
        } else {
            newStartDate to newEndDate
        }
    }
}