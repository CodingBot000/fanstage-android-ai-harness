#!/usr/bin/env python3
from pathlib import Path
import argparse,csv,statistics
from perfetto.trace_processor import TraceProcessor
p=argparse.ArgumentParser();p.add_argument('raw',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
files=sorted(a.raw.rglob('ActiveChatBenchmark_scrollWithIncomingMessages_iter*.perfetto-trace'))
if len(files)!=10:raise SystemExit('expected exactly 10 active-chat traces')
with a.output.open('x',newline='') as out:
 w=csv.writer(out);w.writerow(['iteration','events','elapsed_seconds','first100_mean_ms','last100_mean_ms','all_mean_ms','all_median_ms'])
 for f in files:
  with TraceProcessor(trace=str(f)) as tp:
   rows=list(tp.query("select ts,dur from slice where name='M6ChatAccept' and dur>=0 order by ts"))
   if len(rows)!=500:raise ValueError('expected 500 event sections')
   vals=[r.dur/1e6 for r in rows]
   w.writerow([f.name.split('_iter')[1].split('_')[0],len(vals),(rows[-1].ts+rows[-1].dur-rows[0].ts)/1e9,statistics.mean(vals[:100]),statistics.mean(vals[-100:]),statistics.mean(vals),statistics.median(vals)])
print('PASS: 10 runs and 500 synchronous event sections each; timing excludes network and visible rendering')
