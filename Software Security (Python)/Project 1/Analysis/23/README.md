# Vulnerability: Stored XSS in the Content Field in the Create Posts Form that can Insert Alerts

- Vulnerability: Stored XSS
- Where: `Content` Field in Create Posts Form
- Impact: Allows a prior Stored Script in a Form Content to Display Alerts to the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `New Post` and create it with `Content` = [DisplaysAlert](../Common/Scripts/XSS/DisplaysAlert.html)
5. Logout
6. Login as the Victim, verifying that an Alert was Displayed

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 23`
