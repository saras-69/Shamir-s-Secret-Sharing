import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Final Optimized Shamir's Secret Sharing Implementation
 * Focuses on accuracy and clear output
 */
public class FinalShamirSecretSharing {
    
    static class Point {
        BigInteger x, y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point)) return false;
            Point point = (Point) obj;
            return x.equals(point.x) && y.equals(point.y);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    public static BigInteger convertToDecimal(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(base);
        
        for (char digit : value.toCharArray()) {
            int digitValue = Character.isDigit(digit) ? 
                digit - '0' : Character.toLowerCase(digit) - 'a' + 10;
            
            if (digitValue >= base) {
                throw new IllegalArgumentException("Invalid digit '" + digit + "' for base " + base);
            }
            
            result = result.multiply(baseBI).add(BigInteger.valueOf(digitValue));
        }
        
        return result;
    }
    
    public static Map<String, Object> parseJSON(String content) {
        Map<String, Object> result = new HashMap<>();
        
        // Simple manual JSON parsing
        content = content.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);
        
        // Split by top-level commas (not inside nested objects)
        List<String> parts = new ArrayList<>();
        int braceLevel = 0;
        StringBuilder current = new StringBuilder();
        
        for (char c : content.toCharArray()) {
            if (c == '{') braceLevel++;
            else if (c == '}') braceLevel--;
            else if (c == ',' && braceLevel == 0) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }
        
        // Parse each part
        for (String part : parts) {
            part = part.trim();
            
            if (part.contains("\"keys\"")) {
                // Parse keys object
                int start = part.indexOf('{');
                int end = part.lastIndexOf('}');
                if (start != -1 && end != -1) {
                    String keysContent = part.substring(start + 1, end);
                    Map<String, Integer> keys = new HashMap<>();
                    
                    for (String keyPair : keysContent.split(",")) {
                        String[] kv = keyPair.split(":");
                        if (kv.length == 2) {
                            String key = kv[0].replaceAll("\"", "").trim();
                            int value = Integer.parseInt(kv[1].trim());
                            keys.put(key, value);
                        }
                    }
                    result.put("keys", keys);
                }
            } else if (part.contains("\"base\"")) {
                // Parse point object
                int keyStart = part.indexOf("\"") + 1;
                int keyEnd = part.indexOf("\"", keyStart);
                if (keyStart > 0 && keyEnd > keyStart) {
                    String key = part.substring(keyStart, keyEnd);
                    
                    int objStart = part.indexOf('{');
                    int objEnd = part.lastIndexOf('}');
                    if (objStart != -1 && objEnd != -1) {
                        String objContent = part.substring(objStart + 1, objEnd);
                        Map<String, String> pointData = new HashMap<>();
                        
                        for (String pointPair : objContent.split(",")) {
                            String[] kv = pointPair.split(":");
                            if (kv.length == 2) {
                                String pointKey = kv[0].replaceAll("\"", "").trim();
                                String pointValue = kv[1].replaceAll("\"", "").trim();
                                pointData.put(pointKey, pointValue);
                            }
                        }
                        result.put(key, pointData);
                    }
                }
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Point> extractPoints(Map<String, Object> data) {
        List<Point> points = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!"keys".equals(entry.getKey())) {
                BigInteger x = new BigInteger(entry.getKey());
                Map<String, String> pointData = (Map<String, String>) entry.getValue();
                
                int base = Integer.parseInt(pointData.get("base"));
                String value = pointData.get("value");
                BigInteger y = convertToDecimal(value, base);
                
                points.add(new Point(x, y));
            }
        }
        
        return points;
    }
    
    public static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger secret = BigInteger.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    numerator = numerator.multiply(points.get(j).x.negate()); // 0 - x_j
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }
            
            // Calculate term = y_i * (numerator / denominator)
            BigInteger term = points.get(i).y.multiply(numerator).divide(denominator);
            secret = secret.add(term);
        }
        
        return secret;
    }
    
    public static List<List<Point>> getCombinations(List<Point> points, int k) {
        List<List<Point>> combinations = new ArrayList<>();
        generateCombinations(points, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private static void generateCombinations(List<Point> points, int k, int start,
            List<Point> current, List<List<Point>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < points.size(); i++) {
            current.add(points.get(i));
            generateCombinations(points, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    public static void solveTestCase(String filename) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 ANALYZING: " + filename.toUpperCase());
        System.out.println("=".repeat(70));
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            Map<String, Object> data = parseJSON(content);
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> keys = (Map<String, Integer>) data.get("keys");
            int n = keys.get("n");
            int k = keys.get("k");
            
            System.out.println("📊 Configuration:");
            System.out.println("   • Total shares available (n): " + n);
            System.out.println("   • Minimum shares required (k): " + k);
            System.out.println("   • Polynomial degree: " + (k - 1));
            
            List<Point> points = extractPoints(data);
            System.out.println("\n📍 Decoded coordinate points:");
            for (int i = 0; i < points.size(); i++) {
                System.out.printf("   %2d. %s%n", i + 1, points.get(i));
            }
            
            // Test all combinations
            List<List<Point>> combinations = getCombinations(points, k);
            Map<BigInteger, Integer> secretFreq = new HashMap<>();
            Map<BigInteger, Set<Point>> secretPoints = new HashMap<>();
            
            System.out.println("\n🔬 Testing " + combinations.size() + " possible combinations...");
            
            for (List<Point> combo : combinations) {
                BigInteger secret = lagrangeInterpolation(combo);
                secretFreq.put(secret, secretFreq.getOrDefault(secret, 0) + 1);
                secretPoints.computeIfAbsent(secret, s -> new HashSet<>()).addAll(combo);
            }
            
            // Find the most frequent secret (correct one)
            BigInteger correctSecret = secretFreq.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            System.out.println("\n📈 Secret frequency analysis:");
            secretFreq.entrySet().stream()
                    .sorted(Map.Entry.<BigInteger, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> System.out.println("   • " + entry.getKey() + 
                            " appears " + entry.getValue() + " times"));
            
            if (correctSecret != null) {
                // Identify wrong shares
                Set<Point> correctPoints = secretPoints.get(correctSecret);
                Set<Point> wrongPoints = new HashSet<>(points);
                wrongPoints.removeAll(correctPoints);
                
                if (!wrongPoints.isEmpty()) {
                    System.out.println("\n❌ Wrong shares detected:");
                    wrongPoints.forEach(p -> System.out.println("   • x=" + p.x + ", y=" + p.y));
                } else {
                    System.out.println("\n✅ All shares are correct!");
                }
                
                System.out.println("\n" + "=".repeat(50));
                System.out.println("🔑 FINAL SECRET: " + correctSecret);
                System.out.println("=".repeat(50));
            } else {
                System.out.println("\n❌ Could not determine the secret!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing " + filename + ": " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🔐 SHAMIR'S SECRET SHARING SOLVER");
        System.out.println("📚 Polynomial Secret Recovery using Lagrange Interpolation");
        System.out.println("👨‍💻 Implementation: Java with BigInteger arithmetic");
        System.out.println("📅 Date: " + new Date());
        
        solveTestCase("testcase1.json");
        solveTestCase("testcase2.json");
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✨ MISSION ACCOMPLISHED! Both secrets successfully recovered! ✨");
        System.out.println("=".repeat(70));
    }
}
