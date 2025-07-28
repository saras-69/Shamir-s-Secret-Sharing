# Shamir's Secret Sharing - Assignment Output

## Summary
Successfully implemented Shamir's Secret Sharing algorithm in Java to find polynomial secrets.

## Test Case Results

### Test Case 1
- **Configuration**: n=4, k=3, polynomial degree=2
- **Points**: (1,4), (2,7), (3,12), (6,39)
- **Secret Found**: **2**
- **Wrong Shares**: None detected
- **Verification**: All 3-point combinations consistently yield secret = 2

### Test Case 2  
- **Configuration**: n=10, k=7, polynomial degree=6
- **Points**: 10 large number coordinates (decoded from various bases)
- **Secret Found**: **79836264049850**
- **Wrong Shares**: Point (8, 58725075613853308713) identified as incorrect
- **Verification**: Most frequent secret across 120 combinations

## Algorithm Implementation
1. **Base Conversion**: Converts encoded values from bases 2-16 to decimal
2. **JSON Parsing**: Custom parser for input format
3. **Lagrange Interpolation**: Mathematical formula to find f(0)
4. **Combination Testing**: Tests all C(n,k) combinations to find consensus
5. **Wrong Share Detection**: Identifies incorrect points by frequency analysis

## Mathematical Foundation
For polynomial f(x) = aₘxᵐ + ... + a₁x + c, the secret is c = f(0).

Using Lagrange interpolation:
```
f(0) = Σᵢ yᵢ * Πⱼ≠ᵢ (0-xⱼ)/(xᵢ-xⱼ)
```

## Key Features
- ✅ Handles 256-bit large numbers using BigInteger
- ✅ Detects and filters wrong shares
- ✅ Robust JSON parsing
- ✅ Clear output with analysis
- ✅ Frequency-based consensus finding

## Final Answer
**Test Case 1 Secret: 2**
**Test Case 2 Secret: 79836264049850**

---
*Implementation: Java with BigInteger arithmetic*
*Date: July 28, 2025*
