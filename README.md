# Solight PDF Generator

## Project Purpose

Web application that converts XML product offer files into styled PDF catalogs for Solight (Czech electronics distributor). Users upload XML files exported from their business system, configure display options (prices, locale), and receive a downloadable PDF catalog.

## Technology Stack

- **Java**: 17
- **Web Framework**: Apache Wicket 10.8.0
- **Application Server**: Embedded Jetty 11.0.26
- **PDF Generation**: Playwright 1.52.0 (headless browser), Gotenberg (for product sheets)
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
| `Scheduler.java` | `scheduler` | Quartz scheduler for background jobs |
| `FtpSyncService.java` | `service` | SFTP download/upload for product sheets |

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
│   ├── PdfLocale.java        # Locale settings (CZ/SK)
│   ├── ProductSheet.java     # Product sheet data model
│   └── ProductSheetFormat.java # Format settings (A4/FULLHEIGHT)
├── scheduler/        # Background job processing
│   ├── Scheduler.java           # Quartz scheduler setup
│   ├── JobDailyInTheMorning.java # Daily scheduled job
│   └── JobOneTime.java          # One-time execution job
├── service/          # Business logic
│   ├── OfferXmlParser.java         # Parses XML into IssuedOffer POJOs
│   ├── OfferPdfGenerator.java      # HTML rendering + PDF generation
│   ├── ImagePathConverter.java     # Image path to URL conversion
│   ├── ProductSheetXmlParser.java  # Parses produktove_listy.xml
│   └── ProductSheetPdfGenerator.java # Product sheet PDF via Gotenberg
└── wicket/           # Web layer
    ├── app/          # Application bootstrap
    └── components/   # Wicket panels and components
```

## Key Services

| Service | Purpose |
|---------|---------|
| `OfferXmlParser` | Parses XML offer files into `IssuedOffer` POJOs |
| `OfferPdfGenerator` | Renders HTML from Velocity templates, generates PDF via Playwright |
| `ImagePathConverter` | Converts image paths to URLs for PDF rendering |
| `ProductSheetXmlParser` | Parses `produktove_listy.xml` into `ProductSheet` POJOs |
| `ProductSheetPdfGenerator` | Generates product sheet PDFs via Gotenberg (A4 and full-height formats) |

## Velocity Templates

Located in `src/main/resources/cz/solight/generator/xmltopdf/service/templates/`:

**Offer Catalog:**
- `offer-catalog.vm` - Main PDF HTML template
- `offer-header.vm` - Repeating header template
- `offer-footer.vm` - Repeating footer template

**Product Sheets:**
- `product-sheet.vm` - Main product sheet template
- `product-sheet-header.vm` - Product sheet header
- `product-sheet-footer.vm` - Product sheet footer

## Workflow

### Offer Catalog (Manual)
1. User uploads XML offer file via `ParserPanel`
2. `OfferXmlParser` parses XML into `IssuedOffer` POJO tree
3. User configures `PdfDisplayOptions` (prices, locale)
4. `OfferPdfGenerator` renders HTML using Velocity templates
5. Playwright converts HTML to PDF
6. User downloads the generated PDF catalog

### Product Sheets (Scheduled)
1. Scheduler triggers `FtpSyncService` to download `produktove_listy.xml` from SFTP
2. `ProductSheetXmlParser` parses XML into `ProductSheet` POJOs
3. `ProductSheetPdfGenerator` generates PDFs via Gotenberg for each product:
   - **A4 format**: Fixed page size with repeating header/footer
   - **Full-height format**: Single continuous page with dynamic height
4. Generated PDFs are uploaded back to SFTP

## External Dependencies

### Gotenberg
Product sheet PDF generation requires a running [Gotenberg](https://gotenberg.dev/) instance. Gotenberg provides headless Chromium-based PDF generation with native header/footer support and dynamic page heights.

```bash
# Run Gotenberg via Docker
docker run -d -p 3000:3000 gotenberg/gotenberg:8
```

Configure the URL in `appconfig.yml`:
```yaml
gotenberg:
   url: http://localhost:3000
```
