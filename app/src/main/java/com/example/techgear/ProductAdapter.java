package com.example.techgear;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techgear.databinding.ItemProductBinding;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final ArrayList<Product> products;
    private final Consumer<Product> onEditClick;
    private final Consumer<Product> onDeleteClick;

    public ProductAdapter(ArrayList<Product> products, Consumer<Product> onEditClick, Consumer<Product> onDeleteClick) {
        this.products = products;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.tvName.setText(product.getName());
        holder.binding.tvPrice.setText(String.format("OMR %.2f", product.getPrice()));
        holder.binding.tvStock.setText(String.format("Stock: %d", product.getStock()));

        holder.binding.btnEdit.setOnClickListener(v -> onEditClick.accept(product));
        holder.binding.btnDelete.setOnClickListener(v -> onDeleteClick.accept(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Remove the empty override - use the inherited notifyDataSetChanged() method
    // or add proper implementation if needed
    public void updateProducts() {
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}