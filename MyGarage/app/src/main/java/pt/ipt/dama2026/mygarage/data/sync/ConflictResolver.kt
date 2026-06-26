package pt.ipt.dama2026.mygarage.data.sync

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.model.PartPayload
import pt.ipt.dama2026.mygarage.data.model.PiecePayload
import pt.ipt.dama2026.mygarage.data.model.ServiceLogPayload
import pt.ipt.dama2026.mygarage.data.model.ServiceLogPieceCrossRefPayload
import pt.ipt.dama2026.mygarage.data.model.VehiclePayload
import pt.ipt.dama2026.mygarage.data.model.parseIso8601
import pt.ipt.dama2026.mygarage.data.model.*

object ConflictResolver {

    fun mergeVehicles(
        local: List<VehicleEntity>,
        remote: List<VehiclePayload>
    ): List<VehicleEntity> {
        val merged = mutableMapOf<String, VehicleEntity>()
        local.forEach { merged[it.id] = it }
        
        remote.forEach { remoteVehicle ->
            val localVehicle = merged[remoteVehicle.id]
            val remoteTime = parseIso8601(remoteVehicle.updatedAt)
            
            if (localVehicle == null || remoteTime > localVehicle.updatedAt) {
                merged[remoteVehicle.id] = remoteVehicle.toEntity()
            }
        }
        return merged.values.toList()
    }

    fun mergeServiceLogs(
        local: List<ServiceLogEntity>,
        remote: List<ServiceLogPayload>
    ): List<ServiceLogEntity> {
        val merged = mutableMapOf<String, ServiceLogEntity>()
        local.forEach { merged[it.id.toString()] = it }
        
        remote.forEach { remoteLog ->
            val localLog = merged[remoteLog.id]
            val remoteTime = parseIso8601(remoteLog.updatedAt)
            
            if (localLog == null || remoteTime > localLog.updatedAt) {
                merged[remoteLog.id] = remoteLog.toEntity()
            }
        }
        return merged.values.toList()
    }

    fun mergeParts(
        local: List<PartEntity>,
        remote: List<PartPayload>
    ): List<PartEntity> {
        val merged = mutableMapOf<String, PartEntity>()
        local.forEach { merged[it.id] = it }
        
        remote.forEach { remotePart ->
            val localPart = merged[remotePart.id]
            val remoteTime = parseIso8601(remotePart.updatedAt)
            
            if (localPart == null || remoteTime > localPart.updatedAt) {
                merged[remotePart.id] = remotePart.toEntity()
            }
        }
        return merged.values.toList()
    }

    fun mergePieces(
        local: List<PieceEntity>,
        remote: List<PiecePayload>
    ): List<PieceEntity> {
        val merged = mutableMapOf<String, PieceEntity>()
        local.forEach { merged[it.id] = it }
        
        remote.forEach { remotePiece ->
            val localPiece = merged[remotePiece.id]
            val remoteTime = parseIso8601(remotePiece.updatedAt)
            
            if (localPiece == null || remoteTime > localPiece.updatedAt) {
                merged[remotePiece.id] = remotePiece.toEntity()
            }
        }
        return merged.values.toList()
    }

    fun mergeCrossRefs(
        local: List<ServiceLogPieceCrossRef>,
        remote: List<ServiceLogPieceCrossRefPayload>
    ): List<ServiceLogPieceCrossRef> {
        val merged = mutableMapOf<String, ServiceLogPieceCrossRef>()
        local.forEach { merged["${it.serviceLogId}|${it.pieceId}"] = it }
        
        remote.forEach { remoteCrossRef ->
            val key = "${remoteCrossRef.serviceLogId}|${remoteCrossRef.pieceId}"
            val localCrossRef = merged[key]
            val remoteTime = parseIso8601(remoteCrossRef.updatedAt)
            
            if (localCrossRef == null || remoteTime > localCrossRef.updatedAt) {
                merged[key] = remoteCrossRef.toEntity()
            }
        }
        return merged.values.toList()
    }
}
