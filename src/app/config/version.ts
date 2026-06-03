/**
 * Single source of truth for RootDeck website versioning.
 *
 * To ship a new release: prepend a new entry to RELEASES. The header badge,
 * download button, feature badges, and changelog page all read from here.
 * Nothing else needs to change.
 */

export type Release = {
  version: string;       // e.g. "v0.2.0" — also used as the addedIn key on features
  name: string;          // short title shown on the changelog page
  date: string;          // ISO date (YYYY-MM-DD) for ordering / display
  notes: string[];       // bullet points
};

export const RELEASES: Release[] = [
  {
    version: "v0.16.0",
    name: "Video Feed first test run",
    date: "2026-06-03",
    notes: [
      "Added a first test run step that saves the video feed setup and repeat playback request before opening a scoped test app.",
      "Clarified that RootDeck should be enabled in LSPosed only for user-owned or controlled camera test apps.",
      "Updated workflow checks and dashboard version label for the v0.16.0 build.",
    ],
  },
  {
    version: "v0.15.0",
    name: "Repeat video feed setup",
    date: "2026-06-03",
    notes: [
      "Video Test Feed now saves repeat playback as always on for selected input videos.",
      "Readiness checklist now shows repeat status and clarifies scoped LSPosed testing expectations.",
      "Dashboard version label updated for the repeat playback build.",
    ],
  },
  {
    version: "v0.14.0",
    name: "Clear LSPosed setup results",
    date: "2026-06-03",
    notes: [
      "Improved first-run recommended installs so users see the LSPosed setup gallery even when root mode was already enabled.",
      "Added clearer LSPosed install progress, success, failure, and next-step screens with icon steps.",
      "Added an obvious app version label on the Android dashboard.",
    ],
  },
  {
    version: "v0.13.0",
    name: "Video crop and resize editor",
    date: "2026-06-03",
    notes: [
      "Added a friendlier Video Test Feed editor for black bars, center crop, stretch, and camera-output matching.",
      "Added crop-position controls for fill/crop mode so users can choose center, top, or bottom framing.",
      "Updated the LSPosed readiness summary to show the exact video fit, crop, and output-size setup before testing.",
    ],
  },
  {
    version: "v0.12.0",
    name: "Video fit and crop controls",
    date: "2026-06-03",
    notes: [
      "Added user-friendly Video Test Feed resize options: black bars, fill/crop, stretch, and match camera output.",
      "Added camera output size presets for auto camera match, 720p, 1080p, square, and portrait feeds.",
      "Shows a clear preview preparation summary before LSPosed video-feed testing.",
    ],
  },
  {
    version: "v0.11.0",
    name: "LSPosed to Video Test Feed handoff",
    date: "2026-06-03",
    notes: [
      "Added a direct handoff from successful LSPosed setup to Video Test Feed setup.",
      "Improved Video Test Feed with a user-friendly LSPosed setup checklist and readiness status.",
      "Clarified video file selection and front/back camera target steps for scoped user-owned test apps.",
    ],
  },
  {
    version: "v0.10.0",
    name: "Recommended installs gallery",
    date: "2026-06-03",
    notes: [
      "Improved first-run setup with a recommended installs gallery including LSPosed.",
      "Added clearer LSPosed download/install success and failure states.",
      "Added a visual next-step flow: download, install, reboot, enable.",
    ],
  },
  {
    version: "v0.9.0",
    name: "Guided LSPosed setup flow",
    date: "2026-06-03",
    notes: [
      "Added a first-run setup flow that asks for RootDeck root access in simple language.",
      "Shows recommended installations with LSPosed as an install option after a loading scan.",
      "Adds download progress, install progress messaging, and a clear success/reboot instruction screen.",
    ],
  },
  {
    version: "v0.8.0",
    name: "LSPosed Installer",
    date: "2026-06-03",
    notes: [
      "Added LSPosed Installer tool for Magisk devices.",
      "Detects Magisk version and selects the matching LSPosed Zygisk or Riru release ZIP from the official LSPosed releases.",
      "Downloads the module and installs it through Magisk with RootDeck root-mode gating and command logging.",
    ],
  },
  {
    version: "v0.7.0",
    name: "Video Test Feed (Camera Injection)",
    date: "2026-06-02",
    notes: [
      "Added Video Test Feed tool, moving it from planned to implemented.",
      "LSPosed/Xposed module targeting Camera and Camera2 APIs for hidden video injection.",
      "Supports user-selected video upload and local preview for front and back camera streams.",
      "Strictly limited to user-owned testing environments and creator workflows.",
    ],
  },
  {
    version: "v0.6.0",
    name: "Doppelganger LSPosed Module",
    date: "2026-06-02",
    notes: [
      "Added LSPosed/Xposed module targeting PackageManager, UserManager, ANDROID_ID, and attestation paths.",
      "Module enables true instance separation for Doppelganger, hiding clones from each other and providing unique device identifiers per clone for ethical multi-account workflows.",
      "Implemented attestation bypass to allow social media workflows that require Play Integrity on modified devices."
    ],
  },
  {
    version: "v0.5.0",
    name: "Central safety policy",
    date: "2026-06-02",
    notes: [
      "Single safety-policy source of truth in src/app/config/policy.ts, rendered at /#policy.",
      "Scrubbed scattered policy wording from Android screens, planned-tool models, and file-level kdoc; each surface now links to the policy URL via SafetyPolicyLink.",
      "Planned-tool detail screen simplified — no per-tool safety copy, no allowed/not-allowed lists in code.",
    ],
  },
  {
    version: "v0.4.0",
    name: "Doppelganger update",
    date: "2026-06-02",
    notes: [
      "Doppelganger app cloning via Android's secondary-user mechanism: create users, clone installed apps with pm install-existing --user, launch into a user, remove clones, remove users.",
      "Each clone has its own data and accounts. RootDeck does not hide clones from each other — no inter-instance anti-detection.",
      "Every action goes through the standard command-preview + confirmation + logs pipeline.",
    ],
  },
  {
    version: "v0.3.0",
    name: "Basic Tools Roadmap update",
    date: "2026-06-02",
    notes: [
      "Added 7 planned Basic Tools roadmap modules: Doppelganger, Magisk Manager Integration, LSPosed Module Center, Mock Location Lab, Video Test Feed, Image Test Feed, and Live Camera Spoof Studio.",
      "New PlannedBasicToolDetailScreen with safety copy, allowed scope, and not-allowed list per module.",
      "Dashboard gains a Basic Tools Roadmap card. No commands run from any planned module.",
    ],
  },
  {
    version: "v0.2.0",
    name: "Process Privacy Guard update",
    date: "2026-06-01",
    notes: [
      "Process Privacy Guard tool: per-app force-stop, background and wake-lock restrictions via cmd appops, optional pm disable-user.",
      "Dashboard card with active-policy count and last action.",
      "Replaced the planned 'Process Manager' tile.",
    ],
  },
  {
    version: "v0.1.0",
    name: "First test build",
    date: "2026-05-15",
    notes: [
      "Root Management (global root switch, su -c id probe, per-app internal allowlist, root manager deep-link).",
      "Memory Monitoring with optional rooted /proc/meminfo read.",
      "Scheduled Reboot (reboot · recovery · bootloader).",
      "Logs, Settings, dark/black/system theme.",
    ],
  },
];

export const CURRENT = RELEASES[0];
export const CURRENT_APP_VERSION = CURRENT.version;
