package cl.unab.busca_comercio.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.unab.busca_comercio.R
import com.google.firebase.auth.FirebaseAuth

class WelcomeFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

        // Capitalizar primera letra por estética
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

        // 👉 Ir DIRECTO al buscador (SearchFragment)
        btnGoToHome.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        // Ver / gestionar "Mis comercios"
        btnGoToMyBusinesses.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                // Por seguridad, si no hay sesión, mando a login
                findNavController().navigate(R.id.loginFragment)
            } else {
                findNavController().navigate(R.id.myBusinessesFragment)
            }
        }

        // Cerrar sesión
        btnLogout.setOnClickListener {
            auth.signOut()
            // Volvemos al Home; allí se verá el botón "Iniciar sesión / Crear cuenta"
            findNavController().navigate(R.id.homeFragment)
        }
    }
}

