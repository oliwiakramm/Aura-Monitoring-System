'use strict';
const CONFIG={hubUrl:(window.location.port==='' || window.location.port==='80') ? '/api/status':'http://localhost:8080/api/status',refreshInterval:5,toastMs:4500};

const state={agents:{},totalReports:0,countdown:CONFIG.refreshInterval,countdownTimer:null};
const $=id=>document.getElementById(id);

function updateClock(){$('clock').textContent=new Date().toLocaleTimeString('pl-PL')}
setInterval(updateClock,1000);updateClock();

function setConnection(ok){
  const b=$('live-badge');
  b.className='live-badge '+(ok?'connected':'disconnected');
  $('live-text').textContent=ok?'Connected':'No connection';
}

function startCountdown(){
  if(state.countdownTimer)clearInterval(state.countdownTimer);
  state.countdown=CONFIG.refreshInterval;
  state.countdownTimer=setInterval(()=>{
    state.countdown=Math.max(0,state.countdown-1);
    $('countdown').textContent=state.countdown;
    $('refresh-fill').style.width=(state.countdown/CONFIG.refreshInterval*100)+'%';
    if(state.countdown<=0)clearInterval(state.countdownTimer);
  },1000);
}

async function fetchStatus(){
  try{
    const res=await fetch(CONFIG.hubUrl,{headers:{Accept:'application/json'},signal:AbortSignal.timeout(4000)});
    if(!res.ok)throw new Error('HTTP '+res.status);
    const agents=await res.json();
    setConnection(true);
    render(agents);
  }catch(e){setConnection(false);renderError();
    setNum($('stat-total'),    0);
    setNum($('stat-healthy'),  0);
    setNum($('stat-critical'), 0);
    setNum($('stat-offline'),  0);
    $('bar-total').style.width    = '0%';
    $('bar-healthy').style.width  = '0%';
    $('bar-critical').style.width = '0%';
    $('bar-offline').style.width  = '0%';
    $('agent-count-badge').textContent = '0 agentów';
    $('agent-count-badge').className   = 'panel-badge blue';
  }
  finally{startCountdown();}
}

function render(agents){
  updateStatCards(agents);renderTable(agents);detectChanges(agents);
}

function updateStatCards(agents){
  const offline=agents.filter(a=>a.offline||a.status==='OFFLINE').length;
  const healthy=agents.filter(a=>a.status==='HEALTHY').length;
  const critical=agents.filter(a=>a.status==='CRITICAL').length;
  const online=agents.length-offline;
  setNum($('stat-total'),online);setNum($('stat-healthy'),healthy);
  setNum($('stat-critical'),critical);setNum($('stat-offline'),offline);
  const t=agents.length||1;
  $('bar-total').style.width=(online/t*100)+'%';
  $('bar-healthy').style.width=(healthy/t*100)+'%';
  $('bar-critical').style.width=(critical/t*100)+'%';
  $('bar-offline').style.width=(offline/t*100)+'%';
}

function setNum(el,n){
  if(el.textContent===String(n))return;
  el.style.transition='opacity .15s';el.style.opacity='0.3';
  setTimeout(()=>{el.textContent=n;el.style.opacity='1';},150);
}

const ICON_COLORS=['blue','green','purple','blue','green'];

function agentComputedData(agent, i) {
  const sc=(agent.status||'unknown').toLowerCase();
  const cpu=Math.round((agent.cpuUsage||0)*100);
  const ram=agent.ramUsageMb||0;
  const cpuG=cpu<60?'low':cpu<85?'medium':'high';
  const ramG=ram<300?'low':ram<512?'medium':'high';
  const ramPct=Math.min(100,Math.round(ram/10.24)/100);
  const ic=ICON_COLORS[i%ICON_COLORS.length];
  const ls=formatLastSeen(agent.lastSeenAt);
  const lsc=isRecent(agent.lastSeenAt)?'recent':'stale';
  const slabel={healthy:'Healthy',critical:'Critical',offline:'Offline',unknown:'Unknown'}[sc]||agent.status;
  const urlS=(agent.monitoredUrl||'—').replace(/^https?:\/\//,'');
  return {sc,cpu,ram,cpuG,ramG,ramPct,ic,ls,lsc,slabel,urlS};
}

function renderTable(agents){
  const tbody=$('agents-tbody');
  const cards=$('agent-cards');
  const badge=$('agent-count-badge');

  const EMPTY_SVG=`<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6">
  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z" />
  </svg>
  `;
  const AGENT_SVG=EMPTY_SVG;

  if(!agents||agents.length===0){
    badge.textContent='0 agents';badge.className='panel-badge blue';
    const emptyHtml=`<div class="empty-state">
      <div class="empty-icon">${EMPTY_SVG}</div>
      <div class="empty-title">No agents</div>
      <div class="empty-sub">Run Aura Agent and data will start flowing.</div>
    </div>`;
    tbody.innerHTML=`<tr><td colspan="5">${emptyHtml}</td></tr>`;
    cards.innerHTML=emptyHtml;
    return;
  }

  badge.textContent=agents.length+(agents.length===1?' agent':' agentów');
  const order={CRITICAL:0,HEALTHY:1,OFFLINE:2,UNKNOWN:3};
  const sorted=[...agents].sort((a,b)=>(order[a.status]??9)-(order[b.status]??9));
  const hasCritical=sorted.some(a=>a.status==='CRITICAL');
  badge.className='panel-badge '+(hasCritical?'red':'green');

 
  tbody.innerHTML=sorted.map((agent,i)=>{
    const d=agentComputedData(agent,i);
    return `<tr>
      <td><div class="td-agent">
        <div class="agent-icon ${d.ic}">${AGENT_SVG}</div>
        <div><div class="agent-name">${esc(agent.agentId)}</div><div class="agent-url">${esc(d.urlS)}</div></div>
      </div></td>
      <td><div class="status-pill ${d.sc}"><div class="dot"></div>${d.slabel}</div></td>
      <td><div class="gauge-wrap"><div class="gauge-track"><div class="gauge-fill ${d.cpuG}" style="width:${d.cpu}%"></div></div><div class="gauge-pct">${d.cpu}%</div></div></td>
      <td><div class="gauge-wrap"><div class="gauge-track"><div class="gauge-fill ${d.ramG}" style="width:${d.ramPct}%"></div></div><div class="gauge-pct">${d.ram}MB</div></div></td>
      <td><div class="last-seen ${d.lsc}">${d.ls}</div></td>
    </tr>`;
  }).join('');


  cards.innerHTML=sorted.map((agent,i)=>{
    const d=agentComputedData(agent,i);
    return `<div class="agent-card-m">
      <div class="agent-card-top">
        <div class="agent-card-left">
          <div class="agent-icon ${d.ic}" style="flex-shrink:0">${AGENT_SVG}</div>
          <div style="min-width:0">
            <div class="agent-card-name">${esc(agent.agentId)}</div>
            <div class="agent-card-url">${esc(d.urlS)}</div>
          </div>
        </div>
        <div class="status-pill ${d.sc}" style="flex-shrink:0"><div class="dot"></div>${d.slabel}</div>
      </div>
      <div class="agent-card-metrics">
        <div class="metric-block-m">
          <div class="metric-label-m">CPU</div>
          <div class="metric-row-m">
            <div class="gauge-track" style="flex:1"><div class="gauge-fill ${d.cpuG}" style="width:${d.cpu}%"></div></div>
            <div class="metric-num-m">${d.cpu}%</div>
          </div>
        </div>
        <div class="metric-block-m">
          <div class="metric-label-m">RAM</div>
          <div class="metric-row-m">
            <div class="gauge-track" style="flex:1"><div class="gauge-fill ${d.ramG}" style="width:${d.ramPct}%"></div></div>
            <div class="metric-num-m">${d.ram}MB</div>
          </div>
        </div>
      </div>
      <div class="agent-card-footer-m">
        <div class="last-seen ${d.lsc}">${d.ls}</div>
      </div>
    </div>`;
  }).join('');
}


function detectChanges(agents){
  agents.forEach(agent=>{
    const prev=state.agents[agent.agentId];const curr=agent.status;
    if(prev!==undefined&&prev!==curr){
      if(curr==='CRITICAL')toast(agent.agentId+' changed into Critical','critical');
      else if(curr==='HEALTHY'&&prev==='CRITICAL')toast(agent.agentId+' changed into Healthy','healthy');
      else if(curr==='OFFLINE')toast(agent.agentId+' stopped respondning','critical');
    }
    state.agents[agent.agentId]=curr;
  });
}

function renderError(){
  const errHtml=`<div class="empty-state">
    <div class="empty-icon"><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6">
  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z" />
  </svg></div>
    <div class="empty-title">No Hub connection</div>
    <div class="empty-sub">Make sure Aura Hub is running on port 8080.</div>
  </div>`;
  $('agents-tbody').innerHTML=`<tr><td colspan="5">${errHtml}</td></tr>`;
  $('agent-cards').innerHTML=errHtml;
}

function toast(msg,type){
  const el=document.createElement('div');el.className=`toast ${type}`;el.textContent=msg;
  $('toasts').appendChild(el);
  setTimeout(()=>{el.style.animation='tOut .25s ease forwards';setTimeout(()=>el.remove(),250);},CONFIG.toastMs);
}

function formatLastSeen(iso){
  try{const s=Math.floor((Date.now()-new Date(iso).getTime())/1000);
    if(s<5)return'teraz';if(s<60)return s+'s temu';
    if(s<3600)return Math.floor(s/60)+'m temu';return Math.floor(s/3600)+'h temu';
  }catch{return'—';}
}
function isRecent(iso){try{return Date.now()-new Date(iso).getTime()<15000;}catch{return false;}}
function esc(s){return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');}

fetchStatus();
setInterval(fetchStatus,CONFIG.refreshInterval*1000);