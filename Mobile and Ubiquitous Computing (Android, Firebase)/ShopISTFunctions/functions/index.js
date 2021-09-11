const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();
const fieldValue = admin.firestore.FieldValue;

exports.getPantryToken = functions.https
    .onCall(async (data, context) => {
      const {pantryId} = data;
      return await getToken(context.auth.uid, "Pantry", pantryId);
    });

exports.getShoppingToken = functions.https
    .onCall(async (data, context) => {
      const {shoppingId} = data;
      return await getToken(context.auth.uid, "Shopping", shoppingId);
    });

exports.getListId = functions.https
    .onCall(async (data, context) => {
      const {token} = data;
      if (token.startsWith("Pantry&")) {
        return await addUidToPantry(token, context.auth.uid);
      } else if (token.startsWith("Shopping&")) {
        return await addUidToShopping(token, context.auth.uid);
      }
      return null;
    });

exports.checkIn = functions.https
    .onCall(async (data, context) => {
      const {crowdShoppingId, numberOfItems} = data;
      const millis = Date.now();
      const doc = db.collection("LinearRegression").doc(crowdShoppingId);
      const lr = await doc.get();
      let m = 0;
      let b = 0;
      let totalNumberOfItems = parseInt(numberOfItems);
      if (!lr.exists) {
        await doc.set({
          TotalNumberOfItems: totalNumberOfItems,
          M: m,
          B: b,
          SumX: 0,
          SumY: 0,
          SumXX: 0,
          SumXY: 0,
          Count: 0,
          UsersInQueue: {
            [context.auth.uid]: {
              ArrivedMillis: millis,
              NumberOfItems: totalNumberOfItems,
            },
          },
        });
      } else {
        totalNumberOfItems += parseInt(lr.get("TotalNumberOfItems"));
        m = parseInt(lr.get("M"));
        b = parseInt(lr.get("B"));
        await doc.update({
          TotalNumberOfItems: totalNumberOfItems,
          [`UsersInQueue.${context.auth.uid}`]: {
            ArrivedMillis: millis,
            NumberOfItems: totalNumberOfItems,
          },
        });
      }
      await db.collection("CrowdShopping").doc(crowdShoppingId).update({
        QueueTime: (totalNumberOfItems * m + b) / 1000,
      });
    });

exports.checkOut = functions.https
    .onCall(async (data, context) => {
      const {crowdShoppingId, numberOfItems} = data;
      const millis = Date.now();
      const doc = db.collection("LinearRegression").doc(crowdShoppingId);
      const lr = await doc.get();
      const x = parseInt(
          lr.get(`UsersInQueue.${context.auth.uid}.NumberOfItems`));
      const y = millis - parseInt(
          lr.get(`UsersInQueue.${context.auth.uid}.ArrivedMillis`));
      const sumX = parseInt(lr.get("SumX")) + x;
      const sumY = parseInt(lr.get("SumY")) + y;
      const sumXX = parseInt(lr.get("SumXX")) + x * x;
      const sumXY = parseInt(lr.get("SumXY")) + x * y;
      const count = parseInt(lr.get("Count")) + 1;
      const mDiv = count * sumXX - sumX * sumX;
      const m = mDiv !== 0 ? (count * sumXY - sumX * sumY) / mDiv : 0;
      const b = (sumY - m * sumX) / count;
      const totalNumberOfItems = parseInt(lr.get("TotalNumberOfItems")) -
            parseInt(numberOfItems);
      await doc.update({
        TotalNumberOfItems: totalNumberOfItems,
        M: m,
        B: b,
        SumX: sumX,
        SumY: sumY,
        SumXX: sumXX,
        SumXY: sumXY,
        Count: count,
        [`UsersInQueue.${context.auth.uid}`]: fieldValue.delete(),
      });
      await db.collection("CrowdShopping").doc(crowdShoppingId).update({
        QueueTime: (totalNumberOfItems * m + b) / 1000,
      });
    });

exports.updateSmartSort = functions.https
    .onCall(async (data, context) => {
      const {crowdShoppingId, bCrowdItemId, aCrowdItemIds} = data;
      const doc = db.collection("CrowdShopping").doc(crowdShoppingId);
      const crowdShopping = await doc.get();
      let bCrowdItem = {};
      const smartSort = crowdShopping.get("SmartSort");
      if (smartSort !== undefined && smartSort[bCrowdItemId] !== undefined) {
        bCrowdItem = smartSort[bCrowdItemId];
      }
      for (const aCrowdItemId of aCrowdItemIds) {
        bCrowdItem[aCrowdItemId] = bCrowdItem[aCrowdItemId] === undefined ?
                1 : parseInt(bCrowdItem[aCrowdItemId]) + 1;
      }
      await doc.update({
        [`SmartSort.${bCrowdItemId}`]: bCrowdItem,
      });
    });

exports.registerBarcode = functions.firestore
    .document("CrowdItem/{crowdItemId}")
    .onWrite(async (change, context) => {
      const doc = db.collection("Uniqueness").doc("CrowdItem");

      if (!change.before.exists) {
        // Create
        if (change.after.data().Barcode != null) {
          await doc.update({
            Barcodes: fieldValue.arrayUnion(change.after.data().Barcode),
          });
        }
      } else if (!change.after.exists) {
        // Delete
        if (change.before.data().Barcode != null) {
          await doc.update({
            Barcodes: fieldValue.arrayRemove(change.before.data().Barcode),
          });
        }
      } else if (change.before.data().Barcode !== change.after.data().Barcode) {
        // Edit
        const batch = db.batch();
        if (change.before.data().Barcode != null) {
          batch.update(doc, {
            Barcodes: fieldValue.arrayRemove(change.before.data().Barcode),
          });
        }
        if (change.after.data().Barcode != null) {
          batch.update(doc, {
            Barcodes: fieldValue.arrayUnion(change.after.data().Barcode),
          });
        }
        await batch.commit();
      }
    });

const getToken = async (uid, listName, listId) => {
  const list = await db.doc(`${listName}/${listId}`).get();
  if (!list.exists || !inArray(list.get("Users"), uid)) {
    return null;
  }
  const token = `${listName}&${generateToken(32)}`;
  await db.doc(`Token/${listName}Token`).set(
      {[token]: listId},
      {merge: true}
  );
  setTimeout(async () => {
    await db.doc(`Token/${listName}Token`).update({
      [token]: fieldValue.delete(),
    });
  }, 300000);
  return token;
};

const addUidToPantry = async (token, uid) => {
  const pantryToken = await db.doc("Token/PantryToken").get();
  const pantryId = pantryToken.get(token);
  await db.collection("Pantry").doc(pantryId).update({
    Users: fieldValue.arrayUnion(uid),
    [`PantryCarts.${uid}`]: {},
  });
  return pantryId;
};

const addUidToShopping = async (token, uid) => {
  const shoppingToken = await db.doc("Token/ShoppingToken").get();
  const shoppingId = shoppingToken.get(token);
  await db.collection("Shopping").doc(shoppingId).update({
    Users: fieldValue.arrayUnion(uid),
  });
  return shoppingId;
};

const inArray = (array, value) => {
  const length = array.length;
  for (let i = 0; i < length; i++) {
    if (array[i] === value) {
      return true;
    }
  }
  return false;
};

const generateToken = (length) => {
  const a = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
      .split("");
  const b = [];
  for (let i = 0; i < length; i++) {
    const j = (Math.random() * (a.length - 1)).toFixed(0);
    b[i] = a[j];
  }
  return b.join("");
};
