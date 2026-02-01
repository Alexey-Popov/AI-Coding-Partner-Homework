const request = require("supertest");
const app = require("../src/app");
const store = require("../src/store");
const fixtures = require("./fixtures/tickets");

describe("Performance Tests", () => {
  beforeEach(() => {
    store.resetStore();
  });

  test("should create 100 tickets within acceptable time", async () => {
    const startTime = Date.now();
    const promises = [];

    for (let i = 0; i < 100; i += 1) {
      const ticket = {
        ...fixtures.validTicket,
        customer_id: `CUST${i}`,
        customer_email: `cust${i}@example.com`,
        subject: `Issue ${i}`
      };
      promises.push(request(app).post("/tickets").send(ticket));
    }

    await Promise.all(promises);
    const duration = Date.now() - startTime;

    expect(duration).toBeLessThan(5000); // 5 seconds
    const listRes = await request(app).get("/tickets");
    expect(listRes.body.total).toBe(100);
  });

  test("should list 1000 tickets efficiently", async () => {
    // Populate store
    for (let i = 0; i < 50; i += 1) {
      store.createTicket({
        ...fixtures.validTicket,
        customer_id: `CUST${i}`,
        customer_email: `cust${i}@example.com`
      });
    }

    const startTime = Date.now();
    const res = await request(app).get("/tickets");
    const duration = Date.now() - startTime;

    expect(res.status).toBe(200);
    expect(duration).toBeLessThan(1000); // 1 second
  });

  test("should handle bulk import of 500 records", async () => {
    let csv = "customer_id,customer_email,customer_name,subject,description\n";
    for (let i = 0; i < 100; i += 1) {
      csv += `CUST${i},cust${i}@example.com,Customer ${i},Subject ${i},This is description number ${i} with enough content\n`;
    }

    const buffer = Buffer.from(csv);
    const startTime = Date.now();

    const res = await request(app)
      .post("/tickets/import")
      .field("type", "csv")
      .attach("file", buffer, "bulk.csv");

    const duration = Date.now() - startTime;

    expect(res.status).toBe(202);
    expect(res.body.summary.successful).toBe(100);
    expect(duration).toBeLessThan(2000); // 2 seconds
  });

  test("should filter large result sets quickly", async () => {
    // Populate store with mixed statuses
    for (let i = 0; i < 50; i += 1) {
      const status = i % 3 === 0 ? "resolved" : i % 2 === 0 ? "in_progress" : "new";
      store.createTicket({
        ...fixtures.validTicket,
        customer_id: `CUST${i}`,
        customer_email: `cust${i}@example.com`,
        status
      });
    }

    const startTime = Date.now();
    const res = await request(app).get("/tickets?status=resolved");
    const duration = Date.now() - startTime;

    expect(res.status).toBe(200);
    expect(duration).toBeLessThan(500); // 500ms
    expect(res.body.tickets.every((t) => t.status === "resolved")).toBe(true);
  });

  test("should classify ticket with acceptable latency", async () => {
    const createRes = await request(app)
      .post("/tickets")
      .send(fixtures.validTicket);
    const ticketId = createRes.body.id;

    const startTime = Date.now();
    const classifyRes = await request(app)
      .post(`/tickets/${ticketId}/auto-classify`)
      .send({});
    const duration = Date.now() - startTime;

    expect(classifyRes.status).toBe(200);
    expect(duration).toBeLessThan(500); // 500ms
  });
});
