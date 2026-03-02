const express = require("express");
const ticketsRouter = require("./routes/tickets");

const app = express();

app.use(express.json({ limit: "2mb" }));
app.use(express.urlencoded({ extended: false }));

app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});

app.use("/tickets", ticketsRouter);

app.use((err, req, res, next) => {
  const status = err.status || 500;
  const message = err.message || "Internal Server Error";
  res.status(status).json({ error: message, details: err.details || null });
});

module.exports = app;
