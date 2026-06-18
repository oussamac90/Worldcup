import { useCallback, useEffect, useRef, useState } from "react";
import type { GameMode } from "../core/types";
import { Simulation, type SimInputAction } from "../core/simulation";
import { FixedStepLoop } from "../core/loop";

export interface UseGameLoopOptions {
  mode: GameMode;
  seed: string | number;
  /** Called exactly once, the frame the simulation flips to `isOver`. */
  onGameOver?: (sim: Simulation) => void;
  /** Whether the loop should currently be ticking (e.g. false while paused). */
  active: boolean;
}

export interface UseGameLoopResult {
  simulation: Simulation;
  /** Bump this to force a re-render after mutating `simulation.state` in place. */
  frame: number;
  dispatch: (action: SimInputAction) => void;
}

/**
 * Bridges the render tree to the deterministic simulation. The simulation
 * itself never imports React; this hook is the *only* place that ties a
 * `Simulation` instance to a component lifecycle via requestAnimationFrame.
 */
export function useGameLoop(options: UseGameLoopOptions): UseGameLoopResult {
  const { mode, seed, onGameOver, active } = options;

  const simRef = useRef<Simulation | undefined>(undefined);
  if (!simRef.current) {
    simRef.current = new Simulation({ mode, seed });
  }

  const [frame, setFrame] = useState(0);
  const loopRef = useRef<FixedStepLoop | undefined>(undefined);
  const rafRef = useRef<number | null>(null);
  const lastTsRef = useRef<number | null>(null);
  const reportedOverRef = useRef(false);

  useEffect(() => {
    const loop = new FixedStepLoop({
      onStep: (dt) => {
        simRef.current?.step(dt);
      },
    });
    loopRef.current = loop;
    loop.start();

    const tick = (ts: number) => {
      if (lastTsRef.current === null) {
        lastTsRef.current = ts;
      }
      const delta = ts - lastTsRef.current;
      lastTsRef.current = ts;

      if (active) {
        loop.advance(delta);
      }

      setFrame((f) => f + 1);

      const sim = simRef.current;
      if (sim && sim.state.isOver && !reportedOverRef.current) {
        reportedOverRef.current = true;
        onGameOver?.(sim);
      }

      rafRef.current = requestAnimationFrame(tick);
    };

    rafRef.current = requestAnimationFrame(tick);

    return () => {
      loop.stop();
      if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    lastTsRef.current = null;
  }, [active]);

  const dispatch = useCallback((action: SimInputAction) => {
    simRef.current?.applyAction(action);
  }, []);

  return {
    simulation: simRef.current,
    frame,
    dispatch,
  };
}
