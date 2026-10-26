const {test}=require('node:test');
const assert=require('node:assert/strict');
const {catalog,DEFAULT_SKINS,levelFor,seasonKey,canCapture,rewardFor,inventoryAfterPurchase}=require('../domain');
test('catalog identifiers are unique and role/price values are valid',()=>{
  assert.equal(new Set(catalog.map(s=>s.id)).size,catalog.length);
  for(const item of catalog){assert.ok(['ghost','pacman'].includes(item.role));assert.ok(Number.isInteger(item.price)&&item.price>=0);}
  for(const id of DEFAULT_SKINS)assert.ok(catalog.some(s=>s.id===id));
});
test('buying a skin is idempotent and cannot spend ranking points',()=>{
  const skin=catalog.find(s=>s.price>0),p={coins:skin.price+20,score:900,ownedSkins:[...DEFAULT_SKINS]};
  const next=inventoryAfterPurchase(p,skin.id);assert.equal(next.coins,20);assert.equal(p.score,900);
  assert.deepEqual(inventoryAfterPurchase({...p,...next},skin.id),next);
  assert.throws(()=>inventoryAfterPurchase({...p,coins:0},skin.id));
  assert.throws(()=>inventoryAfterPurchase(p,'unknown'));
});
test('capture requires seeking, fresh accurate positions, and proximity',()=>{
  const time=1000000, ts=x=>({toMillis:()=>x});
  const game={status:'started',startedAt:ts(time-40000),endsAt:ts(time+60000),hidingSeconds:30};
  const host={lat:45,lng:5,accuracy:5,active:true,locationUpdatedAt:ts(time-1000),measuredAt:ts(time-1000)};
  const ghost={...host,lat:45.00001};
  assert.equal(canCapture(game,host,ghost,time),true);
  for(const change of [{accuracy:50},{lat:46},{active:false},{locationUpdatedAt:ts(time-60000)},{measuredAt:ts(time-60000)},{lat:NaN}])assert.equal(canCapture(game,host,{...ghost,...change},time),false);
  assert.equal(canCapture({...game,hidingSeconds:90},host,ghost,time),false);
  assert.equal(canCapture({...game,status:'finished'},host,ghost,time),false);
  assert.equal(canCapture(game,host,ghost,time+90000),false);
});
test('rewards distinguish captures, surviving ghosts, and team winner',()=>{
  const game={hostId:'host',winner:'pacman',foundGhostIds:['g1','g2']};
  assert.deepEqual(rewardFor(game,'host'),{score:220,xp:200,coins:95,won:true});
  assert.equal(rewardFor(game,'g1').won,false);
  assert.equal(rewardFor({...game,winner:'ghosts'},'g3').won,true);
  assert.equal(rewardFor({...game,winner:'ghosts'},'g1').won,false);
});
test('levels and monthly seasons use stable UTC boundaries',()=>{
  assert.equal(levelFor(499),1);assert.equal(levelFor(500),2);assert.equal(levelFor(0),1);
  assert.equal(seasonKey(new Date('2026-10-31T23:59:59Z')),'2026-10');
  assert.equal(seasonKey(new Date('2026-11-01T00:00:00Z')),'2026-11');
});
