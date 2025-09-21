function toggleMenu(){
  const m = document.getElementById('menu');
  if(!m) return;
  m.style.display = m.style.display === 'flex' ? 'none' : 'flex';
  m.style.gap = '18px';
}
const obs = new IntersectionObserver((entries)=>{
  entries.forEach(e=>{ if(e.isIntersecting) e.target.classList.add('in'); });
},{threshold:0.2});
document.addEventListener('DOMContentLoaded', ()=>{
  document.querySelectorAll('.fadein').forEach(el=>obs.observe(el));
});
