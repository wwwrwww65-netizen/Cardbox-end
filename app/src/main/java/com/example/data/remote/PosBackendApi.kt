package com.example.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * جميع الطلبات التي تتطلب مصادقة (Authentication) يتم إضافة التوكن تلقائياً
 * عبر AuthInterceptor الموجود في RetrofitClient، لذلك لا حاجة لتمرير @Header هنا.
 */
interface PosBackendApi {

    @POST("pos/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponseDto>

    @POST("pos/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponseDto>

    @POST("pos/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<LoginResponseDto>

    @POST("pos/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<LoginResponseDto>

    @POST("pos/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponseBase>

    @POST("pos/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponseBase>

    @GET("pos/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @POST("pos/profile")
    suspend fun updateProfile(
        @Body request: UserProfileDto
    ): Response<ApiResponseBase>

    @GET("pos/wallet/balance")
    suspend fun getWallet(): Response<ResponseBody>

    @GET("pos/wallet/transactions")
    suspend fun getWalletTransactions(): Response<ResponseBody>

    @GET("pos/wallet/history")
    suspend fun getWalletHistory(): Response<ResponseBody>

    @POST("pos/wallet/recharge")
    suspend fun requestTopUp(
        @Body request: TopUpRequestDto
    ): Response<ResponseBody>

    @GET("pos/networks")
    suspend fun getNetworks(): Response<ResponseBody>

    @GET("pos/networks/my-networks")
    suspend fun getMyNetworks(): Response<ResponseBody>

    @POST("pos/networks/join")
    suspend fun joinNetwork(
        @Body request: JoinNetworkRequestDto
    ): Response<ResponseBody>

    @GET("pos/networks/{network_id}/packages")
    suspend fun getNetworkPackages(
        @Path("network_id") networkId: String
    ): Response<ResponseBody>

    @POST("pos/vouchers/purchase")
    suspend fun purchaseVoucher(
        @Body request: PurchaseRequestDto
    ): Response<PurchaseResponseDto>

    @GET("pos/sales/history")
    suspend fun getSalesHistory(
        @Query("filter") filter: String? = null
    ): Response<ResponseBody>
}
