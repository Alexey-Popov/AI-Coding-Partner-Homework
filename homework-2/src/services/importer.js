const fs = require("fs");
const path = require("path");
const { parse: parseCsv } = require("csv-parse/sync");
const { XMLParser } = require("fast-xml-parser");

function parseCsvFile(fileContent) {
  const records = parseCsv(fileContent, {
    columns: true,
    skip_empty_lines: true,
    trim: true
  });
  return records.map((record) => ({
    customer_id: record.customer_id,
    customer_email: record.customer_email,
    customer_name: record.customer_name,
    subject: record.subject,
    description: record.description,
    category: record.category || "other",
    priority: record.priority || "medium",
    status: record.status || "new",
    tags: record.tags ? record.tags.split(",").map((t) => t.trim()) : [],
    metadata: {
      source: record.source || "api",
      browser: record.browser || "unknown",
      device_type: record.device_type || "desktop"
    }
  }));
}

function parseJsonFile(fileContent) {
  try {
    const data = JSON.parse(fileContent);
    const records = Array.isArray(data) ? data : [data];
    return records.map((record) => ({
      customer_id: record.customer_id,
      customer_email: record.customer_email,
      customer_name: record.customer_name,
      subject: record.subject,
      description: record.description,
      category: record.category || "other",
      priority: record.priority || "medium",
      status: record.status || "new",
      tags: record.tags || [],
      metadata: record.metadata || {
        source: "api",
        browser: "unknown",
        device_type: "desktop"
      }
    }));
  } catch (err) {
    throw new Error(`JSON parsing error: ${err.message}`);
  }
}

function parseXmlFile(fileContent) {
  const parser = new XMLParser({
    ignoreAttributes: false,
    parseAttributeValue: true
  });
  let xmlData;
  try {
    xmlData = parser.parse(fileContent);
  } catch (err) {
    throw new Error(`XML parsing error: ${err.message}`);
  }

  let tickets = xmlData.tickets?.ticket || xmlData.ticket || [];
  if (!Array.isArray(tickets)) {
    tickets = [tickets];
  }

  return tickets.map((record) => ({
    customer_id: record.customer_id,
    customer_email: record.customer_email,
    customer_name: record.customer_name,
    subject: record.subject,
    description: record.description,
    category: record.category || "other",
    priority: record.priority || "medium",
    status: record.status || "new",
    tags: record.tags ? (Array.isArray(record.tags.tag) ? record.tags.tag : [record.tags.tag]) : [],
    metadata: record.metadata ? {
      source: record.metadata.source || "api",
      browser: record.metadata.browser || "unknown",
      device_type: record.metadata.device_type || "desktop"
    } : {
      source: "api",
      browser: "unknown",
      device_type: "desktop"
    }
  }));
}

function importFromFile(fileBuffer, fileType) {
  const fileContent = fileBuffer.toString("utf-8");

  if (fileType === "csv") {
    return parseCsvFile(fileContent);
  }
  if (fileType === "json") {
    return parseJsonFile(fileContent);
  }
  if (fileType === "xml") {
    return parseXmlFile(fileContent);
  }

  throw new Error(`Unsupported file type: ${fileType}`);
}

module.exports = {
  importFromFile,
  parseCsvFile,
  parseJsonFile,
  parseXmlFile
};
