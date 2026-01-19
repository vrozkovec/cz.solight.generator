# Solight PDF Generator

## Project Purpose

Web application that converts XML product offer files into styled PDF catalogs for Solight (Czech electronics distributor). Users upload XML files exported from their business system, configure display options (prices, locale), and receive a downloadable PDF catalog.

## Technology Stack

- **Java**: 17
- **Web Framework**: Apache Wicket 10.8.0
- **Application Server**: Embedded Jetty 11.0.26
- **PDF Generation**: Playwright 1.52.0 (headless browser)
- **Templates**: Apache Velocity
- **DI**: Google Guice 7.0.0
- **UI**: Bootstrap 5 via wicket-bootstrap

## How to Run

```bash
# Build
mvn clean package

# Run (starts embedded Jetty)
java -jar target/xmltopdf-*.jar
```

The application runs on `http://localhost:8080` by default.

## Main Entry Points

| File | Package | Purpose |
|------|---------|---------|
| `Server.java` | `wicket.app` | Main entry point - starts embedded Jetty server |
| `PdfGeneratorApplication.java` | `wicket.app` | Wicket application configuration |
| `HomePage.java` | `wicket.app` | Landing page |
| `ParserPanel.java` | `wicket.components` | Main UI - file upload, options, PDF generation |

## Package Structure

```
cz.solight.generator.xmltopdf/
├── api/              # REST API endpoints (if any)
├── pojo/             # Data model classes
│   ├── IssuedOffer.java      # Root offer document
│   ├── FirmInfo.java         # Customer company info
│   ├── ProductRow.java       # Single product in the offer
│   ├── ProductPrice.java     # VOC/MOC prices
│   ├── PdfDisplayOptions.java # User-selected display options
│   └── PdfLocale.java        # Locale settings (CZ/SK)
├── service/          # Business logic
│   ├── XmlOfferParser.java      # Parses XML into IssuedOffer POJOs
│   ├── OfferPdfGenerator.java   # HTML rendering + PDF generation
│   └── ImagePathConverter.java  # Image path to URL conversion
└── wicket/           # Web layer
    ├── app/          # Application bootstrap
    └── components/   # Wicket panels and components
```

## Key Services

| Service | Purpose |
|---------|---------|
| `XmlOfferParser` | Parses XML offer files into `IssuedOffer` POJOs |
| `OfferPdfGenerator` | Renders HTML from Velocity templates, generates PDF via Playwright |
| `ImagePathConverter` | Converts image paths to URLs for PDF rendering |

## Velocity Templates

Located in `src/main/resources/cz/solight/generator/xmltopdf/service/templates/`:

- `offer-catalog.vm` - Main PDF HTML template
- `offer-header.vm` - Repeating header template
- `offer-footer.vm` - Repeating footer template

## Workflow

1. User uploads XML offer file via `ParserPanel`
2. `XmlOfferParser` parses XML into `IssuedOffer` POJO tree
3. User configures `PdfDisplayOptions` (prices, locale)
4. `OfferPdfGenerator` renders HTML using Velocity templates
5. Playwright converts HTML to PDF
6. User downloads the generated PDF catalog
