package com.typly.app.di

import android.app.Application
import android.content.Context
import com.typly.app.presentation.call.AgoraManager
import com.typly.app.domain.usecase.ValidateLoginUseCase
import com.typly.app.domain.usecase.ValidateBasicRegisterUseCase
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.data.repository.AuthRepositoryImpl
import com.typly.app.data.remote.service.AuthService
import com.typly.app.data.remote.service.AuthServiceImpl
import com.typly.app.domain.repository.CallRepository
import com.typly.app.data.repository.CallRepositoryImpl
import com.typly.app.domain.repository.UserRepository
import com.typly.app.data.repository.UserRepositoryImpl
import com.typly.app.presentation.call.UserPresenceManager
import com.typly.app.domain.usecase.ValidateCompleteRegisterUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides application-wide singleton dependencies.
 *
 * This object is responsible for defining how to create and provide instances of
 * repositories, services, use cases, and managers that are needed throughout the
 * application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides an instance of [ValidateLoginUseCase].
     */
    @Provides
    fun provideValidateLoginUseCase(): ValidateLoginUseCase = ValidateLoginUseCase()

    /**
     * Provides an instance of [ValidateBasicRegisterUseCase].
     */
    @Provides
    fun provideValidateBasicRegisterUseCase(): ValidateBasicRegisterUseCase = ValidateBasicRegisterUseCase()

    /**
     * Provides an instance of [ValidateCompleteRegisterUseCase].
     */
    @Provides
    fun provideValidateCompleteRegisterUseCase(): ValidateCompleteRegisterUseCase = ValidateCompleteRegisterUseCase()

    /**
     * Provides a singleton instance of [AuthRepository] by providing its implementation.
     * @param authRepositoryImpl The concrete implementation of the repository.
     * @return A singleton [AuthRepository] instance.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository = authRepositoryImpl

    /**
     * Provides a singleton instance of [UserRepository] by providing its implementation.
     * @param userRepositoryImpl The concrete implementation of the repository.
     * @return A singleton [UserRepository] instance.
     */
    @Provides
    @Singleton
    fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository = userRepositoryImpl

    /**
     * Provides a singleton instance of [AuthService] by providing its implementation.
     * @param authServiceImpl The concrete implementation of the service.
     * @return A singleton [AuthService] instance.
     */
    @Provides
    @Singleton
    fun provideAuthService(authServiceImpl: AuthServiceImpl): AuthService = authServiceImpl

    /**
     * Provides a singleton instance of [UserPresenceManager] for handling user online/offline status.
     * @param userRepository Repository to update user status.
     * @param firebaseAuth To get the current user ID.
     * @param firestore To interact with Firestore's presence system.
     * @param application The application context.
     * @return A singleton [UserPresenceManager] instance.
     */
    @Provides
    @Singleton
    fun provideUserPresenceManager(
        userRepository: UserRepository,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        application: Application
    ): UserPresenceManager {
        return UserPresenceManager(userRepository, firebaseAuth, firestore, application)
    }

    /**
     * Provides a singleton instance of [CallRepository] by providing its implementation.
     * @param callRepositoryImpl The concrete implementation of the repository.
     * @return A singleton [CallRepository] instance.
     */
    @Provides
    @Singleton
    fun provideCallRepository(callRepositoryImpl: CallRepositoryImpl): CallRepository = callRepositoryImpl

    /**
     * Provides a singleton instance of [AgoraManager] for handling real-time call functionality.
     * @param context The application context.
     * @return A singleton [AgoraManager] instance.
     */
    @Provides
    @Singleton
    fun provideAgoraManager(@ApplicationContext context: Context): AgoraManager = AgoraManager(context)

}
