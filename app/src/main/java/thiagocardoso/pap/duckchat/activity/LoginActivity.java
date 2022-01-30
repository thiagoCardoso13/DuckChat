package thiagocardoso.pap.duckchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import thiagocardoso.pap.duckchat.R;
import thiagocardoso.pap.duckchat.config.ConfiguracaoFirebase;
import thiagocardoso.pap.duckchat.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editaEmail);
        campoSenha = findViewById(R.id.editaSenha);

    }

    public void logarUsuario(Usuario usuario){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    abrirTelaPrincipal();
                }else{
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não está cadastrado!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void validarAutenticacaoUsuario(View view){

        //Recuperar textos dos campos
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        //Validar se e-mail e senha foram digitados
        if (!email.isEmpty()){  //verifica a senha
            if (!senha.isEmpty()) {  //verifica o email

                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setSenha(senha);

                logarUsuario(usuario);

            }else{
                Toast.makeText(LoginActivity.this, "Escreva a sua senha!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this, "Escreva o seu email!", Toast.LENGTH_SHORT).show();
        }

    }
    //Manter o usuário logado
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

    //abrir tela de cadastro
    public void abrirTelaCadastro(View view){
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(intent);
    }
    //abrir tela de cadastro
    public void abrirTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
