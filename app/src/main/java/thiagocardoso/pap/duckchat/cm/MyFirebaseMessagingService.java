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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import thiagocardoso.pap.duckchat.R;
import thiagocardoso.pap.duckchat.activity.MainActivity;
import thiagocardoso.pap.duckchat.config.ConfiguracaoFirebase;
import thiagocardoso.pap.duckchat.helper.Base64Custom;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao;

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

        //token Pixel API 28
        //epkORwL8QqKs6EiEyMcbym:APA91bEwrv4P4r5EQvJOLocbTFmbxZZbfuEnXU3uBHvU6caMFBpqQC6LsfyXpwmco_83jHlqUGLsvZr4H2btbaTvUsTiTsDUDnrKrXwEdfkOEUWbONQ1x-3_keuOd1pkjlEV67sGfaCa

        //token pixel API 28 2
        //eOfKN6hRRO-5qYjXGJxXkX:APA91bEIH8_sm-IfFAGqrIGprFcPspemzBGY2nxVgYXguSQrVQbD6gMTqImufPE79aWdLKg7U5DEdLlLPvzzqnmkOwa1H0dxNGTeKcGZch4OTJf4qnMBtkjcGurzzkuiq3F6cpb7c1wW

        //token nexus 5
        //dxiV2oMmT5WBwbmPuvfgXF:APA91bFUcEGMNdnYdIXIdp7yo9jqb19cmKFogR1RceA8wSmzvX-lY3BGIVcH6BJadmcYVwRJ5JLarXTtqQcAF7b5t7tFnuWOWLQs5m_AsGXaDsnQWy-pA4X8kWEFjb0lWqonBgl5HpGw

    }
}
