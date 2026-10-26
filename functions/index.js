"use strict";
const {onCall, HttpsError} = require('firebase-functions/v2/https');
const {onSchedule} = require('firebase-functions/v2/scheduler');
const {initializeApp} = require('firebase-admin/app');
const {getFirestore, FieldValue, Timestamp} = require('firebase-admin/firestore');
const crypto = require('node:crypto');
const {catalog, DEFAULT_SKINS, levelFor, unlockedPassSkins, seasonKey, canCapture, rewardFor, inventoryAfterPurchase} = require('./domain');
initializeApp();
const db = getFirestore();
const region = 'europe-west1';
const stamp = () => FieldValue.serverTimestamp();
const fail = (message, code = 'failed-precondition') => {throw new HttpsError(code, message);};
const callable = handler => onCall({region, maxInstances: 10}, async req => {
  if (!req.auth) fail('Connecte-toi pour continuer.', 'unauthenticated');
  return handler(req.auth.uid, req.data || {});
});
const profileRef = uid => db.doc(`users/${uid}`);
const publicRef = uid => db.doc(`players/${uid}`);
function codeValue(data) {
  if (typeof data.code !== 'string' || !/^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$/.test(data.code)) fail('Code de partie invalide.', 'invalid-argument');
  return data.code;
}
function member(game, uid) {
  if (!game || !game.memberIds?.includes(uid)) fail('Tu ne fais pas partie de ce salon.', 'permission-denied');
}
function publicData(p) { return {name:p.name, friendCode:p.friendCode, score:p.score, level:levelFor(p.xp), ghostSkin:p.ghostSkin, pacmanSkin:p.pacmanSkin}; }
exports.ensureProfile = callable(async uid => db.runTransaction(async tx => {
  const ref = profileRef(uid), snap = await tx.get(ref);
  if (snap.exists) return {uid};
  const friendCode = crypto.randomBytes(6).toString('hex').toUpperCase();
  const p = {name:`Joueur ${friendCode.slice(0,4)}`, friendCode, score:0, coins:0, xp:0, games:0, victories:0, captures:0,
    ownedSkins: DEFAULT_SKINS, ghostSkin:'ghost_red_classic', pacmanSkin:'pacman_classic', createdAt:stamp()};
  tx.create(ref,p); tx.create(publicRef(uid),publicData(p)); tx.create(db.doc(`friendCodes/${friendCode}`),{uid});
  return {uid};
}));
exports.saveProfile = callable(async (uid, data) => {
  const name = typeof data.name === 'string' ? data.name.trim().normalize('NFC') : '';
  if (name.length < 2 || name.length > 24 || /[\p{C}]/u.test(name)) fail('Le pseudo doit contenir entre 2 et 24 caractères.', 'invalid-argument');
  const batch = db.batch(); batch.update(profileRef(uid),{name}); batch.update(publicRef(uid),{name}); await batch.commit(); return {ok:true};
});
exports.purchaseSkin = callable(async (uid,data) => db.runTransaction(async tx => {
  const snap = await tx.get(profileRef(uid)); if (!snap.exists) fail('Profil indisponible.');
  let next; try {next = inventoryAfterPurchase(snap.data(),data.skinId);} catch (e) {fail(e.message);}
  tx.update(snap.ref,next); return {ok:true};
}));
exports.claimBattlePass = callable(async uid=>db.runTransaction(async tx=> {
  const snap=await tx.get(profileRef(uid)),p=snap.data();
  if(!p) fail('Profil indisponible.');
  const earned=unlockedPassSkins(p.xp);
  tx.update(snap.ref,{ownedSkins:[...new Set([...p.ownedSkins,...earned])]});
  return {ok:true};
}));
exports.equipSkin = callable(async (uid,data) => db.runTransaction(async tx => {
  const snap = await tx.get(profileRef(uid)); const p = snap.data();
  const item = catalog.find(s=>s.id === data.skinId);
  if (!p || !item || !p.ownedSkins.includes(item.id)) fail('Ce skin ne fait pas partie de ta collection.');
  const field = item.role === 'pacman' ? 'pacmanSkin' : 'ghostSkin';
  const active = p.activeGame ? await tx.get(db.doc(`games/${p.activeGame}`)) : null;
  tx.update(snap.ref,{[field]:item.id}); tx.update(publicRef(uid),{[field]:item.id});
  if(active?.data()?.memberIds?.includes(uid) && ['waiting','started'].includes(active.data().status)) {
    tx.update(active.ref.collection('players').doc(uid),{[field]:item.id});
  }
  return {ok:true};
}));
function playerData(uid,p,host) {return {name:p.name, ghostSkin:p.ghostSkin, pacmanSkin:p.pacmanSkin, isHost:host, ready:host, active:true, joinedAt:stamp(), heading:0};}
async function checkPrevious(tx,p) {
  if (!p.activeGame) return;
  const old = (await tx.get(db.doc(`games/${p.activeGame}`))).data();
  if (old && ['waiting','started'].includes(old.status)) fail(`Tu as déjà une partie : ${p.activeGame}. Reprends-la ou quitte-la.`);
}
exports.createGame = callable(async uid => {
  for(let attempt=0;attempt<5;attempt++) {
    const alphabet='ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    const code = Array.from({length:6},()=>alphabet[crypto.randomInt(alphabet.length)]).join('');
    const made=await db.runTransaction(async tx=> {
      const ref=db.doc(`games/${code}`), existing=await tx.get(ref), ps=await tx.get(profileRef(uid));
      if (existing.exists) return false;
      const p=ps.data(); if(!p) fail('Charge ton profil avant de jouer.'); await checkPrevious(tx,p);
      tx.create(ref,{hostId:uid,status:'waiting',memberIds:[uid],activePlayerIds:[uid],createdAt:stamp(),foundGhostIds:[],hidingSeconds:180,seekingSeconds:540});
      tx.create(ref.collection('players').doc(uid),playerData(uid,p,true)); tx.update(ps.ref,{activeGame:code}); return true;
    });
    if(made) return {code};
  }
  fail('Impossible de créer un code. Réessaie.');
});
exports.joinGame = callable(async(uid,data)=> {
  const code=codeValue(data);
  return db.runTransaction(async tx=> {
    const ref=db.doc(`games/${code}`), snap=await tx.get(ref), ps=await tx.get(profileRef(uid)), g=snap.data(), p=ps.data();
    if(!g) fail('Aucune partie ne correspond à ce code.', 'not-found');
    if(!p) fail('Profil indisponible.');
    const existing=await tx.get(ref.collection('players').doc(uid));
    if(existing.exists && g.memberIds.includes(uid) && g.status==='started' && g.activePlayerIds.includes(uid) && existing.data().active!==false) return {code,isHost:g.hostId===uid,started:true};
    if(g.status!=='waiting') fail('Cette partie a déjà commencé ou est terminée.');
    if(g.memberIds.length>=12 && !g.memberIds.includes(uid)) fail('Ce salon est complet (12 joueurs).');
    if(p.activeGame!==code) await checkPrevious(tx,p);
    tx.update(ref,{memberIds:FieldValue.arrayUnion(uid),activePlayerIds:FieldValue.arrayUnion(uid)});
    if(!existing.exists) tx.create(ref.collection('players').doc(uid),playerData(uid,p,false));
    tx.update(ps.ref,{activeGame:code}); return {code,isHost:g.hostId===uid,started:false};
  });
});
exports.setReady = callable(async(uid,data)=>db.runTransaction(async tx=> {
  const ref=db.doc(`games/${codeValue(data)}`), g=(await tx.get(ref)).data(); member(g,uid);
  if(g.status!=='waiting') fail('Le salon est fermé.');
  tx.update(ref.collection('players').doc(uid),{ready:data.ready===true}); return {ok:true};
}));
exports.startGame = callable(async(uid,data)=>db.runTransaction(async tx=> {
  const ref=db.doc(`games/${codeValue(data)}`), g=(await tx.get(ref)).data(); member(g,uid);
  if(g.hostId!==uid) fail('Seul l’hôte peut lancer la partie.', 'permission-denied');
  if(g.status!=='waiting' || g.memberIds.length<2) fail('Il faut au moins deux joueurs.');
  const players=await tx.get(ref.collection('players')); if(players.docs.some(p=>!p.data().ready)) fail('Tous les joueurs doivent être prêts.');
  const hidingSeconds=data.hidingSeconds, seekingSeconds=data.seekingSeconds;
  if(!Number.isInteger(hidingSeconds)||hidingSeconds<30||hidingSeconds>600||!Number.isInteger(seekingSeconds)||seekingSeconds<60||seekingSeconds>3600) fail('Durées invalides.', 'invalid-argument');
  const now=Date.now();
  tx.update(ref,{status:'started',startedAt:Timestamp.fromMillis(now),endsAt:Timestamp.fromMillis(now+(hidingSeconds+seekingSeconds)*1000),hidingSeconds,seekingSeconds,foundGhostIds:[]}); return {ok:true};
}));
exports.captureGhost = callable(async(uid,data)=>db.runTransaction(async tx=> {
  const ref=db.doc(`games/${codeValue(data)}`), g=(await tx.get(ref)).data(); member(g,uid);
  if(g.hostId!==uid || !g.memberIds.includes(data.ghostId) || data.ghostId===uid) fail('Capture interdite.', 'permission-denied');
  if(g.foundGhostIds.includes(data.ghostId)) return {ok:true};
  const host=(await tx.get(ref.collection('players').doc(uid))).data(), ghost=(await tx.get(ref.collection('players').doc(data.ghostId))).data();
  if(!canCapture(g,host,ghost,Date.now())) fail('Capture non validée : rapproche-toi et attends des positions GPS précises et récentes.');
  tx.update(ref,{foundGhostIds:FieldValue.arrayUnion(data.ghostId)}); return {ok:true};
}));
async function settle(code,uid) {
  return db.runTransaction(async tx=> {
    const ref=db.doc(`games/${code}`), snap=await tx.get(ref), g=snap.data();
    if(uid) member(g,uid);
    if(!g || g.status!=='started') return {ok:true};
    const ghosts=g.activePlayerIds.filter(id=>id!==g.hostId), allFound=ghosts.length>0 && ghosts.every(id=>g.foundGhostIds.includes(id));
    if(!allFound && Date.now()<g.endsAt.toMillis()) {if(uid) fail('La partie n’est pas terminée.'); return {ok:false};}
    const settled={...g,winner:allFound?'pacman':'ghosts'};
    // Read the entire reward set before any writes. A single transaction serializes retries and races.
    const docs=await Promise.all(g.activePlayerIds.map(id=>tx.get(profileRef(id))));
    const season=seasonKey();
    const seasonal=await Promise.all(g.activePlayerIds.map(id=>tx.get(db.doc(`seasons/${season}/scores/${id}`))));
    docs.forEach((ps,i)=> {
      if(!ps.exists) return;
      const id=ps.id,p=ps.data(),r=rewardFor(settled,id),xp=p.xp+r.xp;
      const earned=unlockedPassSkins(xp);
      const next={...p,score:p.score+r.score,coins:p.coins+r.coins,xp,games:p.games+1,victories:p.victories+(r.won?1:0),captures:(p.captures||0)+(id===g.hostId?g.foundGhostIds.length:0),ownedSkins:[...new Set([...p.ownedSkins,...earned])]};
      tx.update(ps.ref,{score:next.score,coins:next.coins,xp:next.xp,games:next.games,victories:next.victories,captures:next.captures,ownedSkins:next.ownedSkins,activeGame:FieldValue.delete()});
      tx.set(publicRef(id),publicData(next));
      tx.set(seasonal[i].ref,{...publicData(next),score:(seasonal[i].data()?.score||0)+r.score});
      tx.create(ps.ref.collection('history').doc(code),{code,won:r.won,score:r.score,coins:r.coins,xp:r.xp,role:id===g.hostId?'pacman':'ghost',at:stamp()});
    });
    tx.update(ref,{status:'finished',winner:settled.winner,finishedAt:stamp(),rewarded:true}); return {ok:true};
  });
}
exports.finishGame=callable((uid,data)=>settle(codeValue(data),uid));
exports.leaveGame=callable(async(uid,data)=>db.runTransaction(async tx=> {
  const code=codeValue(data),ref=db.doc(`games/${code}`),g=(await tx.get(ref)).data(); member(g,uid);
  const cancelled=(g.status==='waiting' && g.hostId===uid) || (g.status==='started' && (g.hostId===uid || g.activePlayerIds.length<=2));
  const profiles=await Promise.all((cancelled?g.memberIds:[uid]).map(id=>tx.get(profileRef(id))));
  if(g.status==='waiting') {
    tx.update(ref,{memberIds:FieldValue.arrayRemove(uid),activePlayerIds:FieldValue.arrayRemove(uid),...(g.hostId===uid?{status:'cancelled'}:{})});
    tx.delete(ref.collection('players').doc(uid));
  } else if(g.status==='started') {
    tx.update(ref,{activePlayerIds:FieldValue.arrayRemove(uid),...((g.hostId===uid || g.activePlayerIds.length<=2)?{status:'cancelled'}:{})});
    tx.update(ref.collection('players').doc(uid),{active:false});
  }
  profiles.forEach(ps=>{if(ps.exists && ps.data().activeGame===code) tx.update(ps.ref,{activeGame:FieldValue.delete()});});
  return {ok:true};
}));
exports.serverTime=callable(async()=>({now:Date.now()}));
exports.leaderboard=callable(async(uid,data)=> {
  const scope=['general','season','friends'].includes(data.scope)?data.scope:'general';
  const collection=scope==='season'?db.collection(`seasons/${seasonKey()}/scores`):db.collection('players');
  const toRow=doc=>({uid:doc.id,...doc.data()});
  if(scope==='friends') {
    const friends=await profileRef(uid).collection('friends').limit(200).get();
    const ids=[uid,...friends.docs.map(d=>d.id)],rows=[];
    for(let i=0;i<ids.length;i+=30) {
      const snap=await collection.where('__name__','in',ids.slice(i,i+30)).get(); rows.push(...snap.docs.map(toRow));
    }
    rows.sort((a,b)=>b.score-a.score || a.uid.localeCompare(b.uid,'en'));
    return {entries:rows,rank:rows.findIndex(r=>r.uid===uid)+1,hasMore:false};
  }
  let query=collection.orderBy('score','desc').orderBy('__name__').limit(50);
  if(data.cursor && Number.isInteger(data.cursor.score) && typeof data.cursor.uid==='string' && /^[A-Za-z0-9_-]{1,128}$/.test(data.cursor.uid)) query=query.startAfter(data.cursor.score,data.cursor.uid);
  const [snap,me]=await Promise.all([query.get(),collection.doc(uid).get()]);
  let rank=null;
  if(me.exists) {
    const score=me.data().score;
    const [above,ties]=await Promise.all([collection.where('score','>',score).count().get(),collection.where('score','==',score).where('__name__','<',uid).count().get()]);
    rank=above.data().count+ties.data().count+1;
  }
  const last=snap.docs.at(-1);
  return {entries:snap.docs.map(toRow),rank,hasMore:snap.size===50,cursor:last?{score:last.data().score,uid:last.id}:null};
});
exports.finishExpiredGames=onSchedule({schedule:'every 1 minutes',region,maxInstances:1},async()=> {
  const games=await db.collection('games').where('status','==','started').where('endsAt','<=',Timestamp.now()).limit(100).get();
  await Promise.all(games.docs.map(g=>settle(g.id,null)));
});
exports.sendFriendRequest=callable(async(uid,data)=> {
  const code=typeof data.friendCode==='string'?data.friendCode.trim().toUpperCase():'';
  if(!/^[A-F0-9]{12}$/.test(code)) fail('Code ami invalide.', 'invalid-argument');
  const target=(await db.doc(`friendCodes/${code}`).get()).data()?.uid;
  if(!target) fail('Joueur introuvable.', 'not-found'); if(target===uid) fail('C’est ton propre code.');
  return db.runTransaction(async tx=> {
    const p=(await tx.get(profileRef(uid))).data(), friend=await tx.get(profileRef(uid).collection('friends').doc(target));
    if(!p) fail('Profil indisponible.'); if(friend.exists) fail('Vous êtes déjà amis.');
    tx.set(profileRef(target).collection('requests').doc(uid),{uid,name:p.name,at:stamp()}); return {ok:true};
  });
});
exports.respondFriendRequest=callable(async(uid,data)=>db.runTransaction(async tx=> {
  if(typeof data.uid!=='string'||! /^[A-Za-z0-9_-]{1,128}$/.test(data.uid)) fail('Joueur invalide.');
  const req=profileRef(uid).collection('requests').doc(data.uid), request=await tx.get(req);
  if(!request.exists) fail('Cette demande n’existe plus.');
  const me=(await tx.get(publicRef(uid))).data(),other=(await tx.get(publicRef(data.uid))).data();
  if(data.accept===true && me && other) {
    tx.set(profileRef(uid).collection('friends').doc(data.uid),{uid:data.uid,name:other.name});
    tx.set(profileRef(data.uid).collection('friends').doc(uid),{uid,name:me.name});
    tx.delete(profileRef(data.uid).collection('requests').doc(uid));
  }
  tx.delete(req); return {ok:true};
}));
exports.removeFriend=callable(async(uid,data)=> {
  if(typeof data.uid!=='string'||! /^[A-Za-z0-9_-]{1,128}$/.test(data.uid)) fail('Joueur invalide.');
  const batch=db.batch(); batch.delete(profileRef(uid).collection('friends').doc(data.uid)); batch.delete(profileRef(data.uid).collection('friends').doc(uid)); await batch.commit(); return {ok:true};
});
exports.inviteFriend=callable(async(uid,data)=>db.runTransaction(async tx=> {
  const code=codeValue(data); if(typeof data.uid!=='string'||! /^[A-Za-z0-9_-]{1,128}$/.test(data.uid)) fail('Joueur invalide.');
  const g=(await tx.get(db.doc(`games/${code}`))).data(); member(g,uid);
  const friend=await tx.get(profileRef(uid).collection('friends').doc(data.uid)), p=(await tx.get(publicRef(uid))).data();
  if(!friend.exists||g.status!=='waiting') fail('Invitation impossible.');
  tx.set(profileRef(data.uid).collection('invites').doc(code),{code,from:p.name,at:stamp()}); return {ok:true};
}));
