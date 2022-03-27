package thiagocardoso.pap.duckchat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import thiagocardoso.pap.duckchat.R;
import thiagocardoso.pap.duckchat.adapter.MensagensAdapter;
import thiagocardoso.pap.duckchat.config.ConfiguracaoFirebase;
import thiagocardoso.pap.duckchat.databinding.ActivityChatttBinding;
import thiagocardoso.pap.duckchat.helper.Base64Custom;
import thiagocardoso.pap.duckchat.helper.UsuarioFirebase;
import thiagocardoso.pap.duckchat.model.Mensagem;
import thiagocardoso.pap.duckchat.model.Usuario;

public class ChatttActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityChatttBinding binding;
    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private ImageView imageCamera;

    private Usuario usuarioDestinatario;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    //identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    //mensagens
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private static final int SELECAO_GALERIA = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatttBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configurações iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageViewFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);

        //recuperar dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //recuperar dados do usuario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText( usuarioDestinatario.getNome());

            String foto = usuarioDestinatario.getFoto();
            if (foto != null){
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatttActivity.this)
                        .load(url)
                        .into(circleImageViewFoto);
            }else{
                circleImageViewFoto.setImageResource(R.drawable.padrao1);
            }

            //recuperar dados do usuario destinatario
            idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());

        }

        //configuração adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //configuração recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);



        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        recuperarMensagens();

        //evento de clique na galeria
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;
            try {

                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                if (imagem != null) {

                    //Recuperar dados de imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    /*Criar nome da imagem (classe UUID serve para criar identificadores únicos e que não se repetem,
                    ela cria identificadores baseados na data, hora, minuto e segundo)*/
                    String nomeImagem = UUID.randomUUID().toString();


                    //configurar referencias do firebase
                    final StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatttActivity.this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show();
                        }

                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = task.getResult().toString();

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario(idUsuarioRemetente);
                                    //mensagem.setMensagem(nomeImagem);
                                    mensagem.setImagem(url);

                                    //salvar mensagem remetente
                                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                    //salvar mensagem destinatario
                                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                                    Toast.makeText(ChatttActivity.this, "Sucesso ao fazer upload da imagem.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //em vez de dar reset à activity anterior, dá finish
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()) {

            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario(idUsuarioRemetente);
            mensagem.setMensagem(textoMensagem);

            //Salvar mensagem para remetente
            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

            //Salvar mensagem para o destinatario
            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

        }else{
            Toast.makeText(ChatttActivity.this,"Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
        }

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //limpar texto
        editMensagem.setText("");

    }


    @Override
    protected void onStart() {
        super.onStart();
        //recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
       // mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}