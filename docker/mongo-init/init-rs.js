// initialise le replica set rs0 avec le host accessible depuis ta machine
try {
  rs.status();
} catch (e) {
  rs.initiate({
    _id: "rs0",
    members: [{ _id: 0, host: "localhost:27017" }],
  });
}
