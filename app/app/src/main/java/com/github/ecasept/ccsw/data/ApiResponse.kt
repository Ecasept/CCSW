package com.github.ecasept.ccsw.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class ApiResponseSerializer<T>(private val dataSerializer: KSerializer<T>) :
    KSerializer<ApiResponse<T>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ApiResponse") {
        element<Boolean>("success")
        element(
            "data",
            dataSerializer.descriptor,
            isOptional = true
        )
        element<String>("error", isOptional = true)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: ApiResponse<T>) {
        encoder.encodeStructure(descriptor) {
            when (val result = value.toResult()) {
                is ApiResponseResult.SuccessResult -> {
                    encodeBooleanElement(descriptor, 0, true)
                    if (result.data is Unit) {
                        // If data is Unit, we encode it as null
                        encodeNullableSerializableElement(descriptor, 1, dataSerializer, null)
                    } else {
                        // Encode the actual data
                        encodeSerializableElement(descriptor, 1, dataSerializer, result.data)
                    }
                }

                is ApiResponseResult.ErrorResult -> {
                    encodeBooleanElement(descriptor, 0, false)
                    encodeStringElement(descriptor, 2, result.error)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): ApiResponse<T> {
        return decoder.decodeStructure(descriptor) {
            var success: Boolean? = null
            var data: T? = null
            var error: String? = null
            var hasData = false
            var hasError = false

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> success = decodeBooleanElement(descriptor, 0)
                    1 -> {
                        data = decodeNullableSerializableElement(descriptor, 1, dataSerializer)
                        hasData = true
                    }

                    2 -> {
                        error = decodeStringElement(descriptor, 2)
                        hasError = true
                    }

                    -1 -> break
                    else -> throw SerializationException("Unknown index: $index")
                }
            }

            val successValue =
                success ?: throw SerializationException("Missing 'success' field")

            // Validate the union type structure
            if (successValue) {
                if (!hasData) {
                    throw SerializationException("Success response must have 'data' field")
                }
                if (hasError) {
                    throw SerializationException("Success response cannot have 'error' field")
                }

                // If the data serializer is kotlin.Unit, then `T` should also be a Unit
                // which is why the unchecked cast should be safe here.
                val finalData = when {
                    data == null && dataSerializer.descriptor.serialName == "kotlin.Unit" -> Unit as T
                    data != null -> data
                    else -> throw SerializationException("Data field cannot be null for successful responses")
                }

                ApiResponse.success(finalData)
            } else {
                if (hasData) {
                    throw SerializationException("Error response cannot have 'data' field")
                }
                if (!hasError) {
                    throw SerializationException("Error response must have 'error' field")
                }
                if (error == null) {
                    throw SerializationException("Error field cannot be null")
                }
                ApiResponse.error(error)
            }
        }
    }
}

@Serializable(with = ApiResponseSerializer::class)
class ApiResponse<T> private constructor(
    private val data: T?,
    private val error: String?,
    private val success: Boolean
) {
    init {
        if (success) {
            require(data != null) { "Data must be present for successful responses" }
            require(error == null) { "Error must be null for successful responses" }
        } else {
            require(data == null) { "Data must be null for failed responses" }
            require(error != null) { "Error must be present for failed responses" }
        }
    }

    fun <R> on(
        success: (T) -> R,
        error: (String) -> R
    ): R {
        return when (val res = toResult()) {
            is ApiResponseResult.SuccessResult -> {
                success(res.data)
            }

            is ApiResponseResult.ErrorResult -> {
                error(res.error)
            }
        }
    }

    /** Same as [on] but for suspend functions.
     * This is useful for coroutines where you want to handle the result asynchronously.
     */
    suspend fun <R> onSuspend(
        success: suspend (T) -> R,
        error: suspend (String) -> R
    ): R {
        return when (val res = toResult()) {
            is ApiResponseResult.SuccessResult -> {
                success(res.data)
            }

            is ApiResponseResult.ErrorResult -> {
                error(res.error)
            }
        }
    }

    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(data = data, error = null, success = true)

        fun <T> error(error: String): ApiResponse<T> =
            ApiResponse(data = null, error = error, success = false)
    }

    fun toResult(): ApiResponseResult<T> {
        return if (success) {
            ApiResponseResult.SuccessResult(data!!)
        } else {
            ApiResponseResult.ErrorResult(error!!)
        }
    }
}

sealed class ApiResponseResult<T> {
    data class SuccessResult<T>(val data: T) : ApiResponseResult<T>()
    data class ErrorResult<T>(val error: String) : ApiResponseResult<T>()
}