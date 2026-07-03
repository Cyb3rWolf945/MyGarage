package pt.ipt.dama2026.mygarage.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pt.ipt.dama2026.mygarage.data.repository.OfflineVehicleRepository
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: OfflineVehicleRepository): VehicleRepository
}
