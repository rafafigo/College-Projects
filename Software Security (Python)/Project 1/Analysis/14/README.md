# Vulnerability: SQL Injection in Update Post Form

- Vulnerability: SQL Injection
- Where: `Content` Field in Update Post Form
- Impact: Allows to Change the Author and Content of all Victim's Posts

## Steps to Reproduce
1. Register as the Attacker `A` with a non existing Username
2. Logout
3. Register as the Victim `V` with a non existing Username
4. Go to `New Post` Tab and create 2 Posts with Content = `C`
5. Logout
6. Login as the Attacker
7. Go to `New Post` Tab and create a Random Post
8. Go to `Edit This Post` and edit it with Content = `C', AUTHOR = 'A' WHERE AUTHOR = 'V' -- "`
9. Verify that the Victim's Posts Author is now the Attacker `A`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 14`
