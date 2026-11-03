package com.example.proyecto_appmovil.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyecto_appmovil.InscripcionesApp
import com.example.proyecto_appmovil.ui.screens.auth.LoginViewModel
import com.example.proyecto_appmovil.ui.screens.auth.RegisterViewModel
import com.example.proyecto_appmovil.ui.screens.documents.DocumentsViewModel
import com.example.proyecto_appmovil.ui.screens.home.HomeViewModel
import com.example.proyecto_appmovil.ui.screens.notifications.NotificationsViewModel
import com.example.proyecto_appmovil.ui.screens.payment.PaymentViewModel
import com.example.proyecto_appmovil.ui.screens.photo.CredentialPhotoViewModel
import com.example.proyecto_appmovil.ui.screens.profile.ProfileViewModel
import com.example.proyecto_appmovil.ui.screens.status.StatusViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LoginViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            RegisterViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            HomeViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            DocumentsViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            NotificationsViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            PaymentViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            CredentialPhotoViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            ProfileViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
        initializer {
            StatusViewModel(
                repository = inscripcionesApplication().container.inscripcionesRepository
            )
        }
    }
}

fun CreationExtras.inscripcionesApplication(): InscripcionesApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InscripcionesApp)
