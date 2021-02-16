# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Posts (With Type `PUBLIC`, `FRIENDS` and even `PRIVATE`) in the System

## Steps to Reproduce
1. Register as the Victim with a non existing Username
2. Creates a Private Post `P`
3. Logout
4. Register as the Attacker with a non existing Username
5. Go to `My Friends` Tab
6. Search = `' AND 1 <> 1 UNION SELECT AUTHOR, 1, TYPE, CONTENT, 1 FROM Posts -- `, verifying that you are presented with Victim's Private Post `P`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 7`
