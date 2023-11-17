package com.example.selfieassignment

data class Image(
    var id: String = "", // A unique identifier for the image, useful for database operations
    var imageUrl: String = "", // The URL of the image
    var timestamp: Long = System.currentTimeMillis(), // The timestamp when the image was added
    var userId: String = ""
)