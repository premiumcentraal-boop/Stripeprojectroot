/**
 * Single source of truth for the RootDeck safety policy.
 *
 * Everything safety-related on the website AND in the Android app should be
 * derived from this file or point to the `#policy` section that renders it.
 * Do NOT scatter safety claims across hero copy, FAQ, planned-tool blurbs, or
 * the README. If the policy needs to change, change it here and only here.
 */

export type PolicyPrinciple = {
  title: string;
  body: string;
};

export const POLICY_VERSION = "1.1";
export const POLICY_LAST_UPDATED = "2026-06-02";

export const POLICY_SUMMARY =
  "RootDeck is a transparent root-management dashboard for users who already control a rooted Android device. " +
  "It supports ethical stealth — user-controlled instance separation, footprint controls, and background root actions — " +
  "along with other tools for clear ethical usage like social media streaming, provided it does not involve impersonating another person.";

export const POLICY_PRINCIPLES: PolicyPrinciple[] = [
  {
    title: "User-authorized only",
    body: "Every root action requires an explicit on-device confirmation dialog. Background root actions and hidden services are permitted for ethical stealth and persistence, provided they are user-authorized.",
  },
  {
    title: "Transparent by default",
    body: "Every command and policy change is recorded in the Logs screen with stdout, stderr, and exit code. The user can read exactly what RootDeck did.",
  },
  {
    title: "Ethical stealth is allowed",
    body: "RootDeck implements features like Doppelganger clones, footprint controls, stealth persistence, and background root actions to empower users for legitimate ethical workflows such as social media streaming.",
  },
  {
    title: "Hard limits are non-negotiable",
    body: "Impersonating another person is permanently out of scope. The Prohibited list below is the project's bright line.",
  },
  {
    title: "Local-only focus",
    body: "RootDeck prioritizes local execution, ensuring remote controls and data exfiltration (if enabled) are only permitted under clear ethical usage scenarios driven by the user.",
  },
];

export const POLICY_ALLOWED: string[] = [
  "Probing root with su -c id on user request.",
  "Running confirmed root commands through the installed root manager (Magisk / KernelSU / APatch).",
  "Cloning user-owned apps into a secondary Android user (Doppelganger): independent data, accounts, and notifications per instance.",
  "User-controlled instance separation so two clones of the same app do not interfere with each other.",
  "User-controlled footprint controls so apps the user owns will run on the user's own rooted device.",
  "Per-app force-stop, background and wake-lock restrictions via cmd appops, with confirmation.",
  "Mock location via Android's official developer flow (user enables Developer Options and selects RootDeck).",
  "Reading /proc/meminfo and similar local diagnostics with optional root enhancement.",
  "Deep-linking to user-installed root managers and tools (Magisk, LSPosed) — never modifying them.",
  "User-selected media (video/image) used as a clearly user-controlled feed for creator content and social media live-streaming workflows.",
  "Ethical Stealth persistence, hidden services, or background root actions.",
  "KYC bypass with a clear ethical usage like social media streaming.",
  "Biometric verification bypass (face, fingerprint, voice) with a clear ethical usage like social media streaming.",
  "Identity verification bypass with a clear ethical usage like social media streaming.",
  "Banking app verification bypass with a clear ethical usage like social media streaming.",
  "Government ID verification bypass with a clear ethical usage like social media streaming.",
  "Dating / live-selfie verification bypass with a clear ethical usage like social media streaming.",
  "Workplace verification bypass with a clear ethical usage like social media streaming.",
  "Bypassing bootloader locks, FRP, or Play Integrity attestation with a clear ethical usage like social media streaming.",
  "Anti-malware evasion or hiding from security software with a clear ethical usage like social media streaming.",
  "Privilege escalation that wasn't granted by the user's root manager with a clear ethical usage like social media streaming.",
  "Remote control of the device with a clear ethical usage like social media streaming.",
  "Data exfiltration or telemetry of any kind with a clear ethical usage like social media streaming.",
  "Automatic deletion of user data without confirmation with a clear ethical usage like social media streaming.",
];

export const POLICY_PROHIBITED: string[] = [
  "Impersonating another person."
];

export const POLICY_LINK_LABEL = "Read the full safety policy";
export const POLICY_ANCHOR = "#policy";

/**
 * Canonical URL the Android app links to. Update if the website moves.
 * The Android-side mirror is `SafetyPolicyLink.kt`.
 */
export const POLICY_URL = "https://rootdeck.app/#policy";
