package pt.ipt.dama2026.mygarage.domain.model

/** 
 * Modelo de domínio que representa um veículo e o seu histórico de serviços.
 * Veículo + histórico de serviços. Usado no ecrã de perfil do veículo. 
 * 
*/
data class VehicleWithServices(
    val vehicle: Vehicle,
    val services: List<ServiceLog>
)
