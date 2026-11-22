package com.example.data.model

enum class UserRole {
    USER,
    ADMIN
}

enum class PaymentMethod(val displayName: String) {
    JAZZCASH("JazzCash"),
    EASYPAISA("EasyPaisa")
}

enum class OrderStatus(val label: String) {
    PENDING_PAYMENT("Pending Payment"),
    PAYMENT_UNDER_REVIEW("Payment Under Review"),
    PAYMENT_APPROVED("Payment Approved"),
    PROCESSING("Processing"),
    DELIVERED("Delivered"),
    REJECTED("Rejected");

    val isPending: Boolean
        get() = this == PENDING_PAYMENT || this == PAYMENT_UNDER_REVIEW

    val isTerminal: Boolean
        get() = this == DELIVERED || this == REJECTED
}

enum class RewardStatus(val label: String) {
    PENDING_MANUAL_FULFILMENT("Pending Manual Fulfilment"),
    FULFILLED("Fulfilled")
}
