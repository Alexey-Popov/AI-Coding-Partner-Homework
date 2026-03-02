const request = require("supertest");
const app = require("../src/app");
const store = require("../src/store");
const fixtures = require("./fixtures/tickets");

describe("Ticket API Endpoints", () => {
  beforeEach(() => {
    store.resetStore();
  });

  test("POST /tickets should create a new ticket", async () => {
    const res = await request(app)
      .post("/tickets")
      .send(fixtures.validTicket);
    expect(res.status).toBe(201);
    expect(res.body.id).toBeDefined();
    expect(res.body.customer_email).toBe(fixtures.validTicket.customer_email);
  });

  test("POST /tickets should reject invalid ticket", async () => {
    const res = await request(app)
      .post("/tickets")
      .send(fixtures.invalidTicketMissingEmail);
    expect(res.status).toBe(400);
    expect(res.body.error).toBe("Validation failed");
  });

  test("GET /tickets should list all tickets", async () => {
    await request(app).post("/tickets").send(fixtures.validTicket);
    await request(app).post("/tickets").send(fixtures.validTicket2);
    const res = await request(app).get("/tickets");
    expect(res.status).toBe(200);
    expect(res.body.total).toBe(2);
    expect(res.body.tickets).toHaveLength(2);
  });

  test("GET /tickets should filter by status", async () => {
    const ticket = { ...fixtures.validTicket, status: "in_progress" };
    await request(app).post("/tickets").send(fixtures.validTicket);
    await request(app).post("/tickets").send(ticket);
    const res = await request(app).get("/tickets?status=in_progress");
    expect(res.status).toBe(200);
    expect(res.body.tickets).toHaveLength(1);
    expect(res.body.tickets[0].status).toBe("in_progress");
  });

  test("GET /tickets should filter by category", async () => {
    await request(app).post("/tickets").send(fixtures.validTicket);
    await request(app).post("/tickets").send(fixtures.validTicket2);
    const res = await request(app).get("/tickets?category=billing_question");
    expect(res.status).toBe(200);
    expect(res.body.tickets).toHaveLength(1);
  });

  test("GET /tickets/:id should return specific ticket", async () => {
    const createRes = await request(app).post("/tickets").send(fixtures.validTicket);
    const ticketId = createRes.body.id;
    const res = await request(app).get(`/tickets/${ticketId}`);
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(ticketId);
  });

  test("GET /tickets/:id should return 404 for non-existent ticket", async () => {
    const res = await request(app).get("/tickets/nonexistent");
    expect(res.status).toBe(404);
    expect(res.body.error).toBe("Ticket not found");
  });

  test("PUT /tickets/:id should update a ticket", async () => {
    const createRes = await request(app).post("/tickets").send(fixtures.validTicket);
    const ticketId = createRes.body.id;
    const res = await request(app)
      .put(`/tickets/${ticketId}`)
      .send({ status: "resolved" });
    expect(res.status).toBe(200);
    expect(res.body.status).toBe("resolved");
    expect(res.body.updated_at).toBeDefined();
  });

  test("PUT /tickets/:id should return 404 for non-existent ticket", async () => {
    const res = await request(app)
      .put("/tickets/nonexistent")
      .send({ status: "resolved" });
    expect(res.status).toBe(404);
  });

  test("DELETE /tickets/:id should delete a ticket", async () => {
    const createRes = await request(app).post("/tickets").send(fixtures.validTicket);
    const ticketId = createRes.body.id;
    const res = await request(app).delete(`/tickets/${ticketId}`);
    expect(res.status).toBe(204);
    const getRes = await request(app).get(`/tickets/${ticketId}`);
    expect(getRes.status).toBe(404);
  });

  test("DELETE /tickets/:id should return 404 for non-existent ticket", async () => {
    const res = await request(app).delete("/tickets/nonexistent");
    expect(res.status).toBe(404);
  });

  test("GET /tickets should handle multiple filters", async () => {
    const ticket1 = fixtures.validTicket;
    const ticket2 = { ...fixtures.validTicket2, status: "in_progress" };
    const ticket3 = { ...fixtures.validTicket3, status: "in_progress" };
    await request(app).post("/tickets").send(ticket1);
    await request(app).post("/tickets").send(ticket2);
    await request(app).post("/tickets").send(ticket3);
    const res = await request(app).get("/tickets?status=in_progress&category=billing_question");
    expect(res.status).toBe(200);
    expect(res.body.tickets).toHaveLength(1);
    expect(res.body.tickets[0].category).toBe("billing_question");
  });
});
