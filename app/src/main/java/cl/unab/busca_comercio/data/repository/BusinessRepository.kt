package cl.unab.busca_comercio.data.repository

import cl.unab.busca_comercio.data.model.Business
import cl.unab.busca_comercio.data.model.Rating
import com.google.firebase.firestore.FirebaseFirestore

class BusinessRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("businesses")
    private val ratingsCollection = db.collection("ratings")

    fun addBusiness(business: Business, callback: (Boolean, String?) -> Unit) {
        val docRef = if (business.id.isBlank()) {
            collection.document()
        } else {
            collection.document(business.id)
        }

        val dataToSave = business.copy(id = docRef.id)

        docRef.set(dataToSave)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    fun getAllBusinesses(callback: (List<Business>, String?) -> Unit) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Business::class.java) }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(emptyList(), e.message)
            }
    }

    fun getBusinessesByOwner(ownerId: String, callback: (List<Business>, String?) -> Unit) {
        collection
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Business::class.java) }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(emptyList(), e.message)
            }
    }

    fun getBusinessById(id: String, callback: (Business?, String?) -> Unit) {
        collection.document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    callback(doc.toObject(Business::class.java), null)
                } else {
                    callback(null, "No existe el comercio")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.message)
            }
    }

    fun deleteBusiness(id: String, callback: (Boolean, String?) -> Unit) {
        collection.document(id)
            .delete()
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    // ---------------- VALORACIONES ----------------

    fun addRating(
        businessId: String,
        userId: String,
        stars: Int,
        comment: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val ratingDoc = ratingsCollection.document()

        val rating = Rating(
            id = ratingDoc.id,
            businessId = businessId,
            userId = userId,
            stars = stars,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            val businessRef = collection.document(businessId)
            val businessSnap = transaction.get(businessRef)

            if (!businessSnap.exists()) {
                throw IllegalStateException("El comercio no existe")
            }

            val currentBusiness = businessSnap.toObject(Business::class.java)
                ?: throw IllegalStateException("Error al leer comercio")

            val oldCount = currentBusiness.ratingCount
            val oldAvg = currentBusiness.avgRating

            val newCount = oldCount + 1
            val newAvg = if (oldCount == 0) {
                stars.toDouble()
            } else {
                (oldAvg * oldCount + stars) / newCount
            }

            // Guardar el rating y actualizar el negocio
            transaction.set(ratingDoc, rating)
            transaction.update(
                businessRef,
                mapOf(
                    "ratingCount" to newCount,
                    "avgRating" to newAvg
                )
            )

            null
        }.addOnSuccessListener {
            callback(true, null)
        }.addOnFailureListener { e ->
            callback(false, e.message)
        }
    }
}

