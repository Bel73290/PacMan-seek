# Historical Blaze backend

This Cloud Functions implementation is archived for reference. It is not loaded,
deployed, or called by the Spark Android app. The active backend consists of
Firestore security rules and Android SparkService transactions. The old tests in
`test/rules` and `test/integration` describe this previous implementation; the
current validation command runs `test/domain.test.js` and `test/spark` only.
