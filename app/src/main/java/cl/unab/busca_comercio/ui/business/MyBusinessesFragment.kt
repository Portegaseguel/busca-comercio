package cl.unab.busca_comercio.ui.business

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.repository.BusinessRepository
import cl.unab.busca_comercio.ui.home.BusinessAdapter
import com.google.firebase.auth.FirebaseAuth

class MyBusinessesFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val repository = BusinessRepository()

    private lateinit var adapter: BusinessAdapter
    private var myBusinesses: List<Business> = emptyList()

    private lateinit var tvEmpty: TextView
    private lateinit var rvMyBusinesses: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var btnAddBusiness: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_businesses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvMyBusinessesTitle)
        tvEmpty = view.findViewById(R.id.tvMyBusinessesEmpty)
        rvMyBusinesses = view.findViewById(R.id.rvMyBusinesses)
        progress = view.findViewById(R.id.progressMyBusinesses)
        btnAddBusiness = view.findViewById(R.id.btnAddBusiness)

        adapter = BusinessAdapter(
            onItemClick = { business ->
                // 👉 Editar comercio: vamos a CreateBusinessFragment con el id
                val bundle = Bundle().apply {
                    putString("businessId", business.id)
                }
                findNavController().navigate(R.id.createBusinessFragment, bundle)
            },
            onItemLongClick = { business ->
                // 👉 Eliminar comercio (ya lo teníamos)
                showDeleteDialog(business)
            }
        )

        rvMyBusinesses.layoutManager = LinearLayoutManager(requireContext())
        rvMyBusinesses.adapter = adapter

        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvTitle.text = "Mis comercios"
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Debes iniciar sesión para ver tus comercios."
            progress.visibility = View.GONE
            rvMyBusinesses.visibility = View.GONE
            btnAddBusiness.visibility = View.GONE
            return
        }

        // Botón para crear nuevo comercio
        btnAddBusiness.setOnClickListener {
            findNavController().navigate(R.id.createBusinessFragment)
        }

        loadMyBusinesses(currentUser.uid)
    }

    private fun loadMyBusinesses(ownerId: String) {
        progress.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        rvMyBusinesses.visibility = View.GONE

        repository.getBusinessesByOwner(ownerId) { list, error ->
            progress.visibility = View.GONE

            if (error != null) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error al cargar tus comercios: $error"
                rvMyBusinesses.visibility = View.GONE
                return@getBusinessesByOwner
            }

            myBusinesses = list

            if (myBusinesses.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Aún no has registrado comercios."
                rvMyBusinesses.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvMyBusinesses.visibility = View.VISIBLE
                adapter.updateData(myBusinesses)
            }
        }
    }

    private fun showDeleteDialog(business: Business) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar comercio")
            .setMessage("¿Seguro que quieres eliminar \"${business.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteBusiness(business)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBusiness(business: Business) {
        progress.visibility = View.VISIBLE

        repository.deleteBusiness(business.id) { success, error ->
            progress.visibility = View.GONE

            if (!success) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "No se pudo eliminar: ${error ?: "Error desconocido"}"
                return@deleteBusiness
            }

            val currentUser = auth.currentUser
            if (currentUser != null) {
                loadMyBusinesses(currentUser.uid)
            }
        }
    }
}


