# Shamir's Secret Sharing Implementation

## Overview
This Java implementation solves the Shamir's Secret Sharing problem by finding the constant term (secret) of a polynomial using the given encoded points.

## Problem Description
- Given n points in encoded format with different bases
- Need to find the constant term of a polynomial of degree (k-1)
- Some points might be incorrect and need to be identified and filtered out
- Use Lagrange interpolation to find the polynomial's constant term

## Features
1. **Base Conversion**: Converts values from any base (2-16) to decimal
2. **JSON Parsing**: Reads input from JSON files
3. **Large Number Support**: Uses BigInteger for handling 256-bit numbers
4. **Wrong Share Detection**: Identifies incorrect shares by testing all combinations
5. **Lagrange Interpolation**: Implements the mathematical algorithm to find the secret

## Algorithm
1. Parse JSON input to extract n, k, and encoded points
2. Convert all y-values from their respective bases to decimal
3. Generate all possible combinations of k points from n points
4. For each combination, calculate the secret using Lagrange interpolation
5. Identify the most frequent secret (correct one)
6. Filter out wrong shares that don't contribute to the correct secret

## Mathematical Foundation
For a polynomial of degree m = k-1:
```
f(x) = a_m * x^m + a_{m-1} * x^{m-1} + ... + a_1 * x + c
```

The secret is the constant term 'c', which is f(0).

Using Lagrange interpolation:
```
f(0) = Σ(i=0 to k-1) y_i * Π(j=0 to k-1, j≠i) (0 - x_j) / (x_i - x_j)
```

## Usage
1. Compile: `javac ShamirSecretSharing.java`
2. Run: `java ShamirSecretSharing`
3. Ensure testcase1.json and testcase2.json are in the same directory

## Input Format
```json
{
    "keys": {
        "n": 4,
        "k": 3
    },
    "1": {
        "base": "10",
        "value": "4"
    },
    "2": {
        "base": "2",
        "value": "111"
    }
}
```

## Expected Output
- Decoded points for each test case
- Analysis of all combinations tested
- Identification of wrong shares (if any)
- The final secret value

## Test Cases
- **Test Case 1**: Simple case with 4 shares, needing 3 for polynomial of degree 2
- **Test Case 2**: Complex case with 10 shares, needing 7 for polynomial of degree 6

## Author
Implementation by Saraswati Chandra for HASHIRA project assignment.
