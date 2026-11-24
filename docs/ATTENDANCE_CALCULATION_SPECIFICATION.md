# ATTENDANCE CALCULATION SPECIFICATION
For institutional threshold `p` (fraction, e.g. `0.75`), attended classes `A`, and conducted classes `T`:
- Current percentage: `(A / T) * 100` (returns `0.0` when `T == 0`).
- Consecutive classes required: `x = max(0, ceil((p * T - A) / (1 - p)))`.
- Safe absence allowance (when `A/T >= p`): `floor((A / p) - T)`.
- Initial Tamil Nadu college preset (configurable per institution): `>= 75%` Normal, `65%..<75%` Condonation, `50%..<65%` Medical Condonation, `<50%` Semester-Repeat Risk. Never guarantees eligibility.
