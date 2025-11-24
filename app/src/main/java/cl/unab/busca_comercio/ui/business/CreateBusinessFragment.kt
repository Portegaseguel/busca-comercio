package cl.unab.busca_comercio.ui.business

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.repository.BusinessRepository
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class CreateBusinessFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val repository = BusinessRepository()

    // Inputs
    private lateinit var etName: EditText
    private lateinit var actCategory: AutoCompleteTextView
    private lateinit var etAddressStreet: EditText
    private lateinit var actRegion: AutoCompleteTextView
    private lateinit var actProvince: AutoCompleteTextView
    private lateinit var etCommune: EditText
    private lateinit var etPhone: EditText
    private lateinit var etHours: EditText
    private lateinit var etEmail: EditText
    private lateinit var etWebsite: EditText
    private lateinit var etInstagram: EditText
    private lateinit var btnSave: Button


    private var editingBusinessId: String? = null

    // Región y Provincias cargadas desde JSON
    private var regionProvinces: Map<String, List<String>> = emptyMap()

    private val businessCategories = listOf(
        "Alimentación",
        "Salud y belleza",
        "Ropa y accesorios",
        "Servicios profesionales",
        "Mascotas",
        "Tecnología",
        "Educación",
        "Construcción",
        "Bienes Raíces",
        "Entretenimiento",
        "Hoteleria y Turismo",
        "Deporte",
        "Juguetería e Infantil",
        "Otros"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingBusinessId = arguments?.getString("businessId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_business, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a vistas
        etName = view.findViewById(R.id.etBusinessName)
        actCategory = view.findViewById(R.id.etBusinessCategory)
        etAddressStreet = view.findViewById(R.id.etBusinessAddress)
        actRegion = view.findViewById(R.id.etBusinessRegion)
        actProvince = view.findViewById(R.id.etBusinessProvince)
        etCommune = view.findViewById(R.id.etBusinessCommune)
        etPhone = view.findViewById(R.id.etBusinessPhone)
        etHours = view.findViewById(R.id.etBusinessHours)
        etEmail = view.findViewById(R.id.etBusinessEmail)
        etWebsite = view.findViewById(R.id.etBusinessWebsite)
        etInstagram = view.findViewById(R.id.etBusinessInstagram)
        btnSave = view.findViewById(R.id.btnSaveBusiness)

        // Cargar datos desde JSON local
        loadRegionProvincesFromJson()

        setupCategoryDropdown()
        setupRegionProvinceDropdowns()
        setupHoursPicker()

        val businessId = editingBusinessId
        if (businessId != null) {
            loadBusinessForEdit(businessId)
        }

        btnSave.setOnClickListener {
            saveBusiness()
        }
    }

    // --- Cargar JSON de regiones y provincias  ---
    private fun loadRegionProvincesFromJson() {
        try {
            val inputStream = resources.openRawResource(R.raw.regiones_provincias)
            val jsonText = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(jsonText)
            val map = mutableMapOf<String, List<String>>()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val regionName = keys.next()
                val provincesJsonArray = jsonObject.getJSONArray(regionName)
                val provinces = mutableListOf<String>()
                for (i in 0 until provincesJsonArray.length()) {
                    provinces.add(provincesJsonArray.getString(i))
                }
                map[regionName] = provinces
            }

            regionProvinces = map
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error al cargar regiones: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            regionProvinces = emptyMap()
        }
    }

    // --- Rubro ---
    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            businessCategories
        )
        actCategory.setAdapter(adapter)

        actCategory.setOnClickListener {
            actCategory.showDropDown()
        }
    }

    // --- Región / Provincia ---
    private fun setupRegionProvinceDropdowns() {
        if (regionProvinces.isEmpty()) {
            // Si falló la carga del JSON, no hacemos nada más
            return
        }

        val regions = regionProvinces.keys.toList()

        val regionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            regions
        )
        actRegion.setAdapter(regionAdapter)

        actRegion.setOnClickListener {
            actRegion.showDropDown()
        }

        actRegion.setOnItemClickListener { _, _, position, _ ->
            val selectedRegion = regions[position]
            val provinces = regionProvinces[selectedRegion].orEmpty()

            val provinceAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                provinces
            )
            actProvince.setAdapter(provinceAdapter)
            actProvince.setText("")
        }

        actProvince.setOnClickListener {
            actProvince.showDropDown()
        }
    }

    // --- Horario ---
    private fun setupHoursPicker() {
        etHours.isFocusable = false
        etHours.isClickable = true
        etHours.setOnClickListener {
            showHoursDialog()
        }
    }

    private fun showHoursDialog() {
        val days = arrayOf(
            "Lunes", "Martes", "Miércoles", "Jueves",
            "Viernes", "Sábado", "Domingo"
        )
        val checked = BooleanArray(days.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona días de atención")
            .setMultiChoiceItems(days, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Continuar") { _, _ ->
                val selectedDays = days
                    .mapIndexedNotNull { index, day ->
                        if (checked[index]) day else null
                    }

                if (selectedDays.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Selecciona al menos un día",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                pickTimeRange(selectedDays)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun pickTimeRange(selectedDays: List<String>) {
        var fromHour = 9
        var fromMinute = 0

        val fromPicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                fromHour = hourOfDay
                fromMinute = minute

                val toPicker = TimePickerDialog(
                    requireContext(),
                    { _, toHour, toMinute ->
                        val daysStr = selectedDays.joinToString(", ")
                        val fromStr = String.format("%02d:%02d", fromHour, fromMinute)
                        val toStr = String.format("%02d:%02d", toHour, toMinute)
                        etHours.setText("$daysStr $fromStr - $toStr")
                    },
                    fromHour,
                    fromMinute,
                    true
                )
                toPicker.setTitle("Selecciona hora de término")
                toPicker.show()
            },
            fromHour,
            fromMinute,
            true
        )

        fromPicker.setTitle("Selecciona hora de inicio")
        fromPicker.show()
    }

    private fun loadBusinessForEdit(businessId: String) {
        repository.getBusinessById(businessId) { business, error ->
            val ctx = context ?: return@getBusinessById

            if (error != null) {
                Toast.makeText(
                    ctx,
                    "Error al cargar comercio: $error",
                    Toast.LENGTH_LONG
                ).show()
                return@getBusinessById
            }

            if (business == null) {
                Toast.makeText(
                    ctx,
                    "El comercio ya no existe",
                    Toast.LENGTH_LONG
                ).show()
                return@getBusinessById
            }

            etName.setText(business.name)
            actCategory.setText(business.category, false)

            // Dirección completa
            etAddressStreet.setText(business.address)

            etPhone.setText(business.phone)
            etHours.setText(business.hours)
            etEmail.setText(business.email)
            etWebsite.setText(business.website)
            etInstagram.setText(business.instagram)
        }
    }

    private fun saveBusiness() {
        val name = etName.text.toString().trim()
        val category = actCategory.text.toString().trim()
        val street = etAddressStreet.text.toString().trim()
        val region = actRegion.text.toString().trim()
        val province = actProvince.text.toString().trim()
        val commune = etCommune.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val hours = etHours.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val website = etWebsite.text.toString().trim()
        val instagram = etInstagram.text.toString().trim()

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Nombre y rubro son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Debes iniciar sesión para guardar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val addressParts = mutableListOf<String>()
        if (street.isNotEmpty()) addressParts.add(street)
        if (commune.isNotEmpty()) addressParts.add(commune)
        if (province.isNotEmpty()) addressParts.add(province)
        if (region.isNotEmpty()) addressParts.add(region)
        val fullAddress = addressParts.joinToString(", ")

        val business = Business(
            id = editingBusinessId ?: "",
            name = name,
            category = category,
            address = fullAddress,
            ownerId = user.uid,
            phone = phone,
            hours = hours,
            email = email,
            website = website,
            instagram = instagram
        )

        repository.addBusiness(business) { success, error ->
            val ctx = context ?: return@addBusiness

            if (!success) {
                Toast.makeText(
                    ctx,
                    "Error al guardar: ${error ?: "desconocido"}",
                    Toast.LENGTH_LONG
                ).show()
                return@addBusiness
            }

            Toast.makeText(
                ctx,
                if (editingBusinessId == null) "Comercio creado" else "Comercio actualizado",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack()
        }
    }
}

