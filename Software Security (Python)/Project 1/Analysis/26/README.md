# Vulnerability: SQL Injection in About Field in Edit Profile (Modifying Username)

- Vulnerability: SQL Injection
- Where: `About` Field in Edit Profile Form
- Impact: Allows a User to change its own Username improperly

## Steps to Reproduce
1. Register with a non existing Username `U` and Password `P`
2. Go to `Update Profile` and update it with `About` = `', USERNAME = 'newU' WHERE USERNAME = 'U' -- `
3. Logout
4. Login with new Username = `newU` and Password `P`, verifying that the Login was Successful

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 26`
