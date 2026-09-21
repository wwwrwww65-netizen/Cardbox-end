package com.example.data.remote

import com.example.data.model.JoinStatus
import com.example.data.model.NetworkItem
import com.example.data.model.VoucherPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

class MikroTikApiService {

    suspend fun loginApi(phone: String, pass: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.login(LoginRequest(phone, pass))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.token ?: body.accessToken ?: ""
                if (token.isNotBlank()) {
                    RetrofitClient.authToken = token
                }
                ApiResponse(true, body.message ?: "تم تسجيل الدخول بالسيرفر بنجاح", data = token)
            } else {
                ApiResponse(false, "بيانات الدخول غير صحيحة. يرجى التأكد من رقم الجوال وكلمة المرور.")
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصالك بالإنترنت والمحاولة مرة أخرى.")
        }
    }

    private fun parseCleanErrorMessage(rawError: String, responseCode: Int): String {
        if (rawError.isBlank()) return "تعذر إنشاء الحساب بالسيرفر (كود: $responseCode)"
        try {
            if (rawError.contains("\"message\"")) {
                val jsonObject = org.json.JSONObject(rawError)
                if (jsonObject.has("message")) {
                    val msg = jsonObject.getString("message")
                    if (msg.contains("SQLSTATE") || msg.contains("Constraint") || msg.contains("database.sqlite")) {
                        return "خطأ في حقول البيانات بالسيرفر (كود: $responseCode). يرجى التأكد من صحة رقم الجوال."
                    }
                    if (msg.isNotBlank()) return msg
                }
            }
        } catch (e: Exception) {
            // ignore json parse error
        }
        if (rawError.contains("SQLSTATE") || rawError.contains("Constraint") || rawError.length > 80) {
            return "تعذر إنشاء الحساب بالسيرفر (كود: $responseCode)"
        }
        return rawError
    }

    suspend fun registerApi(name: String, shopName: String, phone: String, pass: String, address: String = ""): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val reqAddress = address.ifBlank { "العنوان الرئيسي" }
            val response = RetrofitClient.api.register(
                RegisterRequest(
                    name = name,
                    shopName = shopName,
                    phone = phone,
                    password = pass,
                    address = reqAddress
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val otpCode = body.testOtpCode ?: ""
                ApiResponse(true, body.message ?: "تم تسجيل الحساب بنجاح، يرجى إدخال كود التحقق (OTP) المكون من 6 أرقام", data = otpCode)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                val cleanMsg = parseCleanErrorMessage(errorMsg, response.code())
                ApiResponse(false, cleanMsg)
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت والمحاولة مجدداً.")
        }
    }

    suspend fun verifyOtpApi(phone: String, otpCode: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.verifyOtp(VerifyOtpRequest(phone = phone, otpCode = otpCode))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.token ?: body.accessToken ?: ""
                if (token.isNotBlank()) {
                    RetrofitClient.authToken = token
                }
                ApiResponse(true, body.message ?: "تم التحقق من الحساب بنجاح من السيرفر", data = token)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                val cleanMsg = parseCleanErrorMessage(errorMsg, response.code())
                if (cleanMsg == errorMsg) {
                    ApiResponse(false, "رمز التحقق المدخل غير صحيح، يرجى المحاولة مرة أخرى.")
                } else {
                    ApiResponse(false, cleanMsg)
                }
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصالك بالإنترنت.")
        }
    }

    suspend fun forgotPasswordApi(phone: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.forgotPassword(ForgotPasswordRequest(phone = phone))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    ApiResponse(false, body.message ?: "رقم الهاتف غير مسجل لدينا، يرجى التأكد منه.")
                } else {
                    val otpCode = body.testOtpCode ?: ""
                    ApiResponse(true, body.message ?: "تم إرسال رمز استعادة كلمة المرور", data = otpCode)
                }
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val cleanMsg = parseCleanErrorMessage(errBody, response.code())
                ApiResponse(false, cleanMsg.ifBlank { "رقم الهاتف غير مسجل لدينا، يرجى التأكد منه." })
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصالك بالإنترنت.")
        }
    }

    suspend fun resetPasswordApi(phone: String, otpCode: String, newPass: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.resetPassword(ResetPasswordRequest(phone = phone, otpCode = otpCode, newPassword = newPass))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    ApiResponse(false, body.message ?: "كود التحقق غير صحيح")
                } else {
                    ApiResponse(true, body.message ?: "تم إعادة تعيين كلمة المرور بنجاح")
                }
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val cleanMsg = parseCleanErrorMessage(errBody, response.code())
                ApiResponse(false, cleanMsg.ifBlank { "رمز التحقق غير صحيح أو البيانات غير مكتملة." })
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم، يرجى التحقق من الاتصال بالإنترنت.")
        }
    }

    suspend fun changePasswordApi(currentPass: String, newPass: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.changePassword(request = ChangePasswordRequest(currentPassword = currentPass, newPassword = newPass))
            if (response.isSuccessful) {
                ApiResponse(true, response.body()?.message ?: "تم تغيير كلمة المرور بنجاح من السيرفر")
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val cleanMsg = parseCleanErrorMessage(errBody, response.code())
                ApiResponse(false, cleanMsg.ifBlank { "كلمة المرور الحالية غير صحيحة." })
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصالك بالإنترنت.")
        }
    }

    private fun parseNetworkListFromJson(jsonStr: String, isMyNetworks: Boolean = false): List<NetworkItem> {
        if (jsonStr.isBlank()) return emptyList()
        val jsonArray = try {
            val trimmed = jsonStr.trim()
            when {
                trimmed.startsWith("[") -> org.json.JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = org.json.JSONObject(trimmed)
                    when {
                        obj.has("value") && obj.get("value") is org.json.JSONArray -> obj.getJSONArray("value")
                        obj.has("data") && obj.get("data") is org.json.JSONArray -> obj.getJSONArray("data")
                        obj.has("networks") && obj.get("networks") is org.json.JSONArray -> obj.getJSONArray("networks")
                        obj.has("my_networks") && obj.get("my_networks") is org.json.JSONArray -> obj.getJSONArray("my_networks")
                        obj.has("result") && obj.get("result") is org.json.JSONArray -> obj.getJSONArray("result")
                        obj.has("results") && obj.get("results") is org.json.JSONArray -> obj.getJSONArray("results")
                        obj.has("items") && obj.get("items") is org.json.JSONArray -> obj.getJSONArray("items")
                        obj.has("list") && obj.get("list") is org.json.JSONArray -> obj.getJSONArray("list")
                        obj.has("data") && obj.optJSONObject("data") != null -> {
                            val inner = obj.getJSONObject("data")
                            when {
                                inner.has("networks") && inner.get("networks") is org.json.JSONArray -> inner.getJSONArray("networks")
                                inner.has("data") && inner.get("data") is org.json.JSONArray -> inner.getJSONArray("data")
                                inner.has("items") && inner.get("items") is org.json.JSONArray -> inner.getJSONArray("items")
                                inner.has("list") && inner.get("list") is org.json.JSONArray -> inner.getJSONArray("list")
                                else -> {
                                    val arr = org.json.JSONArray()
                                    arr.put(inner)
                                    arr
                                }
                            }
                        }
                        else -> {
                            val arr = org.json.JSONArray()
                            arr.put(obj)
                            arr
                        }
                    }
                }
                else -> return emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }

        val result = mutableListOf<NetworkItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val netId = when {
                obj.has("network_id") && !obj.isNull("network_id") -> obj.optString("network_id")
                obj.has("id") && !obj.isNull("id") -> obj.optString("id")
                obj.has("net_id") && !obj.isNull("net_id") -> obj.optString("net_id")
                obj.has("networkId") && !obj.isNull("networkId") -> obj.optString("networkId")
                else -> ""
            }.trim()

            if (netId.isBlank() || netId == "0" || netId.equals("null", ignoreCase = true)) continue

            val name = when {
                obj.has("name") && !obj.isNull("name") && obj.optString("name").isNotBlank() -> obj.optString("name").trim()
                obj.has("network_name") && !obj.isNull("network_name") && obj.optString("network_name").isNotBlank() -> obj.optString("network_name").trim()
                obj.has("networkName") && !obj.isNull("networkName") && obj.optString("networkName").isNotBlank() -> obj.optString("networkName").trim()
                else -> "شبكة $netId"
            }
            val code = when {
                obj.has("network_code") && !obj.isNull("network_code") && obj.optString("network_code").isNotBlank() -> obj.optString("network_code").trim()
                obj.has("code") && !obj.isNull("code") && obj.optString("code").isNotBlank() -> obj.optString("code").trim()
                else -> "NET-$netId"
            }
            val gov = obj.optString("governorate", "")
            val city = obj.optString("city", "")
            val loc = listOf(gov, city).filter { it.isNotBlank() && !it.equals("null", ignoreCase = true) }.joinToString(" - ").ifBlank { "المركز الرئيسي" }
            val rawStatus = obj.optString("status", "").lowercase().trim()

            val netStatus = if (isMyNetworks) {
                when {
                    rawStatus in listOf("active", "approved", "مقبول", "نشط", "منضم") -> JoinStatus.APPROVED
                    rawStatus in listOf("pending", "معلق", "قيد الانتظار", "بانتظار الموافقة") -> JoinStatus.PENDING
                    rawStatus in listOf("rejected", "suspended", "مرفوض", "موقف") -> JoinStatus.REJECTED
                    else -> JoinStatus.APPROVED
                }
            } else {
                JoinStatus.NOT_JOINED
            }

            fun getDouble(vararg keys: String): Double? {
                for (k in keys) {
                    if (obj.has(k) && !obj.isNull(k)) {
                        val raw = obj.opt(k) ?: continue
                        if (raw is Number) return raw.toDouble()
                        val s = raw.toString().trim()
                        if (s.isNotBlank() && !s.equals("null", ignoreCase = true)) {
                            val parsed = s.toDoubleOrNull()
                            if (parsed != null) return parsed
                        }
                    }
                }
                return null
            }

            val creditLimit = if (isMyNetworks) {
                getDouble("credit_limit", "creditLimit", "financial_ceiling", "financialCeiling", "ceiling", "limit", "max_credit") ?: 0.0
            } else 0.0

            val currentDebt = if (isMyNetworks) {
                getDouble("current_debt", "currentDebt", "debt", "owner_due", "ownerDue", "due_to_owner", "used_credit", "used_limit", "used_balance")
            } else null

            val rawAvail = if (isMyNetworks) {
                getDouble("available_balance", "availableBalance", "balance", "current_balance", "currentBalance", "remaining_balance", "remaining_credit", "available_credit")
            } else null

            val availableBalance = if (isMyNetworks) {
                when {
                    rawAvail != null -> rawAvail
                    currentDebt != null && creditLimit > 0 -> maxOf(0.0, creditLimit - currentDebt)
                    else -> creditLimit
                }
            } else {
                0.0
            }

            result.add(
                NetworkItem(
                    id = netId,
                    code = code,
                    name = name,
                    ownerName = "إدارة $name",
                    financialCeiling = creditLimit,
                    currentBalance = availableBalance,
                    currency = "ريال",
                    status = netStatus,
                    location = loc,
                    packagesCount = 0,
                    description = "الموقع: $loc"
                )
            )
        }
        return result
    }

    private fun parsePackagesFromJson(jsonStr: String, networkId: String): List<VoucherPackage> {
        if (jsonStr.isBlank()) return emptyList()
        val jsonArray = try {
            val trimmed = jsonStr.trim()
            when {
                trimmed.startsWith("[") -> org.json.JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = org.json.JSONObject(trimmed)
                    when {
                        obj.has("value") && obj.get("value") is org.json.JSONArray -> obj.getJSONArray("value")
                        obj.has("data") && obj.get("data") is org.json.JSONArray -> obj.getJSONArray("data")
                        obj.has("packages") && obj.get("packages") is org.json.JSONArray -> obj.getJSONArray("packages")
                        else -> {
                            val arr = org.json.JSONArray()
                            arr.put(obj)
                            arr
                        }
                    }
                }
                else -> return emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }

        val result = mutableListOf<VoucherPackage>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val pkgId = when {
                obj.has("package_id") -> obj.optString("package_id")
                obj.has("id") -> obj.optString("id")
                else -> (i + 1).toString()
            }
            val price = obj.optDouble("price", 0.0)
            val posPrice = if (obj.has("pos_price")) obj.optDouble("pos_price", price) else price
            val rawName = obj.optString("name", "").trim()
            val name = when {
                rawName.startsWith("فئة") || rawName.startsWith("باقة") -> rawName
                rawName.isNotBlank() && rawName.all { it.isDigit() || it == '.' || it.isWhitespace() } -> "فئة $rawName"
                rawName.isNotBlank() -> "فئة $rawName"
                price > 0 -> "فئة ${price.toInt()}"
                else -> "فئة ${i + 1}"
            }
            val stock = obj.optInt("stock", 1)

            val (quota, duration, validity) = extractPackageSpecs(obj, price)

            result.add(
                VoucherPackage(
                    id = pkgId,
                    networkId = networkId,
                    name = name,
                    price = price,
                    posPrice = posPrice,
                    currency = "ريال",
                    duration = duration,
                    dataQuota = quota,
                    validity = validity,
                    colorHex = "#6B21A8",
                    isAvailable = stock > 0,
                    isPopular = stock > 10
                )
            )
        }
        return result
    }

    private fun extractPackageSpecs(
        obj: org.json.JSONObject,
        price: Double
    ): Triple<String, String, String> {
        val desc = obj.optString("description", "").trim()
        val directQuota = obj.optString("quota", "").ifBlank {
            obj.optString("data_quota", "").ifBlank {
                obj.optString("megabytes", "").ifBlank {
                    obj.optString("size", "")
                }
            }
        }.trim()

        val directDuration = obj.optString("duration", "").ifBlank {
            obj.optString("time_limit", "").ifBlank {
                obj.optString("time", "").ifBlank {
                    obj.optString("hours", "")
                }
            }
        }.trim()

        val directValidity = obj.optString("validity", "").ifBlank {
            obj.optString("validity_days", "").ifBlank {
                obj.optString("expire_time", "")
            }
        }.trim()

        // 1. Quota / Size (الحجم / السعة)
        var quota = directQuota
        if (quota.isBlank() && desc.isNotBlank()) {
            val quotaRegex = Regex("""(?:سعة|حجم|السعة|الحجم)[\s:]*([0-9.]+\s*(?:ميجابايت|ميغابايت|ميجابايت|ميجا|جيجابايت|غيغابايت|جيجا|كيلوبايت|كيلو|MB|GB|KB|Gb|Mb))""", RegexOption.IGNORE_CASE)
            val match = quotaRegex.find(desc)
            if (match != null) {
                quota = match.groupValues[1].trim()
            } else {
                val generalSizeRegex = Regex("""([0-9.]+\s*(?:ميجابايت|ميغابايت|ميجابايت|ميجا|جيجابايت|غيغابايت|جيجا|كيلوبايت|MB|GB|KB))""", RegexOption.IGNORE_CASE)
                val genMatch = generalSizeRegex.find(desc)
                if (genMatch != null) {
                    quota = genMatch.groupValues[1].trim()
                } else if (desc.contains("غير محدود") || desc.contains("بلا حدود") || desc.contains("مفتوح")) {
                    quota = "غير محدود"
                }
            }
        }
        if (quota.isBlank()) {
            quota = "حسب الفئة"
        }

        // 2. Duration (المدة)
        var duration = directDuration
        if (duration.isBlank() && desc.isNotBlank()) {
            val durRegex = Regex("""(?:مدة|الوقت|المدة|ساعات)[\s:]*([0-9.]+\s*(?:ساعات|ساعة|دقائق|دقيقة|أيام|ايام|يوم|أشهر|اشهر|شهر|Hours?|Hour|Hrs|Mins?|Days?))""", RegexOption.IGNORE_CASE)
            val match = durRegex.find(desc)
            if (match != null) {
                duration = match.groupValues[1].trim()
            } else {
                val timeRegex = Regex("""([0-9.]+\s*(?:ساعات|ساعة|دقائق|دقيقة|Hours?|Hour|Hrs|Mins?))""", RegexOption.IGNORE_CASE)
                val timeMatch = timeRegex.find(desc)
                if (timeMatch != null) {
                    duration = timeMatch.groupValues[1].trim()
                }
            }
        }
        duration = normalizeArabicTime(duration)
        if (duration.isBlank()) {
            duration = "حسب الرصيد"
        }

        // 3. Validity (الصلاحية)
        var validity = directValidity
        if (validity.isBlank() && desc.isNotBlank()) {
            val valRegex = Regex("""(?:صلاحية|الصلاحية)[\s:]*([0-9.]+\s*(?:أيام|ايام|يوم|ساعات|ساعة|أشهر|اشهر|شهر|Days?|Hours?))""", RegexOption.IGNORE_CASE)
            val match = valRegex.find(desc)
            if (match != null) {
                validity = match.groupValues[1].trim()
            }
        }
        validity = normalizeArabicValidity(validity)
        if (validity.isBlank()) {
            validity = "حسب الاستخدام"
        }

        return Triple(quota, duration, validity)
    }

    private fun normalizeArabicTime(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        val num = trimmed.toIntOrNull()
        if (num != null) {
            return when (num) {
                1 -> "ساعة واحدة"
                2 -> "ساعتان"
                in 3..10 -> "$num ساعات"
                else -> "$num ساعة"
            }
        }
        val regexHour = Regex("""^(\d+)\s*(?:ساعة|ساعات)$""")
        val match = regexHour.find(trimmed)
        if (match != null) {
            val n = match.groupValues[1].toIntOrNull() ?: 1
            return when (n) {
                1 -> "ساعة واحدة"
                2 -> "ساعتان"
                in 3..10 -> "$n ساعات"
                else -> "$n ساعة"
            }
        }
        return trimmed
    }

    private fun normalizeArabicValidity(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        val num = trimmed.toIntOrNull()
        if (num != null) {
            return when (num) {
                1 -> "يوم واحد"
                2 -> "يومان"
                in 3..10 -> "$num أيام"
                else -> "$num يوم"
            }
        }
        val regexDay = Regex("""^(\d+)\s*(?:يوم|ايام|أيام)$""")
        val matchDay = regexDay.find(trimmed)
        if (matchDay != null) {
            val n = matchDay.groupValues[1].toIntOrNull() ?: 1
            return when (n) {
                1 -> "يوم واحد"
                2 -> "يومان"
                in 3..10 -> "$n أيام"
                else -> "$n يوم"
            }
        }
        return trimmed
    }

    suspend fun fetchRealNetworks(): ApiResponse<List<NetworkItem>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getNetworks()
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val items = parseNetworkListFromJson(rawJson, isMyNetworks = false)
                ApiResponse(true, "تم جلب الشبكات من السيرفر بنجاح", data = items)
            } else {
                ApiResponse(false, "عذراً، لم نتمكن من جلب بيانات الشبكات حالياً.", data = emptyList())
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت.", data = emptyList())
        }
    }

    suspend fun fetchMyNetworks(): ApiResponse<List<NetworkItem>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getMyNetworks()
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val items = parseNetworkListFromJson(rawJson, isMyNetworks = true)
                ApiResponse(true, "تم جلب شبكاتي من السيرفر بنجاح", data = items)
            } else {
                ApiResponse(false, "لم نتمكن من جلب بيانات شبكاتك من السيرفر حالياً.", data = emptyList())
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت.", data = emptyList())
        }
    }

    suspend fun searchNetworkByCode(codeQuery: String): ApiResponse<NetworkItem> = withContext(Dispatchers.IO) {
        val realNetsRes = fetchRealNetworks()
        val allNets = realNetsRes.data ?: emptyList()

        val rawQuery = codeQuery.trim()
        if (rawQuery.isBlank()) {
            return@withContext ApiResponse(false, "يرجى كتابة نص للبحث", data = null)
        }

        fun norm(s: String): String {
            return s.lowercase()
                .replace(Regex("[أإآٱ]"), "ا")
                .replace(Regex("[ة]"), "ه")
                .replace(Regex("[ى]"), "ي")
                .replace(Regex("[ؤ]"), "و")
                .replace(Regex("[ئ]"), "ي")
                .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
                .replace(Regex("[\\s\\-_.]"), "")
        }

        val qNorm = norm(rawQuery)

        // 1. Direct or normalized match
        val found = allNets.find { net ->
            val nCode = norm(net.code)
            val nName = norm(net.name)
            val nOwner = norm(net.ownerName)
            val nLoc = norm(net.location)

            nCode.contains(qNorm) ||
            nName.contains(qNorm) ||
            nOwner.contains(qNorm) ||
            nLoc.contains(qNorm) ||
            net.id == rawQuery ||
            net.code.equals(rawQuery, ignoreCase = true)
        }

        if (found != null) {
            ApiResponse(true, "تم العثور على الشبكة بنجاح", data = found)
        } else {
            ApiResponse(false, "لم يتم العثور على شبكة مطابقة لـ ($codeQuery).")
        }
    }

    suspend fun requestJoinNetwork(networkId: String): ApiResponse<NetworkItem> = withContext(Dispatchers.IO) {
        try {
            val netIdInt = networkId.toIntOrNull()
                ?: networkId.filter { it.isDigit() }.toIntOrNull()
                ?: 1
            val response = RetrofitClient.api.joinNetwork(request = JoinNetworkRequestDto(networkId = netIdInt))
            if (response.isSuccessful) {
                val rawJson = response.body()?.string().orEmpty()
                val jsonObj = try { org.json.JSONObject(rawJson) } catch (e: Exception) { null }
                val serverMsg = jsonObj?.optString("message", "")?.ifBlank { jsonObj.optString("msg", "") }
                val finalMsg = if (!serverMsg.isNullOrBlank()) serverMsg else "تم إرسال طلب الانضمام إلى إدارة الشبكة بنجاح عبر السيرفر"

                val isApproved = finalMsg.contains("منضم") || finalMsg.contains("نشط") || finalMsg.contains("approved", ignoreCase = true) || finalMsg.contains("active", ignoreCase = true)
                val finalStatus = if (isApproved) JoinStatus.APPROVED else JoinStatus.PENDING

                ApiResponse(
                    success = true,
                    message = finalMsg,
                    data = NetworkItem(
                        id = networkId,
                        code = "NET-$networkId",
                        name = "شبكة $networkId",
                        ownerName = "إدارة الشبكة",
                        financialCeiling = 0.0,
                        currentBalance = 0.0,
                        currency = "ريال",
                        status = finalStatus,
                        location = "المركز الرئيسي",
                        packagesCount = 0
                    )
                )
            } else {
                // قراءة رسالة السيرفر بدقة عند وجود رد مسبق أو تعليق
                val errBody = response.errorBody()?.string().orEmpty()
                val jsonObj = try { org.json.JSONObject(errBody) } catch (e: Exception) { null }
                val errMsg = jsonObj?.optString("message", "")?.ifBlank { jsonObj?.optString("msg", "") }.orEmpty()
                val finalMsg = errMsg.ifBlank { "تم إرسال طلب الانضمام بنجاح" }
                
                val inferredStatus = when {
                    finalMsg.contains("معلق") || finalMsg.contains("انتظار") || finalMsg.contains("pending", ignoreCase = true) || finalMsg.contains("مسبقا") || finalMsg.contains("مسبقاً") || finalMsg.contains("بالفعل") || finalMsg.contains("مسجل") -> JoinStatus.PENDING
                    finalMsg.contains("منضم") || finalMsg.contains("نشط") || finalMsg.contains("approved", ignoreCase = true) || finalMsg.contains("active", ignoreCase = true) -> JoinStatus.APPROVED
                    else -> JoinStatus.PENDING
                }

                ApiResponse(
                    success = true,
                    message = finalMsg,
                    data = NetworkItem(
                        id = networkId,
                        code = "NET-$networkId",
                        name = "شبكة $networkId",
                        ownerName = "إدارة الشبكة",
                        financialCeiling = 0.0,
                        currentBalance = 0.0,
                        currency = "ريال",
                        status = inferredStatus,
                        location = "المركز الرئيسي",
                        packagesCount = 0
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MikroTikApiService", "Join network error: ${e.message}", e)
            ApiResponse(
                success = false,
                message = "تم تسجيل طلب الانضمام، جارٍ التحقق من السيرفر..."
            )
        }
    }

    suspend fun getVoucherPackages(networkId: String): ApiResponse<List<VoucherPackage>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getNetworkPackages(networkId)
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val packages = parsePackagesFromJson(rawJson, networkId)
                ApiResponse(true, "تم جلب الباقات المتاحة من السيرفر بنجاح", data = packages)
            } else {
                ApiResponse(false, "عذراً، لا توجد باقات متاحة حالياً.", data = emptyList())
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم لجلب الباقات. يرجى التحقق من اتصالك بالإنترنت.", data = emptyList())
        }
    }

    suspend fun purchaseVouchers(
        networkId: String,
        packageItem: VoucherPackage,
        quantity: Int,
        currentCeilingBalance: Double,
        customerPhone: String?,
        paymentMethod: String = "network_credit"
    ): ApiResponse<PurchaseResult> = withContext(Dispatchers.IO) {
        val totalCost = packageItem.price * quantity
        val netIdInt = networkId.toIntOrNull() ?: 1
        val pkgIdInt = packageItem.id.toIntOrNull() ?: 0

        try {
            val requestDto = PurchaseRequestDto(
                networkId = netIdInt,
                packageId = pkgIdInt,
                quantity = quantity,
                customerPhone = customerPhone.takeIf { !it.isNullOrBlank() },
                sendSms = !customerPhone.isNullOrBlank(),
                paymentMethod = paymentMethod
            )

            val response = RetrofitClient.api.purchaseVoucher(request = requestDto)
            if (response.isSuccessful && response.body() != null) {
                val resBody = response.body()!!
                val firstVoucher = resBody.vouchers?.firstOrNull()

                // PIN الحقيقي من السيرفر: نفضّل pin، ثم voucher_code
                val realPin = firstVoucher?.pin
                    ?: firstVoucher?.voucherCode
                    ?: ""

                // إذا كانت الكمية أكثر من واحد، نجمع كل الكروت في نص واحد
                val allPins = resBody.vouchers
                    ?.mapNotNull { v -> v.pin?.takeIf { it.isNotBlank() } ?: v.voucherCode }
                    ?.joinToString(" | ")
                    ?: realPin

                val orderId = firstVoucher?.transactionId ?: "ORD-POS-${System.currentTimeMillis()}"

                val newBalance = (currentCeilingBalance - (resBody.totalDeducted ?: totalCost)).coerceAtLeast(0.0)

                ApiResponse(
                    success = true,
                    message = resBody.message ?: "تم شراء وتوليد الكرت بنجاح من السيرفر",
                    data = PurchaseResult(
                        voucherPin = allPins,
                        newBalance = newBalance,
                        totalAmount = resBody.totalDeducted ?: totalCost,
                        orderId = orderId,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val errMsg = try {
                    org.json.JSONObject(errBody).optString("message", "")
                } catch (e: Exception) { "" }
                ApiResponse(
                    success = false,
                    message = errMsg.ifBlank { "لم نتمكن من إتمام عملية الشراء. تأكد من رصيدك أو صلاحية الباقة." }
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت."
            )
        }
    }
    suspend fun getWalletBalanceApi(): ApiResponse<com.example.data.remote.WalletDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getWallet()
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val parsed = parseWalletInfoFromJson(rawJson)
                val dto = com.example.data.remote.WalletDto(
                    balance = parsed.first,
                    currency = "ريال"
                )
                ApiResponse(true, "تم جلب رصيد المحفظة بنجاح", data = dto)
            } else {
                ApiResponse(false, "لم نتمكن من جلب بيانات المحفظة. يرجى المحاولة لاحقاً.")
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التأكد من اتصالك بالإنترنت.")
        }
    }

    fun normalizeBankNameForServer(raw: String): String {
        val lower = raw.lowercase().trim()
        return when {
            lower.contains("jaib") || lower.contains("jeeb") || lower.contains("جيب") -> "Jaib"
            lower.contains("kuraimi") || lower.contains("كريمي") || lower.contains("حاسب") || lower.contains("ام كريمي") -> "Kuraimi"
            lower.contains("jawali") || lower.contains("جوالي") -> "Jawali"
            lower.contains("onecash") || lower.contains("one_cash") || lower.contains("ون كاش") || lower.contains("ونكاش") || lower.contains("one cash") -> "One_cash"
            lower.contains("floosak") || lower.contains("فلوسك") -> "Floosak"
            lower.contains("saba") || lower.contains("سبأ") || lower.contains("سباكاش") -> "Saba_cash"
            lower.contains("pyes") || lower.contains("بايس") -> "Pyes"
            lower.contains("easy") || lower.contains("ايزي") || lower.contains("إيزي") -> "Easy"
            lower.contains("cash_wallet") || lower.contains("كاش") -> "Cash_wallet"
            lower.contains("jawwal") || lower.contains("جوال") -> "Jawwal"
            else -> raw.trim()
        }
    }

    private fun isMessageIndicatingError(msg: String): Boolean {
        if (msg.isBlank()) return false
        val lower = msg.lowercase().trim()
        val errorKeywords = listOf(
            "لم يتم",
            "غير مطابق",
            "غير متطابق",
            "غير صحيح",
            "غير موجود",
            "لا يوجد",
            "لا توجد",
            "لم نعثر",
            "تعذر",
            "فشل",
            "خطأ",
            "مرفوض",
            "مستخدم مسبق",
            "مستخدم مسبقا",
            "مستخدم من قبل",
            "مكرر",
            "ناقص",
            "تأكد",
            "تحقق",
            "invalid",
            "mismatch",
            "not found",
            "not_found",
            "failed",
            "error",
            "declined",
            "unmatched",
            "incorrect",
            "duplicate",
            "already used",
            "already exists"
        )
        return errorKeywords.any { lower.contains(it) }
    }

    private fun parseTopUpResponse(
        rawJson: String,
        expectedAmount: Double = 0.0,
        expectedBankName: String = ""
    ): ApiResponse<String> {
        if (rawJson.isBlank()) {
            return ApiResponse(false, "لم يتم استلام رد من السيرفر. يرجى المحاولة لاحقاً.")
        }
        try {
            val trimmed = rawJson.trim()
            if (trimmed.startsWith("{")) {
                val obj = org.json.JSONObject(trimmed)

                // 1. Check for explicit error objects or error messages
                val extractedError = extractDetailedErrorFromJson(obj)
                if (extractedError.isNotBlank()) {
                    return ApiResponse(false, extractedError)
                }

                // 2. Check for explicit status code in JSON
                val code = when {
                    obj.has("code") -> obj.optInt("code", 200)
                    obj.has("status_code") -> obj.optInt("status_code", 200)
                    obj.has("statusCode") -> obj.optInt("statusCode", 200)
                    else -> 200
                }
                if (code in 400..599) {
                    val errMsg = obj.optString("message", "").ifBlank {
                        obj.optString("msg", "").ifBlank {
                            obj.optString("error", "فشل التحقق من بيانات الإيداع (كود: $code)")
                        }
                    }
                    return ApiResponse(false, errMsg)
                }

                // 3. Check for the official 'recharge' object returned by the backend
                if (obj.has("recharge")) {
                    val rechargeObj = obj.optJSONObject("recharge")
                    val status = rechargeObj?.optString("status", "")?.lowercase() ?: ""
                    val rechargeAmount = rechargeObj?.optDouble("amount", 0.0) ?: 0.0
                    val rechargeBank = rechargeObj?.optString("bank_name", "") ?: ""
                    val baseMsg = obj.optString("message", "").ifBlank { obj.optString("msg", "") }

                    if (status == "rejected" || status == "failed") {
                        return ApiResponse(false, baseMsg.ifBlank { "تم رفض عملية الإيداع من السيرفر." })
                    }

                    // Strict amount reconciliation: Check if user-entered amount matches server transfer amount
                    if (expectedAmount > 0 && rechargeAmount > 0 && Math.abs(rechargeAmount - expectedAmount) >= 1.0) {
                        return ApiResponse(
                            false,
                            "المبلغ المدخل (${expectedAmount.toInt()} ريال) غير مطابق لمبلغ الحوالة الفعلي بالسيرفر (${rechargeAmount.toInt()} ريال). يرجى تصحيح المبلغ والمحاولة مجدداً."
                        )
                    }

                    // Strict bank reconciliation if bank returned
                    if (expectedBankName.isNotBlank() && rechargeBank.isNotBlank()) {
                        val normExpected = normalizeBankNameForServer(expectedBankName)
                        val normActual = normalizeBankNameForServer(rechargeBank)
                        if (normExpected.isNotBlank() && normActual.isNotBlank() && !normExpected.equals(normActual, ignoreCase = true)) {
                            return ApiResponse(
                                false,
                                "المحفظة المختارة ($expectedBankName) غير مطابقة لمحفظة الحوالة الفعلية بالسيرفر ($rechargeBank)."
                            )
                        }
                    }

                    val finalSuccessMsg = when {
                        rechargeAmount > 0 -> "تم التحقق من مطابقة الحوالة وتغذية محفظتك بنجاح بمبلغ ${rechargeAmount.toInt()} ريال!"
                        baseMsg.isNotBlank() -> baseMsg
                        else -> "تم تغذية محفظتك بنجاح بشكل آلي!"
                    }
                    return ApiResponse(true, finalSuccessMsg)
                }

                // 4. Check for explicit success flags
                val hasSuccessField = obj.has("success")
                val isSuccess = if (hasSuccessField) {
                    when (val v = obj.get("success")) {
                        is Boolean -> v
                        is Number -> v.toInt() == 1
                        is String -> v.equals("true", ignoreCase = true) || v == "1" || v.equals("ok", ignoreCase = true)
                        else -> false
                    }
                } else null

                // 5. Check for explicit status flags
                val hasStatusField = obj.has("status")
                val isStatusOk = if (hasStatusField) {
                    when (val v = obj.get("status")) {
                        is Boolean -> v
                        is Number -> v.toInt() == 1 || v.toInt() == 200
                        is String -> v.equals("true", ignoreCase = true) || v == "1" || v.equals("success", ignoreCase = true) || v.equals("ok", ignoreCase = true)
                        else -> false
                    }
                } else null

                // 6. Check for matched / verified flags
                val isMatched = if (obj.has("matched")) {
                    when (val v = obj.get("matched")) {
                        is Boolean -> v
                        is Number -> v.toInt() == 1
                        is String -> v.equals("true", ignoreCase = true) || v == "1"
                        else -> false
                    }
                } else if (obj.has("is_matched")) {
                    when (val v = obj.get("is_matched")) {
                        is Boolean -> v
                        is Number -> v.toInt() == 1
                        is String -> v.equals("true", ignoreCase = true) || v == "1"
                        else -> false
                    }
                } else if (obj.has("is_verified")) {
                    when (val v = obj.get("is_verified")) {
                        is Boolean -> v
                        is Number -> v.toInt() == 1
                        is String -> v.equals("true", ignoreCase = true) || v == "1"
                        else -> false
                    }
                } else null

                // If any explicit flag indicates a failure
                if (isSuccess == false || isStatusOk == false || isMatched == false) {
                    val rawMsg = obj.optString("message", "").ifBlank {
                        obj.optString("msg", "").ifBlank {
                            obj.optString("error", "")
                        }
                    }
                    val finalMsg = when {
                        rawMsg.isNotBlank() -> rawMsg
                        isMatched == false -> "المبلغ المدخل غير مطابق لمبلغ الحوالة البنكية الفعلية بالسيرفر."
                        else -> "لم يتم العثور على عملية الإيداع. تأكد من صحة رقم المرجع والمحفظة."
                    }
                    return ApiResponse(false, finalMsg)
                }

                // Check message text for error indicators even if status was ambiguous
                val rawMsg = obj.optString("message", "").ifBlank { obj.optString("msg", "") }
                if (rawMsg.isNotBlank() && isMessageIndicatingError(rawMsg)) {
                    return ApiResponse(false, rawMsg)
                }

                // Check for verified positive indications (data payload or explicit true flags)
                val hasPositiveIndicator = (isSuccess == true) || (isStatusOk == true) || (isMatched == true) ||
                        obj.has("data") || obj.has("wallet") || obj.has("new_balance") || obj.has("balance") ||
                        (rawMsg.isNotBlank() && (rawMsg.contains("نجاح") || rawMsg.contains("بنجاح") || rawMsg.contains("تمت التغذية") || rawMsg.contains("تم الشحن") || rawMsg.contains("تمت الإضافة") || rawMsg.contains("تم إضافة") || rawMsg.contains("تم تغذية")))

                if (!hasPositiveIndicator) {
                    return ApiResponse(false, rawMsg.ifBlank { "لم يتم العثور على عملية الإيداع. تأكد من صحة رقم المرجع والمحفظة." })
                }

                // Success
                val successMsg = rawMsg.ifBlank { "تم تغذية محفظتك بنجاح بشكل آلي!" }
                return ApiResponse(true, successMsg)
            }
        } catch (e: Exception) {
            return ApiResponse(false, "حدث خطأ أثناء معالجة رد السيرفر للتحقق من الإيداع.")
        }
        return ApiResponse(false, "لم يتم العثور على عملية الإيداع. تأكد من صحة رقم المرجع والمحفظة.")
    }

    private fun extractDetailedErrorFromJson(obj: org.json.JSONObject): String {
        // Check "errors" object or array (Validation error structure)
        if (obj.has("errors")) {
            val errorsObj = obj.get("errors")
            if (errorsObj is org.json.JSONObject) {
                val errorList = mutableListOf<String>()
                val keys = errorsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val fieldVal = errorsObj.get(key)
                    if (fieldVal is org.json.JSONArray) {
                        for (i in 0 until fieldVal.length()) {
                            val item = fieldVal.optString(i)
                            if (item.isNotBlank()) errorList.add(item)
                        }
                    } else if (fieldVal is String && fieldVal.isNotBlank()) {
                        errorList.add(fieldVal)
                    }
                }
                if (errorList.isNotEmpty()) {
                    return errorList.joinToString("، ")
                }
            } else if (errorsObj is org.json.JSONArray) {
                val list = (0 until errorsObj.length()).mapNotNull { i -> errorsObj.optString(i).takeIf { it.isNotBlank() } }
                if (list.isNotEmpty()) return list.joinToString("، ")
            } else if (errorsObj is String && errorsObj.isNotBlank()) {
                return errorsObj
            }
        }

        // Check "error" key
        if (obj.has("error")) {
            val err = obj.get("error")
            if (err is String && err.isNotBlank() && !err.equals("false", ignoreCase = true) && err != "0" && !err.equals("null", ignoreCase = true)) {
                return err
            } else if (err is org.json.JSONObject) {
                val errInner = err.optString("message", "").ifBlank { err.optString("detail", "").ifBlank { err.optString("msg", "") } }
                if (errInner.isNotBlank()) return errInner
            }
        }

        // Check if message itself is explicitly an error/mismatch description
        val msg = obj.optString("message", "").ifBlank { obj.optString("msg", "") }
        if (msg.isNotBlank() && isMessageIndicatingError(msg)) {
            return msg
        }

        return ""
    }

    suspend fun topUpWalletApi(amount: Double, bankName: String, refNumber: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val serverBankName = normalizeBankNameForServer(bankName)
            val cleanRef = refNumber.trim()
            val req = com.example.data.remote.TopUpRequestDto(
                amount = amount,
                bankName = serverBankName,
                referenceNumber = cleanRef
            )
            val response = RetrofitClient.api.requestTopUp(request = req)
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                parseTopUpResponse(rawJson, expectedAmount = amount, expectedBankName = bankName)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val specificMsg = try {
                    if (errBody.isNotBlank()) {
                        val obj = org.json.JSONObject(errBody)
                        val det = extractDetailedErrorFromJson(obj)
                        if (det.isNotBlank()) det else obj.optString("message", "").ifBlank { obj.optString("msg", "") }
                    } else ""
                } catch (e: Exception) { "" }

                val cleanMsg = parseCleanErrorMessage(errBody, response.code())

                val finalError = when {
                    specificMsg.isNotBlank() -> specificMsg
                    cleanMsg.isNotBlank() && !cleanMsg.startsWith("تعذر") -> cleanMsg
                    response.code() == 422 -> "المبلغ المدخل أو الرقم المرجعي غير مطابق لبيانات الحوالة بالسيرفر."
                    response.code() == 404 -> "لم يتم العثور على عملية الإيداع أو السند بهذا الرقم المرجعي."
                    response.code() == 400 -> "المبلغ المدخل غير مطابق لبيانات السند أو تم استخدام هذا الرقم المرجعي مسبقاً."
                    else -> "عذراً، فشل التحقق من الإيداع. يرجى التأكد من صحة المبلغ والرقم المرجعي والمحاولة مجدداً."
                }
                ApiResponse(false, finalError)
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت.")
        }
    }
    suspend fun updateProfileApi(name: String, shopName: String, phone: String, address: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val requestDto = com.example.data.remote.UserProfileDto(
                name = name,
                shopName = shopName,
                phone = phone,
                address = address
            )
            val response = RetrofitClient.api.updateProfile(request = requestDto)
            if (response.isSuccessful) {
                ApiResponse(true, response.body()?.message ?: "تم تسجيل وتحديث البيانات بنجاح في السيرفر")
            } else {
                ApiResponse(false, "عذراً، لم نتمكن من تحديث البيانات. يرجى المحاولة لاحقاً.")
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت.")
        }
    }

    suspend fun getProfileApi(): ApiResponse<com.example.data.remote.UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse(true, "تم جلب بيانات الحساب بنجاح", data = response.body())
            } else {
                ApiResponse(false, "تعذر جلب الملف الشخصي من السيرفر")
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم لجلب بيانات الحساب")
        }
    }

    suspend fun fetchSalesHistoryApi(): ApiResponse<List<com.example.data.local.OrderTransactionEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getSalesHistory()
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val parsed = parseSalesHistoryFromJson(rawJson)
                ApiResponse(true, "تم جلب سجل المبيعات من السيرفر بنجاح", data = parsed)
            } else {
                ApiResponse(false, "تعذر جلب سجل المبيعات", data = emptyList())
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالخادم لجلب المبيعات", data = emptyList())
        }
    }

    private fun parseSalesHistoryFromJson(jsonStr: String): List<com.example.data.local.OrderTransactionEntity> {
        if (jsonStr.isBlank()) return emptyList()
        val jsonArray = try {
            val trimmed = jsonStr.trim()
            when {
                trimmed.startsWith("[") -> org.json.JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = org.json.JSONObject(trimmed)
                    when {
                        obj.has("sales") && obj.get("sales") is org.json.JSONArray -> obj.getJSONArray("sales")
                        obj.has("data") && obj.get("data") is org.json.JSONArray -> obj.getJSONArray("data")
                        obj.has("history") && obj.get("history") is org.json.JSONArray -> obj.getJSONArray("history")
                        obj.has("orders") && obj.get("orders") is org.json.JSONArray -> obj.getJSONArray("orders")
                        obj.has("transactions") && obj.get("transactions") is org.json.JSONArray -> obj.getJSONArray("transactions")
                        else -> {
                            val arr = org.json.JSONArray()
                            arr.put(obj)
                            arr
                        }
                    }
                }
                else -> return emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }

        val list = mutableListOf<com.example.data.local.OrderTransactionEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue

            fun cleanString(key: String): String? {
                if (!obj.has(key)) return null
                val v = obj.opt(key) ?: return null
                if (v == org.json.JSONObject.NULL) return null
                val str = v.toString().trim()
                return if (str.equals("null", ignoreCase = true) || str.isBlank()) null else str
            }

            val id = cleanString("id")
                ?: cleanString("order_id")
                ?: cleanString("transaction_id")
                ?: "ORD-POS-${System.currentTimeMillis()}-$i"

            val netId = cleanString("network_id")
                ?: cleanString("networkId")
                ?: cleanString("net_id")
                ?: ""

            val netName = cleanString("network_name")
                ?: cleanString("networkName")
                ?: "شبكة"

            val pkgName = cleanString("package_name")
                ?: cleanString("packageName")
                ?: cleanString("name")
                ?: "باقة إنترنت"

            val pkgPrice = obj.optDouble("price", obj.optDouble("package_price", 0.0))
            val costPrice = when {
                obj.has("pos_price") -> obj.optDouble("pos_price", pkgPrice * 0.9)
                obj.has("cost_price") -> obj.optDouble("cost_price", pkgPrice * 0.9)
                obj.has("cost") -> obj.optDouble("cost", pkgPrice * 0.9)
                pkgPrice > 0 -> pkgPrice * 0.9
                else -> 0.0
            }
            val quantity = obj.optInt("quantity", 1)
            val totalAmount = obj.optDouble("total_amount", obj.optDouble("total", pkgPrice * quantity))
            val totalCost = obj.optDouble("total_cost", costPrice * quantity)
            
            val phone = cleanString("customer_phone")
                ?: cleanString("phone")

            // Robust pin extraction handling all possible JSON keys, nested vouchers, and null strings
            var pin: String? = cleanString("pin")
                ?: cleanString("voucher_code")
                ?: cleanString("voucher_pin")
                ?: cleanString("card_code")
                ?: cleanString("code")
                ?: cleanString("serial")
                ?: cleanString("password")
                ?: cleanString("voucher")
                ?: cleanString("voucher_password")

            // If nested in vouchers array or object
            if (pin == null) {
                if (obj.has("vouchers")) {
                    val vouchersObj = obj.opt("vouchers")
                    if (vouchersObj is org.json.JSONArray && vouchersObj.length() > 0) {
                        val pinsList = mutableListOf<String>()
                        for (j in 0 until vouchersObj.length()) {
                            val vItem = vouchersObj.optJSONObject(j) ?: continue
                            val vPin = vItem.optString("pin", vItem.optString("voucher_code", vItem.optString("code", "")))
                                .takeIf { !it.equals("null", ignoreCase = true) && it.isNotBlank() }
                            if (vPin != null) {
                                pinsList.add(vPin)
                            }
                        }
                        if (pinsList.isNotEmpty()) {
                            pin = pinsList.joinToString(" | ")
                        }
                    } else if (vouchersObj is org.json.JSONObject) {
                        pin = vouchersObj.optString("pin", vouchersObj.optString("voucher_code", vouchersObj.optString("code", "")))
                            .takeIf { !it.equals("null", ignoreCase = true) && it.isNotBlank() }
                    }
                }
            }

            val safePin = pin ?: "------"
            
            val timeRaw = obj.opt("purchased_at") ?: obj.opt("created_at") ?: obj.opt("date") ?: obj.opt("timestamp")
            val timestamp = parseServerTimestamp(timeRaw)

            val storeName = cleanString("pos_store_name")
                ?: cleanString("shop_name")
                ?: cleanString("store_name")
                ?: "نقطة البيع"

            val duration = cleanString("duration")
            val quota = cleanString("data_quota") ?: cleanString("quota")
            val validity = cleanString("validity")

            val rawPaymentSource = cleanString("payment_source")
                ?: cleanString("payment_method")
                ?: cleanString("source")
                ?: cleanString("pay_type")
                ?: ""
            
            val paymentSource = if (
                rawPaymentSource.contains("wallet", ignoreCase = true) ||
                rawPaymentSource.contains("محفظ", ignoreCase = true) ||
                rawPaymentSource.contains("cash", ignoreCase = true)
            ) {
                "WALLET"
            } else {
                "NETWORK_CREDIT"
            }

            list.add(
                com.example.data.local.OrderTransactionEntity(
                    id = id,
                    networkId = netId,
                    networkName = netName,
                    packageName = pkgName,
                    packagePrice = pkgPrice,
                    costPrice = costPrice,
                    quantity = quantity,
                    totalAmount = totalAmount,
                    totalCost = totalCost,
                    customerPhone = phone,
                    voucherPin = safePin,
                    timestamp = timestamp,
                    posStoreName = storeName,
                    isPrinted = true,
                    duration = duration,
                    dataQuota = quota,
                    validity = validity,
                    paymentSource = paymentSource
                )
            )
        }
        return list
    }

    suspend fun fetchWalletDataApi(): ApiResponse<Pair<Double, List<com.example.data.local.WalletTransactionEntity>>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getWallet()
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                val parsed = parseWalletInfoFromJson(rawJson)
                ApiResponse(true, "تم جلب بيانات ورصيد المحفظة بنجاح", data = parsed)
            } else {
                ApiResponse(false, "تعذر جلب بيانات المحفظة من السيرفر", data = Pair(0.0, emptyList()))
            }
        } catch (e: Exception) {
            ApiResponse(false, "تعذر الاتصال بالسيرفر لجلب بيانات المحفظة", data = Pair(0.0, emptyList()))
        }
    }

    fun parseWalletInfoFromJson(jsonStr: String): Pair<Double, List<com.example.data.local.WalletTransactionEntity>> {
        if (jsonStr.isBlank()) return Pair(0.0, emptyList())
        try {
            val trimmed = jsonStr.trim()
            val obj = if (trimmed.startsWith("{")) org.json.JSONObject(trimmed) else return Pair(0.0, emptyList())
            
            // Extract balance
            var balance = 0.0
            if (obj.has("balance")) {
                balance = obj.optDouble("balance", 0.0)
            } else if (obj.has("wallet")) {
                val wObj = obj.optJSONObject("wallet")
                if (wObj != null) {
                    balance = wObj.optDouble("balance", wObj.optDouble("amount", 0.0))
                }
            } else if (obj.has("data")) {
                val dObj = obj.optJSONObject("data")
                if (dObj != null) {
                    balance = dObj.optDouble("balance", dObj.optDouble("wallet_balance", 0.0))
                }
            }

            // Extract transactions
            val txJsonArray = when {
                obj.has("recent_transactions") && obj.get("recent_transactions") is org.json.JSONArray -> obj.getJSONArray("recent_transactions")
                obj.has("transactions") && obj.get("transactions") is org.json.JSONArray -> obj.getJSONArray("transactions")
                obj.has("history") && obj.get("history") is org.json.JSONArray -> obj.getJSONArray("history")
                obj.has("recharges") && obj.get("recharges") is org.json.JSONArray -> obj.getJSONArray("recharges")
                obj.has("data") && obj.get("data") is org.json.JSONArray -> obj.getJSONArray("data")
                obj.has("data") && obj.optJSONObject("data")?.has("transactions") == true -> obj.optJSONObject("data")!!.optJSONArray("transactions")
                else -> null
            }

            val list = mutableListOf<com.example.data.local.WalletTransactionEntity>()
            if (txJsonArray != null) {
                for (i in 0 until txJsonArray.length()) {
                    val item = txJsonArray.optJSONObject(i) ?: continue
                    
                    fun clean(key: String): String? {
                        if (!item.has(key)) return null
                        val v = item.opt(key) ?: return null
                        if (v == org.json.JSONObject.NULL) return null
                        val s = v.toString().trim()
                        return if (s.equals("null", ignoreCase = true) || s.isBlank()) null else s
                    }

                    val id = clean("id")
                        ?: clean("transaction_id")
                        ?: clean("recharge_id")
                        ?: clean("ref_id")
                        ?: "tx-srv-$i-${System.currentTimeMillis()}"

                    val amount = item.optDouble("amount", item.optDouble("total", item.optDouble("balance", 0.0)))
                    val rawType = clean("type") ?: clean("tx_type") ?: clean("action") ?: ""
                    val isDebit = rawType.contains("debit", ignoreCase = true) ||
                                  rawType.contains("purchase", ignoreCase = true) ||
                                  rawType.contains("خصم", ignoreCase = true) ||
                                  rawType.contains("شراء", ignoreCase = true)
                    
                    val typeName = if (isDebit) com.example.data.model.WalletTxType.VOUCHER_PURCHASE.name else com.example.data.model.WalletTxType.DEPOSIT.name
                    
                    val bankName = clean("bank_name")
                        ?: clean("bank")
                        ?: clean("wallet_name")
                        ?: clean("payment_method")
                        ?: clean("source")
                        ?: "حوالة بنكية"

                    val refNum = clean("reference_number")
                        ?: clean("ref_number")
                        ?: clean("ref_no")
                        ?: clean("reference")
                        ?: clean("id")
                        ?: "-"

                    val desc = clean("description")
                        ?: clean("title")
                        ?: clean("notes")
                        ?: clean("note")
                        ?: (if (isDebit) "خصم عملية شراء" else "تغذية حساب عبر $bankName")

                    val timeRaw = item.opt("created_at")
                        ?: item.opt("timestamp")
                        ?: item.opt("date")
                        ?: item.opt("purchased_at")

                    val ts = parseServerTimestamp(timeRaw)

                    list.add(
                        com.example.data.local.WalletTransactionEntity(
                            id = id,
                            title = desc,
                            type = typeName,
                            amount = amount,
                            currency = "ريال",
                            referenceNumber = refNum,
                            paymentMethod = bankName,
                            status = com.example.data.model.WalletTxStatus.COMPLETED.name,
                            timestamp = ts,
                            networkName = clean("network_name")
                        )
                    )
                }
            }

            return Pair(balance, list)
        } catch (e: Exception) {
            return Pair(0.0, emptyList())
        }
    }
}

fun parseServerTimestamp(rawDate: Any?): Long {
    if (rawDate == null) return System.currentTimeMillis()
    if (rawDate is Number) {
        val num = rawDate.toLong()
        return if (num < 10000000000L) num * 1000L else num
    }
    val str = rawDate.toString().trim()
    if (str.isBlank() || str.equals("null", ignoreCase = true)) return System.currentTimeMillis()

    // Numeric timestamp string (seconds or milliseconds)
    str.toLongOrNull()?.let { num ->
        return if (num < 10000000000L) num * 1000L else num
    }

    val isoPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm"
    )

    for (pattern in isoPatterns) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(str)
            if (date != null) {
                return date.time
            }
        } catch (e: Exception) {
            // continue to next pattern
        }
    }

    return System.currentTimeMillis()
}

data class PurchaseResult(
    val voucherPin: String,
    val newBalance: Double,
    val totalAmount: Double,
    val orderId: String,
    val timestamp: Long
)

