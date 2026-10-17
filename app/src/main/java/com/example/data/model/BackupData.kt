package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "2.0",
    val maids: List<Maid>,
    val records: List<AttendanceRecord>,
    val salarySnapshots: List<SalarySnapshot> = emptyList()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("version", appVersion)
        root.put("timestamp", exportTimestamp)

        val maidsArray = JSONArray()
        for (maid in maids) {
            val maidObj = JSONObject()
            maidObj.put("id", maid.id)
            maidObj.put("name", maid.name)
            maidObj.put("role", maid.role)
            maidObj.put("monthlySalary", maid.monthlySalary)
            maidObj.put("freeLeavesPerMonth", maid.freeLeavesPerMonth)
            maidObj.put("createdAt", maid.createdAt)
            maidsArray.put(maidObj)
        }
        root.put("maids", maidsArray)

        val recordsArray = JSONArray()
        for (rec in records) {
            val recObj = JSONObject()
            recObj.put("id", rec.id)
            recObj.put("maidId", rec.maidId)
            recObj.put("date", rec.date)
            recObj.put("status", rec.status.name)
            recObj.put("notes", rec.notes ?: "")
            recObj.put("createdAt", rec.createdAt)
            recObj.put("updatedAt", rec.updatedAt)
            recordsArray.put(recObj)
        }
        root.put("records", recordsArray)

        val snapshotsArray = JSONArray()
        for (snap in salarySnapshots) {
            val snapObj = JSONObject()
            snapObj.put("id", snap.id)
            snapObj.put("maidId", snap.maidId)
            snapObj.put("month", snap.month)
            snapObj.put("monthlySalary", snap.monthlySalary)
            snapObj.put("freeLeavesPerMonth", snap.freeLeavesPerMonth)
            snapObj.put("dailyDeductionRate", snap.dailyDeductionRate)
            snapObj.put("totalLeaveDays", snap.totalLeaveDays)
            snapObj.put("freeLeavesConsumed", snap.freeLeavesConsumed)
            snapObj.put("unpaidLeaveDays", snap.unpaidLeaveDays)
            snapObj.put("leaveDeduction", snap.leaveDeduction)
            snapObj.put("estimatedPayableSalary", snap.estimatedPayableSalary)
            snapObj.put("isFinalized", snap.isFinalized)
            snapObj.put("finalizedAt", snap.finalizedAt)
            snapshotsArray.put(snapObj)
        }
        root.put("salarySnapshots", snapshotsArray)

        return root.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): BackupData {
            val root = JSONObject(jsonStr)
            val version = root.optString("version", "2.0")
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            val maids = mutableListOf<Maid>()
            val maidsArray = root.optJSONArray("maids")
            if (maidsArray != null) {
                for (i in 0 until maidsArray.length()) {
                    val obj = maidsArray.getJSONObject(i)
                    // Support both new freeLeavesPerMonth and backward-compatible fallback
                    val freeLeaves = if (obj.has("freeLeavesPerMonth")) {
                        obj.getInt("freeLeavesPerMonth")
                    } else {
                        2 // sensible default if upgrading from v1
                    }
                    maids.add(
                        Maid(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            role = obj.optString("role", "General Housekeeping"),
                            monthlySalary = obj.optDouble("monthlySalary", 5000.0),
                            freeLeavesPerMonth = freeLeaves,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val records = mutableListOf<AttendanceRecord>()
            val recordsArray = root.optJSONArray("records")
            if (recordsArray != null) {
                for (i in 0 until recordsArray.length()) {
                    val obj = recordsArray.getJSONObject(i)
                    val statusStr = obj.getString("status")
                    val status = AttendanceStatus.fromString(statusStr) ?: AttendanceStatus.FULL_DAY
                    val notes = obj.optString("notes", "")
                    records.add(
                        AttendanceRecord(
                            id = obj.optLong("id", 0),
                            maidId = obj.getLong("maidId"),
                            date = obj.getString("date"),
                            status = status,
                            notes = if (notes.isBlank()) null else notes,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val snapshots = mutableListOf<SalarySnapshot>()
            val snapshotsArray = root.optJSONArray("salarySnapshots")
            if (snapshotsArray != null) {
                for (i in 0 until snapshotsArray.length()) {
                    val obj = snapshotsArray.getJSONObject(i)
                    snapshots.add(
                        SalarySnapshot(
                            id = obj.optLong("id", 0),
                            maidId = obj.getLong("maidId"),
                            month = obj.getString("month"),
                            monthlySalary = obj.getDouble("monthlySalary"),
                            freeLeavesPerMonth = obj.getInt("freeLeavesPerMonth"),
                            dailyDeductionRate = obj.getDouble("dailyDeductionRate"),
                            totalLeaveDays = obj.getDouble("totalLeaveDays"),
                            freeLeavesConsumed = obj.getDouble("freeLeavesConsumed"),
                            unpaidLeaveDays = obj.getDouble("unpaidLeaveDays"),
                            leaveDeduction = obj.getDouble("leaveDeduction"),
                            estimatedPayableSalary = obj.getDouble("estimatedPayableSalary"),
                            isFinalized = obj.optBoolean("isFinalized", true),
                            finalizedAt = obj.optLong("finalizedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            return BackupData(
                exportTimestamp = timestamp,
                appVersion = version,
                maids = maids,
                records = records,
                salarySnapshots = snapshots
            )
        }
    }
}
