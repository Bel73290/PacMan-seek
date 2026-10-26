"use strict";
const catalog = require('./catalog.json');
const DEFAULT_SKINS = ['ghost_red_classic', 'pacman_classic'];
function levelFor(xp) { return 1 + Math.floor(Math.max(0, xp) / 500); }
function unlockedPassSkins(xp) { return catalog.filter(s=>s.unlockLevel>0 && s.unlockLevel<=levelFor(xp)).map(s=>s.id); }
function seasonKey(now = new Date()) { return now.toISOString().slice(0, 7); }
function distance(a, b) {
  const rad = x => x * Math.PI / 180;
  const lat = rad(b.lat - a.lat), lng = rad(b.lng - a.lng);
  const s = Math.sin(lat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(lng / 2) ** 2;
  return 6371000 * 2 * Math.atan2(Math.sqrt(s), Math.sqrt(Math.max(0, 1 - s)));
}
function canCapture(game, host, ghost, now) {
  const start = game.startedAt?.toMillis?.();
  if (game.status !== 'started' || !start || now < start + game.hidingSeconds * 1000 || now >= game.endsAt.toMillis()) return false;
  return [host, ghost].every(p => Number.isFinite(p?.lat) && Number.isFinite(p?.lng) && p.accuracy >= 0 && p.accuracy <= 25 &&
    p.locationUpdatedAt?.toMillis?.() <= now && now - p.locationUpdatedAt.toMillis() <= 30000 &&
    p.measuredAt?.toMillis?.() <= now+5000 && now-p.measuredAt.toMillis() <= 30000 && p.active !== false) && distance(host, ghost) <= 15;
}
function rewardFor(game, uid) {
  const host = uid === game.hostId;
  const won = host ? game.winner === 'pacman' : game.winner === 'ghosts' && !game.foundGhostIds.includes(uid);
  const captures = host ? game.foundGhostIds.length : 0;
  return {score: 20 + captures * 50 + (won ? 100 : 0), xp: 50 + captures * 25 + (won ? 100 : 0), coins: 25 + captures * 10 + (won ? 50 : 0), won};
}
function inventoryAfterPurchase(profile, id) {
  const item = catalog.find(s => s.id === id);
  if (!item) throw new Error('Skin inconnu.');
  const owned = profile.ownedSkins || DEFAULT_SKINS;
  if (owned.includes(id)) return {coins: profile.coins, ownedSkins: owned};
  if (item.unlockLevel>0) throw new Error(`Skin du pass de combat : niveau ${item.unlockLevel} requis.`);
  if ((profile.coins || 0) < item.price) throw new Error('Pas assez de pièces.');
  return {coins: profile.coins - item.price, ownedSkins: [...owned, id]};
}
module.exports = {catalog, DEFAULT_SKINS, levelFor, unlockedPassSkins, seasonKey, distance, canCapture, rewardFor, inventoryAfterPurchase};
