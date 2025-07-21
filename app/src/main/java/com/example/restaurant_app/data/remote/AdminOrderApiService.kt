// data/remote/AdminOrderApiService.kt
package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AdminOrderApiService {

    // ============== GESTIÓN BÁSICA DE PEDIDOS ==============

    /**
     * Obtener todos los pedidos (solo administradores)
     */
    @GET("admin/orders/")
    suspend fun getAllOrders(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("status") status: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<OrderList>

    /**
     * Obtener pedido específico con información completa
     */
    @GET("admin/orders/{order_id}")
    suspend fun getOrderById(@Path("order_id") orderId: String): Response<Order>

    /**
     * Actualizar estado de un pedido
     */
    @PATCH("admin/orders/{order_id}/status")
    suspend fun updateOrderStatus(
        @Path("order_id") orderId: String,
        @Body statusUpdate: OrderStatusUpdate
    ): Response<Order>

    /**
     * Actualizar múltiples pedidos (operación batch)
     */
    @PATCH("admin/orders/batch-status")
    suspend fun batchUpdateOrderStatus(
        @Body batchUpdate: BatchOrderStatusUpdate
    ): Response<BatchUpdateResult>

    /**
     * Eliminar/cancelar pedido (solo en casos especiales)
     */
    @DELETE("admin/orders/{order_id}")
    suspend fun deleteOrder(@Path("order_id") orderId: String): Response<Unit>

    // ============== BÚSQUEDA Y FILTROS ==============

    /**
     * Buscar pedidos por información del cliente
     */
    @GET("admin/orders/search")
    suspend fun searchOrdersByCustomer(
        @Query("query") query: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<OrderList>

    /**
     * Obtener pedidos por rango de fechas
     */
    @GET("admin/orders/date-range")
    suspend fun getOrdersByDateRange(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("status") status: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<OrderList>

    /**
     * Filtros avanzados para pedidos
     */
    @POST("admin/orders/filter")
    suspend fun filterOrders(
        @Body filter: OrderFilter,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<OrderList>

    // ============== ESTADÍSTICAS Y ANÁLISIS ==============

    /**
     * Estadísticas diarias
     */
    @GET("admin/orders/stats/daily")
    suspend fun getDailyStats(
        @Query("date") date: String? = null
    ): Response<OrderDailyStats>

    /**
     * Resumen de pedidos activos
     */
    @GET("admin/orders/active/summary")
    suspend fun getActiveOrdersSummary(): Response<ActiveOrdersSummary>

    /**
     * Métricas de rendimiento
     */
    @GET("admin/orders/performance")
    suspend fun getPerformanceMetrics(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<PerformanceMetrics>

    /**
     * Análisis de tiempo de preparación
     */
    @GET("admin/orders/time-analysis")
    suspend fun getTimeAnalysis(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<OrderTimeAnalysis>

    /**
     * Análisis de ingresos
     */
    @GET("admin/orders/revenue-analysis")
    suspend fun getRevenueAnalysis(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("group_by") groupBy: String = "day"
    ): Response<RevenueAnalysis>

    /**
     * Dashboard de cocina
     */
    @GET("admin/orders/kitchen-dashboard")
    suspend fun getKitchenDashboard(): Response<KitchenDashboard>

    // ============== GESTIÓN DE PEDIDOS ESPECIALES ==============

    /**
     * Obtener pedidos urgentes
     */
    @GET("admin/orders/urgent")
    suspend fun getUrgentOrders(
        @Query("minutes_threshold") minutesThreshold: Int = 30
    ): Response<OrderList>

    /**
     * Marcar pedido como prioritario
     */
    @PATCH("admin/orders/{order_id}/priority")
    suspend fun setPriorityOrder(
        @Path("order_id") orderId: String,
        @Query("priority") priority: Boolean = true
    ): Response<Order>

    /**
     * Obtener pedidos por cliente específico
     */
    @GET("admin/orders/customer/{customer_id}")
    suspend fun getOrdersByCustomer(
        @Path("customer_id") customerId: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<OrderList>

    // ============== ANÁLISIS DE CLIENTES ==============

    /**
     * Resumen de pedidos por cliente
     */
    @GET("admin/customers/{customer_id}/summary")
    suspend fun getCustomerOrderSummary(
        @Path("customer_id") customerId: String
    ): Response<CustomerOrderSummary>

    /**
     * Clientes más activos
     */
    @GET("admin/customers/top")
    suspend fun getTopCustomers(
        @Query("limit") limit: Int = 10,
        @Query("period") period: String = "month"
    ): Response<List<CustomerOrderSummary>>

    // ============== ANÁLISIS DE PRODUCTOS ==============

    /**
     * Rendimiento de items del menú
     */
    @GET("admin/menu-items/performance")
    suspend fun getMenuItemsPerformance(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("sort_by") sortBy: String = "times_ordered"
    ): Response<List<MenuItemPerformance>>

    /**
     * Items más populares
     */
    @GET("admin/menu-items/popular")
    suspend fun getPopularMenuItems(
        @Query("limit") limit: Int = 10,
        @Query("period") period: String = "week"
    ): Response<List<PopularMenuItem>>

    // ============== NOTIFICACIONES Y ALERTAS ==============

    /**
     * Obtener notificaciones de pedidos
     */
    @GET("admin/orders/notifications")
    suspend fun getOrderNotifications(
        @Query("unread_only") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 50
    ): Response<List<OrderNotification>>

    /**
     * Marcar notificación como leída
     */
    @PATCH("admin/notifications/{notification_id}/read")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: String
    ): Response<Unit>

    /**
     * Obtener alertas del sistema
     */
    @GET("admin/alerts")
    suspend fun getSystemAlerts(
        @Query("severity") severity: String? = null,
        @Query("unresolved_only") unresolvedOnly: Boolean = true
    ): Response<List<AdminAlert>>

    // ============== EXPORTACIÓN DE DATOS ==============

    /**
     * Exportar pedidos a CSV
     */
    @GET("admin/orders/export/csv")
    suspend fun exportOrdersToCSV(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("status") status: String? = null,
        @Query("include_customer_data") includeCustomerData: Boolean = true
    ): Response<String>

    /**
     * Exportar estadísticas
     */
    @POST("admin/orders/export")
    suspend fun exportOrders(
        @Body exportRequest: OrderExportRequest
    ): Response<String>

    /**
     * Generar reporte PDF
     */
    @GET("admin/orders/report/pdf")
    suspend fun generatePDFReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("report_type") reportType: String = "summary"
    ): Response<String> // URL del PDF generado

    // ============== CONFIGURACIÓN DEL SISTEMA ==============

    /**
     * Obtener configuración de tiempos
     */
    @GET("admin/config/timing")
    suspend fun getTimingConfig(): Response<Map<String, Any>>

    /**
     * Actualizar configuración de tiempos
     */
    @PUT("admin/config/timing")
    suspend fun updateTimingConfig(
        @Body config: Map<String, Any>
    ): Response<Map<String, Any>>

    /**
     * Obtener estadísticas en tiempo real
     */
    @GET("admin/orders/realtime-stats")
    suspend fun getRealtimeStats(): Response<Map<String, Any>>

    // ============== GESTIÓN DE PERSONAL ==============

    /**
     * Obtener estadísticas de staff
     */
    @GET("admin/staff/performance")
    suspend fun getStaffPerformance(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<Map<String, Any>>

    /**
     * Registrar tiempo de preparación
     */
    @POST("admin/orders/{order_id}/preparation-time")
    suspend fun recordPreparationTime(
        @Path("order_id") orderId: String,
        @Body timeData: Map<String, Any>
    ): Response<Unit>

    // ============== WEBHOOKS Y INTEGRACIONES ==============

    /**
     * Configurar webhooks para eventos de pedidos
     */
    @POST("admin/webhooks/orders")
    suspend fun configureOrderWebhooks(
        @Body webhookConfig: Map<String, Any>
    ): Response<Unit>

    /**
     * Obtener logs de actividad
     */
    @GET("admin/orders/activity-logs")
    suspend fun getActivityLogs(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("action_type") actionType: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<List<Map<String, Any>>>
}