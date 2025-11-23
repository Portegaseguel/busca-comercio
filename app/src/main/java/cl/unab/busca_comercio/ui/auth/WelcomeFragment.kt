package cl.unab.busca_comercio.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        val btnGoToHome = view.findViewById<Button>(R.id.btnGoToHome)
        val btnGoToMyBusinesses = view.findViewById<Button>(R.id.btnGoToMyBusinesses)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // 👉 Ir DIRECTO al buscador (SearchFragment)
        btnGoToHome.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        // Ver / gestionar "Mis comercios"
        btnGoToMyBusinesses.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
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
