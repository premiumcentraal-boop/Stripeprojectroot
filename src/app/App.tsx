import { PhoneMockup } from "./components/PhoneMockup";
import { DownloadButton } from "./components/DownloadButton";
import { GITHUB_ACTIONS_URL } from "./config/download";
import { CURRENT_APP_VERSION, RELEASES } from "./config/version";
import {
  POLICY_VERSION,
  POLICY_LAST_UPDATED,
  POLICY_SUMMARY,
  POLICY_PRINCIPLES,
  POLICY_ALLOWED,
  POLICY_PROHIBITED,
} from "./config/policy";
import {
  ShieldCheck,
  Terminal,
  Package,
  ScrollText,
  Settings as SettingsIcon,
  Download,
  Github,
  Cpu,
  Lock,
  AlertTriangle,
  CheckCircle2,
  Code2,
  FlaskConical,
  GitBranch,
  FileText,
  Wrench,
  XCircle,
  Video,
} from "lucide-react";

const features = [
  { icon: ShieldCheck, title: "Root status", body: "Detect Magisk, KernelSU or APatch by running su -c id.", addedIn: "v0.1.0" },
  { icon: Terminal, title: "Safe root tools", body: "Reboot, recovery, bootloader, cache clear, mount status — every action gated by an explicit confirmation dialog.", addedIn: "v0.1.0" },
  { icon: Package, title: "Local app manager", body: "Browse installed packages, search, copy package names, and optionally disable or enable apps for the current user.", addedIn: "v0.1.0" },
  { icon: ScrollText, title: "Visible command log", body: "Every root command is recorded with timestamp, command preview, and full stdout/stderr. Clear anytime.", addedIn: "v0.1.0" },
  { icon: SettingsIcon, title: "Ethical Stealth", body: "Instance separation, footprint controls, and stealth persistence for clear ethical workflows.", addedIn: "v0.1.0" },
  { icon: Cpu, title: "Android 14+ native", body: "Kotlin · Jetpack Compose · Material 3 · minSdk 34. No webview.", addedIn: "v0.1.0" },
  { icon: Lock, title: "Process Privacy Guard", body: "Per-app force-stop, background and wake-lock restrictions via cmd appops, optional pm disable-user.", addedIn: "v0.2.0" },
  { icon: Code2, title: "Doppelganger (LSPosed)", body: "Xposed module targeting PackageManager, UserManager, ANDROID_ID, and attestation. In-app secondary user cloning.", addedIn: "v0.6.0" },
  { icon: Video, title: "Video Test Feed", body: "LSPosed camera injection module for front/back streams. User-selected video upload with local preview.", addedIn: "v0.7.0" },
];

function VersionBadge({ version }: { version: string }) {
  return (
    <a
      href="#changelog"
      className="rounded border border-zinc-700 bg-zinc-800 px-1.5 py-0.5 text-[10px] text-zinc-400 hover:text-zinc-200 hover:border-zinc-600 font-mono"
      title={`Added in ${version}`}
    >
      {version}
    </a>
  );
}

function InternalBadge() {
  return (
    <span className="inline-flex items-center gap-1.5 rounded border border-amber-500/50 bg-amber-500/10 px-2 py-0.5 text-[11px] text-amber-400 font-mono tracking-wide">
      <span className="size-1.5 rounded-full bg-amber-400 animate-pulse" />
      INTERNAL USE ONLY
    </span>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div className="text-[10px] font-mono uppercase tracking-widest text-zinc-500 mb-2">{children}</div>
  );
}

function Section({ id, children, className = "" }: { id?: string; children: React.ReactNode; className?: string }) {
  return (
    <section id={id} className={`relative px-6 md:px-10 py-14 md:py-20 ${className}`}>
      <div className="max-w-5xl mx-auto">{children}</div>
    </section>
  );
}

function Divider() {
  return <div className="border-t border-white/5" />;
}

function Tag({ children, variant = "default" }: { children: React.ReactNode; variant?: "default" | "green" | "amber" | "red" }) {
  const styles = {
    default: "border-zinc-700 bg-zinc-800/60 text-zinc-400",
    green: "border-emerald-500/30 bg-emerald-500/10 text-emerald-400",
    amber: "border-amber-500/30 bg-amber-500/10 text-amber-400",
    red: "border-red-500/30 bg-red-500/10 text-red-400",
  };
  return (
    <span className={`inline-flex items-center rounded border px-1.5 py-0.5 text-[10px] font-mono ${styles[variant]}`}>
      {children}
    </span>
  );
}

export default function App() {
  return (
    <div className="min-h-screen w-full bg-zinc-950 text-zinc-100 overflow-x-hidden">
      {/* Subtle ambient */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -top-60 -left-60 size-[600px] rounded-full bg-emerald-900/20 blur-3xl" />
        <div className="absolute top-1/2 -right-60 size-[400px] rounded-full bg-zinc-800/30 blur-3xl" />
      </div>

      {/* Internal warning bar */}
      <div className="bg-amber-950/60 border-b border-amber-500/20 px-6 py-2 text-center text-xs text-amber-400 font-mono">
        ⚠ This portal is for internal developer use only. Do not share this URL externally.
      </div>

      {/* Nav */}
      <header className="sticky top-0 z-40 backdrop-blur-xl bg-zinc-950/80 border-b border-white/5">
        <div className="max-w-5xl mx-auto px-6 md:px-10 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="size-6 rounded bg-gradient-to-br from-emerald-500 to-cyan-600 flex items-center justify-center">
              <ShieldCheck className="size-3.5 text-black" />
            </div>
            <span className="font-mono text-sm text-zinc-200">rootdeck</span>
            <Tag variant="green">{CURRENT_APP_VERSION}</Tag>
            <InternalBadge />
          </div>
          <nav className="hidden md:flex items-center gap-5 text-xs font-mono text-zinc-500">
            <a href="#overview" className="hover:text-zinc-200">overview</a>
            <a href="#features" className="hover:text-zinc-200">features</a>
            <a href="#build" className="hover:text-zinc-200">build</a>
            <a href="#policy" className="hover:text-zinc-200">policy</a>
            <a href="#changelog" className="hover:text-zinc-200">changelog</a>
          </nav>
          <a
            href={GITHUB_ACTIONS_URL}
            target="_blank"
            rel="noreferrer"
            className="hidden md:inline-flex items-center gap-1.5 rounded border border-white/10 bg-white/5 hover:bg-white/10 px-3 py-1.5 text-xs text-zinc-300"
          >
            <Github className="size-3.5" /> GitHub
          </a>
        </div>
      </header>

      {/* Overview / Hero */}
      <Section id="overview" className="pt-10 md:pt-16">
        <div className="grid md:grid-cols-2 gap-10 items-center">
          <div>
            <SectionLabel>project overview</SectionLabel>
            <h1 className="tracking-tight text-zinc-100" style={{ fontSize: "clamp(2rem, 5vw, 3.5rem)", lineHeight: 1.1 }}>
              RootDeck
            </h1>
            <p className="mt-2 text-zinc-400 font-mono text-sm">
              Android 14+ root management companion · debug build
            </p>
            <p className="mt-5 text-zinc-400 text-sm leading-relaxed max-w-lg">
              Internal developer dashboard for the RootDeck Android app. Track feature status,
              download debug builds, review the safety policy, and follow the changelog.
            </p>

            <div className="mt-6 rounded-lg border border-white/10 bg-zinc-900/50 divide-y divide-white/5 text-sm font-mono">
              {[
                ["Package", "com.rootdeck.app"],
                ["minSdk", "34 (Android 14)"],
                ["Build type", "debug"],
                ["Root required", "Magisk / KernelSU / APatch"],
                ["Policy", `v${POLICY_VERSION} · ${POLICY_LAST_UPDATED}`],
              ].map(([k, v]) => (
                <div key={k} className="flex gap-4 px-4 py-2">
                  <span className="text-zinc-500 w-28 shrink-0">{k}</span>
                  <span className="text-zinc-200">{v}</span>
                </div>
              ))}
            </div>

            <div className="mt-5 flex flex-wrap gap-2 items-center">
              <DownloadButton />
              <a
                href={GITHUB_ACTIONS_URL}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2 rounded border border-white/10 bg-zinc-900 hover:bg-zinc-800 px-4 py-2.5 text-sm text-zinc-300"
              >
                <Github className="size-4" /> CI Build
              </a>
            </div>
            <p className="mt-2 text-xs text-zinc-500 font-mono">
              Sideload only on your own test device after reviewing source.
            </p>
          </div>

          <div className="flex justify-center opacity-80">
            <PhoneMockup />
          </div>
        </div>
      </Section>

      <Divider />

      {/* Implemented features */}
      <Section id="features">
        <SectionLabel>implemented features</SectionLabel>
        <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
          Feature inventory
        </h2>
        <p className="mt-2 text-sm text-zinc-500">
          All features in the current debug build. Click version badge to see changelog.
        </p>
        <div className="mt-8 grid md:grid-cols-2 gap-3">
          {features.map((f) => (
            <div key={f.title} className="rounded-lg border border-white/[0.07] bg-zinc-900/40 p-4 flex gap-3">
              <div className="size-8 rounded bg-zinc-800 border border-white/10 flex items-center justify-center shrink-0">
                <f.icon className="size-4 text-emerald-400" />
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-sm text-zinc-100">{f.title}</span>
                  <VersionBadge version={f.addedIn} />
                </div>
                <p className="mt-1 text-xs text-zinc-500 leading-relaxed">{f.body}</p>
              </div>
            </div>
          ))}
        </div>
      </Section>

      <Divider />

      {/* How root works internally */}
      <Section id="how">
        <SectionLabel>execution model</SectionLabel>
        <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
          Root shell flow
        </h2>
        <p className="mt-2 text-sm text-zinc-500">
          RootDeck never escalates privileges on its own — it asks the installed root manager for a shell.
        </p>
        <ol className="mt-6 space-y-2 max-w-2xl">
          {[
            ["User initiates action", "Each button shows the exact command and a risk label before anything runs."],
            ["Confirmation dialog", "RootDeck shows a modal. No confirm → no command. No exceptions."],
            ["su executes locally", "Root manager (Magisk / KernelSU / APatch) authorizes via its own prompt."],
            ["Result captured", "stdout, stderr, exit code, and timestamp land in Logs tab — permanent record."],
          ].map(([t, b], i) => (
            <li key={t} className="flex gap-3 rounded-lg border border-white/[0.07] bg-zinc-900/40 p-4">
              <div className="size-6 shrink-0 rounded bg-emerald-500/20 border border-emerald-500/30 text-emerald-400 text-xs font-mono flex items-center justify-center">{i + 1}</div>
              <div>
                <div className="text-sm text-zinc-100">{t}</div>
                <div className="text-xs text-zinc-500 mt-0.5">{b}</div>
              </div>
            </li>
          ))}
        </ol>
      </Section>

      <Divider />

      {/* Build & download */}
      <Section id="build">
        <SectionLabel>build & distribution</SectionLabel>
        <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
          Debug build pipeline
        </h2>

        <div className="mt-6 grid md:grid-cols-2 gap-4">
          {/* Build info */}
          <div className="rounded-lg border border-white/[0.07] bg-zinc-900/40 p-5">
            <div className="flex items-center gap-2 text-sm text-zinc-300 mb-3">
              <FlaskConical className="size-4 text-emerald-400" />
              APK artifact details
            </div>
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1.5 text-xs font-mono">
              {[
                ["Build", "GitHub Actions"],
                ["Workflow", "Build RootDeck APK"],
                ["Release", "RootDeck Test APK"],
                ["Artifact", "rootdeck-debug.apk"],
                ["Version", CURRENT_APP_VERSION],
              ].map(([k, v]) => (
                <div key={k} className="contents">
                  <dt className="text-zinc-500">{k}</dt>
                  <dd className="text-zinc-200">{v}</dd>
                </div>
              ))}
            </dl>
            <div className="mt-4 flex gap-2">
              <DownloadButton />
            </div>
          </div>

          {/* Build steps */}
          <div className="rounded-lg border border-white/[0.07] bg-zinc-900/40 p-5">
            <div className="flex items-center gap-2 text-sm text-zinc-300 mb-3">
              <Wrench className="size-4 text-emerald-400" />
              Build locally
            </div>
            <ol className="space-y-2 text-xs text-zinc-400 list-decimal list-inside">
              <li>Open <code className="text-emerald-400">/android</code> in Android Studio (Iguana+) or CLI.</li>
              <li>Bootstrap Gradle: <code className="text-emerald-400 break-all">cd android && gradle wrapper --gradle-version 8.7</code></li>
              <li>Run: <code className="text-emerald-400">bash scripts/build-debug-apk.sh</code></li>
              <li>Output: <code className="text-emerald-400 break-all">public/downloads/rootdeck-debug.apk</code></li>
            </ol>
            <pre className="mt-4 rounded bg-zinc-950 border border-white/10 p-3 text-xs text-emerald-400 overflow-x-auto">
{`$ bash scripts/build-debug-apk.sh

BUILD SUCCESSFUL
→ public/downloads/rootdeck-debug.apk`}
            </pre>
          </div>
        </div>

        {/* Internal dev checklist */}
        <div className="mt-4 rounded-lg border border-white/[0.07] bg-zinc-900/40 p-5">
          <div className="flex items-center gap-2 text-sm text-zinc-300 mb-3">
            <CheckCircle2 className="size-4 text-emerald-400" />
            Dev build checklist
          </div>
          <ul className="grid md:grid-cols-2 gap-2 text-xs text-zinc-400">
            {[
              "GitHub workflow has a green checkmark",
              "Artifact rootdeck-debug-apk exists",
              "Release RootDeck Test APK exists",
              "Release asset rootdeck-debug.apk exists",
              "APK installs cleanly on Android 14+",
              "Root Management screen can request root",
              "Logs capture root check result",
              "No crash on cold start",
            ].map((item) => (
              <li key={item} className="flex items-start gap-2">
                <CheckCircle2 className="size-3.5 text-emerald-500/60 mt-0.5 shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
      </Section>

      <Divider />

      {/* Tech stack */}
      <Section id="tech">
        <SectionLabel>technical reference</SectionLabel>
        <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
          Stack & architecture
        </h2>
        <div className="mt-6 grid md:grid-cols-2 gap-3">
          {[
            ["Language", "Kotlin"],
            ["UI framework", "Jetpack Compose + Material 3"],
            ["Package", "com.rootdeck.app"],
            ["minSdk", "34 · targetSdk 34"],
            ["Permissions", "No INTERNET — fully local"],
            ["Root shell", "RootShell.kt — su with timeout + stdout/stderr capture"],
            ["State", "Compose state holders, no third-party DI"],
            ["Build", "Gradle 8.7 · GitHub Actions CI"],
          ].map(([k, v]) => (
            <div key={k} className="rounded-lg border border-white/[0.07] bg-zinc-900/40 px-4 py-3 flex gap-3 font-mono text-xs">
              <span className="text-zinc-500 w-28 shrink-0">{k}</span>
              <span className="text-zinc-200">{v}</span>
            </div>
          ))}
        </div>
      </Section>

      <Divider />

      {/* Safety policy — single source of truth */}
      <Section id="policy">
        <SectionLabel>safety policy · centralized source of truth</SectionLabel>
        <div className="flex flex-wrap items-baseline gap-3">
          <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
            Policy v{POLICY_VERSION}
          </h2>
          <Tag variant="default">last updated {POLICY_LAST_UPDATED}</Tag>
        </div>

        <div className="mt-2 rounded border border-amber-500/20 bg-amber-950/30 p-3 text-xs text-amber-400 font-mono">
          Source of truth: <code className="text-amber-300">src/app/config/policy.ts</code> — do not duplicate safety claims anywhere else. Android app references via <code className="text-amber-300">SafetyPolicyLink.kt</code>.
        </div>

        {/* Summary */}
        <div className="mt-4 rounded-lg border border-white/[0.07] bg-zinc-900/40 p-5">
          <div className="flex items-center gap-2 text-xs font-mono text-zinc-400 mb-2">
            <FileText className="size-3.5 text-zinc-500" /> summary
          </div>
          <p className="text-sm text-zinc-300 leading-relaxed">{POLICY_SUMMARY}</p>
        </div>

        {/* Principles */}
        <div className="mt-4">
          <div className="text-xs font-mono text-zinc-500 mb-2">principles ({POLICY_PRINCIPLES.length})</div>
          <div className="grid gap-2 md:grid-cols-2">
            {POLICY_PRINCIPLES.map((p) => (
              <div key={p.title} className="rounded-lg border border-white/[0.07] bg-zinc-900/40 p-4">
                <div className="flex items-start gap-2">
                  <ShieldCheck className="size-4 text-emerald-400 mt-0.5 shrink-0" />
                  <div>
                    <div className="text-sm text-zinc-100">{p.title}</div>
                    <p className="mt-1 text-xs text-zinc-500 leading-relaxed">{p.body}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Prohibited */}
        <div className="mt-4">
          <div className="text-xs font-mono text-zinc-500 mb-2">
            prohibited — hard limits ({POLICY_PROHIBITED.length})
          </div>
          <div className="rounded-lg border border-red-500/20 bg-red-950/20 p-4">
            <ul className="space-y-2">
              {POLICY_PROHIBITED.map((item) => (
                <li key={item} className="flex items-start gap-2 text-sm">
                  <XCircle className="size-4 text-red-400 mt-0.5 shrink-0" />
                  <span className="text-zinc-300">{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Allowed */}
        <div className="mt-4">
          <div className="text-xs font-mono text-zinc-500 mb-2">
            allowed ({POLICY_ALLOWED.length} items)
          </div>
          <div className="rounded-lg border border-emerald-500/20 bg-emerald-950/10 p-4">
            <ul className="grid md:grid-cols-2 gap-x-6 gap-y-2">
              {POLICY_ALLOWED.map((item) => (
                <li key={item} className="flex items-start gap-2 text-xs text-zinc-400">
                  <CheckCircle2 className="size-3.5 text-emerald-500 mt-0.5 shrink-0" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <p className="mt-4 text-xs text-zinc-600 font-mono">
          To update the policy: edit <code className="text-zinc-400">src/app/config/policy.ts</code> only.
          The website and Android app both derive from this file.
        </p>
      </Section>

      <Divider />

      {/* Changelog */}
      <Section id="changelog">
        <SectionLabel>release history</SectionLabel>
        <div className="flex items-center gap-3">
          <h2 className="tracking-tight" style={{ fontSize: "clamp(1.3rem, 2.5vw, 1.8rem)" }}>
            Changelog
          </h2>
          <Tag variant="default">version.ts</Tag>
        </div>
        <p className="mt-1 text-xs text-zinc-500 font-mono">
          Add new entries to <code className="text-zinc-400">src/app/config/version.ts</code>.
        </p>
        <ol className="mt-6 space-y-3 max-w-3xl">
          {RELEASES.map((r, i) => (
            <li key={r.version} className="rounded-lg border border-white/[0.07] bg-zinc-900/40 p-5">
              <div className="flex flex-wrap items-center gap-2 mb-3">
                <Tag variant="green">{r.version}</Tag>
                <span className="text-sm text-zinc-200">{r.name}</span>
                <span className="text-xs text-zinc-500 font-mono">{r.date}</span>
                {i === 0 && <Tag variant="amber">current</Tag>}
              </div>
              <ul className="space-y-1.5">
                {r.notes.map((n) => (
                  <li key={n} className="flex gap-2 text-xs text-zinc-400">
                    <GitBranch className="size-3.5 text-emerald-500/60 mt-0.5 shrink-0" />
                    <span>{n}</span>
                  </li>
                ))}
              </ul>
            </li>
          ))}
        </ol>
      </Section>

      <footer className="border-t border-white/5 px-6 md:px-10 py-8">
        <div className="max-w-5xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <div className="size-5 rounded bg-gradient-to-br from-emerald-500 to-cyan-600 flex items-center justify-center">
              <ShieldCheck className="size-3 text-black" />
            </div>
            <span className="text-xs font-mono text-zinc-500">RootDeck {CURRENT_APP_VERSION} · internal dev portal</span>
          </div>
          <InternalBadge />
        </div>
      </footer>
    </div>
  );
}
