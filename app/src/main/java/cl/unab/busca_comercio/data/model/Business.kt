package cl.unab.busca_comercio.data.model

data class Business(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val ownerId: String = "",

    // Campos nuevos
    val phone: String = "",
    val hours: String = "",
    val email: String = "",
    val website: String = "",
    val instagram: String = "",

    // Valoraciones
    val avgRating: Double = 0.0,
    val ratingCount: Int = 0
)

