package cl.unab.busca_comercio.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Rating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RateDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_BUSINESS_ID = "businessId"

        fun newInstance(businessId: String): RateDialogFragment {
            val frag = RateDialogFragment()
            val args = Bundle()
            args.putString(ARG_BUSINESS_ID, businessId)
            frag.arguments = args
            return frag
        }
    }

    var onRatingSaved: (() -> Unit)? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val businessId = arguments?.getString(ARG_BUSINESS_ID)
        if (businessId.isNullOrBlank()) {
            dismiss()
            return super.onCreateDialog(savedInstanceState)
        }

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_rate_business, null)

        val rbStars: RatingBar = view.findViewById(R.id.rbStars)
        val etComment: EditText = view.findViewById(R.id.etComment)

        return AlertDialog.Builder(requireContext())
            .setTitle("Valorar comercio")
            .setView(view)
            .setPositiveButton("Enviar") { _, _ ->
                val user = auth.currentUser
                val ctx = context
                if (user == null || ctx == null) {
                    Toast.makeText(
                        ctx ?: return@setPositiveButton,
                        "Debes iniciar sesión para valorar",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val stars = rbStars.rating.toInt()
                if (stars <= 0) {
                    Toast.makeText(
                        ctx,
                        "Selecciona al menos 1 estrella",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val commentText = etComment.text.toString().trim()
                val now = System.currentTimeMillis()

                // Nombre visible del usuario (mismo criterio que WelcomeFragment)
                val rawName: String? = when {
                    user.displayName?.isNotBlank() == true -> user.displayName
                    user.email?.isNotBlank() == true       -> user.email!!.substringBefore("@")
                    else                                   -> null
                }

                val userName = rawName
                    ?.trim()
                    ?.lowercase()
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    ?: "Usuario"

                // 1) Guardar rating en "ratings"
                val ratingsRef = db.collection("ratings")
                val ratingDoc = ratingsRef.document()

                val rating = Rating(
                    id = ratingDoc.id,
                    businessId = businessId,
                    userId = user.uid,
                    userName = userName,
                    stars = stars,
                    comment = if (commentText.isBlank()) null else commentText,
                    createdAt = now
                )

                ratingDoc.set(rating)
                    .addOnSuccessListener {
                        val c = context ?: return@addOnSuccessListener
                        Toast.makeText(
                            c,
                            "¡Gracias por tu valoración!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Avisamos al fragment para que recargue negocio + comentarios
                        onRatingSaved?.invoke()
                    }
                    .addOnFailureListener { e ->
                        val c = context ?: return@addOnFailureListener
                        Toast.makeText(
                            c,
                            "Error al guardar valoración: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .create()
    }
}
