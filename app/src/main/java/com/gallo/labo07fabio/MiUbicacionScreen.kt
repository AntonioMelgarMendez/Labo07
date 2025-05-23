package com.gallo.labo07fabio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

data class Place(
    val name: String,
    val remark: String,
    val latLng: LatLng,
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MiUbicacionScreen(
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fineAccessPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseAccessPermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val locationText = remember { mutableStateOf("Presiona el botón para obtener la ubicación.") }

    fun requestLocation() {
        if (!coarseAccessPermission.status.isGranted) {
            coarseAccessPermission.launchPermissionRequest()
        } else if (!fineAccessPermission.status.isGranted) {
            fineAccessPermission.launchPermissionRequest()
        } else {
            obtenerUbicacionActual(context, fusedLocationClient) { lat, lon ->
                locationText.value = if (lat != null && lon != null) {
                    "Latitud: $lat\nLongitud: $lon"
                } else {
                    "No se pudo obtener la ubicación."
                }
            }
        }
    }

    LaunchedEffect(
        coarseAccessPermission.status.isGranted,
        fineAccessPermission.status.isGranted
    ) {
        if (coarseAccessPermission.status.isGranted && fineAccessPermission.status.isGranted) {
            obtenerUbicacionActual(context, fusedLocationClient) { lat, lon ->
                locationText.value = if (lat != null && lon != null) {
                    "Latitud: $lat\nLongitud: $lon"
                } else {
                    "No se pudo obtener la ubicación."
                }
            }
        }
    }
// caca pedo culo pis
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = locationText.value)
        Button(
            onClick = { requestLocation() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Calcular ubicación")
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
fun obtenerUbicacionActual(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Double?, Double?) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    )
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationResult(location.latitude, location.longitude)
            } else {
                onLocationResult(null, null)
                Toast.makeText(context, "No se pudo obtener la ubicación (null).", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { exception ->
            onLocationResult(null, null)
            Toast.makeText(context, "Error al obtener ubicación: ${exception.message}", Toast.LENGTH_LONG).show()
        }
}


