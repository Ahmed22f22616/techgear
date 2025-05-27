package com.example.techgear;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techgear.databinding.ActivityTransactionBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {
    private ActivityTransactionBinding binding;
    private FirebaseFirestore db;
    private ArrayList<Product> products;
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        products = new ArrayList<>();

        loadProducts();
        setupListeners();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    products.clear();
                    ArrayList<String> productNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        products.add(product);
                        productNames.add(product.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, productNames);
                    binding.actvProduct.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading products: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.actvProduct.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedProduct = products.stream()
                    .filter(p -> p.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (selectedProduct != null) {
                binding.tvPrice.setText(String.format("OMR %.2f", selectedProduct.getPrice()));
                calculateTotal();
            }
        });

        binding.etQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                calculateTotal();
            }
        });

        binding.btnCalculate.setOnClickListener(v -> calculateTotal());

        binding.btnConfirm.setOnClickListener(v -> confirmTransaction());
    }

    private void calculateTotal() {
        if (selectedProduct != null && !binding.etQuantity.getText().toString().isEmpty()) {
            int quantity = Integer.parseInt(binding.etQuantity.getText().toString());
            double total = selectedProduct.getPrice() * quantity;
            binding.tvTotal.setText(String.format("OMR %.2f", total));
        }
    }

    private void confirmTransaction() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityStr = binding.etQuantity.getText().toString();
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity > selectedProduct.getStock()) {
            Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create transaction record
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("productId", selectedProduct.getId());
        transaction.put("productName", selectedProduct.getName());
        transaction.put("quantity", quantity);
        transaction.put("totalAmount", selectedProduct.getPrice() * quantity);
        transaction.put("timestamp", System.currentTimeMillis());

        // Update stock
        Map<String, Object> updates = new HashMap<>();
        updates.put("stock", selectedProduct.getStock() - quantity);

        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    db.collection("products")
                            .document(selectedProduct.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Transaction completed successfully", Toast.LENGTH_SHORT).show();
                                resetForm();
                                loadProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error updating stock: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating transaction: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void resetForm() {
        binding.actvProduct.setText("");
        binding.etQuantity.setText("");
        binding.tvPrice.setText("");
        binding.tvTotal.setText("");
        selectedProduct = null;
    }
} 