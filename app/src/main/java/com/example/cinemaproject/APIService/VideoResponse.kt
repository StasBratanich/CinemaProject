package com.example.cinemaproject.APIService
import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("results")
    val videos: List<Video>
)

data class Video(
    @SerializedName("key")
    val key: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("site")
    val site: String,
    @SerializedName("type")
    val type: String
)