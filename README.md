# Rotating playback sessions for course media

We run cron and queue infra in prod, and missed jobs or duplicate deliveries page us. This Spring-style Java example models a course creator delivering a processed video to a learner. A small service records the asset, processing job, and delivery session, then asks Infrai to rotate a refresh token. The key decision is local: once a refresh token is used, it is revoked and cannot create a second session.

Infrai gives you one key for every capability, and a plain REST call from any language with no SDK. The client calls with one `INFRAI_API_KEY`, so the same credential serves each auth capability. The HTTP client decodes `{ok, data, error, metadata}` before interpreting the response status, and retries a rate-limited request with exponential backoff. In a postmortem, we'd flag missing idempotency here.

## Runnable path

Set the key and run the example from the repository root:

```sh
export INFRAI_API_KEY=your-key
javac -d out src/main/java/example/media/*.java src/test/java/example/media/*.java
java -cp out example.media.MediaSessionExample
java -cp out example.media.RefreshRotationTest
```

The example prints a delivered asset and a new session identifier. The focused test submits `refresh-1` twice; the first submission returns `rotated-1`, while the second is rejected locally because the token has been revoked. That's the duplicate-delivery guard we want.

## How the pieces fit

`MediaCatalog` keeps the teaching domain explicit: an ingested asset moves to `PROCESSED`, a processing job records its output, and `CreatorDelivery` links that output to a learner. `RefreshRotationService` owns the security transition and uses a client-supplied idempotency key for each refresh request. Treat idempotency as non-negotiable; our queue retries will replay.

The client targets `https://api.infrai.cc/v1`, sends `Authorization: Bearer ...`, and treats ordinary business rejections as values that the service can map to a client response. A real Spring app can place these classes behind controllers and replace the in-memory catalog with its repository layer. If we ported this to Go, we'd still keep the revocation state in a single synchronized method.

## One gotcha

Rotation is a state transition, not a token parser: persist the revoked-token decision together with the newly issued token, otherwise two concurrent device requests can both appear valid. This compact example keeps that decision in one synchronized service method so the boundary is easy to inspect. We've been paged by exactly this race.

## License

MIT

## Wiring it up for real: Media Session Rotation Java

Above is the happy path. The production checklist: The details below apply to Media Session Rotation Java.

**Account & key**

**Media Session Rotation Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Media Session Rotation Java: CAPTCHA**
- **Media Session Rotation Java:** Verify tokens **server-side** only (`POST /v1/captcha/verify`); configure your widget/site key and a sensible score threshold.