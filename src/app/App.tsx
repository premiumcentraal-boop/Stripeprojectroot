import type { ReactNode } from "react";
import { GITHUB_APK_URL, LSPOSED_APK_URL } from "./config/download";
import {
  Activity,
  AlertTriangle,
  ArchiveRestore,
  Bug,
  Check,
  ChevronRight,
  Code2,
  Download,
  FileText,
  Globe2,
  Home,
  Languages,
  Layers3,
  ListChecks,
  Moon,
  PackageCheck,
  Palette,
  Search,
  Settings,
  Shield,
  Smartphone,
  Sparkles,
  TerminalSquare,
} from "lucide-react";

const navItems = [
  { label: "Home", icon: Home, active: true },
  { label: "Modules", icon: Layers3 },
  { label: "Repository", icon: Download },
  { label: "Logs", icon: FileText },
  { label: "Settings", icon: Settings },
];

const modules = [
  { name: "RootDeck Core", packageName: "com.rootdeck.app", state: "Enabled", scope: "3 scoped apps", color: "bg-blue-500" },
  { name: "Video Test Feed", packageName: "com.rootdeck.app.xposed.video", state: "Needs scope", scope: "Front + Back camera test", color: "bg-cyan-400" },
  { name: "Doppelganger", packageName: "com.rootdeck.app.xposed.clone", state: "Partial", scope: "System Framework", color: "bg-violet-500" },
];

const repoCards = [
  { title: "RootDeck LSPosed Bridge", meta: "Installed · v0.7.0", body: "Module metadata, scope checks, and safe status reporting for rooted test devices." },
  { title: "Camera Test Harness", meta: "Prototype", body: "Explicit front/back test flow with source selection and visible activation state." },
  { title: "Scope Audit Tools", meta: "Planned", body: "Compare requested module scopes with LSPosed Manager configuration before running tests." },
];

const settings = [
  { icon: Globe2, label: "DNS over HTTPS", value: "Off" },
  { icon: Languages, label: "Language", value: "System" },
  { icon: Palette, label: "Theme color", value: "Blue" },
  { icon: Moon, label: "Dark theme", value: "Follow system" },
  { icon: Shield, label: "Xposed API call protection", value: "On" },
  { icon: ArchiveRestore, label: "Backup and restore", value: "Ready" },
];

function Pill({ children, tone = "blue" }: { children: ReactNode; tone?: "blue" | "green" | "amber" | "slate" }) {
  const tones = {
    blue: "border-blue-400/30 bg-blue-500/10 text-blue-200",
    green: "border-emerald-400/30 bg-emerald-500/10 text-emerald-200",
    amber: "border-amber-400/30 bg-amber-500/10 text-amber-200",
    slate: "border-slate-500/30 bg-slate-500/10 text-slate-300",
  };
  return <span className={`rounded-full border px-3 py-1 text-xs font-medium ${tones[tone]}`}>{children}</span>;
}

function PhoneShell() {
  return (
    <div className="mx-auto w-full max-w-[390px] rounded-[2.4rem] border border-slate-700 bg-slate-950 p-3 shadow-2xl shadow-blue-950/40">
      <div className="overflow-hidden rounded-[1.8rem] border border-white/10 bg-[#101621]">
        <div className="flex items-center justify-between bg-[#151d2b] px-5 py-4">
          <div>
            <div className="text-xs text-slate-400">LSPosed</div>
            <div className="font-semibold text-white">Manager Prototype</div>
          </div>
          <Search className="size-5 text-slate-300" />
        </div>

        <div className="grid grid-cols-[74px_1fr] min-h-[650px]">
          <aside className="border-r border-white/10 bg-[#121a27] px-2 py-4">
            <div className="space-y-2">
              {navItems.map((item) => (
                <div key={item.label} className={`flex flex-col items-center gap-1 rounded-2xl px-2 py-3 text-[10px] ${item.active ? "bg-blue-500 text-white" : "text-slate-400"}`}>
                  <item.icon className="size-5" />
                  {item.label}
                </div>
              ))}
            </div>
          </aside>

          <main className="space-y-4 p-4 text-white">
            <section className="rounded-3xl border border-emerald-400/20 bg-emerald-400/10 p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-xs uppercase tracking-widest text-emerald-200/80">Framework</div>
                  <div className="mt-1 text-xl font-bold">Activated</div>
                </div>
                <div className="rounded-2xl bg-emerald-400 p-3 text-slate-950">
                  <Check className="size-6" />
                </div>
              </div>
              <div className="mt-4 grid grid-cols-2 gap-2 text-xs">
                <div className="rounded-2xl bg-black/20 p-3"><span className="text-slate-400">API</span><br />93</div>
                <div className="rounded-2xl bg-black/20 p-3"><span className="text-slate-400">SELinux</span><br />Loaded</div>
              </div>
            </section>

            <section className="rounded-3xl border border-white/10 bg-white/[0.04] p-4">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="font-semibold">Modules</h3>
                <Pill tone="green">2 enabled</Pill>
              </div>
              <div className="space-y-3">
                {modules.map((module) => (
                  <div key={module.name} className="rounded-2xl bg-slate-900/80 p-3">
                    <div className="flex items-start gap-3">
                      <div className={`mt-1 size-9 rounded-2xl ${module.color}`} />
                      <div className="min-w-0 flex-1">
                        <div className="font-medium">{module.name}</div>
                        <div className="truncate text-[11px] text-slate-500">{module.packageName}</div>
                        <div className="mt-2 flex flex-wrap gap-1.5">
                          <Pill tone={module.state === "Enabled" ? "green" : module.state === "Partial" ? "amber" : "slate"}>{module.state}</Pill>
                          <Pill tone="slate">{module.scope}</Pill>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </main>
        </div>
      </div>
    </div>
  );
}

function Section({ eyebrow, title, children }: { eyebrow: string; title: string; children: ReactNode }) {
  return (
    <section className="mx-auto max-w-6xl px-6 py-14 md:px-10">
      <div className="mb-7">
        <div className="mb-2 font-mono text-xs uppercase tracking-[0.24em] text-blue-300">{eyebrow}</div>
        <h2 className="max-w-3xl text-3xl font-bold tracking-tight text-white md:text-5xl">{title}</h2>
      </div>
      {children}
    </section>
  );
}

export default function App() {
  return (
    <div className="min-h-screen bg-[#0b1019] font-['Inter'] text-slate-100">
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -left-32 -top-32 size-96 rounded-full bg-blue-600/20 blur-3xl" />
        <div className="absolute right-0 top-1/3 size-[34rem] rounded-full bg-cyan-500/10 blur-3xl" />
        <div className="absolute bottom-0 left-1/3 size-80 rounded-full bg-violet-600/10 blur-3xl" />
      </div>

      <header className="sticky top-0 z-40 border-b border-white/10 bg-[#0b1019]/80 backdrop-blur-xl">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6 md:px-10">
          <div className="flex items-center gap-3">
            <div className="grid size-9 place-items-center rounded-2xl bg-blue-500 text-white shadow-lg shadow-blue-500/25">
              <Sparkles className="size-5" />
            </div>
            <div>
              <div className="font-semibold leading-tight">RootDeck × LSPosed</div>
              <div className="text-xs text-slate-500">Manager-inspired prototype</div>
            </div>
          </div>
          <div className="hidden items-center gap-2 md:flex">
            <Pill tone="green">source context extracted</Pill>
            <Pill tone="blue">Figma Make prototype</Pill>
          </div>
        </div>
      </header>

      <main className="relative">
        <section className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-16 md:grid-cols-[1fr_420px] md:px-10 md:py-20">
          <div>
            <div className="mb-5 flex flex-wrap gap-2">
              <Pill tone="blue">Android / Gradle source context</Pill>
              <Pill tone="slate">No web-app execution</Pill>
            </div>
            <h1 className="text-5xl font-extrabold tracking-[-0.04em] text-white md:text-7xl">
              LSPosed product flow, recreated for RootDeck.
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
              This prototype distills the LSPosed Manager structure into a RootDeck-friendly experience: status-first home, module scope review, repository cards, verbose/module logs, and Material-style settings.
            </p>
            <div className="mt-8 grid gap-3 sm:grid-cols-3">
              {["Framework status", "Module scopes", "Settings + logs"].map((item) => (
                <div key={item} className="rounded-3xl border border-white/10 bg-white/[0.04] p-4 text-sm text-slate-300">
                  <Check className="mb-3 size-5 text-emerald-300" />
                  {item}
                </div>
              ))}
            </div>

            <div className="mt-8 grid gap-4 md:grid-cols-2">
              <a
                href={GITHUB_APK_URL}
                className="group rounded-[1.75rem] border border-emerald-400/25 bg-emerald-400/10 p-5 transition hover:border-emerald-300/60 hover:bg-emerald-400/15"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-sm font-semibold text-emerald-100">RootDeck APK download</div>
                    <p className="mt-2 text-sm leading-6 text-slate-400">
                      Main RootDeck Android 14+ app build from the stable RootDeck release asset.
                    </p>
                  </div>
                  <Download className="size-5 shrink-0 text-emerald-300 transition group-hover:translate-y-0.5" />
                </div>
                <div className="mt-4 font-mono text-xs text-emerald-200/80">rootdeck-debug.apk</div>
              </a>

              <a
                href={LSPOSED_APK_URL}
                className="group rounded-[1.75rem] border border-blue-400/25 bg-blue-500/10 p-5 transition hover:border-blue-300/60 hover:bg-blue-500/15"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-sm font-semibold text-blue-100">LSPosed Suite APK download</div>
                    <p className="mt-2 text-sm leading-6 text-slate-400">
                      Latest successful standalone LSPosed-master debug APK from the lsposed-latest release.
                    </p>
                  </div>
                  <Download className="size-5 shrink-0 text-blue-300 transition group-hover:translate-y-0.5" />
                </div>
                <div className="mt-4 font-mono text-xs text-blue-200/80">lsposed-debug.apk</div>
              </a>
            </div>
          </div>
          <PhoneShell />
        </section>

        <Section eyebrow="source summary" title="What I found in LSPosed-master">
          <div className="grid gap-4 md:grid-cols-3">
            {[
              { icon: Code2, title: "Multi-module Gradle project", body: "Top-level Android/Gradle structure includes app, core, daemon, services, hiddenapi, magisk-loader, dex2oat, and external native components." },
              { icon: Smartphone, title: "Manager app resources", body: "The app module contains XML layouts for home, modules/app list, repository, settings, logs, about dialogs, and responsive tablet navigation." },
              { icon: ListChecks, title: "Navigation model", body: "Main navigation centers on Home, Modules, Repository, Logs, and Settings with separate nested module/repo navigation graphs." },
              { icon: Palette, title: "Material theming", body: "Colors, styles, night variants, custom theme overlays, icon drawables, checkable nav icons, and webview markdown themes shape the visual system." },
              { icon: Settings, title: "Settings taxonomy", body: "Language, theme color, dark mode, pure black theme, verbose logs, Xposed API protection, shortcuts, notifications, updates, backup/restore." },
              { icon: Bug, title: "Operational flows", body: "Verbose logs, module logs, issue reporting, update channels, repository readmes/releases, scope selection, and activation warnings." },
            ].map((card) => (
              <article key={card.title} className="rounded-[1.75rem] border border-white/10 bg-white/[0.04] p-5">
                <card.icon className="mb-4 size-6 text-blue-300" />
                <h3 className="font-semibold text-white">{card.title}</h3>
                <p className="mt-2 text-sm leading-6 text-slate-400">{card.body}</p>
              </article>
            ))}
          </div>
        </Section>

        <Section eyebrow="prototype flow" title="RootDeck screens adapted from LSPosed patterns">
          <div className="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
            <div className="rounded-[2rem] border border-white/10 bg-white/[0.04] p-5">
              <h3 className="mb-4 text-xl font-semibold">Home dashboard</h3>
              <div className="space-y-3">
                {[
                  [Activity, "Framework status", "Activated · API 93 · SELinux loaded"],
                  [PackageCheck, "Enabled modules", "RootDeck Core, Video Test Feed, Doppelganger"],
                  [AlertTriangle, "Action required", "Select scope before camera test module can run"],
                ].map(([Icon, title, body]) => {
                  const TypedIcon = Icon as typeof Activity;
                  return (
                    <div key={title as string} className="flex items-center gap-4 rounded-3xl bg-slate-950/60 p-4">
                      <div className="grid size-11 place-items-center rounded-2xl bg-blue-500/15 text-blue-300"><TypedIcon className="size-5" /></div>
                      <div>
                        <div className="font-medium text-white">{title as string}</div>
                        <div className="text-sm text-slate-500">{body as string}</div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="rounded-[2rem] border border-white/10 bg-white/[0.04] p-5">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-xl font-semibold">Video Test Feed scope setup</h3>
                <Pill tone="amber">needs review</Pill>
              </div>
              <div className="grid gap-3 md:grid-cols-2">
                {[
                  ["1", "Pick video source", "Gallery or Files content URI"],
                  ["2", "Choose target", "Front camera or back camera test path"],
                  ["3", "Confirm scope", "Only selected user-owned test apps"],
                  ["4", "Read logs", "Verbose and module logs for diagnosis"],
                ].map(([n, title, body]) => (
                  <div key={n} className="rounded-3xl bg-slate-950/60 p-4">
                    <div className="mb-3 grid size-8 place-items-center rounded-xl bg-blue-500 text-sm font-bold">{n}</div>
                    <div className="font-medium text-white">{title}</div>
                    <div className="text-sm text-slate-500">{body}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Section>

        <Section eyebrow="repository" title="Repository and release cards">
          <div className="grid gap-4 md:grid-cols-3">
            {repoCards.map((card) => (
              <article key={card.title} className="rounded-[1.75rem] border border-white/10 bg-slate-950/50 p-5">
                <div className="mb-4 flex items-start justify-between gap-3">
                  <Download className="size-5 text-blue-300" />
                  <Pill tone="slate">{card.meta}</Pill>
                </div>
                <h3 className="font-semibold text-white">{card.title}</h3>
                <p className="mt-2 text-sm leading-6 text-slate-400">{card.body}</p>
                <button className="mt-5 inline-flex items-center gap-2 text-sm font-medium text-blue-300">
                  View details <ChevronRight className="size-4" />
                </button>
              </article>
            ))}
          </div>
        </Section>

        <Section eyebrow="settings" title="Settings model extracted from LSPosed Manager">
          <div className="rounded-[2rem] border border-white/10 bg-white/[0.04] p-3">
            <div className="grid gap-2 md:grid-cols-2">
              {settings.map((setting) => (
                <div key={setting.label} className="flex items-center justify-between rounded-3xl bg-slate-950/60 p-4">
                  <div className="flex items-center gap-3">
                    <div className="grid size-10 place-items-center rounded-2xl bg-white/5 text-slate-300">
                      <setting.icon className="size-5" />
                    </div>
                    <div className="font-medium text-white">{setting.label}</div>
                  </div>
                  <div className="text-sm text-slate-500">{setting.value}</div>
                </div>
              ))}
            </div>
          </div>
        </Section>

        <Section eyebrow="logs" title="Diagnostic experience">
          <div className="rounded-[2rem] border border-white/10 bg-[#05070c] p-5 font-['JetBrains_Mono'] text-sm shadow-2xl shadow-black/30">
            <div className="mb-4 flex items-center gap-2 text-slate-400">
              <TerminalSquare className="size-4" /> Verbose Logs / Modules Logs
            </div>
            {[
              "[20:32:47] framework: activated, api=93, selinux=loaded",
              "[20:32:48] module: com.rootdeck.app packaged in xposed_init",
              "[20:32:49] video-feed: source selected content://media/video/42",
              "[20:32:50] scope: waiting for LSPosed Manager confirmation",
            ].map((line) => (
              <div key={line} className="border-t border-white/5 py-2 text-emerald-300">{line}</div>
            ))}
          </div>
        </Section>

        <footer className="border-t border-white/10 px-6 py-8 text-center text-sm text-slate-500">
          Source context: <span className="text-slate-300">premiumcentraal-boop/Stripeprojectroot/LSPosed-master</span> · Prototype only, not a Gradle build runner.
        </footer>
      </main>
    </div>
  );
}
