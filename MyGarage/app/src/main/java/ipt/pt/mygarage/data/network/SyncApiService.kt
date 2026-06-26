package ipt.pt.mygarage.data.network

import com.google.gson.annotations.SerializedName
import ipt.pt.mygarage.data.model.SyncPullResponse
import ipt.pt.mygarage.data.model.SyncPushBody
import ipt.pt.mygarage.data.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

data class UserProfileUpdate(
    @SerializedName("name") val name: String?,
    @SerializedName("garageName") val garageName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class UserProfileResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("garageName") val garageName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

interface SyncApiService {

    @POST("api/sync/push")
    suspend fun push(@Body body: SyncPushBody): Response<SyncResponse>

    @GET("api/sync/pull")
    suspend fun pull(
        @Query("lastSyncTimestamp") lastSyncTimestamp: String? = null
    ): Response<SyncPullResponse>

    @POST("api/sync/merge-guest-data")
    suspend fun mergeGuestData(@Body body: SyncPushBody): Response<SyncResponse>

    @GET("api/sync/pull-initial")
    suspend fun pullInitial(
        @Query("lastSyncTimestamp") lastSyncTimestamp: String? = null
    ): Response<SyncPullResponse>

    @PATCH("api/user/profile")
    suspend fun updateProfile(@Body profile: UserProfileUpdate): Response<SyncResponse>

    @GET("api/user/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @DELETE("api/user/account")
    suspend fun deleteAccount(): Response<SyncResponse>
}
