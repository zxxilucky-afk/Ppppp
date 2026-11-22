const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("educational_articles").get());
  await assertFails(unauthDb.collection("support_requests").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("diary_entries").doc("d1").set({
      userId: ALICE_UID,
      title: "Secret",
      body: "Body",
      dateEpochDay: 20400,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profile creation, update, teen ovulation guard, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Teen cannot enable ovulation tracking
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      preferredName: "Asha",
      ageGroup: "teen_13_17",
      language: "hi",
      parentalConsentConfirmed: true,
      aiHealthDataConsent: false,
      onboardingCompleted: true,
      averageCycleLength: 28,
      averagePeriodLength: 5,
      ovulationTrackingEnabled: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid teen profile succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      preferredName: "Asha",
      ageGroup: "teen_13_17",
      language: "hi",
      parentalConsentConfirmed: true,
      aiHealthDataConsent: false,
      onboardingCompleted: true,
      averageCycleLength: 28,
      averagePeriodLength: 5,
      ovulationTrackingEnabled: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Shadow update with ghost field isAdmin fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot read or write Alice's profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      preferredName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Private health records (period, symptom, mood, diary) are isolated to owner and blocked from admins", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("admins").doc(ADMIN_UID).set({ enabled: true });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "luckymbbs44@gmail.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  // Alice creates period record, symptom record, mood record, and diary entry
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("period_records").doc("p1").set({
      userId: ALICE_UID,
      startDateEpochDay: 20400,
      endDateEpochDay: 20404,
      flowIntensity: "medium",
      notes: "Day 1",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("symptom_records").doc("s1").set({
      userId: ALICE_UID,
      dateEpochDay: 20400,
      symptoms: ["cramps", "fatigue"],
      painIntensity: 5,
      redFlagAlert: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("mood_records").doc("m1").set({
      userId: ALICE_UID,
      dateEpochDay: 20400,
      mood: "calm".replace("calm", "relaxed"),
      energyLevel: 4,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("diary_entries").doc("d1").set({
      userId: ALICE_UID,
      title: "Private thought",
      body: "Feeling hopeful today.",
      dateEpochDay: 20400,
      moodTag: "relaxed",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update on diary preserves createdAt
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("diary_entries").doc("d1").update({
      body: "Updated private reflection.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("diary_entries").doc("d2").set({
      userId: ALICE_UID,
      title: "Future",
      body: "Tampered",
      dateEpochDay: 20400,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Bob AND Admin CANNOT read Alice's private diary, period, symptom, or mood records
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("diary_entries").doc("d1").get());
  await assertFails(adminDb.collection("users").doc(ALICE_UID).collection("diary_entries").get());
  await assertFails(adminDb.collection("users").doc(ALICE_UID).collection("period_records").get());
  await assertFails(adminDb.collection("users").doc(ALICE_UID).collection("symptom_records").get());
  await assertFails(adminDb.collection("users").doc(ALICE_UID).collection("mood_records").get());
});

test("Admin content collections & spoofed email rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_1", { email: "luckymbbs44@gmail.com", email_verified: false }).firestore();
  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "luckymbbs44@gmail.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  const articlePayload = {
    titleEn: "Safe Hygiene",
    titleHi: "सुरक्षित स्वच्छता",
    summaryEn: "Clean habits",
    summaryHi: "स्वच्छ आदतें",
    bodyEn: "Change pads every 4 to 6 hours.",
    bodyHi: "हर 4 से 6 घंटे में पैड बदलें।",
    category: "hygiene",
    ageAppropriateFor: "all",
    reviewStatus: "draft_pending_doctor_review",
    createdAt: now,
    updatedAt: now,
  };

  // Non-admin & unverified email spoof cannot create articles
  await assertFails(aliceDb.collection("educational_articles").doc("art_1").set(articlePayload));
  await assertFails(spoofedAdminDb.collection("educational_articles").doc("art_1").set(articlePayload));

  // Real admin creates article, signed-in user reads it
  await assertSucceeds(realAdminDb.collection("educational_articles").doc("art_1").set(articlePayload));
  await assertSucceeds(aliceDb.collection("educational_articles").get());
});

test("Support requests scoped to user and admin", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "luckymbbs44@gmail.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("support_requests").doc("sr_1").set({
      userId: ALICE_UID,
      subject: "Question",
      message: "Need help with reminder times.",
      status: "open",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered query by Alice succeeds, unfiltered list by Alice fails
  await assertSucceeds(aliceDb.collection("support_requests").where("userId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("support_requests").get());

  // Bob cannot read Alice's ticket
  await assertFails(bobDb.collection("support_requests").doc("sr_1").get());

  // Admin can list & reply
  await assertSucceeds(realAdminDb.collection("support_requests").get());
  await assertSucceeds(
    realAdminDb.collection("support_requests").doc("sr_1").update({
      status: "resolved",
      adminReply: "Updated in settings.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
