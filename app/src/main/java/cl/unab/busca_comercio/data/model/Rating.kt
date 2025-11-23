package cl.unab.busca_comercio.data.model

data class Rating(
    val id: String = "",            // id del documento de rating
    val businessId: String = "",    // id del comercio al que pertenece
    val userId: String = "",        // uid de FirebaseAuth
    val stars: Int = 0,             // 1 a 5
    val comment: String? = null,    // comentario opcional
    val createdAt: Long = 0L        // timestamp en milisegundos (opcional)
)
