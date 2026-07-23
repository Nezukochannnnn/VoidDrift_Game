package com.voidrift.game.domain.usecase

import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.domain.repository.IRunRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRunHistoryUseCase @Inject constructor(
    private val runRepo: IRunRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<RunRecord>> =
        runRepo.getTopRunsByScore(limit)

    fun all(): Flow<List<RunRecord>> = runRepo.getAllRuns()

    fun deepestRun(): Flow<RunRecord?> = runRepo.getDeepestRun()
}
