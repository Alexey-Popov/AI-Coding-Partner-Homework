const request = require("supertest");
const app = require("../src/app");
const store = require("../src/store");
const fixtures = require("./fixtures/tickets");

describe("Integration Tests", () => {
  beforeEach(() => {
    store.resetStore();
  });

  test("should complete full ticket lifecycle", async () => {
    // Create
    const createRes = await request(app)
      .post("/tickets")
      .send(fixtures.validTicket);
    expect(createRes.status).toBe(201);
    const ticketId = createRes.body.id;

    // Get
    let getRes = await request(app).get(`/tickets/${ticketId}`);
    expect(getRes.status).toBe(200);
    expect(getRes.body.status).toBe("new");

    // Update to in_progress
    let updateRes = await request(app)
      .put(`/tickets/${ticketId}`)
      .send({ status: "in_progress" });
    expect(updateRes.status).toBe(200);
    expect(updateRes.body.status).toBe("in_progress");

    // Auto-classify
    const classifyRes = await request(app)
      .post(`/tickets/${ticketId}/auto-classify`)
      .send({});
    expect(classifyRes.status).toBe(200);
    expect(classifyRes.body.category).toBeDefined();

    // Resolve
    updateRes = await request(app)
      .put(`/tickets/${ticketId}`)
      .send({ status: "resolved" });
    expect(updateRes.status).toBe(200);
    expect(updateRes.body.status).toBe("resolved");
    expect(updateRes.body.resolved_at).toBeDefined();

    // Delete
    const deleteRes = await request(app).delete(`/tickets/${ticketId}`);
    expect(deleteRes.status).toBe(204);

    getRes = await request(app).get(`/tickets/${ticketId}`);
    expect(getRes.status).toBe(404);
  });

  test("should handle bulk import and classify", async () => {
    const csv = `customer_id,customer_email,customer_name,subject,description
CUST001,john@example.com,John Doe,Cannot login,Login issue with password
CUST002,jane@example.com,Jane Smith,Invoice wrong,Billing charge discrepancy`;
    const buffer = Buffer.from(csv);

    const importRes = await request(app)
      .post("/tickets/import")
      .field("type", "csv")
      .attach("file", buffer, "test.csv");

    expect(importRes.status).toBe(202);
    expect(importRes.body.summary.successful).toBe(2);
    expect(importRes.body.summary.total).toBe(2);

    const listRes = await request(app).get("/tickets");
    expect(listRes.body.total).toBe(2);
  });

  test("should filter by multiple criteria", async () => {
    const t1 = fixtures.validTicket;
    const t2 = { ...fixtures.validTicket2, status: "in_progress" };
    const t3 = { ...fixtures.validTicket3, status: "in_progress" };

    await request(app).post("/tickets").send(t1);
    await request(app).post("/tickets").send(t2);
    await request(app).post("/tickets").send(t3);

    const res = await request(app).get(
      "/tickets?status=in_progress&category=billing_question"
    );

    expect(res.body.tickets).toHaveLength(1);
    expect(res.body.tickets[0].category).toBe("billing_question");
    expect(res.body.tickets[0].status).toBe("in_progress");
  });

  test("should handle concurrent requests", async () => {
    const promises = [];
    for (let i = 0; i < 20; i += 1) {
      const ticket = {
        ...fixtures.validTicket,
        customer_id: `CUST${i}`,
        customer_email: `cust${i}@example.com`
      };
      promises.push(request(app).post("/tickets").send(ticket));
    }

    const results = await Promise.all(promises);
    results.forEach((res) => {
      expect(res.status).toBe(201);
    });

    const listRes = await request(app).get("/tickets");
    expect(listRes.body.total).toBe(20);
  });

  test("should auto-classify urgent tickets correctly", async () => {
    const urgentTicket = {
      customer_id: "CUST001",
      customer_email: "test@example.com",
      customer_name: "Test User",
      subject: "Critical production down",
      description: "Production system is down with critical security issue"
    };

    const createRes = await request(app).post("/tickets").send(urgentTicket);
    const ticketId = createRes.body.id;

    const classifyRes = await request(app)
      .post(`/tickets/${ticketId}/auto-classify`)
      .send({});

    expect(classifyRes.body.priority).toBe("urgent");
    expect(classifyRes.body.confidence).toBeGreaterThan(0);
  });
});
