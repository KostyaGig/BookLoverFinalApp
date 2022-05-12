package com.example.data.cloud.source

import com.example.data.ResourceProvider
import com.example.data.base.BaseApiResponse
import com.example.data.cloud.mappers.StudentBookMapper
import com.example.data.cloud.mappers.UserMapper
import com.example.data.cloud.models.UpdateCloud
import com.example.data.cloud.models.UserUpdateCloud
import com.example.data.cloud.service.UserService
import com.example.data.models.StudentData
import com.example.data.models.UserImageData
import com.example.domain.Mapper
import com.example.domain.Resource
import com.example.domain.models.UpdateAnswerDomain
import com.example.domain.models.UserUpdateDomain

interface UsersCloudDataSource {

    suspend fun fetchMyStudents(classId: String): Resource<List<StudentData>>

    suspend fun updateUser(
        id: String,
        user: UserUpdateDomain,
        sessionToken: String,
    ): Resource<UpdateAnswerDomain>

    class Base(
        private val service: UserService,
        private val userToDataMapper: Mapper<UserUpdateDomain, UserUpdateCloud>,
        private val updateMapper: Mapper<UpdateCloud, UpdateAnswerDomain>,
        private val resourceProvider: ResourceProvider,
    ) : UsersCloudDataSource, BaseApiResponse(resourceProvider = resourceProvider) {

        override suspend fun fetchMyStudents(
            classId: String,
        ): Resource<List<StudentData>> = try {
            val studentList = mutableListOf<StudentData>()
            val user =
                service.fetchMyBook(id = "{\"userType\":\"student\",\"classId\":\"$classId\"}")

            user.body()!!.users.forEach { userCloud ->
                val response =
                    service.fetchStudentAttributes("{\"userId\":\"${userCloud.objectId}\"}")

                var booksRead = 0
                var chapterRead = 0
                var progress = 0
                val booksId = mutableListOf<String>()
                response.body()!!.books.forEach { bookThatReadCloud ->
                    if (bookThatReadCloud.isReadingPages[bookThatReadCloud.isReadingPages.lastIndex]) booksRead += 1
                    chapterRead += bookThatReadCloud.chaptersRead
                    progress += bookThatReadCloud.progress
                    booksId.add(bookThatReadCloud.bookId)
                }

                val attributes =
                    StudentBookMapper.Base(chaptersRead = chapterRead,
                        booksRead = booksRead,
                        progress = progress,
                        booksId = booksId)

                val student =
                    UserMapper.Base(
                        objectId = userCloud.objectId,
                        className = userCloud.className,
                        createAt = userCloud.createAt,
                        classId = userCloud.classId,
                        userType = userCloud.userType,
                        schoolName = userCloud.schoolName,
                        image = UserImageData(
                            name = userCloud.image.name,
                            url = userCloud.image.url,
                            type = userCloud.image.type),
                        email = userCloud.email,
                        gender = userCloud.gender,
                        lastname = userCloud.lastname,
                        name = userCloud.name,
                        number = userCloud.number)

                studentList.add(student.map(StudentBookMapper.ComplexMapper(attributes)))

            }
            Resource.success(studentList)

        } catch (exception: Exception) {
            Resource.error(message = resourceProvider.errorType(exception = exception))

        }

        override suspend fun updateUser(
            id: String,
            user: UserUpdateDomain,
            sessionToken: String,
        ): Resource<UpdateAnswerDomain> = safeApiMapperCall(mapper = updateMapper) {
            service.updateUser(sessionToken = sessionToken,
                id = id,
                student = userToDataMapper.map(user))
        }
    }
}