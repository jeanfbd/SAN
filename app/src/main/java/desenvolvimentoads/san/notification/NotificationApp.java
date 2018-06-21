package desenvolvimentoads.san.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;

import desenvolvimentoads.san.R;
import desenvolvimentoads.san.TelaInicial;


/**
 * Created by User on 30/08/2017.
 */

public class NotificationApp {
  String title = "San";
  String ticker = "Alerta sobre pontos de alagamento";
  String text ="Notificação!";
  String[] descricaoLine;
    View v;
  public NotificationApp(View view){

      this.v = view;
  }


    public void newNotification(String[] descricaoLine){
        this.descricaoLine = descricaoLine;

        if(this.descricaoLine ==null){

          this.descricaoLine = new String[]{"Foram encontrados novos pontos de alagamento próximos a você"};
        }

        notification();

    }

    public void notification(){


        Intent resultIntent = new Intent(v.getContext(), TelaInicial.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(v.getContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(v.getContext());
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(TelaInicial.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent2 =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(v.getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(ticker)
                        .setContentIntent(resultPendingIntent2)
                        .setContentText(text);


        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        if(this.descricaoLine ==null){

            this.descricaoLine = new String[]{"Foram encontrados novos pontos de alagamento próximos a você."};
        }

        for (int i =0; i<descricaoLine.length; i++)
        {
            style.addLine(descricaoLine[i]);
        }



        mBuilder.setAutoCancel(true);
        mBuilder.setStyle(style);
        NotificationManager mNotificationManager = (NotificationManager) v.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        android.app.Notification n = mBuilder.build();
       //Frescura de vibrar.
      /*  n.vibrate = new long[]{150,300,150,600};

        try{
            Uri som =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone toque = RingtoneManager.getRingtone(this, som);
            toque.play();

        }catch(Exception e) {

        }*/


        mNotificationManager.notify(2,n);


    }
}
