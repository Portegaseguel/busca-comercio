package cl.unab.busca_comercio.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.repository.BusinessRepository

class SearchFragment : Fragment() {

    private val repository = BusinessRepository()

    private lateinit var adapter: BusinessAdapter
    private var allBusinesses: List<Business> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rvBusinesses = view.findViewById<RecyclerView>(R.id.rvBusinesses)
        val tvEmpty = view.findViewById<TextView>(R.id.tvSearchEmpty)

        // Al tocar un comercio desde el buscador -> ir al detalle
        adapter = BusinessAdapter(
            onItemClick = { business ->
                val bundle = Bundle().apply {
                    putString("businessId", business.id)
                }
                findNavController().navigate(R.id.businessDetailFragment, bundle)
            }
        )

        rvBusinesses.layoutManager = LinearLayoutManager(requireContext())
        rvBusinesses.adapter = adapter

        // Cargar comercios desde Firestore
        repository.getAllBusinesses { list, error ->
            if (error != null) {
                tvEmpty.text = "Error al cargar comercios: $error"
                tvEmpty.visibility = View.VISIBLE
                return@getAllBusinesses
            }

            allBusinesses = list

            if (list.isEmpty()) {
                tvEmpty.text = "Aún no hay comercios registrados."
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
                adapter.updateData(list)
            }
        }

        // Filtro simple por texto
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                filterList(query, tvEmpty)
            }
        })
    }

    private fun filterList(query: String, tvEmpty: TextView) {
        if (query.isEmpty()) {
            adapter.updateData(allBusinesses)
            tvEmpty.visibility = if (allBusinesses.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        val filtered = allBusinesses.filter { b ->
            b.name.lowercase().contains(query) ||
                    b.category.lowercase().contains(query) ||
                    b.address.lowercase().contains(query)
        }

        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        if (filtered.isEmpty()) {
            tvEmpty.text = "No se encontraron comercios para \"$query\"."
        }
    }
}
