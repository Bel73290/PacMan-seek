const {test,before,beforeEach}=require('node:test');
const assert=require('node:assert/strict');
process.env.GCLOUD_PROJECT='demo-pac-and-seek';
const api=require('../../index');
const {getFirestore,Timestamp}=require('firebase-admin/firestore');
const {catalog}=require('../../domain');
const db=getFirestore();
const call=(fn,uid,data={})=>api[fn].run({auth:uid?{uid}:null,data});
async function seed(...uids){for(const uid of uids)await call('ensureProfile',uid);}
async function game(){
  await seed('host','ghost');
  const {code}=await call('createGame','host');
  await call('joinGame','ghost',{code});await call('setReady','ghost',{code,ready:true});
  await call('startGame','host',{code,hidingSeconds:30,seekingSeconds:60});return code;
}
before(()=>{assert.ok(process.env.FIRESTORE_EMULATOR_HOST,'Integration tests require the local Firestore emulator.');});
beforeEach(async()=>{
  const host=process.env.FIRESTORE_EMULATOR_HOST;
  const response=await fetch(`http://${host}/emulator/v1/projects/demo-pac-and-seek/databases/(default)/documents`,{method:'DELETE'});
  assert.equal(response.ok,true);
});
test('anonymous requests cannot create profiles or games',async()=>{
  await assert.rejects(()=>call('ensureProfile',null),{code:'unauthenticated'});
  await assert.rejects(()=>call('createGame',null),{code:'unauthenticated'});
});
test('parallel profile initialization preserves the same identity and initial balance',async()=>{
  await Promise.all([call('ensureProfile','host'),call('ensureProfile','host')]);
  const p=(await db.doc('users/host').get()).data();assert.equal(p.coins,0);assert.equal(p.score,0);assert.equal(p.friendCode.length,12);
});
test('parallel purchases debit once and keep ranking score',async()=>{
  await seed('host');const skin=catalog.find(s=>s.price>0);
  await db.doc('users/host').update({coins:10000,score:444});
  await Promise.all([call('purchaseSkin','host',{skinId:skin.id}),call('purchaseSkin','host',{skinId:skin.id})]);
  const p=(await db.doc('users/host').get()).data();assert.equal(p.coins,10000-skin.price);assert.equal(p.score,444);assert.equal(p.ownedSkins.filter(id=>id===skin.id).length,1);
  await assert.rejects(()=>call('equipSkin','host',{skinId:'premium_ghost_gold_king'}));
});
test('equipped skins propagate to the active lobby and public profile',async()=>{
  await seed('host');const skin=catalog.find(s=>s.role==='pacman' && s.price>0);
  await db.doc('users/host').update({coins:10000});
  const {code}=await call('createGame','host');
  await call('purchaseSkin','host',{skinId:skin.id});await call('equipSkin','host',{skinId:skin.id});
  assert.equal((await db.doc(`games/${code}/players/host`).get()).data().pacmanSkin,skin.id);
  assert.equal((await db.doc('players/host').get()).data().pacmanSkin,skin.id);
});
test('leaderboard pagination uses a deterministic tie break and full personal rank',async()=>{
  await seed('host');const batch=db.batch();
  for(let i=0;i<55;i++)batch.set(db.doc(`players/u${String(i).padStart(3,'0')}`),{name:`Joueur ${i}`,score:100,level:1});
  await batch.commit();
  const first=await call('leaderboard','host',{scope:'general'});
  assert.equal(first.entries.length,50);assert.equal(first.rank,56);assert.equal(first.hasMore,true);
  const next=await call('leaderboard','host',{scope:'general',cursor:first.cursor});
  assert.equal(next.entries.length,6);assert.equal(new Set([...first.entries,...next.entries].map(e=>e.uid)).size,56);
});
test('lobby requires membership, host privileges, and readiness',async()=>{
  await seed('host','ghost','stranger');const {code}=await call('createGame','host');
  await assert.rejects(()=>call('startGame','host',{code,hidingSeconds:30,seekingSeconds:60}));
  await call('joinGame','ghost',{code});
  await assert.rejects(()=>call('startGame','ghost',{code,hidingSeconds:30,seekingSeconds:60}),{code:'permission-denied'});
  await assert.rejects(()=>call('setReady','stranger',{code,ready:true}),{code:'permission-denied'});
  await assert.rejects(()=>call('startGame','host',{code,hidingSeconds:30,seekingSeconds:60}));
  await call('setReady','ghost',{code,ready:true});await call('startGame','host',{code,hidingSeconds:30,seekingSeconds:60});
  await assert.rejects(()=>call('joinGame','stranger',{code}));
  const resumed=await call('joinGame','ghost',{code});assert.equal(resumed.started,true);
});
test('capture and concurrent completion produce exactly one history and reward',async()=>{
  const code=await game();const ref=db.doc(`games/${code}`);
  await ref.update({startedAt:Timestamp.fromMillis(Date.now()-40000)});
  const position={lat:45,lng:5,accuracy:5,measuredAt:Timestamp.now(),locationUpdatedAt:Timestamp.now()};
  await ref.collection('players').doc('host').update(position);
  await ref.collection('players').doc('ghost').update({...position,lat:46});
  await assert.rejects(()=>call('captureGhost','host',{code,ghostId:'ghost'}));
  await ref.collection('players').doc('ghost').update(position);
  await call('captureGhost','host',{code,ghostId:'ghost'});
  await Promise.all([call('finishGame','host',{code}),call('finishGame','ghost',{code}),call('finishGame','host',{code})]);
  const p=(await db.doc('users/host').get()).data();assert.equal(p.games,1);assert.equal(p.score,170);assert.equal(p.coins,85);
  assert.equal((await db.collection('users/host/history').get()).size,1);
  assert.equal((await ref.get()).data().rewarded,true);
});
test('timeout winner is calculated by server, never supplied by the phone',async()=>{
  const code=await game();await db.doc(`games/${code}`).update({endsAt:Timestamp.fromMillis(Date.now()-1000)});
  await call('finishGame','ghost',{code,winner:'pacman'});
  assert.equal((await db.doc(`games/${code}`).get()).data().winner,'ghosts');
  assert.equal((await db.doc('users/ghost').get()).data().victories,1);
});
test('leaving as host cancels the game without awarding points',async()=>{
  const code=await game();await call('leaveGame','host',{code});
  assert.equal((await db.doc(`games/${code}`).get()).data().status,'cancelled');
  assert.equal((await db.doc('users/ghost').get()).data().activeGame,undefined);
  await call('finishGame','ghost',{code});assert.equal((await db.doc('users/ghost').get()).data().games,0);
});
test('friendship needs acceptance; invitations require an existing friend',async()=>{
  await seed('host','ghost');const ghost=(await db.doc('users/ghost').get()).data();
  const {code}=await call('createGame','host');
  await assert.rejects(()=>call('inviteFriend','host',{code,uid:'ghost'}));
  await call('sendFriendRequest','host',{friendCode:ghost.friendCode});
  await call('respondFriendRequest','ghost',{uid:'host',accept:true});
  await call('inviteFriend','host',{code,uid:'ghost'});
  assert.equal((await db.doc(`users/ghost/invites/${code}`).get()).exists,true);
  const board=await call('leaderboard','host',{scope:'friends'});assert.equal(board.entries.length,2);
  await call('removeFriend','host',{uid:'ghost'});assert.equal((await db.doc('users/ghost/friends/host').get()).exists,false);
});
