const {test,before,after,beforeEach}=require('node:test');
const fs=require('node:fs');
const path=require('node:path');
const {initializeTestEnvironment,assertSucceeds,assertFails}=require('@firebase/rules-unit-testing');
const {doc,getDoc,setDoc,updateDoc,serverTimestamp,Timestamp}=require('firebase/firestore');
let env;
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-pac-and-seek',firestore:{rules:fs.readFileSync(path.join(__dirname,'../../../firestore.rules'),'utf8')}});});
after(async()=>{await env.cleanup();});
beforeEach(async()=>{
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async c=>{
    const db=c.firestore();
    await setDoc(doc(db,'users/alice'),{coins:1000});
    await setDoc(doc(db,'players/alice'),{name:'Alice',score:5});
    await setDoc(doc(db,'games/ABCDEF'),{status:'started',memberIds:['alice','bob']});
    await setDoc(doc(db,'games/ABCDEF/players/alice'),{active:true,isHost:true,name:'Alice'});
    await setDoc(doc(db,'games/ABCDEF/players/bob'),{active:true,isHost:false,name:'Bob'});
  });
});
const client=uid=>env.authenticatedContext(uid).firestore();
test('private profiles are visible only to their owner; coins cannot be edited',async()=>{
  await assertSucceeds(getDoc(doc(client('alice'),'users/alice')));
  await assertFails(getDoc(doc(client('bob'),'users/alice')));
  await assertFails(updateDoc(doc(client('alice'),'users/alice'),{coins:99999}));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(),'players/alice')));
});
test('outsiders cannot read game positions or give themselves membership',async()=>{
  await assertFails(getDoc(doc(client('mallory'),'games/ABCDEF/players/bob')));
  await assertFails(updateDoc(doc(client('mallory'),'games/ABCDEF'),{memberIds:['alice','bob','mallory']}));
  await assertFails(setDoc(doc(client('mallory'),'games/ABCDEF/players/mallory'),{active:true}));
});
test('members publish only their own current valid GPS readings',async()=>{
  const valid={lat:45,lng:5,accuracy:5,heading:60,locationUpdatedAt:serverTimestamp(),measuredAt:Timestamp.now()};
  await assertSucceeds(updateDoc(doc(client('alice'),'games/ABCDEF/players/alice'),valid));
  await assertFails(updateDoc(doc(client('bob'),'games/ABCDEF/players/alice'),valid));
  await assertFails(updateDoc(doc(client('alice'),'games/ABCDEF/players/alice'),{...valid,isHost:false}));
  await assertFails(updateDoc(doc(client('alice'),'games/ABCDEF/players/alice'),{...valid,ghostSkin:'premium_ghost_gold_king'}));
  await assertFails(updateDoc(doc(client('alice'),'games/ABCDEF/players/alice'),{...valid,lat:91}));
  await assertFails(updateDoc(doc(client('alice'),'games/ABCDEF/players/alice'),{...valid,measuredAt:Timestamp.fromMillis(Date.now()-60000)}));
});
test('client cannot confirm captures, rewards, or friendships',async()=>{
  await assertFails(updateDoc(doc(client('alice'),'games/ABCDEF'),{foundGhostIds:['bob'],status:'finished'}));
  await assertFails(setDoc(doc(client('alice'),'users/alice/history/ABCDEF'),{score:9000}));
  await assertFails(setDoc(doc(client('alice'),'users/alice/friends/bob'),{uid:'bob'}));
  await assertFails(updateDoc(doc(client('alice'),'players/alice'),{score:9999}));
});
