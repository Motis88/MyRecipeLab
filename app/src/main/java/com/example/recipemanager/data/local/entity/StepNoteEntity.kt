package com.example.recipemanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "step_notes",
    foreignKeys = [
        ForeignKey(
            entity = StepEntity::class,
            parentColumns = ["id"],
            childColumns = ["stepId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stepId")]
)
data class StepNoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "stepId")
    val stepId: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "orderIndex")
    val orderIndex: Int
)
