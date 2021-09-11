# Vulnerability: SQL Injection in About Field in Edit Profile (Take Over a Victim's Account)

- Vulnerability: SQL Injection
- Where: `About` Field in Profile Form
- Impact: Allows an Attacker to Take Over a Victim's Account

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username `U`
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `Update Profile` and update it with `About` = `newA', USERNAME = 'newU', PASSWORD = 'newPwd', NAME = 'newN', PHOTO = 'newP' WHERE USERNAME = 'U' -- `
5. Logout
6. Login as the Victim `V` with new Username = `newU` and new Password `newPwd`
7. Go to `Update Profile`, verifying that all Fields have been modified

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 30`
