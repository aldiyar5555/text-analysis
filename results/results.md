### Dynamic programming (average of 5 runs)

| Algorithm | n = m | Avg time (ms) | Min (ms) | Max (ms) | DP cells | Result |
|---|---:|---:|---:|---:|---:|---:|
| LCS | 100 | 0.0191 | 0.0184 | 0.0207 | 10000 | 64 |
| EditDistance | 100 | 0.0197 | 0.0195 | 0.0199 | 10000 | 55 |
| LCS | 500 | 0.3720 | 0.3657 | 0.3837 | 250000 | 326 |
| EditDistance | 500 | 0.4441 | 0.4276 | 0.4604 | 250000 | 267 |
| LCS | 1000 | 1.4349 | 1.3181 | 1.6715 | 1000000 | 648 |
| EditDistance | 1000 | 1.4950 | 1.3465 | 2.0320 | 1000000 | 521 |

### String matching (pattern length 16, average of 5 runs)

| Scenario | Algorithm | Text length | Avg time (ms) | Search comparisons | Preprocess comparisons | Matches | Hash hits | Spurious hits |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| present | Naive | 10000 | 0.0327 | 13435 | 0 | 1 | 0 | 0 |
| present | KMP | 10000 | 0.0304 | 12521 | 19 | 1 | 0 | 0 |
| present | Rabin-Karp | 10000 | 0.1702 | 16 | 0 | 1 | 1 | 0 |
| absent | Naive | 10000 | 0.0344 | 13382 | 0 | 0 | 0 | 0 |
| absent | KMP | 10000 | 0.0246 | 12528 | 18 | 0 | 0 | 0 |
| absent | Rabin-Karp | 10000 | 0.1712 | 0 | 0 | 0 | 0 | 0 |
| worst_case | Naive | 10000 | 0.1256 | 159760 | 0 | 0 | 0 | 0 |
| worst_case | KMP | 10000 | 0.0270 | 19985 | 29 | 0 | 0 | 0 |
| worst_case | Rabin-Karp | 10000 | 0.1802 | 0 | 0 | 0 | 0 | 0 |
| present | Naive | 50000 | 0.3416 | 66631 | 0 | 1 | 0 | 0 |
| present | KMP | 50000 | 0.3056 | 62441 | 22 | 1 | 0 | 0 |
| present | Rabin-Karp | 50000 | 0.8655 | 16 | 0 | 1 | 1 | 0 |
| absent | Naive | 50000 | 0.3247 | 66821 | 0 | 0 | 0 | 0 |
| absent | KMP | 50000 | 0.2931 | 62584 | 16 | 0 | 0 | 0 |
| absent | Rabin-Karp | 50000 | 0.9824 | 0 | 0 | 0 | 0 | 0 |
| worst_case | Naive | 50000 | 1.3729 | 799760 | 0 | 0 | 0 | 0 |
| worst_case | KMP | 50000 | 0.1395 | 99985 | 29 | 0 | 0 | 0 |
| worst_case | Rabin-Karp | 50000 | 0.9309 | 0 | 0 | 0 | 0 | 0 |
| present | Naive | 100000 | 0.6606 | 132848 | 0 | 1 | 0 | 0 |
| present | KMP | 100000 | 0.5556 | 123186 | 17 | 1 | 0 | 0 |
| present | Rabin-Karp | 100000 | 1.7366 | 16 | 0 | 1 | 1 | 0 |
| absent | Naive | 100000 | 0.6295 | 132691 | 0 | 0 | 0 | 0 |
| absent | KMP | 100000 | 0.5343 | 123228 | 17 | 0 | 0 | 0 |
| absent | Rabin-Karp | 100000 | 1.8427 | 0 | 0 | 0 | 0 | 0 |
| worst_case | Naive | 100000 | 1.1921 | 1599760 | 0 | 0 | 0 | 0 |
| worst_case | KMP | 100000 | 0.2794 | 199985 | 29 | 0 | 0 | 0 |
| worst_case | Rabin-Karp | 100000 | 1.8127 | 0 | 0 | 0 | 0 | 0 |

### Kasai LCP (suffix array given as input, average of 5 runs)

| n | Avg time (ms) | Min (ms) | Max (ms) | Char comparisons | Max LCP |
|---:|---:|---:|---:|---:|---:|
| 1000 | 0.0071 | 0.0059 | 0.0107 | 1990 | 11 |
| 5000 | 0.0555 | 0.0543 | 0.0572 | 9991 | 11 |
| 10000 | 0.1364 | 0.1343 | 0.1397 | 19988 | 14 |
