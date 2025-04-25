package com.example.goalmate.di

import android.content.Context
import androidx.hilt.work.WorkerAssistedFactory
import androidx.room.Room
import androidx.work.WorkManager
import com.example.goalmate.data.localdata.AppDatabase
import com.example.goalmate.data.localdata.BadgesDao
import com.example.goalmate.data.localdata.CompletedDayDao
import com.example.goalmate.data.localdata.DaoHabits
import com.example.goalmate.data.localdata.HabitHistoryDao
import com.example.goalmate.data.localdata.UserPointsCoinDao
import com.example.goalmate.data.repository.BadgesRepository
import com.example.goalmate.data.repository.CompleteDayDaoRepository
import com.example.goalmate.viewmodel.StarCoinViewModel
import com.example.goalmate.data.repository.HabitRepository
import com.example.goalmate.data.repository.HistoryHabitsRepository
import com.example.goalmate.data.repository.MotivationQuoteRepository
import com.example.goalmate.data.repository.PointsRepository
import com.example.goalmate.data.repository.StarCoinRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ExerciseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "goalmate_dataasee"
        ).fallbackToDestructiveMigration().build()
    }




    @Provides
    @Singleton
    fun provideDao(appDatabase: AppDatabase): DaoHabits {
        return appDatabase.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitHistoryDao(database: AppDatabase): HabitHistoryDao {
        return database.habitHistoryDao()
    }



    @Provides
    @Singleton

    fun providePointsRepository(firestore: FirebaseFirestore , auth: FirebaseAuth) : PointsRepository{
        return PointsRepository(auth,firestore)
    }


    @Provides
    @Singleton
    fun provideExerciseRepository(
        daoHabits: DaoHabits,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): HabitRepository {
        return HabitRepository(daoHabits, firestore, auth)
    }

    // --------------Tamamlanan günler fonksiyonları--------------

    @Provides
    @Singleton
    fun provideCompletedDayDao(appDatabase: AppDatabase): CompletedDayDao {
        return appDatabase.completedDayDao()
    }

    @Provides
    @Singleton
    fun provideMotivationQuoteRepository(firebaseDatabase: FirebaseDatabase): MotivationQuoteRepository {
        return MotivationQuoteRepository(firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideCompleteDayRepository(completedDayDao: CompletedDayDao): CompleteDayDaoRepository {
        return CompleteDayDaoRepository(completedDayDao)
    }

    @Provides
    @Singleton
    fun ProvidePointsCoinDao(appDatabase: AppDatabase): UserPointsCoinDao {
        return appDatabase.userPointsCoinDao()
    }

    @Provides
    @Singleton
    fun provideStarCoinRepository(userPointsCoinDao: UserPointsCoinDao): StarCoinRepository {
        return StarCoinRepository(userPointsCoinDao)
    }

    @Provides
    @Singleton
    fun provideHistoryHabitsRepository(habitHistoryDao: HabitHistoryDao): HistoryHabitsRepository {
        return HistoryHabitsRepository(habitHistoryDao)
    }

    @Provides
    @Singleton
    fun provideStarCoinViewModel(starCoinRepository: StarCoinRepository): StarCoinViewModel {
        return StarCoinViewModel(starCoinRepository)
    }




    //------------------------------------Badges----------------------------------------------------//

    @Provides
    @Singleton
    fun provideBadgesDao(appDatabase: AppDatabase): BadgesDao{
        return appDatabase.badgesDao()
    }


    @Provides
    @Singleton
    fun provideBadgesRepository(badgesDao: BadgesDao) : BadgesRepository{
        return BadgesRepository(badgesDao)
    }
}


