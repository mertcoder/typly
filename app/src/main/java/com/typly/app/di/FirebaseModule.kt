package com.typly.app.di

import com.typly.app.domain.repository.ChatRepository
import com.typly.app.data.repository.ChatRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Firebase-related dependencies.
 *
 * This module is responsible for providing singleton instances of various Firebase services
 * and binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    /**
     * Binds the [ChatRepository] interface to its implementation [ChatRepositoryImpl].
     * This allows for injecting the interface while Hilt provides the concrete implementation.
     *
     * @param chatRepositoryImpl The concrete implementation of the ChatRepository.
     * @return An instance of [ChatRepository].
     */
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    companion object {
        /**
         * Provides a singleton instance of [FirebaseAuth].
         * @return The singleton [FirebaseAuth] instance.
         */
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        /**
         * Provides a singleton instance of [FirebaseFirestore].
         * @return The singleton [FirebaseFirestore] instance.
         */
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        /**
         * Provides a singleton instance of [FirebaseStorage].
         * @return The singleton [FirebaseStorage] instance.
         */
        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }

        /**
         * Provides a singleton instance of [FirebaseMessaging] for FCM operations.
         * @return The singleton [FirebaseMessaging] instance.
         */
        @Provides
        @Singleton
        fun provideFirebaseCloudMessaging(): FirebaseMessaging {
            return FirebaseMessaging.getInstance()
        }
    }
}
