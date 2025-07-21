// data/models/AdminOrderModels.kt
package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderStatusUpdate(
    val status: OrderStatus
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BatchOrderStatusUpdate(
    val order_ids: List<String>,
    val status: OrderStatus
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BatchUpdateResult(
    val success_count: Int,
    val error_count: Int,
    val errors: List<String> = emptyList()
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderDailyStats(
    val date: String,
    val total_orders: Int,
    val total_revenue: String,
    val orders_by_status: Map<String, Int>,
    val avg_order_value: String,
    val peak_hour: String? = null,
    val most_popular_items: List<PopularMenuItem> = emptyList()
) {
    val totalRevenueAmount: Double
        get() = total_revenue.toDoubleOrNull() ?: 0.0

    val avgOrderValueAmount: Double
        get() = avg_order_value.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PopularMenuItem(
    val menu_item_id: String,
    val menu_item_name: String,
    val quantity_sold: Int,
    val revenue: String
) {
    val revenueAmount: Double
        get() = revenue.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ActiveOrdersSummary(
    val pending_count: Int,
    val in_preparation_count: Int,
    val ready_count: Int,
    val urgent_orders: List<String> = emptyList(), // IDs de pedidos urgentes
    val avg_preparation_time: String? = null, // en minutos
    val oldest_pending_order: String? = null // timestamp
) {
    val avgPreparationTimeMinutes: Double
        get() = avg_preparation_time?.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PerformanceMetrics(
    val total_orders: Int,
    val completed_orders: Int,
    val cancelled_orders: Int,
    val total_revenue: String,
    val avg_order_value: String,
    val completion_rate: Double, // porcentaje
    val cancellation_rate: Double, // porcentaje
    val avg_preparation_time: String, // en minutos
    val customer_satisfaction: Double? = null, // si tienes sistema de rating
    val busiest_hours: List<BusyHour> = emptyList(),
    val revenue_by_day: List<DailyRevenue> = emptyList()
) {
    val totalRevenueAmount: Double
        get() = total_revenue.toDoubleOrNull() ?: 0.0

    val avgOrderValueAmount: Double
        get() = avg_order_value.toDoubleOrNull() ?: 0.0

    val avgPreparationTimeMinutes: Double
        get() = avg_preparation_time.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BusyHour(
    val hour: Int, // 0-23
    val order_count: Int,
    val revenue: String
) {
    val revenueAmount: Double
        get() = revenue.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DailyRevenue(
    val date: String,
    val order_count: Int,
    val revenue: String,
    val avg_order_value: String
) {
    val revenueAmount: Double
        get() = revenue.toDoubleOrNull() ?: 0.0

    val avgOrderValueAmount: Double
        get() = avg_order_value.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderNotification(
    val id: String,
    val type: NotificationType,
    val order_id: String,
    val message: String,
    val timestamp: String,
    val is_read: Boolean = false,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

@Serializable
enum class NotificationType {
    NEW_ORDER,
    ORDER_URGENT,
    ORDER_CANCELLED,
    PAYMENT_ISSUE,
    SYSTEM_ALERT
}

@Serializable
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderFilter(
    val status: OrderStatus? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val customer_name: String? = null,
    val min_amount: String? = null,
    val max_amount: String? = null,
    val sort_by: OrderSortField = OrderSortField.CREATED_AT,
    val sort_order: SortOrder = SortOrder.DESC
)

@Serializable
enum class OrderSortField {
    CREATED_AT,
    UPDATED_AT,
    TOTAL_AMOUNT,
    STATUS,
    CUSTOMER_NAME
}

@Serializable
enum class SortOrder {
    ASC,
    DESC
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderExportRequest(
    val format: ExportFormat = ExportFormat.CSV,
    val start_date: String? = null,
    val end_date: String? = null,
    val status: OrderStatus? = null,
    val include_customer_data: Boolean = true,
    val include_items_detail: Boolean = true
)

@Serializable
enum class ExportFormat {
    CSV,
    EXCEL,
    PDF
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CustomerOrderSummary(
    val customer_id: String,
    val customer_name: String,
    val customer_email: String,
    val total_orders: Int,
    val total_spent: String,
    val avg_order_value: String,
    val last_order_date: String,
    val favorite_items: List<String> = emptyList(),
    val customer_since: String
) {
    val totalSpentAmount: Double
        get() = total_spent.toDoubleOrNull() ?: 0.0

    val avgOrderValueAmount: Double
        get() = avg_order_value.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MenuItemPerformance(
    val menu_item_id: String,
    val menu_item_name: String,
    val category_name: String,
    val times_ordered: Int,
    val total_revenue: String,
    val avg_rating: Double? = null,
    val last_ordered: String? = null,
    val trend: ItemTrend = ItemTrend.STABLE
) {
    val totalRevenueAmount: Double
        get() = total_revenue.toDoubleOrNull() ?: 0.0
}

@Serializable
enum class ItemTrend {
    RISING,
    STABLE,
    DECLINING
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderTimeAnalysis(
    val avg_preparation_time: String, // en minutos
    val avg_delivery_time: String? = null, // si hay delivery
    val peak_hours: List<Int>, // horas del día con más pedidos
    val slow_hours: List<Int>, // horas con menos pedidos
    val preparation_time_by_status: Map<String, String>,
    val efficiency_score: Double // 0-100
) {
    val avgPreparationTimeMinutes: Double
        get() = avg_preparation_time.toDoubleOrNull() ?: 0.0

    val avgDeliveryTimeMinutes: Double?
        get() = avg_delivery_time?.toDoubleOrNull()
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KitchenDashboard(
    val active_orders: List<Order>,
    val urgent_orders: List<Order>,
    val avg_wait_time: String,
    val orders_this_hour: Int,
    val completion_rate_today: Double,
    val staff_on_duty: Int? = null,
    val equipment_status: Map<String, String> = emptyMap()
) {
    val avgWaitTimeMinutes: Double
        get() = avg_wait_time.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RevenueAnalysis(
    val daily_revenue: List<DailyRevenue>,
    val monthly_revenue: String,
    val revenue_growth: Double, // porcentaje vs período anterior
    val top_revenue_days: List<DailyRevenue>,
    val revenue_by_category: Map<String, String>,
    val projected_monthly_revenue: String? = null
) {
    val monthlyRevenueAmount: Double
        get() = monthly_revenue.toDoubleOrNull() ?: 0.0

    val projectedMonthlyRevenueAmount: Double?
        get() = projected_monthly_revenue?.toDoubleOrNull()
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class AdminAlert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: String,
    val is_resolved: Boolean = false,
    val action_required: Boolean = false,
    val related_order_id: String? = null
)

@Serializable
enum class AlertType {
    SYSTEM,
    ORDER_ISSUE,
    PAYMENT_PROBLEM,
    INVENTORY_LOW,
    CUSTOMER_COMPLAINT,
    PERFORMANCE_WARNING
}

@Serializable
enum class AlertSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

// Extensiones útiles para trabajar con órdenes
fun Order.isUrgent(minutesThreshold: Int = 30): Boolean {
    return status == OrderStatus.PENDING &&
            isOlderThan(created_at, minutesThreshold)
}

fun Order.getWaitTimeMinutes(): Long? {
    return try {
        // Implementar cálculo real del tiempo de espera
        // Por ahora retorna null
        null
    } catch (e: Exception) {
        null
    }
}

fun Order.canBeModified(): Boolean {
    return status in listOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION)
}

fun Order.getStatusPriority(): Int {
    return when (status) {
        OrderStatus.PENDING -> 1
        OrderStatus.IN_PREPARATION -> 2
        OrderStatus.READY -> 3
        OrderStatus.DELIVERED -> 4
        OrderStatus.CANCELLED -> 5
    }
}

private fun isOlderThan(timestamp: String, minutes: Int): Boolean {
    return try {
        // Implementar lógica real de comparación de tiempo
        // Por ahora retorna false
        false
    } catch (e: Exception) {
        false
    }
}