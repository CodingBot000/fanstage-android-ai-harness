import csv,sys
from pathlib import Path
from perfetto.trace_processor import TraceProcessor
root=Path(sys.argv[1]);out=Path(sys.argv[2]);traces=sorted(root.rglob('*ActiveChatBenchmark_scrollWithIncomingMessages_iter*.perfetto-trace'))
q="""with m as (select ts,dur from slice where name='measureBlock' and dur>0 order by dur desc limit 1) select count(*) n,round(sum(max(0,min(s.ts+s.dur,m.ts+m.dur)-max(s.ts,m.ts)))/1e9,6) cpu,round(m.dur/1e9,6) wall from sched s join thread t on t.utid=s.utid join process p on p.upid=t.upid cross join m where p.name='dev.fanstage.android.benchmark' and s.dur>0 and s.ts<m.ts+m.dur and s.ts+s.dur>m.ts"""
if len(traces)!=10:raise SystemExit('expected ten traces')
with out.open('x',newline='') as f:
 w=csv.writer(f);w.writerow(['iteration','sched_slices','app_cpu_seconds','measure_wall_seconds','average_core_equivalents'])
 for t in traces:
  with TraceProcessor(trace=str(t)) as tp:
   row=next(iter(tp.query(q)));assert row.n and row.wall;w.writerow([t.name.split('_iter')[1].split('_')[0],row.n,row.cpu,row.wall,row.cpu/row.wall])
