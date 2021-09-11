# Vulnerability: CSRF in New Post Form Submission

- Vulnerability: CSRF
- Where: `New Post` Form
- Impact: Allows an External Website to Forger a Request to Create a Post Impersonating a User

## Steps to Reproduce
1. Clean DB
2. Register as the Victim with a non existing Username
3. Visit [Attacker Website](http://web.tecnico.ulisboa.pt/ist190774/SSof/ZUfXoyNXyAZj4GFlS9GX.html)
4. Go to `/`, verifying that you did a Public Post with Content "Exploited"

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 12`
