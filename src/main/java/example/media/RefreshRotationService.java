package example.media;

import java.io.IOException;
import java.util.*;

public final class RefreshRotationService {
  private final InfraiClient client;
  private final Set<String> revoked = new HashSet<>();
  public RefreshRotationService(InfraiClient client) { this.client = client; }
  public synchronized String rotate(String refreshToken) throws IOException {
    if (!revoked.add(refreshToken)) throw new IllegalStateException("refresh token revoked");
    return client.refresh(refreshToken, "media-session-" + refreshToken);
  }
}
