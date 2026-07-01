<template>
  <div v-if="visible" class="overlay">
    <!-- 背景光晕 -->
    <div class="bg-glow bg-glow--tl"></div>
    <div class="bg-glow bg-glow--br"></div>
    <div class="bg-grid"></div>

    <!-- 顶栏 -->
    <header class="topbar">
      <div class="topbar-left">
        <div class="topbar-logo">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="8" height="8" rx="2" fill="#FF6900"/>
            <rect x="13" y="3" width="8" height="8" rx="2" fill="#FF6900" opacity=".5"/>
            <rect x="3" y="13" width="8" height="8" rx="2" fill="#FF6900" opacity=".5"/>
            <rect x="13" y="13" width="8" height="8" rx="2" fill="#FF6900" opacity=".25"/>
          </svg>
        </div>
        <div class="topbar-info">
          <h1 class="topbar-title">{{ qrInfo.name }}</h1>
          <span class="topbar-code">{{ qrInfo.code }}</span>
        </div>
      </div>
      <button class="close-btn" @click="$emit('close')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </header>

    <!-- 主体 -->
    <main class="main">
      <template v-if="loading">
        <!-- 骨架屏 -->
        <section class="kpi-row">
          <div v-for="i in 3" :key="i" class="skeleton-card">
            <div class="skeleton-line w-24 mb-4"></div>
            <div class="skeleton-line w-32 h-10 mb-4"></div>
            <div class="skeleton-line w-full h-2"></div>
          </div>
        </section>
        <div class="grid-2">
          <div class="skeleton-card" style="height:380px">
            <div class="skeleton-line w-40 mb-6"></div>
            <div class="skeleton-block" style="height:280px"></div>
          </div>
          <div class="skeleton-card" style="height:380px">
            <div class="skeleton-line w-40 mb-6"></div>
            <div class="skeleton-block" style="height:280px"></div>
          </div>
        </div>
      </template>

      <template v-else>
        <!-- KPI 三卡片 -->
        <section class="kpi-row">
          <div class="kpi-card" v-for="kpi in kpiCards" :key="kpi.label">
            <div class="kpi-accent" :style="{ background: kpi.color }"></div>
            <div class="kpi-body">
              <div class="kpi-header">
                <div class="kpi-icon-wrap" :style="{ background: kpi.iconBg }">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" :stroke="kpi.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <template v-if="kpi.icon === 'pv'">
                      <rect x="3" y="12" width="4" height="9" rx="1"/>
                      <rect x="10" y="7" width="4" height="14" rx="1"/>
                      <rect x="17" y="3" width="4" height="18" rx="1"/>
                    </template>
                    <template v-else-if="kpi.icon === 'uv'">
                      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
                      <circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 00-3-3.87"/>
                      <path d="M16 3.13a4 4 0 010 7.75"/>
                    </template>
                    <template v-else>
                      <circle cx="12" cy="12" r="10"/>
                      <polyline points="12 6 12 12 16 14"/>
                    </template>
                  </svg>
                </div>
                <span class="kpi-label">{{ kpi.label }}</span>
              </div>
              <div class="kpi-value" :style="{ color: kpi.color }">{{ kpi.value }}</div>
              <div class="kpi-progress">
                <div class="kpi-bar"><div class="kpi-bar-fill" :style="{ width: kpi.pct + '%', background: kpi.gradient }"></div></div>
                <span class="kpi-pct">{{ kpi.pct }}%</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 双栏 -->
        <section class="grid-2">
          <div class="card">
            <div class="card-head">
              <span class="card-dot" style="background:#FF6900"></span>
              <span class="card-title">最近 7 天趋势</span>
            </div>
            <div class="card-body">
              <div v-if="stats.last7Days && stats.last7Days.length">
                <div class="chart-box">
                  <canvas ref="trendCanvas"></canvas>
                </div>
                <div class="day-strip">
                  <div v-for="d in stats.last7Days" :key="d.date" class="day-chip">
                    <span class="day-d">{{ fmtDay(d.date) }}</span>
                    <div class="day-bars">
                      <span class="day-barpv" :style="{ height: dayBarH(d.pv, 'pv') + 'px', background: 'linear-gradient(180deg,#FF6900,#fb923c)' }" :title="'PV: '+d.pv"></span>
                      <span class="day-baruv" :style="{ height: dayBarH(d.uv, 'uv') + 'px', background: 'linear-gradient(180deg,#10B981,#34D399)' }" :title="'UV: '+d.uv"></span>
                    </div>
                    <span class="day-pv">{{ d.pv }}</span>
                    <span class="day-uv">{{ d.uv }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无数据</div>
            </div>
          </div>

          <div class="card">
            <div class="card-head">
              <span class="card-dot" style="background:#8B5CF6"></span>
              <span class="card-title">24 小时访问分布</span>
            </div>
            <div class="card-body">
              <div v-if="hourStats.length">
                <div class="hour-grid">
                  <div v-for="h in 24" :key="h" class="hour-col">
                    <div class="hour-track">
                      <div class="hour-bar" :style="{ height: hourH(h-1)+'px', background: hourGradient(h-1) }" :title="((hourStats.find(function(x){return x.hour===h-1})||{}).count||0) + ' 次'"></div>
                    </div>
                    <span class="hour-lbl" v-if="(h-1)%4===0">{{ h-1 }}</span>
                  </div>
                </div>
                <div class="hour-foot" v-if="hourStats.length">
                  峰值 <em style="color:#FF6900;font-style:normal">{{ Math.max(...hourStats.map(x=>x.count)) }}</em> 次
                  <span class="hour-foot-dot"></span>
                  {{ hourStats.filter(x=>x.count>0).length }} 个时段活跃
                </div>
              </div>
              <div v-else class="empty">暂无数据</div>
            </div>
          </div>
        </section>

        <!-- 三栏排行 -->
        <section class="grid-3">
          <div class="card" v-for="p in panels" :key="p.title">
            <div class="card-head">
              <span class="card-dot" :style="{ background: p.dotColor }"></span>
              <span class="card-title">{{ p.title }}</span>
            </div>
            <div class="card-body">
              <div v-if="p.items.length" class="rank">
                <div v-for="(it,i) in p.items" :key="it[p.key]" class="rank-item">
                  <span class="rank-badge" v-html="medalIcon(i)"></span>
                  <span class="rank-name" :title="it[p.key]">{{ p.fmt ? p.fmt(it[p.key]) : it[p.key] }}</span>
                  <div class="rank-bar"><div class="rank-bar-fill" :style="{ width: pct(it.count, p.items[0].count), background: p.color }"></div></div>
                  <span class="rank-count">{{ it.count }}</span>
                </div>
              </div>
              <div v-else class="empty">暂无数据</div>
            </div>
          </div>
        </section>

        <!-- 地区 -->
        <div class="card" style="max-width:600px">
          <div class="card-head">
            <span class="card-dot" style="background:linear-gradient(90deg,#FF6900,#8B5CF6)"></span>
            <span class="card-title">地区分布 Top10</span>
          </div>
          <div class="card-body">
            <div v-if="locationStats.length" class="rank">
              <div v-for="(it,i) in locationStats" :key="it.location" class="rank-item">
                <span class="rank-badge" v-html="medalIcon(i)"></span>
                <span class="rank-name" :title="it.location">{{ it.location }}</span>
                <div class="rank-bar"><div class="rank-bar-fill" :style="{ width: pct(it.count, locationStats[0].count), background: 'linear-gradient(90deg,#FF6900,#8B5CF6)' }"></div></div>
                <span class="rank-count">{{ it.count }}</span>
              </div>
            </div>
            <div v-else class="empty">暂无数据</div>
          </div>
        </div>

        <!-- 最近访问 -->
        <div class="card" v-if="stats.latestVisits && stats.latestVisits.length">
          <div class="card-head">
            <span class="card-dot" style="background:#10B981"></span>
            <span class="card-title">最近访问记录</span>
            <span class="card-badge">{{ stats.latestVisits.length }} 条</span>
          </div>
          <div class="card-body" style="overflow-x:auto;padding:0">
            <table class="vtbl">
              <thead><tr>
                <th>时间</th><th>IP</th><th>地区</th><th>设备</th><th>浏览器</th><th>来源</th>
              </tr></thead>
              <tbody>
                <tr v-for="(v,i) in stats.latestVisits" :key="i">
                  <td class="cell-mono">{{ v.createdAt }}</td>
                  <td class="cell-mono cell-ip">{{ v.ip }}</td>
                  <td><span class="cell-loc-icon">📍</span> {{ v.location || '-' }}</td>
                  <td><span class="cell-device">{{ devIcon(v.device) }}</span> {{ v.device }}</td>
                  <td><span class="cell-browser">{{ brIcon(v.browser) }}</span> {{ v.browser }}</td>
                  <td class="cell-ref" :title="v.referer">{{ fmtRef(v.referer) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script>
import {
  Chart, CategoryScale, LinearScale, PointElement, LineElement, LineController,
  Tooltip, Legend, Filler
} from 'chart.js'
Chart.register(CategoryScale, LinearScale, PointElement, LineElement, LineController, Tooltip, Legend, Filler)

export default {
  name: 'DashboardOverlay',
  props: {
    visible: Boolean,
    qrInfo: { type: Object, default: () => ({}) },
    stats: { type: Object, default: () => ({}) },
    refererStats: { type: Array, default: () => [] },
    hourStats: { type: Array, default: () => [] },
    deviceStats: { type: Array, default: () => [] },
    browserStats: { type: Array, default: () => [] },
    locationStats: { type: Array, default: () => [] },
    loading: Boolean
  },
  data: () => ({ chart: null }),
  computed: {
    maxPv() { return Math.max(this.stats.pv || 0, 1) },
    kpiCards() {
      return [
        { label:'总访问 PV', value:this.stats.pv||0, icon:'pv', color:'#FF6900', iconBg:'rgba(255,105,0,0.15)', gradient:'linear-gradient(90deg,#FF6900,#fb923c)', pct: Math.min((this.stats.pv||0)/this.maxPv*100, 100) },
        { label:'累计 UV', value:this.stats.uv||0, icon:'uv', color:'#10B981', iconBg:'rgba(16,185,129,0.15)', gradient:'linear-gradient(90deg,#10B981,#34D399)', pct: Math.min((this.stats.uv||0)/this.maxPv*100, 100) },
        { label:'今日 UV', value:this.stats.todayUv||0, icon:'uv', color:'#8B5CF6', iconBg:'rgba(139,92,246,0.15)', gradient:'linear-gradient(90deg,#8B5CF6,#A78BFA)', pct: Math.min((this.stats.todayUv||0)/Math.max(this.stats.todayUv||0,1)*100, 100) },
      ]
    },
    panels() {
      return [
        { title:'访问来源 Top10', color:'linear-gradient(90deg,#FF6900,#fb923c)', dotColor:'#FF6900', key:'referer', items:this.refererStats, fmt:this.fmtRef },
        { title:'设备分布', color:'linear-gradient(90deg,#10b981,#34d399)', dotColor:'#10b981', key:'device', items:this.deviceStats },
        { title:'浏览器分布', color:'linear-gradient(90deg,#818cf8,#a78bfa)', dotColor:'#818cf8', key:'browser', items:this.browserStats },
      ]
    }
  },
  watch: {
    visible(v) {
      if(v){ document.body.style.overflow='hidden'; document.addEventListener('keydown',this.esc); this.$nextTick(()=>this.draw()) }
      else{ document.body.style.overflow=''; document.removeEventListener('keydown',this.esc); this.kill() }
    },
    stats:{ deep:true, handler(){ this.$nextTick(()=>this.draw()) } }
  },
  beforeDestroy(){ document.body.style.overflow=''; document.removeEventListener('keydown',this.esc); this.kill() },
  methods: {
    esc(e){ if(e.key==='Escape') this.$emit('close') },
    kill(){ if(this.chart){this.chart.destroy();this.chart=null} },
    draw() {
      if(!this.$refs.trendCanvas||!this.stats.last7Days||!this.stats.last7Days.length) return
      this.kill()
      const d=this.stats.last7Days
      this.chart = new Chart(this.$refs.trendCanvas, {
        type:'line',
        data:{
          labels:d.map(x=>this.fmtDay(x.date)),
          datasets:[
            { label:'PV', data:d.map(x=>x.pv), borderColor:'#FF6900', backgroundColor:'rgba(255,105,0,0.08)', borderWidth:2.5, pointRadius:4, pointHoverRadius:6, pointBackgroundColor:'#FF6900', pointBorderColor:'#1a1a24', pointBorderWidth:2, tension:.4, fill:true },
            { label:'UV', data:d.map(x=>x.uv), borderColor:'#10B981', backgroundColor:'rgba(16,185,129,0.08)', borderWidth:2.5, pointRadius:4, pointHoverRadius:6, pointBackgroundColor:'#10B981', pointBorderColor:'#1a1a24', pointBorderWidth:2, tension:.4, fill:true }
          ]
        },
        options:{
          responsive:true, maintainAspectRatio:false,
          interaction:{ intersect:false, mode:'index' },
          plugins:{
            legend:{ position:'top', align:'end', labels:{ usePointStyle:true, pointStyle:'circle', padding:16, font:{size:11,weight:'500'}, color:'#94a3b8', boxWidth:6 } },
            tooltip:{ backgroundColor:'rgba(15,23,42,0.95)', titleColor:'#f1f5f0', bodyColor:'#cbd5e1', padding:12, cornerRadius:12, titleFont:{size:12}, bodyFont:{size:12}, borderColor:'rgba(255,255,255,0.06)', borderWidth:1, boxPadding:4 }
          },
          scales:{
            x:{ grid:{display:false}, ticks:{font:{size:10},color:'#475569'} },
            y:{ beginAtZero:true, grid:{color:'rgba(148,163,184,0.05)',drawTicks:false}, ticks:{font:{size:10},color:'#475569',padding:8,precision:0}, border:{display:false} }
          }
        }
      })
    },
    pct(c,m){ return m? Math.round(c/m*100)+'%' : '0%' },
    hourH(h){ const it=this.hourStats.find(x=>x.hour===h); if(!it||!it.count) return 2; const mx=Math.max(...this.hourStats.map(x=>x.count)); return Math.round(it.count/mx*160)+2 },
    hourGradient(h) {
      const it = this.hourStats.find(x => x.hour === h)
      if (!it || !it.count) return 'linear-gradient(180deg,rgba(139,92,246,0.35),rgba(139,92,246,0.15))'
      const mx = Math.max(...this.hourStats.map(x => x.count))
      const r = it.count / mx
      if (r > 0.7) return 'linear-gradient(180deg,#FF6900,#fb923c)'
      if (r > 0.4) return 'linear-gradient(180deg,#8B5CF6,#A78BFA)'
      return 'linear-gradient(180deg,#6366f1,#818CF8)'
    },
    dayBarH(v, type) {
      const arr = this.stats.last7Days.map(x => type === 'pv' ? x.pv : x.uv)
      const mx = Math.max(...arr, 1)
      return Math.round(v / mx * 28) + 2
    },
    medalIcon(i) {
      if (i === 0) return '<svg class="medal" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="9" fill="#FFD700"/><circle cx="10" cy="10" r="5.5" fill="#FFF3B0" opacity=".35"/><path d="M10 5l1.5 3 3.2.3-2.4 2.2.7 3.2L10 12.5 7 13.7l.7-3.2L5.3 8.3l3.2-.3L10 5z" fill="#fff" opacity=".9"/></svg>'
      if (i === 1) return '<svg class="medal" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="9" fill="#C0C0C0"/><circle cx="10" cy="10" r="5.5" fill="#fff" opacity=".2"/><path d="M10 5l1.5 3 3.2.3-2.4 2.2.7 3.2L10 12.5 7 13.7l.7-3.2L5.3 8.3l3.2-.3L10 5z" fill="#fff" opacity=".8"/></svg>'
      if (i === 2) return '<svg class="medal" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="9" fill="#CD7F32"/><circle cx="10" cy="10" r="5.5" fill="#fff" opacity=".15"/><path d="M10 5l1.5 3 3.2.3-2.4 2.2.7 3.2L10 12.5 7 13.7l.7-3.2L5.3 8.3l3.2-.3L10 5z" fill="#fff" opacity=".8"/></svg>'
      return `<span class="rank-num">${i+1}</span>`
    },
    fmtRef(r){ if(!r||r==='direct') return '直接访问'; try{return new URL(r).hostname}catch(e){return r.substring(0,18)} },
    fmtDay(s){ if(!s)return''; const p=s.split('-'); return p.length>=3?p[1]+'/'+p[2]:s },
    devIcon(d){ if(!d)return'📱'; const l=d.toLowerCase(); if(l.includes('mobile')||l.includes('phone'))return'📱'; if(l.includes('tablet'))return'📋'; return'💻' },
    brIcon(b){ if(!b)return'🌐'; const l=b.toLowerCase(); if(l.includes('chrome'))return'🟡'; if(l.includes('safari'))return'🔵'; if(l.includes('firefox'))return'🟠'; if(l.includes('wechat'))return'💚'; return'🌐' }
  }
}
</script>

<style scoped>
/* ========== 全局 ========== */
.overlay {
  position:fixed; inset:0; z-index:9999; display:flex; flex-direction:column;
  background:#0a0a0f;
  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Hiragino Sans GB","Microsoft YaHei",sans-serif;
  overflow:hidden;
}

/* ========== 背景 ========== */
.bg-glow {
  position:fixed; width:600px; height:600px; border-radius:50%;
  pointer-events:none; z-index:0; opacity:.12;
}
.bg-glow--tl {
  top:-200px; left:-200px;
  background:radial-gradient(circle, #FF6900 0%, transparent 70%);
  animation: glowFloat 8s ease-in-out infinite alternate;
}
.bg-glow--br {
  bottom:-200px; right:-200px;
  background:radial-gradient(circle, #8B5CF6 0%, transparent 70%);
  animation: glowFloat 10s ease-in-out infinite alternate-reverse;
}
@keyframes glowFloat {
  0%{ transform:translate(0,0) scale(1) }
  100%{ transform:translate(30px,-30px) scale(1.1) }
}
.bg-grid {
  position:fixed; inset:0; z-index:0; pointer-events:none;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* ========== 入场动画 ========== */
@keyframes fadeUp {
  from{ opacity:0; transform:translateY(20px) }
  to{ opacity:1; transform:translateY(0) }
}
.kpi-card,
.grid-2 .card,
.grid-3 .card,
.card {
  animation: fadeUp .5s cubic-bezier(.22,1,.36,1) both;
}
.kpi-card:nth-child(1){ animation-delay:.05s }
.kpi-card:nth-child(2){ animation-delay:.1s }
.kpi-card:nth-child(3){ animation-delay:.15s }
.grid-2 .card:nth-child(1){ animation-delay:.15s }
.grid-2 .card:nth-child(2){ animation-delay:.2s }
.grid-3 .card:nth-child(1){ animation-delay:.2s }
.grid-3 .card:nth-child(2){ animation-delay:.25s }
.grid-3 .card:nth-child(3){ animation-delay:.3s }
.card:nth-child(4){ animation-delay:.3s }
.card:nth-child(5){ animation-delay:.35s }

/* ========== 顶栏 ========== */
.topbar {
  position:relative; z-index:1;
  display:flex; align-items:center; justify-content:space-between;
  padding:0 28px; height:60px;
  background:rgba(255,255,255,0.02);
  border-bottom:1px solid rgba(255,255,255,0.04);
  flex-shrink:0;
}
.topbar::after {
  content:''; position:absolute; bottom:-1px; left:50%; transform:translateX(-50%);
  width:60%; height:1px;
  background:linear-gradient(90deg, transparent, rgba(255,105,0,.4), transparent);
}
.topbar-left{ display:flex; align-items:center; gap:14px }
.topbar-logo{
  width:34px; height:34px; display:flex; align-items:center; justify-content:center;
  background:rgba(255,105,0,0.1); border-radius:10px;
  box-shadow: 0 0 20px rgba(255,105,0,0.1);
}
.topbar-info{ display:flex; align-items:baseline; gap:10px }
.topbar-title{ font-size:16px; font-weight:600; color:#f1f5f9; letter-spacing:-.3px }
.topbar-code{
  font-size:11px; color:#475569;
  font-family:ui-monospace,SFMono-Regular,Menlo,monospace;
  padding:2px 8px; border-radius:4px; background:rgba(255,255,255,0.04);
  letter-spacing:.3px;
}
.close-btn{
  width:34px; height:34px; display:flex; align-items:center; justify-content:center;
  border-radius:10px; background:transparent; color:#475569;
  border:none; cursor:pointer; transition:all .25s;
}
.close-btn:hover{ background:rgba(255,105,0,0.12); color:#FF6900 }

/* ========== 主体 ========== */
.main{
  position:relative; z-index:1;
  flex:1; overflow-y:auto; padding:24px 28px 48px;
  scrollbar-width:thin; scrollbar-color:rgba(255,255,255,0.06) transparent;
}
.main::-webkit-scrollbar{ width:4px }
.main::-webkit-scrollbar-track{ background:transparent }
.main::-webkit-scrollbar-thumb{ background:rgba(255,255,255,0.06); border-radius:2px }
.main::-webkit-scrollbar-thumb:hover{ background:rgba(255,255,255,0.12) }

/* ========== KPI 卡片 ========== */
.kpi-row {
  display:grid; grid-template-columns:repeat(3,1fr); gap:14px; margin-bottom:22px;
}
@media(max-width:900px){ .kpi-row{ grid-template-columns:1fr } }
.kpi-card {
  position:relative; display:flex; overflow:hidden;
  background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.06);
  border-radius:16px;
  backdrop-filter:blur(20px); -webkit-backdrop-filter:blur(20px);
  transition:all .35s cubic-bezier(.22,1,.36,1);
}
.kpi-card:hover{
  transform:translateY(-4px);
  border-color:rgba(255,255,255,0.12);
  box-shadow:0 12px 40px rgba(0,0,0,.3);
}
.kpi-accent{ width:3px; flex-shrink:0; align-self:stretch; border-radius:0 2px 2px 0; opacity:.8 }
.kpi-body{ flex:1; padding:18px 20px }
.kpi-header{ display:flex; align-items:center; gap:10px; margin-bottom:14px }
.kpi-icon-wrap{
  width:32px; height:32px; display:flex; align-items:center; justify-content:center;
  border-radius:10px; flex-shrink:0;
}
.kpi-label{ font-size:12px; color:#64748b; font-weight:400 }
.kpi-value{
  font-size:38px; font-weight:700; line-height:1; letter-spacing:-1.5px;
  margin-bottom:16px; font-variant-numeric:tabular-nums;
}
.kpi-progress{ display:flex; align-items:center; gap:10px }
.kpi-bar{ flex:1; height:3px; background:rgba(255,255,255,0.06); border-radius:2px; overflow:hidden }
.kpi-bar-fill{ height:100%; border-radius:2px; transition:width .8s cubic-bezier(.22,1,.36,1) }
.kpi-pct{ font-size:10px; color:#475569; font-variant-numeric:tabular-nums; width:30px; text-align:right }

/* ========== 通用卡片 ========== */
.card{
  background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.05);
  border-radius:16px; margin-bottom:16px;
  backdrop-filter:blur(20px); -webkit-backdrop-filter:blur(20px);
  overflow:hidden; transition:all .3s cubic-bezier(.22,1,.36,1);
}
.card:hover{
  border-color:rgba(255,255,255,0.09);
  box-shadow:0 8px 32px rgba(0,0,0,.2);
}
.card-head{
  display:flex; align-items:center; gap:8px;
  padding:16px 20px 0;
}
.card-dot{ width:6px; height:6px; border-radius:50%; flex-shrink:0 }
.card-title{ font-size:13px; font-weight:500; color:#94a3b8 }
.card-badge{
  margin-left:auto; font-size:10px; color:#475569;
  padding:1px 8px; border-radius:4px; background:rgba(255,255,255,0.04);
  font-variant-numeric:tabular-nums;
}
.card-body{ padding:14px 20px 18px }
.grid-2{ display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:8px }
.grid-3{ display:grid; grid-template-columns:repeat(3,1fr); gap:14px; margin-bottom:8px }
@media(max-width:1024px){ .grid-3{ grid-template-columns:1fr 1fr } }
@media(max-width:768px){ .grid-2,.grid-3{ grid-template-columns:1fr } }
.empty{ text-align:center; color:#334151; padding:40px 0; font-size:12px; letter-spacing:.3px }

/* ========== 趋势图 ========== */
.chart-box{ height:240px; position:relative; margin-bottom:14px }

/* ========== 日期条 ========== */
.day-strip{ display:grid; grid-template-columns:repeat(7,1fr); gap:6px }
.day-chip{
  background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.04);
  border-radius:12px; padding:10px 4px; text-align:center;
  display:flex; flex-direction:column; align-items:center; gap:4px;
  transition:all .25s;
}
.day-chip:hover{ background:rgba(255,255,255,0.06); border-color:rgba(255,255,255,0.1); transform:translateY(-2px) }
.day-d{ font-size:10px; color:#475569; font-weight:400; letter-spacing:.2px }
.day-bars{ display:flex; align-items:flex-end; gap:2px; height:30px; padding:2px 0 }
.day-barpv,.day-baruv{ width:5px; border-radius:2px 2px 1px 1px; transition:height .5s }
.day-pv{ font-size:14px; font-weight:600; color:#FF6900; font-variant-numeric:tabular-nums; line-height:1 }
.day-uv{ font-size:11px; font-weight:500; color:#34D399; font-variant-numeric:tabular-nums }

/* ========== 24小时 ========== */
.hour-grid{ display:flex; align-items:flex-end; gap:2px; height:190px; padding-top:6px; margin-bottom:10px }
.hour-col{ flex:1; display:flex; flex-direction:column; align-items:center; height:100% }
.hour-track{ flex:1; width:100%; display:flex; align-items:flex-end; justify-content:center }
.hour-bar{
  width:70%; max-width:24px; border-radius:4px 4px 1px 1px; min-height:6px;
  transition:height .5s cubic-bezier(.22,1,.36,1), opacity .3s;
  position:relative;
}
.hour-bar:hover{ filter:brightness(1.3); transform:scaleX(1.1); transition:all .2s }
.hour-lbl{ font-size:11px; color:#64748b; margin-top:6px; font-family:ui-monospace,monospace; letter-spacing:.3px }
.hour-foot{
  text-align:center; font-size:11px; color:#475569;
  padding:8px 0 2px; border-top:1px solid rgba(255,255,255,0.04);
}
.hour-foot-dot{ display:inline-block; width:3px; height:3px; border-radius:50%; background:#475569; margin:0 8px; vertical-align:middle }

/* ========== 排行 ========== */
.rank{ display:flex; flex-direction:column; gap:10px }
.rank-item{ display:flex; align-items:center; gap:8px; transition:all .2s; padding:2px 0; border-radius:8px }
.rank-item:hover{ background:rgba(255,255,255,0.02) }
.rank-badge{ width:20px; text-align:center; flex-shrink:0; display:flex; align-items:center; justify-content:center }
.medal{ width:16px; height:16px; display:block }
.rank-num{ font-size:11px; font-weight:500; color:#475569; font-variant-numeric:tabular-nums }
.rank-name{ font-size:11px; color:#64748b; flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; min-width:0 }
.rank-bar{ flex:2; height:5px; background:rgba(255,255,255,0.04); border-radius:3px; overflow:hidden }
.rank-bar-fill{ height:100%; border-radius:3px; transition:width .6s cubic-bezier(.22,1,.36,1) }
.rank-count{ font-size:11px; color:#94a3b8; font-weight:500; width:32px; text-align:right; flex-shrink:0; font-variant-numeric:tabular-nums }

/* ========== 表格 ========== */
.vtbl{ width:100%; border-collapse:collapse; font-size:11px }
.vtbl thead{ background:rgba(255,255,255,0.02) }
.vtbl th{
  text-align:left; padding:10px 14px; color:#475569; font-weight:500;
  font-size:10px; text-transform:uppercase; letter-spacing:.5px;
  border-bottom:1px solid rgba(255,255,255,0.04);
}
.vtbl td{ padding:10px 14px; color:#94a3b8; border-bottom:1px solid rgba(255,255,255,0.02); transition:all .15s }
.vtbl tbody tr:nth-child(even) td{ background:rgba(255,255,255,0.01) }
.vtbl tbody tr:hover td{ background:rgba(255,255,255,0.04) }
.cell-mono{ font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:10px; color:#64748b }
.cell-ip{ color:#475569; letter-spacing:.2px }
.cell-loc-icon{ font-size:10px; opacity:.6 }
.cell-device,.cell-browser{ font-size:12px }
.cell-ref{ max-width:130px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#475569 }

/* ========== 骨架屏 ========== */
@keyframes shimmer{ 0%{opacity:.3} 50%{opacity:.6} 100%{opacity:.3} }
.skeleton-card{
  background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.05);
  border-radius:16px; padding:20px;
}
.skeleton-line{
  height:12px; border-radius:6px; background:rgba(255,255,255,0.06);
  animation:shimmer 1.8s ease-in-out infinite;
}
.skeleton-line.w-24{ width:100px }
.skeleton-line.w-32{ width:140px }
.skeleton-line.w-40{ width:160px }
.skeleton-line.h-10{ height:40px }
.skeleton-line.h-2{ height:8px }
.skeleton-block{
  border-radius:12px; background:rgba(255,255,255,0.04);
  animation:shimmer 1.8s ease-in-out infinite;
}
.w-full{ width:100% }
.mb-4{ margin-bottom:16px }
.mb-6{ margin-bottom:24px }
</style>
