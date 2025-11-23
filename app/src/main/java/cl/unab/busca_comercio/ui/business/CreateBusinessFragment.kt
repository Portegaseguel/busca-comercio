package cl.unab.busca_comercio.ui.business

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.repository.BusinessRepository
import com.google.firebase.auth.FirebaseAuth

class CreateBusinessFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val repository = BusinessRepository()

    private lateinit var etName: EditText
    private lateinit var etCategory: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etHours: EditText
    private lateinit var etEmail: EditText
    private lateinit var etWebsite: EditText
    private lateinit var etInstagram: EditText
    private lateinit var btnSave: Button

    // null = creando, valor = editando
    private var editingBusinessId: String? = null

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

        etName = view.findViewById(R.id.etBusinessName)
        etCategory = view.findViewById(R.id.etBusinessCategory)
        etAddress = view.findViewById(R.id.etBusinessAddress)
        etPhone = view.findViewById(R.id.etBusinessPhone)
        etHours = view.findViewById(R.id.etBusinessHours)
        etEmail = view.findViewById(R.id.etBusinessEmail)
        etWebsite = view.findViewById(R.id.etBusinessWebsite)
        etInstagram = view.findViewById(R.id.etBusinessInstagram)
        btnSave = view.findViewById(R.id.btnSaveBusiness)

        val businessId = editingBusinessId
        if (businessId != null) {
            loadBusinessForEdit(businessId)
        }

        btnSave.setOnClickListener {
            saveBusiness()
        }
    }

    private fun loadBusinessForEdit(businessId: String) {
        repository.getBusinessById(businessId) { business, error ->
            if (error != null) {
                Toast.makeText(
                    requireContext(),
                    "Error al cargar comercio: $error",
                    Toast.LENGTH_LONG
                ).show()
                return@getBusinessById
            }

            if (business == null) {
                Toast.makeText(
                    requireContext(),
                    "El comercio ya no existe",
                    Toast.LENGTH_LONG
                ).show()
                return@getBusinessById
            }

            etName.setText(business.name)
            etCategory.setText(business.category)
            etAddress.setText(business.address)
            etPhone.setText(business.phone)
            etHours.setText(business.hours)
            etEmail.setText(business.email)
            etWebsite.setText(business.website)
            etInstagram.setText(business.instagram)
        }
    }

    private fun saveBusiness() {
        val name = etName.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val address = etAddress.text.toString().trim()
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

        val business = Business(
            id = editingBusinessId ?: "",
            name = name,
            category = category,
            address = address,
            ownerId = user.uid,
            phone = phone,
            hours = hours,
            email = email,
            website = website,
            instagram = instagram
        )

        repository.addBusiness(business) { success, error ->
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar: ${error ?: "desconocido"}",
                    Toast.LENGTH_LONG
                ).show()
                return@addBusiness
            }

            Toast.makeText(
                requireContext(),
                if (editingBusinessId == null) "Comercio creado" else "Comercio actualizado",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack()
        }
    }
}

