package cl.unab.busca_comercio.ui.business

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Rating
import cl.unab.busca_comercio.data.repository.BusinessRepository
import cl.unab.busca_comercio.ui.common.RateDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BusinessDetailFragment : Fragment() {

    private val repository = BusinessRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private var businessId: String? = null

    private lateinit var tvName: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var tvInstagram: TextView
    private lateinit var progress: ProgressBar

    private lateinit var tvRating: TextView
    private lateinit var btnRate: Button

    // Lista de comentarios / valoraciones
    private lateinit var rvComments: RecyclerView
    private lateinit var commentsAdapter: RatingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        businessId = arguments?.getString("businessId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_business_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvDetailName)
        tvCategory = view.findViewById(R.id.tvDetailCategory)
        tvAddress = view.findViewById(R.id.tvDetailAddress)
        tvPhone = view.findViewById(R.id.tvDetailPhone)
        tvHours = view.findViewById(R.id.tvDetailHours)
        tvEmail = view.findViewById(R.id.tvDetailEmail)
        tvWebsite = view.findViewById(R.id.tvDetailWebsite)
        tvInstagram = view.findViewById(R.id.tvDetailInstagram)
        progress = view.findViewById(R.id.progressDetail)

        tvRating = view.findViewById(R.id.tvDetailRating)
        btnRate = view.findViewById(R.id.btnRateBusiness)

        rvComments = view.findViewById(R.id.rvComments)
        rvComments.layoutManager = LinearLayoutManager(requireContext())
        commentsAdapter = RatingAdapter(emptyList())
        rvComments.adapter = commentsAdapter

        val id = businessId
        if (id == null) {
            Toast.makeText(requireContext(), "No se encontró el comercio", Toast.LENGTH_LONG).show()
            return
        }

        loadBusiness(id)
    }

    private fun loadBusiness(id: String) {
        progress.visibility = View.VISIBLE

        repository.getBusinessById(id) { business, error ->
            val ctx = context ?: return@getBusinessById
            progress.visibility = View.GONE

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

            tvName.text = business.name.ifBlank { "Sin nombre" }
            tvCategory.text = business.category.ifBlank { "Sin rubro" }
            tvAddress.text = business.address.ifBlank { "Sin dirección" }

            tvPhone.text = if (business.phone.isBlank()) "No registrado" else business.phone
            tvHours.text = if (business.hours.isBlank()) "No registrado" else business.hours
            tvEmail.text = if (business.email.isBlank()) "No registrado" else business.email
            tvWebsite.text = if (business.website.isBlank()) "No registrado" else business.website
            tvInstagram.text = if (business.instagram.isBlank()) "No registrado" else business.instagram

            // Mostrar / ocultar boton de valoracion segun sesion
            val user = FirebaseAuth.getInstance().currentUser
            btnRate.visibility = if (user == null) View.GONE else View.VISIBLE

            btnRate.setOnClickListener {
                openRateDialog(business.id)
            }

            // Hacer clicables
            setupPhoneClick(business.phone)
            setupEmailClick(business.email)
            setupWebsiteClick(business.website)
            setupInstagramClick(business.instagram)

            // Cargar comentarios
            loadRatingsForBusiness(business.id)
        }
    }

    private fun loadRatingsForBusiness(businessId: String) {
        firestore.collection("ratings")
            .whereEqualTo("businessId", businessId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Rating::class.java) }
                commentsAdapter.updateData(list)

                // Promedio y cantidad desde valoraciones
                val ctx = context ?: return@addOnSuccessListener

                if (list.isEmpty()) {
                    tvRating.text = "Sin valoraciones"

                    // Actualiza el negocio en Firestore
                    firestore.collection("businesses")
                        .document(businessId)
                        .update(
                            mapOf(
                                "avgRating" to 0.0,
                                "ratingCount" to 0
                            )
                        )
                    return@addOnSuccessListener
                }

                val count = list.size
                val sumStars = list.sumOf { it.stars }
                val avg = sumStars.toDouble() / count.toDouble()

                tvRating.text = "⭐ %.1f (%d valoraciones)".format(avg, count)

                val updates = mapOf(
                    "avgRating" to avg,
                    "ratingCount" to count
                )

                firestore.collection("businesses")
                    .document(businessId)
                    .update(updates)
            }
            .addOnFailureListener { e ->
                val ctx = context ?: return@addOnFailureListener
                Toast.makeText(
                    ctx,
                    "Error al cargar comentarios: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openRateDialog(businessId: String) {
        val dialog = RateDialogFragment.newInstance(businessId)
        dialog.onRatingSaved = {
            val id = this.businessId
            if (isAdded && id != null) {
                loadBusiness(id)
            }
        }
        dialog.show(parentFragmentManager, "RateDialog")
    }

    // --- TELEFONO: llamar o WhatsApp ---
    private fun setupPhoneClick(phoneRaw: String) {
        if (phoneRaw.isBlank()) {
            tvPhone.setOnClickListener(null)
            tvPhone.isClickable = false
            return
        }

        tvPhone.isClickable = true
        tvPhone.setOnClickListener {
            val options = arrayOf("Llamar", "Enviar WhatsApp")

            AlertDialog.Builder(requireContext())
                .setTitle("Contactar por teléfono")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openDialer(phoneRaw)
                        1 -> openWhatsApp(phoneRaw)
                    }
                }
                .show()
        }
    }

    private fun openDialer(phoneRaw: String) {
        val phone = phoneRaw.trim()
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No se pudo abrir el marcador",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openWhatsApp(phoneRaw: String) {
        val cleaned = phoneRaw.filter { it.isDigit() || it == '+' }.trim()

        if (cleaned.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Número de teléfono no válido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val url = "https://wa.me/${cleaned.replace("+", "")}"
        openExternalLink(url)
    }

    // --- EMAIL: abrir correo ---
    private fun setupEmailClick(emailRaw: String) {
        if (emailRaw.isBlank()) {
            tvEmail.setOnClickListener(null)
            tvEmail.isClickable = false
            return
        }

        tvEmail.isClickable = true
        tvEmail.setOnClickListener {
            val email = emailRaw.trim()
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo abrir el correo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- SITIO WEB ---
    private fun setupWebsiteClick(websiteRaw: String) {
        if (websiteRaw.isBlank()) {
            tvWebsite.setOnClickListener(null)
            tvWebsite.isClickable = false
            return
        }

        tvWebsite.isClickable = true
        tvWebsite.setOnClickListener {
            val url = websiteRaw.trim()
            val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }

            openExternalLink(finalUrl)
        }
    }

    // --- INSTAGRAM ---
    private fun setupInstagramClick(instagramRaw: String) {
        if (instagramRaw.isBlank()) {
            tvInstagram.setOnClickListener(null)
            tvInstagram.isClickable = false
            return
        }

        tvInstagram.isClickable = true
        tvInstagram.setOnClickListener {
            val handle = instagramRaw.trim()

            val finalUrl = when {
                handle.startsWith("http://") || handle.startsWith("https://") ->
                    handle
                handle.startsWith("@") ->
                    "https://instagram.com/${handle.removePrefix("@")}"
                else ->
                    "https://instagram.com/$handle"
            }

            openExternalLink(finalUrl)
        }
    }

    // --- Utilidad para abrir enlaces externos ---
    private fun openExternalLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No se pudo abrir el enlace",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}



