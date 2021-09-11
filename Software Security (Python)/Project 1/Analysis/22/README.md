# Vulnerability: Stored XSS in the Content Field in the Create Posts Form that can Create a Post

- Vulnerability: Stored XSS
- Where: `Content` Field in Create Posts Form
- Impact: Allows a prior Stored Script in a Form Content to Create a Post impersonating the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `New Post` and create it with `Content` = [CreatesPost](../Common/Scripts/XSS/CreatesPost.html)
5. Logout
6. Login as the Victim
7. Go to `/` (Refresh), verifying that a Post was created impersonating the Victim `V`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 22`
