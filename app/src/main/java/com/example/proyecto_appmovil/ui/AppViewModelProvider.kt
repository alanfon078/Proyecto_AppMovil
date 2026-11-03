package com.example.proyecto_appmovil.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyecto_appmovil.InscripcionesApp
import com.example.proyecto_appmovil.ui.screens.admin.AdminAspiranteDetailViewModel
import com.example.proyecto_appmovil.ui.screens.admin.AdminFechasViewModel
import com.example.proyecto_appmovil.ui.screens.admin.AdminHomeViewModel
import com.example.proyecto_appmovil.ui.screens.auth.LoginViewModel
import com.example.proyecto_appmovil.ui.screens.auth.RegisterViewModel
import com.example.proyecto_appmovil.ui.screens.documents.DocumentsViewModel
import com.example.proyecto_appmovil.ui.screens.home.HomeViewModel
import com.example.proyecto_appmovil.ui.screens.notifications.NotificationsViewModel
import com.example.proyecto_appmovil.ui.screens.payment.PaymentViewModel
import com.example.proyecto_appmovil.ui.screens.photo.CredentialPhotoViewModel
import com.example.proyecto_appmovil.ui.screens.profile.ProfileViewModel
import com.example.proyecto_appmovil.ui.screens.reglamento.ReglamentoViewModel
import com.example.proyecto_appmovil.ui.screens.status.StatusViewModel

/**
 * Proveedor central de ViewModels que inyecta el repositorio
 * a través del AppContainer (Application -> Activity -> ViewModel).
 */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LoginViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            RegisterViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            HomeViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            DocumentsViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            StatusViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            ProfileViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            NotificationsViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            CredentialPhotoViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            PaymentViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            ReglamentoViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            AdminHomeViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            AdminFechasViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
        initializer {
            AdminAspiranteDetailViewModel(inscripcionesApp().container.inscripcionesRepository)
        }
    }
}

/**
 * Extensión para recuperar la instancia de Application como InscripcionesApp.
 */
fun CreationExtras.inscripcionesApp(): InscripcionesApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InscripcionesApp)
