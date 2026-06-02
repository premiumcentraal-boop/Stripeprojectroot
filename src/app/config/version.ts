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
