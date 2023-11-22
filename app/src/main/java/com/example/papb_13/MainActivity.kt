package com.example.papb_13

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.papb_13.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivityMainBinding
    private var updateId = ""
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }
    private val ADD_DATA_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnOpenAddPage.setOnClickListener {
                val intent = Intent(this@MainActivity, AddDataActivity::class.java)
                startActivityForResult(intent, ADD_DATA_REQUEST_CODE)
            }

            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val item = listView.adapter.getItem(position) as Budget
                updateId = item.id
                val intent = Intent(this@MainActivity, AddDataActivity::class.java)
                intent.putExtra("UPDATE_BUDGET_ID", updateId)
                startActivityForResult(intent, ADD_DATA_REQUEST_CODE)
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listView.adapter.getItem(position) as Budget
                deleteBudget(item)
                true
            }
        }
        observeBudgets()
        getAllBudgets()
    }

    private fun getAllBudgets() {
        observeBudgetChanges()
    }

    private fun observeBudgets() {
        budgetListLiveData.observe(this) { budgets ->
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                budgets.toMutableList()
            )
            binding.listView.adapter = adapter
        }
    }

    private fun observeBudgetChanges() {
        budgetCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val budgets = snapshots?.toObjects(Budget::class.java)
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget)
            .addOnSuccessListener { documentReference ->
                val createdBudgetId = documentReference.id
                budget.id = createdBudgetId
                documentReference.set(budget)
                    .addOnFailureListener {
                        Log.d("MainActivity", "Error updating budget ID: ", it)
                    }
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Error adding budget: ", it)
            }
    }

    private fun updateBudget(budget: Budget) {
        budget.id = updateId
        budgetCollectionRef.document(updateId).set(budget)
            .addOnFailureListener {
                Log.d("MainActivity", "Error updating budget: ", it)
            }
    }

    private fun deleteBudget(budget: Budget) {
        if (budget.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: budget ID is empty!")
            return
        }
        budgetCollectionRef.document(budget.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ", it)
            }
    }
}
