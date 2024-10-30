package com.dicoding.asclepius.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.remote.response.ArticlesItem
import com.dicoding.asclepius.data.remote.response.NewsResponse
import com.dicoding.asclepius.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<ArticlesItem>>()
    val articles: LiveData<List<ArticlesItem>> get() = _articles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.postValue(false)
        _errorMessage.postValue(throwable.localizedMessage)
    }

    fun fetchNewsData(apiKey: String) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val apiService = ApiConfig.getApiService()
            apiService.getNews(apiKey).enqueue(object : Callback<NewsResponse> {
                override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val articles = response.body()?.articles?.filterNotNull()
                            ?.filter {
                                it.title != "[Removed]" &&
                                        it.description != "[Removed]"
                            } ?: emptyList()
                        _articles.value = articles
                    } else {
                        _errorMessage.value = "Error: ${response.code()}"
                    }
                }
                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = t.localizedMessage ?: "An error occurred"
                }
            })
        }
    }
}
