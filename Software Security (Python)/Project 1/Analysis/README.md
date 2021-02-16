# Analysis Report

Group Image Available [(Here)](http://1f940ab5ce93aff9942913dead9e2fe9c12b2bdb7076be3c73ab6ba9d563.project.ssof.rnl.tecnico.ulisboa.pt)

- Vulnerability 1: SQL Injection in Register (Password) Field. Allows Users to Register themselves with an Empty Password, being able to Update its Profile but unable to Login later. [(Link)](01/README.md)
- Vulnerability 2: SQL Injection in Login (Username) Field. Allows to Login as Any User without knowing their Passwords. [(Link)](02/README.md)
- Vulnerability 3: SQL Injection in My Friends (Search) Field. Allows Any User to List all Columns in a Given Table in DB. [(Link)](03/README.md)
- Vulnerability 4: SQL Injection in My Friends (Search) Field. Allows Any User to List all Table Names in DB. [(Link)](04/README.md)
- Vulnerability 5: SQL Injection in My Friends (Search) Field. Allows Any User to List all Users in the System. [(Link)](05/README.md)
- Vulnerability 6: SQL Injection in My Friends (Search) Field. Allows Any User to List all Users and their Passwords in the System. [(Link)](06/README.md)
- Vulnerability 7: SQL Injection in My Friends (Search) Field. Allows Any User to List all Posts (With Type `PUBLIC`, `FRIENDS` and even `PRIVATE`) in the System. [(Link)](07/README.md)
- Vulnerability 8: SQL Injection in My Friends (Search) Field. Allows Any User to List all Friends Requests in the System. [(Link)](08/README.md)
- Vulnerability 9: SQL Injection in My Friends (Search) Field. Allows Any User to List all Friends Relationships in the System. [(Link)](09/README.md)
- Vulnerability 10: CSRF in Add Friend Form Submission. Allows an External Website to Forger a Request to Add Attacker as Friend Impersonating a User. [(Link)](10/README.md)
- Vulnerability 11: CSRF in Accept This Friend Form Submission. Allows an External Website to Forger a Request to Accept Attacker Friend Request Impersonating a User. [(Link)](11/README.md)
- Vulnerability 12: CSRF in New Post Form Submission. Allows an External Website to Forger a Request to Create a Post Impersonating a User. [(Link)](12/README.md)
- Vulnerability 13: SQL Injection in Update Post Form. Allows to Post as a different User. [(Link)](13/README.md)
- Vulnerability 14: SQL Injection in Update Post Form. Allows to Change the Author and Content of all Victim's Posts. [(Link)](14/README.md)
- Vulnerability 15: Stored XSS in the Name Field of Edit Profile that can Create a Post. [(Link)](15/README.md)
- Vulnerability 16: Stored XSS in the Name Field of Edit Profile that can Insert Alerts. [(Link)](16/README.md)
- Vulnerability 17: Stored XSS in the Name Field of Edit Profile that Accepts Friend Requests. [(Link)](17/README.md)
- Vulnerability 18: Stored XSS in the Name Field of Edit Profile that Makes Friend Requests. [(Link)](18/README.md)
- Vulnerability 19: Stored XSS in the About Field of Edit Profile that can Create a Post. [(Link)](19/README.md)
- Vulnerability 20: Stored XSS in the About Field of Edit Profile that can Insert Alerts. [(Link)](20/README.md)
- Vulnerability 21: Stored XSS in the About Field of Edit Profile that Accepts Friend Requests. [(Link)](21/README.md)
- Vulnerability 22: Stored XSS in the Content Field in the Create Posts Form that can Create a Post. [(Link)](22/README.md)
- Vulnerability 23: Stored XSS in the Content Field in the Create Posts Form that can Insert Alerts. [(Link)](23/README.md)
- Vulnerability 24: Stored XSS in the Content Field in the Create Posts Form that Accepts Friend Requests. [(Link)](24/README.md)
- Vulnerability 25: Stored XSS in the Content Field in the Create Posts Form that Makes Friend Requests. [(Link)](25/README.md)
- Vulnerability 26: SQL Injection in About Field in Edit Profile (Modifying Username). [(Link)](26/README.md)
- Vulnerability 27: SQL Injection in About Field in Edit Profile (Modifying Password). [(Link)](27/README.md)
- Vulnerability 28: SQL Injection in About Field in Edit Profile (Modifying Name). [(Link)](28/README.md)
- Vulnerability 29: SQL Injection in About Field in Edit Profile (Modifying Photo). [(Link)](29/README.md)
- Vulnerability 30: SQL Injection in About Field in Edit Profile (Take Over a Victim's Account). [(Link)](30/README.md)
- Vulnerability 31: SQL Injection & Stored XSS Attack in About Field in Edit Profile (Store Script in Photo Field). [(Link)](31/README.md)

## To Run:
Commands:
- Check [Notes](#Notes)
- `pipenv install`
- `./runVulnN.sh {1..31}`

## Notes
- To Run XSS & CSRF Scripts it's necessary to Download a Google Chrome Driver [(Here)](https://sites.google.com/a/chromium.org/chromedriver/downloads)
- And insert it in the `PATH`: `mv $HOME/chromedriver $HOME/.local/bin`

