package os.app.b_360.proxy;

import static os.app.b_360.proxy.data.P360.url;
public class Platform360 implements Notifier {
    @Override
    public void sendEvent() {

        System.out.println(url);
        System.out.println("Platform360 --> sendEvent");
    }
}

