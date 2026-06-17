import { createAudioPlayer, setAudioModeAsync, type AudioPlayer, type AudioSource } from "expo-audio";
import { SFX_KEYS, type SfxKey } from "./sounds";

/**
 * Lazily-loaded SFX player with a global mute toggle.
 *
 * Sound assets are optional: register sources via `SoundManager.registerSource`
 * (e.g. from app bootstrap, once .mp3/.wav files are added under
 * `assets/sfx/`). Until a source is registered for a given key, `play()` is
 * a harmless no-op so the rest of the app/engine doesn't need to special-case
 * missing audio files during early development.
 */
export class SoundManager {
  private static instance: SoundManager | null = null;

  private sources = new Map<SfxKey, AudioSource>();
  private players = new Map<SfxKey, AudioPlayer>();
  private muted = false;
  private ready = false;

  static get shared(): SoundManager {
    if (!SoundManager.instance) {
      SoundManager.instance = new SoundManager();
    }
    return SoundManager.instance;
  }

  registerSource(key: SfxKey, source: AudioSource): void {
    this.sources.set(key, source);
    const existing = this.players.get(key);
    if (existing) {
      existing.release();
      this.players.delete(key);
    }
  }

  async init(): Promise<void> {
    if (this.ready) return;
    await setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: false,
      interruptionMode: "mixWithOthers",
    });
    this.ready = true;
  }

  setMuted(muted: boolean): void {
    this.muted = muted;
  }

  get isMuted(): boolean {
    return this.muted;
  }

  toggleMuted(): boolean {
    this.muted = !this.muted;
    return this.muted;
  }

  play(key: SfxKey): void {
    if (this.muted) return;
    const source = this.sources.get(key);
    if (!source) return; // no asset registered yet — silent no-op

    let player = this.players.get(key);
    if (!player) {
      player = createAudioPlayer(source);
      this.players.set(key, player);
    }
    player.seekTo(0).catch(() => undefined);
    player.play();
  }

  releaseAll(): void {
    for (const player of this.players.values()) {
      player.release();
    }
    this.players.clear();
  }
}

export function isKnownSfxKey(value: string): value is SfxKey {
  return (SFX_KEYS as readonly string[]).includes(value);
}
