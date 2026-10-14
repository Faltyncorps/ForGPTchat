# US copyright enforcement – outline (DRAFT)

> **DRAFT – NOT LEGAL ADVICE. Not notarized. Not for filing as-is.** Generic public-law outline for orientation only; must be reviewed and adapted by a licensed US attorney in the relevant jurisdiction. Placeholders in `[BRACKETS]` must never be filled with secrets, bank data or personal identifiers in this repository.

## 1. Preconditions
- Identify the work (repository path, commit hash, first publication date) – author per AUTHORSHIP-DECLARATION.md.
- Consider US Copyright Office registration (eCO) – required to sue for US works; affects statutory damages for all works (17 U.S.C. §§ 411–412).

## 2. Escalation ladder
1. Polite request to the infringer (cease & desist – drafted/reviewed by counsel).
2. **Platform notice – DMCA-style structure (17 U.S.C. § 512(c)(3))**. A valid notice must contain:
   - (i) physical or electronic signature of the owner or authorized agent – `[SIGNATURE]`
   - (ii) identification of the copyrighted work – `[WORK: repository / file / commit]`
   - (iii) identification of the infringing material and its location – `[INFRINGING URL(S)]`
   - (iv) contact information – `[CONTACT – via counsel or platform form, not committed here]`
   - (v) good-faith belief statement that use is not authorized
   - (vi) statement that information is accurate and, **under penalty of perjury**, that the sender is authorized
   Warning: § 512(f) – knowingly material misrepresentation creates liability. Consider fair use before sending (Lenz v. Universal).
   GitHub: use its official DMCA form/process (github.com/contact/dmca); counter-notices under § 512(g) may restore content after 10–14 business days unless suit is filed.
3. Counter-notice received → decide with counsel whether to file suit (see US-DRAFT-CIVIL-COMPLAINT-OUTLINE).
4. Litigation → judgment → enforcement (see US-DRAFT-JUDGMENT-JURISDICTION).

## 3. Remedies (orientation)
Injunction (§ 502), actual damages/profits or statutory damages (§ 504), costs/fees (§ 505, if registration timely), impoundment (§ 503).

## 4. Records
Keep an evidence log outside the public repo (screenshots, hashes, dates). Never commit personal data.
