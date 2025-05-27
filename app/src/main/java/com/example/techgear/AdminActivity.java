package com.example.techgear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;



import com.example.techgear.databinding.ActivityAdminBinding;
import com.example.techgear.databinding.DialogAddProductBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private FirebaseFirestore db;
    private com.example.techgear.ProductAdapter adapter;
    private ArrayList<Product> products;
    private DialogAddProductBinding dialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = FirebaseFirestore.getInstance();
        products = new ArrayList<>();
        adapter = new ProductAdapter(products, this::showEditDialog, this::deleteProduct);

        binding.rvRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecords.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> showAddDialog());
        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_reset) {
            resetForm();
            return true;
        } else if (id == R.id.action_close) {
            showCloseConfirmationDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void resetForm() {
        if (dialogBinding != null) {
            dialogBinding.etName.setText("");
            dialogBinding.etPrice.setText("");
            dialogBinding.etStock.setText("");
            Toast.makeText(this, "Form reset", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCloseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Close App")
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    products.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        products.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading products: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void showAddDialog() {
        dialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (dialogInterface, which) -> {
                    String name = dialogBinding.etName.getText().toString().trim();
                    String price = dialogBinding.etPrice.getText().toString().trim();
                    String stock = dialogBinding.etStock.getText().toString().trim();

                    if (name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> product = new HashMap<>();
                    product.put("name", name);
                    product.put("price", Double.parseDouble(price));
                    product.put("stock", Integer.parseInt(stock));

                    db.collection("products")
                            .add(product)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialogBinding.btnReset.setOnClickListener(v -> resetForm());
        dialog.show();
    }

    private void showEditDialog(Product product) {
        dialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(this));
        dialogBinding.etName.setText(product.getName());
        dialogBinding.etPrice.setText(String.valueOf(product.getPrice()));
        dialogBinding.etStock.setText(String.valueOf(product.getStock()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Update", (dialogInterface, which) -> {
                    String name = dialogBinding.etName.getText().toString().trim();
                    String price = dialogBinding.etPrice.getText().toString().trim();
                    String stock = dialogBinding.etStock.getText().toString().trim();

                    if (name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", name);
                    updates.put("price", Double.parseDouble(price));
                    updates.put("stock", Integer.parseInt(stock));

                    db.collection("products")
                            .document(product.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialogBinding.btnReset.setOnClickListener(v -> resetForm());
        dialog.show();
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("products")
                            .document(product.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}