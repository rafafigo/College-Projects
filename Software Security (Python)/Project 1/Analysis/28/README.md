# Vulnerability: SQL Injection in About Field in Edit Profile (Modifying Name)

- Vulnerability: SQL Injection
- Where: `About` Field in Edit Profile Form
- Impact: Allows a User to change its own Name improperly

## Steps to Reproduce
1. Register with a non existing Username `U`
2. Go to `Update Profile` and update it with `About` = `', NAME = 'newN' WHERE USERNAME = 'U' -- `
3. Verify that the `Name` was modified to `newN`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 28`
