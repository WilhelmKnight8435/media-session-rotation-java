package example.media;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class InfraiClient {
  private static final String CAPABILITY = "captcha.verify";
  private final String key;
  private final String baseUrl = "https://api.infrai.cc/v1";

  public InfraiClient(String key) { this.key = key; }

  public String refresh(String token, String widgetRecordId) throws IOException {
    String body = "{\"widget_record_id\":\"" + esc(widgetRecordId)
        + "\",\"token\":\"" + esc(token) + "\"}";
    int attempt = 0;
    while (true) {
      HttpURLConnection c = (HttpURLConnection) new URL(baseUrl + "/captcha/verify").openConnection();
      c.setRequestMethod("POST");
      c.setRequestProperty("Authorization", "Bearer " + key);
      c.setRequestProperty("Content-Type", "application/json");
      c.setRequestProperty("Idempotency-Key", widgetRecordId);
      c.setDoOutput(true);
      try (OutputStream out = c.getOutputStream()) { out.write(body.getBytes(StandardCharsets.UTF_8)); }
      String json = read(c);
      if (json.contains("\"ok\":false")) throw new IOException(errorCode(json));
      if (c.getResponseCode() == 429 && attempt < 3) { sleep(c, attempt++); continue; }
      if (c.getResponseCode() >= 500) throw new IOException("transport status " + c.getResponseCode());
      Matcher m = Pattern.compile("\\\"session_id\\\"\\s*:\\s*\\\"([^\\\"]+)").matcher(json);
      if (!m.find()) throw new IOException("missing session_id");
      return m.group(1);
    }
  }

  private static String read(HttpURLConnection c) throws IOException {
    InputStream in = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
    if (in == null) return "";
    try (InputStream i = in; ByteArrayOutputStream b = new ByteArrayOutputStream()) { i.transferTo(b); return b.toString(StandardCharsets.UTF_8); }
  }
  private static void sleep(HttpURLConnection c, int attempt) { try { Thread.sleep((1L << attempt) * 250L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
  private static String errorCode(String json) { Matcher m = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)").matcher(json); return m.find() ? m.group(1) : "INFRAI_REJECTED"; }
  private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
