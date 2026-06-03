import { Download, AlertCircle, Github } from "lucide-react";
import { GITHUB_APK_URL } from "../config/download";
import { CURRENT_APP_VERSION } from "../config/version";

const BUTTON_TEXT = `Download RootDeck ${CURRENT_APP_VERSION}`;

export function DownloadButton() {
  return (
    <div className="inline-flex flex-col items-start">
      <a
        href={GITHUB_APK_URL}
        className="inline-flex items-center gap-2 rounded-md bg-emerald-500 text-black px-5 py-3"
      >
        <Download className="size-4" /> {BUTTON_TEXT}
      </a>
      <span className="mt-2 text-xs text-zinc-400 inline-flex items-center gap-1">
        <Github className="size-3" /> Status: RootDeck v0.13.0 APK served from the latest tag release asset
      </span>
    </div>
  );
}

/**
 * Fully disabled state used when the GitHub release URL has been intentionally
 * removed (rare). The live `DownloadButton` above links directly to the release
 * asset attached to the `latest` tag, so this is exported for manual use in
 * maintenance modes.
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
