/**
 * SFX identifiers used by the engine. Mapped to actual audio sources by the
 * SoundManager — kept as an enum-like union so call sites never need to know
 * file paths.
 */
export type SfxKey =
  | "save"
  | "perfectSave"
  | "goal"
  | "powerupCollect"
  | "whistleStart"
  | "whistleEnd"
  | "buttonTap";

export const SFX_KEYS: readonly SfxKey[] = [
  "save",
  "perfectSave",
  "goal",
  "powerupCollect",
  "whistleStart",
  "whistleEnd",
  "buttonTap",
];
