package com.adyen.android.assignment.modelFactory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adyen.android.assignment.repository.IRepository

/**
 * View Model Factory Generator
 */
class ViewModelFactory(private val repository: IRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IRepository::class.java)
            .newInstance(repository)
    }
}
