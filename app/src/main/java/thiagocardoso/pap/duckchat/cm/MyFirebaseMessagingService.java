package thiagocardoso.pap.duckchat.cm;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import thiagocardoso.pap.duckchat.R;
import thiagocardoso.pap.duckchat.activity.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage notificacao) {

        if( notificacao.getNotification() !=null){

            String titulo = notificacao.getNotification().getTitle();
            String corpo = notificacao.getNotification().getBody();

            enviarNotificacao(titulo, corpo);

            //Log.i("Notificacao", "recebida titulo: " + titulo + " corpo: " + corpo);

        }

    }

    private void enviarNotificacao(String titulo, String corpo){

        //configuração para a notificação
        String canal = getString(R.string.default_notification_channel_id);
        //Definindo Ringtone da notificação
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ao clicar na notificação fazer ela sumir e abrir a aplicação(MainActivity)
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, FLAG_ONE_SHOT);

        //criar notificação
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this, canal)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setSmallIcon(R.drawable.ic_baseline_message_24)
                .setSound(uriSom)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        //Recupera notificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //verifica a versão do Android a partir do Pie para configurar canal de notificação
        //Trecho de código feito para compatibilidade
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            NotificationChannel channel = new NotificationChannel(canal,"canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //Envia notificação
        notificationManager.notify(0, notificacao.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
