package pt.ipt.dama2026.mygarage.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pt.ipt.dama2026.mygarage.data.repository.OfflineVehicleRepository
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import javax.inject.Singleton

/**
 * Módulo Hilt que liga a interface de domínio à implementação concreta.
 *
 * Usa @Binds (em vez de @Provides) porque a implementação OfflineVehicleRepository
 * já é injetável via @Inject — o Hilt só precisa de saber que quando alguém pede
 * um VehicleRepository, deve receber um OfflineVehicleRepository.
 *
 * Isto permite que a UI e os ViewModels dependam apenas da interface (VehicleRepository),
 * sem conhecerem a implementação Room. Se um dia se trocar a BD, basta mudar aqui.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: OfflineVehicleRepository): VehicleRepository
}
