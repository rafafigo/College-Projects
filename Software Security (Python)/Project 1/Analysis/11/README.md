# Vulnerability: CSRF in Accept This Friend Form Submission

- Vulnerability: CSRF
- Where: `Accept This Friend` Form
- Impact: Allows an External Website to Forger a Request to Accept Attacker Friend Request Impersonating a User

## Steps to Reproduce
1. Clean DB
2. Register as the Victim with a non existing Username
3. Logout
4. Register as the Attacker with Username = `Attacker`
5. Sends a Friend Request to Victim
6. Logout
7. Login as the Victim
8. Visit [Attacker Website](http://web.tecnico.ulisboa.pt/ist190774/SSof/WsGFKpWmHjfjMWn0fS3f.html)
9. Go to `My Friends` Tab, verifying that the Attacker is now your Friend

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 11`
