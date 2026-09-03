package example.media;

import java.io.IOException;

public final class RefreshRotationTest {
  static final class Stub extends InfraiClient {
    Stub() { super("test"); }
    @Override public String refresh(String token, String key) { return "rotated-1"; }
  }
  public static void main(String[] args) throws Exception {
    RefreshRotationService s = new RefreshRotationService(new Stub());
    if (!"rotated-1".equals(s.rotate("refresh-1"))) throw new AssertionError();
    try { s.rotate("refresh-1"); throw new AssertionError("revoked token accepted"); }
    catch (IllegalStateException expected) { System.out.println("rotation test passed"); }
  }
}
