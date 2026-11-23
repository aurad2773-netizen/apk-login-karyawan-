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

test("Unauthenticated user: cannot read, create, or list user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice_p",
      email: "alice@example.com",
      registrationMethod: "manual",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner: valid manual registration create, read, and partial update succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice.pratama",
      email: "alice@example.com",
      phone: "081234567890",
      occupation: "Desainer Produk",
      bio: "Halo semuanya, senang bergabung!",
      registrationMethod: "manual",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Alice Pratama Putri",
      occupation: "Lead Designer",
      bio: "Profil diperbarui!",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated user: rejects ghost fields, @DocumentId id field, invalid username, and immutable field mutation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Reject @DocumentId 'id' inside payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice_p",
      email: "alice@example.com",
      registrationMethod: "manual",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject ghost field (Shadow Update test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice_p",
      email: "alice@example.com",
      registrationMethod: "manual",
      isAdmin: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject invalid username (spaces / too short)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "al",
      email: "alice@example.com",
      registrationMethod: "manual",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice_p",
      email: "alice@example.com",
      registrationMethod: "manual",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Create valid doc first, then verify immutable fields cannot be modified
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Pratama",
      username: "alice_p",
      email: "alice@example.com",
      registrationMethod: "manual",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      registrationMethod: "google_quick",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation: Alice cannot read, write, delete Bob's profile or list /users", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Bob Santoso",
      username: "bob_santoso",
      email: "bob@example.com",
      registrationMethod: "manual",
      createdAt: new Date(Date.now() - 5000),
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      fullName: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).delete());
});
