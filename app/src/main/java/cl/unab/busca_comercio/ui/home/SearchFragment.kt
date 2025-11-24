package cl.unab.busca_comercio.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.repository.BusinessRepository
import org.json.JSONObject
import androidx.navigation.fragment.findNavController


class SearchFragment : Fragment() {

    private val repository = BusinessRepository()

    private lateinit var etSearch: EditText
    private lateinit var actFilterCategory: AutoCompleteTextView
    private lateinit var actFilterProvince: AutoCompleteTextView
    private lateinit var rvBusinessList: RecyclerView
    private lateinit var progress: ProgressBar

    private lateinit var adapter: cl.unab.busca_comercio.ui.home.BusinessAdapter

    private var allBusinesses: List<Business> = emptyList()
    private var provinces: List<String> = emptyList()

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
        "Juguetería e Intantil",
        "Deporte",
        "Otros"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearch)
        actFilterCategory = view.findViewById(R.id.actFilterCategory)
        actFilterProvince = view.findViewById(R.id.actFilterProvince)
        rvBusinessList = view.findViewById(R.id.rvBusinessList)
        progress = view.findViewById(R.id.progressSearch)

        rvBusinessList.layoutManager = LinearLayoutManager(requireContext())
        adapter = cl.unab.busca_comercio.ui.home.BusinessAdapter(emptyList(),
            onItemClick = { business ->
                val bundle = Bundle().apply {
                    putString("businessId", business.id)
                }
                findNavController().navigate(R.id.businessDetailFragment, bundle)
            }
        )
        rvBusinessList.adapter = adapter

        // Cargar provincias desde el JSON local
        loadProvincesFromJson()

        setupCategoryFilter()
        setupProvinceFilter()
        setupSearchListener()

        loadBusinesses()
    }

    // --- Carga todos los comercios desde Firestore ---
    private fun loadBusinesses() {
        progress.visibility = View.VISIBLE

        repository.getAllBusinesses { list, error ->
            progress.visibility = View.GONE

            if (error != null) {
                Toast.makeText(
                    requireContext(),
                    "Error al cargar comercios: $error",
                    Toast.LENGTH_LONG
                ).show()
                return@getAllBusinesses
            }

            allBusinesses = list
            applyFilters()
        }
    }

    // --- Cargar Provincias desde json ---
    private fun loadProvincesFromJson() {
        try {
            val inputStream = resources.openRawResource(R.raw.regiones_provincias)
            val jsonText = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(jsonText)
            val setProvinces = linkedSetOf<String>()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val regionName = keys.next()
                val provincesArray = jsonObject.getJSONArray(regionName)
                for (i in 0 until provincesArray.length()) {
                    setProvinces.add(provincesArray.getString(i))
                }
            }

            provinces = setProvinces.toList().sorted()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error al cargar provincias: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            provinces = emptyList()
        }
    }

    // --- Filtro por Rubro ---
    private fun setupCategoryFilter() {
        val items = listOf("Todos") + businessCategories
        val adapterCat = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )
        actFilterCategory.setAdapter(adapterCat)

        actFilterCategory.setOnClickListener {
            actFilterCategory.showDropDown()
        }

        actFilterCategory.setOnItemClickListener { _, _, _, _ ->
            applyFilters()
        }
    }

    // --- Filtro por Provincia ---
    private fun setupProvinceFilter() {
        val items = listOf("Todas") + provinces

        val adapterProv = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )
        actFilterProvince.setAdapter(adapterProv)

        actFilterProvince.setOnClickListener {
            actFilterProvince.showDropDown()
        }

        actFilterProvince.setOnItemClickListener { _, _, _, _ ->
            applyFilters()
        }
    }

    // --- Texto de busqueda ---
    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }
        })
    }

    // --- Aplicar busqueda y filtros sobre la lista completa ---
    private fun applyFilters() {
        val query = etSearch.text.toString().trim().lowercase()
        val selectedCategory = actFilterCategory.text.toString().trim()
        val selectedProvince = actFilterProvince.text.toString().trim()

        var result = allBusinesses

        // Filtro por texto (nombre / rubro / direccion)
        if (query.isNotEmpty()) {
            result = result.filter { business ->
                business.name.contains(query, ignoreCase = true) ||
                        business.category.contains(query, ignoreCase = true) ||
                        business.address.contains(query, ignoreCase = true)
            }
        }

        // Filtro por rubro
        if (selectedCategory.isNotEmpty() && selectedCategory != "Todos") {
            result = result.filter { business ->
                business.category.equals(selectedCategory, ignoreCase = true)
            }
        }

        // Filtro por provincia
        if (selectedProvince.isNotEmpty() && selectedProvince != "Todas") {
            result = result.filter { business ->
                business.address.contains(selectedProvince, ignoreCase = true)
            }
        }

        adapter.updateData(result)
    }
}

