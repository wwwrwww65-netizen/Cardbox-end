package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "shop_name") val shopName: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String,
    @Json(name = "address") val address: String = "العنوان الرئيسي"
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "otp_code") val otpCode: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "token") val token: String? = null,
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: Boolean? = null,
    @Json(name = "test_otp_code") val testOtpCode: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "network_id") val networkId: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "network_code") val networkCode: String? = null,
    @Json(name = "governorate") val governorate: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "status") val status: String? = null, // "pending", "active", "suspended", "approved"
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "credit_limit") val creditLimit: Double? = null,
    @Json(name = "current_debt") val currentDebt: Double? = null,
    @Json(name = "available_balance") val availableBalance: Double? = null
)

@JsonClass(generateAdapter = true)
data class PackageDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "package_id") val packageId: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "pos_price") val posPrice: Double? = null,
    @Json(name = "validity") val validity: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "stock") val stock: Int? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseRequestDto(
    @Json(name = "network_id") val networkId: Int,
    @Json(name = "package_id") val packageId: Int,
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    @Json(name = "send_sms") val sendSms: Boolean = true,
    @Json(name = "payment_method") val paymentMethod: String = "network_credit"
)

@JsonClass(generateAdapter = true)
data class VoucherDto(
    @Json(name = "voucher_code") val voucherCode: String? = null,
    @Json(name = "pin") val pin: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "expiry_date") val expiryDate: String? = null,
    @Json(name = "transaction_id") val transactionId: String? = null
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "otp_code") val otpCode: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "shop_name") val shopName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "commercial_reg") val commercialReg: String? = null
)

@JsonClass(generateAdapter = true)
data class WalletDto(
    @Json(name = "balance") val balance: Double? = 0.0,
    @Json(name = "recent_transactions") val recentTransactions: List<WalletTransactionDto>? = emptyList(),
    @Json(name = "currency") val currency: String? = "ريال"
)

@JsonClass(generateAdapter = true)
data class WalletTransactionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "amount") val amount: Double? = 0.0,
    @Json(name = "type") val type: String? = null, // "credit" or "debit"
    @Json(name = "bank_name") val bankName: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TopUpRequestDto(
    @Json(name = "amount") val amount: Double,
    @Json(name = "bank_name") val bankName: String? = "حوالة بنكية",
    @Json(name = "reference_number") val referenceNumber: String
)

@JsonClass(generateAdapter = true)
data class JoinNetworkRequestDto(
    @Json(name = "network_id") val networkId: Int
)

@JsonClass(generateAdapter = true)
data class SaleRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "voucher_code") val voucherCode: String? = null,
    @Json(name = "pin") val pin: String? = null,
    @Json(name = "network_name") val networkName: String? = null,
    @Json(name = "package_name") val packageName: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "purchased_at") val purchasedAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "payment_method") val paymentMethod: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiResponseBase(
    @Json(name = "status") val status: Boolean? = true,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseResponseDto(
    @Json(name = "message") val message: String? = null,
    @Json(name = "vouchers") val vouchers: List<VoucherDto>? = null,
    @Json(name = "network_name") val networkName: String? = null,
    @Json(name = "total_deducted") val totalDeducted: Double? = null,
    @Json(name = "sms_sent") val smsSent: Boolean? = null,
    @Json(name = "sms_message") val smsMessage: String? = null
)
