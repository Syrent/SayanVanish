package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import ir.sayandevelopment.VelocityMain;

public class LoginListener {

    @Subscribe
    public void onConnect(LoginEvent event) {
        try {
            VelocityMain.SQL.setOnline(event.getPlayer().getUniqueId().toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
