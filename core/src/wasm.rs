//! wasm-bindgen rozhraní pro web. Úmyslně jen veřejné operace (rámování, ověření podpisu):
//! v prohlížeči nedržíme dlouhodobé tajné klíče (XSS) – podepisuje autentizátor/passkey nebo
//! mobilní aplikace. Viz docs/security/ZERO-TRUST-ROADMAP.md.
use crate::{framing, sign};
use wasm_bindgen::prelude::*;

fn js(e: crate::CoreError) -> JsError { JsError::new(&e.to_string()) }

#[wasm_bindgen(js_name = signingInput)]
pub fn signing_input(msg: &[u8]) -> Result<Vec<u8>, JsError> { framing::frame(msg).map_err(js) }

#[wasm_bindgen(js_name = parseSigningInput)]
pub fn parse_signing_input(input: &[u8]) -> Result<Vec<u8>, JsError> { framing::unframe(input).map(<[u8]>::to_vec).map_err(js) }

#[wasm_bindgen(js_name = verifyFramed)]
pub fn verify_framed(public: &[u8], msg: &[u8], sig: &[u8]) -> Result<bool, JsError> { sign::verify_framed(public, msg, sig).map_err(js) }
