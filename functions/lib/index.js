"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.markTaskClaimed = exports.seedMyRewards = exports.rollWeeklyRewards = exports.rollDailyRewards = void 0;
/* eslint-disable object-curly-spacing */
/* eslint-disable max-len */
/* eslint-disable require-jsdoc */
const admin = __importStar(require("firebase-admin"));
const scheduler_1 = require("firebase-functions/v2/scheduler");
const https_1 = require("firebase-functions/v2/https");
const firestore_1 = require("firebase-functions/v2/firestore");
admin.initializeApp();
const db = admin.firestore();
const TZ = "Australia/Melbourne"; // change for your locale
// ---- time keys --------------------------------------------------------------
function todayKey(date = new Date()) {
    const y = new Intl.DateTimeFormat("en-CA", { timeZone: TZ, year: "numeric" }).format(date);
    const m = new Intl.DateTimeFormat("en-CA", { timeZone: TZ, month: "2-digit" }).format(date);
    const d = new Intl.DateTimeFormat("en-CA", { timeZone: TZ, day: "2-digit" }).format(date);
    return `${y}-${m}-${d}`; // e.g. 2025-10-15
}
function weekKey(date = new Date()) {
    // returns 2025-W42 (ISO week)
    const y = new Intl.DateTimeFormat("en-CA", { timeZone: TZ, year: "numeric" }).format(date);
    const dt = new Date(date);
    dt.setUTCHours(0, 0, 0, 0);
    const day = (dt.getUTCDay() + 6) % 7;
    dt.setUTCDate(dt.getUTCDate() - day + 3);
    const firstThursday = new Date(Date.UTC(dt.getUTCFullYear(), 0, 4));
    const week = 1 + Math.round(((dt.getTime() - firstThursday.getTime()) / 86400000 - 3) / 7);
    return `${y}-W${String(week).padStart(2, "0")}`;
}
async function getAllUserIds() {
    // assumes you keep a doc per user in /users/{uid}
    const snap = await db.collection("users").select().get();
    return snap.docs.map((d) => d.id);
}
async function fetchTemplates(cadence) {
    const snap = await db
        .collection("rewardTemplates")
        .where("cadence", "==", cadence)
        .where("active", "==", true)
        .get();
    return snap.docs.map((d) => (Object.assign({ id: d.id }, d.data())));
}
function pickSome(templates, count) {
    if (templates.length <= count)
        return templates;
    const arr = templates.slice();
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr.slice(0, count);
}
function makeTask(t, periodKey) {
    return {
        templateId: t.id,
        title: t.title,
        description: t.description,
        points: t.points,
        cadence: t.cadence,
        periodKey,
        claimed: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };
}
// ---- scheduled jobs ---------------------------------------------------------
// Daily roll at 00:05 local time
exports.rollDailyRewards = (0, scheduler_1.onSchedule)({ schedule: "5 0 * * *", timeZone: TZ }, async () => {
    const key = todayKey();
    const templates = await fetchTemplates("daily");
    if (!templates.length)
        return;
    const users = await getAllUserIds();
    const tasks = pickSome(templates, 4).map((t) => makeTask(t, key));
    const batch = db.batch();
    for (const uid of users) {
        const ref = db.collection("users").doc(uid).collection("rewardTasks").doc(key);
        batch.set(ref, { tasks }, { merge: true });
    }
    await batch.commit();
});
// Weekly roll (Mon 00:10 local time)
exports.rollWeeklyRewards = (0, scheduler_1.onSchedule)({ schedule: "10 0 * * 1", timeZone: TZ }, async () => {
    const key = weekKey();
    const templates = await fetchTemplates("weekly");
    if (!templates.length)
        return;
    const users = await getAllUserIds();
    const tasks = pickSome(templates, 2).map((t) => makeTask(t, key));
    const batch = db.batch();
    for (const uid of users) {
        const ref = db.collection("users").doc(uid).collection("rewardTasks").doc(key);
        batch.set(ref, { tasks: admin.firestore.FieldValue.arrayUnion(...tasks) }, { merge: true });
    }
    await batch.commit();
});
// ---- callable (for testing from Android) ------------------------------------
exports.seedMyRewards = (0, https_1.onCall)(async (req) => {
    var _a;
    const uid = (_a = req.auth) === null || _a === void 0 ? void 0 : _a.uid;
    if (!uid)
        throw new Error("Unauthenticated");
    const dKey = todayKey();
    const wKey = weekKey();
    const daily = await fetchTemplates("daily");
    const weekly = await fetchTemplates("weekly");
    const dTasks = pickSome(daily, 4).map((t) => makeTask(t, dKey));
    const wTasks = pickSome(weekly, 2).map((t) => makeTask(t, wKey));
    await db.collection("users").doc(uid).collection("rewardTasks").doc(dKey)
        .set({ tasks: dTasks }, { merge: true });
    await db.collection("users").doc(uid).collection("rewardTasks").doc(wKey)
        .set({ tasks: admin.firestore.FieldValue.arrayUnion(...wTasks) }, { merge: true });
    return { ok: true };
});
// ---- mark task claimed when your app writes to /rewards ---------------------
// Your app already writes: users/{uid}/rewards/{docId}
// where docId = `${templateId}_${periodKey}` from your Rewards screen.
// This hook flags the corresponding task in rewardTasks as claimed=true.
exports.markTaskClaimed = (0, firestore_1.onDocumentCreated)("users/{uid}/rewards/{rewardId}", async (event) => {
    const { uid, rewardId } = event.params;
    const [templateId, periodKey] = rewardId.split("_");
    if (!templateId || !periodKey)
        return;
    const taskRef = db.collection("users").doc(uid).collection("rewardTasks").doc(periodKey);
    const snap = await taskRef.get();
    if (!snap.exists)
        return;
    const data = snap.data();
    if (!(data === null || data === void 0 ? void 0 : data.tasks))
        return;
    const updated = data.tasks.map((t) => t.templateId === templateId ? Object.assign(Object.assign({}, t), { claimed: true }) : t);
    await taskRef.set({ tasks: updated }, { merge: true });
});
//# sourceMappingURL=index.js.map