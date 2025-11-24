package cl.unab.busca_comercio.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.unab.busca_comercio.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcomeMessage = view.findViewById<TextView>(R.id.tvWelcomeMessage)

        // Usuario actual
        val currentUser = auth.currentUser

        // 1) Usamos displayName si existe
        // 2) Si no, la parte antes del @ del correo
        val rawName: String? = when {
            currentUser?.displayName?.isNotBlank() == true -> currentUser.displayName
            currentUser?.email?.isNotBlank() == true -> currentUser.email!!.substringBefore("@")
            else -> null
        }

        val name = rawName
            ?.trim()
            ?.lowercase()
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        tvWelcomeMessage.text = if (name.isNullOrBlank()) {
            "¡Bienvenid@!"
        } else {
            "¡Bienvenid@ $name!"
        }

        val btnGoToHome = view.findViewById<Button>(R.id.btnGoToHome)
        val btnGoToMyBusinesses = view.findViewById<Button>(R.id.btnGoToMyBusinesses)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = view.findViewById<Button?>(R.id.btnDeleteAccount)

        // Ir al buscador
        btnGoToHome.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        // Ver / gestionar "Mis comercios"
        btnGoToMyBusinesses.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                findNavController().navigate(R.id.loginFragment)
            } else {
                findNavController().navigate(R.id.myBusinessesFragment)
            }
        }

        // Cerrar sesión
        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.homeFragment)
        }

        // Eliminar cuenta
        btnDeleteAccount?.setOnClickListener {
            confirmDeleteAccount()
        }
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage(
                "Esta acción eliminará tu cuenta y tus datos básicos " +
                        "(comercios creados y valoraciones). No se puede deshacer.\n\n" +
                        "¿Quieres continuar?"
            )
            .setPositiveButton("Eliminar") { _, _ ->
                deleteAccountAndData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccountAndData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        // 1) Borrar comercios del usuario
        firestore.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }

        // 2) Borrar valoraciones del usuario
        firestore.collection("ratings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }

        // 3) Eliminar cuenta de Firebase
        user.delete()
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Tu cuenta ha sido eliminada",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.homeFragment)
            }
            .addOnFailureListener { e ->
                val msg = if (e is FirebaseAuthRecentLoginRequiredException) {
                    "Por seguridad, vuelve a iniciar sesión y luego intenta eliminar tu cuenta de nuevo."
                } else {
                    "No se pudo eliminar la cuenta: ${e.message}"
                }

                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
    }
}

