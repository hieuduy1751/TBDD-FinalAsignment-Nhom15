package com.example.finalassignment;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalassignment.adapter.FolderAdapter;
import com.example.finalassignment.entity.Folder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class todo extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Context context;
    List<Folder> listFolder = new ArrayList<Folder>();
    private FirebaseUser user;
    private ListView listFolderView;
    private GoogleSignInAccount account;

    @Override
    protected void onStart() {
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_todo);

        TextView txtName = (TextView) findViewById(R.id.txtName);
        ImageButton btnLogout = (ImageButton) findViewById(R.id.btnLogoutL);
        ImageButton btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        EditText txtFolderAdd = (EditText) findViewById(R.id.txtFolderAdd);
        Button btnToday = (Button) findViewById(R.id.btnToday);
        Button btnImportant = (Button) findViewById(R.id.btnImportant);
        Button btnAll = (Button) findViewById(R.id.btnAll);

        listFolderView = (ListView) findViewById(R.id.listViewFolder);

        if(mAuth.getCurrentUser() != null) {
            user = mAuth.getCurrentUser();
            txtName.setText(user.getDisplayName());
            getFolders();
            FolderAdapter adapter = new FolderAdapter(this, R.layout.folder_button, listFolder);
            listFolderView.setAdapter(adapter);
        }else if(account != null){
            txtName.setText(account.getDisplayName());
            Log.d("Display nam", account.getDisplayName());
            getFolders();
            FolderAdapter adapter = new FolderAdapter(this, R.layout.folder_button, listFolder);
            listFolderView.setAdapter(adapter);
        }
        else {
            openWelcome();
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                openWelcome();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo get folder name and create collection
                db.collection("folders")
                        .add(new Folder(txtFolderAdd.getText().toString(), user.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Them thu muc", "Them thanh cong");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Them thu muc", "Them that bai", e);
                            }
                        });
            }
        });

        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail("Today");
            }
        });

        btnImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail("Important");
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail("All");
            }
        });

    }

    public void openDetail(String tag) {
        Intent intent = new Intent(this, detail.class);
        intent.putExtra("tag", tag);
        startActivity(intent);
    }

    public void openWelcome() {
        Intent intent = new Intent(this, welcome.class);
        startActivity(intent);
    }

    public void getFolders() {
        if (user != null) {
                db.collection("folders")
                    .whereEqualTo("ownerId", user.getUid())
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("Load data", "Listen failed.", error);
                                return;
                            }

                            List<Folder> listFolderLoad = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.get("name") != null) {
                                    listFolderLoad.add(new Folder(doc.getId(), doc.getString("name"), doc.getString("ownerId"), doc.getLong("createdAt"), doc.getLong("updatedAt")));
                                }
                            }
                            listFolder = listFolderLoad;
                            Log.d("Load data", "thanh cong");
                            FolderAdapter adapter = new FolderAdapter(context, R.layout.folder_button, listFolder);
                            listFolderView.setAdapter(adapter);
                        }
                    });
        }
    }
}
