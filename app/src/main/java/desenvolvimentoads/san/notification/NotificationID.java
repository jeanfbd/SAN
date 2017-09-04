package desenvolvimentoads.san.notification;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by User on 30/08/2017.
 */

public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
    }
}


