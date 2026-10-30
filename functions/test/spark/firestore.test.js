const {test,before,after,beforeEach}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');const path=require('node:path');
const {initializeTestEnvironment,assertSucceeds,assertFails}=require('@firebase/rules-unit-testing');
const {doc,getDoc,getDocs,collection,query,orderBy,limit,setDoc,updateDoc,deleteDoc,writeBatch,runTransaction,serverTimestamp,Timestamp}=require('firebase/firestore');
let env;
const clients=new Map();
const db=uid=>{if(!clients.has(uid)) clients.set(uid,env.authenticatedContext(uid).firestore());return clients.get(uid);};
const ref=(uid,p)=>doc(db(uid),p);
const publicData=p=>({name:p.name,friendCode:p.friendCode,score:p.score,level:1+Math.floor(p.xp/500),ghostSkin:p.ghostSkin,pacmanSkin:p.pacmanSkin});
const defaultProfile=(uid)=>({name:uid==='alice'?'Alice':'Joueur '+uid,friendCode:Buffer.from(uid.padEnd(6,'0').slice(0,6)).toString('hex').toUpperCase(),score:0,coins:0,xp:0,games:0,victories:0,captures:0,ownedSkins:['ghost_red_classic','pacman_classic'],ghostSkin:'ghost_red_classic',pacmanSkin:'pacman_classic',createdAt:serverTimestamp()});
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-pac-and-seek',firestore:{rules:fs.readFileSync(path.join(__dirname,'../../../firestore.rules'),'utf8')}});});
after(async()=>env.cleanup());beforeEach(async()=>env.clearFirestore());
async function createProfile(uid){const p=defaultProfile(uid),batch=writeBatch(db(uid));batch.set(ref(uid,`users/${uid}`),p);batch.set(ref(uid,`players/${uid}`),publicData(p));batch.set(ref(uid,`friendCodes/${p.friendCode}`),{uid});await batch.commit();return p;}
async function seedGame({status='waiting',startedAt=Date.now()-40000,coins=0,xp=0}={}) {
  await createProfile('alice');await createProfile('bob');
  await env.withSecurityRulesDisabled(async c=>{
    const f=c.firestore(),batch=writeBatch(f),p=defaultProfile('alice');
    batch.set(doc(f,'games/ABCDEF'),{schemaVersion:2,hostId:'alice',status,memberIds:['alice','bob'],activePlayerIds:['alice','bob'],readyIds:['alice','bob'],foundGhostIds:[],createdAt:Timestamp.now(),hidingSeconds:30,seekingSeconds:60,...(status==='started'?{startedAt:Timestamp.fromMillis(startedAt)}:{})});
    for(const id of ['alice','bob'])batch.set(doc(f,`games/ABCDEF/players/${id}`),{name:id==='alice'?'Alice':'Joueur bob',ghostSkin:'ghost_red_classic',pacmanSkin:'pacman_classic',isHost:id==='alice',ready:true,active:true,joinedAt:Timestamp.now(),heading:0,lat:45,lng:5,accuracy:5,locationUpdatedAt:Timestamp.now(),measuredAt:Timestamp.now()});
    batch.update(doc(f,'users/alice'),{coins,xp,activeGame:'ABCDEF'});batch.update(doc(f,'users/bob'),{activeGame:'ABCDEF'});
    batch.set(doc(f,'players/alice'),publicData({...p,coins,xp}));await batch.commit();
  });
}
test('profiles and public projection can be initialized atomically on Spark',async()=>{
  await assertSucceeds(createProfile('alice'));
  assert.equal((await getDoc(ref('alice','users/alice'))).data().coins,0);
  await assertFails(getDoc(ref('bob','users/alice')));
  await assertFails(setDoc(ref('bob','users/bob'),{...defaultProfile('bob'),coins:999}));
  await assertFails(updateDoc(ref('alice','users/alice'),{coins:500}));
});
test('profile name and public leaderboard stay tied to the private source',async()=>{
  const p=await createProfile('alice');const batch=writeBatch(db('alice'));
  batch.update(ref('alice','users/alice'),{name:'Alice test'});batch.set(ref('alice','players/alice'),publicData({...p,name:'Alice test'}));await assertSucceeds(batch.commit());
  await assertFails(updateDoc(ref('alice','players/alice'),{score:90000}));
  await assertFails(updateDoc(ref('alice','users/alice'),{ghostSkin:'premium_ghost_gold_king'}));
});
test('purchase price and pass levels are enforced without Cloud Functions',async()=>{
  await seedGame({coins:1000});
  await assertSucceeds(updateDoc(ref('alice','users/alice'),{coins:850,ownedSkins:['ghost_red_classic','pacman_classic','ghost_pink_classic'],lastAction:{kind:'purchase',skinId:'ghost_pink_classic'}}));
  await assertFails(updateDoc(ref('alice','users/alice'),{coins:850,ownedSkins:['ghost_red_classic','pacman_classic','ghost_pink_classic','ghost_cyan_classic'],lastAction:{kind:'purchase',skinId:'ghost_cyan_classic'}}));
  await assertFails(updateDoc(ref('alice','users/alice'),{ownedSkins:['ghost_red_classic','pacman_classic','ghost_pink_classic','ghost_red_headphones'],lastAction:{kind:'pass',skinId:'ghost_red_headphones'}}));
});
test('eligible pass claim appends one skin without spending coins',async()=>{
  await seedGame({coins:1000,xp:4500});
  await assertSucceeds(updateDoc(ref('alice','users/alice'),{ownedSkins:['ghost_red_classic','pacman_classic','ghost_red_headphones'],lastAction:{kind:'pass',skinId:'ghost_red_headphones'}}));
});
test('new salon creation requires its host player, with a real shareable code',async()=>{
  const p=await createProfile('alice');const batch=writeBatch(db('alice'));
  batch.set(ref('alice','games/ABCDEF'),{schemaVersion:2,hostId:'alice',status:'waiting',memberIds:['alice'],activePlayerIds:['alice'],readyIds:['alice'],foundGhostIds:[],createdAt:serverTimestamp(),hidingSeconds:180,seekingSeconds:540});
  batch.set(ref('alice','games/ABCDEF/players/alice'),{name:p.name,ghostSkin:p.ghostSkin,pacmanSkin:p.pacmanSkin,isHost:true,ready:true,active:true,joinedAt:serverTimestamp(),heading:0});
  batch.update(ref('alice','users/alice'),{activeGame:'ABCDEF'});await assertSucceeds(batch.commit());
  await assertFails(setDoc(ref('alice','games/BCDEFG'),{schemaVersion:2,hostId:'alice',status:'finished'}));
});
test('joining adds only the current user and requires the player record in the same batch',async()=>{
  await seedGame();const p=await createProfile('charlie');
  const batch=writeBatch(db('charlie'));
  batch.update(ref('charlie','games/ABCDEF'),{memberIds:['alice','bob','charlie'],activePlayerIds:['alice','bob','charlie']});
  batch.set(ref('charlie','games/ABCDEF/players/charlie'),{name:p.name,ghostSkin:p.ghostSkin,pacmanSkin:p.pacmanSkin,isHost:false,ready:false,active:true,joinedAt:serverTimestamp(),heading:0});
  batch.update(ref('charlie','users/charlie'),{activeGame:'ABCDEF'});await assertSucceeds(batch.commit());
  await assertFails(updateDoc(ref('charlie','games/ABCDEF'),{memberIds:['alice','bob','charlie','mallory'],activePlayerIds:['alice','bob','charlie','mallory']}));
});
test('readiness is self-only and starting requires all players ready',async()=>{
  await seedGame();
  let batch=writeBatch(db('bob'));batch.update(ref('bob','games/ABCDEF'),{readyIds:['alice']});batch.update(ref('bob','games/ABCDEF/players/bob'),{ready:false});await assertSucceeds(batch.commit());
  await assertFails(updateDoc(ref('alice','games/ABCDEF'),{status:'started',startedAt:serverTimestamp(),hidingSeconds:30,seekingSeconds:60}));
  batch=writeBatch(db('bob'));batch.update(ref('bob','games/ABCDEF'),{readyIds:['alice','bob']});batch.update(ref('bob','games/ABCDEF/players/bob'),{ready:true});await assertSucceeds(batch.commit());
  await assertFails(updateDoc(ref('bob','games/ABCDEF'),{status:'started',startedAt:serverTimestamp(),hidingSeconds:30,seekingSeconds:60}));
  await assertSucceeds(updateDoc(ref('alice','games/ABCDEF'),{status:'started',startedAt:serverTimestamp(),hidingSeconds:30,seekingSeconds:60}));
});
test('legacy games remain readable to participants, and cannot be overwritten or rewarded',async()=>{
  await env.withSecurityRulesDisabled(async c=>setDoc(doc(c.firestore(),'games/6TUA39'),{hostId:'alice',status:'finished',winner:'pacman',createdAt:Timestamp.now(),hidingSeconds:180,seekingSeconds:540}));
  await assertSucceeds(getDoc(ref('alice','games/6TUA39')));
  await assertFails(updateDoc(ref('alice','games/6TUA39'),{status:'waiting',schemaVersion:2}));
  await assertFails(setDoc(ref('alice','users/alice/history/6TUA39'),{score:90000}));
});
test('GPS is self-only, recent and restricted to the active game',async()=>{
  await seedGame({status:'started'});
  const valid={lat:45,lng:5,accuracy:5,heading:60,measuredAt:Timestamp.now(),locationUpdatedAt:serverTimestamp()};
  await assertSucceeds(updateDoc(ref('alice','games/ABCDEF/players/alice'),valid));
  await assertFails(updateDoc(ref('bob','games/ABCDEF/players/alice'),valid));
  await assertFails(updateDoc(ref('alice','games/ABCDEF/players/alice'),{...valid,measuredAt:Timestamp.fromMillis(Date.now()-60000)}));
  await assertFails(updateDoc(ref('alice','games/ABCDEF/players/alice'),{...valid,lat:92}));
  await assertFails(getDoc(ref('mallory','games/ABCDEF/players/bob')));
});
test('host capture needs seeking phase and recent nearby GPS readings',async()=>{
  await seedGame({status:'started'});
  await assertFails(updateDoc(ref('bob','games/ABCDEF'),{foundGhostIds:['bob'],lastCapture:'bob'}));
  await assertSucceeds(updateDoc(ref('alice','games/ABCDEF'),{foundGhostIds:['bob'],lastCapture:'bob'}));
  await assertSucceeds(updateDoc(ref('alice','games/ABCDEF'),{status:'finished',winner:'pacman',finishedAt:serverTimestamp()}));
});
test('finishing early or choosing an unearned winner is rejected',async()=>{
  await seedGame({status:'started'});
  await assertFails(updateDoc(ref('alice','games/ABCDEF'),{status:'finished',winner:'pacman',finishedAt:serverTimestamp()}));
  await assertFails(updateDoc(ref('bob','games/ABCDEF'),{status:'finished',winner:'ghosts',finishedAt:serverTimestamp()}));
});
test('reward receipt, profile, public score and seasonal score must commit together, once',async()=>{
  await seedGame({status:'started'});
  await updateDoc(ref('alice','games/ABCDEF'),{foundGhostIds:['bob'],lastCapture:'bob'});
  await updateDoc(ref('alice','games/ABCDEF'),{status:'finished',winner:'pacman',finishedAt:serverTimestamp()});
  const month=new Date().toISOString().slice(0,7),p=(await getDoc(ref('alice','users/alice'))).data();
  const next={...p,score:170,coins:85,xp:175,games:1,victories:1,captures:1};
  const mutation={score:170,coins:85,xp:175,games:1,victories:1,captures:1,lastAction:{kind:'reward',code:'ABCDEF',season:month}};
  await assertFails(updateDoc(ref('alice','users/alice'),mutation));
  const batch=writeBatch(db('alice'));batch.update(ref('alice','users/alice'),mutation);
  batch.set(ref('alice','players/alice'),publicData(next));
  batch.set(ref('alice',`seasons/${month}/scores/alice`),publicData(next));
  batch.set(ref('alice','users/alice/history/ABCDEF'),{code:'ABCDEF',score:170,coins:85,xp:175,won:true,role:'pacman',at:serverTimestamp(),season:month});
  await assertSucceeds(batch.commit());
  await assertFails(updateDoc(ref('alice','users/alice'),{...mutation,score:340,coins:170,xp:350,games:2,victories:2,captures:2}));
});
test('friendship requires a received request and a reciprocal transaction',async()=>{
  const alice=await createProfile('alice'),bob=await createProfile('bob');
  await assertFails(setDoc(ref('alice','users/alice/friends/bob'),{uid:'bob',name:bob.name}));
  await assertSucceeds(setDoc(ref('alice','users/bob/requests/alice'),{uid:'alice',name:alice.name,at:serverTimestamp()}));
  const batch=writeBatch(db('bob'));batch.set(ref('bob','users/bob/friends/alice'),{uid:'alice',name:alice.name});batch.set(ref('bob','users/alice/friends/bob'),{uid:'bob',name:bob.name});batch.delete(ref('bob','users/bob/requests/alice'));await assertSucceeds(batch.commit());
});
test('only friends can receive invitations to a waiting salon',async()=>{
  await seedGame();
  const invite={code:'ABCDEF',from:'Alice',senderId:'alice',at:serverTimestamp()};
  await assertFails(setDoc(ref('alice','users/bob/invites/ABCDEF'),invite));
  await env.withSecurityRulesDisabled(async c=>setDoc(doc(c.firestore(),'users/alice/friends/bob'),{uid:'bob',name:'Joueur bob'}));
  await assertSucceeds(setDoc(ref('alice','users/bob/invites/ABCDEF'),invite));
});

test('leaving a waiting lobby removes only self, and host cancellation preserves the archive',async()=>{
  await seedGame();
  await assertFails(updateDoc(ref('bob','games/ABCDEF'),{memberIds:['bob'],activePlayerIds:['bob'],readyIds:['bob']}));
  const batch=writeBatch(db('bob'));
  batch.update(ref('bob','games/ABCDEF'),{memberIds:['alice'],activePlayerIds:['alice'],readyIds:['alice']});
  batch.delete(ref('bob','games/ABCDEF/players/bob'));
  batch.update(ref('bob','users/bob'),{activeGame:null});
  await assertSucceeds(batch.commit());
  await assertSucceeds(updateDoc(ref('alice','games/ABCDEF'),{status:'cancelled'}));
  await assertFails(deleteDoc(ref('alice','games/ABCDEF')));
});

test('capture rejects the hiding phase, distant coordinates and stale GPS',async()=>{
  await seedGame({status:'started',startedAt:Date.now()});
  const capture={foundGhostIds:['bob'],lastCapture:'bob'};
  await assertFails(updateDoc(ref('alice','games/ABCDEF'),capture));
  await env.withSecurityRulesDisabled(async c=>{
    await updateDoc(doc(c.firestore(),'games/ABCDEF'),{startedAt:Timestamp.fromMillis(Date.now()-40000)});
    await updateDoc(doc(c.firestore(),'games/ABCDEF/players/bob'),{lat:46});
  });
  await assertFails(updateDoc(ref('alice','games/ABCDEF'),capture));
  await env.withSecurityRulesDisabled(async c=>updateDoc(doc(c.firestore(),'games/ABCDEF/players/bob'),{lat:45,locationUpdatedAt:Timestamp.fromMillis(Date.now()-60000)}));
  await assertFails(updateDoc(ref('alice','games/ABCDEF'),capture));
});

test('an expired game can be finalized by a ghost with a single valid reward',async()=>{
  await seedGame({status:'started',startedAt:Date.now()-120000});
  await assertSucceeds(updateDoc(ref('bob','games/ABCDEF'),{status:'finished',winner:'ghosts',finishedAt:serverTimestamp()}));
  const month=new Date().toISOString().slice(0,7),p=(await getDoc(ref('bob','users/bob'))).data();
  const next={...p,score:120,coins:75,xp:150,games:1,victories:1,captures:0};
  const batch=writeBatch(db('bob'));
  batch.update(ref('bob','users/bob'),{score:120,coins:75,xp:150,games:1,victories:1,captures:0,lastAction:{kind:'reward',code:'ABCDEF',season:month}});
  batch.set(ref('bob','players/bob'),publicData(next));
  batch.set(ref('bob',`seasons/${month}/scores/bob`),publicData(next));
  batch.set(ref('bob','users/bob/history/ABCDEF'),{code:'ABCDEF',score:120,coins:75,xp:150,won:true,role:'ghost',at:serverTimestamp(),season:month});
  await assertSucceeds(batch.commit());
});

test('concurrent purchases debit once and cannot mint inventory or equip the wrong role',async()=>{
  await seedGame({coins:1000});
  const buy=()=>runTransaction(db('alice'),async tx=>{
    const r=ref('alice','users/alice'),p=(await tx.get(r)).data();
    if(!p.ownedSkins.includes('ghost_pink_classic'))tx.update(r,{coins:p.coins-150,ownedSkins:[...p.ownedSkins,'ghost_pink_classic'],lastAction:{kind:'purchase',skinId:'ghost_pink_classic'}});
  });
  await assertSucceeds(Promise.all([buy(),buy()]));
  assert.equal((await getDoc(ref('alice','users/alice'))).data().coins,850);
  await assertFails(updateDoc(ref('alice','users/alice'),{pacmanSkin:'ghost_red_classic'}));
  await assertFails(updateDoc(ref('alice','users/alice'),{ownedSkins:['ghost_red_classic','pacman_classic','premium_ghost_gold_king']}));
});
