# Vulnerability: SQL Injection in Update Post Form

- Vulnerability: SQL Injection
- Where: `Content` Field in Update Post Form
- Impact: Allows to Post as a different User

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `New Post` Tab and create a Post with Content = `C`
5. Go to `Edit This Post` and edit it with Content = `C', AUTHOR = 'V' WHERE AUTHOR = 'A' AND CONTENT = 'C' -- "`
6. Verify that the Post Author is now the Victim `V`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 13`
