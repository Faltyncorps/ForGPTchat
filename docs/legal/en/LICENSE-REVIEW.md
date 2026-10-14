# License review (explanation of the current LICENSE)

> **DRAFT – not legal advice, must be reviewed by a qualified lawyer (CZ/EU; US only if needed).**

The repository's `LICENSE` file is **kept unchanged**. This document only explains it in English.

## What the current license says
- **Type:** proprietary, "All rights reserved". It is not an open-source license.
- **Owner:** Ivo Faltýn (GitHub @ivosicek123), author, developer and owner of ELFALL / Fallcoin / Eleanor.
- **Legal basis:** Czech Copyright Act No. 121/2000 Coll. Software is protected as a computer program (§ 2(2), § 65).
- **Permitted:** reading and reviewing the published source code.
- **Not permitted** without the author's prior written consent: copying, modifying, distributing or otherwise
  using the software, beyond statutory exceptions (e.g. §§ 66 of the Act – backup copy, interoperability).
- **Upstream:** the repository started as a fork of "bitcoin-wallet" (GNU GPL v3). The current tree (eleanor-rebuild /
  FaltynIvo) is a new implementation without upstream code. Any upstream code that is reused stays under GPL v3,
  and its original notices must be kept (see `NOTICE`).
- **Third-party libraries** (Ktor, kotlinx, bitcoin-kmp, libsodium, Leptos, nimbus-jose-jwt, Tink …) keep their own
  licenses (Apache-2.0, MIT, BSD, ISC). The SBOM workflow lists them.

## Points for the lawyer
1. Whether the public GitHub repository together with "All rights reserved" is consistent with GitHub's Terms of Service
   (users may view and fork within GitHub). Consider stating this explicitly.
2. Compatibility of the proprietary license with GPL v3 history: confirm that no GPL code remains (git history, SBOM).
3. Whether a later dual license (e.g. source-available or AGPL plus commercial) is desirable.
4. Trademark protection of the names ELFALL, Fallcoin, FLC, yFLC and Eleanor. They are not registered marks; check
   ÚPV/EUIPO.
