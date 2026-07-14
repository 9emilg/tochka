package bg.tochka.reader.data.remote

import bg.tochka.reader.data.remote.dto.GithubReleaseDto
import retrofit2.http.GET

interface GithubApiService {

    @GET("repos/9emilg/tochka/releases/latest")
    suspend fun getLatestRelease(): GithubReleaseDto

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}
