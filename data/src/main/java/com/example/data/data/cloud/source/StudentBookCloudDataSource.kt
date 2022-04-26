package com.example.data.data.cloud.source

import com.example.data.base.BaseApiResponse
import com.example.data.data.cloud.mappers.BookMapper
import com.example.data.data.cloud.mappers.BookThatReadMapper
import com.example.data.data.cloud.models.AddNewBookCloud
import com.example.data.data.cloud.models.PostRequestAnswer
import com.example.data.data.cloud.models.UpdateChaptersCloud
import com.example.data.data.cloud.models.UpdateProgressCloud
import com.example.data.data.cloud.service.StudentBookService
import com.example.data.data.models.ChaptersData
import com.example.data.data.models.ProgressData
import com.example.data.data.models.StudentBookData
import com.example.domain.models.Resource
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface StudentBookCloudDataSource {

    suspend fun fetchMyBooks(id: String): Resource<List<StudentBookData>>

    suspend fun deleteBook(id: String): Resource<Unit>

    suspend fun addNewBook(book: AddNewBookCloud): Resource<PostRequestAnswer>

    suspend fun updateProgress(id: String, progress: ProgressData): Resource<Unit>


    suspend fun updateChapters(id: String, chapters: ChaptersData): Resource<Unit>

    class Base(
        private val service: StudentBookService,
    ) : StudentBookCloudDataSource,
        BaseApiResponse() {

        override suspend fun fetchMyBooks(id: String): Resource<List<StudentBookData>> = try {
            val bookList = mutableListOf<StudentBookData>()

            val booksThatReadCloud = service.fetchMyBooks(id = "{\"userId\":\"${id}\"}")

            booksThatReadCloud.body()!!.books.forEach { booksThatRead ->

                val response = service.getBook(id = "{\"objectId\":\"${booksThatRead.bookId}\"}")

                val bookCloud = response.body()!!.books[0]

                val thatReadBook = BookThatReadMapper.Base(
                    progress = booksThatRead.progress,
                    objectId = booksThatRead.objectId,
                    createdAt = booksThatRead.createdAt,
                    chaptersRead = booksThatRead.chaptersRead,
                    bookId = booksThatRead.bookId,
                    studentId = booksThatRead.studentId,
                    isReadingPages = booksThatRead.isReadingPages)

                val book = BookMapper.Base(author = bookCloud.author,
                    id = bookCloud.id,
                    poster = bookCloud.poster,
                    page = bookCloud.page,
                    chapterCount = bookCloud.chapterCount,
                    publicYear = bookCloud.publicYear,
                    book = bookCloud.book,
                    title = bookCloud.title, updatedAt = bookCloud.updatedAt)

                bookList.add(thatReadBook.map(BookMapper.ComplexMapper(book)))
            }
            Resource.success(bookList)
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> Resource.error(message = "Нету подключение к интернету")
                is SocketTimeoutException -> Resource.error(message = "Нету подключение к интернету")
                is HttpException -> Resource.error(message = "Ошибка Сервера")
                else -> Resource.error(message = "Что то пошло не так")
            }
        }


        override suspend fun deleteBook(id: String) = safeApiCall { service.deleteMyBook(id = id) }


        override suspend fun addNewBook(book: AddNewBookCloud): Resource<PostRequestAnswer> =
            safeApiCall { service.addNewBookStudent(book = book) }

        override suspend fun updateProgress(id: String, progress: ProgressData): Resource<Unit> =
            safeApiCall {
                service.updateProgressStudentBook(id = id,
                    progress = UpdateProgressCloud(progress = progress.progress))
            }


        override suspend fun updateChapters(id: String, chapters: ChaptersData): Resource<Unit> =
            safeApiCall {
                service.updatePagesStudentBook(id = id,
                    chapters = UpdateChaptersCloud(chaptersRead = chapters.chaptersRead,
                        isReadingPages = chapters.isReadingPages))
            }


    }
}


