# Vulnerability: SQL Injection in Login (Username) Field

- Vulnerability: SQL Injection
- Where: `Username` Field in Login's Form
- Impact: Allows to Login as Any User without knowing their Passwords

## Steps to Reproduce
1. Register with a non existing Username `X`
2. Logout
3. Login with Username = `X' -- ` and a Random Password, verifying that you are now Logged in as the User `X`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 2`
