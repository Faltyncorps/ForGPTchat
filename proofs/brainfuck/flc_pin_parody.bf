ELFALL FLC pin check parody (NOT production; just art)
Reads one byte; if it equals the char '7' prints "OK" else prints "NO"
Note: comments in BF must avoid the eight command chars; so no dots or commas or dashes here

,                       read input into c0
>+++++++[<------->-]    c0 = c0 minus 49
<------                 c0 = c0 minus 6 (total minus 55 = ascii 7)
>+<                     c1 = 1 (else flag)
[                       if c0 != 0 (mismatch)
  >-<                   clear flag
  [-]                   clear c0
  ++++++++[>>++++++++++<<-]>>--.  print N (78)
  +.                    print O (79)
  [-]<<
]
>[                      flag still set (match)
  -
  >++++++++[<++++++++++>-]<-.     print O (79)
  ----.                 print K (75)
  [-]
]
++++++++++.             newline
