package com.example.papb_13

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.papb_13.databinding.ActivityAddDataBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddDataActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivityAddDataBinding
    private var updateId = ""
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val updateBudgetId = intent.getStringExtra("UPDATE_BUDGET_ID")
            if (updateBudgetId != null && updateBudgetId.isNotEmpty()) {
                updateId = updateBudgetId
                getBudgetById(updateBudgetId)
            } else {
                setEmptyField()
                btnAddUpdate.text = "Tambah Data"
            }

            btnAddUpdate.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val description = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val newBudget = Budget(nominal = nominal, description = description, date = date)
                addOrUpdateBudget(newBudget)
                finish()
            }
        }
    }

    private fun getBudgetById(budgetId: String) {
        budgetCollectionRef.document(budgetId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val budget = documentSnapshot.toObject(Budget::class.java)
                    if (budget != null) {
                        binding.edtNominal.setText(budget.nominal)
                        binding.edtDesc.setText(budget.description)
                        binding.edtDate.setText(budget.date)
                        binding.btnAddUpdate.text = "Update Data"
                    }
                }
            }
            .addOnFailureListener {
                Log.d("AddDataActivity", "Error getting budget by ID: ", it)
            }
    }

    private fun addOrUpdateBudget(budget: Budget) {
        if (updateId.isEmpty()) {
            addBudget(budget)
        } else {
            updateBudget(budget)
        }
    }

    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget)
            .addOnSuccessListener { documentReference ->
                val createdBudgetId = documentReference.id
                budget.id = createdBudgetId
                documentReference.set(budget)
                    .addOnFailureListener {
                        Log.d("AddDataActivity", "Error updating budget ID: ", it)
                    }
            }
            .addOnFailureListener {
                Log.d("AddDataActivity", "Error adding budget: ", it)
            }
    }

    private fun updateBudget(budget: Budget) {
        budget.id = updateId
        budgetCollectionRef.document(updateId).set(budget)
            .addOnFailureListener {
                Log.d("AddDataActivity", "Error updating budget: ", it)
            }
    }

    private fun setEmptyField() {
        with(binding) {
            edtNominal.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }
}
