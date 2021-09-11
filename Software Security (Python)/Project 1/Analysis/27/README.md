# Vulnerability: SQL Injection in About Field in Edit Profile (Modifying Password)

- Vulnerability: SQL Injection
- Where: `About` Field in Edit Profile Form
- Impact: Allows a User to change its own Password improperly

## Steps to Reproduce
1. Register with a non existing Username `U` and Password `P`
2. Go to `Update Profile` and update it with `About` = `', PASSWORD = 'newPwd' WHERE USERNAME = 'U' -- `
3. Logout
4. Login with Username = `U` and new Password `newPwd`, verifying that the Login was Successful

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 27`
