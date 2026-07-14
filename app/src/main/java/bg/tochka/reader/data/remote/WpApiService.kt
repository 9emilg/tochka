package bg.tochka.reader.data.remote

import bg.tochka.reader.data.remote.dto.CategoryDto
import bg.tochka.reader.data.remote.dto.PostDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WpApiService {

    @GET("wp-json/wp/v2/posts/{id}?_embed")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @GET("wp-json/wp/v2/posts?_embed")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
        @Query("categories") categoryId: Int? = null,
    ): List<PostDto>

    @GET("wp-json/wp/v2/posts?_embed")
    suspend fun searchPosts(
        @Query("search") query: String,
        @Query("per_page") perPage: Int = 20,
    ): List<PostDto>

    @GET("wp-json/wp/v2/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 50,
    ): List<CategoryDto>

    companion object {
        const val BASE_URL = "https://svobodnatochka.bg/"
    }
}
