package cl.unab.busca_comercio.data.repository

import cl.unab.busca_comercio.data.model.Business
import com.google.firebase.firestore.FirebaseFirestore

class BusinessRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("businesses")

    fun addBusiness(business: Business, callback: (Boolean, String?) -> Unit) {
        // Si no trae id -> creamos uno nuevo
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
}

