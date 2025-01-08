package os.app.b_360.proxy;

import static os.app.b_360.proxy.data.Rapi.url;
public class PlatformRapi implements Notifier {
    @Override
    public void sendEvent() {

        System.out.println(url);
        System.out.println("PlatformRapi --> sendEvent");
    }
}
