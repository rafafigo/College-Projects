# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Users and their Passwords in the System

## Steps to Reproduce
1. Register as the Victim with a non existing Username `X` and Random Password `Y`
2. Logout
3. Register as the Attacker with a non existing Username `W` and Random Password `Z`
4. Go to `My Friends` Tab
5. Search = `' AND 1 <> 1 UNION SELECT USERNAME, 1, NAME, PASSWORD, 1 FROM Users -- `, verifying that you are presented with Attacker's Username `W` and Password `Z`, as well as Victim's Username `X` and Password `Y`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 6`
