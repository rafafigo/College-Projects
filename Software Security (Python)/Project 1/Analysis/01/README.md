# Vulnerability: SQL Injection in Register (Password) Field

- Vulnerability: SQL Injection
- Where: `Password` Field in Register's Form
- Impact: Allows Users to Register themselves with an Empty Password, being able to Update its Profile but unable to Login later

## Steps to Reproduce
1. Register with a non existing Username and Password = `') -- `
2. Update your Profile with an Empty Password (This Step cannot be Reproduced in the Browser because it causes an Error = `Please fill out this field` to be Displayed)
3. Logout
4. Try to Login with an Empty Password, verifying that it's no longer Possible

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 1`
