package cl.unab.busca_comercio.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.unab.busca_comercio.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGoToLogin = view.findViewById<Button>(R.id.btnGoToLogin)
        val btnGoToSearch = view.findViewById<Button>(R.id.btnGoToSearch)

        val currentUser = auth.currentUser

        btnGoToSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        if (currentUser == null) {
            btnGoToLogin.text = "Iniciar sesión / Crear cuenta"
            btnGoToLogin.setOnClickListener {
                findNavController().navigate(R.id.loginFragment)
            }
        } else {
            btnGoToLogin.text = "Ver mi cuenta"
            btnGoToLogin.setOnClickListener {
                findNavController().navigate(R.id.welcomeFragment)
            }
        }
    }
}
