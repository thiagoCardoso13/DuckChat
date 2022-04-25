package thiagocardoso.pap.duckchat.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import thiagocardoso.pap.duckchat.R;
import thiagocardoso.pap.duckchat.helper.UsuarioFirebase;
import thiagocardoso.pap.duckchat.model.Mensagem;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> mensagens;
    private Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> lista, Context c) {
        this.mensagens = lista;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;
        if (viewType == TIPO_REMETENTE){

            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);

        }else if (viewType == TIPO_DESTINATARIO){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = mensagens.get(position);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        if(imagem != null){
            holder.imagem.setVisibility(View.VISIBLE);
            Uri url = Uri.parse(imagem);
            Glide.with(context).load(url).into(holder.imagem);

            String nome = mensagem.getNome();
            if( !nome.isEmpty() ){
                holder.nome.setText( nome );
            }else {
                holder.nome.setVisibility(View.GONE);
            }

            //esconder mensagem
            holder.mensagem.setVisibility(View.GONE);

        }else{
            holder.mensagem.setVisibility(View.VISIBLE);
            holder.mensagem.setText(msg);

           String nome = mensagem.getNome();
            if( !nome.isEmpty() ){
                holder.nome.setText( nome );
            }else {
                holder.nome.setVisibility(View.GONE);
            }

            //esconder foto
            holder.imagem.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = mensagens.get(position);

        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        if (idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_REMETENTE;
        }
        return TIPO_DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mensagem;
        TextView nome;
        ImageView imagem;

        public MyViewHolder(View itemView){
            super(itemView);

            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            imagem = itemView.findViewById(R.id.imageMensagemFoto);
            nome = itemView.findViewById(R.id.textNomeExibicao);

        }
    }
}
