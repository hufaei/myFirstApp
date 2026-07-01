package com.example.lifelab.feature.discover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.DiscoverContent
import com.example.lifelab.feature.discover.domain.LoadDiscoverContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val loadDiscoverContent: LoadDiscoverContentUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadContent(DiscoverCategory.All)
    }

    fun onEvent(event: DiscoverUiEvent) {
        when (event) {
            is DiscoverUiEvent.CategorySelected -> onCategorySelected(event.category)
            is DiscoverUiEvent.ContentSelected -> onContentSelected(event.contentId)
            DiscoverUiEvent.DetailDismissed -> dismissDetail()
            DiscoverUiEvent.RetrySelected -> retry()
        }
    }

    private fun onCategorySelected(category: DiscoverCategory) {
        loadContent(category)
    }

    private fun retry() {
        loadContent(_uiState.value.selectedCategory)
    }

    private fun onContentSelected(contentId: String) {
        val content = (_uiState.value.listState as? DiscoverListState.Content)
            ?.items
            ?.firstOrNull { item -> item.id == contentId }
            ?: return

        _uiState.update {
            it.copy(selectedContentDetail = content.toDetail())
        }
    }

    private fun dismissDetail() {
        _uiState.update { it.copy(selectedContentDetail = null) }
    }

    private fun loadContent(category: DiscoverCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                listState = DiscoverListState.Loading,
                selectedContentDetail = null,
            )
        }

        viewModelScope.launch {
            val listState = when (val result = loadDiscoverContent(category)) {
                is AppResult.Failure -> DiscoverListState.Error(result.error.message)
                is AppResult.Success -> {
                    if (result.value.isEmpty()) {
                        DiscoverListState.Empty
                    } else {
                        DiscoverListState.Content(result.value)
                    }
                }
            }

            _uiState.update { it.copy(listState = listState) }
        }
    }

    private fun DiscoverContent.toDetail(): DiscoverContentDetail =
        when (this) {
            is DiscoverContent.Article -> DiscoverContentDetail(
                id = id,
                title = title,
                summary = summary,
                tag = tag,
                metadata = author,
                kind = DiscoverContentKind.Article,
                category = DiscoverCategory.Articles,
            )

            is DiscoverContent.Course -> DiscoverContentDetail(
                id = id,
                title = title,
                summary = summary,
                tag = tag,
                metadata = listOf(instructor, duration)
                    .filter { value -> value.isNotBlank() }
                    .joinToString(" · "),
                kind = DiscoverContentKind.Course,
                category = DiscoverCategory.Courses,
            )

            is DiscoverContent.Offer.Product -> DiscoverContentDetail(
                id = id,
                title = title,
                summary = summary,
                tag = tag,
                metadata = priceLabel,
                kind = DiscoverContentKind.Product,
                category = DiscoverCategory.Offers,
            )

            is DiscoverContent.Offer.Membership -> DiscoverContentDetail(
                id = id,
                title = title,
                summary = summary,
                tag = tag,
                metadata = priceLabel,
                kind = DiscoverContentKind.Membership,
                category = DiscoverCategory.Membership,
            )
        }
}
