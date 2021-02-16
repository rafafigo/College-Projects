# Vulnerability: Stored XSS in the Content Field in the Create Posts Form that Makes Friend Requests

- Vulnerability: Stored XSS
- Where: `Content` Field in Create Posts Form
- Impact: Allows a prior Stored Script in a Form Content to Make a Friend Request impersonating the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `New Post` and create it with `Content` = [MakesFriendRequest](../Common/Scripts/XSS/MakesFriendRequest.html)
5. Logout
6. Login as the Victim
7. Logout
8. Login as the Attacker
9. Go to `Pending Requests`, verifying that the Victim `V` is in it

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 25`
