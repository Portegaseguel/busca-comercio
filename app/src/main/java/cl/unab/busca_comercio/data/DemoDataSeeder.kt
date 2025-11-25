package cl.unab.busca_comercio.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object DemoDataSeeder {

    private const val PREFS_NAME = "demo_data_prefs"
    private const val KEY_DEMO_LOADED = "demo_data_loaded"

    // UIDs reales de usuarios
    private const val OWNER_UID_PORTEGA = "AGZi6OsoM8fkDvwQ5AxSBR4Ntsi2" // portegaseguel@gmail.com
    private const val OWNER_UID_WALEED  = "xX2d8rI8L2aH65T3p1Q7gW8uMHz1" // waleed@gmail.com
    private const val OWNER_UID_JULIA   = "Tlgy0WcMxvSHKTEddaIF6SIwaaz1" // julia@gmail.com

    fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Para no duplicar datos cada vez que se abre la app
        if (prefs.getBoolean(KEY_DEMO_LOADED, false)) {
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val businessesRef = firestore.collection("businesses")

        // Comercios DEMO
        val demoBusinesses = listOf(
            DemoBusiness(
                name = "Juguetelandia",
                category = "Juguetería e Infantil",
                address = "Av. Las Salinas 123, Viña del Mar, Valparaíso, Región de Valparaíso",
                ownerId = OWNER_UID_PORTEGA,
                phone = "+56 9 1234 5678",
                hours = "Lunes a Sábado 10:00 - 19:30",
                email = "contacto@juguetelandia.cl",
                website = "https://www.juguetelandia.cl",
                instagram = "@juguetelandia_vina"
            ),
            DemoBusiness(
                name = "JW Sport Management",
                category = "Deporte",
                address = "Los Maquis 123, El Tabo, San Antonio, Región de Valparaíso",
                ownerId = OWNER_UID_WALEED,
                phone = "+56 9 9876 5432",
                hours = "Lunes a Viernes 09:00 - 18:00",
                email = "info@jwsport.cl",
                website = "https://www.jwsport.cl",
                instagram = "@jw_sport_management"
            ),
            DemoBusiness(
                name = "Café Aromas del Puerto",
                category = "Alimentación",
                address = "Calle Principal 45, Valparaíso, Valparaíso, Región de Valparaíso",
                ownerId = OWNER_UID_JULIA,
                phone = "+56 32 234 5678",
                hours = "Lunes a Domingo 08:30 - 20:00",
                email = "contacto@aromasdelpuerto.cl",
                website = "https://www.aromasdelpuerto.cl",
                instagram = "@cafe_aromas_del_puerto"
            ),
            DemoBusiness(
                name = "Panadería La Masa Feliz",
                category = "Alimentación",
                address = "Libertad 890, Viña del Mar, Valparaíso, Región de Valparaíso",
                ownerId = OWNER_UID_PORTEGA,
                phone = "+56 32 345 6789",
                hours = "Lunes a Sábado 07:30 - 14:30",
                email = "ventas@lamasafeliz.cl",
                website = "",
                instagram = "@panaderia_lamasafeliz"
            ),
            DemoBusiness(
                name = "Veterinaria Huellitas",
                category = "Mascotas",
                address = "Los Robles 321, Quilpué, Marga Marga, Región de Valparaíso",
                ownerId = OWNER_UID_WALEED,
                phone = "+56 32 456 7890",
                hours = "Lunes a Sábado 10:00 - 19:00",
                email = "contacto@huellitasvet.cl",
                website = "https://www.huellitasvet.cl",
                instagram = "@huellitas_vet"
            ),
            DemoBusiness(
                name = "Aula Creativa",
                category = "Educación",
                address = "Av. Libertad 456, El Tabo, Valparaíso, Región de Valparaíso",
                ownerId = OWNER_UID_JULIA,
                phone = "+56 9 5566 7788",
                hours = "Lunes a Viernes 16:00 - 20:00",
                email = "contacto@aulacreativa.cl",
                website = "https://www.aulacreativa.cl",
                instagram = "@aulacreativa_vina"
            )
        )

        val batch = firestore.batch()

        demoBusinesses.forEach { demo ->
            val docRef = businessesRef.document() // crea ID automático
            val data = mapOf(
                "id" to docRef.id,
                "name" to demo.name,
                "category" to demo.category,
                "address" to demo.address,
                "ownerId" to demo.ownerId,
                "phone" to demo.phone,
                "hours" to demo.hours,
                "email" to demo.email,
                "website" to demo.website,
                "instagram" to demo.instagram
            )
            batch.set(docRef, data)
        }

        batch.commit()
            .addOnSuccessListener {
                prefs.edit().putBoolean(KEY_DEMO_LOADED, true).apply()
                Log.d("DemoDataSeeder", "Datos demo cargados correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("DemoDataSeeder", "Error cargando datos demo: ${e.message}")
            }
    }
}

private data class DemoBusiness(
    val name: String,
    val category: String,
    val address: String,
    val ownerId: String,
    val phone: String,
    val hours: String,
    val email: String,
    val website: String,
    val instagram: String
)
