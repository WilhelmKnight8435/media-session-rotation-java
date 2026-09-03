package example.media;

public final class MediaSessionExample {
  public static void main(String[] args) throws Exception {
    MediaCatalog catalog = new MediaCatalog();
    catalog.ingest("asset-course-1", "Algebra lesson");
    catalog.process("job-1", "asset-course-1", "https://cdn.example/algebra.mp4");
    String key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
    String session = new RefreshRotationService(new InfraiClient(key)).rotate("refresh-1");
    System.out.println("Delivered " + catalog.job("job-1").output() + " in session " + session);
  }
}
