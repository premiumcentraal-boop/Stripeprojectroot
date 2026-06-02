import { useEffect, useState } from "react";
import { Download, AlertCircle, Loader2, Github } from "lucide-react";
import { LOCAL_APK_PATH, GITHUB_APK_URL } from "../config/download";
import { CURRENT_APP_VERSION } from "../config/version";

const BUTTON_TEXT = `Download RootDeck ${CURRENT_APP_VERSION}`;

type ApkState =
  | { kind: "checking" }
  | { kind: "local"; sizeMB: string | null }
  | { kind: "github" };

export function DownloadButton() {
  const [state, setState] = useState<ApkState>({ kind: "checking" });

  useEffect(() => {
    let cancelled = false;
    // A. Try the locally-staged APK first.
    fetch(LOCAL_APK_PATH, { method: "HEAD" })
      .then((res) => {
        if (cancelled) return;
        const ct = res.headers.get("content-type") ?? "";
        const cl = res.headers.get("content-length");
        const looksLikeApk =
          res.ok && !ct.includes("text/html") && !ct.includes("text/plain");
        if (looksLikeApk) {
          const sizeMB = cl
            ? (parseInt(cl, 10) / 1024 / 1024).toFixed(1)
            : null;
          setState({ kind: "local", sizeMB });
        } else {
          // B. Fall back to the GitHub Release. We do NOT probe it with HEAD —
          // GitHub may block or redirect HEAD requests across origins.
          setState({ kind: "github" });
        }
      })
      .catch(() => !cancelled && setState({ kind: "github" }));
    return () => {
      cancelled = true;
    };
  }, []);

  if (state.kind === "checking") {
    return (
      <div className="inline-flex items-center gap-2 rounded-md border border-white/15 text-zinc-400 px-5 py-3">
        <Loader2 className="size-4 animate-spin" /> Checking for APK…
      </div>
    );
  }

  if (state.kind === "local") {
    return (
      <div className="inline-flex flex-col items-start">
        <a
          href={LOCAL_APK_PATH}
          download
          className="inline-flex items-center gap-2 rounded-md bg-emerald-500 text-black px-5 py-3"
        >
          <Download className="size-4" /> {BUTTON_TEXT}
          {state.sizeMB && (
            <span className="text-xs opacity-70">· {state.sizeMB} MB</span>
          )}
        </a>
        <span className="mt-2 text-xs text-emerald-400">
          Status: APK ready from website
        </span>
      </div>
    );
  }

  // state.kind === "github"
  return (
    <div className="inline-flex flex-col items-start">
      <a
        href={GITHUB_APK_URL}
        className="inline-flex items-center gap-2 rounded-md bg-emerald-500 text-black px-5 py-3"
      >
        <Download className="size-4" /> {BUTTON_TEXT}
      </a>
      <span className="mt-2 text-xs text-zinc-400 inline-flex items-center gap-1">
        <Github className="size-3" /> Status: APK served from GitHub Release
      </span>
    </div>
  );
}

/**
 * Fully disabled state used when the GitHub release URL has been intentionally
 * removed (rare). The live `DownloadButton` above always at least falls back to
 * GitHub, so this is exported for manual use in maintenance modes.
 */
export function DownloadButtonDisabled() {
  return (
    <div className="inline-flex flex-col items-start">
      <button
        disabled
        className="inline-flex items-center gap-2 rounded-md bg-zinc-800 text-zinc-500 px-5 py-3 cursor-not-allowed"
      >
        <AlertCircle className="size-4" /> {BUTTON_TEXT}
      </button>
      <span className="mt-2 text-xs text-zinc-500">
        APK not built yet. Run GitHub Actions → Build RootDeck APK.
      </span>
    </div>
  );
}
