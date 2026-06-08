package ipt.pt.mygarage.data.repository

import ipt.pt.mygarage.data.model.VehicleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface VehicleRepository {
    fun getAllVehiclesStream(): Flow<List<VehicleEntity>>
}

class InMemoryVehicleRepository : VehicleRepository {

    private val vehicles = listOf(
        VehicleEntity(
            id = "porsche-911-gt3-rs",
            modelName = "Porsche 911 GT3 RS",
            plate = "911-GT3-RS",
            mileage = "12,450 mi",
            year = "2024",
            status = "READY"
        ),
        VehicleEntity(
            id = "bmw-m4-comp",
            modelName = "BMW M4 Competition",
            plate = "BMW-M4-COMP",
            mileage = "8,920 mi",
            year = "2023",
            status = "IN SERVICE"
        )
    )

    override fun getAllVehiclesStream(): Flow<List<VehicleEntity>> = flowOf(vehicles)
}
