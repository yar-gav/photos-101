package com.example.photos101.domain.model

/**
 * Result of a paged photo list (recent or search).
 */
data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalCount: Int
)
