import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Shamir's Secret Sharing Implementation
 * This program finds the constant term (secret) of a polynomial using Lagrange interpolation
 * and handles base conversion, wrong share detection, and large number arithmetic.
 */
public class ShamirSecretSharing {
    
    /**
     * Represents a point (x, y) in the polynomial
     */
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    /**
     * Convert a value from given base to decimal (BigInteger)
     */
    public static BigInteger convertToDecimal(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(base);
        
        for (int i = 0; i < value.length(); i++) {
            char digit = value.charAt(i);
            int digitValue;
            
            if (Character.isDigit(digit)) {
                digitValue = digit - '0';
            } else {
                digitValue = Character.toLowerCase(digit) - 'a' + 10;
            }
            
            if (digitValue >= base) {
                throw new IllegalArgumentException("Invalid digit for base " + base + ": " + digit);
            }
            
            result = result.multiply(baseBI).add(BigInteger.valueOf(digitValue));
        }
        
        return result;
    }
    
    /**
     * Parse JSON-like input to extract points
     */
    public static Map<String, Object> parseInput(String jsonContent) {
        Map<String, Object> result = new HashMap<>();
        
        // Remove whitespace and braces
        jsonContent = jsonContent.trim().replaceAll("\\s+", "");
        if (jsonContent.startsWith("{")) {
            jsonContent = jsonContent.substring(1);
        }
        if (jsonContent.endsWith("}")) {
            jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
        }
        
        // Split by commas but handle nested objects
        List<String> parts = new ArrayList<>();
        int braceCount = 0;
        StringBuilder current = new StringBuilder();
        
        for (char c : jsonContent.toCharArray()) {
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            else if (c == ',' && braceCount == 0) {
                parts.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        
        for (String part : parts) {
            if (part.contains("\"keys\"")) {
                // Parse keys object
                String keysContent = part.substring(part.indexOf('{') + 1, part.lastIndexOf('}'));
                String[] keyPairs = keysContent.split(",");
                Map<String, Integer> keys = new HashMap<>();
                
                for (String keyPair : keyPairs) {
                    String[] kv = keyPair.split(":");
                    String key = kv[0].replaceAll("\"", "").trim();
                    int value = Integer.parseInt(kv[1].trim());
                    keys.put(key, value);
                }
                result.put("keys", keys);
            } else if (part.contains("\"base\"")) {
                // Parse point object
                String key = part.substring(1, part.indexOf("\"", 1));
                String pointContent = part.substring(part.indexOf('{') + 1, part.lastIndexOf('}'));
                String[] pointPairs = pointContent.split(",");
                
                Map<String, String> pointData = new HashMap<>();
                for (String pointPair : pointPairs) {
                    String[] kv = pointPair.split(":");
                    String pointKey = kv[0].replaceAll("\"", "").trim();
                    String pointValue = kv[1].replaceAll("\"", "").trim();
                    pointData.put(pointKey, pointValue);
                }
                result.put(key, pointData);
            }
        }
        
        return result;
    }
    
    /**
     * Extract points from parsed input
     */
    public static List<Point> extractPoints(Map<String, Object> parsedInput) {
        List<Point> points = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : parsedInput.entrySet()) {
            if (!entry.getKey().equals("keys")) {
                try {
                    BigInteger x = new BigInteger(entry.getKey());
                    Map<String, String> pointData = (Map<String, String>) entry.getValue();
                    
                    int base = Integer.parseInt(pointData.get("base"));
                    String value = pointData.get("value");
                    
                    BigInteger y = convertToDecimal(value, base);
                    points.add(new Point(x, y));
                } catch (Exception e) {
                    System.err.println("Error parsing point " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }
        
        return points;
    }
    
    /**
     * Calculate polynomial value at x=0 using Lagrange interpolation
     */
    public static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            BigInteger term = points.get(i).y;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    // Calculate (0 - x_j) / (x_i - x_j)
                    BigInteger numerator = points.get(j).x.negate();
                    BigInteger denominator = points.get(i).x.subtract(points.get(j).x);
                    
                    // For modular arithmetic, we need to handle division carefully
                    // Since we're working with the secret at x=0, we can use direct calculation
                    term = term.multiply(numerator).divide(denominator);
                }
            }
            
            result = result.add(term);
        }
        
        return result;
    }
    
    /**
     * Generate all combinations of k elements from n elements
     */
    public static List<List<Point>> generateCombinations(List<Point> points, int k) {
        List<List<Point>> combinations = new ArrayList<>();
        generateCombinationsHelper(points, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private static void generateCombinationsHelper(List<Point> points, int k, int start, 
            List<Point> current, List<List<Point>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < points.size(); i++) {
            current.add(points.get(i));
            generateCombinationsHelper(points, k, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Find the correct secret by trying all combinations and identifying the most common result
     */
    public static BigInteger findCorrectSecret(List<Point> points, int k) {
        List<List<Point>> combinations = generateCombinations(points, k);
        Map<BigInteger, Integer> secretCounts = new HashMap<>();
        Map<BigInteger, List<List<Point>>> secretCombinations = new HashMap<>();
        
        System.out.println("Testing " + combinations.size() + " combinations of " + k + " points...");
        
        for (List<Point> combination : combinations) {
            try {
                BigInteger secret = lagrangeInterpolation(combination);
                secretCounts.put(secret, secretCounts.getOrDefault(secret, 0) + 1);
                secretCombinations.computeIfAbsent(secret, k1 -> new ArrayList<>()).add(combination);
            } catch (Exception e) {
                System.err.println("Error calculating secret for combination: " + e.getMessage());
            }
        }
        
        // Find the most frequent secret
        BigInteger correctSecret = null;
        int maxCount = 0;
        
        for (Map.Entry<BigInteger, Integer> entry : secretCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                correctSecret = entry.getKey();
            }
        }
        
        System.out.println("Secret frequency analysis:");
        for (Map.Entry<BigInteger, Integer> entry : secretCounts.entrySet()) {
            System.out.println("Secret: " + entry.getKey() + " appears " + entry.getValue() + " times");
        }
        
        // Identify wrong shares
        if (correctSecret != null) {
            List<List<Point>> correctCombinations = secretCombinations.get(correctSecret);
            Set<Point> correctPoints = new HashSet<>();
            
            for (List<Point> combo : correctCombinations) {
                correctPoints.addAll(combo);
            }
            
            Set<Point> wrongPoints = new HashSet<>(points);
            wrongPoints.removeAll(correctPoints);
            
            if (!wrongPoints.isEmpty()) {
                System.out.println("Identified wrong shares:");
                for (Point wrongPoint : wrongPoints) {
                    System.out.println("Wrong share: x=" + wrongPoint.x + ", y=" + wrongPoint.y);
                }
            } else {
                System.out.println("No wrong shares detected.");
            }
        }
        
        return correctSecret;
    }
    
    /**
     * Process a single test case
     */
    public static void processTestCase(String filename) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Processing test case: " + filename);
        System.out.println("=".repeat(50));
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            Map<String, Object> parsedInput = parseInput(content);
            
            Map<String, Integer> keys = (Map<String, Integer>) parsedInput.get("keys");
            int n = keys.get("n");
            int k = keys.get("k");
            
            System.out.println("n (total shares): " + n);
            System.out.println("k (minimum shares needed): " + k);
            System.out.println("Polynomial degree: " + (k - 1));
            
            List<Point> points = extractPoints(parsedInput);
            System.out.println("\nExtracted points:");
            for (Point point : points) {
                System.out.println(point);
            }
            
            BigInteger secret = findCorrectSecret(points, k);
            
            System.out.println("\n" + "=".repeat(30));
            System.out.println("SECRET FOUND: " + secret);
            System.out.println("=".repeat(30));
            
        } catch (Exception e) {
            System.err.println("Error processing test case " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Shamir's Secret Sharing - Polynomial Secret Recovery");
        System.out.println("Author: GitHub Copilot");
        System.out.println("Date: " + new Date());
        
        // Process both test cases
        processTestCase("testcase1.json");
        processTestCase("testcase2.json");
    }
}
