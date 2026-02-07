# 📄 Homework 3: Specification-Driven Design 

## 📋 Overview

Design a **specification package** for a finance-oriented application. You produce only documents: specification, agent rules, and a README that explains your choices and industry practices. **No implementation required.**

---

## 🎯 Learning Objectives

By completing this homework, you will:
- ✅ Structure high-level goals, mid-level objectives, and low-level tasks for AI-driven implementation
- ✅ Define agent configuration (`agents.md`) so an AI coding partner behaves consistently in your domain
- ✅ Capture project conventions in Copilot rules, Claude Code `.md`, or Cursor rules
- ✅ Reflect FinTech/banking best practices in your spec (compliance, security, audit, data handling)

---

## 📝 Task

### Project Requirements (High-Level Only)

You choose scope and depth; the following is intentionally broad so you can refine it in your specification.

- **Domain**: A finance-related application—e.g. **virtual card** lifecycle (create, freeze/unfreeze, set limits, view transactions) or another small finance feature you prefer (e.g. spending caps, card replacement, notifications).
- **Stakeholders**: Assume at least end-users and an internal ops/compliance view; no need to specify exact personas in detail unless you want to.
- **Constraints**: The system should be suitable for a regulated environment (think auditability, security, and clear boundaries for sensitive data). You decide how strict and where to reflect that in the spec.
- **Out of scope for this homework**: Actual code, APIs, or UI. Only written specification and supporting docs (agents.md, rules, README) are required.

Use these points as the **complex project requirements**; your job is to turn them into a concrete, implementable specification (objectives, context, low-level tasks) and to justify your choices in the README.

---

## 📦 Deliverables

Your submission must include the following files in `homework-3/`:

### 1️⃣ specification.md

Full product/feature spec: high-level objective, mid-level objectives, implementation notes, context (beginning/ending), and low-level tasks. You cab use the structure from `specification-TEMPLATE-example.md` as reference.

### 2️⃣ agents.md

Agent/AI guidelines: tech stack, domain rules (e.g. banking), code style, testing expectations, security and compliance constraints.

### 3️⃣ Editor / AI rules

One set of editor/AI rules (e.g. `.github/copilot-instructions.md`, `.claude/` file, or `.cursor/rules/*.md`) that steer how AI should work in this project (naming, patterns, what to avoid).

### 4️⃣ README.md

| Section | Content |
|---------|---------|
| Student & task summary | Your name and brief summary of the homework |
| Rationale | Why this specification was written this way |
| Industry best practices | Which practices you added and **where they appear** in the spec |


---

<div align="center">

**Good luck! No coding required—depth of the specification and clarity of rationale and best practices are what matter. 📋**

</div>
