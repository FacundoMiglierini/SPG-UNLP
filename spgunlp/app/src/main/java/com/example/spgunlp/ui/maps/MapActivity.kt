package com.example.spgunlp.ui.maps

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.spgunlp.R
import com.example.spgunlp.databinding.ActivityMapBinding
import com.example.spgunlp.model.Poligono
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polygon.OnClickListener
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private val FINE_LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var mPoligonoViewModel: PoligonoViewModel
    private lateinit var geoPoints: ArrayList<GeoPoint>
    private lateinit var polygon: Polygon
    private lateinit var currentLocationFab: FloatingActionButton


    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        // get fab
        currentLocationFab = binding.currentLocationFab

        // init viewmodel
        mPoligonoViewModel = ViewModelProvider(this)[PoligonoViewModel::class.java]

        val ID_VISIT = intent.getLongExtra("ID_VISIT", 0)

        // map config
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        mMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)


        controller = mMap.controller

        val mapPoint = GeoPoint(-34.9214500, -57.9545300)
        controller.animateTo(mapPoint)
        controller.setZoom(13.0)

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)

        setCurrentLocation()
        if (isPermissionAllowed(android.Manifest.permission.ACCESS_FINE_LOCATION) || isPermissionAllowed(android.Manifest.permission.ACCESS_COARSE_LOCATION))
            setFab()


        if (!isPermissionAllowed(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION_REQUEST_CODE
            )
            setCurrentLocation()
        }

        mMap.overlays.add(mMyLocationOverlay)

        mMap.addMapListener(this)

        // inicializar con datos de la base de datos
        mPoligonoViewModel.getPoliByIdVisit(ID_VISIT).observe(this) { poligonos ->
            mMap.overlays.clear()
            poligonos.forEach {
                val geoPoints = it.coordenadas.replace("[", "").replace("]", "").split(",0.0,")
                val points = ArrayList<GeoPoint>()
                geoPoints.forEach {
                    val latlon = it.split(",")
                    points.add(GeoPoint(latlon[0].toDouble(), latlon[1].toDouble()))
                }
                polygon = Polygon()
                polygon.points = points
                polygon.fillPaint.color = Color.parseColor("#1EFFE70E") //set fill color
                polygon.outlinePaint.color = Color.parseColor("#028A0F") //set outline color
                polygon.setOnClickListener(onClickPolygon(it.id))
                mMap.overlays.add(polygon)
            }
            mMap.overlays.add(mMyLocationOverlay)
        }


        binding.drawBtn.setOnClickListener {
            binding.drawBtn.visibility = android.view.View.GONE
            binding.cancelBtn.visibility = android.view.View.VISIBLE
            binding.saveBtn.visibility = android.view.View.VISIBLE
            binding.crosshair.visibility = android.view.View.VISIBLE
            binding.markBtn.visibility = android.view.View.VISIBLE
            geoPoints = ArrayList()
            polygon = Polygon()

            polygon.fillPaint.color = Color.parseColor("#1EFFE70E") //set fill color
            polygon.outlinePaint.color = Color.parseColor("#028A0F") //set outline color
        }

        binding.cancelBtn.setOnClickListener {
            binding.drawBtn.visibility = android.view.View.VISIBLE
            binding.cancelBtn.visibility = android.view.View.GONE
            binding.saveBtn.visibility = android.view.View.GONE
            binding.crosshair.visibility = android.view.View.GONE
            binding.markBtn.visibility = android.view.View.GONE
            mMap.overlays.remove(polygon)
            mMap.invalidate()
        }

        binding.markBtn.setOnClickListener {
            mMap.overlays.remove(polygon)
            geoPoints.add(mMap.mapCenter as GeoPoint)
            polygon.points = geoPoints
            mMap.overlays.add(polygon)
            mMap.invalidate()
        }

        binding.saveBtn.setOnClickListener {
            binding.drawBtn.visibility = android.view.View.VISIBLE
            binding.cancelBtn.visibility = android.view.View.GONE
            binding.saveBtn.visibility = android.view.View.GONE
            binding.crosshair.visibility = android.view.View.GONE
            binding.markBtn.visibility = android.view.View.GONE
            // save map polygons
            val poligono = Poligono(0, ID_VISIT, polygon.title, geoPoints.toString())
            mPoligonoViewModel.addPoligono(poligono)
        }
        binding.currentLocationFab
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onGpsStatusChanged(event: Int) {
    }

    private fun onClickPolygon(id: Long): OnClickListener {
        return OnClickListener { polygon, _, _ ->
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Eliminar Poligono")
            builder.setMessage("Esta seguro que desea eliminar el poligono?")
            builder.setPositiveButton("Si") { _, _ ->
                mPoligonoViewModel.deletePoliById(id)
                mMap.overlays.remove(polygon)
                mMap.invalidate()
            }
            builder.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alert = builder.create()
            alert.show()
            true
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setCurrentLocation()
        if (isPermissionAllowed(android.Manifest.permission.ACCESS_FINE_LOCATION) || isPermissionAllowed(android.Manifest.permission.ACCESS_COARSE_LOCATION))
            setFab()
    }

    private fun setCurrentLocation() {
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }
    }

    private fun setFab() {
        currentLocationFab.visibility = android.view.View.VISIBLE
        currentLocationFab.setOnClickListener {
            if (isLocationEnabled()) {
                if (mMyLocationOverlay.mMyLocationProvider.lastKnownLocation != null) {
                    controller.setCenter(mMyLocationOverlay.myLocation)
                    controller.animateTo(mMyLocationOverlay.myLocation)
                } else
                    Toast.makeText(
                        this,
                        "Cargando ubicación...",
                        Toast.LENGTH_SHORT
                    ).show()
            } else
                Toast.makeText(
                    this,
                    "No tiene conexión para acceder a su ubicación",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun isPermissionAllowed(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}