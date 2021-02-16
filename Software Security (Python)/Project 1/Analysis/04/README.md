# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Table Names in DB

## Steps to Reproduce
1. Register with a non existing Username
2. Go to `My Friends` Tab
3. Search = `' AND 1 <> 1 UNION SELECT TABLE_NAME, 1, 1, 1, 1 FROM INFORMATION_SCHEMA.TABLES -- `, verifying that you are presented with all Table Names in DB

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 4`
