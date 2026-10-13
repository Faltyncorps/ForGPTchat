# Off-ramp providers: crypto → card payout (CZK / EUR / USD), usable from CZ/EU

Researched 2026-10-13 from public docs. Fees/coverage change often; always confirm at runtime via each provider's
API (currencies / payment-methods endpoints) and in the partner contract. ELFALL never handles card data, KYC
documents or fiat: the user completes KYC and enters card details **inside the provider's hosted widget**.

| Provider | Card payout | Payout fiat (relevant) | KYC | API / SDK | Fees (public) | EU licensing | Fit for ELFALL |
|---|---|---|---|---|---|---|---|
| **MoonPay** | Yes – Visa Direct cards (fast ≤ 30 min, standard ≤ 2 business days); also SEPA, PayPal | `quoteCurrencyCode` includes **CZK**, EUR, USD | Full KYC in MoonPay | Web SDK / widget `flow: "sell"`, `paymentMethod=credit_debit_card`, sandbox | Not fixed publicly; shown in widget quote | MoonPay Europe B.V. – MiCA CASP (AFM, NL, 30 Dec 2024), passported EEA | **High** – only one with documented CZK card payout |
| **Ramp Network** | Yes – Visa & Mastercard payout-to-card (fast ≤ 30 min, else ≤ 2 days), EEA | EUR (fees priced in EUR for cards), other via conversion | Full KYC | Ramp Instant SDK (web/mobile) | Published per payout method in help centre; partner pricing negotiable | Ramp Swaps (Ireland) Ltd – MiCA CASP (Central Bank of Ireland) | **High** – EU-native, MiCA CASP |
| **Transak** | Yes – Visa Direct / Mastercard, card verified via $0 3DS | EUR, USD (+ many local) | Full KYC | Widget + API, sandbox | Shown per quote | Check current MiCA status in ESMA register before contracting | Medium-High |
| **Banxa** | Yes – card payouts (instant) in selected EU countries **incl. Czech Republic** | EUR | KYC if required | Hosted Checkout + `GET /{partner}/v2/payment-methods/sell`; off-ramp must be enabled per partner | Per quote | EU Internet Ventures B.V. – MiCA CASP (AFM, NL, Oct 2025) | Medium-High (EUR only) |
| **Mercuryo** | Yes – Visa/Mastercard; API `POST /b2b/oor/sell/card` (PCI) or redirect widget | **EUR and USD only** | Mandatory KYC (SumSub; light-KYC up to €699 for buy) | Widget + Partner API, callbacks, sandbox; off-ramp disabled by default | Rate+fee via `GET /v1.6/widget/sell/rate` | Verify MiCA status in ESMA register | Medium |
| **Coinbase Onramp/Offramp (CDP)** | Yes – `paymentMethod: CARD`, cards in 90+ countries incl. EU | EUR, USD (`cashoutCurrency`) | Coinbase account + KYC | REST: `/onramp/v1/sell/config`, `/sell/options`, `/sell/quote`; one-click sell URL | Quote-based | Coinbase Europe – MiCA CASP (Ireland) | Medium – needs user Coinbase account |

Aggregators (Onramper, Alchemy Pay) can route to several of the above but add a layer; consider later.

## Recommendation
1. **MoonPay** first (CZK card payout + MiCA CASP), **Ramp Network** second (EU CASP, card payouts), Banxa as EUR backup.
2. Integrate via hosted widget / redirect only (`shared/.../offramp/` already models providers as stubs).
3. Contract terms to check: who is the "customer" (provider, not ELFALL), Travel Rule handling, refund address,
   partner fee disclosure (MiCA conflicts/fees disclosures), data processing agreement (GDPR Art. 28).

## Sources
- MoonPay sell params (CZK, `credit_debit_card`): https://dev.moonpay.com/widget/off-ramp/customization/parameters
- MoonPay payout timing / Visa Direct: https://support.moonpay.com/en/articles/522834-how-do-i-confirm-locate-or-troubleshoot-my-sell-order-payout-on-moonpay
- MoonPay Europe MiCA CASP: https://paymentproviders.io/registry/mica/254900KXWAZ3B1340G26
- Ramp payout methods: https://support.rampnetwork.com/en/articles/8967-what-determines-the-payout-amount-i-get-when-selling-crypto
- Ramp sell fees: https://support.rampnetwork.com/en/articles/8957-what-are-the-fees-for-selling-crypto
- Ramp MiCA CASP: https://rampnetwork.com/blog/ramp-swaps-micar-authorisation
- Transak card off-ramp: https://support.transak.com/en/articles/7846035-what-is-offramp-and-how-does-it-work-on-transak
- Transak × Visa Direct: https://transak.com/blog/transak-partners-with-visa
- Banxa payment methods (card payouts incl. CZ): https://docs.banxa.com/products/hosted-checkout/docs/reference/supported-payment-methods
- Banxa MiCA licence: https://banxa.com/press/banxa-secures-mica-licence-in-the-netherlands/
- Mercuryo off-ramp widget: https://widget.docs.mercuryo.io/guide/flows/off-ramp
- Mercuryo OOR API: https://api.docs.mercuryo.io/oor-api/guide
- Coinbase Offramp quotes: https://docs.cdp.coinbase.com/onramp/offramp/generating-quotes
- Coinbase payment methods: https://docs.cdp.coinbase.com/onramp/additional-resources/payment-methods
- ESMA interim MiCA register (verify any provider): https://www.esma.europa.eu/esmas-activities/digital-finance-and-innovation/markets-crypto-assets-regulation-mica
