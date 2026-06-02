import { ShieldCheck, Cpu, HardDrive, Activity, Terminal, Smartphone } from "lucide-react";

export function PhoneMockup() {
  return (
    <div className="relative mx-auto w-[300px] h-[620px] rounded-[44px] border border-white/10 bg-gradient-to-b from-zinc-900 to-black p-3 shadow-2xl shadow-emerald-500/10">
      <div className="absolute top-3 left-1/2 -translate-x-1/2 w-28 h-6 rounded-b-2xl bg-black z-10" />
      <div className="h-full w-full rounded-[34px] bg-zinc-950 overflow-hidden flex flex-col">
        <div className="px-5 pt-10 pb-3 flex items-center justify-between">
          <span className="text-xs text-zinc-400">RootDeck</span>
          <span className="text-xs text-emerald-400">● online</span>
        </div>

        <div className="px-4 space-y-3 flex-1 overflow-hidden">
          <div className="rounded-2xl border border-emerald-500/30 bg-emerald-500/5 p-4">
            <div className="flex items-center gap-2 text-emerald-400">
              <ShieldCheck className="size-5" />
              <span className="text-sm">Root active</span>
            </div>
            <div className="mt-1 text-xs text-zinc-400">Provider: Magisk · uid=0</div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="rounded-xl border border-white/5 bg-white/5 p-3">
              <div className="flex items-center gap-1 text-zinc-400 text-xs"><Smartphone className="size-3" /> Android</div>
              <div className="text-zinc-100 mt-1 text-sm">14 · API 34</div>
            </div>
            <div className="rounded-xl border border-white/5 bg-white/5 p-3">
              <div className="flex items-center gap-1 text-zinc-400 text-xs"><Cpu className="size-3" /> CPU</div>
              <div className="text-zinc-100 mt-1 text-sm">23% · 8 cores</div>
            </div>
            <div className="rounded-xl border border-white/5 bg-white/5 p-3">
              <div className="flex items-center gap-1 text-zinc-400 text-xs"><Activity className="size-3" /> RAM</div>
              <div className="text-zinc-100 mt-1 text-sm">5.1 / 12 GB</div>
            </div>
            <div className="rounded-xl border border-white/5 bg-white/5 p-3">
              <div className="flex items-center gap-1 text-zinc-400 text-xs"><HardDrive className="size-3" /> Storage</div>
              <div className="text-zinc-100 mt-1 text-sm">142 / 256 GB</div>
            </div>
          </div>

          <div className="rounded-xl border border-white/5 bg-black/50 p-3">
            <div className="flex items-center gap-2 text-zinc-400 text-xs">
              <Terminal className="size-3" /> Last command
            </div>
            <pre className="mt-1 text-[10px] text-emerald-400 leading-snug">$ su -c id
uid=0(root) gid=0(root) groups=0(root)</pre>
          </div>

          <button className="w-full rounded-xl bg-emerald-500 text-black py-3 text-sm">
            Check Root Access
          </button>
        </div>

        <div className="border-t border-white/5 mt-3 px-2 py-3 grid grid-cols-5 text-[10px] text-zinc-500">
          {["Dashboard", "Tools", "Apps", "Logs", "Settings"].map((t, i) => (
            <div key={t} className={`text-center ${i === 0 ? "text-emerald-400" : ""}`}>{t}</div>
          ))}
        </div>
      </div>
    </div>
  );
}
