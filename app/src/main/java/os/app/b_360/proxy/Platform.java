package os.app.b_360.proxy;

public class Platform implements Notifier {

    Platform360  platform360;
    PlatformRapi platformRapi;

    public Platform () {

        platform360  = new Platform360();
        platformRapi = new PlatformRapi();
    }
    @Override
    public void sendEvent() {

        platform360.sendEvent();
        platformRapi.sendEvent();
    }
}
