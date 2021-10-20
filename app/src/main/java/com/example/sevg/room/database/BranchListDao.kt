package com.example.sevg.room.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sevg.room.model.BranchListColumns

@Dao
interface BranchListDao
{
    @Query("SELECT * FROM BRANCH_LIST")
    fun getBranchListDetails(): LiveData<List<BranchListColumns>>?

    @Query("DELETE FROM BRANCH_LIST")
    fun clearBranchListTable()

    @Insert fun insertBranchListDetails(branchListDetails: List<BranchListColumns>)
    @Update fun updateBranchListDetails(branchListDetails: BranchListColumns?)
    @Delete fun delete(branchListDetails: BranchListColumns?)
}