/* eslint-disable object-curly-spacing */
/* eslint-disable max-len */
/* eslint-disable require-jsdoc */
import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onCall } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

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

// ---- data helpers -----------------------------------------------------------

type Template = {
  id: string;
  title: string;
  description: string;
  points: number;
  cadence: "daily" | "weekly";
  active?: boolean;
  weight?: number;
};

async function getAllUserIds(): Promise<string[]> {
  // assumes you keep a doc per user in /users/{uid}
  const snap = await db.collection("users").select().get();
  return snap.docs.map((d) => d.id);
}

async function fetchTemplates(cadence: "daily" | "weekly"): Promise<Template[]> {
  const snap = await db
    .collection("rewardTemplates")
    .where("cadence", "==", cadence)
    .where("active", "==", true)
    .get();

  return snap.docs.map((d) => ({ id: d.id, ...(d.data() as Omit<Template, "id">) }));
}

function pickSome(templates: Template[], count: number): Template[] {
  if (templates.length <= count) return templates;
  const arr = templates.slice();
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr.slice(0, count);
}

function makeTask(t: Template, periodKey: string) {
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
export const rollDailyRewards = onSchedule({ schedule: "5 0 * * *", timeZone: TZ }, async () => {
  const key = todayKey();
  const templates = await fetchTemplates("daily");
  if (!templates.length) return;

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
export const rollWeeklyRewards = onSchedule(
  { schedule: "10 0 * * 1", timeZone: TZ },
  async () => {
    const key = weekKey();
    const templates = await fetchTemplates("weekly");
    if (!templates.length) return;

    const users = await getAllUserIds();
    const tasks = pickSome(templates, 2).map((t) => makeTask(t, key));

    const batch = db.batch();
    for (const uid of users) {
      const ref = db.collection("users").doc(uid).collection("rewardTasks").doc(key);
      batch.set(ref, { tasks: admin.firestore.FieldValue.arrayUnion(...tasks) }, { merge: true });
    }
    await batch.commit();
  }
);

// ---- callable (for testing from Android) ------------------------------------

export const seedMyRewards = onCall(async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new Error("Unauthenticated");

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
export const markTaskClaimed = onDocumentCreated(
  "users/{uid}/rewards/{rewardId}",
  async (event) => {
    const { uid, rewardId } = event.params as { uid: string; rewardId: string };
    const [templateId, periodKey] = rewardId.split("_");
    if (!templateId || !periodKey) return;

    const taskRef = db.collection("users").doc(uid).collection("rewardTasks").doc(periodKey);
    const snap = await taskRef.get();
    if (!snap.exists) return;

    const data = snap.data() as { tasks?: any[] } | undefined;
    if (!data?.tasks) return;

    const updated = data.tasks.map((t) =>
      t.templateId === templateId ? { ...t, claimed: true } : t
    );

    await taskRef.set({ tasks: updated }, { merge: true });
  }
);
