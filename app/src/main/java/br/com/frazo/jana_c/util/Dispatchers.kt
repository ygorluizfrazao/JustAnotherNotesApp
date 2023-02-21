package br.com.frazo.jana_c.util

import kotlinx.coroutines.CoroutineDispatcher

data class Dispatchers(
    val io: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO,
    val main: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main,
    val default: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
    val unconfined: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
)