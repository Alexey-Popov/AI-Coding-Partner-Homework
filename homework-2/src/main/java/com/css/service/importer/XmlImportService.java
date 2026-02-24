package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.model.DeviceType;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketMetadata;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;
import com.css.model.TicketStatus;
import com.css.service.TicketService;
import com.css.service.TicketValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class XmlImportService {

    private final TicketService ticketService;
    private final TicketValidationService validationService;

    public XmlImportService(TicketService ticketService, TicketValidationService validationService) {
        this.ticketService = ticketService;
        this.validationService = validationService;
    }

    public ImportResult importFromXml(MultipartFile file) {
        ImportResult result = new ImportResult();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable external entities to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            document.getDocumentElement().normalize();

            NodeList ticketNodes = document.getElementsByTagName("ticket");
            result.setTotalRecords(ticketNodes.getLength());

            for (int i = 0; i < ticketNodes.getLength(); i++) {
                int recordNumber = i + 1;
                try {
                    Element ticketElement = (Element) ticketNodes.item(i);
                    Ticket ticket = parseTicketFromXml(ticketElement);

                    Map<String, String> errors = validationService.validateTicketData(ticket);
                    if (errors.isEmpty()) {
                        Ticket saved = ticketService.saveTicket(ticket);
                        result.addImportedTicketId(saved.getId().toString());
                        result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    } else {
                        for (Map.Entry<String, String> error : errors.entrySet()) {
                            result.addError(new ImportResult.ImportError(
                                    recordNumber, error.getKey(), error.getValue(),
                                    elementToString(ticketElement)));
                        }
                        result.setFailedRecords(result.getFailedRecords() + 1);
                    }
                } catch (Exception e) {
                    result.addError(new ImportResult.ImportError(
                            recordNumber, "parsing", "Failed to parse record: " + e.getMessage(),
                            "Record #" + recordNumber));
                    result.setFailedRecords(result.getFailedRecords() + 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read XML file: " + e.getMessage(), e);
        }

        return result;
    }

    private Ticket parseTicketFromXml(Element element) {
        Ticket ticket = new Ticket();

        ticket.setCustomerId(getElementText(element, "customer_id"));
        ticket.setCustomerEmail(getElementText(element, "customer_email"));
        ticket.setCustomerName(getElementText(element, "customer_name"));
        ticket.setSubject(getElementText(element, "subject"));
        ticket.setDescription(getElementText(element, "description"));

        String categoryStr = getElementText(element, "category");
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                ticket.setCategory(TicketCategory.valueOf(categoryStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setCategory(TicketCategory.OTHER);
            }
        } else {
            ticket.setCategory(TicketCategory.OTHER);
        }

        String priorityStr = getElementText(element, "priority");
        if (priorityStr != null && !priorityStr.isEmpty()) {
            try {
                ticket.setPriority(TicketPriority.valueOf(priorityStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
        } else {
            ticket.setPriority(TicketPriority.MEDIUM);
        }

        String statusStr = getElementText(element, "status");
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                ticket.setStatus(TicketStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setStatus(TicketStatus.NEW);
            }
        } else {
            ticket.setStatus(TicketStatus.NEW);
        }

        String assignedTo = getElementText(element, "assigned_to");
        if (assignedTo != null && !assignedTo.isEmpty()) {
            ticket.setAssignedTo(assignedTo);
        }

        // Parse tags
        NodeList tagsNodes = element.getElementsByTagName("tags");
        if (tagsNodes.getLength() > 0) {
            Element tagsElement = (Element) tagsNodes.item(0);
            NodeList tagNodes = tagsElement.getElementsByTagName("tag");
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < tagNodes.getLength(); i++) {
                tags.add(tagNodes.item(i).getTextContent().trim());
            }
            ticket.setTags(tags);
        }

        // Parse metadata
        NodeList metadataNodes = element.getElementsByTagName("metadata");
        if (metadataNodes.getLength() > 0) {
            Element metadataElement = (Element) metadataNodes.item(0);
            TicketMetadata metadata = new TicketMetadata();

            String sourceStr = getElementText(metadataElement, "source");
            if (sourceStr != null && !sourceStr.isEmpty()) {
                try {
                    metadata.setSource(TicketSource.valueOf(sourceStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    metadata.setSource(TicketSource.API);
                }
            }

            metadata.setBrowser(getElementText(metadataElement, "browser"));

            String deviceTypeStr = getElementText(metadataElement, "device_type");
            if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                try {
                    metadata.setDeviceType(DeviceType.valueOf(deviceTypeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    metadata.setDeviceType(DeviceType.DESKTOP);
                }
            }

            ticket.setMetadata(metadata);
        }

        return ticket;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    private String elementToString(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(element.getTagName()).append(">");
        // Simplified string representation
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                sb.append("<").append(child.getTagName()).append(">")
                  .append(child.getTextContent())
                  .append("</").append(child.getTagName()).append(">");
            }
        }
        sb.append("</").append(element.getTagName()).append(">");
        return sb.toString();
    }
}

