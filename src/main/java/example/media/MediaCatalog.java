package example.media;

import java.util.*;

public final class MediaCatalog {
  public record Asset(String id, String title, String state) {}
  public record Job(String id, String assetId, String output) {}
  private final Map<String, Asset> assets = new HashMap<>();
  private final Map<String, Job> jobs = new HashMap<>();
  public Asset ingest(String id, String title) { Asset a = new Asset(id, title, "INGESTED"); assets.put(id, a); return a; }
  public Job process(String jobId, String assetId, String output) { assets.computeIfPresent(assetId, (k,v) -> new Asset(v.id(), v.title(), "PROCESSED")); Job j = new Job(jobId, assetId, output); jobs.put(jobId, j); return j; }
  public Job job(String id) { return jobs.get(id); }
}
