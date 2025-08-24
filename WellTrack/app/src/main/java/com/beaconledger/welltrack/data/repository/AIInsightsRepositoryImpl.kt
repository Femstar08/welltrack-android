package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.AIInsightsRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime