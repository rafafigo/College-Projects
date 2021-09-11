# Vulnerability: CSRF in Add Friend Form Submission

- Vulnerability: CSRF
- Where: `Add Friend` Form
- Impact: Allows an External Website to Forger a Request to Add Attacker as Friend Impersonating a User

## Steps to Reproduce
1. Clean DB
2. Register as the Attacker with Username = `Attacker`
3. Logout
4. Register as the Victim with a non existing Username
5. Visit [Attacker Website](http://web.tecnico.ulisboa.pt/ist190774/SSof/R2Ai2t0bslrVyMxUOUyO.html)
6. Logout
7. Login as the Attacker
8. Go to `Pending Requests` Tab, verifying that the Victim's Friend Request is Pending

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 10`
