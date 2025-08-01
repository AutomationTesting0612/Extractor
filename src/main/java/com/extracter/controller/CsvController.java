package com.extracter.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class CsvController {

    @GetMapping("/")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/extract")
    public String extractColumns(@RequestParam("file") MultipartFile file,
                                 @RequestParam("columns") String columnInput,
                                 @RequestParam("url") String url,
                                 Model model) throws IOException {
        List<String> columns = Arrays.stream(columnInput.split(","))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .toList();

        if (columns.isEmpty()) {
            model.addAttribute("message", "Please provide column names.");
            return "upload";
        }

        String userHome = System.getProperty("user.home");
        Path downloadsDir = Paths.get(userHome, "Downloads");
        Files.createDirectories(downloadsDir);

        String outputFileName = "filtered_" + System.currentTimeMillis() + ".csv";
        Path outputPath = downloadsDir.resolve(outputFileName);

        try (
                Reader reader = new InputStreamReader(file.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
                BufferedWriter writer = Files.newBufferedWriter(outputPath);
                CSVPrinter printer = new CSVPrinter(writer,
                        CSVFormat.DEFAULT.withHeader(
                                Stream.concat(columns.stream(), Stream.of("URL")).toArray(String[]::new))
                )) {
            for (CSVRecord record : parser) {
                List<String> values = new ArrayList<>();
                for (String col : columns) {
                    values.add(record.get(col));
                }
                values.add(url);
                printer.printRecord(values);
            }
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "upload";
        }

        model.addAttribute("message", "CSV processed successfully!");
        model.addAttribute("path", outputPath.toAbsolutePath().toString());
        return "upload";
    }
}