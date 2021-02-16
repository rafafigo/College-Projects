# Vulnerability: SQL Injection in About Field in Edit Profile (Modifying Photo)

- Vulnerability: SQL Injection
- Where: `About` Field in Edit Profile Form
- Impact: Allows a User to change its own Photo improperly

## Steps to Reproduce
1. Register with a non existing Username `U`
2. Go to `Update Profile` and update it with `About` = `', PHOTO = 'newP' WHERE USERNAME = 'U' -- `
3. Verify that the `Photo` was modified to `newP`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 29`
