package name.berries.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line PDF generator using Playwright with Chromium.
 * 
 * This tool runs on-demand to convert HTML content to PDF files.
 * It uses Chromium browser engine via Playwright for accurate rendering
 * including Bootstrap 5, modern CSS, and complex layouts.
 */
public class PdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
    
    /**
     * Main entry point for command-line usage.
     */
    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
    
    /**
     * Testable run method that returns exit code instead of calling System.exit().
     * 
     * @param args command line arguments
     * @return exit code (0 = success, non-zero = error)
     */
    public static int run(String[] args) {
        // First check for help to avoid validation errors
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            Options options = createOptions();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("pdf-generator", options);
            return 0;
        }
        
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            // Required parameters
            String outputPath = cmd.getOptionValue("output");
            if (outputPath == null) {
                logger.error("Output file path is required");
                formatter.printHelp("pdf-generator", options);
                return 1;
            }
            
            // HTML content source
            String htmlContent = null;
            if (cmd.hasOption("file")) {
                String htmlFile = cmd.getOptionValue("file");
                htmlContent = readHtmlFile(htmlFile);
            } else if (cmd.hasOption("url")) {
                // URL will be handled by PdfGeneratorService
                htmlContent = null;
            } else if (cmd.hasOption("html")) {
                htmlContent = cmd.getOptionValue("html");
            } else {
                logger.error("HTML source is required (--file, --url, or --html)");
                formatter.printHelp("pdf-generator", options);
                return 1;
            }
            
            // Optional parameters
            PdfOptions pdfOptions = createPdfOptions(cmd);
            
            // Generate PDF
            PdfGeneratorService service = new PdfGeneratorService();
            
            if (cmd.hasOption("url")) {
                String url = cmd.getOptionValue("url");
                service.generatePdfFromUrl(url, outputPath, pdfOptions);
            } else {
                service.generatePdfFromHtml(htmlContent, outputPath, pdfOptions);
            }
            
            logger.info("PDF generated successfully: {}", outputPath);
            return 0;
            
        } catch (ParseException e) {
            logger.error("Error parsing command line arguments: {}", e.getMessage());
            formatter.printHelp("pdf-generator", options);
            return 1;
        } catch (Exception e) {
            logger.error("Error generating PDF: {}", e.getMessage(), e);
            return 1;
        }
    }
    
    /**
     * Creates command line options for the PDF generator.
     */
    private static Options createOptions() {
        Options options = new Options();
        
        // Help option
        options.addOption("h", "help", false, "Show help message");
        
        // Required options
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .required()
                .desc("Output PDF file path")
                .build());
        
        // HTML source options (mutually exclusive)
        OptionGroup htmlSource = new OptionGroup();
        htmlSource.setRequired(true);
        
        htmlSource.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg()
                .desc("HTML file to convert")
                .build());
                
        htmlSource.addOption(Option.builder("u")
                .longOpt("url")
                .hasArg()
                .desc("URL to convert")
                .build());
                
        htmlSource.addOption(Option.builder()
                .longOpt("html")
                .hasArg()
                .desc("HTML content string")
                .build());
                
        options.addOptionGroup(htmlSource);
        
        // PDF format options
        options.addOption(Option.builder()
                .longOpt("format")
                .hasArg()
                .desc("PDF format (A4, A3, Letter, etc.) - default: A4")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("landscape")
                .desc("Use landscape orientation")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("margin-top")
                .hasArg()
                .desc("Top margin (e.g., '10mm', '1in') - default: 0")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("margin-bottom")
                .hasArg()
                .desc("Bottom margin - default: 0")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("margin-left")
                .hasArg()
                .desc("Left margin - default: 0")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("margin-right")
                .hasArg()
                .desc("Right margin - default: 0")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("print-background")
                .desc("Include background colors and images")
                .build());
                
        options.addOption(Option.builder()
                .longOpt("wait-for")
                .hasArg()
                .desc("Wait condition: networkidle, load, or timeout in ms")
                .build());
        
        return options;
    }
    
    /**
     * Creates PDF options from command line arguments.
     */
    private static PdfOptions createPdfOptions(CommandLine cmd) {
        PdfOptions options = new PdfOptions();
        
        if (cmd.hasOption("format")) {
            options.setFormat(cmd.getOptionValue("format"));
        }
        
        if (cmd.hasOption("landscape")) {
            options.setLandscape(true);
        }
        
        if (cmd.hasOption("margin-top")) {
            options.setMarginTop(cmd.getOptionValue("margin-top"));
        }
        
        if (cmd.hasOption("margin-bottom")) {
            options.setMarginBottom(cmd.getOptionValue("margin-bottom"));
        }
        
        if (cmd.hasOption("margin-left")) {
            options.setMarginLeft(cmd.getOptionValue("margin-left"));
        }
        
        if (cmd.hasOption("margin-right")) {
            options.setMarginRight(cmd.getOptionValue("margin-right"));
        }
        
        if (cmd.hasOption("print-background")) {
            options.setPrintBackground(true);
        }
        
        if (cmd.hasOption("wait-for")) {
            options.setWaitFor(cmd.getOptionValue("wait-for"));
        }
        
        return options;
    }
    
    /**
     * Reads HTML content from a file.
     */
    private static String readHtmlFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("HTML file not found: " + filePath);
        }
        
        return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
    }
}