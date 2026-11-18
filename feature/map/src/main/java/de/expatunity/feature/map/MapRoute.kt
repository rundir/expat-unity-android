package de.expatunity.feature.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapRoute(onOpenComplex: (String) -> Unit, vm: MapViewModel = hiltViewModel()) {
    val items by vm.markers.collectAsStateWithLifecycle()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.16, 10.45), 5.5f)
    }
    GoogleMap(Modifier.fillMaxSize(), cameraPositionState = camera) {
        items.forEach { c ->
            val loc = c.location ?: return@forEach
            key(c.id) {
                Marker(
                    state = rememberUpdatedMarkerState(position = LatLng(loc.lat, loc.lng)),
                    title = c.name,
                    snippet = c.city,
                    onInfoWindowClick = { onOpenComplex(c.id) },
                )
            }
        }
    }
}