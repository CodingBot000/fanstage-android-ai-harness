#!/usr/bin/env python3
from pathlib import Path
import argparse,csv
from perfetto.trace_processor import TraceProcessor
p=argparse.ArgumentParser();p.add_argument('raw',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
files=sorted(f for f in a.raw.rglob('*.perfetto-trace') if ('ChatScrollBenchmark_scrollWithPlayingFixture_' in f.name or 'ChatScrollControlBenchmark_scrollWithPausedPlayer_' in f.name))
if not files:raise SystemExit('no control traces')
q="""with m as (select ts,dur from slice where name='measureBlock' and dur>0 order by dur desc limit 1) select coalesce(a.jank_type,'unknown') j,count(*) n from actual_frame_timeline_slice a join process p on p.upid=a.upid cross join m where p.name='dev.fanstage.android.benchmark' and a.ts<m.ts+m.dur and a.ts+a.dur>m.ts group by 1 order by 1"""
with a.output.open('x',newline='') as out:
 w=csv.writer(out);w.writerow(['scenario','iteration','category','count'])
 for f in files:
  scenario='paused' if 'Control' in f.name else 'playing';i=f.name.split('_iter')[1].split('_')[0]
  with TraceProcessor(trace=str(f)) as tp:
   for r in tp.query(q):w.writerow([scenario,i,r.j,r.n])
print('PASS: local measurement-window frame categories')
