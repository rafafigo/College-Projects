# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Friends Relationships in the System

## Steps to Reproduce
1. Register as the Victim `A` with a non existing Username
2. Logout
3. Register as the Victim `B` with a non existing Username
4. Sends a Friend Request to Victim `A`
5. Logout
6. Login as the Victim `A`
7. Accept Victim's `B` Friend Request
8. Logout
9. Register as the Attacker with a non existing Username
10. Go to `My Friends` Tab
11. Search = `' AND 1 <> 1 UNION SELECT USERNAME1, 1, USERNAME2, ID, 1 FROM Friends -- `, verifying that you are presented with Victim's `A` Friend Relationship with Victim `B`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 9`
