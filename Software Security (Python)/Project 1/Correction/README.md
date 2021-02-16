# Correction Report of Group 10

- Vulnerability 1: SQL Injection in Register (Password) Field. Allows Users to Register themselves with an Empty Password, being able to Update its Profile but unable to Login later. [(Link)](../Analysis/01/README.md)
  - Root Cause: Password not being Sanitized.
  - Changes: Fixed Register Query by Sanitizing Password.

- Vulnerability 2: SQL Injection in Login (Username) Field. Allows to Login as Any User without knowing their Passwords. [(Link)](../Analysis/02/README.md)
  - Root Cause: Username not being Sanitized.
  - Changes: Fixed Register Query by Sanitizing Username.

- Vulnerability 3: SQL Injection in My Friends (Search) Field. Allows Any User to List all Columns in a Given Table in DB. [(Link)](../Analysis/03/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 4: SQL Injection in My Friends (Search) Field. Allows Any User to List all Table Names in DB. [(Link)](../Analysis/04/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 5: SQL Injection in My Friends (Search) Field. Allows Any User to List all Users in the System. [(Link)](../Analysis/05/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 6: SQL Injection in My Friends (Search) Field. Allows Any User to List all Users and their Passwords in the System. [(Link)](../Analysis/06/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 7: SQL Injection in My Friends (Search) Field. Allows Any User to List all Posts (With Type `PUBLIC`, `FRIENDS` and even `PRIVATE`) in the System. [(Link)](../Analysis/07/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 8: SQL Injection in My Friends (Search) Field. Allows Any User to List all Friends Requests in the System. [(Link)](../Analysis/08/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 9: SQL Injection in My Friends (Search) Field. Allows Any User to List all Friends Relationships in the System. [(Link)](../Analysis/09/README.md)
  - Root Cause: My Friends Search not being Sanitized.
  - Changes: Fixed My Friends Search Query by Sanitizing it.

- Vulnerability 10: CSRF in Add Friend Form Submission. Allows an External Website to Forger a Request to Add Attacker as Friend Impersonating a User. [(Link)](../Analysis/10/README.md)
  - Root Cause: Add Friend Form not containing a Hidden Nonce Input.
  - Changes: Fixed Add Friend Form by adding a CSRF Token to it.

- Vulnerability 11: CSRF in Accept This Friend Form Submission. Allows an External Website to Forger a Request to Accept Attacker Friend Request Impersonating a User. [(Link)](../Analysis/11/README.md)
  - Root Cause: Accept This Friend Form not containing a Hidden Nonce Input.
  - Changes: Fixed Accept This Friend Form by adding a CSRF Token to it.

- Vulnerability 12: CSRF in New Post Form Submission. Allows an External Website to Forger a Request to Create a Post Impersonating a User. [(Link)](../Analysis/12/README.md)
  - Root Cause: New Post Form not containing a Hidden Nonce Input.
  - Changes: Fixed New Post Form by adding a CSRF Token to it.

- Vulnerability 13: SQL Injection in Update Post Form. Allows to Post as a different User. [(Link)](../Analysis/13/README.md)
  - Root Cause: Post Content not being Sanitized.
  - Changes: Fixed Update Post Query by Sanitizing Post Content.

- Vulnerability 14: SQL Injection in Update Post Form. Allows to Change the Author and Content of all Victim's Posts. [(Link)](../Analysis/14/README.md)
  - Root Cause: Post Content not being Sanitized.
  - Changes: Fixed Update Post Query by Sanitizing Post Content.

- Vulnerability 15: Stored XSS in the Name Field of Edit Profile that can Create a Post. [(Link)](../Analysis/15/README.md)
  - Root Cause: Name Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 16: Stored XSS in the Name Field of Edit Profile that can Insert Alerts. [(Link)](../Analysis/16/README.md)
  - Root Cause: Name Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 17: Stored XSS in the Name Field of Edit Profile that Accepts Friend Requests. [(Link)](../Analysis/17/README.md)
  - Root Cause: Name Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 18: Stored XSS in the Name Field of Edit Profile that Makes Friend Requests. [(Link)](../Analysis/18/README.md)
  - Root Cause: Name Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 19: Stored XSS in the About Field of Edit Profile that can Create a Post. [(Link)](../Analysis/19/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 20: Stored XSS in the About Field of Edit Profile that can Insert Alerts. [(Link)](../Analysis/20/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 21: Stored XSS in the About Field of Edit Profile that Accepts Friend Requests. [(Link)](../Analysis/21/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 22: Stored XSS in the Content Field in the Create Posts Form that can Create a Post. [(Link)](../Analysis/22/README.md)
  - Root Cause: Posts Content not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 23: Stored XSS in the Content Field in the Create Posts Form that can Insert Alerts. [(Link)](../Analysis/23/README.md)
  - Root Cause: Posts Content not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 24: Stored XSS in the Content Field in the Create Posts Form that Accepts Friend Requests. [(Link)](../Analysis/24/README.md)
  - Root Cause: Posts Content not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 25: Stored XSS in the Content Field in the Create Posts Form that Makes Friend Requests. [(Link)](../Analysis/25/README.md)
  - Root Cause: Posts Content not being Sanitized.
  - Changes: Fixed Templates by enabling Auto Escape on it.

- Vulnerability 26: SQL Injection in About Field in Edit Profile (Modifying Username). [(Link)](../Analysis/26/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing About Field.

- Vulnerability 27: SQL Injection in About Field in Edit Profile (Modifying Password). [(Link)](../Analysis/27/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing About Field.

- Vulnerability 28: SQL Injection in About Field in Edit Profile (Modifying Name). [(Link)](../Analysis/28/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing About Field.

- Vulnerability 29: SQL Injection in About Field in Edit Profile (Modifying Photo). [(Link)](../Analysis/29/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing About Field.

- Vulnerability 30: SQL Injection in About Field in Edit Profile (Take Over a Victim's Account). [(Link)](../Analysis/30/README.md)
  - Root Cause: About Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing About Field.

- Vulnerability 31: SQL Injection & Stored XSS Attack in About Field in Edit Profile (Store Script in Photo Field). [(Link)](../Analysis/31/README.md)
  - Root Cause: Photo Field not being Sanitized.
  - Changes: Fixed Update Profile Query by Sanitizing Photo Field.

## Patches

- XSS Vulnerabilies. [(Patch)](Patches/0001-XSS-Vulnerabilities-Mitigated.patch)
- CSRF Vulnerabilies. [(Patch)](Patches/0002-CSRF-Vulnerabilities-Mitigated.patch)
- SQL Injection Vulnerabilies. [(Patch)](Patches/0004-SQLi-Vulnerabilities-Mitigated.patch)

## New Vulnerabilities

- Vulnerability: Possible XSS By Attribute Injection. [(Patch)](Patches/0003-Possible-XSS-By-Attribute-Injection-Mitigated.patch)
  - Root Cause: Some Attributes not quoted.
  - Changes: Fixed Templates by quoting not quoted Attributes.

- Vulnerability: Unsecure User's Password Storage. [(Patch)](Patches/0005-Passwords-Securely-Stored-in-DB.patch)
  - Root Cause: User's Password stored in Plaintext.
  - Changes: Fixed User's Password Storage by Hashing it.

- Vulnerability: Illegal Queries. Allows to Reveal Relevant Data from DB. [(Patch)](Patches/0006-Illegal-Queries-Mitigated.patch)
  - Root Cause: Too detailed Error Messages.
  - Changes: Fixed Error Messages by decreasing its Level of Detail.

## Notes

- New Attacker Websites (Accessing http://localhost:5000/):
  - To Vulnerability 10: [(Here)](http://web.tecnico.ulisboa.pt/ist190774/SSof/Local/R2Ai2t0bslrVyMxUOUyO.html)
  - To Vulnerability 11: [(Here)](http://web.tecnico.ulisboa.pt/ist190774/SSof/Local/WsGFKpWmHjfjMWn0fS3f.html)
  - To Vulnerability 12: [(Here)](http://web.tecnico.ulisboa.pt/ist190774/SSof/Local/ZUfXoyNXyAZj4GFlS9GX.html)

