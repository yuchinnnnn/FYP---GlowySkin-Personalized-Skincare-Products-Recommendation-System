package com.example.personalizedskincareproductsrecommendation;

import static android.content.Intent.getIntent;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalizedskincareproductsrecommendation.HomeFragment;
import com.example.personalizedskincareproductsrecommendation.R;
import com.google.android.material.textfield.TextInputLayout;

// For fetching product item from firestore
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;



public class ProductsFragment extends Fragment {

    private TextInputLayout products_category;
    private AutoCompleteTextView hintText;
    private ImageView back, filter;
    private ArrayAdapter<String> adapter;
    private String userId;

    // For fetching product item from firestore
    private FirebaseFirestore db;
    private ListView productListView;
    private ArrayList<Product> productList;
    private ProductAdapter productAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        products_category = view.findViewById(R.id.dropdown);
        hintText = view.findViewById(R.id.hint_text);

        if (getArguments() != null) {
            userId = getArguments().getString("ARG_USER_ID");
            Log.d("ProductFragment", "User ID: " + userId); // Log userId
        } else {
            Log.e("ProductFragment", "Arguments are null!"); // Log if arguments are null
        }

        back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backHomepage();
            }
        });

        filter = view.findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryFilterDialog();
            }
        });

        db = FirebaseFirestore.getInstance();
        productListView = view.findViewById(R.id.product_list); // Update this to match your ListView ID
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList, userId);
        Log.d("UserId passed", "UserId: " + userId);
        productListView.setAdapter(productAdapter);

        // Fetch products from Firestore and populate the AutoCompleteTextView
        fetchProductsForAutocomplete();
        fetchProducts();

        return view;
    }

    private void fetchProductsForAutocomplete() {
        db.collection("skin_care_product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> productNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("name") && document.contains("brand")) {
                                String name = document.getString("name");
                                String brand = document.getString("brand");
                                productNames.add(name + " - " + brand);  // Combine name and brand into a single string
                            }
                        }
                        // Set the adapter for hintText with the product names
                        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, productNames);
                        hintText.setAdapter(adapter);
                        hintText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String item = adapterView.getItemAtPosition(i).toString();
                                filterProductsBySelectedItem(item);
                            }
                        });
                    } else {
                        Log.w(TAG, "Error getting product names for autocomplete.", task.getException());
                    }
                });
    }

    private void fetchProducts() {
        db.collection("skin_care_product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();  // Clear the list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (document.contains("name")) {
                                String brand = document.getString("brand");
                                String name = document.getString("name");
                                String imageUrl = document.getString("image_url");

                                product.setBrand(brand);
                                product.setName(name);
                                product.setImageUrl(imageUrl);

                                // Add the product to the list
                                productList.add(product);
                            } else {
                                Log.d(TAG, "Product does not exist.");
                            }
                        }

                        // Notify the adapter that the data has changed
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    // Filter the product list based on the selected item from AutoCompleteTextView
    private void filterProductsBySelectedItem(String selectedItem) {
        String[] parts = selectedItem.split(" - ");
        if (parts.length == 2) {
            String selectedName = parts[0];
            String selectedBrand = parts[1];

            ArrayList<Product> filteredList = new ArrayList<>();
            for (Product product : productList) {
                if (product.getName().equals(selectedName) && product.getBrand().equals(selectedBrand)) {
                    filteredList.add(product);
                }
            }

            // Update the adapter with the filtered list
            productAdapter.updateProductList(filteredList);
        }
    }

    private void showCategoryFilterDialog() {
        // Define product categories
        final String[] categories = {"Acne Treatment", "Anti-Aging Skin Care", "Brightening",
                "Dryness Control", "Oily Skin Care", "Pore Care", "Reduce Spots",
                "Sensitive Skin Care"};
        boolean[] selectedCategories = new boolean[categories.length];
        ArrayList<String> selectedCategoryList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Categories");

        // Set multi-choice items
        builder.setMultiChoiceItems(categories, selectedCategories, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    selectedCategoryList.add(categories[which]);
                } else {
                    selectedCategoryList.remove(categories[which]);
                }
            }

        });

        builder.setPositiveButton("Next", (dialog, which) -> {
            showTypeFilterDialog(selectedCategoryList);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showTypeFilterDialog(ArrayList<String> selectedCategories) {
        final String[] type = {"Toner", "Face Cleanser", "Facial Treatment", "Serum" ,"General Moisturizer",
                "Sunscreen" ,"Exfoliator", "Makeup Remover" ,"Day Moisturizer",
                "Eye Moisturizer" ,"Wet Mask", "Emulsion", "Night Moisturizer", "Sheet Mask"
                ,"Oil", "Essence" ,"Overnight Mask" ,"Eye Mask"};
        boolean[] selectedTypes = new boolean[type.length];
        ArrayList<String> selectedTypeList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Product Type");

        builder.setMultiChoiceItems(type, selectedTypes, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedTypeList.add(type[which]);
            } else {
                selectedTypeList.remove(type[which]);
            }
        });

        builder.setPositiveButton("Filter", (dialog, which) -> {
            filterProductsByCategoryAndType(selectedCategories, selectedTypeList);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void filterProductsByCategoryAndType(ArrayList<String> selectedCategories, ArrayList<String> selectedTypes) {
        ArrayList<Product> filteredList = new ArrayList<>();

        for (Product product : productList) {
            boolean matchesCategory = selectedCategories.isEmpty() || selectedCategories.contains(product.getFunction());
            boolean matchesType = selectedTypes.isEmpty() || selectedTypes.contains(product.getType());

            if (matchesCategory && matchesType) {
                filteredList.add(product);
            }
        }

        productAdapter.updateProductList(filteredList);
    }

    public void backHomepage() {
        HomeFragment homeFragment = HomeFragment.newInstance(userId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

}

