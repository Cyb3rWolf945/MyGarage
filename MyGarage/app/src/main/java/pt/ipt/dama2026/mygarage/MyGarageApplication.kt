package pt.ipt.dama2026.mygarage

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application da app. Anotada com @HiltAndroidApp para ativar
 * a injeção de dependências com Hilt em toda a aplicação.
 * É o ponto de entrada do grafo de dependências.
 * Dependecy layer (DI) é inicializada aqui.
 */
@HiltAndroidApp
class MyGarageApplication : Application()
