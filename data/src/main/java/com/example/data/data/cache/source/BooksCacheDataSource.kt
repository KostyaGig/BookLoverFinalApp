package com.example.data.data.cache.source

import com.example.data.data.cache.db.BooksDao
import com.example.data.data.cache.models.BookDb
import com.example.data.data.models.BookData
import com.example.domain.domain.Mapper
import com.example.domain.models.Resource

interface BooksCacheDataSource {

    suspend fun fetchBooks(): List<BookDb>

    suspend fun saveBooks(books: List<BookData>)


    class Base(
        private val bookDao: BooksDao,
        private val dataMapper: Mapper<BookData, BookDb>,
    ) : BooksCacheDataSource {
        override suspend fun fetchBooks() = bookDao.getAllBooks()

        override suspend fun saveBooks(books: List<BookData>) {
            books.forEach { book -> bookDao.addNewBook(book = dataMapper.map(book)) }
        }

    }
}