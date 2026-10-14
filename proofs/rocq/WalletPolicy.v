(* ELFALL – politika peněženky (model, Rocq 9): tajný klíč nikdy neopustí peněženku,
   off-ramp jen na pevný allowlist, výplata jen na navázanou adresu.
   Model NEpracuje s žádným skutečným klíčem; klíče jsou abstraktní identifikátory. *)
From Stdlib Require Import String List Bool.
Import ListNotations.
Open Scope string_scope.

(* ---------- Klíče a stav ---------- *)
Record Key := { kid : nat; exportable : bool }.

Inductive Provider := MoonPay | Coinbase | Anycoin.

Definition prefix_of (p : Provider) : string :=
  match p with
  | MoonPay  => "https://sell.moonpay.com?"
  | Coinbase => "https://pay.coinbase.com/v3/sell/input?"
  | Anycoin  => "https://www.anycoin.cz/?locale="
  end.

(* allowlist = integrity/critical.json: offramp_provider_url_prefixes *)
Definition allowlist : list string := map prefix_of [MoonPay; Coinbase; Anycoin].

Record State := { keys : list Key; payout : option string }.

Inductive Op :=
  | Generate (n : nat)               (* nový klíč v SE/Keystore/StrongBox *)
  | Sign (n : nat) (msg : string)    (* podpis uvnitř bezpečného prvku *)
  | Delete (n : nat)
  | ExportKey (n : nat)              (* pokus o export – musí selhat *)
  | OfframpOpen (p : Provider) (query : string)
  | BindPayout (dest : string)       (* jednorázové navázání výplatní adresy *)
  | Withdraw (dest : string).

(* Výstupy API. Jediný konstruktor nesoucí tajemství je Secret. *)
Inductive Out :=
  | Signature (n : nat) (msg : string)
  | Url (u : string)
  | Secret (n : nat)
  | Paid (dest : string)
  | Done
  | Denied.

Definition find_key (n : nat) (ks : list Key) : option Key :=
  find (fun k => Nat.eqb (kid k) n) ks.

Definition step (s : State) (o : Op) : State * Out :=
  match o with
  | Generate n => ({| keys := {| kid := n; exportable := false |} :: keys s; payout := payout s |}, Done)
  | Sign n m => match find_key n (keys s) with Some _ => (s, Signature n m) | None => (s, Denied) end
  | Delete n => ({| keys := filter (fun k => negb (Nat.eqb (kid k) n)) (keys s); payout := payout s |}, Done)
  | ExportKey n =>
      match find_key n (keys s) with
      | Some k => if exportable k then (s, Secret n) else (s, Denied)
      | None => (s, Denied)
      end
  | OfframpOpen p q => (s, Url (prefix_of p ++ q))
  | BindPayout d =>
      match payout s with
      | None => ({| keys := keys s; payout := Some d |}, Done)
      | Some _ => (s, Denied)
      end
  | Withdraw d =>
      match payout s with
      | Some d' => if String.eqb d d' then (s, Paid d) else (s, Denied)
      | None => (s, Denied)
      end
  end.

Fixpoint run (s : State) (ops : list Op) : State * list Out :=
  match ops with
  | [] => (s, [])
  | o :: rest => let (s1, out) := step s o in
                 let (s2, outs) := run s1 rest in (s2, out :: outs)
  end.

(* ---------- 1. Nevyjmutelnost ---------- *)
Definition inv (s : State) : Prop := Forall (fun k => exportable k = false) (keys s).

Lemma filter_forall : forall (P : Key -> Prop) f l, Forall P l -> Forall P (filter f l).
Proof.
  intros P f l H; induction H as [|x l Hx Hl IH]; simpl; [constructor|].
  destruct (f x); [constructor; assumption | assumption].
Qed.

Lemma find_key_in : forall n ks k, find_key n ks = Some k -> In k ks.
Proof.
  intros n ks k H; unfold find_key in H.
  apply find_some in H; tauto.
Qed.

Theorem step_preserves : forall s o, inv s -> inv (fst (step s o)).
Proof.
  intros s o H; destruct o; simpl;
    try (destruct (find_key _ _) as [k|]; try destruct (exportable k));
    try (destruct (payout s); try destruct (String.eqb _ _));
    unfold inv in *; simpl;
    first [ constructor; [reflexivity | assumption] | apply filter_forall; assumption | assumption ].
Qed.

Theorem step_never_secret : forall s o n, inv s -> snd (step s o) <> Secret n.
Proof.
  intros s o n H; destruct o as [m|m msg|m|m|p q|d|d]; simpl.
  - discriminate.
  - destruct (find_key m (keys s)); discriminate.
  - discriminate.
  - destruct (find_key m (keys s)) as [k|] eqn:E; [|discriminate].
    destruct (exportable k) eqn:X; [|discriminate].
    apply find_key_in in E. unfold inv in H. rewrite Forall_forall in H.
    rewrite (H k E) in X; discriminate.
  - discriminate.
  - destruct (payout s); discriminate.
  - destruct (payout s) as [b|]; [destruct (String.eqb d b)|]; discriminate.
Qed.

(* Hlavní věta: z bezpečného stavu žádná posloupnost operací API nevydá tajný klíč. *)
Theorem never_exports_secret : forall ops s n,
  inv s -> ~ In (Secret n) (snd (run s ops)) /\ inv (fst (run s ops)).
Proof.
  induction ops as [|o rest IH]; intros s n H; simpl; [tauto|].
  destruct (step s o) as [s1 out] eqn:E.
  assert (H1 : inv s1) by (pose proof (step_preserves s o H) as P; rewrite E in P; exact P).
  assert (Hn : out <> Secret n) by (pose proof (step_never_secret s o n H) as P; rewrite E in P; exact P).
  destruct (run s1 rest) as [s2 outs] eqn:R.
  destruct (IH s1 n H1) as [IH1 IH2]; rewrite R in IH1, IH2; simpl in *.
  split; [intros [C|C]; [congruence|contradiction] | assumption].
Qed.

Corollary initial_safe : forall ops n, ~ In (Secret n) (snd (run {| keys := []; payout := None |} ops)).
Proof. intros; apply never_exports_secret; constructor. Qed.

(* ---------- 2. Off-ramp allowlist ---------- *)
Theorem allowlist_exact :
  allowlist = ["https://sell.moonpay.com?"; "https://pay.coinbase.com/v3/sell/input?"; "https://www.anycoin.cz/?locale="].
Proof. reflexivity. Qed.

Theorem allowlist_has_three : length allowlist = 3 /\ NoDup allowlist.
Proof.
  split; [reflexivity|].
  repeat constructor; simpl; intros H; repeat (destruct H as [H|H]; [discriminate|]); contradiction.
Qed.

Theorem offramp_only_allowlisted : forall s p q u,
  snd (step s (OfframpOpen p q)) = Url u -> exists pre, In pre allowlist /\ u = pre ++ q.
Proof.
  intros s p q u H; simpl in H; inversion H; subst.
  exists (prefix_of p); split; [destruct p; simpl; tauto | reflexivity].
Qed.

(* ---------- 3. Vazba výplatní adresy ---------- *)
Theorem withdraw_only_bound : forall s d d',
  snd (step s (Withdraw d)) = Paid d' -> payout s = Some d /\ d' = d.
Proof.
  intros s d d' H; simpl in H.
  destruct (payout s) as [b|] eqn:P; [|discriminate].
  destruct (String.eqb d b) eqn:E; [|discriminate].
  apply String.eqb_eq in E; subst; inversion H; auto.
Qed.

Lemma step_keeps_binding : forall s o d, payout s = Some d -> payout (fst (step s o)) = Some d.
Proof.
  intros s o d H; destruct o; simpl;
    try (destruct (find_key _ _) as [k|]; try destruct (exportable k));
    try (rewrite H; try destruct (String.eqb _ _)); simpl; auto.
Qed.

(* Jakmile je výplatní adresa navázána, žádná posloupnost operací ji nezmění. *)
Theorem binding_permanent : forall ops s d, payout s = Some d -> payout (fst (run s ops)) = Some d.
Proof.
  induction ops as [|o rest IH]; intros s d H; simpl; [assumption|].
  destruct (step s o) as [s1 out] eqn:E.
  assert (H1 : payout s1 = Some d) by (pose proof (step_keeps_binding s o d H) as P; rewrite E in P; exact P).
  destruct (run s1 rest) as [s2 outs] eqn:R.
  pose proof (IH s1 d H1) as P; rewrite R in P; exact P.
Qed.
